package com.sofm.recommend.application.service.channelHandler;

import com.sofm.recommend.common.dto.RecommendContext;
import com.sofm.recommend.infrastructure.redis.RedisHelper;
import com.sofm.recommend.infrastructure.redis.source.SourceRedisHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.sofm.recommend.infrastructure.redis.RedisConstants.*;
import static com.sofm.recommend.infrastructure.redis.source.SourceRedisConstants.USER_FOLLOWED;

@Component
@Slf4j
public class SocialHandler extends AbstractChannelHandler {

    @Autowired
    private SourceRedisHelper sourceRedisHelper;

    @Autowired
    private RedisHelper redisHelper;

    @Override
    public List<String> load(RecommendContext context, int startPos) {
        int size = loadSize(context);
        String unionKey = union_user_follow_key + context.getUserId();
        Set<Object> items = new HashSet<>();
        if (redisHelper.hasKey(unionKey)) {
            items = redisHelper.zRange(unionKey, false, false, startPos, Math.max(0, startPos + size - 1));

        } else {
            List<String> creatorKeys = new ArrayList<>();
            loadFollowUser(context.getUserId(), creatorKeys);
            loadRecentUser(context.getUserId(), creatorKeys);
            if (!creatorKeys.isEmpty()) {
                redisHelper.zUnion(creatorKeys, unionKey);
                redisHelper.expire(unionKey, 5, TimeUnit.MINUTES);
                items = redisHelper.zRange(unionKey, false, false, startPos, Math.max(0, startPos + size - 1));
            }
        }
        if (!items.isEmpty()) {
            return items.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    public void loadFollowUser(int userId, List<String> creatorKeys) {
        String followKey = USER_FOLLOWED + userId;
        if (sourceRedisHelper.hasKey(followKey)) {
            Set<String> follows = sourceRedisHelper.rangeAll(followKey);
            if (!follows.isEmpty()) {
                creatorKeys.addAll(follows.stream().map(record -> creator_recent_items.replace("{user_id}", String.valueOf(record))).toList());
            }
        }
    }

    public void loadRecentUser(int userId, List<String> creatorKeys) {
        String recentKey = user_recent_interaction_publisher.replace("{user_id}", String.valueOf(userId));
        if (redisHelper.hasKey(recentKey)) {
            Set<Object> recent = redisHelper.zRange(recentKey, false, false, 0, -1);
            if (!recent.isEmpty()) {
                creatorKeys.addAll(recent.stream().map(record -> creator_recent_items.replace("{user_id}", String.valueOf(record))).toList());
            }
        }
    }

    @Override
    public double loadChannelWeight(RecommendContext context) {
        return context.getUserMab().getSocial();
    }
}

