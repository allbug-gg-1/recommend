package com.sofm.recommend.domain.activity.service;

import com.sofm.recommend.domain.activity.entity.Activity;
import com.sofm.recommend.infrastructure.mysql.repository.ActivityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;

    public ActivityService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @Transactional(readOnly = true)
    public int countLiveActivity() {
        return activityRepository.countByStatusAndFinished("1", 0);
    }

    @Transactional(readOnly = true)
    public List<Activity> loadLiveActivity() {
        return activityRepository.findByStatusAndFinished("1", 0);
    }
}
