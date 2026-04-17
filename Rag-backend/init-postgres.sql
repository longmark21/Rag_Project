-- PostgreSQL 数据库初始化脚本
-- 连接命令: psql -U postgres

-- 创建 rag_knowledge 数据库
CREATE DATABASE rag_knowledge;

-- 连接到 rag_knowledge 数据库
\c rag_knowledge

-- 启用 pgvector 扩展（如果不存在）
CREATE EXTENSION IF NOT EXISTS vector;

-- 创建 document_chunks 表
CREATE TABLE IF NOT EXISTS document_chunks (
    id SERIAL PRIMARY KEY,
    document_id INTEGER NOT NULL,
    chunk_text TEXT NOT NULL,
    embedding vector(1536)
);

-- 创建索引以提高向量搜索性能
CREATE INDEX IF NOT EXISTS idx_document_chunks_embedding ON document_chunks USING ivfflat (embedding vector_cosine_ops);

-- 查看创建的表
\dt

-- 查看表结构
\d document_chunks

-- 插入测试数据
INSERT INTO document_chunks (document_id, chunk_text, embedding) VALUES 
(1, '这是第一个文档的第一个 chunk', '[0.1, 0.2, 0.3, 0.4, 0.5]'),
(1, '这是第一个文档的第二个 chunk', '[0.6, 0.7, 0.8, 0.9, 1.0]'),
(2, '这是第二个文档的第一个 chunk', '[1.1, 1.2, 1.3, 1.4, 1.5]');

-- 查看数据
SELECT * FROM document_chunks;

-- 测试向量搜索
SELECT id, document_id, chunk_text, embedding <-> '[0.1, 0.2, 0.3, 0.4, 0.5]' as distance 
FROM document_chunks 
ORDER BY distance 
LIMIT 2;
