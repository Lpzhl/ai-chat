package com.lzh.ai.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        // 根据您的 Redis 配置调整
        config.useSingleServer().setAddress("redis://xxx110:6379").setPassword("xxun");

        return Redisson.create(config);
    }
}
