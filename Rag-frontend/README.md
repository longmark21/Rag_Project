# Rag-frontend

基于 Vue 3 + Vite + TypeScript + Element Plus + Pinia + TailwindCSS 构建的现代化 RAG 知识库前端应用。

## 技术栈

- **Vue 3** (Composition API)
- **Vite** (构建工具)
- **TypeScript** (类型系统)
- **Element Plus** (UI 库)
- **Pinia** (状态管理)
- **TailwindCSS** (样式)
- **MarkdownIt** (Markdown 渲染)

## 快速开始

### 1. 安装依赖

```bash
npm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

前端应用将在 `http://localhost:3000` 启动。

### 3. 构建生产版本

```bash
npm run build
```

## 项目结构

```
Rag-frontend/
├── src/
│   ├── components/            # Vue 组件
│   ├── styles/                # 样式文件
│   ├── api.ts                 # API 接口定义
│   ├── store.ts               # Pinia 状态管理
│   ├── main.ts                # 应用入口
│   └── App.vue                # 根组件
├── index.html                 # HTML 模板
├── vite.config.ts             # Vite 配置
├── tsconfig.json              # TypeScript 配置
└── package.json               # 项目配置
```

## API 接口

前端应用需要后端服务在 `http://localhost:8080` 提供以下接口：

- **文件上传**：`POST /api/documents/upload`
- **获取文档列表**：`GET /api/documents`
- **删除文档**：`DELETE /api/documents/{id}`
- **流式聊天**：`POST /api/chat/stream`
