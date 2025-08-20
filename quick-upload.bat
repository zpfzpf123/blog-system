@echo off
chcp 65001 >nul
title 🚀 AI智能博客系统 - 快速上传工具

echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                    🚀 AI智能博客系统                        ║
echo ║                    快速上传工具                              ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

:menu
echo 📋 请选择要执行的操作：
echo.
echo [1] 🚀 自动上传（推荐）
echo [2] 📝 指定提交信息上传
echo [3] ⚠️  强制推送上传
echo [4] 📚 查看帮助信息
echo [5] 🔧 检查Git配置
echo [6] ❌ 退出程序
echo.
set /p choice="请输入选项 (1-6): "

if "%choice%"=="1" goto :auto_upload
if "%choice%"=="2" goto :custom_message
if "%choice%"=="3" goto :force_push
if "%choice%"=="4" goto :show_help
if "%choice%"=="5" goto :check_git
if "%choice%"=="6" goto :exit
goto :invalid_choice

:auto_upload
echo.
echo 🚀 开始自动上传...
echo.
call auto-git-upload.bat
echo.
pause
goto :menu

:custom_message
echo.
set /p message="请输入提交信息: "
if "%message%"=="" (
    echo ❌ 提交信息不能为空！
    pause
    goto :menu
)
echo.
echo 📝 使用自定义提交信息上传...
echo.
call auto-git-upload.bat -m "%message%"
echo.
pause
goto :menu

:force_push
echo.
echo ⚠️  警告：强制推送将覆盖远程分支！
echo.
set /p confirm="确定要继续吗？(y/N): "
if /i "%confirm%"=="y" (
    echo.
    echo 🚀 开始强制推送...
    echo.
    call auto-git-upload.bat --force
) else (
    echo.
    echo ✅ 已取消强制推送
)
echo.
pause
goto :menu

:show_help
echo.
echo 📚 帮助信息：
echo.
echo 自动上传脚本支持以下功能：
echo.
echo • 自动检测文件变化
echo • 智能生成提交信息
echo • 自动推送到远程仓库
echo • 彩色输出和状态显示
echo • 安全检查（Git安装、仓库状态等）
echo • 详细的执行过程反馈
echo.
echo 使用方法：
echo • 选项1：自动检测变化并上传
echo • 选项2：使用自定义提交信息
echo • 选项3：强制推送（覆盖远程分支）
echo • 选项4：查看详细帮助
echo • 选项5：检查Git配置状态
echo.
pause
goto :menu

:check_git
echo.
echo 🔧 检查Git配置状态...
echo.

echo 📍 检查Git安装...
git --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Git未安装或不在PATH中
) else (
    echo ✅ Git已安装
    for /f "tokens=*" %%i in ('git --version') do echo   版本: %%i
)

echo.
echo 📍 检查Git仓库状态...
if exist ".git" (
    echo ✅ 当前目录是Git仓库
) else (
    echo ❌ 当前目录不是Git仓库
    goto :check_done
)

echo.
echo 📍 检查远程仓库配置...
git remote get-url origin >nul 2>&1
if errorlevel 1 (
    echo ❌ 未配置远程仓库
) else (
    echo ✅ 已配置远程仓库
    for /f "tokens=*" %%i in ('git remote get-url origin') do echo   地址: %%i
)

echo.
echo 📍 检查当前分支...
for /f "tokens=*" %%i in ('git branch --show-current 2^>nul') do set "current_branch=%%i"
if "!current_branch!"=="" (
    echo ❌ 无法获取当前分支
) else (
    echo ✅ 当前分支: !current_branch!
)

echo.
echo 📍 检查Git用户配置...
for /f "tokens=*" %%i in ('git config --global user.name 2^>nul') do set "git_user=%%i"
for /f "tokens=*" %%i in ('git config --global user.email 2^>nul') do set "git_email=%%i"

if "!git_user!"=="" (
    echo ❌ 未配置Git用户名
) else (
    echo ✅ Git用户名: !git_user!
)

if "!git_email!"=="" (
    echo ❌ 未配置Git邮箱
) else (
    echo ✅ Git邮箱: !git_email!
)

:check_done
echo.
pause
goto :menu

:invalid_choice
echo.
echo ❌ 无效选项，请输入1-6之间的数字
echo.
pause
goto :menu

:exit
echo.
echo 👋 感谢使用AI智能博客系统快速上传工具！
echo.
pause
exit /b 0
