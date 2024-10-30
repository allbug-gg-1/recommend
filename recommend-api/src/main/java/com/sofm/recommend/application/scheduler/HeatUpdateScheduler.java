package com.sofm.recommend.application.scheduler;

import com.sofm.recommend.infrastructure.redis.RedisConstants;
import com.sofm.recommend.infrastructure.redis.RedisHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class HeatUpdateScheduler {

    private final Map<String, Double> heatUpdates = new ConcurrentHashMap<>();

    private final RedisHelper redisHelper;

    public HeatUpdateScheduler(RedisHelper redisHelper) {
        this.redisHelper = redisHelper;
    }

    private static boolean isStart = false;

    @Scheduled(fixedRate = 12, timeUnit = TimeUnit.SECONDS)
    public void updateHotToRedis() {
        if (isStart) {
            log.info("syncNoteToPNote mission is running,missing this time");
            return;
        }
        isStart = true;
        if (!heatUpdates.isEmpty()) {
            redisHelper.addToZSetWithLuaScript(RedisConstants.hot_items, heatUpdates, 2000);
            heatUpdates.clear();
        }
        isStart = false;
    }

    public void addToUpdate(String key, double score) {
        heatUpdates.put(key, score);
    }
}
