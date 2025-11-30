@echo off
chcp 65001 >nul
echo ========================================
echo    Docker 博客项目重建脚本
echo ========================================
echo.
echo 此脚本会重新构建所有镜像并启动服务
echo 适用于修改代码后需要更新的情况
echo.

set /p confirm="确认要重建吗？(y/n): "
if /i not "%confirm%"=="y" (
    echo 已取消
    pause
    exit /b 0
)

REM 检查 Docker 是否运行
docker ps >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] Docker 未运行！
    echo 请启动 Docker Desktop 后重试
    pause
    exit /b 1
)

echo.
echo [步骤 1/3] 停止现有服务...
docker-compose stop

echo.
echo [步骤 2/3] 重新构建镜像...
docker-compose build --no-cache

if %errorlevel% neq 0 (
    echo.
    echo [错误] 构建失败！
    pause
    exit /b 1
)

echo.
echo [步骤 3/3] 启动服务...
docker-compose up -d

if %errorlevel% neq 0 (
    echo.
    echo [错误] 启动失败！
    pause
    exit /b 1
)

echo.
echo ========================================
echo    重建成功！
echo ========================================
echo.
echo 访问地址：
echo   - 前端界面: http://localhost:3000
echo   - 后端 API: http://localhost:4567
echo.

pause
