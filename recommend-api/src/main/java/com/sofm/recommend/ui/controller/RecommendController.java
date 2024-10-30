package com.sofm.recommend.ui.controller;

import com.sofm.recommend.application.exception.ServiceException;
import com.sofm.recommend.application.service.RecommendApplicationService;
import com.sofm.recommend.common.dto.RecommendResult;
import com.sofm.recommend.common.response.StandardResponse;
import com.sofm.recommend.ui.dto.request.RecommendRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RecommendController {

    private final RecommendApplicationService recommendApplicationService;

    public RecommendController(RecommendApplicationService recommendApplicationService) {
        this.recommendApplicationService = recommendApplicationService;
    }

    @PostMapping(value = "/recommend")
    public StandardResponse loadRecommend(@RequestBody RecommendRequest request) {
        try {
            RecommendResult result = recommendApplicationService.getRecommendResults(request);
            return StandardResponse.success(result.getRecommend_id(), result.getData());
        } catch (ServiceException e) {
            return StandardResponse.error(e.getMessage());
        }
    }

    @GetMapping(value = "/status")
    public Map<String, Object> status() {
        return Map.of("live", 1);
    }
}
