package com.sofm.recommend.infrastructure.kafka.streams;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sofm.recommend.application.scheduler.HeatUpdateScheduler;
import com.sofm.recommend.common.dto.MessageDto;
import com.sofm.recommend.common.utils.JSONUtils;
import com.sofm.recommend.infrastructure.redis.RedisHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.WindowStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class KafkaStreamsService {

    @Autowired
    private RedisHelper redisHelper;
    @Autowired
    private HeatUpdateScheduler heatUpdateScheduler;

    // 使用Caffeine缓存来去重，减少对Redis的重复写入
    private final Cache<String, Double> heatCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(10)) // 缓存10分钟，防止过多无效更新
            .build();


    public void createHeatAggregationTopology(StreamsBuilder builder) {
        KStream<String, String> inputStream = builder.stream("ugc-stream");

        // 过滤不同类型的交互事件
        Map<String, KStream<String, String>> interactionStreams = new HashMap<>();
        interactionStreams.put("CLICK", inputStream.filter((key, value) -> isEventType(value, "CLICK")));
        interactionStreams.put("LIKE", inputStream.filter((key, value) -> isEventType(value, "LIKE")));
        interactionStreams.put("COMMENT", inputStream.filter((key, value) -> isEventType(value, "COMMENT")));
        interactionStreams.put("SHARE", inputStream.filter((key, value) -> isEventType(value, "SHARE")));

        // 定义跳跃窗口及其权重
        Map<String, TimeWindows> hoppingWindows = new HashMap<>();
        hoppingWindows.put("5m", TimeWindows.ofSizeAndGrace(Duration.ofMinutes(5), Duration.ofMinutes(1)).advanceBy(Duration.ofMinutes(1)));
        hoppingWindows.put("30m", TimeWindows.ofSizeAndGrace(Duration.ofMinutes(30), Duration.ofMinutes(5)).advanceBy(Duration.ofMinutes(5)));
        hoppingWindows.put("3h", TimeWindows.ofSizeAndGrace(Duration.ofHours(3), Duration.ofMinutes(30)).advanceBy(Duration.ofMinutes(30)));
        hoppingWindows.put("1d", TimeWindows.ofSizeAndGrace(Duration.ofDays(1), Duration.ofHours(6)).advanceBy(Duration.ofHours(6)));
        hoppingWindows.put("7d", TimeWindows.ofSizeAndGrace(Duration.ofDays(7), Duration.ofDays(1)).advanceBy(Duration.ofDays(1)));

        // 定义交互类型及其权重
        Map<String, Double> interactionWeights = new HashMap<>();
        interactionWeights.put("CLICK", 0.5);
        interactionWeights.put("LIKE", 1.0);
        interactionWeights.put("COMMENT", 1.0);
        interactionWeights.put("SHARE", 1.1);

        // 计算每种交互类型在不同时间窗口内的热度值
        KTable<String, Double> totalHeat = null;

        for (Map.Entry<String, KStream<String, String>> interactionEntry : interactionStreams.entrySet()) {
            String interactionType = interactionEntry.getKey();
            KStream<String, String> stream = interactionEntry.getValue();
            double interactionWeight = interactionWeights.get(interactionType);

            for (Map.Entry<String, TimeWindows> windowEntry : hoppingWindows.entrySet()) {
                String windowName = windowEntry.getKey();
                TimeWindows window = windowEntry.getValue();
                // 分组聚合统计交互次数并去重限流
                KTable<Windowed<String>, Long> interactionCounts = stream
                        .groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
                        .windowedBy(window)
                        .aggregate(
                                () -> 0L,
                                (key, value, aggregate) -> isDuplicateInteraction(key, value, aggregate, windowName) ? aggregate : aggregate + 1,
                                Materialized.<String, Long, WindowStore<Bytes, byte[]>>as(windowName + "-" + interactionType + "-counts-store")
                                        .withKeySerde(Serdes.String())
                                        .withValueSerde(Serdes.Long())
                        );

                KTable<String, Double> weightedHeat = aggregateHeat(interactionCounts, interactionWeight);
                if (totalHeat == null) {
                    totalHeat = weightedHeat;
                } else {
                    totalHeat = totalHeat.leftJoin(weightedHeat, this::sumValues);
                }
            }
        }
        if (totalHeat != null) {
            totalHeat.toStream().foreach((itemId, heatScore) -> {
                System.out.println(itemId + "----------->" + heatScore);
                Double cachedHeatScore = heatCache.getIfPresent(itemId);
                if (cachedHeatScore == null || !cachedHeatScore.equals(heatScore)) {
                    heatUpdateScheduler.addToUpdate(itemId, heatScore);
                    heatCache.put(itemId, heatScore);
                }
            });
        }
    }

    // 去重逻辑：防止刷点击，针对用户对物品的多次交互行为
    private boolean isDuplicateInteraction(String key, String value, Long currentCount, String windowName) {
        // 假设 key 是 itemId，value 是 userId
        MessageDto msg = JSONUtils.fromJson(value, MessageDto.class);
        // 设置每分钟的交互次数上限
        if (currentCount >= 5) { // 每分钟最多 5 次交互
            log.info("Rate limit exceeded for userId: {}, itemId: {},count:{},windowName:{}", msg.getUserId(), key, currentCount, windowName);
            return true;
        }
        return false;
    }

    // 打印每个窗口内的最新聚合结果
    private KTable<String, Double> aggregateHeat(KTable<Windowed<String>, Long> clickCounts, double weight) {
        return clickCounts.toStream()
                .map((windowedKey, count) -> {
                    double adjustedCount = Math.log1p(count) * weight;
                    return new KeyValue<>(windowedKey.key(), adjustedCount);
                })
                .groupByKey(Grouped.with(Serdes.String(), Serdes.Double()))
                .reduce(Double::sum);
    }

    private Double sumValues(Double v1, Double v2) {
        if (v1 == null && v2 == null) {
            return 0.0;
        } else if (v1 == null) {
            return v2;
        } else if (v2 == null) {
            return v1;
        } else {
            return v1 + v2;
        }
    }

    private boolean isEventType(String value, String event) {
        return value.contains(event);
    }
}
