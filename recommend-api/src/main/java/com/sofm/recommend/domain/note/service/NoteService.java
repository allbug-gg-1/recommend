package com.sofm.recommend.domain.note.service;

import com.google.common.collect.ImmutableList;
import com.sofm.recommend.common.utils.BeanUtils;
import com.sofm.recommend.common.utils.DateUtils;
import com.sofm.recommend.common.utils.StringUtils;
import com.sofm.recommend.domain.model.mongo.PNote;
import com.sofm.recommend.domain.model.mongo.dto.UpdatePNote;
import com.sofm.recommend.domain.note.entity.Note;
import com.sofm.recommend.domain.note.entity.NoteMysqlEntity;
import com.sofm.recommend.infrastructure.mongo.repository.PNoteRepository;
import com.sofm.recommend.infrastructure.mysql.repository.NoteRepository;
import com.sofm.recommend.infrastructure.redis.RedisConstants;
import com.sofm.recommend.infrastructure.redis.RedisHelper;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class NoteService {

    private final RedisHelper redisHelper;
    private final NoteRepository noteRepository;
    private final MongoTemplate mongoTemplate;
    private final PNoteRepository pNoteRepository;


    public NoteService(RedisHelper redisHelper, NoteRepository noteRepository, MongoTemplate mongoTemplate, PNoteRepository pNoteRepository) {
        this.redisHelper = redisHelper;
        this.noteRepository = noteRepository;
        this.mongoTemplate = mongoTemplate;
        this.pNoteRepository = pNoteRepository;
    }

    @Transactional(readOnly = true)
    public int countLastModifyNote(Date lastTime) {
        return noteRepository.countByLastModifyTimeAfter(lastTime);
    }

    @Transactional(readOnly = true)
    public Page<NoteMysqlEntity> loadLastModifyNote(Date lastTime, int page, int pageSize) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), pageSize);
        return noteRepository.findByLastModifyTimeAfter(lastTime, pageable);
    }

    public Optional<PNote> getPNoteByRecordId(int recordId) {
        return pNoteRepository.getByRecordId(recordId);
    }

    public List<PNote> listPNoteByRecordIds(List<Integer> recordIds) {
        return pNoteRepository.findByRecordIdIn(recordIds);
    }

    public void batchUpsertRecord(List<UpdatePNote> pNotes) {
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, PNote.class);
        Map<String, Object> emptyData = new java.util.HashMap<>(Map.of("hotPoint5m", 0, "hotPoint30m", 0, "hotPoint3h", 0, "hotPoint1d", 0, "hotPoint7d", 0));
        emptyData.put("lastInteractionTime", null);
        for (UpdatePNote pNote : pNotes) {
            Query query = new Query(Criteria.where("recordId").is(pNote.getRecordId()));
            Update update = new Update();
            Map<String, Object> data = BeanUtils.convertToMap(pNote);
            data.forEach(update::set);
            emptyData.forEach(update::setOnInsert);
            bulkOps.upsert(query, update);
        }
        bulkOps.execute();
    }

    // 随机数据
    public List<PNote> getRandomNote(int sampleSize) {
        Date before30 = DateUtils.addDays(new Date(), -30);
        MatchOperation match = Aggregation.match(
                Criteria.where("visibility").is(true).and("checkType")
                        .is(true).and("status").is("1").and("createTime").gte(before30.getTime()));
        ProjectionOperation project = Aggregation.project("recordId").andExclude("_id");
        AggregationOperation sample = Aggregation.sample(sampleSize);
        Aggregation aggregation = Aggregation.newAggregation(match, sample, project);
        return mongoTemplate.aggregate(aggregation, "note", PNote.class).getMappedResults();
    }

    public List<PNote> getAdoptRandomNote(int sampleSize) {
        MatchOperation match = Aggregation.match(Criteria.where("visibility").is(true).and("checkType").is(true).and("status").is("1").and("waitAdopt").is(true));
        ProjectionOperation project = Aggregation.project("recordId").andExclude("_id");
        AggregationOperation sample = Aggregation.sample(sampleSize);
        Aggregation aggregation = Aggregation.newAggregation(match, sample, project);
        return mongoTemplate.aggregate(aggregation, "note", PNote.class).getMappedResults();
    }

    public List<PNote> getTopicRandomNote(List<String> topics, int sampleSize) {
        MatchOperation match = Aggregation.match(Criteria.where("visibility").is(true).and("checkType").is(true).and("status").is("1").and("topic").in(topics));
        ProjectionOperation project = Aggregation.project("recordId").andExclude("_id");
        AggregationOperation sample = Aggregation.sample(sampleSize);
        Aggregation aggregation = Aggregation.newAggregation(match, sample, project);
        return mongoTemplate.aggregate(aggregation, "note", PNote.class).getMappedResults();
    }

    public List<PNote> getRegionRandomNote(String province, String city, int sampleSize) {
        Date beforeDay = DateUtils.addDays(new Date(), -90);
        Criteria criteria = Criteria.where("visibility").is(true).and("checkType").is(true).and("status")
                .is("1").and("createTime").gte(beforeDay.getTime());
        if (StringUtils.isNotEmpty(city)) {
            criteria.and("city").is(city);
        } else if (StringUtils.isNotEmpty(province)) {
            criteria.and("province").is(province);
        }
        MatchOperation match = Aggregation.match(criteria);
        ProjectionOperation project = Aggregation.project("recordId").andExclude("_id");
        AggregationOperation sample = Aggregation.sample(sampleSize);
        Aggregation aggregation = Aggregation.newAggregation(match, sample, project);
        return mongoTemplate.aggregate(aggregation, "note", PNote.class).getMappedResults();
    }

    public List<PNote> getActivityRandomNote(List<Integer> activityIds, int sampleSize) {
        MatchOperation match = Aggregation.match(Criteria.where("visibility").is(true).and("checkType").is(true).and("status").is("1").and("activityId").in(activityIds));
        ProjectionOperation project = Aggregation.project("recordId").andExclude("_id");
        AggregationOperation sample = Aggregation.sample(sampleSize);
        Aggregation aggregation = Aggregation.newAggregation(match, sample, project);
        return mongoTemplate.aggregate(aggregation, "note", PNote.class).getMappedResults();
    }

    public void syncAdItemToRedis(List<Pair<Integer, Long>> recents, List<Integer> disable) {
        try {
            List result = rebuildRecent(RedisConstants.ad_items, disable, recents, 1000);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to execute Lua script", e);
        }
    }

    public void syncTotalItemToRedis(List<Pair<Integer, Long>> recents, List<Integer> disable) {
        try {
            List result = rebuildRecent(RedisConstants.total_recent_items, disable, recents, 500);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to execute Lua script", e);
        }
    }

    public void syncUserItemToRedis(Map<Integer, List<Pair<Integer, Long>>> recents, Map<Integer, List<Integer>> disable) {
        try {
            Set<Integer> allUser = new HashSet<>();
            allUser.addAll(disable.keySet());
            allUser.addAll(recents.keySet());
            for (Integer user : allUser) {
                List result = rebuildRecent(RedisConstants.creator_recent_items.replace("{user_id}", String.valueOf(user)), disable.getOrDefault(user, ImmutableList.of()), recents.getOrDefault(user, ImmutableList.of()), 20);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to execute Lua script", e);
        }
    }


    public List rebuildRecent(String key, List<Integer> elementsToRemove, List<Pair<Integer, Long>> elementsToAdd, int maxSize) {
        // 验证输入
        if (elementsToRemove == null || elementsToAdd == null) {
            throw new IllegalArgumentException("Input lists cannot be null");
        }

        for (Integer element : elementsToRemove) {
            if (element == null) {
                throw new IllegalArgumentException("elementsToRemove contains null values");
            }
        }

        for (Pair<Integer, Long> pair : elementsToAdd) {
            if (pair == null || pair.getKey() == null || pair.getValue() == null) {
                throw new IllegalArgumentException("elementsToAdd contains null values");
            }
        }

        // Lua 脚本
        String luaScript = "local zset_key = KEYS[1]\n" +
                "local max_size = tonumber(ARGV[1]) or 0\n" +
                "local remove_count = tonumber(ARGV[2]) or 0\n" +
                "-- 删除指定的元素\n" +
                "for i = 3, 2 + remove_count do\n" +
                "    redis.call('ZREM', zset_key, ARGV[i])\n" +
                "end\n" +
                "-- 批量添加元素到 ZSet\n" +
                "local add_start = 3 + remove_count\n" +
                "local add_end = #ARGV\n" +
                "for i = add_start, add_end, 2 do\n" +
                "    local score = tonumber(ARGV[i]) or 0\n" +
                "    local value = ARGV[i + 1]\n" +
                "    redis.call('ZADD', zset_key, score, value)\n" +
                "end\n" +
                "-- 获取 ZSet 中的元素数量\n" +
                "local current_size = redis.call('ZCARD', zset_key)\n" +
                "local excess_count = 0\n" +
                "if current_size > max_size and max_size > 0 then\n" +
                "    excess_count = current_size - max_size\n" +
                "    if excess_count >= current_size then\n" +
                "        excess_count = current_size - 1\n" +
                "    end\n" +
                "    redis.call('ZREMRANGEBYRANK', zset_key, 0, excess_count - 1)\n" +
                "end\n" +
                "-- 返回结果\n" +
                "local result = {}\n" +
                "result[1] = max_size\n" +
                "result[2] = remove_count\n" +
                "result[3] = current_size\n" +
                "result[4] = excess_count\n" +
                "return result";

        // 构建 Lua 脚本参数
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(maxSize));
        args.add(String.valueOf(elementsToRemove.size()));

        elementsToRemove.forEach(element -> {
            args.add(String.valueOf(element));
        });

        elementsToAdd.forEach(pair -> {
            args.add(String.valueOf(pair.getValue()));  // 先添加分数 (score)
            args.add(String.valueOf(pair.getKey()));    // 再添加元素值 (value)
        });
        // 执行 Lua 脚本
        return redisHelper.executeLuaScript(luaScript, List.of(key), args, List.class);
    }
}
