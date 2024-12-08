package com.lzh.ai.utils;

public class RedisKey {

    public static final String API_LIMIT_IP_TIMES = "API_LIMIT_IP_TIMES";

    /**
     * 拼接 Redis key
     *
     * @param keyPrefix 前缀
     * @param args      参数
     * @return 拼接后的 key
     */
    public static String getKey(String keyPrefix, Object... args) {
        StringBuilder keyBuilder = new StringBuilder(keyPrefix);
        for (Object arg : args) {
            keyBuilder.append(":").append(arg);
        }
        return keyBuilder.toString();
    }
}
