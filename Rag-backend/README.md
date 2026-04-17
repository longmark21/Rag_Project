# Rag-backend

基于 Spring Boot 3.2.0 + Spring AI 的 RAG 知识库后端服务。

## 技术栈

- **Spring Boot 3.2.0** (Java 17)
- **Spring AI** (Kimi Moonshot 大模型集成)
- **Spring Data JPA** (MySQL/PostgreSQL)
- **Spring Data Redis** (缓存)
- **PostgreSQL** + **pgvector** (向量数据库)
- **Apache PDFBox** (PDF 解析)
- **Apache POI** (Word 文档解析)
- **SpringDoc OpenAPI** (API 文档)

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+ / PostgreSQL 14+
- Redis

### 2. 配置

复制 `.env.example` 为 `.env` 并配置相关参数。

### 3. 构建

```bash
mvn clean package
```

### 4. 运行

```bash
java -jar target/rag-knowledge-base-1.0.0.jar
```

服务将在 `http://localhost:8080` 启动。

## 项目结构

```
Rag-backend/
├── src/
│   ├── main/
│   │   ├── java/com/example/rag/
│   │   │   ├── controller/      # REST 控制器
│   │   │   ├── service/        # 业务逻辑
│   │   │   ├── repository/      # 数据访问
│   │   │   ├── entity/         # 实体类
│   │   │   ├── dto/             # 数据传输对象
│   │   │   ├── config/          # 配置类
│   │   │   ├── util/            # 工具类
│   │   │   └── RagApplication.java
│   │   └── resources/
│   │       └── application.yml  # 应用配置
│   └── test/                    # 测试
├── pom.xml                      # Maven 配置
├── Dockerfile                   # Docker 配置
├── docker-compose.yml           # Docker Compose 配置
└── README.md
```

## API 文档

启动服务后访问：`http://localhost:8080/swagger-ui.html`
