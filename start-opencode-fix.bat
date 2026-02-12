@echo off
echo 正在启动 OpenCode（兼容模式）...

REM 设置环境变量强制使用软件渲染
set WEBVIEW2_ADDITIONAL_BROWSER_ARGUMENTS=--disable-gpu --disable-software-rasterizer --disable-gpu-compositing --no-sandbox --disable-features=RendererCodeIntegrity --disable-dev-shm-usage --force-device-scale-factor=1 --disable-accelerated-2d-canvas --disable-accelerated-video-decode

REM 启动 OpenCode
start "" "D:\OpenCode\OpenCode.exe"

echo OpenCode 已启动
timeout /t 2 /nobreak >nul
