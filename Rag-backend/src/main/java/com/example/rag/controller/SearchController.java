package com.example.rag.controller;

import com.example.rag.dto.SearchRequest;
import com.example.rag.dto.SearchResponse;
import com.example.rag.service.VectorStoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 向量检索控制器
 * 提供文档向量相似度检索接口
 */
@Slf4j
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final VectorStoreService vectorStoreService;

    /**
     * 向量相似度检索接口
     *
     * @param request 检索请求
     * @return 检索结果
     */
    @PostMapping
    public ResponseEntity<?> search(@RequestBody SearchRequest request) {
        try {
            if (request.getQuery() == null || request.getQuery().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(buildErrorResponse("查询文本不能为空"));
            }

            List<SearchResponse> results = vectorStoreService.search(request);
            
            log.info("向量检索完成: query={}, 结果数={}", 
                request.getQuery().substring(0, Math.min(request.getQuery().length(), 50)), 
                results.size());

            return ResponseEntity.ok(buildSuccessResponse("检索成功", results));
        } catch (Exception e) {
            log.error("向量检索失败", e);
            return ResponseEntity.internalServerError()
                .body(buildErrorResponse("检索失败: " + e.getMessage()));
        }
    }

    /**
     * 简化版向量检索接口（GET方式）
     *
     * @param query 查询文本
     * @param topK 返回结果数量（可选，默认3）
     * @param threshold 相似度阈值（可选，默认0.7）
     * @return 检索结果
     */
    @GetMapping
    public ResponseEntity<?> searchGet(
            @RequestParam String query,
            @RequestParam(required = false) Integer topK,
            @RequestParam(required = false) Double threshold) {
        try {
            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(buildErrorResponse("查询文本不能为空"));
            }

            SearchRequest request = new SearchRequest();
            request.setQuery(query);
            request.setTopK(topK);
            request.setThreshold(threshold);

            List<SearchResponse> results = vectorStoreService.search(request);
            
            log.info("向量检索完成: query={}, 结果数={}", 
                query.substring(0, Math.min(query.length(), 50)), 
                results.size());

            return ResponseEntity.ok(buildSuccessResponse("检索成功", results));
        } catch (Exception e) {
            log.error("向量检索失败", e);
            return ResponseEntity.internalServerError()
                .body(buildErrorResponse("检索失败: " + e.getMessage()));
        }
    }

    /**
     * 构建成功响应
     *
     * @param message 消息
     * @param data 数据
     * @return 响应Map
     */
    private Map<String, Object> buildSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        if (data != null) {
            response.put("data", data);
        }
        return response;
    }

    /**
     * 构建错误响应
     *
     * @param message 错误消息
     * @return 响应Map
     */
    private Map<String, Object> buildErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
}
