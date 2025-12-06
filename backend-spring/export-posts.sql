-- 导出所有文章的内容到文件，方便AI分析
USE blogdb;

SELECT 
    CONCAT(
        '========== 文章ID: ', id, ' ==========\n',
        '标题: ', title, '\n',
        '作者: ', IFNULL(author, '未知'), '\n',
        '当前摘要: ', IFNULL(`desc`, '无'), '\n',
        '内容预览:\n',
        LEFT(content, 500), '\n',
        '==========================================\n\n'
    ) AS article_info
FROM posts
ORDER BY id
INTO OUTFILE 'C:/ProgramData/MySQL/MySQL Server 8.0/Uploads/posts_export.txt'
LINES TERMINATED BY '';
