@echo off
chcp 65001 >nul
echo ========================================
echo WebView2 诊断工具
echo ========================================
echo.

echo [检查 1] WebView2 Runtime 版本:
reg query "HKLM\SOFTWARE\WOW6432Node\Microsoft\EdgeUpdate\Clients\{F3017226-FE2A-4295-8BDF-00C3A9A7E4C5}" /v pv 2>nul
echo.

echo [检查 2] 当前 WebView2 配置:
reg query "HKCU\Software\Microsoft\Edge\WebView2" 2>nul
echo.

echo [检查 3] OpenCode 进程状态:
tasklist | findstr /I "OpenCode"
echo.

echo [检查 4] 显卡信息:
wmic path win32_VideoController get name
echo.

echo [检查 5] 系统 DPI 设置:
reg query "HKCU\Control Panel\Desktop" /v LogPixels 2>nul
echo.

echo [检查 6] 兼容性设置:
reg query "HKCU\Software\Microsoft\Windows NT\CurrentVersion\AppCompatFlags\Layers" /v "D:\OpenCode\OpenCode.exe" 2>nul
echo.

echo ========================================
echo 诊断完成
echo ========================================
pause
