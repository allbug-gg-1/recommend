package com.sofm.recommend.application.service.modeHandler;

import com.sofm.recommend.common.code.RecommendType;
import com.sofm.recommend.common.dto.RecommendContext;

import java.util.Map;
import java.util.Set;

public interface ModeHandler {

    RecommendType getRecommendType();

    int recall(RecommendContext context, Map<String, Set<String>> channelItems, int loadTime);

}
