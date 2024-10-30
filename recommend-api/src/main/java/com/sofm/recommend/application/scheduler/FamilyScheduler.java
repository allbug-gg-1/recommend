package com.sofm.recommend.application.scheduler;

import com.sofm.recommend.domain.model.mysql.Pet;
import com.sofm.recommend.domain.service.PetService;
import com.sofm.recommend.infrastructure.redis.RedisHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class FamilyScheduler {

    private final PetService petService;
    private final RedisHelper redisHelper;
    private final RedisTemplate<String,String> sourceRedisTemplate;

    public FamilyScheduler(PetService petService, RedisHelper redisHelper, RedisTemplate<String, String> sourceRedisTemplate) {
        this.petService = petService;
        this.redisHelper = redisHelper;
        this.sourceRedisTemplate = sourceRedisTemplate;
    }

    private static boolean isStart = false;
    private static final int pageSize = 500;

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.SECONDS)
    public void syncFamily() {
        if (isStart) {
            log.info("syncPetFamily mission is running,missing this time");
            return;
        }
        isStart = true;
//        Date lastTime;
//        if (redisHelper.hasKey(RedisConstants.family_sync_time)) {
//            String lastTimeStr = (String) redisHelper.getValue(RedisConstants.family_sync_time);
//            lastTime = new Date(Long.parseLong(lastTimeStr));
//        } else {
//            lastTime = new Date(0);
//        }
//        int petCount = petService.countLastModifyPet(lastTime);
//        if (petCount > 0) {
//            checkRecords(1, lastTime, petCount);
//        } else {
//            log.info("no pet modify");
//        }
        isStart = false;
    }

    public void checkRecords(int page, Date lastTime, int allCount) {
        Page<Pet> pets = petService.loadLastModifyPet(lastTime, page, pageSize);
        Map<Integer, List<Integer>> familyUsers = new HashMap<>();
        for (Pet pet : pets.getContent()) {
            int pid = pet.getRecordId();
            int fid = pet.getFamilyId();
//            familyUsers.putIfAbsent();
        }
    }
}
