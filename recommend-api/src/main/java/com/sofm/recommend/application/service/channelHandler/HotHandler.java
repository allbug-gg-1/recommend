package com.sofm.recommend.application.service.channelHandler;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sofm.recommend.common.dto.RecommendContext;
import com.sofm.recommend.infrastructure.redis.RedisConstants;
import com.sofm.recommend.infrastructure.redis.RedisHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class HotHandler extends AbstractChannelHandler {

    private final String hotKey = "hot";

    private final Cache<String, List<String>> cache = Caffeine.newBuilder()
            .maximumSize(1)
            .expireAfterWrite(Duration.ofSeconds(10))
            .build();

    @Autowired
    private RedisHelper redisHelper;

    @Override
    public List<String> load(RecommendContext context, int startPos) {
        List<String> cacheItems = List.of();
        int size = loadSize(context);
        if (cache.asMap().containsKey(hotKey)) {
            cacheItems = cache.getIfPresent(hotKey);
            assert cacheItems != null;
        } else {
            if (redisHelper.hasKey(RedisConstants.hot_items)) {
                Set<Object> caches = redisHelper.zRange(RedisConstants.hot_items, false, false, startPos, Math.max(0, startPos + size - 1));
                if (!caches.isEmpty()) {
                    cacheItems = caches.stream().map(String::valueOf).toList();
                    cache.put(hotKey, cacheItems);
                }
            }
        }
        return cacheItems.subList(Math.min(cacheItems.size() - 1, startPos), Math.min(cacheItems.size() - 1, startPos + size - 1));
    }

    @Override
    public double loadChannelWeight(RecommendContext context) {
        return context.getUserMab().getHot();
    }
}
