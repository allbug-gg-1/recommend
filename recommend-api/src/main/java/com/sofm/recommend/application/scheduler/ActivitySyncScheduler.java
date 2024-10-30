package com.sofm.recommend.application.scheduler;

import com.sofm.recommend.domain.activity.entity.Activity;
import com.sofm.recommend.domain.activity.service.ActivityService;
import com.sofm.recommend.infrastructure.redis.RedisConstants;
import com.sofm.recommend.infrastructure.redis.RedisHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ActivitySyncScheduler {

    private final ActivityService activityService;
    private final RedisHelper redisHelper;

    public ActivitySyncScheduler(ActivityService activityService, RedisHelper redisHelper) {
        this.activityService = activityService;
        this.redisHelper = redisHelper;
    }

    private boolean isStart = false;

    @Scheduled(cron = "0 40 0 * * ?") // 每天12:40
//    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    public void syncActivity() {
        if (isStart) {
            log.info("syncActivity mission is running,missing this time");
            return;
        }
        isStart = true;
        List<Activity> activityList = activityService.loadLiveActivity();
        if (!activityList.isEmpty()) {
            List<Integer> activity = activityList.stream().map(Activity::getRecordId).toList();
            redisHelper.delete(RedisConstants.active_activity);
            redisHelper.addSet(RedisConstants.active_activity, activity.toArray());
        }
        isStart = false;
    }
}
