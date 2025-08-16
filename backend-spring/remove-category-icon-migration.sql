-- 移除分类图标字段的数据库迁移脚本
-- 执行时间: 2025-02-27
-- 描述: 移除网站分类表中的icon字段，因为网站合集模块不再需要分类图标

-- 1. 移除视图中的分类图标字段
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
    w.visit_count,
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

-- 2. 移除分类统计视图中的分类图标字段
DROP VIEW IF EXISTS category_statistics;
CREATE OR REPLACE VIEW category_statistics AS
SELECT 
    wc.id,
    wc.name,
    wc.description,
    wc.color,
    wc.sort_order,
    COUNT(w.id) as website_count,
    SUM(w.visit_count) as total_visits,
    MAX(w.updated_at) as last_updated
FROM website_categories wc
LEFT JOIN websites w ON wc.id = w.category_id AND w.is_active = TRUE
WHERE wc.is_active = TRUE
GROUP BY wc.id, wc.name, wc.description, wc.color, wc.sort_order
ORDER BY wc.sort_order, wc.name;

-- 3. 移除website_categories表中的icon字段
ALTER TABLE website_categories DROP COLUMN IF EXISTS icon;

-- 4. 显示迁移结果
SELECT 'Category icon field removed successfully!' as message;
SELECT 'Views updated successfully!' as message;
SELECT COUNT(*) as total_categories FROM website_categories;
