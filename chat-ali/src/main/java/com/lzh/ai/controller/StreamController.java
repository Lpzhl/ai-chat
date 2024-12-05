package com.lzh.ai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lzh.ai.config.ApiConfig;
import jakarta.annotation.Resource;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/stream")
public class StreamController {

    @Resource
    private  ApiConfig apiConfig;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/generate")
    public ResponseEntity<StreamingResponseBody> streamData(@RequestBody String userQuery) throws IOException {
        // 创建流式响应体
        StreamingResponseBody responseBody = outputStream -> {
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", userQuery);

            JSONArray messages = new JSONArray();
            messages.put(message);

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "qwen-turbo");
            requestBody.put("messages", messages);
            requestBody.put("result_format", "message");
            requestBody.put("stream", true);
            requestBody.put("incremental_output", true);

            // 设置 HTTP 连接
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

            // 处理响应流
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    // 去除 "data:" 前缀并解析
                    if (inputLine.startsWith("data:")) {
                        String jsonResponse = inputLine.substring(5).trim();
                        try {
                            // 解析并提取 content 字段
                            org.json.JSONObject response = new org.json.JSONObject(jsonResponse);
                            String content = response.getJSONArray("choices")
                                    .getJSONObject(0)
                                    .getJSONObject("delta")
                                    .getString("content");
                            // 将内容写入输出流，实时返回给前端
                            outputStream.write(content.getBytes(StandardCharsets.UTF_8));
                            outputStream.flush();  // 确保数据被发送到客户端
                        } catch (Exception e) {

                        }
                    }
                }
            }
        };

        // 设置响应头，返回流式响应
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(responseBody);
    }
}
