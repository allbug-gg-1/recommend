package com.sofm.recommend.application.service.rankHandler;

import com.sofm.recommend.common.dto.RecommendContext;

import java.util.List;

public interface RankHandler {

    List<Integer> rank(RecommendContext context, List<Integer> items);
}
