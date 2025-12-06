@echo off
chcp 65001 >nul
color 0A
echo.
echo ╔════════════════════════════════════════════╗
echo ║     SQLGrammarException 完整修复工具      ║
echo ╚════════════════════════════════════════════╝
echo.

echo [步骤 1/5] 停止后端服务...
taskkill /F /IM java.exe 2>nul
if %errorlevel% equ 0 (
    echo [√] 后端服务已停止
) else (
    echo [!] 没有运行中的后端服务
)
timeout /t 2 >nul

echo.
echo [步骤 2/5] 备份当前数据库...
mkdir backup 2>nul
mysqldump -u root -p990328 blogdb > backup\blogdb_backup_%date:~0,4%%date:~5,2%%date:~8,2%_%time:~0,2%%time:~3,2%%time:~6,2%.sql 2>nul
if %errorlevel% equ 0 (
    echo [√] 数据库备份完成
) else (
    echo [!] 数据库备份失败，但继续执行
)

echo.
echo [步骤 3/5] 执行表结构修复...
mysql -u root -p990328 blogdb -e "ALTER TABLE posts MODIFY COLUMN content LONGTEXT NOT NULL; ALTER TABLE posts MODIFY COLUMN \`desc\` VARCHAR(500);"
if %errorlevel% equ 0 (
    echo [√] 表结构修复成功
) else (
    echo [×] 表结构修复失败
    echo.
    echo 请检查：
    echo 1. MySQL服务是否启动
    echo 2. 数据库 blogdb 是否存在
    echo 3. 数据库密码是否正确
    pause
    exit /b 1
)

echo.
echo [步骤 4/5] 验证表结构...
mysql -u root -p990328 blogdb -e "DESC posts;" > table_structure.txt
type table_structure.txt
del table_structure.txt
echo [√] 表结构已显示在上方

echo.
echo [步骤 5/5] 重新编译并启动后端...
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo [×] 编译失败
    pause
    exit /b 1
)

echo.
echo [√] 编译完成，正在启动后端服务...
start "Backend Server" cmd /c "java -jar target\backend-0.0.1-SNAPSHOT.jar"
timeout /t 3 >nul

echo.
echo ╔════════════════════════════════════════════╗
echo ║            修复完成！                      ║
echo ║                                            ║
echo ║  后端服务正在新窗口中启动...              ║
echo ║  请等待约10-15秒后测试                    ║
echo ║                                            ║
echo ║  测试地址：                                ║
echo ║  http://localhost:4567/api/posts          ║
echo ╚════════════════════════════════════════════╝
echo.
pause
