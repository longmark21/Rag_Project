package com.example.rag.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocumentUploadResponse {

    private Long documentId;
    private String fileName;
    private String status;
    private String message;
}
