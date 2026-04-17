-- 连接到新数据库
-- 使用 JDBC 连接时不需要手动切换数据库，连接字符串中已指定

-- 创建 vector 扩展（需要 PostgreSQL 11+ 和 pgvector 扩展）
-- 如果未安装 pgvector，请先安装：
-- Ubuntu/Debian: sudo apt-get install postgresql-15-pgvector
-- Windows: 下载并安装 pgvector

-- 创建向量存储表
CREATE TABLE IF NOT EXISTS vector_chunks (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL,
    chunk_index INTEGER NOT NULL,
    content TEXT NOT NULL,
    embedding TEXT,  -- 使用 TEXT 类型存储向量，暂时不依赖 pgvector 扩展
    metadata TEXT,  -- 使用 TEXT 类型存储 metadata，暂时不依赖 jsonb 类型
    token_count INTEGER,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_document_id ON vector_chunks(document_id);
CREATE INDEX IF NOT EXISTS idx_chunk_index ON vector_chunks(chunk_index);

-- 创建文档表
CREATE TABLE IF NOT EXISTS documents (
    id BIGSERIAL PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_type VARCHAR(20) NOT NULL,
    file_size BIGINT,
    status VARCHAR(20) NOT NULL,
    text_content TEXT,
    character_count INTEGER,
    chunk_count INTEGER,
    total_chunks INTEGER,
    error_message TEXT,
    upload_date VARCHAR(10),
    upload_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    process_time TIMESTAMP,
    update_time TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_file_name ON documents(file_name);
CREATE INDEX IF NOT EXISTS idx_upload_time ON documents(upload_time);
CREATE INDEX IF NOT EXISTS idx_status ON documents(status);

-- 创建对话表
CREATE TABLE IF NOT EXISTS conversations (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(255) UNIQUE NOT NULL,
    user_id VARCHAR(255),
    title VARCHAR(255),
    status VARCHAR(20),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP,
    message_count INTEGER DEFAULT 0
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_session_id ON conversations(session_id);
CREATE INDEX IF NOT EXISTS idx_user_id ON conversations(user_id);
CREATE INDEX IF NOT EXISTS idx_create_time ON conversations(create_time);

-- 创建消息表
CREATE TABLE IF NOT EXISTS messages (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    context TEXT,
    sources JSONB,
    token_count INTEGER,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_conversation_id ON messages(conversation_id);

-- 授权（如果需要）
-- GRANT ALL PRIVILEGES ON DATABASE rag_vector TO postgres;
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO postgres;
