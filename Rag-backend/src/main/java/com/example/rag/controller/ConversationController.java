package com.example.rag.controller;

import com.example.rag.entity.Conversation;
import com.example.rag.service.ConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @GetMapping
    public ResponseEntity<List<Conversation>> getAllConversations() {
        List<Conversation> conversations = conversationService.getAllConversations();
        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Conversation> getConversationById(@PathVariable Long id) {
        try {
            Conversation conversation = conversationService.getConversationById(id);
            return ResponseEntity.ok(conversation);
        } catch (Exception e) {
            log.error("获取对话失败: id={}", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/title")
    public ResponseEntity<String> updateConversationTitle(@PathVariable Long id, @RequestBody String title) {
        try {
            conversationService.updateConversationTitle(id, title);
            return ResponseEntity.ok("对话标题更新成功");
        } catch (Exception e) {
            log.error("更新对话标题失败: id={}", id, e);
            return ResponseEntity.badRequest()
                .body("更新对话标题失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteConversation(@PathVariable Long id) {
        try {
            conversationService.deleteConversation(id);
            return ResponseEntity.ok("对话删除成功");
        } catch (Exception e) {
            log.error("删除对话失败: id={}", id, e);
            return ResponseEntity.badRequest()
                .body("删除对话失败: " + e.getMessage());
        }
    }
}
