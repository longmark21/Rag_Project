package com.example.rag.mock;

import com.example.rag.service.KimiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * ChatClient 类，用于调用真实的 Kimi API
 */
@Slf4j
@RequiredArgsConstructor
public class ChatClient {

    private final KimiService kimiService;

    /**
     * 使用 Kimi API 生成回答
     */
    public PromptBuilder prompt(String prompt) {
        return new PromptBuilder(prompt);
    }

    /**
     * PromptBuilder 类
     */
    public class PromptBuilder {
        private final String prompt;

        public PromptBuilder(String prompt) {
            this.prompt = prompt;
        }

        /**
         * 调用 Kimi API
         */
        public Response call() {
            log.info("调用 Kimi API 生成回答");
            return new Response(prompt);
        }
    }

    /**
     * Response 类
     */
    public class Response {
        private final String content;

        public Response(String prompt) {
            if (kimiService != null) {
                this.content = kimiService.chat(prompt);
            } else {
                this.content = "Kimi 服务未初始化";
            }
        }

        /**
         * 获取回答内容
         */
        public String content() {
            return content;
        }
    }

    /**
     * ChatClientBuilder 类
     */
    public static class ChatClientBuilder {
        private KimiService kimiService;

        /**
         * 构建 ChatClient
         */
        public static ChatClientBuilder builder(KimiService kimiService) {
            ChatClientBuilder builder = new ChatClientBuilder();
            builder.kimiService = kimiService;
            return builder;
        }

        /**
         * 构建 ChatClient 实例
         */
        public ChatClient build() {
            return new ChatClient(kimiService);
        }
    }
}