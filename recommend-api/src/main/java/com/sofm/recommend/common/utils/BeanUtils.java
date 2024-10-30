package com.sofm.recommend.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class BeanUtils {

    public static Map<String, Object> convertToMap(Object obj) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(obj, Map.class);
    }

    public static <T> T convertMapToClass(Map<String, Object> map, Class<T> clazz) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(map, clazz);
    }
}
