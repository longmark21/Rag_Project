package com.example.rag.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 根路径控制器，处理 /api 根路径的请求
 */
@RestController
public class RootController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "RAG 知识库服务运行正常");
        response.put("version", "1.0.0");
        response.put("timestamp", new Date());
        response.put("endpoints", new String[] {
            "/api/v1/documents - 获取文档列表",
            "/api/v1/documents/upload - 上传文档",
            "/api/v1/chat/stream - 流式聊天",
            "/api/swagger-ui.html - Swagger API 文档"
        });
        return ResponseEntity.ok(response);
    }
}
