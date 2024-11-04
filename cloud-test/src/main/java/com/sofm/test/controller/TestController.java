package com.sofm.test.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class TestController {

    @Autowired(required = false)
    private Registration registration; // 当前实例的注册信息

    @GetMapping(value = "/test/all")
    public String test() {
        String instanceId = (registration != null) ? registration.getInstanceId() : "Unknown Instance ID";
        log.info("request process in :{}", instanceId);
        return "hello test" + instanceId;
    }
}
