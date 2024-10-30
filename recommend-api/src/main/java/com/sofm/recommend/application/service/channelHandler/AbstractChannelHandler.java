package com.sofm.recommend.application.service.channelHandler;

import com.sofm.recommend.application.config.UserMabWeightConfig;
import com.sofm.recommend.common.dto.RecommendContext;

import java.util.List;

public class AbstractChannelHandler implements ChannelHandler {

    @Override
    public List<String> load(RecommendContext context, int startPos) {
        return List.of();
    }

    @Override
    public int loadSize(RecommendContext context) {
        double channelWeight = loadChannelWeight(context);
        int size = (int) (UserMabWeightConfig.DEFAULT_CHANNEL_COUNT * channelWeight);
        if (size > UserMabWeightConfig.MAX_CHANNEL_COUNT) {
            return UserMabWeightConfig.MAX_CHANNEL_COUNT;
        } else if (size < UserMabWeightConfig.MIN_CHANNEL_COUNT) {
            return UserMabWeightConfig.MIN_CHANNEL_COUNT;
        }
        return size;
    }

    @Override
    public double loadChannelWeight(RecommendContext context) {
        return 0d;
    }
}
