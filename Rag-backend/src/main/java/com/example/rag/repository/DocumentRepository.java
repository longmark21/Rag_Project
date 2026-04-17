package com.example.rag.repository;

import com.example.rag.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByStatus(String status);

    @Query("SELECT d FROM Document d WHERE d.fileName LIKE %:keyword% OR d.originalFileName LIKE %:keyword%")
    List<Document> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT d FROM Document d WHERE d.uploadTime BETWEEN :startTime AND :endTime")
    List<Document> findByUploadTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                          @Param("endTime") LocalDateTime endTime);

    @Query("SELECT d FROM Document d WHERE d.status = 'PROCESSING' AND d.uploadTime < :time")
    List<Document> findStuckProcessingDocuments(@Param("time") LocalDateTime time);

    List<Document> findByUploadDate(String uploadDate);

    @Query("SELECT COUNT(d) FROM Document d WHERE d.status = :status")
    Long countByStatus(@Param("status") String status);

    @Query("SELECT d.status, COUNT(d) FROM Document d GROUP BY d.status")
    List<Object[]> countByStatusGroup();

    @Query("SELECT SUM(d.fileSize) FROM Document d WHERE d.status = 'COMPLETED'")
    Long getTotalFileSizeOfCompletedDocuments();
}
