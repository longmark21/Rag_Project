package com.example.rag.service;

import com.example.rag.dto.ChatRequest;
import com.example.rag.dto.ChatResponse;
import com.example.rag.entity.Message;
import com.example.rag.entity.VectorChunk;
import com.example.rag.mock.ChatClient;
import com.example.rag.repository.ConversationRepository;
import com.example.rag.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j // 自动生成日志对象
@Service // 标记为服务类，Spring会自动扫描并管理
@RequiredArgsConstructor // 自动生成构造函数，注入依赖
public class RagService { // RAG服务类，处理知识库问答逻辑

    private final ChatClient chatClient; // 注入ChatClient实例，用于与大模型交互
    private final VectorStoreService vectorStoreService; // 注入VectorStoreService实例，用于向量存储和检索
    private final ConversationService conversationService; // 注入ConversationService实例，用于处理对话
    private final MessageRepository messageRepository; // 注入MessageRepository实例，用于操作消息数据
    private final DocumentService documentService; // 注入DocumentService实例，用于处理文档
    private final RedisTemplate<String, Object> redisTemplate; // 注入RedisTemplate实例，用于操作Redis

    @Value("${rag.retrieval.top-k:5}") // 注入检索时返回的最大结果数，默认为5
    private int topK; // 检索时返回的最大结果数

    @Value("${rag.retrieval.score-threshold:0.7}") // 注入相似度阈值，默认为0.7
    private double scoreThreshold; // 相似度阈值

    @Value("${rag.conversation.max-history:10}") // 注入最大历史消息数，默认为10
    private int maxHistory; // 最大历史消息数

    // 删除了旧的返回 ChatResponse 的 chat 方法，使用新的返回 String 的 chat 方法（第 229 行）

    private String buildContext(List<com.example.rag.dto.SearchResponse> chunks) { // 构建上下文的方法
        if (chunks.isEmpty()) { // 如果没有相关文档块
            return "没有找到相关的文档内容。"; // 返回提示信息
        }

        StringBuilder context = new StringBuilder(); // 创建字符串构建器
        for (int i = 0; i < chunks.size(); i++) { // 遍历文档块
            com.example.rag.dto.SearchResponse chunk = chunks.get(i); // 获取当前文档块
            context.append(chunk.getContent()).append("\n\n"); // 添加文档块内容
        }

        return context.toString(); // 返回上下文字符串
    }

    private List<Message> getConversationHistory(Long conversationId) { // 获取对话历史的方法
        String cacheKey = "conversation:history:" + conversationId; // 构建缓存键
        
        @SuppressWarnings("unchecked") // 抑制类型转换警告
        List<Message> cachedHistory = (List<Message>) redisTemplate.opsForValue().get(cacheKey); // 从Redis获取缓存的历史消息
        
        if (cachedHistory != null) { // 如果缓存存在
            return cachedHistory; // 返回缓存的历史消息
        }

        List<Message> history = messageRepository.findRecentMessagesByConversationId(conversationId, maxHistory); // 从数据库获取历史消息
        
        redisTemplate.opsForValue().set(cacheKey, history, 1, TimeUnit.HOURS); // 将历史消息缓存到Redis，有效期1小时
        
        return history; // 返回历史消息
    }

    private String buildPrompt(String question, String context) { // 构建提示词的方法
        StringBuilder prompt = new StringBuilder(); // 创建字符串构建器
        
        prompt.append("你是一个企业内部知识库问答助手，请严格根据下面提供的上下文回答用户问题，要求：\n"); // 添加系统提示
        prompt.append("1.  只基于上下文回答，不要编造任何信息\n"); // 添加要求1
        prompt.append("2.  如果上下文中没有答案，直接回答「未在知识库中找到相关信息」\n"); // 添加要求2
        prompt.append("3.  回答末尾必须标注信息来源，格式为：\n— 依据：{文档名称}\n"); // 添加要求3
        prompt.append("4.  回答控制在300字以内，简洁明了\n"); // 添加要求4
        prompt.append("\n"); // 添加空行
        prompt.append("上下文：\n"); // 添加上下文标题
        prompt.append(context); // 添加上下文内容
        prompt.append("用户问题：" ).append(question).append("\n"); // 添加用户问题

        return prompt.toString(); // 返回提示词字符串
    }

    private ChatResponse generateResponse(String prompt, Long conversationId, String question, 
                                         String context, List<com.example.rag.dto.SearchResponse> sources) { // 生成响应的方法
        Message userMessage = new Message(); // 创建用户消息
        userMessage.setConversationId(conversationId); // 设置对话ID
        userMessage.setRole("USER"); // 设置角色为用户
        userMessage.setContent(question); // 设置消息内容
        userMessage.setContext(context); // 设置上下文
        userMessage.setSources(buildSourcesJson(sources)); // 设置来源信息
        userMessage.setTokenCount(question.length()); // 设置令牌数
        messageRepository.save(userMessage); // 保存用户消息

        String answer = chatClient.prompt(prompt).call().content(); // 调用大模型生成回答

        Message assistantMessage = new Message(); // 创建助手消息
        assistantMessage.setConversationId(conversationId); // 设置对话ID
        assistantMessage.setRole("ASSISTANT"); // 设置角色为助手
        assistantMessage.setContent(answer); // 设置消息内容
        assistantMessage.setContext(context); // 设置上下文
        assistantMessage.setSources(buildSourcesJson(sources)); // 设置来源信息
        assistantMessage.setTokenCount(answer.length()); // 设置令牌数
        messageRepository.save(assistantMessage); // 保存助手消息

        String cacheKey = "conversation:history:" + conversationId; // 构建缓存键
        redisTemplate.delete(cacheKey); // 删除缓存的历史消息

        conversationService.incrementMessageCount(conversationId); // 增加对话消息数

        return ChatResponse.builder() // 构建ChatResponse
            .answer(answer) // 设置回答
            .sources(sources.stream() // 处理来源信息
                .map(chunk -> buildSourceInfo(chunk)) // 转换每个来源
                .collect(Collectors.toList())) // 收集为列表
            .context(context) // 设置上下文
            .conversationId(conversationId) // 设置对话ID
            .build(); // 构建响应
    }

    private String buildSourcesJson(List<com.example.rag.dto.SearchResponse> chunks) { // 构建来源信息JSON的方法
        if (chunks.isEmpty()) { // 如果没有来源
            return "[]"; // 返回空数组
        }

        StringBuilder json = new StringBuilder("["); // 创建字符串构建器
        for (int i = 0; i < chunks.size(); i++) { // 遍历来源
            com.example.rag.dto.SearchResponse chunk = chunks.get(i); // 获取当前来源
            json.append("{"); // 添加开始符号
            json.append("\"documentId\":").append(chunk.getDocumentId()).append(","); // 添加文档ID
            json.append("\"chunkIndex\":").append(chunk.getChunkIndex()).append(","); // 添加块索引
            json.append("\"content\":\"").append(escapeJson(chunk.getContent())).append("\""); // 添加内容
            json.append("}"); // 添加结束符号
            if (i < chunks.size() - 1) { // 如果不是最后一个
                json.append(","); // 添加逗号
            }
        }
        json.append("]"); // 添加结束数组符号
        return json.toString(); // 返回JSON字符串
    }

    private Map<String, Object> buildSourceInfo(com.example.rag.dto.SearchResponse chunk) { // 构建来源信息的方法
        Map<String, Object> source = new HashMap<>(); // 创建来源信息映射
        source.put("documentId", chunk.getDocumentId()); // 添加文档ID
        source.put("chunkIndex", chunk.getChunkIndex()); // 添加块索引
        source.put("content", chunk.getContent()); // 添加内容
        source.put("score", chunk.getScore()); // 添加相似度分数
        source.put("documentName", chunk.getDocumentName()); // 添加文档名称
        return source; // 返回来源信息
    }

    private String escapeJson(String text) { // 转义JSON字符串的方法
        return text.replace("\\", "\\\\") // 转义反斜杠
                  .replace("\"", "\\\"") // 转义双引号
                  .replace("\n", "\\n") // 转义换行符
                  .replace("\r", "\\r") // 转义回车符
                  .replace("\t", "\\t"); // 转义制表符
    }

    @Transactional(readOnly = true) // 标记为只读事务方法
    public List<Message> getConversationMessages(Long conversationId) { // 获取对话消息的方法
        return messageRepository.findByConversationIdOrderByCreateTimeAsc(conversationId); // 从数据库获取对话消息，按创建时间排序
    }

    // 移除 @Transactional，因为流式聊天不适合放在事务中
    public void streamChat(ChatRequest request, java.util.function.Consumer<String> chunkConsumer, java.util.function.Consumer<List<Map<String, Object>>> sourcesConsumer) {
        String sessionId = request.getSessionId(); // 获取会话 ID
        String question = request.getQuestion(); // 获取用户问题
        
        // 添加空值检查
        if (question == null || question.trim().isEmpty()) {
            log.error("问题为空：sessionId={}", sessionId);
            try {
                chunkConsumer.accept("抱歉，您的问题不能为空");
            } catch (Exception e) {
                log.error("发送错误消息失败", e);
            }
            return;
        }
        
        // 直接调用内部方法生成回答
        String answer = generateAnswer(request);
        
        // 模拟打字机效果，逐字输出（减少延迟到 20ms）
        for (int i = 0; i < answer.length(); i++) {
            String chunk = answer.substring(i, i + 1);
            chunkConsumer.accept(chunk); // 调用回调函数处理每个字符
            try {
                Thread.sleep(20); // 减少延迟，加快输出速度
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        // 调用回调函数处理来源信息
        List<Map<String, Object>> sources = getSourcesFromLastMessage(request.getSessionId());
        sourcesConsumer.accept(sources);
    }

    /**
     * 非流式聊天方法
     */
    public String chat(ChatRequest request) {
        return generateAnswer(request);
    }

    /**
     * 生成回答的内部方法
     */
    private String generateAnswer(ChatRequest request) {
        String sessionId = request.getSessionId();
        String question = request.getQuestion();
        
        if (question == null || question.trim().isEmpty()) {
            return "抱歉，您的问题不能为空";
        }
        
        List<Long> fileIds = request.getFileIds();
        Long conversationId = conversationService.getOrCreateConversation(sessionId);

        // 搜索相关的文档块（如果向量表不存在或检索失败，使用空上下文）
        List<com.example.rag.dto.SearchResponse> relevantChunks;
        try {
            relevantChunks = vectorStoreService.searchSimilarChunks(question, topK, scoreThreshold, fileIds);
        } catch (Exception e) {
            log.warn("向量检索失败，使用纯聊天模式：{}", e.getMessage());
            relevantChunks = new java.util.ArrayList<>();
        }

        String context = buildContext(relevantChunks);
        String prompt = buildPrompt(question, context);

        // 保存用户消息
        Message userMessage = new Message();
        userMessage.setConversationId(conversationId);
        userMessage.setRole("USER");
        userMessage.setContent(question);
        userMessage.setContext(context);
        userMessage.setSources(buildSourcesJson(relevantChunks));
        userMessage.setTokenCount(question.length());
        messageRepository.save(userMessage);

        // 直接生成回答（不流式）
        String answer = chatClient.prompt(prompt).call().content();

        // 保存助手消息
        Message assistantMessage = new Message();
        assistantMessage.setConversationId(conversationId);
        assistantMessage.setRole("ASSISTANT");
        assistantMessage.setContent(answer);
        assistantMessage.setContext(context);
        assistantMessage.setSources(buildSourcesJson(relevantChunks));
        assistantMessage.setTokenCount(answer.length());
        messageRepository.save(assistantMessage);

        String cacheKey = "conversation:history:" + conversationId;
        redisTemplate.delete(cacheKey);

        conversationService.incrementMessageCount(conversationId);

        return answer;
    }

    /**
     * 从最后一条助手消息获取来源信息
     */
    private List<Map<String, Object>> getSourcesFromLastMessage(String sessionId) {
        Long conversationId = conversationService.getOrCreateConversation(sessionId);
        List<Message> messages = messageRepository.findByConversationIdOrderByCreateTimeAsc(conversationId);
        if (messages.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        Message lastMessage = messages.get(messages.size() - 1);
        String sourcesJson = lastMessage.getSources();
        // 这里可以解析 JSON 字符串返回 Map 列表，简化处理返回空列表
        return new java.util.ArrayList<>();
    }
}
