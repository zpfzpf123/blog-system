-- 移除网站表字段长度限制的数据库迁移脚本
-- 执行前请备份数据库

-- 1. 修改name字段，移除长度限制
ALTER TABLE websites MODIFY COLUMN name VARCHAR(65535) NOT NULL;

-- 2. 修改url字段，移除长度限制
ALTER TABLE websites MODIFY COLUMN url VARCHAR(65535) NOT NULL;

-- 3. 修改icon字段，移除长度限制
ALTER TABLE websites MODIFY COLUMN icon VARCHAR(65535);

-- 4. 修改favicon字段，移除长度限制
ALTER TABLE websites MODIFY COLUMN favicon VARCHAR(65535);

-- 5. 修改screenshot字段，移除长度限制
ALTER TABLE websites MODIFY COLUMN screenshot VARCHAR(65535);

-- 6. 验证修改结果
DESCRIBE websites;

-- 7. 检查是否有数据丢失
SELECT 
    '网站总数' as info,
    COUNT(*) as count
FROM websites
UNION ALL
SELECT 
    '有名称的网站' as info,
    COUNT(*) as count
FROM websites
WHERE name IS NOT NULL AND name != ''
UNION ALL
SELECT 
    '有URL的网站' as info,
    COUNT(*) as count
FROM websites
WHERE url IS NOT NULL AND url != '';

-- 8. 测试插入长数据（可选）
-- INSERT INTO websites (name, url, description, icon, favicon, screenshot, visit_count, is_favorite, is_active, status, created_at, updated_at)
-- VALUES (
--     '这是一个非常长的网站名称，用于测试字段长度限制是否已经移除。这个名称包含了大量的中文字符和英文字符，以确保数据库能够正确处理长字符串。',
--     'https://www.example.com/very/long/url/path/that/exceeds/normal/length/limits/and/includes/many/parameters/and/subdirectories/to/test/the/database/field/length/constraints',
--     '这是一个非常长的网站描述，用于测试TEXT字段是否能够正确处理大量文本内容。这个描述包含了详细的功能介绍、技术特点、使用说明等信息，以确保数据库能够存储完整的网站信息。',
--     'https://www.example.com/very/long/icon/url/path/that/exceeds/normal/length/limits/and/includes/many/parameters/and/subdirectories/to/test/the/database/field/length/constraints/icon.png',
--     'https://www.example.com/very/long/favicon/url/path/that/exceeds/normal/length/limits/and/includes/many/parameters/and/subdirectories/to/test/the/database/field/length/constraints/favicon.ico',
--     'https://www.example.com/very/long/screenshot/url/path/that/exceeds/normal/length/limits/and/includes/many/parameters/and/subdirectories/to/test/the/database/field/length/constraints/screenshot.png',
--     0, false, true, 'ACTIVE', NOW(), NOW()
-- );

-- 9. 清理测试数据（如果执行了测试插入）
-- DELETE FROM websites WHERE name LIKE '这是一个非常长的网站名称%';

-- 迁移完成提示
SELECT '字段长度限制移除完成' as status;
