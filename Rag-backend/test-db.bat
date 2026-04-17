@echo off
echo ========================================
echo 数据库连通性测试
echo ========================================
echo.

echo 1. 测试 MySQL 端口 (192.168.31.29:3306)...
powershell -Command "$client = New-Object System.Net.Sockets.TcpClient; try { $client.Connect('192.168.31.29', 3306); Write-Host 'MySQL: SUCCESS' -ForegroundColor Green; $client.Close() } catch { Write-Host 'MySQL: FAILED' -ForegroundColor Red }"
echo.

echo 2. 测试 PostgreSQL 端口 (192.168.31.29:5433)...
powershell -Command "$client = New-Object System.Net.Sockets.TcpClient; try { $client.Connect('192.168.31.29', 5433); Write-Host 'PostgreSQL: SUCCESS' -ForegroundColor Green; $client.Close() } catch { Write-Host 'PostgreSQL: FAILED' -ForegroundColor Red }"
echo.

echo 3. 测试 Redis 端口 (192.168.31.29:6379)...
powershell -Command "$client = New-Object System.Net.Sockets.TcpClient; try { $client.Connect('192.168.31.29', 6379); Write-Host 'Redis: SUCCESS' -ForegroundColor Green; $client.Close() } catch { Write-Host 'Redis: FAILED' -ForegroundColor Red }"
echo.

echo ========================================
echo 测试完成
echo ========================================
pause
