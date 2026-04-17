package com.example.rag.dto;

import lombok.Data;

/**
 * 检索响应DTO
 * 用于返回向量相似度检索的结果
 */
@Data
public class SearchResponse {

    /**
     * 向量块ID
     */
    private Long chunkId;

    /**
     * 文档ID（关联文档元数据，方便答案溯源）
     */
    private Long documentId;

    /**
     * 块索引
     */
    private Integer chunkIndex;

    /**
     * 块内容
     */
    private String content;

    /**
     * 相似度分数（0-1之间，越大越相似）
     */
    private Double score;

    /**
     * 文档名称（用于溯源展示）
     */
    private String documentName;

    /**
     * 无参构造方法
     */
    public SearchResponse() {
    }

    /**
     * 带参数的构造方法
     *
     * @param chunkId 向量块ID
     * @param documentId 文档ID
     * @param chunkIndex 块索引
     * @param content 块内容
     * @param score 相似度分数
     */
    public SearchResponse(Long chunkId, Long documentId, Integer chunkIndex, String content, Double score) {
        this.chunkId = chunkId;
        this.documentId = documentId;
        this.chunkIndex = chunkIndex;
        this.content = content;
        this.score = score;
    }

    /**
     * 获取带文档名称的构造方法
     *
     * @param chunkId 向量块ID
     * @param documentId 文档ID
     * @param chunkIndex 块索引
     * @param content 块内容
     * @param score 相似度分数
     * @param documentName 文档名称
     */
    public SearchResponse(Long chunkId, Long documentId, Integer chunkIndex, String content, Double score, String documentName) {
        this.chunkId = chunkId;
        this.documentId = documentId;
        this.chunkIndex = chunkIndex;
        this.content = content;
        this.score = score;
        this.documentName = documentName;
    }

    @Override
    public String toString() {
        return "SearchResponse{" +
                "chunkId=" + chunkId +
                ", documentId=" + documentId +
                ", chunkIndex=" + chunkIndex +
                ", content='" + (content != null ? content.substring(0, Math.min(content.length(), 50)) + "..." : "null") + '\'' +
                ", score=" + score +
                ", documentName='" + documentName + '\'' +
                '}';
    }
}
