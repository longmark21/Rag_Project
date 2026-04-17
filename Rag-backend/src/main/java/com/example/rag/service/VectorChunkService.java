package com.example.rag.service;

import com.example.rag.entity.Document;
import com.example.rag.entity.VectorChunk;
import com.example.rag.repository.VectorChunkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorChunkService {

    private final VectorChunkRepository vectorChunkRepository;
    private final EmbeddingService embeddingService;

    @Transactional
    public void processAndSaveChunks(Document document, List<String> chunks) {
        if (document == null || chunks == null || chunks.isEmpty()) {
            log.warn("文档或文本块为空，无法处理");
            return;
        }

        try {
            List<VectorChunk> vectorChunks = new ArrayList<>();

            for (int i = 0; i < chunks.size(); i++) {
                VectorChunk vectorChunk = new VectorChunk();
                vectorChunk.setDocumentId(document.getId());
                vectorChunk.setChunkIndex(i);
                vectorChunk.setContent(chunks.get(i));

                // 使用 EmbeddingService 进行向量化
                float[] embedding = embeddingService.embed(chunks.get(i));
                vectorChunk.setEmbedding(embedding);

                vectorChunks.add(vectorChunk);
            }

            vectorChunkRepository.saveAll(vectorChunks);
            log.info("向量块保存完成：documentId={}, chunkCount={}", document.getId(), chunks.size());
        } catch (Exception e) {
            log.error("处理和保存向量块失败：documentId={}, error={}", document.getId(), e.getMessage(), e);
            throw new RuntimeException("处理和保存向量块失败：" + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<VectorChunk> getChunksByDocumentId(Long documentId) {
        if (documentId == null || documentId <= 0) {
            log.warn("无效的文档ID：{}", documentId);
            return new ArrayList<>();
        }

        try {
            return vectorChunkRepository.findByDocumentIdOrderByChunkIndex(documentId);
        } catch (Exception e) {
            log.error("获取文档向量块失败：documentId={}, error={}", documentId, e.getMessage(), e);
            throw new RuntimeException("获取文档向量块失败：" + e.getMessage(), e);
        }
    }

    @Transactional
    public void deleteByDocumentId(Long documentId) {
        if (documentId == null || documentId <= 0) {
            log.warn("无效的文档ID：{}", documentId);
            return;
        }

        try {
            vectorChunkRepository.deleteByDocumentId(documentId);
            log.info("删除文档向量块：documentId={}", documentId);
        } catch (Exception e) {
            log.error("删除文档向量块失败：documentId={}, error={}", documentId, e.getMessage(), e);
            throw new RuntimeException("删除文档向量块失败：" + e.getMessage(), e);
        }
    }

    public List<VectorChunk> searchSimilarChunks(String query, int topK) {
        if (query == null || query.trim().isEmpty()) {
            log.warn("查询文本为空，无法检索");
            return new ArrayList<>();
        }

        if (topK <= 0) {
            topK = 5; // 默认值
        }

        try {
            // 使用 EmbeddingService 进行向量化
            float[] queryEmbedding = embeddingService.embed(query);

            // 注意：这里使用了简单的内存计算方法
            // 实际生产环境中，应该使用 PostgreSQL 的 pgvector 扩展进行向量搜索
            List<VectorChunk> allChunks = vectorChunkRepository.findAll();
            List<VectorChunk> similarChunks = new ArrayList<>();

            for (VectorChunk chunk : allChunks) {
                if (chunk.getEmbedding() != null) {
                    double similarity = calculateCosineSimilarity(queryEmbedding, chunk.getEmbedding());
                    if (similarity >= 0.7) {
                        similarChunks.add(chunk);
                    }
                }
            }

            // 按相似度排序
            similarChunks.sort((a, b) -> {
                double similarityA = calculateCosineSimilarity(queryEmbedding, a.getEmbedding());
                double similarityB = calculateCosineSimilarity(queryEmbedding, b.getEmbedding());
                return Double.compare(similarityB, similarityA);
            });

            return similarChunks.stream().limit(topK).toList();
        } catch (Exception e) {
            log.error("搜索相似向量块失败：query={}, error={}", query, e.getMessage(), e);
            throw new RuntimeException("搜索相似向量块失败：" + e.getMessage(), e);
        }
    }

    private double calculateCosineSimilarity(float[] vec1, float[] vec2) {
        if (vec1 == null || vec2 == null || vec1.length != vec2.length) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vec1.length; i++) {
            dotProduct += vec1[i] * vec2[i];
            norm1 += Math.pow(vec1[i], 2);
            norm2 += Math.pow(vec2[i], 2);
        }

        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
