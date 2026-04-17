Write-Host "Testing MySQL connection to 192.168.31.29:3306..."
$mysqlTest = New-Object System.Net.Sockets.TcpClient
try {
    $mysqlTest.Connect("192.168.31.29", 3306)
    Write-Host "SUCCESS: MySQL is reachable" -ForegroundColor Green
    $mysqlTest.Close()
} catch {
    Write-Host "FAILED: MySQL connection error - $_" -ForegroundColor Red
}

Write-Host "`nTesting PostgreSQL connection to 192.168.31.29:5433..."
$postgresTest = New-Object System.Net.Sockets.TcpClient
try {
    $postgresTest.Connect("192.168.31.29", 5433)
    Write-Host "SUCCESS: PostgreSQL is reachable" -ForegroundColor Green
    $postgresTest.Close()
} catch {
    Write-Host "FAILED: PostgreSQL connection error - $_" -ForegroundColor Red
}

Write-Host "`nTesting Redis connection to 192.168.31.29:6379..."
$redisTest = New-Object System.Net.Sockets.TcpClient
try {
    $redisTest.Connect("192.168.31.29", 6379)
    Write-Host "SUCCESS: Redis is reachable" -ForegroundColor Green
    $redisTest.Close()
} catch {
    Write-Host "FAILED: Redis connection error - $_" -ForegroundColor Red
}

Write-Host "`nAll tests completed!"
