package com.sofm.feign.client.test;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "test-service")
public interface TestServiceClient {

    @GetMapping("/test/all")
    String test();


}
