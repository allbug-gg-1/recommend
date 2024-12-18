package com.sofm.recommend.application.service;

import com.sofm.recommend.application.exception.ServiceException;
import com.sofm.recommend.application.service.modeHandler.ModeHandler;
import com.sofm.recommend.application.service.rankHandler.RankHandler;
import com.sofm.recommend.application.service.ruleHandler.DefaultRuleHandler;
import com.sofm.recommend.common.code.RecommendType;
import com.sofm.recommend.common.constants.Constants;
import com.sofm.recommend.common.dto.ProvinceCityDto;
import com.sofm.recommend.common.dto.RecommendContext;
import com.sofm.recommend.common.dto.RecommendResult;
import com.sofm.recommend.common.dto.UserMab;
import com.sofm.recommend.common.status.ResponseStatus;
import com.sofm.recommend.common.utils.DateUtils;
import com.sofm.recommend.common.utils.JSONUtils;
import com.sofm.recommend.domain.recommendLog.service.RecommendLogService;
import com.sofm.recommend.domain.user.entity.AppUser;
import com.sofm.recommend.domain.user.service.UserService;
import com.sofm.recommend.infrastructure.geo.GeoIPService;
import com.sofm.recommend.infrastructure.redis.RedisHelper;
import com.sofm.recommend.ui.dto.request.RecommendRequest;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.sofm.recommend.infrastructure.redis.RedisConstants.*;

@Service
public class RecommendApplicationService {
    private final RedisHelper redisHelper;
    private final Map<String, ModeHandler> modeHandlers;
    private final Map<String, RankHandler> rankHandlers;
    private final UserService userService;
    private final DefaultRuleHandler ruleHandler;
    private final GeoIPService geoIPService;
    private final RecommendLogService recommendLogService;
    private final Random random = new Random();

    public RecommendApplicationService(RedisHelper redisHelper, Map<String, ModeHandler> modeHandlers, Map<String, RankHandler> rankHandlers, UserService userService, DefaultRuleHandler ruleHandler, GeoIPService geoIPService, RecommendLogService recommendLogService) {
        this.redisHelper = redisHelper;
        this.modeHandlers = modeHandlers;
        this.rankHandlers = rankHandlers;
        this.userService = userService;
        this.ruleHandler = ruleHandler;
        this.geoIPService = geoIPService;
        this.recommendLogService = recommendLogService;
    }

    public RecommendResult getRecommendResults(RecommendRequest request) throws ServiceException {
        AppUser appUser = userService.loadUser(request.getUser_id());
        if (appUser == null) {
            throw ServiceException.of(ResponseStatus.INTERNAL_SERVER_ERROR, "用户不存在");
        }
        ProvinceCityDto provinceCityDto = geoIPService.getLocationByIP(request.getIp());
        Map<String, Set<String>> channelItems = new HashMap<>();
        RecommendContext context = new RecommendContext(appUser.getRecordId(), request.getIp(), provinceCityDto);
        RecommendType recommendType;
        if (!request.isNear_by()) {
            if (random.nextDouble() < Constants.EPSILON_RATE) { // 探索
                recommendType = RecommendType.Explore;
            } else {  // 利用
                recommendType = RecommendType.Exploit;
                loadUserMab(context);
            }
        } else {
            recommendType = RecommendType.NearBy;
        }
        context.setRecommendType(recommendType);
        dynamicRecall(context, channelItems, 0);
        List<Integer> results = filterWatched(context, channelItems);
        results = qualityRank(context, results);
        results = cacheRankResults(context.getUserId(), results);
        results = ruleRank(results);
        String logId = generateRecommendLog(context, results.stream().map(String::valueOf).toList(), channelItems);
        return RecommendResult.of(logId, results);
    }

    public void dynamicRecall(RecommendContext context, Map<String, Set<String>> channelItems, int loadTime) {
        int totalCount = 0;
        for (Map.Entry<String, ModeHandler> modeHandlerEntry : modeHandlers.entrySet()) {
            ModeHandler modeHandler = modeHandlerEntry.getValue();
            if (modeHandler.getRecommendType() == context.getRecommendType()) {
                int size = modeHandler.recall(context, channelItems, loadTime);
                totalCount += size;
            }
        }
        if (totalCount < Constants.minRecommendRecallCount) {
            if (loadTime <= 2) {
                loadTime++;
                dynamicRecall(context, channelItems, loadTime);
            }
        }
    }

    public List<Integer> filterWatched(RecommendContext context, Map<String, Set<String>> channelItems) {
        Set<String> totalItems = new HashSet<>();
        for (String key : channelItems.keySet()) {
            totalItems.addAll(channelItems.get(key));
        }
        int month = DateUtils.getMonth();
        int half = DateUtils.isFirstHalfOfMonth() ? 1 : 2;
        String currentKey = user_month_exposure.replace("{month}", String.valueOf(month)).replace("{half}", String.valueOf(half)).replace("{user_id}", String.valueOf(context.getUserId()));
        String lastKey = getString(context, half, month);
        List<String> nonExists = redisHelper.getNonExistentValues(List.of(lastKey, currentKey), totalItems.stream().toList());
        return nonExists.stream().map(Integer::parseInt).toList();
    }

    private static String getString(RecommendContext context, int half, int month) {
        String lastKey;
        if (half == 1) {
            int lastMonth = (month - 1) == 0 ? 12 : (month - 1);
            int lastHalf = 2;
            lastKey = user_month_exposure.replace("{month}", String.valueOf(lastMonth)).replace("{half}", String.valueOf(lastHalf)).replace("{user_id}", String.valueOf(context.getUserId()));
        } else {
            int lastHalf = 1;
            lastKey = user_month_exposure.replace("{month}", String.valueOf(month)).replace("{half}", String.valueOf(lastHalf)).replace("{user_id}", String.valueOf(context.getUserId()));
        }
        return lastKey;
    }

    public List<Integer> qualityRank(RecommendContext context, List<Integer> items) {
        List<Integer> resultIds = new ArrayList<>();
        for (Map.Entry<String, RankHandler> entry : rankHandlers.entrySet()) {
            resultIds = entry.getValue().rank(context, items);
        }
        return resultIds;
    }

    public List<Integer> ruleRank(List<Integer> rankedIds) {
        return ruleHandler.process(rankedIds);
    }


    public List<Integer> cacheRankResults(int userId, List<Integer> resultIds) {
        List<Integer> needCaches = resultIds.subList(0, Math.min(50, resultIds.size()));
        String cacheKey = user_rank_cache_pool.replace("{user_id}", String.valueOf(userId));
        redisHelper.addToZSetWithPipeline(cacheKey, needCaches.stream().map(String::valueOf).toList(), System.currentTimeMillis());
        return new ArrayList<>(resultIds.subList(0, Math.min(20, resultIds.size())));
    }

    public String generateRecommendLog(RecommendContext context, List<String> results, Map<String, Set<String>> channelItems) {
        UUID uuid = UUID.randomUUID();
        for (String key : channelItems.keySet()) {
            Set<String> channelItem = channelItems.get(key);
            channelItem.retainAll(results);
        }
        recommendLogService.saveRecommendLog(uuid.toString(), context.getUserId(), context.getRecommendType(), channelItems);
        return uuid.toString();
    }

    public void loadUserMab(RecommendContext context) {
        UserMab userMab;
        String userMabKey = user_mab.replace("{user_id}", String.valueOf(context.getUserId()));
        if (redisHelper.hasKey(userMabKey)) {
            String userMabStr = (String) redisHelper.getValue(userMabKey);
            userMab = JSONUtils.fromJson(userMabStr, UserMab.class);
        } else {
            userMab = new UserMab();
        }
        context.setUserMab(userMab);
    }
}
