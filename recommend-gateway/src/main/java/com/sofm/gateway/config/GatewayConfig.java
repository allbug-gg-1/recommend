package com.sofm.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("recommend-service", r -> r.path("/recommend/**").uri("lb://recommend-service"))
                .route("test-service", r -> r.path("/test/**").uri("lb://test-service"))
                .build();
    }
}
