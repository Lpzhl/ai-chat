package com.lzh.ai.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/paperReview")
public class PaperReviewController {

    private static final Logger logger = Logger.getLogger(PaperReviewController.class.getName());

    @Value("${app.authorization.token}")
    private String authToken;

    @PostMapping
    public String paperReview(@RequestParam("text") String text) throws IOException {
        logger.info("Request received at '/paperReview' - Method: POST");

        // Step 1: 创建并保存文档
        File tempFile = createDocxFile(text);

        // Step 2: 准备外部API请求的Header和Payload
        String url = "https://qianfan.baidubce.com/v2/app/conversation/file/upload";
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Appbuilder-Authorization", "Bearer " + authToken);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // 准备Payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("app_id", "8f818d20-13ea-48ff-b362-902f60806a04");

        // Step 3: 通过RestTemplate发送POST请求
        RestTemplate restTemplate = new RestTemplate();

        // 构建多部分请求
        Map<String, Object> body = new HashMap<>();
        body.put("file", tempFile);

        // 执行请求
        ResponseEntity<String> response = restTemplate.postForEntity(url, body, String.class, payload);

        // Step 4: 处理响应，提取document_id和conversation_id
        String responseJson = response.getBody();
        // 假设响应是包含'id'和'conversation_id'的JSON
        String documentId = extractJsonField(responseJson, "id");
        String conversationId = extractJsonField(responseJson, "conversation_id");

        logger.info("Document ID: " + documentId);
        logger.info("Conversation ID: " + conversationId);

        // Step 5: 返回AI请求的论文评审结果
        return requestAiReview(documentId, conversationId);
    }

    private File createDocxFile(String text) throws IOException {
        // 将输入的文本保存为.docx文件
        File docFile = new File("output.docx");
        Files.write(docFile.toPath(), text.getBytes());
        return docFile;
    }

    private String extractJsonField(String json, String field) {
        // 实现简单的JSON解析，提取特定字段（可以使用Jackson或Gson）
        // 这是一个简单的占位方法
        return json.split("\"" + field + "\":\"")[1].split("\"")[0];
    }

    private String requestAiReview(String documentId, String conversationId) {
        // 假设你会向AI服务发送请求进行论文评审
        // 这个步骤也可以通过RestTemplate或其他方法来完成
        return "AI评审请求已发送，文档ID: " + documentId + " 和 会话ID: " + conversationId;
    }
}
