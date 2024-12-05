package com.lzh.ai.config;


import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;


@Configuration
@Data
public class ApiConfig {
    @Value("${springai.dashscope.base-url}")
    private String baseUrl;

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

}
