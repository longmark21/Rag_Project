package com.example.rag.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "messages", indexes = {
    @Index(name = "idx_conversation_id", columnList = "conversation_id"),
    @Index(name = "idx_role", columnList = "role"),
    @Index(name = "idx_create_time", columnList = "create_time")
})
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conversation_id", nullable = false)
    private Long conversationId;

    @Column(name = "role", nullable = false, length = 20)
    private String role;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "context", columnDefinition = "TEXT")
    private String context;

    @Column(name = "sources", columnDefinition = "JSON")
    private String sources;

    @Column(name = "token_count")
    private Integer tokenCount;

    @Column(name = "create_time", nullable = false)
    @CreationTimestamp
    private LocalDateTime createTime;
}
