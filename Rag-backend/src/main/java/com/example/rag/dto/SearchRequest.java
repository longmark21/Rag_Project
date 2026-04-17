package com.example.rag.dto;

import lombok.Data;

/**
 * 检索请求DTO
 * 用于向量相似度检索的请求参数
 */
@Data
public class SearchRequest {

    /**
     * 查询文本
     */
    private String query;

    /**
     * 返回结果数量（默认3）
     */
    private Integer topK;

    /**
     * 相似度阈值（默认0.7）
     */
    private Double threshold;

    /**
     * 文档ID过滤（可选）
     */
    private Long documentId;

    /**
     * 无参构造方法
     */
    public SearchRequest() {
    }

    /**
     * 带查询文本的构造方法
     *
     * @param query 查询文本
     */
    public SearchRequest(String query) {
        this.query = query;
    }

    /**
     * 全参构造方法
     *
     * @param query 查询文本
     * @param topK 返回结果数量
     * @param threshold 相似度阈值
     */
    public SearchRequest(String query, Integer topK, Double threshold) {
        this.query = query;
        this.topK = topK;
        this.threshold = threshold;
    }
}
