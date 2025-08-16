@echo off
chcp 65001 >nul
title 安装博客服务启动脚本

echo ========================================
echo 博客服务启动脚本安装程序
echo ========================================

:: 获取当前脚本所在目录
set "SCRIPT_DIR=%~dp0"
set "STARTUP_FOLDER=%APPDATA%\Microsoft\Windows\Start Menu\Programs\Startup"

echo 当前目录: %SCRIPT_DIR%
echo 启动文件夹: %STARTUP_FOLDER%
echo.

:: 检查启动文件夹是否存在
if not exist "%STARTUP_FOLDER%" (
    echo 错误: 启动文件夹不存在
    echo 路径: %STARTUP_FOLDER%
    pause
    exit /b 1
)

:: 复制批处理脚本
echo Copying batch startup script...
copy "%SCRIPT_DIR%start-blog-services.bat" "%STARTUP_FOLDER%\start-blog-services.bat" >nul
if errorlevel 1 (
    echo Error: Failed to copy batch script
    pause
    exit /b 1
) else (
    echo ✓ Batch script copied successfully
)

:: 复制PowerShell脚本
echo Copying PowerShell startup script...
copy "%SCRIPT_DIR%start-blog-services.ps1" "%STARTUP_FOLDER%\start-blog-services.ps1" >nul
if errorlevel 1 (
    echo Error: Failed to copy PowerShell script
    pause
    exit /b 1
) else (
    echo ✓ PowerShell script copied successfully
)

:: 创建PowerShell执行策略设置脚本
echo Creating PowerShell execution policy setup script...
(
echo @echo off
echo echo 设置PowerShell执行策略...
echo powershell -Command "Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser -Force"
echo echo 执行策略设置完成
echo pause
) > "%STARTUP_FOLDER%\setup-powershell-policy.bat"

echo ✓ PowerShell execution policy setup script created successfully

echo.
echo ========================================
echo 安装完成！
echo ========================================
echo 已安装的脚本:
echo - start-blog-services.bat ^(批处理版本^)
echo - start-blog-services.ps1 ^(PowerShell版本^)
echo - setup-powershell-policy.bat ^(PowerShell策略设置^)
echo.
echo 启动文件夹: %STARTUP_FOLDER%
echo.
echo 使用说明:
echo 1. 首次使用PowerShell脚本前，请运行 setup-powershell-policy.bat
echo 2. 系统启动时会自动运行 start-blog-services.bat
echo 3. 如需使用PowerShell版本，请手动运行 start-blog-services.ps1
echo.
echo 提示: 您可以在启动文件夹中删除不需要的脚本
echo ========================================

:: 询问是否立即测试脚本
echo.
set /p "test=是否立即测试启动脚本？(y/n): "
if /i "%test%"=="y" (
    echo.
    echo 正在测试启动脚本...
    call "%SCRIPT_DIR%start-blog-services.bat"
)

echo.
echo 按任意键退出...
pause >nul
