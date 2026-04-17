-- 创建 MySQL 数据库（文档元数据）
CREATE DATABASE IF NOT EXISTS rag_knowledge CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建 PostgreSQL 数据库（向量存储）
-- 注意：这个需要在 PostgreSQL 中执行
-- CREATE DATABASE rag_vector;

-- 授予权限
GRANT ALL PRIVILEGES ON DATABASE rag_knowledge TO 'root'@'%';
FLUSH PRIVILEGES;
