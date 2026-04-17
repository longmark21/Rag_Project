package com.example.rag.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    @NotBlank(message = "问题不能为空")
    private String question;

    @NotBlank(message = "会话 ID 不能为空")
    private String sessionId;

    private String userId;
    
    private List<Long> fileIds;
}
