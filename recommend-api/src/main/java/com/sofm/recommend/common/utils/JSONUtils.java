package com.sofm.recommend.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONUtils {

    // 创建一个全局的 ObjectMapper 实例，ObjectMapper 是线程安全的
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 将对象序列化为 JSON 字符串
     *
     * @param object 要序列化的对象
     * @return JSON 字符串
     */
    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("序列化对象到 JSON 时出错", e);
        }
    }

    /**
     * 将 JSON 字符串反序列化为对象
     *
     * @param json  JSON 字符串
     * @param clazz 目标对象的类类型
     * @param <T>   返回的对象类型
     * @return 反序列化后的对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("反序列化 JSON 到对象时出错", e);
        }
    }

    /**
     * 将 JSON 字符串反序列化为泛型对象
     *
     * @param json          JSON 字符串
     * @param typeReference Jackson 提供的类型引用，用于处理泛型
     * @param <T>           返回的对象类型
     * @return 反序列化后的对象
     */
    public static <T> T fromJson(String json, com.fasterxml.jackson.core.type.TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("反序列化 JSON 到泛型对象时出错", e);
        }
    }
}

