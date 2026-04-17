package com.example.rag.repository;

import com.example.rag.entity.VectorChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VectorChunkRepository extends JpaRepository<VectorChunk, Long> {

    List<VectorChunk> findByDocumentId(Long documentId);

    List<VectorChunk> findByDocumentIdOrderByChunkIndex(Long documentId);

    void deleteByDocumentId(Long documentId);

    @Query("SELECT COUNT(vc) FROM VectorChunk vc WHERE vc.documentId = :documentId")
    Integer countByDocumentId(@Param("documentId") Long documentId);
}
