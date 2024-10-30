package com.sofm.recommend.application.service.channelHandler;

import com.sofm.recommend.application.config.UserMabWeightConfig;
import com.sofm.recommend.common.code.RecommendType;
import com.sofm.recommend.common.dto.RecommendContext;
import com.sofm.recommend.domain.model.mongo.PNote;
import com.sofm.recommend.domain.note.service.NoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class RegionHandler extends AbstractChannelHandler {

    @Autowired
    private NoteService noteService;

    @Override
    public List<String> load(RecommendContext context, int startPos) {
        int size = loadSize(context);
        List<PNote> regionPNotes = noteService.getRegionRandomNote(context.getProvinceCityDto().getProvince(), context.getProvinceCityDto().getCity(), size);
        return regionPNotes.stream().map(record -> String.valueOf(record.getRecordId())).toList();
    }

    @Override
    public double loadChannelWeight(RecommendContext context) {
        if (context.getRecommendType() == RecommendType.NearBy) {
            return UserMabWeightConfig.getOnlyNearByWeight();
        } else {
            return context.getUserMab().getRegion();
        }
    }
}
