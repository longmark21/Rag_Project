package com.example.rag.entity;

import com.example.rag.util.VectorConverter;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "vector_chunks", indexes = {
    @Index(name = "idx_document_id", columnList = "document_id"),
    @Index(name = "idx_chunk_index", columnList = "chunk_index")
})
public class VectorChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id", nullable = false)
    private Long documentId;

    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "embedding", columnDefinition = "TEXT")
    @Convert(converter = VectorConverter.class)
    private float[] embedding;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @Column(name = "token_count")
    private Integer tokenCount;

    @Column(name = "create_time", nullable = false)
    @CreationTimestamp
    private LocalDateTime createTime;
}
