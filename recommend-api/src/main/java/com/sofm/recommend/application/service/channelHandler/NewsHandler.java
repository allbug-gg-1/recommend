package com.sofm.recommend.application.service.channelHandler;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sofm.recommend.common.dto.RecommendContext;
import com.sofm.recommend.infrastructure.redis.RedisHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static com.sofm.recommend.infrastructure.redis.RedisConstants.total_recent_items;

@Component
@Slf4j
public class NewsHandler extends AbstractChannelHandler {

    private final String recentKey = "recent";

    private final Cache<String, List<String>> cache = Caffeine.newBuilder()
            .maximumSize(1)
            .expireAfterWrite(Duration.ofSeconds(3))
            .build();

    @Autowired
    private RedisHelper redisHelper;


    @Override
    public List<String> load(RecommendContext context, int startPos) {
        List<String> items = List.of();
        int size = loadSize(context);
        if (cache.asMap().containsKey(recentKey)) {
            items = cache.getIfPresent(recentKey);
            assert items != null;
        } else {
            String newsKey = total_recent_items;
            if (redisHelper.hasKey(newsKey)) {
                Set<Object> recentItems = redisHelper.zRange(newsKey, false, false, startPos, -1);
                if (!recentItems.isEmpty()) {
                    items = recentItems.stream().map(String::valueOf).toList();
                    cache.put(recentKey, items);
                }
            }
        }
        return items.subList(Math.min(items.size() - 1, startPos), Math.min(items.size() - 1, startPos + size - 1));
    }

    @Override
    public double loadChannelWeight(RecommendContext context) {
        return context.getUserMab().getNews();
    }
}
