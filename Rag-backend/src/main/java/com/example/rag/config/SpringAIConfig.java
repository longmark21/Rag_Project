package com.example.rag.config;

import com.example.rag.mock.EmbeddingModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI 配置类
 * 配置 EmbeddingModel 等 AI 相关组件
 */
@Configuration
public class SpringAIConfig {

    /**
     * 配置 EmbeddingModel
     * 使用模拟实现
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        return new EmbeddingModel();
    }
}
