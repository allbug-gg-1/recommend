package com.sofm.recommend.application.service.channelHandler;

import com.sofm.recommend.common.dto.RecommendContext;
import com.sofm.recommend.domain.model.mongo.PNote;
import com.sofm.recommend.domain.note.service.NoteService;
import com.sofm.recommend.infrastructure.redis.RedisHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.sofm.recommend.infrastructure.redis.RedisConstants.adopt_user;
import static com.sofm.recommend.infrastructure.redis.RedisConstants.user_recent_interaction_topic;

@Component
@Slf4j
public class InterestHandler extends AbstractChannelHandler {

    @Autowired
    private RedisHelper redisHelper;

    @Autowired
    private NoteService noteService;

    @Override
    public List<String> load(RecommendContext context, int startPos) {
        List<String> allInterestNotes = new ArrayList<>();
        int size = loadSize(context);
        allInterestNotes.addAll(loadTopicItems(context.getUserId(), size));
        allInterestNotes.addAll(loadAdoptItems(context.getUserId()));
        return allInterestNotes;
    }

    public List<String> loadTopicItems(int userId, int size) {
        String topicKey = user_recent_interaction_topic.replace("{user_id}", String.valueOf(userId));
        if (redisHelper.hasKey(topicKey)) {
            Set<Object> topics = redisHelper.zRange(topicKey, false, false, 0, -1);
            if (!topics.isEmpty()) {
                List<String> tos = topics.stream().map(String::valueOf).toList();
                List<PNote> topicRandomNote = noteService.getTopicRandomNote(tos, Math.max(40, size - 20));
                return topicRandomNote.stream().map(record -> String.valueOf(record.getRecordId())).toList();
            }
        }
        return List.of();
    }

    public List<String> loadAdoptItems(int userId) {
        if (redisHelper.hasKey(adopt_user)) {
            Long adoptUser = redisHelper.getRank(adopt_user, String.valueOf(userId));
            if (adoptUser != null) {
                List<PNote> adoptNotes = noteService.getAdoptRandomNote(20);
                return adoptNotes.stream().map(record -> String.valueOf(record.getRecordId())).toList();
            }
        }
        return List.of();
    }

    @Override
    public double loadChannelWeight(RecommendContext context) {
        return context.getUserMab().getInterest();
    }
}
