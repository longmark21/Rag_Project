package com.example.rag.repository;

import com.example.rag.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findBySessionId(String sessionId);

    List<Conversation> findByUserId(String userId);

    List<Conversation> findByStatus(String status);

    @Query("SELECT c FROM Conversation c WHERE c.userId = :userId AND c.status = :status ORDER BY c.createTime DESC")
    List<Conversation> findByUserIdAndStatusOrderByCreateTimeDesc(@Param("userId") String userId, 
                                                                   @Param("status") String status);

    @Query("SELECT c FROM Conversation c WHERE c.createTime BETWEEN :startTime AND :endTime")
    List<Conversation> findByCreateTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                               @Param("endTime") LocalDateTime endTime);
}
