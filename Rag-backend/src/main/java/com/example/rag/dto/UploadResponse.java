package com.example.rag.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 统一上传响应DTO
 * 用于返回文档上传的结果信息
 */
@Data
@Builder
public class UploadResponse {

    /**
     * 文档ID
     */
    private Long documentId;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 原始文件名
     */
    private String originalFileName;

    /**
     * 上传状态
     */
    private String status;

    /**
     * 响应消息
     */
    private String message;
}
