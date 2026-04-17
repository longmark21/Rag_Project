package com.example.rag.service;

import com.example.rag.entity.Conversation;
import com.example.rag.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;

    @Transactional
    public Long getOrCreateConversation(String sessionId) {
        Optional<Conversation> existingConversation = conversationRepository.findBySessionId(sessionId);
        
        if (existingConversation.isPresent()) {
            return existingConversation.get().getId();
        }

        Conversation conversation = new Conversation();
        conversation.setSessionId(sessionId);
        conversation.setStatus("ACTIVE");
        conversation.setMessageCount(0);

        Conversation savedConversation = conversationRepository.save(conversation);
        log.info("创建新对话: sessionId={}", sessionId);
        
        return savedConversation.getId();
    }

    @Transactional
    public void incrementMessageCount(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new RuntimeException("对话不存在"));
        
        conversation.setMessageCount(conversation.getMessageCount() + 1);
        conversationRepository.save(conversation);
    }

    @Transactional(readOnly = true)
    public List<Conversation> getAllConversations() {
        return conversationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Conversation getConversationById(Long id) {
        return conversationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("对话不存在"));
    }

    @Transactional
    public void deleteConversation(Long id) {
        Conversation conversation = getConversationById(id);
        conversationRepository.delete(conversation);
        log.info("删除对话: id={}", id);
    }

    @Transactional
    public void updateConversationTitle(Long id, String title) {
        Conversation conversation = getConversationById(id);
        conversation.setTitle(title);
        conversationRepository.save(conversation);
    }
}
