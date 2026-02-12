@echo off
echo ========================================
echo WebView2 完全修复脚本
echo ========================================
echo.

echo [1/5] 停止所有 OpenCode 进程...
taskkill /F /IM OpenCode.exe 2>nul
timeout /t 1 /nobreak >nul

echo [2/5] 清理 WebView2 缓存...
rd /s /q "%LOCALAPPDATA%\Microsoft\Edge\User Data" 2>nul
rd /s /q "%LOCALAPPDATA%\EBWebView" 2>nul
rd /s /q "%APPDATA%\OpenCode" 2>nul
rd /s /q "%LOCALAPPDATA%\OpenCode" 2>nul

echo [3/5] 配置 WebView2 注册表...
reg add "HKCU\Software\Microsoft\Edge\WebView2" /v "AdditionalBrowserArguments" /t REG_SZ /d "--disable-gpu --disable-software-rasterizer --disable-gpu-compositing --no-sandbox --disable-features=RendererCodeIntegrity --disable-dev-shm-usage --force-device-scale-factor=1 --disable-accelerated-2d-canvas --disable-accelerated-video-decode --use-angle=swiftshader" /f >nul

echo [4/5] 配置兼容性设置...
reg add "HKCU\Software\Microsoft\Windows NT\CurrentVersion\AppCompatFlags\Layers" /v "D:\OpenCode\OpenCode.exe" /t REG_SZ /d "~ HIGHDPIAWARE" /f >nul
reg add "HKCU\Software\Microsoft\Avalon.Graphics" /v DisableHWAcceleration /t REG_DWORD /d 1 /f >nul

echo [5/5] 启动 OpenCode...
set WEBVIEW2_ADDITIONAL_BROWSER_ARGUMENTS=--disable-gpu --no-sandbox
start "" "D:\OpenCode\OpenCode.exe"

echo.
echo ========================================
echo 修复完成！OpenCode 已启动
echo 如果仍然空白，请尝试：
echo 1. 更新显卡驱动
echo 2. 以管理员身份运行此脚本
echo 3. 重启电脑后再试
echo ========================================
timeout /t 3 /nobreak >nul
