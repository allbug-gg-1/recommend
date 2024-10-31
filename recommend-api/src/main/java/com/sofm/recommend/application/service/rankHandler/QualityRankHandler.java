package com.sofm.recommend.application.service.rankHandler;

import com.sofm.recommend.common.dto.RankedPNote;
import com.sofm.recommend.common.dto.RecommendContext;
import com.sofm.recommend.domain.note.entity.NoteMongoEntity;
import com.sofm.recommend.domain.note.service.NoteService;
import com.sofm.recommend.infrastructure.redis.RedisHelper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.sofm.recommend.infrastructure.redis.RedisConstants.user_dislike_pet;
import static com.sofm.recommend.infrastructure.redis.RedisConstants.user_dislike_user;

@Component
public class QualityRankHandler extends AbstractRankHandler {
    private final NoteService noteService;
    private final RedisHelper redisHelper;

    public QualityRankHandler(NoteService noteService, RedisHelper redisHelper) {
        this.noteService = noteService;
        this.redisHelper = redisHelper;
    }


    public List<Integer> rank(RecommendContext context, List<Integer> items) {
        List<NoteMongoEntity> pNotes = noteService.listPNoteByRecordIds(items);
        if (pNotes.isEmpty()) {
            return List.of();
        }
        List<RankedPNote> results = new ArrayList<>();
        List<Integer> unLikePetType = loadUnLikePet(context.getUserId());
        List<Integer> unLikeUser = loadUnLikeUser(context.getUserId());
        int user_dislike_weight = 9;
        int pet_dislike_weight = 9;
        for (NoteMongoEntity pNote : pNotes) {
            int userFrequency = Collections.frequency(unLikeUser, pNote.getUid());
            if (userFrequency >= 1) {
                user_dislike_weight = Math.max(0, user_dislike_weight - userFrequency * 3);
            }
            if (pNote.getPetType() != null) {
                int petFrequency = Collections.frequency(unLikePetType, pNote.getPetType());
                if (petFrequency >= 1) {
                    pet_dislike_weight = Math.max(0, pet_dislike_weight - petFrequency * 3);
                }
            }
            double point;
            point = (pNote.getQualityPoint() * ((double) (100 + pNote.getNoteScore()) / 100)) * user_dislike_weight / 9 * pet_dislike_weight / 9;
            results.add(new RankedPNote(pNote.getRecordId(), point));
        }
        return results.stream().sorted(Comparator.comparingDouble(RankedPNote::getQualityScore).reversed()).map(RankedPNote::getNoteId).toList();
    }


    public List<Integer> loadUnLikePet(int userId) {
        String unLikeKey = user_dislike_pet.replace("{user_id}", String.valueOf(userId));
        if (redisHelper.hasKey(unLikeKey)) {
            return redisHelper.lRange(unLikeKey, 0, -1).stream().map(record -> Integer.parseInt(String.valueOf(record))).toList();
        }
        return List.of();
    }

    public List<Integer> loadUnLikeUser(int userId) {
        String unLikeKey = user_dislike_user.replace("{user_id}", String.valueOf(userId));
        if (redisHelper.hasKey(unLikeKey)) {
            return redisHelper.lRange(unLikeKey, 0, -1).stream().map(record -> Integer.parseInt(String.valueOf(record))).toList();
        }
        return List.of();
    }

}
