-- 查询所有文章的基本信息
USE blogdb;

SELECT 
    id,
    title,
    `desc`,
    LEFT(content, 100) as content_preview,
    author,
    created_at
FROM posts
ORDER BY id;
