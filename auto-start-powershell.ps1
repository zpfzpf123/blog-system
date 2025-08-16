# 博客系统自动启动脚本 (PowerShell版本)
# 设置执行策略（如果需要）
# Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser

param(
    [switch]$Silent,
    [switch]$NoBrowser
)

# 设置控制台标题
$Host.UI.RawUI.WindowTitle = "Blog Application Auto Start"

# 获取脚本所在目录
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ScriptDir

Write-Host "正在启动博客系统..." -ForegroundColor Green
Write-Host ""

# 检查必要的工具是否安装
$tools = @{
    "Node.js" = "node"
    "Java" = "java"
    "Maven" = "mvn"
}

foreach ($tool in $tools.GetEnumerator()) {
    try {
        $null = Get-Command $tool.Value -ErrorAction Stop
        Write-Host "✓ $($tool.Key) 已安装" -ForegroundColor Green
    }
    catch {
        Write-Host "✗ 错误: 未找到 $($tool.Key)，请先安装 $($tool.Key)" -ForegroundColor Red
        if (-not $Silent) {
            Read-Host "按任意键退出"
        }
        exit 1
    }
}

# 创建日志目录
if (-not (Test-Path "logs")) {
    New-Item -ItemType Directory -Path "logs" | Out-Null
}

# 启动后端服务
Write-Host "启动后端服务..." -ForegroundColor Yellow
$backendJob = Start-Job -ScriptBlock {
    Set-Location $using:ScriptDir\backend-spring
    mvn spring-boot:run
} -Name "BlogBackend"

# 等待后端服务启动
Write-Host "等待后端服务启动..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

# 启动前端服务
Write-Host "启动前端服务..." -ForegroundColor Yellow
$frontendJob = Start-Job -ScriptBlock {
    Set-Location $using:ScriptDir
    npm run dev
} -Name "BlogFrontend"

# 等待前端服务启动
Write-Host "等待前端服务启动..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

# 打开浏览器（除非指定不打开）
if (-not $NoBrowser) {
    Write-Host "打开浏览器..." -ForegroundColor Yellow
    Start-Process "http://localhost:5173"
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "博客系统启动完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "前端地址: http://localhost:5173" -ForegroundColor White
Write-Host "后端地址: http://localhost:3000" -ForegroundColor White
Write-Host "日志文件位置: logs\" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "服务正在后台运行。" -ForegroundColor Yellow
Write-Host "使用以下命令查看运行状态:" -ForegroundColor Yellow
Write-Host "  Get-Job" -ForegroundColor Gray
Write-Host "使用以下命令停止服务:" -ForegroundColor Yellow
Write-Host "  Stop-Job -Name 'BlogBackend', 'BlogFrontend'" -ForegroundColor Gray
Write-Host ""

if (-not $Silent) {
    Read-Host "按任意键退出"
}
