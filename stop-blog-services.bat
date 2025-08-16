@echo off
chcp 65001 >nul
title 停止博客服务

echo ========================================
echo 停止博客服务脚本
echo ========================================

:: 停止后端服务 (查找包含mvn spring-boot:run的进程)
echo 正在停止后端服务...
taskkill /f /im java.exe /fi "WINDOWTITLE eq 博客后端服务*" >nul 2>&1
taskkill /f /im java.exe /fi "WINDOWTITLE eq *mvn*" >nul 2>&1

:: 停止前端服务 (查找包含npm run dev的进程)
echo 正在停止前端服务...
taskkill /f /im node.exe /fi "WINDOWTITLE eq 博客前端服务*" >nul 2>&1
taskkill /f /im node.exe /fi "WINDOWTITLE eq *npm*" >nul 2>&1

:: 停止PowerShell后台任务
echo 正在停止PowerShell后台任务...
powershell -Command "Get-Job | Where-Object { $_.Name -like '博客*服务' } | Stop-Job; Get-Job | Where-Object { $_.Name -like '博客*服务' } | Remove-Job" >nul 2>&1

echo.
echo ========================================
echo 服务停止完成！
echo ========================================
echo 已停止的服务:
echo - Spring Boot 后端服务
echo - Vue.js 前端服务
echo - PowerShell 后台任务
echo ========================================

echo.
echo 按任意键退出...
pause >nul
