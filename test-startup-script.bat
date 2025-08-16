@echo off
chcp 65001 >nul
title 测试博客服务启动脚本

echo ========================================
echo 测试博客服务启动脚本
echo ========================================

echo 正在测试启动脚本...
echo.

:: 测试批处理脚本
echo 1. 测试批处理启动脚本...
if exist "start-blog-services.bat" (
    echo ✓ 批处理脚本存在
) else (
    echo ✗ 批处理脚本不存在
)

:: 测试PowerShell脚本
echo 2. 测试PowerShell启动脚本...
if exist "start-blog-services.ps1" (
    echo ✓ PowerShell脚本存在
) else (
    echo ✗ PowerShell脚本不存在
)

:: 测试停止脚本
echo 3. 测试停止脚本...
if exist "stop-blog-services.bat" (
    echo ✓ 停止脚本存在
) else (
    echo ✗ 停止脚本不存在
)

:: 检查环境
echo.
echo 4. 检查环境依赖...
java -version >nul 2>&1
if errorlevel 1 (
    echo ✗ Java未安装或未配置环境变量
) else (
    echo ✓ Java环境正常
)

node --version >nul 2>&1
if errorlevel 1 (
    echo ✗ Node.js未安装或未配置环境变量
) else (
    echo ✓ Node.js环境正常
)

mvn --version >nul 2>&1
if errorlevel 1 (
    echo ✗ Maven未安装或未配置环境变量
) else (
    echo ✓ Maven环境正常
)

:: 检查项目路径
echo.
echo 5. 检查项目路径...
if exist "E:\ai博客\blog" (
    echo ✓ 项目路径存在
) else (
    echo ✗ 项目路径不存在
)

if exist "E:\ai博客\blog\backend-spring" (
    echo ✓ 后端路径存在
) else (
    echo ✗ 后端路径不存在
)

if exist "E:\ai博客\blog\package.json" (
    echo ✓ 前端项目存在
) else (
    echo ✗ 前端项目不存在
)

echo.
echo ========================================
echo 测试完成！
echo ========================================
echo.
echo 如果所有检查都通过，您可以：
echo 1. 运行 start-blog-services.bat 启动服务
echo 2. 运行 stop-blog-services.bat 停止服务
echo 3. 重启系统测试自动启动功能
echo.
echo 按任意键退出...
pause >nul
