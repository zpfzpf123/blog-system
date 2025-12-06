@echo off
chcp 65001 >nul
echo ================================
echo 数据库快速修复工具
echo ================================
echo.

echo [1/3] 检查数据库连接...
mysql -u root -p990328 -e "USE blogdb; SELECT 'Database connection OK' as status;" 2>nul
if %errorlevel% neq 0 (
    echo [错误] 无法连接到数据库，请检查MySQL是否启动
    pause
    exit /b 1
)
echo [√] 数据库连接正常

echo.
echo [2/3] 执行表结构修复...
mysql -u root -p990328 blogdb < fix-posts-table.sql
if %errorlevel% neq 0 (
    echo [错误] 表结构修复失败
    pause
    exit /b 1
)
echo [√] 表结构修复完成

echo.
echo [3/3] 验证修复结果...
mysql -u root -p990328 -e "USE blogdb; SHOW COLUMNS FROM posts WHERE Field='content';"
echo.

echo ================================
echo 修复完成！现在可以重启后端服务
echo ================================
pause
