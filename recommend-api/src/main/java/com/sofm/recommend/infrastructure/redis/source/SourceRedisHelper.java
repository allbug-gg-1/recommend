package com.sofm.recommend.infrastructure.redis.source;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class SourceRedisHelper {


    private final RedisTemplate<String, String> sourceRedisTemplate;

    public SourceRedisHelper(@Qualifier("sourceRedisTemplate") RedisTemplate<String, String> sourceRedisTemplate) {
        this.sourceRedisTemplate = sourceRedisTemplate;
    }

    // ================= ZSet（有序集合）操作 =================

    /**
     * 获取 ZSet 中的所有元素，按分数从小到大排序
     *
     * @param key ZSet 的 key
     * @return 有序的元素集合
     */
    public Set<String> rangeAll(String key) {
        return sourceRedisTemplate.opsForZSet().range(key, 0, -1);
    }

    /**
     * 获取 ZSet 中的元素总数
     *
     * @param key ZSet 的 key
     * @return 元素总数
     */
    public Long zSetSize(String key) {
        return sourceRedisTemplate.opsForZSet().zCard(key);
    }

    /**
     * 获取指定元素的分数
     *
     * @param key   ZSet 的 key
     * @param value 元素的值
     * @return 元素的分数
     */
    public Double getScore(String key, Object value) {
        return sourceRedisTemplate.opsForZSet().score(key, value);
    }

    /**
     * 删除 ZSet 中的一个或多个元素
     *
     * @param key    ZSet 的 key
     * @param values 要删除的元素
     * @return 删除的元素数量
     */
    public Long removeZSetValues(String key, Object... values) {
        return sourceRedisTemplate.opsForZSet().remove(key, values);
    }

    /**
     * 获取 ZSet 中某个元素的排名（从小到大排序）
     *
     * @param key   ZSet 的 key
     * @param value 元素的值
     * @return 元素的排名
     */
    public Long getRank(String key, Object value) {
        return sourceRedisTemplate.opsForZSet().rank(key, value);
    }

    /**
     * 获取 ZSet 中某个元素的倒序排名（从大到小排序）
     *
     * @param key   ZSet 的 key
     * @param value 元素的值
     * @return 元素的倒序排名
     */
    public Long getReverseRank(String key, Object value) {
        return sourceRedisTemplate.opsForZSet().reverseRank(key, value);
    }

    /**
     * 删除指定分数范围内的元素
     *
     * @param key      ZSet 的 key
     * @param minScore 最小分数
     * @param maxScore 最大分数
     * @return 删除的元素数量
     */
    public Long removeRangeByScore(String key, double minScore, double maxScore) {
        return sourceRedisTemplate.opsForZSet().removeRangeByScore(key, minScore, maxScore);
    }

    /**
     * 删除指定排名范围内的元素（从小到大）
     *
     * @param key   ZSet 的 key
     * @param start 起始排名
     * @param end   结束排名
     * @return 删除的元素数量
     */
    public Long removeRangeByRank(String key, long start, long end) {
        return sourceRedisTemplate.opsForZSet().removeRange(key, start, end);
    }

    // ================= 通用操作 =================

    public void delete(String key) {
        sourceRedisTemplate.delete(key);
    }

    // 检查键是否存在
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(sourceRedisTemplate.hasKey(key));
    }

    // 检查键是否存在
    public String randomKey() {
        return sourceRedisTemplate.randomKey();
    }

    // lua
    public <T> T executeLuaScript(String scriptText, List<String> keys, List<String> args, Class<T> resultType) {
        DefaultRedisScript<T> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(scriptText);
        redisScript.setResultType(resultType);
        return sourceRedisTemplate.execute(redisScript, keys, args.toArray());
    }

    // pipeline
    public List<Object> executePipelined(RedisCallback<?> action) {
        return sourceRedisTemplate.executePipelined(action);
    }

}
