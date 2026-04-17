# 后端 Java Spring Boot Dockerfile
FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 暴露端口
EXPOSE 8082

# 启动命令 - jar包从宿主机挂载到 /app/app.jar
ENTRYPOINT ["java", "-jar", "-Xms512m", "-Xmx1024m", "/app/app.jar"]
