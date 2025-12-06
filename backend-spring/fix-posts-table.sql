-- 修复posts表结构问题
USE blogdb;

-- 1. 先查看当前content字段的类型
SELECT 
    COLUMN_NAME, 
    DATA_TYPE, 
    CHARACTER_MAXIMUM_LENGTH
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'blogdb' 
AND TABLE_NAME = 'posts'
AND COLUMN_NAME = 'content';

-- 2. 修改content字段为LONGTEXT类型（支持更大的内容）
ALTER TABLE posts MODIFY COLUMN content LONGTEXT NOT NULL;

-- 3. 确保desc字段有反引号保护（desc是MySQL关键字）
ALTER TABLE posts MODIFY COLUMN `desc` VARCHAR(500);

-- 4. 验证修改结果
DESC posts;

-- 5. 测试插入一条数据
-- INSERT INTO posts (title, `desc`, content, author, created_at, category_id) 
-- VALUES ('测试文章', '测试摘要', '这是一篇测试文章的内容', '周鹏飞', NOW(), NULL);
