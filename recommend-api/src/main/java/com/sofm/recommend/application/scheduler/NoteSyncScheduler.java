package com.sofm.recommend.application.scheduler;

import com.sofm.recommend.application.strategy.ScoringStrategy;
import com.sofm.recommend.common.utils.StringUtils;
import com.sofm.recommend.domain.note.entity.NoteMongoEntity;
import com.sofm.recommend.domain.note.entity.NoteMysqlEntity;
import com.sofm.recommend.domain.note.service.NoteService;
import com.sofm.recommend.domain.pet.service.PetService;
import com.sofm.recommend.infrastructure.redis.RedisConstants;
import com.sofm.recommend.infrastructure.redis.RedisHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class NoteSyncScheduler {

    private final NoteService noteService;
    private final PetService petService;
    private final RedisHelper redisHelper;

    public NoteSyncScheduler(NoteService noteService, PetService petService, RedisHelper redisHelper) {
        this.noteService = noteService;
        this.petService = petService;
        this.redisHelper = redisHelper;
    }

    private static boolean isStart = false;
    private static final int pageSize = 500;

    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.SECONDS)
    public void syncNoteToPNote() {
        if (isStart) {
            log.info("syncNoteToPNote mission is running,missing this time");
            return;
        }
        isStart = true;
        Date lastTime;
        Date now = new Date();
        if (redisHelper.hasKey(RedisConstants.note_sync_time)) {
            String lastTimeStr = (String) redisHelper.getValue(RedisConstants.note_sync_time);
            lastTime = new Date(Long.parseLong(lastTimeStr));
        } else {
            lastTime = new Date(0);
        }
        int total = noteService.countLastModifyNote(lastTime);
        if (total > 0) {
            log.info("note count:{} to sync", total);
            processNote(lastTime, 1, total);
        }
        redisHelper.setValue(RedisConstants.note_sync_time, String.valueOf(now.getTime()));
        isStart = false;
    }

    public void processNote(Date lastTime, int page, int total) {
        Page<NoteMysqlEntity> notes = noteService.loadLastModifyNote(lastTime, page, pageSize);
        List<NoteMongoEntity> updates = new ArrayList<>();
        List<Pair<Integer, Long>> totalRecent = new ArrayList<>();
        List<Integer> disable = new ArrayList<>();
        Map<Integer, List<Pair<Integer, Long>>> userRecent = new HashMap<>();
        Map<Integer, List<Integer>> userDisable = new HashMap<>();
        List<Pair<Integer, Long>> ads = new ArrayList<>();
        List<Integer> disableAds = new ArrayList<>();
        for (NoteMysqlEntity note : notes.getContent()) {
            Integer petType = null;
            if (note.getPetId() > 0) {
                petType = petService.getTypeByRecordId(note.getPetId());
            }
            NoteMongoEntity pNote = new NoteMongoEntity();
            pNote.setRecordId(note.getRecordId());
            pNote.setUid(note.getUid());
            pNote.setPetId(note.getPetId());
            pNote.setPetType(petType);
            pNote.setVisibility("0".equals(note.getVisibility()));
            pNote.setCheckType("1".equals(note.getCheckType()));
            pNote.setActivityId(note.getActivityId());
            if (StringUtils.isNotEmpty(note.getTopic())) {
                pNote.setTopic(Arrays.asList(note.getTopic().split(",")));
            }
            if (StringUtils.isNotEmpty(note.getCityCode())) {
                pNote.setProvince(note.getCityCode().split(",")[0]);
                pNote.setCity(note.getCityCode().split(",")[1]);
            }
            pNote.setStatus(note.getStatus());
            pNote.setNoteScore(note.getNoteScore());
            pNote.setVideoDuration(note.getVideoDuration());
            pNote.setCreateTime(note.getCreateTime());
            pNote.setLastModifyTime(note.getLastModifyTime());
            pNote.setWaitAdopt(1 == note.getAdopt() && 0 == note.getCommentId());
            pNote.setContentLength(StringUtils.length(note.getContent()));
            if (note.getVideoDuration() == 0) {
                pNote.setImageNum(note.getUrl().split(",").length);
            }
            pNote.setQualityPoint(ScoringStrategy.calculateScore(note.getVideoDuration(), pNote.getImageNum(), pNote.getContentLength()));
            updates.add(pNote);
            if ("0".equals(note.getVisibility()) && "1".equals(note.getCheckType()) && "1".equals(note.getStatus())) {
                Pair<Integer, Long> notePair = Pair.of(note.getRecordId(), note.getCreateTime().getTime());
                totalRecent.add(notePair);
                if (userRecent.containsKey(pNote.getUid())) {
                    List<Pair<Integer, Long>> list = userRecent.get(pNote.getUid());
                    list.add(notePair);
                } else {
                    List<Pair<Integer, Long>> list = new ArrayList<>();
                    list.add(notePair);
                    userRecent.put(pNote.getUid(), list);
                }
                if ("05".equals(note.getChangeScoreType())) {
                    ads.add(Pair.of(note.getRecordId(), note.getCreateTime().getTime()));
                }
            } else {
                disable.add(pNote.getRecordId());
                if (userDisable.containsKey(pNote.getUid())) {
                    List<Integer> list = userDisable.get(pNote.getUid());
                    list.add(pNote.getRecordId());
                } else {
                    List<Integer> list = new ArrayList<>();
                    list.add(pNote.getRecordId());
                    userDisable.put(pNote.getUid(), list);
                }
                if ("05".equals(note.getChangeScoreType())) {
                    disableAds.add(note.getRecordId());
                }
            }
        }
        if (!updates.isEmpty()) {
            noteService.batchUpsertRecord(updates);
        }
        if (!totalRecent.isEmpty() || !disable.isEmpty()) {
            noteService.syncTotalItemToRedis(totalRecent, disable);
            noteService.syncUserItemToRedis(userRecent, userDisable);
        }
        if (!ads.isEmpty() || !disableAds.isEmpty()) {
            noteService.syncAdItemToRedis(ads, disableAds);
        }
        if (page * pageSize < total) {
            page++;
            processNote(lastTime, page, total);
        } else {
            log.info("finish note mission");
        }
    }
}
