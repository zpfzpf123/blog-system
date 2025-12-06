@echo off
echo 正在更新文章摘要字段...
echo.

mysql -u root -p990328 -e "USE blogdb; UPDATE posts SET `desc` = CONCAT(LEFT(REPLACE(REPLACE(content, '#', ''), CHAR(10), ' '), 150), '...') WHERE `desc` IS NULL OR `desc` = '';"

if %errorlevel% equ 0 (
    echo 更新成功！
    echo.
    echo 正在查询更新结果...
    mysql -u root -p990328 -e "USE blogdb; SELECT id, title, LEFT(`desc`, 80) as desc_preview FROM posts ORDER BY id DESC LIMIT 5;"
) else (
    echo 更新失败！请检查MySQL是否正在运行。
)

echo.
pause
