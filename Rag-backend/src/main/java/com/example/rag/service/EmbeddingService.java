package com.example.rag.service;

import com.example.rag.mock.EmbeddingModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 文本向量化服务
 * 调用通义千问 Embedding API 实现文本向量化
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;
    
    // 本地缓存，避免重复向量化相同的文本
    private final ConcurrentHashMap<String, float[]> embeddingCache = new ConcurrentHashMap<>();

    /**
     * 单条文本向量化
     *
     * @param text 待向量化的文本
     * @return 向量数组
     */
    public float[] embed(String text) {
        if (text == null || text.trim().isEmpty()) {
            log.warn("文本为空，无法向量化");
            return new float[0];
        }

        try {
            // 尝试从缓存获取
            String trimmedText = text.trim();
            if (embeddingCache.containsKey(trimmedText)) {
                log.debug("从缓存获取向量：文本长度={}", trimmedText.length());
                return embeddingCache.get(trimmedText);
            }

            // 调用 EmbeddingModel 进行向量化
            float[] embedding = embeddingModel.embed(trimmedText);
            
            // 缓存结果
            if (embedding.length > 0) {
                // 只缓存较短的文本，避免缓存过大
                if (trimmedText.length() < 1000) {
                    embeddingCache.put(trimmedText, embedding);
                }
            }
            
            log.debug("文本向量化完成：文本长度={}, 向量维度={}", trimmedText.length(), embedding.length);
            return embedding;
        } catch (Exception e) {
            log.error("文本向量化失败：{}", e.getMessage(), e);
            throw new RuntimeException("文本向量化失败：" + e.getMessage(), e);
        }
    }

    /**
     * 批量文本向量化
     *
     * @param texts 待向量化的文本列表
     * @return 向量数组列表
     */
    public List<float[]> embedBatch(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            log.warn("文本列表为空，无法向量化");
            return new ArrayList<>();
        }

        try {
            List<float[]> embeddings = new ArrayList<>(texts.size());
            
            for (String text : texts) {
                // 重用单条文本向量化方法，利用缓存
                float[] embedding = embed(text);
                embeddings.add(embedding);
            }

            log.info("批量文本向量化完成：文本数量={}, 向量维度={}", texts.size(), 
                embeddings.isEmpty() ? 0 : embeddings.get(0).length);
            return embeddings;
        } catch (Exception e) {
            log.error("批量文本向量化失败：{}", e.getMessage(), e);
            throw new RuntimeException("批量文本向量化失败：" + e.getMessage(), e);
        }
    }

    /**
     * 获取向量维度
     *
     * @return 向量维度
     */
    public int getDimensions() {
        try {
            // 通过向量化一个测试文本来获取维度
            float[] testEmbedding = embed("test");
            return testEmbedding.length;
        } catch (Exception e) {
            log.warn("获取向量维度失败，使用默认值 1536: {}", e.getMessage());
            return 1536; // 通义千问 text-embedding-v2 的默认维度
        }
    }

    /**
     * 清除缓存
     */
    public void clearCache() {
        int cacheSize = embeddingCache.size();
        embeddingCache.clear();
        log.info("清除向量缓存：清除了 {} 条缓存", cacheSize);
    }

    /**
     * 获取缓存大小
     *
     * @return 缓存大小
     */
    public int getCacheSize() {
        return embeddingCache.size();
    }
}
