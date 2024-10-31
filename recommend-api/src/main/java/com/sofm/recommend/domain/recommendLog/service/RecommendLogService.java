package com.sofm.recommend.domain.recommendLog.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sofm.recommend.common.code.RecommendType;
import com.sofm.recommend.domain.recommendLog.entity.RecommendLog;
import com.sofm.recommend.infrastructure.mongo.repository.RecommendLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class RecommendLogService {

    private final Cache<String, RecommendLog> logCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(10))
            .build();

    private final RecommendLogRepository recommendLogRepository;

    public RecommendLogService(RecommendLogRepository recommendLogRepository) {
        this.recommendLogRepository = recommendLogRepository;
    }

    public void saveRecommendLog(String logId, int uid, RecommendType recommendType, Map<String, Set<String>> channelItems) {
        RecommendLog recommendLog = new RecommendLog();
        recommendLog.setId(logId);
        recommendLog.setUid(uid);
        recommendLog.setRecommendType(recommendType.getCode());
        recommendLog.setCreateTime(new Date());
        recommendLog.setChannelItem(channelItems);
        recommendLogRepository.save(recommendLog);
    }

    public RecommendLog getLogById(String id) {
        RecommendLog recommendLog = logCache.getIfPresent(id);
        if (recommendLog == null) {
            Optional<RecommendLog> optional = recommendLogRepository.getById(id);
            if (optional.isEmpty()) {
                return null;
            } else {
                recommendLog = optional.get();
                logCache.put(recommendLog.getId(), recommendLog);
            }
        }
        return recommendLog;
    }
}
