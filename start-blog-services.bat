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

echo.
echo 开始启动服务...
echo.

:: 第一步：启动Ollama服务
echo [%time%] 正在启动Ollama服务...
echo 检查Ollama是否已安装...
ollama --version >nul 2>&1
if errorlevel 1 (
    echo 错误: 未找到Ollama，请确保已安装Ollama
    echo 可以运行 install-ollama.bat 来安装Ollama
    pause
    exit /b 1
) else (
    echo Ollama已安装，版本信息:
    ollama --version
)

echo [%time%] 启动Ollama服务...
start "Ollama服务" cmd /k "echo 启动Ollama服务... && ollama serve"

:: 等待Ollama服务启动
echo [%time%] 等待Ollama服务启动...
timeout /t 10 /nobreak >nul

:: 检查Ollama服务是否启动成功
echo [%time%] 检查Ollama服务状态...
timeout /t 3 /nobreak >nul
ollama list >nul 2>&1
if errorlevel 1 (
    echo 警告: Ollama服务可能未完全启动，继续启动其他服务...
    echo 如果AI功能有问题，请检查Ollama服务状态
) else (
    echo Ollama服务启动成功！
)

echo.
echo ========================================
echo Ollama服务启动完成，开始启动其他服务...
echo ========================================
echo.

:: 第二步：启动后端服务
echo [%time%] 正在启动后端服务...
cd /d "%BACKEND_PATH%"
start "博客后端服务" cmd /k "echo 启动Spring Boot后端服务... && mvn spring-boot:run"

:: 等待后端服务启动
echo [%time%] 等待后端服务启动...
timeout /t 15 /nobreak >nul

:: 第三步：启动前端服务
echo [%time%] 正在启动前端服务...
cd /d "%FRONTEND_PATH%"
start "博客前端服务" cmd /k "echo 启动Vue.js前端服务... && npm run dev"

echo.
echo ========================================
echo 所有服务启动完成！
echo ========================================
echo Ollama服务: 已启动 (AI模型服务)
echo 后端服务: http://localhost:4567
echo 前端服务: http://localhost:5173
echo 管理后台: http://localhost:5173/admin
echo.
echo 提示:
echo - Ollama服务窗口标题: "Ollama服务"
echo - 后端服务窗口标题: "博客后端服务"
echo - 前端服务窗口标题: "博客前端服务"
echo - 如需停止服务，请关闭对应的命令行窗口
echo - 如果AI功能异常，请检查Ollama服务状态
echo ========================================

:: 等待用户确认
echo.
echo 按任意键关闭此窗口...
pause >nul
