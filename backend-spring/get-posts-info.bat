@echo off
chcp 65001 >nul
echo 正在导出所有文章信息...
echo.

mysql -u root -p990328 --default-character-set=utf8mb4 -e "USE blogdb; SELECT id, title, LEFT(content, 300) as content_preview FROM posts ORDER BY id;" > posts_data.txt

if %errorlevel% equ 0 (
    echo 导出成功！文件保存在: posts_data.txt
    echo.
    type posts_data.txt
) else (
    echo 导出失败！
)

pause
