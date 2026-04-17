@echo off

echo 启动后端服务...
start cmd /c "mvn spring-boot:run"

echo 等待 10 秒让后端启动...
timeout /t 10 /nobreak >nul

echo 启动前端服务...
start cmd /c "npm run dev"

echo 启动完成！
echo 后端 Swagger: http://localhost:8080/swagger-ui.html
echo 前端页面: http://localhost:5173