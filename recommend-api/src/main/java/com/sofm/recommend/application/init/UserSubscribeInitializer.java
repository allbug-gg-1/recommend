package com.sofm.recommend.application.init;

import jakarta.annotation.PostConstruct;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class UserSubscribeInitializer{

    private RedisTemplate<String, String> sourceRedisTemplate;

    public UserSubscribeInitializer(RedisTemplate<String, String> sourceRedisTemplate) {
        this.sourceRedisTemplate = sourceRedisTemplate;
    }

    @PostConstruct
    public void init() {
        Set<String> a = sourceRedisTemplate.opsForZSet().range("usr:fan:54", 0, -1);
        System.out.println(a);
    }
}

