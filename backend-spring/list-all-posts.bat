@echo off
chcp 65001 >nul
cls
echo ================================================
echo 查询所有博客文章列表
echo ================================================
echo.

mysql -u root -p990328 --table -e "USE blogdb; SELECT id, title, CHAR_LENGTH(content) as content_length, IFNULL(`desc`, '待生成') as current_desc FROM posts ORDER BY id;"

echo.
echo ================================================
echo 共查询到以上文章
echo ================================================
echo.

echo 是否需要查看某篇文章的完整内容？
echo 请输入文章ID（直接回车跳过）:
set /p article_id=

if not "%article_id%"=="" (
    echo.
    echo ================================================
    echo 文章ID %article_id% 的完整内容:
    echo ================================================
    mysql -u root -p990328 -e "USE blogdb; SELECT id, title, content FROM posts WHERE id = %article_id%\G"
)

echo.
pause
