@echo off
title Blog Application Auto Start

:: 设置工作目录
cd /d "%~dp0"

:: 检查Node.js是否安装
where node >nul 2>nul
if %errorlevel% neq 0 (
    echo 错误: 未找到Node.js，请先安装Node.js
    pause
    exit /b 1
)

:: 检查Java是否安装
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo 错误: 未找到Java，请先安装Java JDK
    pause
    exit /b 1
)

:: 检查Maven是否安装
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo 错误: 未找到Maven，请先安装Maven
    pause
    exit /b 1
)

echo 正在启动博客系统...
echo.

:: 创建日志目录
if not exist "logs" mkdir logs

:: 启动后端服务（Spring Boot）
echo 启动后端服务...
cd backend-spring
start "Blog Backend" /min cmd /k "mvn spring-boot:run > ..\logs\backend.log 2>&1"
cd ..

:: 等待后端服务启动
echo 等待后端服务启动...
timeout /t 15 /nobreak >nul

:: 启动前端开发服务器
echo 启动前端服务...
start "Blog Frontend" /min cmd /k "npm run dev > logs\frontend.log 2>&1"

:: 等待前端服务启动
echo 等待前端服务启动...
timeout /t 10 /nobreak >nul

:: 打开浏览器访问应用
echo 打开浏览器...
start http://localhost:5173

echo.
echo ========================================
echo 博客系统启动完成！
echo ========================================
echo 前端地址: http://localhost:5173
echo 后端地址: http://localhost:3000
echo 日志文件位置: logs\
echo ========================================
echo.
echo 服务正在后台运行，关闭此窗口不会停止服务。
echo 如需停止服务，请关闭对应的命令行窗口。
echo.
pause
