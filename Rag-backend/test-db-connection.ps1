# 数据库连通性测试脚本

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "数据库连通性测试" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 配置信息
$mysqlHost = "192.168.31.29"
$mysqlPort = "3306"
$mysqlUser = "root"
$mysqlPassword = "625625mahao"

$postgresHost = "192.168.31.29"
$postgresPort = "5433"
$postgresUser = "postgres"
$postgresPassword = "625625mahao"

$redisHost = "192.168.31.29"
$redisPort = "6379"
$redisPassword = "625625mahao"

# 测试 MySQL 连通性
Write-Host "1. 测试 MySQL 连通性..." -ForegroundColor Yellow
try {
    # 使用 TCP 连接测试
    $tcpClient = New-Object System.Net.Sockets.TcpClient
    $timeout = $tcpClient.BeginConnect($mysqlHost, $mysqlPort, $null, $null)
    $wait = $timeout.AsyncWaitHandle.WaitOne(3000)
    
    if ($wait -and $tcpClient.Connected) {
        Write-Host "   ✓ MySQL ($mysqlHost:$mysqlPort) 连通成功" -ForegroundColor Green
        $tcpClient.Close()
    } else {
        Write-Host "   ✗ MySQL ($mysqlHost:$mysqlPort) 连接超时或失败" -ForegroundColor Red
    }
} catch {
    Write-Host "   ✗ MySQL 测试失败：$($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 测试 PostgreSQL 连通性
Write-Host "2. 测试 PostgreSQL 连通性..." -ForegroundColor Yellow
try {
    $tcpClient = New-Object System.Net.Sockets.TcpClient
    $timeout = $tcpClient.BeginConnect($postgresHost, $postgresPort, $null, $null)
    $wait = $timeout.AsyncWaitHandle.WaitOne(3000)
    
    if ($wait -and $tcpClient.Connected) {
        Write-Host "   ✓ PostgreSQL ($postgresHost:$postgresPort) 连通成功" -ForegroundColor Green
        $tcpClient.Close()
    } else {
        Write-Host "   ✗ PostgreSQL ($postgresHost:$postgresPort) 连接超时或失败" -ForegroundColor Red
    }
} catch {
    Write-Host "   ✗ PostgreSQL 测试失败：$($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 测试 Redis 连通性
Write-Host "3. 测试 Redis 连通性..." -ForegroundColor Yellow
try {
    $tcpClient = New-Object System.Net.Sockets.TcpClient
    $timeout = $tcpClient.BeginConnect($redisHost, $redisPort, $null, $null)
    $wait = $timeout.AsyncWaitHandle.WaitOne(3000)
    
    if ($wait -and $tcpClient.Connected) {
        Write-Host "   ✓ Redis ($redisHost:$redisPort) 连通成功" -ForegroundColor Green
        $tcpClient.Close()
    } else {
        Write-Host "   ✗ Redis ($redisHost:$redisPort) 连接超时或失败" -ForegroundColor Red
    }
} catch {
    Write-Host "   ✗ Redis 测试失败：$($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "测试完成" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
