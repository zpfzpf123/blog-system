@echo off
 echo Starting Spring Boot application...

:: Check Java installation
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Java not found. Please check Java installation path
    pause
    exit /b 1
)

:: Start Spring Boot application
java -jar target\backend-0.0.1-SNAPSHOT.jar

if %errorlevel% neq 0 (
    echo Failed to start application
    pause
    exit /b 1
)

pause