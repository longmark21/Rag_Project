package com.example.rag.config;

import com.example.rag.mock.ChatClient;
import com.example.rag.service.KimiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 聊天模型配置类
 * 根据配置动态注入对应的 ChatClient，默认走 Kimi
 */
@Configuration
public class ChatModelConfig {

    @Value("${spring.ai.default-model:kimi}")
    private String defaultModel;

    @Value("${spring.ai.moonshot.api-key}")
    private String moonshotApiKey;

    @Value("${spring.ai.moonshot.base-url:https://api.moonshot.cn/v1}")
    private String moonshotBaseUrl;

    @Value("${spring.ai.moonshot.chat.options.model:moonshot-k2-thinking}")
    private String moonshotModel;

    @Value("${spring.ai.moonshot.chat.options.temperature:0.3}")
    private Double moonshotTemperature;

    @Value("${spring.ai.dashscope.chat.options.max-tokens:2000}")
    private Integer maxTokens;

    /**
     * 注入 KimiService
     */
    @Bean
    public KimiService kimiService() {
        return new KimiService();
    }

    /**
     * 注入 ChatClient
     * 使用真实的 Kimi API
     */
    @Bean
    public ChatClient chatClient(KimiService kimiService) {
        System.out.println("使用 Kimi 作为默认聊天模型");
        return ChatClient.ChatClientBuilder.builder(kimiService).build();
    }
}
