-- 为现有文章批量生成desc摘要（从content截取前200个字符）
-- 使用数据库：blogdb

USE blogdb;

-- 更新所有desc为null的文章，从content中提取摘要
UPDATE posts 
SET `desc` = CASE 
    WHEN content IS NOT NULL AND LENGTH(content) > 0 THEN
        CONCAT(
            SUBSTRING(
                REGEXP_REPLACE(
                    REGEXP_REPLACE(content, '#+ ', ''),  -- 去除markdown标题符号
                    '\n+', ' '  -- 将换行符替换为空格
                ),
                1, 200
            ),
            CASE WHEN LENGTH(content) > 200 THEN '...' ELSE '' END
        )
    ELSE '暂无摘要'
END
WHERE `desc` IS NULL OR `desc` = '';

-- 查看更新后的结果
SELECT id, title, `desc`, 
       SUBSTRING(content, 1, 50) as content_preview,
       created_at
FROM posts
ORDER BY id DESC
LIMIT 10;
