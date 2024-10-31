package com.sofm.recommend.domain.user.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.ImmutableSet;
import com.sofm.recommend.application.config.UserMabWeightConfig;
import com.sofm.recommend.common.code.RecommendType;
import com.sofm.recommend.common.code.UgcType;
import com.sofm.recommend.common.constants.Constants;
import com.sofm.recommend.common.dto.MessageDto;
import com.sofm.recommend.common.dto.UserMab;
import com.sofm.recommend.common.utils.DateUtils;
import com.sofm.recommend.common.utils.JSONUtils;
import com.sofm.recommend.domain.note.entity.NoteMongoEntity;
import com.sofm.recommend.domain.recommendLog.entity.RecommendLog;
import com.sofm.recommend.domain.user.entity.AppUser;
import com.sofm.recommend.domain.note.service.NoteService;
import com.sofm.recommend.domain.recommendLog.service.RecommendLogService;
import com.sofm.recommend.infrastructure.mysql.repository.AppUserRepositoryImpl;
import com.sofm.recommend.infrastructure.redis.RedisHelper;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.sofm.recommend.infrastructure.redis.RedisConstants.*;

@Service
public class UserService {

    private final static Set<String> interactiveAction = ImmutableSet.of(UgcType.CLICK.getCode(), UgcType.LIKE.getCode(), UgcType.SHARE.getCode(), UgcType.COMMENT.getCode());
    private final static Set<String> unLikeAction = ImmutableSet.of(UgcType.DISLIKE_USER.getCode(), UgcType.DISLIKE_PET.getCode(), UgcType.DISLIKE_ITEM.getCode());

    private final RedisHelper redisHelper;
    private final NoteService noteService;
    private final AppUserRepositoryImpl appUserRepository;
    private final RecommendLogService recommendLogService;
    private final Cache<Integer, AppUser> cache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(5))
            .build();

    public UserService(RedisHelper redisHelper, NoteService noteService, AppUserRepositoryImpl appUserRepository, RecommendLogService recommendLogService) {
        this.redisHelper = redisHelper;
        this.noteService = noteService;
        this.appUserRepository = appUserRepository;
        this.recommendLogService = recommendLogService;
    }

    public void processMsg(MessageDto dto) {
        if (dto.getType().equals(UgcType.VIEWS.getCode())) {
            processViewMsg(dto);
        } else {
            if (interactiveAction.contains(dto.getType())) {
                processInteractiveMsg(dto);
                updateMabWeight(dto.getUserId(), dto.getItemsId().get(0), dto.getRecommend_id(), dto.getType());
            } else if (unLikeAction.contains(dto.getType())) {
                processUnLikeMsg(dto);
                updateMabWeight(dto.getUserId(), dto.getItemsId().get(0), dto.getRecommend_id(), dto.getType());
            }
        }

    }

    public void processViewMsg(MessageDto dto) {
        int month = DateUtils.getMonth();
        int half = DateUtils.isFirstHalfOfMonth() ? 1 : 2;
        String userKey = user_month_exposure.replace("{month}", String.valueOf(month)).replace("{half}", String.valueOf(half)).replace("{user_id}", String.valueOf(dto.getUserId()));
        redisHelper.addToBloomFilterWithLua(userKey, dto.getItemsId());
        redisHelper.expire(userKey, 60, TimeUnit.DAYS);
    }

    public void processInteractiveMsg(MessageDto dto) {
        List<Integer> itemsId = dto.getItemsId().stream().map(Integer::parseInt).toList();
        List<NoteMongoEntity> pNotes = noteService.listPNoteByRecordIds(itemsId);
        Date now = new Date();
        AtomicBoolean adopt = new AtomicBoolean(false);
        Set<String> allTopics = new HashSet<>();
        Set<String> allUsers = new HashSet<>();
        if (!pNotes.isEmpty()) {
            pNotes.forEach(record -> {
                if (record.getTopic() != null) {
                    allTopics.addAll(record.getTopic());
                }
                if (record.getUid() != dto.getUserId()) {
                    allUsers.add(String.valueOf(record.getUid()));
                }
                if (record.isWaitAdopt()) {
                    adopt.set(true);
                }
            });
        }
        String itemKey = user_recent_item.replace("{user_id}", String.valueOf(dto.getUserId()));
        redisHelper.addToZSetWithPipeline(itemKey, dto.getItemsId(), now.getTime());
        if (!allUsers.isEmpty()) {
            String publisherKey = user_recent_interaction_publisher.replace("{user_id}", String.valueOf(dto.getUserId()));
            redisHelper.addToZSetWithPipeline(publisherKey, new ArrayList<>(allUsers), now.getTime());
        }
        if (!allTopics.isEmpty()) {
            String topicKey = user_recent_interaction_topic.replace("{user_id}", String.valueOf(dto.getUserId()));
            redisHelper.addToZSetWithPipeline(topicKey, new ArrayList<>(allTopics), now.getTime());
        }
        if (adopt.get()) {
            redisHelper.addZSet(adopt_user, String.valueOf(dto.getUserId()), now.getTime());
        }
    }

    public void processUnLikeMsg(MessageDto dto) {
        if (dto.getType().equals(UgcType.DISLIKE_ITEM.getCode())) {
            String itemKey = user_dislike_items.replace("{user_id}", String.valueOf(dto.getUserId()));
            redisHelper.addToBloomFilterWithLua(itemKey, dto.getItemsId());
        } else if (dto.getType().equals(UgcType.DISLIKE_PET.getCode())) {
            String petKey = user_dislike_pet.replace("{user_id}", String.valueOf(dto.getUserId()));
            dto.getItemsId().forEach(record -> redisHelper.leftPush(petKey, record));
        } else {
            String userKey = user_dislike_user.replace("{user_id}", String.valueOf(dto.getUserId()));
            dto.getItemsId().forEach(record -> redisHelper.leftPush(userKey, record));
        }
    }

    public Optional<AppUser> getUser(int recordId) {
        return appUserRepository.getByRecordId(recordId);
    }

    public AppUser loadUser(int recordId) {
        AppUser appUser = cache.getIfPresent(recordId);
        if (appUser == null) {
            Optional<AppUser> user = getUser(recordId);
            if (user.isPresent()) {
                appUser = user.get();
                cache.put(recordId, appUser);
            }
        }
        return appUser;
    }

    public void updateMabWeight(int uid, String itemId, String rid, String action) {
        String userMabKey = user_mab.replace("{user_id}", String.valueOf(uid));
        UserMab userMab;
        if (redisHelper.hasKey(userMabKey)) {
            String userMabStr = (String) redisHelper.getValue(userMabKey);
            userMab = JSONUtils.fromJson(userMabStr, UserMab.class);
        } else {
            userMab = new UserMab();
        }
        if (rid == null) { // 没有推荐ID
            return;
        }
        RecommendLog log = recommendLogService.getLogById(rid);
        if (log == null) {
            return;
        }
        if (log.getRecommendType() != RecommendType.Exploit.getCode()) {
            return;
        }
        Map<String, Set<String>> channelItems = log.getChannelItem();
        List<String> channelKeys = new ArrayList<>(10);
        for (Map.Entry<String, Set<String>> channel : channelItems.entrySet()) {
            if (channel.getValue().contains(itemId)) {
                channelKeys.add(channel.getKey());
            }
        }
        if (channelKeys.isEmpty()) {
            return;
        }
        double weightDelta = UgcType.getWeightByCode(action) / channelKeys.size();
        for (String channelKey : channelKeys) {
            double oldWeight = userMab.getChannelWeight(channelKey);

            // 基于用户的交互，更新 currentWeight
            double currentWeight = oldWeight + weightDelta;
            // 使用指数加权平均更新权重，平滑过渡
            double newWeight = Constants.user_mab_promote_alpha * currentWeight + (1 - Constants.user_mab_promote_alpha) * (oldWeight + weightDelta);
            int size = (int) (UserMabWeightConfig.DEFAULT_CHANNEL_COUNT * newWeight);
            if (size > UserMabWeightConfig.MAX_CHANNEL_COUNT) { // 超过最大渠道数量
                newWeight = (double) UserMabWeightConfig.MAX_CHANNEL_COUNT / UserMabWeightConfig.DEFAULT_CHANNEL_COUNT;
            } else if (size <= UserMabWeightConfig.MIN_CHANNEL_COUNT) { // 低于最大渠道数量
                newWeight = (double) UserMabWeightConfig.MIN_CHANNEL_COUNT / UserMabWeightConfig.DEFAULT_CHANNEL_COUNT;
            }
            userMab.updateChannelWeight(channelKey, newWeight);
        }
        redisHelper.setValue(userMabKey, JSONUtils.toJson(userMab));
        redisHelper.addZSet(recent_run_user, String.valueOf(uid), System.currentTimeMillis());
    }
}
