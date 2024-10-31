package com.sofm.recommend.application.service.ruleHandler;

import com.sofm.recommend.domain.note.entity.NoteMongoEntity;
import com.sofm.recommend.domain.note.service.NoteService;
import com.sofm.recommend.infrastructure.redis.RedisHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.sofm.recommend.infrastructure.redis.RedisConstants.active_activity;
import static com.sofm.recommend.infrastructure.redis.RedisConstants.ad_items;

@Component
public class DefaultRuleHandler implements RuleHandler {

    @Autowired
    private RedisHelper redisHelper;

    @Autowired
    private NoteService noteService;

    @Override
    public List<Integer> process(List<Integer> rankedIds) {
        if (redisHelper.hasKey(ad_items)) {
            double adProbability = Math.random();
            if (adProbability >= 0.5d) {
                String ad = (String) redisHelper.getRandomElementFromZSet(ad_items);
                if (!rankedIds.contains(Integer.parseInt(ad))) {
                    rankedIds.add(Integer.parseInt(ad));
                }
            }
        }
        if (redisHelper.hasKey(active_activity)) {
            double activityProbability = Math.random();
            if (activityProbability >= 0.5d) {
                Set<Object> activity = redisHelper.getSetMembers(active_activity);
                if (!activity.isEmpty()) {
                    List<NoteMongoEntity> notes = noteService.getActivityRandomNote(activity.stream().map(record -> Integer.parseInt(String.valueOf(record))).toList(), 2);
                    for (NoteMongoEntity noteMongoEntity : notes) {
                        if (!rankedIds.contains(noteMongoEntity.getRecordId())) {
                            rankedIds.add(noteMongoEntity.getRecordId());
                        }
                    }
                }
            }
        }
        List<Integer> top2 = new ArrayList<>(rankedIds.subList(0, Math.min(2, rankedIds.size())));
        List<Integer> other;
        if (rankedIds.size() > 2) {
            other = new ArrayList<>(rankedIds.subList(2, rankedIds.size()));
            Collections.shuffle(other);
            top2.addAll(other);
        }
        return top2;
    }
}
