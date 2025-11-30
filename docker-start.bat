@echo off
chcp 65001 >nul
echo ========================================
echo    Docker 博客项目启动脚本
echo ========================================
echo.

REM 检查 Docker 是否安装
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到 Docker！
    echo 请先安装 Docker Desktop: https://www.docker.com/products/docker-desktop/
    pause
    exit /b 1
)

echo [✓] Docker 已安装
echo.

REM 检查 Docker 是否运行
docker ps >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] Docker 未运行！
    echo 请启动 Docker Desktop 后重试
    pause
    exit /b 1
)

echo [✓] Docker 正在运行
echo.

echo 正在启动服务...
echo.

REM 启动 Docker Compose
docker-compose up -d

if %errorlevel% neq 0 (
    echo.
    echo [错误] 启动失败！请查看上方错误信息
    pause
    exit /b 1
)

echo.
echo ========================================
echo    启动成功！
echo ========================================
echo.
echo 服务正在启动中，请稍等 2-3 分钟...
echo.
echo 访问地址：
echo   - 前端界面: http://localhost:3000
echo   - 后端 API: http://localhost:4567
echo.
echo 常用命令：
echo   查看状态: docker-compose ps
echo   查看日志: docker-compose logs -f
echo   停止服务: docker-compose stop
echo   重启服务: docker-compose restart
echo.

REM 询问是否查看日志
set /p choice="是否查看实时日志？(y/n): "
if /i "%choice%"=="y" (
    echo.
    echo 按 Ctrl+C 退出日志查看...
    timeout /t 2 >nul
    docker-compose logs -f
)

pause
