@echo off
chcp 65001 >nul
echo ========================================
echo    Docker 博客项目停止脚本
echo ========================================
echo.

REM 检查 Docker 是否运行
docker ps >nul 2>&1
if %errorlevel% neq 0 (
    echo [提示] Docker 未运行或未安装
    pause
    exit /b 0
)

echo 正在停止所有服务...
echo.

docker-compose stop

if %errorlevel% neq 0 (
    echo.
    echo [错误] 停止失败！
    pause
    exit /b 1
)

echo.
echo ========================================
echo    停止成功！
echo ========================================
echo.
echo 所有服务已停止
echo.
echo 如需重新启动，运行: docker-start.bat
echo 如需删除容器，运行: docker-compose down
echo.

pause
