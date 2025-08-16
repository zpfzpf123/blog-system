@echo off
chcp 65001 >nul
title 博客服务自动启动脚本

echo ========================================
echo 博客服务自动启动脚本
echo 启动时间: %date% %time%
echo ========================================

:: 设置项目路径
set PROJECT_PATH=E:\ai博客\blog
set BACKEND_PATH=%PROJECT_PATH%\backend-spring
set FRONTEND_PATH=%PROJECT_PATH%

:: 检查项目路径是否存在
if not exist "%PROJECT_PATH%" (
    echo 错误: 项目路径不存在 - %PROJECT_PATH%
    echo 请检查项目路径是否正确
    pause
    exit /b 1
)

echo 项目路径: %PROJECT_PATH%
echo 后端路径: %BACKEND_PATH%
echo 前端路径: %FRONTEND_PATH%
echo.

:: 检查Java环境
echo 检查Java环境...
java -version >nul 2>&1
if errorlevel 1 (
    echo 错误: 未找到Java环境，请确保已安装Java并配置环境变量
    pause
    exit /b 1
) else (
    echo Java环境检查通过
)

:: 检查Node.js环境
echo 检查Node.js环境...
node --version >nul 2>&1
if errorlevel 1 (
    echo 错误: 未找到Node.js环境，请确保已安装Node.js并配置环境变量
    pause
    exit /b 1
) else (
    echo Node.js环境检查通过
)

:: 检查Maven环境
echo 检查Maven环境...
mvn --version >nul 2>&1
if errorlevel 1 (
    echo 错误: 未找到Maven环境，请确保已安装Maven并配置环境变量
    pause
    exit /b 1
) else (
    echo Maven环境检查通过
)

echo.
echo 开始启动服务...
echo.

:: 启动后端服务
echo [%time%] 正在启动后端服务...
cd /d "%BACKEND_PATH%"
start "博客后端服务" cmd /k "echo 启动Spring Boot后端服务... && mvn spring-boot:run"

:: 等待后端服务启动
echo [%time%] 等待后端服务启动...
timeout /t 15 /nobreak >nul

:: 启动前端服务
echo [%time%] 正在启动前端服务...
cd /d "%FRONTEND_PATH%"
start "博客前端服务" cmd /k "echo 启动Vue.js前端服务... && npm run dev"

echo.
echo ========================================
echo 服务启动完成！
echo ========================================
echo 后端服务: http://localhost:4567
echo 前端服务: http://localhost:5173
echo 管理后台: http://localhost:5173/admin
echo.
echo 提示:
echo - 后端服务窗口标题: "博客后端服务"
echo - 前端服务窗口标题: "博客前端服务"
echo - 如需停止服务，请关闭对应的命令行窗口
echo ========================================

:: 等待用户确认
echo.
echo 按任意键关闭此窗口...
pause >nul
