package com.sofm.recommend.application.service.channelHandler;

import com.sofm.recommend.application.config.UserMabWeightConfig;
import com.sofm.recommend.common.code.RecommendType;
import com.sofm.recommend.common.dto.RecommendContext;
import com.sofm.recommend.domain.note.entity.NoteMongoEntity;
import com.sofm.recommend.domain.note.service.NoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class RandomHandler extends AbstractChannelHandler {

    @Autowired
    private NoteService noteService;

    @Override
    public List<String> load(RecommendContext context, int startPos) {
        int size = loadSize(context);
        List<NoteMongoEntity> notes = noteService.getRandomNote(size);
        return notes.stream().map(record -> String.valueOf(record.getRecordId())).toList();
    }

    @Override
    public double loadChannelWeight(RecommendContext context) {
        if (context.getRecommendType() == RecommendType.Explore) {
            return UserMabWeightConfig.getOnlyRandomWeight();
        } else {
            return context.getUserMab().getRegion();
        }
    }
}
