package com.sofm.recommend.application.service.channelHandler;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sofm.recommend.application.config.UserMabWeightConfig;
import com.sofm.recommend.common.dto.RecommendContext;
import com.sofm.recommend.infrastructure.redis.RedisHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static com.sofm.recommend.infrastructure.redis.RedisConstants.user_rank_cache_pool;

@Component
@Slf4j
public class CacheHandler extends AbstractChannelHandler {

    private final Cache<String, List<String>> cache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofSeconds(5))
            .build();

    @Autowired
    private RedisHelper redisHelper;

    @Override
    public List<String> load(RecommendContext context, int startPos) {
        List<String> cacheItems = List.of();
        int size = loadSize(context);
        if (cache.asMap().containsKey(String.valueOf(context.getUserId()))) {
            cacheItems = cache.getIfPresent(String.valueOf(context.getUserId()));
            assert cacheItems != null;
        } else {
            String cacheKey = user_rank_cache_pool.replace("{user_id}", String.valueOf(context.getUserId()));
            if (redisHelper.hasKey(cacheKey)) {
                Set<Object> caches = redisHelper.zRange(cacheKey, false, false, startPos, -1);
                if (!caches.isEmpty()) {
                    cacheItems = caches.stream().map(String::valueOf).toList();
                    cache.put(String.valueOf(context.getUserId()), cacheItems);
                }
            }
        }
        return cacheItems.subList(Math.min(cacheItems.size() - 1, startPos), Math.min(cacheItems.size() - 1, startPos + size - 1));
    }

    @Override
    public double loadChannelWeight(RecommendContext context) {
        return context.getUserMab().getCache();
    }
}
