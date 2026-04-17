# 基于eclipse-temurin:17-jre-alpine基础镜像
FROM eclipse-temurin:17-jre-alpine

# 维护者信息
MAINTAINER RAG Knowledge Base Team

# 设置工作目录
WORKDIR /vol1/1000/Docker/openjdk/app

# 复制jar文件到容器中
COPY target/rag-knowledge-base-1.0.0.jar /vol1/1000/Docker/openjdk/app/rag-knowledge-base.jar

# 复制环境变量配置文件
COPY .env /vol1/1000/Docker/openjdk/app/.env

# 创建上传目录
RUN mkdir -p /vol1/1000/My-Rag-Project/uploads
RUN mkdir -p /vol1/1000/My-Rag-Project/logs

# 暴露端口
EXPOSE 8080

# 启动应用
ENTRYPOINT ["java", "-jar", "/vol1/1000/Docker/openjdk/app/rag-knowledge-base.jar"]
