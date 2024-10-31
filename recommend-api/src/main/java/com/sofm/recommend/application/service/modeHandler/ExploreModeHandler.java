package com.sofm.recommend.application.service.modeHandler;

import com.sofm.recommend.application.service.channelHandler.ChannelHandler;
import com.sofm.recommend.common.code.RecommendType;
import com.sofm.recommend.common.dto.RecommendContext;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ExploreModeHandler extends AbstractModeHandler {

    @Override
    public RecommendType getRecommendType() {
        return RecommendType.Explore;
    }

    @Override
    public int recall(RecommendContext context, Map<String, Set<String>> channelItems, int loadTime) {
        ChannelHandler regionHandler = channelHandlers.get("randomHandler");
        Set<String> items = channelItems.getOrDefault("random", new HashSet<>());
        List<String> channelResults = regionHandler.load(context, loadTime);
        items.addAll(channelResults);
        channelItems.put("random", items);
        return items.size();
    }
}
