@echo off
chcp 65001 >nul
echo ====================================
echo 重新编译并重启后端服务
echo ====================================
echo.

echo [1/4] 停止现有的Java进程...
taskkill /F /IM java.exe 2>nul
timeout /t 2 /nobreak >nul

echo.
echo [2/4] 清理并重新编译项目...
call "E:\google下载\apache-maven-3.8.5-bin\apache-maven-3.8.5\bin\mvn.cmd" clean package -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [错误] Maven 编译失败！
    pause
    exit /b 1
)

echo.
echo [3/4] 编译成功，准备启动服务...
timeout /t 2 /nobreak >nul

echo.
echo [4/4] 启动后端服务...
start "Blog Backend" java -jar target\backend-0.0.1-SNAPSHOT.jar

echo.
echo ====================================
echo 后端服务已启动！
echo 服务地址: http://localhost:4567
echo ====================================
echo.
pause
