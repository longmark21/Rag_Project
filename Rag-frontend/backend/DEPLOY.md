# 飞牛NAS Docker部署指南

## 前置要求
- 飞牛NAS已安装Docker
- SSH访问权限（可选）

---

## 文件夹结构

```
Rag-frontend/
├── backend/
│   ├── Dockerfile          # 后端Dockerfile
│   └── app.jar             # ← 你的jar包放这里，命名为app.jar
├── Dockerfile              # 前端Dockerfile
├── docker-compose.yml
└── nginx.conf
```

## 操作步骤

### 1. 准备jar包

将你的jar包复制到 `backend/` 目录，并重命名为 `app.jar`：
```
/path/to/your-project/backend/
├── Dockerfile
└── app.jar        ← 放这里
```

### 2. 上传到飞牛NAS

将整个 `Rag-frontend` 文件夹上传到NAS，例如：
```
/vol1/1000/Rag-frontend/
```

### 3. SSH登录或使用飞牛Docker界面

#### 方式A：SSH终端（推荐）

```bash
# 进入项目目录
cd /vol1/1000/Rag-frontend

# 构建并启动所有服务
docker-compose up -d --build

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

#### 方式B：使用docker命令

```bash
# 1. 先构建后端镜像
cd /vol1/1000/Rag-frontend/backend
docker build -t rag-backend:latest .

# 2. 再构建前端镜像
cd /vol1/1000/Rag-frontend
docker build -t rag-frontend:latest .

# 3. 创建网络
docker network create rag-network

# 4. 运行后端
docker run -d \
  --name rag-backend \
  --network rag-network \
  -p 8082:8082 \
  -v /vol1/1000/Rag-frontend/data:/data \
  rag-backend:latest

# 5. 运行前端
docker run -d \
  --name rag-frontend \
  --network rag-network \
  -p 3000:80 \
  --link rag-backend \
  rag-frontend:latest
```

### 4. 验证部署

- 前端地址: `http://你的NASIP:3000`
- 后端API: `http://你的NASIP:8082/api/v1/`

### 5. 修改jar包后的更新

```bash
cd /vol1/1000/Rag-frontend
docker-compose down
docker-compose up -d --build
```

---

## 注意事项

1. **jar包命名**: 必须命名为 `app.jar`，否则需要修改Dockerfile
2. **端口冲突**: 确保3000和8082端口未被占用
3. **数据持久化**: 数据保存在 `./data` 目录
