package com.sofm.recommend.infrastructure.redis;

import com.google.common.collect.ImmutableSet;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class RedisHelper {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisHelper(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public RedisSerializer<String> getStringSerializer() {
        return this.redisTemplate.getStringSerializer();
    }

// ================= Value（字符串）操作 =================

    public void setValue(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void batchSetKeys(Map<String, String> keyValuePairs) {
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            keyValuePairs.forEach((key, value) ->
                    connection.set(redisTemplate.getStringSerializer().serialize(key),
                            redisTemplate.getStringSerializer().serialize(value)));
            return null;
        });
    }

    public Object getValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }


    public List<Object> batchGetKeys(List<String> keys) {
        return redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (String key : keys) {
                connection.stringCommands().get(redisTemplate.getStringSerializer().serialize(key));
            }
            return null; // pipeline 中不需要返回值
        });
    }


    // ================= List（列表）操作 =================

    public void leftPush(String key, Object value) {
        redisTemplate.opsForList().leftPush(key, value);
    }

    public Object leftPop(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    public List<Object> lRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    // ================= Set（集合）操作 =================

    public void addSet(String key, Object... values) {
        redisTemplate.opsForSet().add(key, values);
    }

    public Set<Object> getSetMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    public boolean isMember(String key, Object value) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
    }

    // ================= Hash（哈希）操作 =================

    public void putHash(String key, Object hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    public Object getHashValue(String key, Object hashKey) {
        return redisTemplate.opsForHash().get(key, hashKey);
    }

    public Map<Object, Object> getHashEntries(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    // ================= ZSet（有序集合）操作 =================

    /**
     * 添加元素到 ZSet 中
     *
     * @param key   ZSet 的 key
     * @param value 元素的值
     * @param score 元素的分数（用于排序）
     */
    public void addZSet(String key, Object value, double score) {
        redisTemplate.opsForZSet().add(key, value, score);
    }

    /**
     * 添加元素到 ZSet 中
     *
     * @param key    ZSet 的 key
     * @param values 元素的值
     * @param score  元素的分数（用于排序）
     */
    public void addToZSetWithPipeline(String key, List<String> values, double score) {
        redisTemplate.executePipelined((RedisCallback<?>) (connection) -> {
            ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
            values.forEach((value) -> zSetOps.add(key, value, score));
            return null;  // Pipeline 不需要返回结果
        });
    }

    /**
     * 添加元素到 ZSet 中
     *
     * @param key     ZSet 的 key
     * @param updates 元素的值
     */
    public void addToZSetWithLuaScript(String key, Map<String, Double> updates, int maxSize) {
        String luaScript =
                "for i = 1, #ARGV - 1, 2 do "  // 每两个参数为一组
                        + "  local member = ARGV[i]; "
                        + "  local score = tonumber(ARGV[i + 1]); "
                        + "  redis.call('ZADD', KEYS[1], score, member); "
                        + "end; "
                        + "local size = redis.call('ZCARD', KEYS[1]); "
                        + "if size > tonumber(ARGV[#ARGV]) then "
                        + "  redis.call('ZREMRANGEBYRANK', KEYS[1], 0, size - tonumber(ARGV[#ARGV]) - 1); "
                        + "end;";

        List<String> keys = Collections.singletonList(key);
        List<String> args = new ArrayList<>();
        updates.forEach((member, score) -> {
            args.add(member);
            args.add(String.valueOf(score));
        });
        args.add(String.valueOf(maxSize));
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(luaScript);
        redisScript.setResultType(Long.class);
        // 执行 Lua 脚本
        redisTemplate.execute(redisScript, keys, args.toArray());
    }

    /**
     * 获取指定分数范围内的元素
     *
     * @param key      ZSet 的 key
     * @param minScore 最小分数
     * @param maxScore 最大分数
     * @return 符合条件的元素集合
     */
    public Set<Object> rangeByScore(String key, double minScore, double maxScore) {
        return redisTemplate.opsForZSet().rangeByScore(key, minScore, maxScore);
    }

    /**
     * 获取 ZSet 中的所有元素，按分数从小到大排序
     *
     * @param key ZSet 的 key
     * @return 有序的元素集合
     */
    public Set<Object> zRange(String key, boolean asc, boolean isScore, long start, long end) {
        if (asc) {
            if (isScore) {
                return redisTemplate.opsForZSet().rangeByScore(key, start, end);
            } else {
                return redisTemplate.opsForZSet().range(key, start, end);
            }
        } else {
            if (isScore) {
                return redisTemplate.opsForZSet().reverseRangeByScore(key, start, end);
            } else {
                return redisTemplate.opsForZSet().reverseRange(key, start, end);
            }
        }
    }

    /**
     * 获取 ZSet 中的元素总数
     *
     * @param key ZSet 的 key
     * @return 元素总数
     */
    public Long zSetSize(String key) {
        return redisTemplate.opsForZSet().zCard(key);
    }

    /**
     * 获取指定元素的分数
     *
     * @param key   ZSet 的 key
     * @param value 元素的值
     * @return 元素的分数
     */
    public Double getScore(String key, Object value) {
        return redisTemplate.opsForZSet().score(key, value);
    }

    /**
     * 删除 ZSet 中的一个或多个元素
     *
     * @param key    ZSet 的 key
     * @param values 要删除的元素
     * @return 删除的元素数量
     */
    public Long removeZSetValues(String key, Object... values) {
        return redisTemplate.opsForZSet().remove(key, values);
    }

    /**
     * 获取 ZSet 中某个元素的排名（从小到大排序）
     *
     * @param key   ZSet 的 key
     * @param value 元素的值
     * @return 元素的排名
     */
    public Long getRank(String key, Object value) {
        return redisTemplate.opsForZSet().rank(key, value);
    }

    /**
     * 获取 ZSet 中某个元素的倒序排名（从大到小排序）
     *
     * @param key   ZSet 的 key
     * @param value 元素的值
     * @return 元素的倒序排名
     */
    public Long getReverseRank(String key, Object value) {
        return redisTemplate.opsForZSet().reverseRank(key, value);
    }

    /**
     * 按排名范围获取元素（从小到大排序）
     *
     * @param key   ZSet 的 key
     * @param start 起始排名
     * @param end   结束排名
     * @return 符合条件的元素集合
     */
    public Set<Object> rangeByRank(String key, long start, long end) {
        return redisTemplate.opsForZSet().range(key, start, end);
    }

    /**
     * 按排名范围获取元素（从大到小排序）
     *
     * @param key   ZSet 的 key
     * @param start 起始排名
     * @param end   结束排名
     * @return 符合条件的元素集合
     */
    public Set<Object> reverseRangeByRank(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRangeByScore(key, start, end);
    }

    /**
     * 增加指定元素的分数
     *
     * @param key   ZSet 的 key
     * @param value 元素的值
     * @param delta 增加的分数值
     * @return 更新后的分数
     */
    public Double incrementScore(String key, Object value, double delta) {
        return redisTemplate.opsForZSet().incrementScore(key, value, delta);
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
        return redisTemplate.opsForZSet().removeRangeByScore(key, minScore, maxScore);
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
        return redisTemplate.opsForZSet().removeRange(key, start, end);
    }

    public Object getRandomElementFromZSet(String key) {
        return redisTemplate.execute((RedisCallback<Object>) connection ->
                connection.execute("ZRANDMEMBER", redisTemplate.getStringSerializer().serialize(key))
        );
    }

    public Set<Object> zUnion(List<String> keys, String destKey) {
        if (!keys.isEmpty()) {
            return Collections.singleton(redisTemplate.opsForZSet().unionAndStore(keys.get(0), keys, destKey));
        }
        return ImmutableSet.of();
    }

    // ================= 布隆过滤器操作 =================

    // 使用 Lua 脚本批量添加元素到布隆过滤器
    public void addToBloomFilterWithLua(String filterName, List<String> values) {
        String script =
                "local filterName = KEYS[1] " +
                        "local values = {} " +
                        // 在这里直接定义误判率和容量
                        "local errorRate = 0.001 " +   // 误判率为 0.1%
                        "local capacity = 100000 " +   // 容量为 100,000
                        // 收集所有的 ARGV 作为要添加的值
                        "for i = 1, #ARGV do " +
                        "    table.insert(values, ARGV[i]) " +
                        "end " +
                        // 检查布隆过滤器是否存在
                        "local exists = redis.call('EXISTS', filterName) " +
                        "if exists == 0 then " +
                        "    redis.call('BF.RESERVE', filterName, errorRate, capacity) " +
                        "end " +
                        // 添加元素到布隆过滤器
                        "local results = {} " +
                        "for i, value in ipairs(values) do " +
                        "    local result = redis.call('BF.ADD', filterName, value) " +
                        "    table.insert(results, result) " +
                        "end " +
                        "return results";
        DefaultRedisScript<List> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(script);
        redisScript.setResultType(List.class);
        List<String> keys = Collections.singletonList(filterName);
        Object[] args = values.toArray();
        redisTemplate.execute(redisScript, keys, args);
    }

    // 检查元素是否在布隆过滤器中
    public Boolean bloomFilterExists(String filterName, String value) {
        return redisTemplate.execute((RedisCallback<Boolean>) connection -> connection.execute("BF.EXISTS", redisTemplate.getStringSerializer().serialize(filterName), redisTemplate.getStringSerializer().serialize(value)) != null);
    }

    // 使用 BF.MEXISTS 检查多个值是否存在于指定的布隆过滤器中
    public List<Long> checkValuesInBloomFilter(String filterName, List<String> values) {
        // 定义 Lua 脚本
        String script = "return redis.call('BF.MEXISTS', KEYS[1], unpack(ARGV))";

        // 设置 RedisScript 对象
        DefaultRedisScript<List> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(script);
        redisScript.setResultType(List.class);

        // 执行脚本
        List<Long> result = (List<Long>) redisTemplate.execute(redisScript, Collections.singletonList(filterName), values.toArray());

        return result;
    }

    // 获取在布隆过滤器中不存在的值
    public List<String> getNonExistentValues(List<String> filterNames, List<String> values) {
        List<String> nonExistentValues = new ArrayList<>();
        List<List<Long>> existsResults = new ArrayList<>();
        for (String filterName : filterNames) {
            existsResults.add(checkValuesInBloomFilter(filterName, values));
        }
        for (int i = 0; i < values.size(); i++) {
            boolean non = true;
            for (List<Long> exists : existsResults) {
                if (exists.get(i) == 1) {
                    non = false;
                    break;
                }
            }
            if (non) {
                nonExistentValues.add(values.get(i));
            }
        }

        return nonExistentValues;
    }


    // ================= 通用操作 =================

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public void batchDeleteKeys(List<String> keys) {
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (String key : keys) {
                connection.del(redisTemplate.getStringSerializer().serialize(key));
            }
            return null;
        });
    }

    // 检查键是否存在
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void expire(String key, long timeout, TimeUnit timeUnit) {
        redisTemplate.expire(key, timeout, timeUnit);
    }

    public void expireAt(String key, Date expireDate) {
        redisTemplate.expireAt(key, expireDate);
    }

    // lua
    public <T> T executeLuaScript(String scriptText, List<String> keys, List<String> args, Class<T> resultType) {
        DefaultRedisScript<T> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(scriptText);
        redisScript.setResultType(resultType);
        return redisTemplate.execute(redisScript, keys, args.toArray());
    }

    // pipeline
    public List<Object> executePipelined(RedisCallback<?> action) {
        return redisTemplate.executePipelined(action);
    }

}
