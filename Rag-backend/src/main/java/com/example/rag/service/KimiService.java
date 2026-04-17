package com.example.rag.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kimi API 服务
 */
@Slf4j
@Service
public class KimiService {

    @Value("${spring.ai.moonshot.api-key}")
    private String apiKey;

    @Value("${spring.ai.moonshot.base-url:https://api.moonshot.cn/v1}")
    private String baseUrl;

    @Value("${spring.ai.moonshot.chat.options.model:moonshot-k2-thinking}")
    private String model;

    @Value("${spring.ai.moonshot.chat.options.temperature:0.3}")
    private Double temperature;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 调用 Kimi API 生成回答
     * 
     * @param prompt 提示词
     * @return AI 生成的回答
     */
    public String chat(String prompt) {
        try {
            log.info("调用 Kimi API: model={}, prompt 长度={}", model, prompt.length());

            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", List.of(
                Map.of("role", "user", "content", prompt)
            ));
            requestBody.put("temperature", temperature);
            requestBody.put("max_tokens", 2000);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 发送请求
            ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/chat/completions",
                entity,
                Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    Map<String, String> message = (Map<String, String>) choice.get("message");
                    String content = message.get("content");
                    log.info("Kimi API 调用成功");
                    return content;
                }
            }

            log.error("Kimi API 响应异常：{}", response);
            return "抱歉，调用 Kimi API 时出现错误，请稍后重试。";

        } catch (Exception e) {
            log.error("Kimi API 调用失败", e);
            if (e.getMessage() != null && e.getMessage().contains("401")) {
                return "API Key 验证失败，请检查 Kimi API Key 是否正确。错误信息：" + e.getMessage();
            } else if (e.getMessage() != null && e.getMessage().contains("403")) {
                return "API Key 余额不足或权限不足，请检查 Kimi 账户状态。错误信息：" + e.getMessage();
            } else if (e.getMessage() != null && e.getMessage().contains("429")) {
                return "请求过于频繁，请稍后重试。错误信息：" + e.getMessage();
            } else if (e.getMessage() != null && e.getMessage().contains("500")) {
                return "Kimi 服务器内部错误，请稍后重试。错误信息：" + e.getMessage();
            } else {
                return "调用 Kimi API 失败：" + e.getMessage();
            }
        }
    }
}
