-- 检查posts表结构
USE blogdb;

-- 查看完整表结构
DESC posts;

-- 查看content和desc字段详细信息
SELECT 
    COLUMN_NAME as '字段名', 
    DATA_TYPE as '数据类型', 
    CHARACTER_MAXIMUM_LENGTH as '最大长度',
    IS_NULLABLE as '可空',
    COLUMN_TYPE as '完整类型'
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'blogdb' 
AND TABLE_NAME = 'posts'
AND COLUMN_NAME IN ('content', 'desc')
ORDER BY ORDINAL_POSITION;
