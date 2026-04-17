package com.example.rag.controller;

import com.example.rag.dto.ChatRequest;
import com.example.rag.dto.ChatResponse;
import com.example.rag.dto.UploadResponse;
import com.example.rag.entity.Document;
import com.example.rag.service.DocumentService;
import com.example.rag.service.RagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@Tag(name = "RAG API", description = "RAG 知识库接口")
public class RagController {

    private final RagService ragService;
    private final DocumentService documentService;

    @Operation(
        summary = "聊天",
        description = "直接返回 AI 回答，不使用流式",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "聊天成功"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "请求参数错误"
            )
        }
    )
    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(
            @Parameter(description = "聊天请求", required = true)
            @RequestBody ChatRequest request) {
        try {
            // 调用服务层进行聊天
            String answer = ragService.chat(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", answer);
            response.put("sources", new java.util.ArrayList<>());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("聊天失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "聊天失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @Operation(
        summary = "流式聊天",
        description = "使用 SSE 流式返回 AI 回答，支持打字机效果",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "流式响应成功",
                content = @Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE)
            ),
            @ApiResponse(
                responseCode = "400",
                description = "请求参数错误"
            )
        }
    )
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public StreamingResponseBody streamChat(
            @Parameter(description = "聊天请求", required = true)
            @RequestBody ChatRequest request) {
        return outputStream -> {
            try {
                // 调用服务层进行流式处理
                ragService.streamChat(request, (chunk) -> {
                    try {
                        // 发送 SSE 事件
                        outputStream.write("data: ".getBytes(StandardCharsets.UTF_8));
                        outputStream.write(chunk.getBytes(StandardCharsets.UTF_8));
                        outputStream.write("\n\n".getBytes(StandardCharsets.UTF_8));
                        outputStream.flush();
                    } catch (IOException e) {
                        log.error("流式输出失败", e);
                    }
                }, (sources) -> {
                    try {
                        // 发送完成事件，包含来源信息
                        String sourcesJson = "{\"sources\":[" + 
                            sources.stream()
                                .map(source -> "{\"name\":\"" + source.get("name") + "\",\"page\":" + source.get("page") + "}")
                                .reduce((a, b) -> a + "," + b)
                                .orElse("") + 
                            "]}";
                        outputStream.write("event: sources\n".getBytes(StandardCharsets.UTF_8));
                        outputStream.write("data: ".getBytes(StandardCharsets.UTF_8));
                        outputStream.write(sourcesJson.getBytes(StandardCharsets.UTF_8));
                        outputStream.write("\n\n".getBytes(StandardCharsets.UTF_8));
                        outputStream.flush();
                    } catch (IOException e) {
                        log.error("发送来源信息失败", e);
                    }
                });

                // 发送结束事件
                outputStream.write("event: end\n".getBytes(StandardCharsets.UTF_8));
                outputStream.write("data: done\n\n".getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            } catch (Exception e) {
                log.error("流式聊天失败", e);
                try {
                    outputStream.write("data: 抱歉，回答您的问题时出现错误\n\n".getBytes(StandardCharsets.UTF_8));
                    outputStream.flush();
                } catch (IOException ex) {
                    log.error("发送错误信息失败", ex);
                }
            }
        };
    }

    @Operation(
        summary = "上传文档",
        description = "上传 PDF/Word/TXT 文件并进行向量化处理",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "上传成功",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UploadResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "文件为空"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "上传失败"
            )
        }
    )
    @PostMapping(value = "/documents/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadDocument(
            @Parameter(description = "上传的文件", required = true)
            @RequestParam("file") @NotNull(message = "文件不能为空") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "文件内容不能为空"));
            }

            UploadResponse response = documentService.uploadDocument(file);
            
            // 异步处理文档向量化
            if (response.getDocumentId() != null) {
                documentService.processDocument(response.getDocumentId());
                log.info("文档向量化任务已提交: documentId={}", response.getDocumentId());
            }
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("文档上传参数校验失败: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("文档上传失败", e);
            return ResponseEntity.status(500)
                .body(Map.of("success", false, "message", "文档上传失败: " + e.getMessage()));
        }
    }

    @Operation(
        summary = "获取文档列表",
        description = "获取所有已上传的文档列表",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "查询成功",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Document.class)
                )
            ),
            @ApiResponse(
                responseCode = "500",
                description = "查询失败"
            )
        }
    )
    @GetMapping("/documents")
    public ResponseEntity<?> getAllDocuments() {
        try {
            List<Document> documents = documentService.getAllDocuments();
            log.info("查询所有文档: 结果数={}", documents.size());
            return ResponseEntity.ok(Map.of("success", true, "data", documents));
        } catch (Exception e) {
            log.error("查询文档列表失败", e);
            return ResponseEntity.status(500)
                .body(Map.of("success", false, "message", "查询文档列表失败: " + e.getMessage()));
        }
    }

    @Operation(
        summary = "删除文档",
        description = "根据文档ID删除文档",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "删除成功"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "参数错误"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "删除失败"
            )
        }
    )
    @DeleteMapping("/documents/{documentId}")
    public ResponseEntity<?> deleteDocument(@PathVariable Long documentId) {
        try {
            if (documentId == null || documentId <= 0) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "文档ID不能为空且必须大于0"));
            }

            documentService.deleteDocument(documentId);
            log.info("删除文档成功: documentId={}", documentId);
            return ResponseEntity.ok(Map.of("success", true, "message", "文档删除成功"));
        } catch (IllegalArgumentException e) {
            log.warn("删除文档参数校验失败: documentId={}, {}", documentId, e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("删除文档失败: documentId={}", documentId, e);
            return ResponseEntity.status(500)
                .body(Map.of("success", false, "message", "删除文档失败: " + e.getMessage()));
        }
    }
}