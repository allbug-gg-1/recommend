package com.sofm.recommend.application.service.rankHandler;

import com.sofm.recommend.common.dto.RecommendContext;

import java.util.List;

public class AbstractRankHandler implements RankHandler {

    @Override
    public List<Integer> rank(RecommendContext context, List<Integer> items) {
        return List.of();
    }
}
