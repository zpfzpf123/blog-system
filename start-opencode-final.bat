@echo off
chcp 65001 >nul
echo ========================================
echo 启动 OpenCode（完全兼容模式）
echo ========================================

REM 设置所有可能的环境变量
set WEBVIEW2_ADDITIONAL_BROWSER_ARGUMENTS=--use-gl=swiftshader --disable-gpu --no-sandbox
set WEBVIEW2_USER_DATA_FOLDER=%TEMP%\OpenCodeWebView2
set LIBGL_ALWAYS_SOFTWARE=1

echo 正在启动...
start "" "D:\OpenCode\OpenCode.exe"

timeout /t 2 /nobreak >nul
echo.
echo OpenCode 已启动！
echo 如果仍然空白，可能需要：
echo 1. 更新或重装显卡驱动
echo 2. 检查是否有杀毒软件拦截
echo 3. 尝试在另一台电脑上测试
echo ========================================
