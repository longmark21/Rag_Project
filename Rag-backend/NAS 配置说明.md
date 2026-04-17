# NAS 数据库配置说明

## ✅ 配置已更新

项目已经成功配置为连接您的 NAS 上的数据库服务。

### 当前配置

#### 📊 MySQL（主数据库）
- **主机**: 192.168.31.29
- **端口**: 3306
- **数据库**: rag_knowledge
- **用户名**: root
- **密码**: abc
- **用途**: 存储文档元数据、对话记录等

#### 🐘 PostgreSQL + Pgvector（向量数据库）
- **主机**: 192.168.31.29
- **端口**: 5433
- **数据库**: rag_vector
- **用户名**: postgres
- **密码**: abc
- **用途**: 存储文本向量数据

#### 🔴 Redis（缓存）
- **主机**: 192.168.31.29
- **端口**: 6379
- **密码**: abc
- **数据库**: 0
- **用途**: 缓存对话历史、会话数据

#### 🌐 应用服务
- **端口**: 8081（避免与 Nginx 8080 冲突）
- **上下文路径**: /api
- **访问地址**: http://192.168.31.29:8081/api

#### 🚀 Nginx
- **端口**: 8080
- **用途**: 反向代理、负载均衡

## 配置详情

### application.yml 关键配置

```yaml
server:
  port: 8081  # 应用端口

spring:
  datasource:
    primary:  # MySQL 配置
      jdbc-url: jdbc:mysql://192.168.31.29:3306/rag_knowledge
      username: root
      password: abc
    
    vector:  # PostgreSQL 配置
      jdbc-url: jdbc:postgresql://192.168.31.29:5433/rag_vector
      username: postgres
      password: abc
  
  data:
    redis:  # Redis 配置
      host: 192.168.31.29
      port: 6379
      password: abc
```

## 启动步骤

### 1. 确认数据库已创建

通过 SSH 连接到 NAS，检查数据库是否已创建：

```bash
# 检查 MySQL
mysql -h 192.168.31.29 -P 3306 -u root -p625625mahao -e "SHOW DATABASES;"

# 检查 PostgreSQL
psql -h 192.168.31.29 -p 5433 -U postgres -c "\l"

# 检查 Redis
redis-cli -h 192.168.31.29 -p 6379 -a 625625mahao ping
```

### 2. 启动应用

```bash
mvn spring-boot:run
```

### 3. 访问应用

- **直接访问**: http://localhost:8081/api
- **通过 NAS 访问**: http://192.168.31.29:8081/api

### 4. 测试接口

```bash
# 健康检查
curl http://192.168.31.29:8081/api/actuator/health

# 测试问答
curl -X POST http://192.168.31.29:8081/api/chat/question \
  -H "Content-Type: application/json" \
  -d '{"question": "你好", "sessionId": "test"}'
```

## Nginx 配置建议

如果需要将应用通过 Nginx 暴露到 8080 端口，可以配置反向代理：

```nginx
server {
    listen 8080;
    server_name 192.168.31.29;

    location /api/ {
        proxy_pass http://192.168.31.29:8081/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

配置后访问：http://192.168.31.29:8080/api

## 注意事项

### ⚠️ 网络安全
- 确保 NAS 防火墙允许以下端口：
  - 3306 (MySQL)
  - 5433 (PostgreSQL)
  - 6379 (Redis)
  - 8081 (应用)
  - 8080 (Nginx)

### 🔒 密码安全
- 当前使用的是明文密码
- 建议在生产环境中使用环境变量或配置中心
- 定期更换密码

### 📝 数据库初始化
应用启动时会自动创建表结构（`ddl-auto: update`）

### 🚀 性能优化
- MySQL 连接池：最大 20 个连接
- PostgreSQL 连接池：最大 10 个连接
- Redis 连接池：最大 20 个连接

## 故障排查

### 连接失败
```bash
# 测试 MySQL 连接
mysql -h 192.168.31.29 -P 3306 -u root -p625625mahao

# 测试 PostgreSQL 连接
psql -h 192.168.31.29 -p 5433 -U postgres

# 测试 Redis 连接
redis-cli -h 192.168.31.29 -p 6379 -a 625625mahao ping
```

### 查看日志
```bash
# 应用日志
tail -f logs/rag-knowledge-base.log
```

## 当前状态

✅ MySQL 配置完成
✅ PostgreSQL 配置完成
✅ Redis 配置完成
✅ 应用端口配置完成（8081）
✅ 避免与 Nginx 端口冲突

## 下一步

1. ✅ 配置已更新完成
2. 🚀 启动应用测试连接
3. 📊 验证数据库连接
4. 🧪 测试问答功能
