@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM AI智能博客系统 - 自动Git上传脚本
REM 功能：检测文件变化，自动提交并推送到GitHub仓库

set "COMMIT_MESSAGE="
set "FORCE_PUSH=false"
set "SHOW_HELP=false"

REM 解析命令行参数
:parse_args
if "%~1"=="" goto :main
if "%~1"=="-m" (
    set "COMMIT_MESSAGE=%~2"
    shift
    shift
    goto :parse_args
)
if "%~1"=="--message" (
    set "COMMIT_MESSAGE=%~2"
    shift
    shift
    goto :parse_args
)
if "%~1"=="-f" (
    set "FORCE_PUSH=true"
    shift
    goto :parse_args
)
if "%~1"=="--force" (
    set "FORCE_PUSH=true"
    shift
    goto :parse_args
)
if "%~1"=="-h" (
    set "SHOW_HELP=true"
    shift
    goto :parse_args
)
if "%~1"=="--help" (
    set "SHOW_HELP=true"
    shift
    goto :parse_args
)
if "%~1"=="-?" (
    set "SHOW_HELP=true"
    shift
    goto :parse_args
)
echo ❌ 未知选项: %~1
goto :show_help

:show_help
echo.
echo 🚀 AI智能博客系统 - 自动Git上传脚本
echo.
echo 用法:
echo     auto-git-upload.bat [选项]
echo.
echo 选项:
echo     -m, --message ^<消息^>    指定提交信息（可选）
echo     -f, --force             强制推送（覆盖远程分支）
echo     -h, --help              显示此帮助信息
echo.
echo 示例:
echo     auto-git-upload.bat
echo     auto-git-upload.bat -m "更新README文档"
echo     auto-git-upload.bat --force
echo.
echo 注意:
echo     - 首次使用前请确保已配置Git用户信息和远程仓库
echo     - 脚本会自动检测所有文件变化并提交
echo     - 使用--force选项时要谨慎，会覆盖远程分支
echo.
pause
exit /b 0

:main
REM 显示帮助信息
if "%SHOW_HELP%"=="true" goto :show_help

echo 🚀 AI智能博客系统 - 自动Git上传脚本
echo ===============================================
echo.

REM 检查Git是否安装
git --version >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误: Git未安装或不在PATH中
    echo 请先安装Git: https://git-scm.com/
    pause
    exit /b 1
)

REM 检查是否为Git仓库
if not exist ".git" (
    echo ❌ 错误: 当前目录不是Git仓库
    echo 请先初始化Git仓库: git init
    pause
    exit /b 1
)

REM 检查远程仓库配置
git remote get-url origin >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误: 未配置远程仓库
    echo 请先添加远程仓库: git remote add origin ^<仓库URL^>
    pause
    exit /b 1
)

REM 获取当前分支
for /f "tokens=*" %%i in ('git branch --show-current 2^>nul') do set "CURRENT_BRANCH=%%i"
if "!CURRENT_BRANCH!"=="" (
    echo ❌ 错误: 无法获取当前分支
    pause
    exit /b 1
)

echo 📍 当前分支: !CURRENT_BRANCH!
echo.

REM 获取Git状态
git status --porcelain > temp_git_status.txt 2>nul
if errorlevel 1 (
    echo ❌ 错误: 无法获取Git状态
    pause
    exit /b 1
)

REM 检查是否有文件变化
set "HAS_CHANGES=false"
for /f "tokens=*" %%i in (temp_git_status.txt) do (
    set "HAS_CHANGES=true"
    goto :check_changes_done
)
:check_changes_done

if "!HAS_CHANGES!"=="false" (
    echo ✅ 没有文件变化，无需提交
    del temp_git_status.txt 2>nul
    pause
    exit /b 0
)

echo 📝 检测到以下文件变化:
echo.
for /f "tokens=*" %%i in (temp_git_status.txt) do (
    set "line=%%i"
    set "status=!line:~0,2!"
    set "filename=!line:~3!"
    
    if "!status!"=="M" set "status_icon=📝"
    if "!status!"=="A" set "status_icon=➕"
    if "!status!"=="D" set "status_icon=🗑️"
    if "!status!"=="R" set "status_icon=🔄"
    if "!status!"=="C" set "status_icon=📋"
    if "!status_icon!"=="" set "status_icon=❓"
    
    echo   !status_icon! !filename!
)
echo.

REM 添加所有文件到暂存区
echo 📦 添加文件到暂存区...
git add . >nul 2>&1
if errorlevel 1 (
    echo ❌ 添加文件失败
    del temp_git_status.txt 2>nul
    pause
    exit /b 1
)
echo ✅ 文件已添加到暂存区
echo.

REM 生成提交信息
if "!COMMIT_MESSAGE!"=="" (
    for /f "tokens=1-3 delims=/ " %%a in ('date /t') do set "date_part=%%a"
    for /f "tokens=1-2 delims=: " %%a in ('time /t') do set "time_part=%%a"
    set "COMMIT_MESSAGE=自动更新 - !date_part! !time_part!"
)

echo 💬 提交信息: !COMMIT_MESSAGE!
echo.

REM 提交更改
echo 📝 提交更改...
git commit -m "!COMMIT_MESSAGE!" >nul 2>&1
if errorlevel 1 (
    echo ❌ 提交失败
    del temp_git_status.txt 2>nul
    pause
    exit /b 1
)
echo ✅ 更改已提交
echo.

REM 获取最新提交信息
for /f "tokens=*" %%i in ('git log -1 --oneline 2^>nul') do set "LAST_COMMIT=%%i"
if not "!LAST_COMMIT!"=="" (
    echo 📋 最新提交: !LAST_COMMIT!
    echo.
)

REM 推送到远程仓库
echo 🚀 推送到远程仓库...
if "!FORCE_PUSH!"=="true" (
    echo ⚠️ 使用强制推送模式
    git push origin "!CURRENT_BRANCH!" --force >nul 2>&1
    if errorlevel 1 (
        echo ❌ 推送失败
        echo 💡 提示: 请检查网络连接和权限
        del temp_git_status.txt 2>nul
        pause
        exit /b 1
    )
) else (
    git push origin "!CURRENT_BRANCH!" >nul 2>&1
    if errorlevel 1 (
        echo ❌ 推送失败
        echo 💡 提示: 如果远程有更新，请先执行 git pull
        del temp_git_status.txt 2>nul
        pause
        exit /b 1
    )
)
echo ✅ 成功推送到远程仓库
echo.

REM 显示推送结果
for /f "tokens=*" %%i in ('git remote get-url origin 2^>nul') do set "REMOTE_URL=%%i"
if not "!REMOTE_URL!"=="" (
    set "REMOTE_URL=!REMOTE_URL:.git=!"
    echo 🌐 远程仓库: !REMOTE_URL!
    echo 🌿 分支: !CURRENT_BRANCH!
    echo.
)

echo 🎉 自动上传完成！
echo ===============================================
echo.

REM 清理临时文件
del temp_git_status.txt 2>nul

pause
exit /b 0
