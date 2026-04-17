# Kimi AI 配置说明

## ✅ 配置已完成

项目已经成功配置为使用 **Kimi** 作为主要 AI 模型，**通义千问** 作为 Embedding 服务。

### 当前配置

```yaml
spring:
  ai:
    default-model: kimi  # 默认使用 Kimi 模型
    
    # Kimi API 配置（聊天）
    moonshot:
      api-key: abc
      base-url: https://api.moonshot.cn/v1
      chat:
        options:
          model: moonshot-k2-thinking
          temperature: 0.3
    
    # 通义千问 API 配置（Embedding）
    dashscope:
      api-key: abc
      embedding:
        options:
          model: text-embedding-v2
          dimensions: 1536
```

## 架构说明

### 🗣️ 聊天服务 - Kimi
- **模型**：moonshot-k2-thinking
- **特点**：推理能力强，适合复杂问题
- **温度**：0.3（回答稳定）

### 🔢 Embedding 服务 - 通义千问
- **模型**：text-embedding-v2
- **维度**：1536
- **特点**：中文语义理解好，向量质量高

## 为什么这样配置？

1. **Kimi** 在中文聊天和推理方面表现优秀
2. **通义千问** 的 Embedding 模型在中文文本向量化方面效果好
3. 两者结合可以发挥各自优势，提供最佳的 RAG 问答体验

## 测试配置

### 1. 启动项目
```bash
mvn spring-boot:run
```

### 2. 测试聊天接口
```bash
curl -X POST http://localhost:8080/api/chat/question \
  -H "Content-Type: application/json" \
  -d '{
    "question": "你好，请介绍一下你自己",
    "sessionId": "test-session"
  }'
```

### 3. 测试文档上传
```bash
curl -X POST -F "file=@test.pdf" \
  http://localhost:8080/api/documents/upload
```

## 注意事项

### ⚠️ API Key 安全
- ✅ 已配置到 application.yml
- ⚠️ 不要将 API Key 提交到版本控制系统
- 💡 建议使用环境变量或配置中心

### 💰 费用控制
- **Kimi**：约 0.004 元/千 tokens
- **通义千问 Embedding**：约 0.0007 元/千 tokens
- 💡 关注 API 调用量，避免费用超标

### 🌐 网络要求
- 确保服务器可以访问：
  - https://api.moonshot.cn/v1
  - https://dashscope.aliyuncs.com/compatible-mode/v1

## 切换模型

如需切换到通义千问聊天，修改配置：
```yaml
spring:
  ai:
    default-model: qwen  # 改为 qwen
```

## 当前状态

✅ 项目已配置为使用 Kimi 作为默认聊天模型
✅ 通义千问作为 Embedding 服务
✅ 支持动态切换模型
✅ 使用真实的 API 调用，不再使用模拟实现

## 下一步

1. ✅ 配置已完成
2. 🚀 启动项目测试
3. 📊 监控 API 调用情况
4. 🎯 根据实际需求调整模型参数
