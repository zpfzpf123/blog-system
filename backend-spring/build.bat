@echo off
echo 正在打包Spring Boot应用...

:: 使用完整路径检查Maven
"E:\google下载\apache-maven-3.8.5-bin\apache-maven-3.8.5\bin\mvn.cmd" -version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到Maven，请检查Maven安装路径
    pause
    exit /b 1
)

:: 执行Maven打包
"E:\google下载\apache-maven-3.8.5-bin\apache-maven-3.8.5\bin\mvn.cmd" clean package

if %errorlevel% neq 0 (
    echo 打包失败
    pause
    exit /b 1
)

echo.
echo 打包完成! 文件位置: target\backend-0.0.1-SNAPSHOT.jar
echo.
pause