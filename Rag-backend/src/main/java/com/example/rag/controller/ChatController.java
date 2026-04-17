package com.example.rag.controller; // 包声明，定义了类的所属包

import com.example.rag.dto.ChatRequest; // 导入ChatRequest类，用于接收聊天请求
import com.example.rag.dto.ChatResponse; // 导入ChatResponse类，用于返回聊天响应
import com.example.rag.entity.Message; // 导入Message类，用于表示消息实体
import com.example.rag.service.RagService; // 导入RagService类，用于处理RAG逻辑
import lombok.RequiredArgsConstructor; // 导入@RequiredArgsConstructor注解，自动生成构造函数
import lombok.extern.slf4j.Slf4j; // 导入@Slf4j注解，自动生成日志对象
import org.springframework.http.ResponseEntity; // 导入ResponseEntity类，用于构建HTTP响应
import org.springframework.web.bind.annotation.*; // 导入Spring Web注解

import java.util.List; // 导入List类

@Slf4j // 自动生成日志对象
@RestController // 标记为REST控制器
@RequestMapping("/chat") // 定义请求路径前缀
@RequiredArgsConstructor // 自动生成构造函数，注入依赖
public class ChatController { // 聊天控制器类

    private final RagService ragService; // 注入RagService实例

    @PostMapping // 处理POST请求，路径为/chat
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) { // 处理聊天请求的方法
        try { // 尝试执行
            String answer = ragService.chat(request); // 调用ragService处理聊天请求
            ChatResponse response = ChatResponse.builder()
                .answer(answer)
                .build();
            return ResponseEntity.ok(response); // 返回成功响应
        } catch (Exception e) { // 捕获异常
            log.error("问答失败", e); // 记录错误日志
            return ResponseEntity.badRequest() // 返回错误响应
                .body(ChatResponse.builder() // 构建ChatResponse
                    .answer("抱歉，回答您的问题时出现错误: " + e.getMessage()) // 设置错误信息
                    .build()); // 构建响应体
        }
    }

    @PostMapping("/question") // 处理POST请求，路径为/chat/question
    public ResponseEntity<ChatResponse> question(@RequestBody ChatRequest request) { // 处理问答请求的方法
        try { // 尝试执行
            String answer = ragService.chat(request); // 调用ragService处理聊天请求
            ChatResponse response = ChatResponse.builder()
                .answer(answer)
                .build();
            return ResponseEntity.ok(response); // 返回成功响应
        } catch (Exception e) { // 捕获异常
            log.error("问答失败", e); // 记录错误日志
            return ResponseEntity.badRequest() // 返回错误响应
                .body(ChatResponse.builder() // 构建ChatResponse
                    .answer("抱歉，回答您的问题时出现错误: " + e.getMessage()) // 设置错误信息
                    .build()); // 构建响应体
        }
    }

    @GetMapping("/conversations/{conversationId}/messages") // 处理GET请求，路径为/chat/conversations/{conversationId}/messages
    public ResponseEntity<List<Message>> getConversationMessages(@PathVariable Long conversationId) { // 获取对话消息的方法
        try { // 尝试执行
            List<Message> messages = ragService.getConversationMessages(conversationId); // 调用ragService获取对话消息
            return ResponseEntity.ok(messages); // 返回成功响应
        } catch (Exception e) { // 捕获异常
            log.error("获取对话消息失败: conversationId={}", conversationId, e); // 记录错误日志
            return ResponseEntity.badRequest().build(); // 返回错误响应
        }
    }
}
