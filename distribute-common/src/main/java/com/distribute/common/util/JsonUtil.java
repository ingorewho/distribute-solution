package com.distribute.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @Author renzhiqiang
 * @Description json工具类
 * @Date 2019-08-21
 **/
public final class JsonUtil {
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static String obj2json(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }

        return null;
    }
}
