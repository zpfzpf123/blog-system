-- 移除网站访问次数字段的数据库迁移脚本
-- 执行时间: 2025-01-27

-- 1. 删除visit_count相关的索引
DROP INDEX IF EXISTS idx_website_visit_count ON websites;
DROP INDEX IF EXISTS idx_websites_visit_created ON websites;

-- 2. 删除visit_count字段
ALTER TABLE websites DROP COLUMN IF EXISTS visit_count;

-- 3. 删除访问次数相关的存储过程
DROP PROCEDURE IF EXISTS UpdateWebsiteVisitCount;

-- 4. 更新视图，移除visit_count字段
DROP VIEW IF EXISTS website_details;
CREATE OR REPLACE VIEW website_details AS
SELECT 
    w.id,
    w.name,
    w.url,
    w.description,
    w.category_id,
    w.icon,
    w.favicon,
    w.screenshot,
    w.is_favorite,
    w.is_active,
    w.status,
    w.last_check_time,
    w.created_at,
    w.updated_at,
    wc.name as category_name,
    wc.color as category_color
FROM websites w
LEFT JOIN website_categories wc ON w.category_id = wc.id
WHERE w.is_active = TRUE;

-- 5. 更新分类统计视图，移除total_visits字段
DROP VIEW IF EXISTS category_statistics;
CREATE OR REPLACE VIEW category_statistics AS
SELECT 
    wc.id,
    wc.name,
    wc.description,
    wc.color,
    wc.sort_order,
    COUNT(w.id) as website_count,
    MAX(w.updated_at) as last_updated
FROM website_categories wc
LEFT JOIN websites w ON wc.id = w.category_id AND w.is_active = TRUE
WHERE wc.is_active = TRUE
GROUP BY wc.id, wc.name, wc.description, wc.color, wc.sort_order
ORDER BY wc.sort_order, wc.name;

-- 6. 更新网站统计表，移除total_visits字段
ALTER TABLE website_statistics DROP COLUMN IF EXISTS total_visits;

-- 迁移完成
SELECT 'visit_count字段移除完成' as migration_status;
