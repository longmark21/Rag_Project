package com.example.rag.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "documents", indexes = {
    @Index(name = "idx_file_name", columnList = "file_name"),
    @Index(name = "idx_upload_time", columnList = "upload_time"),
    @Index(name = "idx_status", columnList = "status")
})
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "file_type", nullable = false, length = 20)
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "chunk_count")
    private Integer chunkCount = 0;

    @Column(name = "total_chunks")
    private Integer totalChunks = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "upload_time", nullable = false)
    @CreationTimestamp
    private LocalDateTime uploadTime;

    @Column(name = "process_time")
    private LocalDateTime processTime;

    @Column(name = "update_time")
    @UpdateTimestamp
    private LocalDateTime updateTime;

    @Column(name = "upload_date", length = 10)
    private String uploadDate;

    @Column(name = "text_content", columnDefinition = "TEXT")
    private String textContent;

    @Column(name = "character_count")
    private Integer characterCount;
}
