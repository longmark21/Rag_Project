package com.example.rag.service;

import com.example.rag.dto.SearchRequest;
import com.example.rag.dto.SearchResponse;
import com.example.rag.entity.VectorChunk;
import com.example.rag.repository.VectorChunkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class VectorStoreService {

    private final VectorChunkRepository vectorChunkRepository;
    private final EmbeddingService embeddingService;
    private final JdbcTemplate jdbcTemplate;

    public VectorStoreService(
        VectorChunkRepository vectorChunkRepository,
        EmbeddingService embeddingService,
        @Qualifier("vectorJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.vectorChunkRepository = vectorChunkRepository;
        this.embeddingService = embeddingService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Value("${rag.retrieval.top-k:3}")
    private int topK;

    @Value("${rag.retrieval.score-threshold:0.7}")
    private double scoreThreshold;

    @Transactional
    public void saveVectorChunks(Long documentId, List<String> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            log.warn("文本块列表为空，无法保存向量");
            return;
        }

        try {
            List<float[]> embeddings = embeddingService.embedBatch(chunks);

            List<VectorChunk> vectorChunks = new ArrayList<>();
            for (int i = 0; i < chunks.size(); i++) {
                VectorChunk vectorChunk = new VectorChunk();
                vectorChunk.setDocumentId(documentId);
                vectorChunk.setChunkIndex(i);
                vectorChunk.setContent(chunks.get(i));
                vectorChunk.setEmbedding(embeddings.get(i));
                vectorChunks.add(vectorChunk);
            }

            vectorChunkRepository.saveAll(vectorChunks);
            log.info("向量块保存完成：documentId={}, chunkCount={}", documentId, chunks.size());
        } catch (Exception e) {
            log.error("保存向量块失败：documentId={}, error={}", documentId, e.getMessage(), e);
            throw new RuntimeException("保存向量块失败：" + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<SearchResponse> searchSimilarChunks(String query, int topK, double threshold, List<Long> fileIds) {
        if (query == null || query.trim().isEmpty()) {
            log.warn("查询文本为空，无法检索");
            return new ArrayList<>();
        }

        try {
            float[] queryEmbedding = embeddingService.embed(query);

            StringBuilder sql = new StringBuilder("""
                SELECT 
                    vc.id,
                    vc.document_id,
                    vc.chunk_index,
                    vc.content,
                    vc.embedding
                FROM vector_chunks vc
                WHERE vc.embedding IS NOT NULL
                """);

            if (fileIds != null && !fileIds.isEmpty()) {
                sql.append(" AND vc.document_id IN (");
                for (int i = 0; i < fileIds.size(); i++) {
                    if (i > 0) sql.append(",");
                    sql.append("?");
                }
                sql.append(")");
            }

            List<SearchResponse> results;
            if (fileIds != null && !fileIds.isEmpty()) {
                Object[] params = fileIds.toArray();
                results = jdbcTemplate.query(sql.toString(), params, 
                    (rs, rowNum) -> mapToSearchResponse(rs, queryEmbedding));
            } else {
                results = jdbcTemplate.query(sql.toString(), 
                    (rs, rowNum) -> mapToSearchResponse(rs, queryEmbedding));
            }

            List<SearchResponse> filteredResults = results.stream()
                .filter(r -> r.getScore() >= threshold)
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .limit(topK)
                .toList();

            log.info("相似度检索完成：query={}, fileIds={}, 返回结果数={}, 过滤后结果数={}", 
                query.substring(0, Math.min(query.length(), 50)), fileIds, results.size(), filteredResults.size());

            return filteredResults;
        } catch (Exception e) {
            log.error("相似度检索失败：{}", e.getMessage(), e);
            throw new RuntimeException("相似度检索失败：" + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<SearchResponse> searchSimilarChunks(String query, int topK, double threshold) {
        return searchSimilarChunks(query, topK, threshold, null);
    }

    @Transactional(readOnly = true)
    public List<SearchResponse> search(SearchRequest request) {
        int requestTopK = request.getTopK() != null ? request.getTopK() : topK;
        double requestThreshold = request.getThreshold() != null ? request.getThreshold() : scoreThreshold;
        return searchSimilarChunks(request.getQuery(), requestTopK, requestThreshold);
    }

    @Transactional
    public void deleteByDocumentId(Long documentId) {
        try {
            vectorChunkRepository.deleteByDocumentId(documentId);
            log.info("删除文档向量块：documentId={}", documentId);
        } catch (Exception e) {
            log.error("删除文档向量块失败：documentId={}, error={}", documentId, e.getMessage(), e);
            throw new RuntimeException("删除文档向量块失败：" + e.getMessage(), e);
        }
    }

    private SearchResponse mapToSearchResponse(ResultSet rs, float[] queryEmbedding) throws SQLException {
        SearchResponse response = new SearchResponse();
        response.setChunkId(rs.getLong("id"));
        response.setDocumentId(rs.getLong("document_id"));
        response.setChunkIndex(rs.getInt("chunk_index"));
        response.setContent(rs.getString("content"));
        
        String embeddingStr = rs.getString("embedding");
        float[] chunkEmbedding = parseEmbeddingFromString(embeddingStr);
        double similarity = calculateCosineSimilarity(queryEmbedding, chunkEmbedding);
        response.setScore(similarity);
        
        return response;
    }
    
    private float[] parseEmbeddingFromString(String embeddingStr) {
        if (embeddingStr == null || embeddingStr.trim().isEmpty()) {
            return new float[0];
        }
        String trimmed = embeddingStr.trim();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }
        if (trimmed.isEmpty()) {
            return new float[0];
        }
        String[] parts = trimmed.split(",");
        float[] result = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Float.parseFloat(parts[i].trim());
        }
        return result;
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
