# 博客服务自动启动脚本
param(
    [string]$ProjectPath = "E:\ai博客\blog",
    [int]$BackendPort = 4567,
    [int]$FrontendPort = 5173
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "博客服务自动启动脚本" -ForegroundColor Cyan
Write-Host "启动时间: $(Get-Date)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 检查项目路径
if (-not (Test-Path $ProjectPath)) {
    Write-Host "错误: 项目路径不存在" -ForegroundColor Red
    exit 1
}

$BackendPath = Join-Path $ProjectPath "backend-spring"
$FrontendPath = $ProjectPath

# 检查环境
$deps = @("java", "node", "mvn", "npm")
foreach ($dep in $deps) {
    if (-not (Get-Command $dep -ErrorAction SilentlyContinue)) {
        Write-Host "错误: $dep 未找到" -ForegroundColor Red
        exit 1
    }
}

# 启动后端服务
Write-Host "启动后端服务..." -ForegroundColor Yellow
$backendJob = Start-Job -ScriptBlock {
    param($path)
    Set-Location $path
    mvn spring-boot:run
} -ArgumentList $BackendPath -Name "博客后端服务"

# 等待后端启动
Start-Sleep -Seconds 15

# 启动前端服务
Write-Host "启动前端服务..." -ForegroundColor Yellow
$frontendJob = Start-Job -ScriptBlock {
    param($path)
    Set-Location $path
    npm run dev
} -ArgumentList $FrontendPath -Name "博客前端服务"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "服务启动完成！" -ForegroundColor Green
Write-Host "后端: http://localhost:$BackendPort" -ForegroundColor White
Write-Host "前端: http://localhost:$FrontendPort" -ForegroundColor White
Write-Host "管理: http://localhost:$FrontendPort/admin" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Cyan

Get-Job | Where-Object { $_.Name -like "博客*服务" } | Format-Table Name, State
