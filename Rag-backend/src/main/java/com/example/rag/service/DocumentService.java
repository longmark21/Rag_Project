package com.example.rag.service; // 包声明，定义了类的所属包

import com.example.rag.dto.UploadResponse; // 导入UploadResponse类，用于返回上传响应
import com.example.rag.entity.Document; // 导入Document类，用于表示文档实体
import com.example.rag.repository.DocumentRepository; // 导入DocumentRepository接口，用于操作文档数据
import com.example.rag.util.FileParser; // 导入FileParser类，用于解析文件
import com.example.rag.util.TextSplitter; // 导入TextSplitter类，用于文本分块
import lombok.RequiredArgsConstructor; // 导入@RequiredArgsConstructor注解，自动生成构造函数
import lombok.extern.slf4j.Slf4j; // 导入@Slf4j注解，自动生成日志对象
import org.springframework.beans.factory.annotation.Value; // 导入@Value注解，用于注入配置值
import org.springframework.scheduling.annotation.Async; // 导入@Async注解，标记为异步方法
import org.springframework.stereotype.Service; // 导入@Service注解，标记为服务类
import org.springframework.transaction.annotation.Transactional; // 导入@Transactional注解，用于事务管理
import org.springframework.web.multipart.MultipartFile; // 导入MultipartFile类，用于接收上传的文件

import java.io.File; // 导入File类，用于操作文件
import java.io.IOException; // 导入IOException类，用于处理IO异常
import java.nio.file.Files; // 导入Files类，用于文件操作
import java.nio.file.Path; // 导入Path类，用于表示文件路径
import java.nio.file.Paths; // 导入Paths类，用于创建Path对象
import java.time.LocalDate; // 导入LocalDate类，用于表示日期
import java.time.format.DateTimeFormatter; // 导入DateTimeFormatter类，用于日期格式化
import java.util.List; // 导入List接口，用于表示列表
import java.util.UUID; // 导入UUID类，用于生成唯一标识符

@Slf4j // 自动生成日志对象
@Service // 标记为服务类，Spring会自动扫描并管理
@RequiredArgsConstructor // 自动生成构造函数，注入依赖
public class DocumentService { // 文档服务类

    private final DocumentRepository documentRepository; // 注入DocumentRepository实例，用于操作文档数据
    private final VectorStoreService vectorStoreService; // 注入VectorStoreService实例，用于向量存储
    private final FileParser fileParser; // 注入FileParser实例，用于解析文件
    private final TextSplitter textSplitter; // 注入TextSplitter实例，用于文本分块

    @Value("${file.upload.path}") // 注入文件上传路径配置
    private String uploadPath; // 文件上传路径

    private String getAbsoluteUploadPath() { // 获取绝对上传路径的方法
        // 如果路径不是绝对路径，则相对于用户目录
        Path path = Paths.get(uploadPath);
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir"), uploadPath);
        }
        return path.toString();
    }

    @Value("${file.upload.allowed-types}") // 注入允许的文件类型配置
    private List<String> allowedTypes; // 允许的文件类型列表

    @Value("${file.upload.max-size}") // 注入最大文件大小配置
    private Long maxFileSize; // 最大文件大小

    @Transactional // 标记为事务方法
    public UploadResponse uploadDocument(MultipartFile file) throws IOException { // 上传文档的方法
        String originalFileName = file.getOriginalFilename(); // 获取原始文件名
        
        if (originalFileName == null || originalFileName.isEmpty()) { // 如果文件名为空
            throw new IllegalArgumentException("文件名不能为空"); // 抛出非法参数异常
        }

        if (file.isEmpty()) { // 如果文件内容为空
            throw new IllegalArgumentException("文件内容不能为空"); // 抛出非法参数异常
        }

        if (file.getSize() > maxFileSize) { // 如果文件大小超过限制
            throw new IllegalArgumentException("文件大小超过限制，最大支持" + (maxFileSize / 1024 / 1024) + "MB"); // 抛出非法参数异常
        }

        String fileType = getFileExtension(originalFileName); // 获取文件扩展名
        if (!allowedTypes.contains(fileType.toLowerCase())) { // 如果文件类型不允许
            throw new IllegalArgumentException("不支持的文件类型: " + fileType + "，仅支持" + allowedTypes); // 抛出非法参数异常
        }

        String uploadDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); // 获取当前日期
        String fileName = UUID.randomUUID() + "." + fileType; // 生成唯一文件名
        String absoluteUploadPath = getAbsoluteUploadPath(); // 获取绝对上传路径
        Path dateDir = Paths.get(absoluteUploadPath, uploadDate); // 创建日期目录路径
        Path filePath = Paths.get(absoluteUploadPath, uploadDate, fileName); // 创建文件路径
        
        Files.createDirectories(dateDir); // 创建目录
        file.transferTo(filePath.toFile()); // 保存文件

        Document document = new Document(); // 创建文档实体
        document.setFileName(fileName); // 设置文件名
        document.setOriginalFileName(originalFileName); // 设置原始文件名
        document.setFilePath(filePath.toString()); // 设置文件路径
        document.setFileType(fileType); // 设置文件类型
        document.setFileSize(file.getSize()); // 设置文件大小
        document.setUploadDate(uploadDate); // 设置上传日期
        document.setStatus("PENDING"); // 设置状态为待处理

        Document savedDocument = documentRepository.save(document); // 保存文档

        log.info("文档上传成功: 文件名={}, 大小={}, 存储路径={}", originalFileName, file.getSize(), filePath); // 记录信息日志

        return UploadResponse.builder() // 构建上传响应
            .documentId(savedDocument.getId()) // 设置文档ID
            .fileName(originalFileName) // 设置文件名
            .originalFileName(originalFileName) // 设置原始文件名
            .status("PENDING") // 设置状态
            .message("文档上传成功，正在处理中...") // 设置消息
            .build(); // 构建响应
    }

    @Async // 标记为异步方法
    public void processDocument(Long documentId) { // 处理文档的方法
        try { // 尝试执行
            Document document = documentRepository.findById(documentId) // 根据ID获取文档
                .orElseThrow(() -> new RuntimeException("文档不存在")); // 如果不存在，抛出异常

            document.setStatus("PROCESSING"); // 设置状态为处理中
            documentRepository.save(document); // 保存文档

            File file = new File(document.getFilePath()); // 创建文件对象
            if (!file.exists()) { // 如果文件不存在
                throw new RuntimeException("文件不存在: " + document.getFilePath()); // 抛出异常
            }

            String textContent = fileParser.parseFile(file); // 解析文件内容
            
            if (textContent == null || textContent.trim().isEmpty()) { // 如果内容为空
                throw new RuntimeException("文件内容为空"); // 抛出异常
            }

            document.setTextContent(textContent); // 设置文本内容
            document.setCharacterCount(textContent.length()); // 设置字符数

            List<String> chunks = textSplitter.split(textContent); // 文本分块
            document.setTotalChunks(chunks.size()); // 设置总块数

            vectorStoreService.saveVectorChunks(document.getId(), chunks); // 保存向量块

            document.setStatus("COMPLETED"); // 设置状态为完成
            document.setChunkCount(chunks.size()); // 设置块数
            document.setProcessTime(java.time.LocalDateTime.now()); // 设置处理时间
            documentRepository.save(document); // 保存文档

            log.info("文档处理完成: 文件名={}, 字符数={}, 分块数={}", 
                document.getOriginalFileName(), textContent.length(), chunks.size()); // 记录信息日志

        } catch (Exception e) { // 捕获异常
            log.error("文档处理失败: documentId={}", documentId, e); // 记录错误日志
            Document document = documentRepository.findById(documentId).orElse(null); // 获取文档
            if (document != null) { // 如果文档存在
                document.setStatus("FAILED"); // 设置状态为失败
                document.setErrorMessage(e.getMessage()); // 设置错误信息
                documentRepository.save(document); // 保存文档
            }
        }
    }

    private String getFileExtension(String fileName) { // 获取文件扩展名的方法
        if (fileName == null || fileName.lastIndexOf(".") == -1) { // 如果文件名为空或没有扩展名
            return ""; // 返回空字符串
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1); // 返回扩展名
    }

    @Transactional(readOnly = true) // 标记为只读事务方法
    public List<Document> getAllDocuments() { // 获取所有文档的方法
        return documentRepository.findAll(); // 返回所有文档
    }

    @Transactional(readOnly = true) // 标记为只读事务方法
    public Document getDocumentById(Long id) { // 根据ID获取文档的方法
        return documentRepository.findById(id) // 根据ID查询文档
            .orElseThrow(() -> new RuntimeException("文档不存在")); // 如果不存在，抛出异常
    }

    @Transactional // 标记为事务方法
    public void deleteDocument(Long id) { // 删除文档的方法
        Document document = getDocumentById(id); // 获取文档
        
        vectorStoreService.deleteByDocumentId(id); // 删除关联的向量块
        
        try { // 尝试执行
            Files.deleteIfExists(Paths.get(document.getFilePath())); // 删除文件
        } catch (IOException e) { // 捕获异常
            log.error("删除文件失败: {}", document.getFilePath(), e); // 记录错误日志
        }
        
        documentRepository.delete(document); // 删除文档
        log.info("文档删除成功: {}", document.getOriginalFileName()); // 记录信息日志
    }

    @Transactional(readOnly = true) // 标记为只读事务方法
    public List<Document> getDocumentsByStatus(String status) { // 根据状态获取文档的方法
        return documentRepository.findByStatus(status); // 返回指定状态的文档
    }

    @Transactional(readOnly = true) // 标记为只读事务方法
    public List<Document> getDocumentsByDate(String date) { // 根据日期获取文档的方法
        return documentRepository.findByUploadDate(date); // 返回指定日期的文档
    }

    @Transactional(readOnly = true) // 标记为只读事务方法
    public java.util.Map<String, Object> getStatistics() { // 获取统计信息的方法
        java.util.Map<String, Object> stats = new java.util.HashMap<>(); // 创建统计信息映射
        
        List<Object[]> statusCounts = documentRepository.countByStatusGroup(); // 获取状态统计
        java.util.Map<String, Long> statusStats = new java.util.HashMap<>(); // 创建状态统计映射
        for (Object[] row : statusCounts) { // 遍历统计结果
            String status = (String) row[0]; // 获取状态
            Long count = (Long) row[1]; // 获取数量
            statusStats.put(status, count); // 添加到状态统计映射
        }
        stats.put("statusCounts", statusStats); // 添加状态统计到统计信息
        
        stats.put("totalDocuments", documentRepository.count()); // 添加总文档数
        stats.put("completedDocuments", documentRepository.countByStatus("COMPLETED")); // 添加已完成文档数
        stats.put("pendingDocuments", documentRepository.countByStatus("PENDING")); // 添加待处理文档数
        stats.put("processingDocuments", documentRepository.countByStatus("PROCESSING")); // 添加处理中文档数
        stats.put("failedDocuments", documentRepository.countByStatus("FAILED")); // 添加失败文档数
        
        Long totalFileSize = documentRepository.getTotalFileSizeOfCompletedDocuments(); // 获取已完成文档的总大小
        stats.put("totalFileSizeMB", totalFileSize != null ? totalFileSize / 1024 / 1024 : 0); // 添加总文件大小（MB）
        
        log.info("获取文档统计信息: {}", stats); // 记录信息日志
        return stats; // 返回统计信息
    }
}
