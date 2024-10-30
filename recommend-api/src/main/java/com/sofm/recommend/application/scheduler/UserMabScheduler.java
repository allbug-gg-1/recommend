package com.sofm.recommend.application.scheduler;

import com.sofm.recommend.application.config.UserMabWeightConfig;
import com.sofm.recommend.common.constants.Constants;
import com.sofm.recommend.common.dto.UserMab;
import com.sofm.recommend.common.utils.BeanUtils;
import com.sofm.recommend.common.utils.JSONUtils;
import com.sofm.recommend.infrastructure.redis.RedisConstants;
import com.sofm.recommend.infrastructure.redis.RedisHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class UserMabScheduler {

    private final RedisHelper redisHelper;

    public UserMabScheduler(RedisHelper redisHelper) {
        this.redisHelper = redisHelper;
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
    public void decayWeights() {
        if (!redisHelper.hasKey(RedisConstants.recent_run_user)) {
            return;
        }
        Set<Object> inactiveUsers = getInactiveUsers();
        if (inactiveUsers.isEmpty()) {
            return;
        }
        List<String> keys = inactiveUsers.stream().map(String::valueOf).toList();
        List<Object> userMabList = redisHelper.batchGetKeys(keys.stream().map(record -> RedisConstants.user_mab.replace("{user_id}", record)).toList());
        List<String> originalUser = new ArrayList<>();
        Map<String, String> updates = new HashMap<>();
        for (int i = 0; i < userMabList.size(); i++) {
            Object userMabStr = userMabList.get(i);
            String uid = keys.get(i);
            UserMab userMab = JSONUtils.fromJson(userMabStr.toString(), UserMab.class);
            decayWeights(uid, userMab, originalUser, updates);
        }
        if (!originalUser.isEmpty()) {
            redisHelper.removeZSetValues(RedisConstants.recent_run_user, originalUser.toArray());
            List<String> originMabKeys = originalUser.stream().map(record -> RedisConstants.user_mab.replace("{user_id}", record)).toList();
            redisHelper.batchDeleteKeys(originMabKeys);
        }
        if (!updates.isEmpty()) {
            redisHelper.batchSetKeys(updates);
        }
    }

    private void decayWeights(String uid, UserMab userMab, List<String> originalUser, Map<String, String> updates) {
        Map<String, Object> userMabMap = BeanUtils.convertToMap(userMab);
        boolean originFlag = true;
        for (Map.Entry<String, Object> entry : userMabMap.entrySet()) {
            String channel = entry.getKey();
            double currentWeight = (double) entry.getValue();
            double defaultWeight = UserMabWeightConfig.getChannelDefaultWeight(channel);
            if (currentWeight > defaultWeight) {
                currentWeight = currentWeight - (currentWeight - defaultWeight) * Constants.user_mab_attenuation_alpha;
            } else if (currentWeight < defaultWeight) {
                currentWeight = currentWeight + (defaultWeight - currentWeight) * Constants.user_mab_attenuation_alpha;
            }
            if (Math.abs(currentWeight - defaultWeight) < 0.01d) { // 避免浮点数误差
                currentWeight = defaultWeight;
            }
            if (currentWeight != defaultWeight) {
                originFlag = false;
            }
            // 更新当前权重
            userMabMap.put(channel, currentWeight);
        }
        if (originFlag) {
            originalUser.add(uid); // 衰减到正常值了
        } else {
            updates.put(RedisConstants.user_mab.replace("{user_id}", uid), JSONUtils.toJson(userMabMap)); // 个性化的userMab
        }

    }

    public Set<Object> getInactiveUsers() {
        long oneHourAgoTimestamp = System.currentTimeMillis() - 3600000; // 一小时前的时间戳
        return redisHelper.rangeByScore(RedisConstants.recent_run_user, 0, oneHourAgoTimestamp);
    }
}
