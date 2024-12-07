package com.lzh.ai.controller;

import com.lzh.ai.config.ApiConfig;
import com.lzh.ai.strategy.TextProcessingStrategy;
import com.lzh.ai.strategyFactory.TextProcessingStrategyFactory;
import jakarta.annotation.Resource;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/stream")
public class StreamController {

    @Resource
    private ApiConfig apiConfig;

    @Resource
    private TextProcessingStrategyFactory strategyFactory;

    @PostMapping("/generate")
    public ResponseEntity<StreamingResponseBody> streamData(
            @RequestParam("type") String type,
            @RequestBody String userQuery) throws IOException {

        // 根据 'type' 参数选择适当的策略
        TextProcessingStrategy strategy = strategyFactory.getStrategy(type);
        String prompt = strategy.generatePrompt(userQuery);

        // 创建用户消息
        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", prompt);

        JSONArray messages = new JSONArray();
        messages.put(message);

        // 准备外部API的请求体
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "qwen-turbo");
        requestBody.put("messages", messages);
        requestBody.put("result_format", "message");
        requestBody.put("stream", true);
        requestBody.put("incremental_output", true);

        // 创建 StreamingResponseBody 以处理流式响应
        StreamingResponseBody responseBody = outputStream -> {
            // 设置与外部API的HTTP连接
            URL url = new URL(apiConfig.getBaseUrl());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + apiConfig.getApiKey());
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // 发送请求体
            try (OutputStream os = connection.getOutputStream()) {
                os.write(requestBody.toString().getBytes(StandardCharsets.UTF_8));
            }

            // 读取并流式传输外部API的响应
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    if (inputLine.startsWith("data:")) {
                        String jsonResponse = inputLine.substring(5).trim();
                        try {
                            JSONObject response = new JSONObject(jsonResponse);
                            String content = response.getJSONArray("choices")
                                    .getJSONObject(0)
                                    .getJSONObject("delta")
                                    .optString("content", "");

                            if (!content.isEmpty()) {
                                outputStream.write(content.getBytes(StandardCharsets.UTF_8));
                                outputStream.flush();
                            }
                        } catch (Exception e) {
                            // 处理解析异常或根据需要记录日志
                        }
                    }
                }
            } finally {
                connection.disconnect();
            }
        };

        // 返回流式响应
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(responseBody);
    }
}
