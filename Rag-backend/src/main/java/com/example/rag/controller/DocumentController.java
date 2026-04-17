package com.example.rag.controller; // 包声明，定义了类的所属包

import com.example.rag.dto.UploadResponse; // 导入UploadResponse类，用于返回上传响应
import com.example.rag.entity.Document; // 导入Document类，用于表示文档实体
import com.example.rag.service.DocumentService; // 导入DocumentService类，用于处理文档业务逻辑
import jakarta.validation.constraints.NotNull; // 导入@NotNull注解，用于参数校验
import lombok.RequiredArgsConstructor; // 导入@RequiredArgsConstructor注解，自动生成构造函数
import lombok.extern.slf4j.Slf4j; // 导入@Slf4j注解，自动生成日志对象
import org.springframework.http.HttpStatus; // 导入HttpStatus类，用于设置HTTP状态码
import org.springframework.http.ResponseEntity; // 导入ResponseEntity类，用于构建HTTP响应
import org.springframework.validation.annotation.Validated; // 导入@Validated注解，用于启用参数校验
import org.springframework.web.bind.annotation.*; // 导入Spring Web注解
import org.springframework.web.multipart.MultipartFile; // 导入MultipartFile类，用于接收上传的文件

import java.util.HashMap; // 导入HashMap类，用于创建映射
import java.util.List; // 导入List接口，用于表示列表
import java.util.Map; // 导入Map接口，用于表示映射

@Slf4j // 自动生成日志对象
@RestController // 标记为REST控制器
@RequestMapping("/documents") // 定义请求路径前缀
@RequiredArgsConstructor // 自动生成构造函数，注入依赖
@Validated // 启用参数校验
public class DocumentController { // 文档控制器类

    private final DocumentService documentService; // 注入DocumentService实例

    @PostMapping("/upload") // 处理POST请求，路径为/documents/upload
    public ResponseEntity<?> uploadDocument(@RequestParam("file") @NotNull(message = "文件不能为空") MultipartFile file) { // 上传文档的方法
        try { // 尝试执行
            if (file.isEmpty()) { // 如果文件为空
                return ResponseEntity.badRequest() // 返回错误响应
                    .body(buildErrorResponse("文件内容不能为空")); // 构建错误响应
            }

            UploadResponse response = documentService.uploadDocument(file); // 调用documentService上传文档
            
            // 异步处理文档向量化
            if (response.getDocumentId() != null) {
                documentService.processDocument(response.getDocumentId());
                log.info("文档向量化任务已提交: documentId={}", response.getDocumentId());
            }
            
            return ResponseEntity.ok(response); // 返回成功响应
        } catch (IllegalArgumentException e) { // 捕获参数异常
            log.warn("文档上传参数校验失败: {}", e.getMessage()); // 记录警告日志
            return ResponseEntity.badRequest() // 返回错误响应
                .body(buildErrorResponse(e.getMessage())); // 构建错误响应
        } catch (Exception e) { // 捕获其他异常
            log.error("文档上传失败", e); // 记录错误日志
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) // 返回服务器错误响应
                .body(buildErrorResponse("文档上传失败: " + e.getMessage())); // 构建错误响应
        }
    }

    @PostMapping("/{documentId}/process") // 处理POST请求，路径为/documents/{documentId}/process
    public ResponseEntity<?> processDocument(@PathVariable Long documentId) { // 处理文档的方法
        try { // 尝试执行
            if (documentId == null || documentId <= 0) { // 如果文档ID无效
                return ResponseEntity.badRequest() // 返回错误响应
                    .body(buildErrorResponse("文档ID不能为空且必须大于0")); // 构建错误响应
            }

            documentService.processDocument(documentId); // 调用documentService处理文档
            return ResponseEntity.ok(buildSuccessResponse("文档处理任务已提交", null)); // 返回成功响应
        } catch (IllegalArgumentException e) { // 捕获参数异常
            log.warn("文档处理参数校验失败: documentId={}, {}", documentId, e.getMessage()); // 记录警告日志
            return ResponseEntity.badRequest() // 返回错误响应
                .body(buildErrorResponse(e.getMessage())); // 构建错误响应
        } catch (Exception e) { // 捕获其他异常
            log.error("文档处理失败: documentId={}", documentId, e); // 记录错误日志
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) // 返回服务器错误响应
                .body(buildErrorResponse("文档处理失败: " + e.getMessage())); // 构建错误响应
        }
    }

    @GetMapping // 处理GET请求，路径为/documents
    public ResponseEntity<?> getAllDocuments(
            @RequestParam(required = false) String status, // 状态参数，可选
            @RequestParam(required = false) String date) { // 日期参数，可选
        try { // 尝试执行
            List<Document> documents; // 文档列表
            
            if (status != null && !status.isEmpty()) { // 如果提供了状态参数
                documents = documentService.getDocumentsByStatus(status); // 按状态查询文档
                log.info("按状态查询文档: status={}, 结果数={}", status, documents.size()); // 记录信息日志
            } else if (date != null && !date.isEmpty()) { // 如果提供了日期参数
                documents = documentService.getDocumentsByDate(date); // 按日期查询文档
                log.info("按日期查询文档: date={}, 结果数={}", date, documents.size()); // 记录信息日志
            } else { // 如果没有提供参数
                documents = documentService.getAllDocuments(); // 查询所有文档
                log.info("查询所有文档: 结果数={}", documents.size()); // 记录信息日志
            }
            
            return ResponseEntity.ok(buildSuccessResponse("查询成功", documents)); // 返回成功响应
        } catch (Exception e) { // 捕获异常
            log.error("查询文档列表失败", e); // 记录错误日志
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) // 返回服务器错误响应
                .body(buildErrorResponse("查询文档列表失败: " + e.getMessage())); // 构建错误响应
        }
    }

    @GetMapping("/{documentId}") // 处理GET请求，路径为/documents/{documentId}
    public ResponseEntity<?> getDocumentById(@PathVariable Long documentId) { // 获取文档详情的方法
        try { // 尝试执行
            if (documentId == null || documentId <= 0) { // 如果文档ID无效
                return ResponseEntity.badRequest() // 返回错误响应
                    .body(buildErrorResponse("文档ID不能为空且必须大于0")); // 构建错误响应
            }

            Document document = documentService.getDocumentById(documentId); // 调用documentService获取文档
            log.info("查询文档详情: documentId={}", documentId); // 记录信息日志
            return ResponseEntity.ok(buildSuccessResponse("查询成功", document)); // 返回成功响应
        } catch (IllegalArgumentException e) { // 捕获参数异常
            log.warn("查询文档参数校验失败: documentId={}, {}", documentId, e.getMessage()); // 记录警告日志
            return ResponseEntity.badRequest() // 返回错误响应
                .body(buildErrorResponse(e.getMessage())); // 构建错误响应
        } catch (Exception e) { // 捕获其他异常
            log.error("获取文档失败: documentId={}", documentId, e); // 记录错误日志
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) // 返回服务器错误响应
                .body(buildErrorResponse("获取文档失败: " + e.getMessage())); // 构建错误响应
        }
    }

    @DeleteMapping("/{documentId}") // 处理DELETE请求，路径为/documents/{documentId}
    public ResponseEntity<?> deleteDocument(@PathVariable Long documentId) { // 删除文档的方法
        try { // 尝试执行
            if (documentId == null || documentId <= 0) { // 如果文档ID无效
                return ResponseEntity.badRequest() // 返回错误响应
                    .body(buildErrorResponse("文档ID不能为空且必须大于0")); // 构建错误响应
            }

            documentService.deleteDocument(documentId); // 调用documentService删除文档
            log.info("删除文档成功: documentId={}", documentId); // 记录信息日志
            return ResponseEntity.ok(buildSuccessResponse("文档删除成功", null)); // 返回成功响应
        } catch (IllegalArgumentException e) { // 捕获参数异常
            log.warn("删除文档参数校验失败: documentId={}, {}", documentId, e.getMessage()); // 记录警告日志
            return ResponseEntity.badRequest() // 返回错误响应
                .body(buildErrorResponse(e.getMessage())); // 构建错误响应
        } catch (Exception e) { // 捕获其他异常
            log.error("删除文档失败: documentId={}", documentId, e); // 记录错误日志
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) // 返回服务器错误响应
                .body(buildErrorResponse("删除文档失败: " + e.getMessage())); // 构建错误响应
        }
    }

    @GetMapping("/stats") // 处理GET请求，路径为/documents/stats
    public ResponseEntity<?> getStatistics() { // 获取统计信息的方法
        try { // 尝试执行
            Map<String, Object> stats = documentService.getStatistics(); // 调用documentService获取统计信息
            return ResponseEntity.ok(buildSuccessResponse("统计信息获取成功", stats)); // 返回成功响应
        } catch (Exception e) { // 捕获异常
            log.error("获取统计信息失败", e); // 记录错误日志
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) // 返回服务器错误响应
                .body(buildErrorResponse("获取统计信息失败: " + e.getMessage())); // 构建错误响应
        }
    }

    private Map<String, Object> buildSuccessResponse(String message, Object data) { // 构建成功响应的方法
        Map<String, Object> response = new HashMap<>(); // 创建响应映射
        response.put("success", true); // 设置成功标志
        response.put("message", message); // 设置消息
        if (data != null) { // 如果有数据
            response.put("data", data); // 设置数据
        }
        return response; // 返回响应映射
    }

    private Map<String, Object> buildErrorResponse(String message) { // 构建错误响应的方法
        Map<String, Object> response = new HashMap<>(); // 创建响应映射
        response.put("success", false); // 设置失败标志
        response.put("message", message); // 设置错误消息
        return response; // 返回响应映射
    }
}
