package com.sofm.recommend.application.service.modeHandler;

import com.sofm.recommend.application.service.channelHandler.ChannelHandler;
import com.sofm.recommend.common.code.RecommendType;
import com.sofm.recommend.common.dto.RecommendContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Set;

public abstract class AbstractModeHandler implements ModeHandler {

    @Autowired
    protected Map<String, ChannelHandler> channelHandlers;

    public abstract RecommendType getRecommendType();

    @Override
    public abstract int recall(RecommendContext context, Map<String, Set<String>> channelItems, int loadTime);


}
