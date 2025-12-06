-- 为所有desc为null的文章生成简单摘要
USE blogdb;

-- 更新desc字段：从content中提取前150个字符作为摘要
UPDATE posts 
SET `desc` = CONCAT(LEFT(REPLACE(REPLACE(content, '#', ''), '\n', ' '), 150), '...')
WHERE `desc` IS NULL OR `desc` = '';

-- 验证更新结果
SELECT id, title, `desc` FROM posts WHERE id = 40;
