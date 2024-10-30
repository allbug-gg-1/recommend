package com.sofm.recommend.ui.controller;

import com.sofm.feign.client.test.TestServiceClient;
import com.sofm.recommend.common.response.StandardResponse;
import com.sofm.recommend.infrastructure.redis.RedisHelper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/recommend")
public class TestController {

    private final RedisHelper redisHelper;

    private final TestServiceClient testServiceClient;

    public TestController(RedisHelper redisHelper, TestServiceClient testServiceClient) {
        this.redisHelper = redisHelper;
        this.testServiceClient = testServiceClient;
    }

    @GetMapping("/test")
    public StandardResponse getUser() {
        return StandardResponse.success(testServiceClient.test());
    }
}