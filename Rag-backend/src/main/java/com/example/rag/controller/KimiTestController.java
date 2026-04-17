package com.example.rag.controller;

import com.example.rag.service.KimiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Kimi API 测试控制器
 */
@RestController
@RequestMapping("/v1/test")
@RequiredArgsConstructor
@Tag(name = "Kimi API 测试", description = "测试 Kimi API 连通性")
public class KimiTestController {

    private final KimiService kimiService;

    @GetMapping("/kimi")
    @Operation(summary = "测试 Kimi API 连通性")
    public ResponseEntity<Map<String, Object>> testKimi() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 发送一个简单的测试消息
            String testPrompt = "你好，请简单回复一下，证明你能正常工作。";
            String result = kimiService.chat(testPrompt);
            
            response.put("success", true);
            response.put("message", "Kimi API 调用成功");
            response.put("response", result);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Kimi API 调用失败");
            response.put("message", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
