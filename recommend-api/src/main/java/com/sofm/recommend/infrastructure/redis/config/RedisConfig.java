package com.sofm.recommend.infrastructure.redis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    // 从配置文件中读取源 Redis 配置信息
    @Value("${spring.redis.source.host}")
    private String sourceRedisHost;

    @Value("${spring.redis.source.port}")
    private int sourceRedisPort;

    @Value("${spring.redis.source.database}")
    private int sourceRedisDatabase;

    // 从配置文件中读取目标 Redis 配置信息
    @Value("${spring.redis.target.host}")
    private String targetRedisHost;

    @Value("${spring.redis.target.port}")
    private int targetRedisPort;

    @Value("${spring.redis.target.database}")
    private int targetRedisDatabase;

    @Bean
    public RedisConnectionFactory sourceRedisConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(sourceRedisHost);
        redisStandaloneConfiguration.setPort(sourceRedisPort);
        redisStandaloneConfiguration.setDatabase(sourceRedisDatabase);
        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }

    // 目标 Redis 连接工厂
    @Bean
    public RedisConnectionFactory targetRedisConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(targetRedisHost);
        redisStandaloneConfiguration.setPort(targetRedisPort);
        redisStandaloneConfiguration.setDatabase(targetRedisDatabase);
        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }

    @Bean
    public RedisTemplate<String, String> sourceRedisTemplate(RedisConnectionFactory sourceRedisConnectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(sourceRedisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }

    // 目标 Redis 的 RedisTemplate
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory targetRedisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(targetRedisConnectionFactory);
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        template.setValueSerializer(stringRedisSerializer);
        template.setHashValueSerializer(stringRedisSerializer);
        template.afterPropertiesSet();
        return template;
    }
}
