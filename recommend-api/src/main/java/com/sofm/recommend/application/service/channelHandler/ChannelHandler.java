package com.sofm.recommend.application.service.channelHandler;

import com.sofm.recommend.common.dto.RecommendContext;

import java.util.List;

public interface ChannelHandler {

    List<String> load(RecommendContext context, int startPos);

    int loadSize(RecommendContext context);

    double loadChannelWeight(RecommendContext context);
}
