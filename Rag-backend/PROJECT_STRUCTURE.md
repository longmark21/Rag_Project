# RAG 知识库项目结构说明

## 项目概述
基于 Spring Boot 3.2.0 / Spring AI 1.0.0-M3 的 RAG（检索增强生成）知识库问答系统。

## 技术栈
- **框架**: Spring Boot 3.2.0
- **AI**: Spring AI 1.0.0-M3 (Moonshot/Kimi, DashScope)
- **数据库**: 
  - MySQL 8.0 (文档元数据、对话、消息)
  - PostgreSQL 15 + pgvector (向量存储)
  - Redis 7 (会话缓存)
- **ORM**: Spring Data JPA + Hibernate
- **文档解析**: Apache PDFBox 3.0, Apache POI 5.2.5
- **构建工具**: Maven

## 项目结构

```
RAG_Project/
├── src/
│   ├── main/
│   │   ├── java/com/example/rag/
│   │   │   ├── RagApplication.java          # 主启动类
│   │   │   ├── config/                       # 配置类
│   │   │   │   ├── ChatModelConfig.java     # 聊天模型配置
│   │   │   │   ├── DataSourceConfig.java    # 双数据源配置
│   │   │   │   ├── JpaConfig.java           # JPA 配置（主数据源）
│   │   │   │   ├── JdbcTemplateConfig.java  # JdbcTemplate 配置
│   │   │   │   ├── RedisConfig.java         # Redis 配置
│   │   │   │   └── SpringAIConfig.java      # Spring AI 配置
│   │   │   ├── controller/                   # 控制器层
│   │   │   │   ├── ChatController.java      # 问答接口
│   │   │   │   ├── ConversationController.java  # 对话管理接口
│   │   │   │   ├── DocumentController.java  # 文档管理接口
│   │   │   │   └── SearchController.java    # 搜索接口
│   │   │   ├── dto/                          # 数据传输对象
│   │   │   │   ├── ChatRequest.java         # 聊天请求
│   │   │   │   ├── ChatResponse.java        # 聊天响应
│   │   │   │   ├── DocumentUploadResponse.java
│   │   │   │   ├── SearchRequest.java
│   │   │   │   ├── SearchResponse.java
│   │   │   │   └── UploadResponse.java
│   │   │   ├── entity/                       # 实体类
│   │   │   │   ├── Conversation.java        # 对话实体
│   │   │   │   ├── Document.java            # 文档实体
│   │   │   │   ├── Message.java             # 消息实体
│   │   │   │   └── VectorChunk.java         # 向量块实体
│   │   │   ├── mock/                         # 模拟实现
│   │   │   │   ├── ChatClient.java          # 模拟聊天客户端
│   │   │   │   └── EmbeddingModel.java      # 模拟嵌入模型
│   │   │   ├── repository/                   # 数据访问层
│   │   │   │   ├── ConversationRepository.java
│   │   │   │   ├── DocumentRepository.java
│   │   │   │   ├── MessageRepository.java
│   │   │   │   └── VectorChunkRepository.java
│   │   │   ├── service/                      # 业务逻辑层
│   │   │   │   ├── ConversationService.java
│   │   │   │   ├── DocumentService.java
│   │   │   │   ├── EmbeddingService.java
│   │   │   │   ├── RagService.java          # RAG 核心服务
│   │   │   │   ├── VectorChunkService.java
│   │   │   │   └── VectorStoreService.java  # 向量存储服务
│   │   │   └── util/                         # 工具类
│   │   │       ├── FileParser.java          # 文件解析器
│   │   │       ├── GlobalExceptionHandler.java
│   │   │       ├── TextSplitter.java        # 文本分割器
│   │   │       └── VectorConverter.java     # 向量类型转换器
│   │   └── resources/
│   │       ├── application.yml              # 应用配置文件
│   │       └── RAG.txt
│   └── test/
│       ├── java/com/example/rag/
│       │   └── RagApplicationTests.java     # 基础测试类
│       └── resources/
├── pom.xml                                   # Maven 配置
├── docker-compose.yml                        # Docker 编排
├── Dockerfile                                # Docker 镜像
├── .env.example                              # 环境变量示例
├── init-database.sql                         # 数据库初始化脚本
└── README.md                                 # 项目说明
```

## 核心模块说明

### 1. 配置模块 (config)
- **DataSourceConfig**: 配置 MySQL 和 PostgreSQL 双数据源
- **JpaConfig**: 配置主数据源的 EntityManagerFactory 和 TransactionManager
- **JdbcTemplateConfig**: 为两个数据源分别配置 JdbcTemplate
- **RedisConfig**: 配置 Redis 连接池
- **ChatModelConfig**: 配置聊天模型（Kimi/Moonshot）
- **SpringAIConfig**: 配置 Spring AI 的 EmbeddingModel

### 2. 实体模块 (entity)
- **Document**: 文档元数据（MySQL）
  - 文件名、路径、类型、大小
  - 处理状态、分块信息
  - 上传时间、处理时间
  
- **Conversation**: 对话会话（MySQL）
  - 会话 ID、用户 ID、会话标题
  - 消息计数、最后活跃时间
  
- **Message**: 对话消息（MySQL）
  - 对话 ID、角色（USER/ASSISTANT）
  - 消息内容、上下文、来源信息
  - Token 数量
  
- **VectorChunk**: 向量块（PostgreSQL）
  - 文档 ID、块索引、内容
  - 向量嵌入（float[]，使用 VectorConverter 转换）
  - 元数据、Token 数量

### 3. 服务模块 (service)
- **RagService**: RAG 核心业务逻辑
  - chat(): 处理问答请求
  - buildContext(): 构建上下文
  - buildPrompt(): 构建提示词
  - generateResponse(): 生成响应
  
- **DocumentService**: 文档管理服务
  - uploadDocument(): 上传文档
  - processDocument(): 处理文档（解析、分块、向量化）
  - deleteDocument(): 删除文档
  
- **VectorStoreService**: 向量存储服务
  - saveVectorChunks(): 保存向量块到 PostgreSQL
  - searchSimilarChunks(): 相似度检索
  - deleteByDocumentId(): 删除文档向量
  
- **EmbeddingService**: 文本向量化服务
  - embed(): 单文本向量化
  - embedBatch(): 批量向量化

### 4. 控制器模块 (controller)
- **ChatController**: 问答接口
  - POST /api/chat - 问答
  - GET /api/chat/conversations/{id}/messages - 获取对话历史
  
- **DocumentController**: 文档管理接口
  - POST /api/documents/upload - 上传文档
  - POST /api/documents/{id}/process - 处理文档
  - GET /api/documents - 获取文档列表
  - DELETE /api/documents/{id} - 删除文档
  
- **SearchController**: 搜索接口
  - POST /api/search - 搜索相似文档

### 5. 工具模块 (util)
- **FileParser**: 文件解析器
  - 支持 PDF、DOC、DOCX、TXT 格式
  - 文本清洗和编码检测
  
- **TextSplitter**: 文本分割器
  - 按段落、句子分割
  - 控制分块大小和重叠度
  
- **VectorConverter**: 向量类型转换器
  - float[] ↔ String 转换
  - 用于 JPA 持久化

## 数据流

### 文档上传流程
1. 用户上传文件 → DocumentController.uploadDocument()
2. 保存文件到本地 → DocumentService.uploadDocument()
3. 创建 Document 实体（状态=PENDING）→ MySQL
4. 异步处理文档 → DocumentService.processDocument()
5. 解析文件内容 → FileParser.parseFile()
6. 分割文本 → TextSplitter.split()
7. 向量化文本块 → EmbeddingService.embedBatch()
8. 保存向量块 → VectorStoreService.saveVectorChunks() → PostgreSQL
9. 更新 Document 状态 → COMPLETED

### 问答流程
1. 用户提问 → ChatController.chat()
2. 获取或创建对话 → ConversationService.getOrCreateConversation()
3. 问题向量化 → EmbeddingService.embed()
4. 检索相似向量块 → VectorStoreService.searchSimilarChunks() → PostgreSQL
5. 构建上下文和提示词 → RagService.buildContext(), buildPrompt()
6. 调用 LLM 生成回答 → ChatClient.prompt() → Kimi/Moonshot
7. 保存用户和助手消息 → MessageRepository.save() → MySQL
8. 返回回答 → ChatResponse

## 关键配置

### 数据库配置 (application.yml)
```yaml
spring:
  datasource:
    primary:  # MySQL
      jdbc-url: jdbc:mysql://192.168.31.29:3306/rag_knowledge
      username: root
      password: xxx
    vector:   # PostgreSQL
      jdbc-url: jdbc:postgresql://192.168.31.29:5433/rag_vector
      username: postgres
      password: xxx
  jpa:
    hibernate:
      ddl-auto: create
  data:
    redis:
      host: 192.168.31.29
      port: 6379
      password: xxx
```

### 向量配置
```yaml
spring-ai:
  vectorstore:
    pgvector:
      dimension: 1536
      distance-type: cosine
      index-type: ivfflat
```

### 应用配置
```yaml
server:
  port: 8082
  servlet:
    context-path: /api

text:
  chunk:
    size: 500
    overlap: 50

rag:
  retrieval:
    top-k: 5
    score-threshold: 0.7
```

## API 接口

### 问答接口
```bash
POST /api/chat
{
  "question": "什么是 RAG？",
  "sessionId": "user123"
}

Response:
{
  "answer": "RAG 是检索增强生成...",
  "sources": [...],
  "context": "...",
  "conversationId": 1
}
```

### 文档上传
```bash
POST /api/documents/upload
Content-Type: multipart/form-data

file: <binary>
```

### 相似度搜索
```bash
POST /api/search
{
  "query": "人工智能",
  "topK": 5,
  "threshold": 0.7
}
```

## 已修复的关键问题

1. ✅ **EmbeddingModel 返回类型不匹配**: 从 `List<Float>` 改为`float[]`
2. ✅ **JdbcTemplate 双数据源冲突**: 显式定义 `vectorJdbcTemplate`和`primaryJdbcTemplate`
3. ✅ **VectorChunk embedding 类型**: 使用 `VectorConverter` 转换`float[]`为 TEXT
4. ✅ **向量读写数据源不一致**: VectorStoreService 使用 `vectorJdbcTemplate` 确保读写 PostgreSQL
5. ✅ **DTO 缺少 Jackson 构造函数**: 添加`@NoArgsConstructor`和`@AllArgsConstructor`
6. ✅ **FileParser PDF 解析**: 使用 PDFBox 3.0 API（当前为模拟实现，待完善）
7. ✅ **测试目录缺失**: 创建 `src/test/java`和`src/test/resources`

## 待办事项

- [ ] 完善 PDFBox 3.0.1 的真实解析实现
- [ ] 添加集成测试
- [ ] 添加性能监控和日志分析
- [ ] 优化向量检索性能（添加索引）
- [ ] 实现文档增量更新

## 部署说明

### 环境要求
- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- PostgreSQL 15+ (带 pgvector 扩展)
- Redis 7+

### 启动步骤
1. 启动数据库和 Redis
2. 执行 `init-database.sql` 初始化数据库
3. 配置 `application.yml` 中的数据库连接
4. 运行 `mvn spring-boot:run`
5. 访问 `http://localhost:8082/api/chat`

### Docker 部署
```bash
docker-compose up -d
```

## 注意事项

1. **PDFBox 3.0 API**: 当前使用模拟实现，实际部署需要参考 PDFBox 3.0.1 文档更新 `FileParser.parsePDF()` 方法
2. **向量维度**: 当前使用 1536 维（通义千问 text-embedding-v2），如使用其他模型需调整
3. **数据库初始化**: PostgreSQL 需要先安装 pgvector 扩展
4. **文件上传路径**: 默认为 `./uploads`，需确保有写权限
