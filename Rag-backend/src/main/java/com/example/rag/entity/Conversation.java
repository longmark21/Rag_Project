package com.example.rag.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "conversations", indexes = {
    @Index(name = "idx_session_id", columnList = "session_id"),
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_create_time", columnList = "create_time")
})
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false, length = 100, unique = true)
    private String sessionId;

    @Column(name = "user_id", length = 100)
    private String userId;

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "status", length = 20)
    private String status = "ACTIVE";

    @Column(name = "message_count")
    private Integer messageCount = 0;

    @Column(name = "create_time", nullable = false)
    @CreationTimestamp
    private LocalDateTime createTime;

    @Column(name = "update_time")
    @UpdateTimestamp
    private LocalDateTime updateTime;
}
