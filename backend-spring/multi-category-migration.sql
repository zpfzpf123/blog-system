-- 网站合集多分类支持数据库迁移脚本
-- 创建时间: 2025-08-15
-- 描述: 将单分类设计迁移为多分类设计，支持一个网站属于多个分类

-- 1. 创建网站分类关系表
CREATE TABLE IF NOT EXISTS website_category_relations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关系ID',
    website_id BIGINT NOT NULL COMMENT '网站ID',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    created_by BIGINT COMMENT '创建者ID',
    
    UNIQUE KEY uk_website_category (website_id, category_id),
    INDEX idx_relation_website (website_id),
    INDEX idx_relation_category (category_id),
    INDEX idx_relation_created (created_at),
    
    FOREIGN KEY (website_id) REFERENCES websites(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (category_id) REFERENCES website_categories(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='网站分类关系表';

-- 2. 备份现有数据
CREATE TEMPORARY TABLE temp_website_categories AS
SELECT id, website_id, category_id, created_at, created_by
FROM (
    SELECT 
        ROW_NUMBER() OVER () as id,
        w.id as website_id,
        w.category_id,
        w.created_at,
        w.created_by
    FROM websites w
    WHERE w.category_id IS NOT NULL
) t;

-- 3. 插入关系数据
INSERT INTO website_category_relations (website_id, category_id, created_at, created_by)
SELECT website_id, category_id, created_at, created_by
FROM temp_website_categories;

-- 4. 修改websites表结构，移除category_id字段
-- 注意：由于外键约束，我们需要先删除外键约束
ALTER TABLE websites DROP FOREIGN KEY websites_ibfk_1;

-- 删除category_id字段
ALTER TABLE websites DROP COLUMN category_id;

-- 5. 删除临时表
DROP TEMPORARY TABLE IF EXISTS temp_website_categories;

-- 6. 更新索引
-- 删除旧的分类索引
DROP INDEX IF EXISTS idx_website_category ON websites;

-- 7. 验证迁移结果
SELECT 'Migration completed successfully!' as message;
SELECT COUNT(*) as total_relations FROM website_category_relations;
SELECT COUNT(*) as total_websites FROM websites;

-- 8. 显示示例数据
SELECT 
    w.name as website_name,
    GROUP_CONCAT(wc.name) as categories
FROM websites w
LEFT JOIN website_category_relations wcr ON w.id = wcr.website_id
LEFT JOIN website_categories wc ON wcr.category_id = wc.id
GROUP BY w.id, w.name
LIMIT 10;
