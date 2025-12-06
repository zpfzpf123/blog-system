@echo off
chcp 65001 >nul
echo ================================================
echo 自动为所有博客文章生成desc摘要
echo ================================================
echo.

echo [1/3] 正在查询当前文章列表...
echo.
mysql -u root -p990328 -e "USE blogdb; SELECT id, title, IF(`desc` IS NULL OR `desc` = '', '无', '有') as has_desc FROM posts ORDER BY id;"

echo.
echo [2/3] 正在批量生成desc摘要...
mysql -u root -p990328 blogdb < auto-generate-desc.sql

if %errorlevel% equ 0 (
    echo.
    echo [3/3] 生成成功！查看更新结果：
    echo.
    mysql -u root -p990328 -e "USE blogdb; SELECT id, title, LEFT(`desc`, 60) as description FROM posts ORDER BY id DESC LIMIT 10;"
    echo.
    echo ================================================
    echo 更新完成！所有文章的desc摘要已自动生成。
    echo ================================================
) else (
    echo.
    echo ❌ 更新失败！请检查：
    echo 1. MySQL服务是否正在运行
    echo 2. 数据库连接信息是否正确（用户名: root, 密码: 990328）
    echo 3. blogdb数据库是否存在
)

echo.
pause
