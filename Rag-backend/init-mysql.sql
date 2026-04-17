-- MySQL 数据库初始化脚本
-- 连接命令: mysql -u root -p

-- 创建 rag_knowledge 数据库
CREATE DATABASE IF NOT EXISTS rag_knowledge CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用 rag_knowledge 数据库
USE rag_knowledge;

-- 创建 user 表
CREATE TABLE IF NOT EXISTS user (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'user',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建 document 表
CREATE TABLE IF NOT EXISTS document (
    id INT AUTO_INCREMENT PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 查看创建的表
SHOW TABLES;

-- 查看表结构
DESCRIBE user;
DESCRIBE document;

-- 插入测试数据
INSERT INTO user (username, password, role) VALUES 
('admin', 'admin123', 'admin'),
('user1', 'user123', 'user');

INSERT INTO document (file_name, file_url, status, created_by) VALUES 
('test.pdf', '/uploads/test.pdf', 'completed', 'admin'),
('document.docx', '/uploads/document.docx', 'pending', 'user1');

-- 查看数据
SELECT * FROM user;
SELECT * FROM document;
