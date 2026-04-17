#!/bin/bash

echo "启动后端服务..."
mvn spring-boot:run &

echo "等待 10 秒让后端启动..."
sleep 10

echo "启动前端服务..."
npm run dev &

echo "启动完成！"
echo "后端 Swagger: http://localhost:8080/swagger-ui.html"
echo "前端页面: http://localhost:5173"