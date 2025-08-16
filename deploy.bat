@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

:: 博客系统Windows部署脚本
:: 支持Railway、Render、Vercel等平台

echo.
echo ==========================================
echo           博客系统部署工具
echo ==========================================
echo.

:: 检查必要的工具
echo [STEP] 检查部署环境...

where git >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Git未安装，请先安装Git
    pause
    exit /b 1
)

where node >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Node.js未安装，请先安装Node.js
    pause
    exit /b 1
)

where java >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java未安装，请先安装Java 8或更高版本
    pause
    exit /b 1
)

echo [INFO] 环境检查通过
echo.

:menu
echo ==========================================
echo          请选择部署方案
echo ==========================================
echo 1. Railway部署 (推荐)
echo 2. Render部署
echo 3. Vercel + Railway分离部署
echo 4. 本地构建测试
echo 5. 查看部署状态
echo 6. 退出
echo ==========================================
echo.

set /p choice=请输入选择 (1-6): 

if "%choice%"=="1" goto railway_deploy
if "%choice%"=="2" goto render_deploy
if "%choice%"=="3" goto vercel_railway_deploy
if "%choice%"=="4" goto local_build
if "%choice%"=="5" goto check_status
if "%choice%"=="6" goto exit
echo [ERROR] 无效选择，请重新输入
goto menu

:railway_deploy
echo.
echo [STEP] 开始Railway部署...
echo.

:: 检查Railway CLI
railway --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [WARNING] Railway CLI未安装，正在安装...
    npm install -g @railway/cli
)

:: 登录Railway
echo [INFO] 请登录Railway...
railway login

:: 创建项目
echo [INFO] 创建Railway项目...
railway init

:: 部署后端
echo [INFO] 部署后端服务...
cd backend-spring
railway up

:: 等待后端启动
echo [INFO] 等待后端服务启动...
timeout /t 30 /nobreak >nul

:: 部署前端
echo [INFO] 部署前端应用...
cd ..
echo VITE_API_BASE_URL=https://your-backend-domain.railway.app > .env.production
npm run build:prod
railway up

echo [INFO] Railway部署完成！
pause
goto end

:render_deploy
echo.
echo [STEP] 开始Render部署...
echo.

:: 检查render.yaml是否存在
if not exist "render.yaml" (
    echo [ERROR] render.yaml文件不存在
    pause
    goto menu
)

:: 提交代码
echo [INFO] 提交代码到GitHub...
git add .
git commit -m "Add Render deployment configuration"
git push

echo [INFO] Render部署配置完成！
echo [INFO] 请访问 https://render.com 完成部署：
echo [INFO] 1. 登录Render账号
echo [INFO] 2. 点击'New +' -^> 'Blueprint'
echo [INFO] 3. 选择您的GitHub仓库
echo [INFO] 4. 点击'Apply'开始部署
pause
goto end

:vercel_railway_deploy
echo.
echo [STEP] 开始Vercel + Railway分离部署...
echo.

echo [INFO] 请先完成Railway后端部署，然后：
echo [INFO] 1. 访问 https://vercel.com
echo [INFO] 2. 导入GitHub仓库
echo [INFO] 3. 配置环境变量: VITE_API_BASE_URL=https://your-backend-domain.railway.app
echo [INFO] 4. 点击'Deploy'
pause
goto end

:local_build
echo.
echo [STEP] 开始本地构建测试...
echo.

:: 构建后端
echo [INFO] 构建后端...
cd backend-spring
call mvn clean package -DskipTests

if %errorlevel% equ 0 (
    echo [INFO] 后端构建成功
) else (
    echo [ERROR] 后端构建失败
    pause
    goto menu
)

:: 构建前端
echo [INFO] 构建前端...
cd ..
call npm run build:prod

if %errorlevel% equ 0 (
    echo [INFO] 前端构建成功
) else (
    echo [ERROR] 前端构建失败
    pause
    goto menu
)

echo [INFO] 本地构建测试完成！
pause
goto end

:check_status
echo.
echo [STEP] 检查部署状态...
echo.

:: 检查Railway状态
railway --version >nul 2>&1
if %errorlevel% equ 0 (
    echo [INFO] Railway服务状态：
    railway status
)

:: 检查Git状态
echo [INFO] Git仓库状态：
git status

:: 检查本地构建
if exist "backend-spring\target\backend-0.0.1-SNAPSHOT.jar" (
    echo [INFO] 后端JAR包已构建
) else (
    echo [WARNING] 后端JAR包未构建
)

if exist "dist" (
    echo [INFO] 前端构建目录存在
) else (
    echo [WARNING] 前端构建目录不存在
)

pause
goto menu

:exit
echo [INFO] 退出部署工具
goto end

:end
echo.
echo 部署完成！按任意键退出...
pause >nul
