package com.example.rag.repository;

import com.example.rag.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByConversationIdOrderByCreateTimeAsc(Long conversationId);

    @Query("SELECT m FROM Message m WHERE m.conversationId = :conversationId ORDER BY m.createTime DESC LIMIT :limit")
    List<Message> findRecentMessagesByConversationId(@Param("conversationId") Long conversationId, 
                                                     @Param("limit") int limit);

    void deleteByConversationId(Long conversationId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversationId = :conversationId")
    Integer countByConversationId(@Param("conversationId") Long conversationId);
}
