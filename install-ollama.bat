@echo off
echo ========================================
echo Ollama 安装和配置脚本
echo ========================================
echo.

echo 正在检查Ollama是否已安装...
ollama --version >nul 2>&1
if %errorlevel% == 0 (
    echo Ollama 已安装，版本信息：
    ollama --version
    echo.
    goto :check_service
) else (
    echo Ollama 未安装，正在下载安装包...
    goto :download_ollama
)

:download_ollama
echo 正在下载 Ollama Windows 安装包...
echo 请访问: https://ollama.ai/download
echo 下载完成后，请运行安装程序
echo.
pause
echo.
echo 安装完成后，请重新运行此脚本
goto :end

:check_service
echo 检查 Ollama 服务状态...
netstat -an | findstr :11434 >nul
if %errorlevel% == 0 (
    echo Ollama 服务正在运行 (端口 11434)
) else (
    echo Ollama 服务未运行，正在启动...
    start "" ollama serve
    timeout /t 5 /nobreak >nul
)

echo.
echo 正在下载推荐的中文模型...
echo 下载 qwen 模型 (推荐中文使用)...
ollama pull qwen
if %errorlevel% == 0 (
    echo qwen 模型下载成功！
) else (
    echo qwen 模型下载失败，尝试下载 llama2...
    ollama pull llama2
)

echo.
echo 正在测试模型...
echo 测试提示: "你好，请用中文回答"
ollama run qwen "你好，请用中文回答" 2>nul
if %errorlevel% == 0 (
    echo 模型测试成功！
) else (
    echo 模型测试失败，请检查服务状态
)

echo.
echo ========================================
echo 配置完成！
echo ========================================
echo.
echo 在博客系统中配置 Ollama：
echo 1. 服务提供商: 选择 "Ollama (本地)"
echo 2. 服务地址: http://localhost:11434
echo 3. 模型名称: qwen 或 llama2
echo.
echo 如果遇到问题，请检查：
echo - Ollama 服务是否正在运行
echo - 端口 11434 是否被占用
echo - 模型是否下载成功
echo.

:end
pause
