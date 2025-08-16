-- 网站合集功能数据库表结构设计
-- 创建时间: 2025-02-27
-- 描述: 支持网站收藏、分类管理、标签系统等功能的完整数据库设计

-- 1. 网站分类表
CREATE TABLE IF NOT EXISTS website_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID',
    name VARCHAR(100) NOT NULL COMMENT '分类名称',
    description TEXT COMMENT '分类描述',
    color VARCHAR(20) DEFAULT '#409EFF' COMMENT '分类颜色',

    sort_order INT DEFAULT 0 COMMENT '排序顺序',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建者ID',
    updated_by BIGINT COMMENT '更新者ID',
    
    UNIQUE KEY uk_category_name (name),
    INDEX idx_category_active (is_active),
    INDEX idx_category_sort (sort_order),
    INDEX idx_category_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='网站分类表';

-- 2. 网站信息表
CREATE TABLE IF NOT EXISTS websites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '网站ID',
    name VARCHAR(200) NOT NULL COMMENT '网站名称',
    url VARCHAR(500) NOT NULL COMMENT '网站URL',
    description TEXT COMMENT '网站描述',
    category_id BIGINT NOT NULL COMMENT '所属分类ID',
    icon VARCHAR(500) COMMENT '网站图标URL',
    favicon VARCHAR(500) COMMENT '网站favicon URL',
    screenshot VARCHAR(500) COMMENT '网站截图URL',
    visit_count BIGINT DEFAULT 0 COMMENT '访问次数',
    is_favorite BOOLEAN DEFAULT FALSE COMMENT '是否收藏',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    status ENUM('active', 'inactive', 'broken') DEFAULT 'active' COMMENT '网站状态',
    last_check_time TIMESTAMP NULL COMMENT '最后检查时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建者ID',
    updated_by BIGINT COMMENT '更新者ID',
    
    UNIQUE KEY uk_website_url (url),
    INDEX idx_website_category (category_id),
    INDEX idx_website_name (name),
    INDEX idx_website_favorite (is_favorite),
    INDEX idx_website_active (is_active),
    INDEX idx_website_status (status),
    INDEX idx_website_visit_count (visit_count),
    INDEX idx_website_created (created_at),
    INDEX idx_website_updated (updated_at),
    
    FOREIGN KEY (category_id) REFERENCES website_categories(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='网站信息表';





-- 5. 网站访问记录表
CREATE TABLE IF NOT EXISTS website_visits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '访问记录ID',
    website_id BIGINT NOT NULL COMMENT '网站ID',
    visitor_ip VARCHAR(45) COMMENT '访问者IP地址',
    user_agent TEXT COMMENT '用户代理信息',
    referer VARCHAR(500) COMMENT '来源页面',
    visit_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '访问时间',
    
    INDEX idx_visit_website (website_id),
    INDEX idx_visit_time (visit_time),
    INDEX idx_visit_ip (visitor_ip),
    
    FOREIGN KEY (website_id) REFERENCES websites(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='网站访问记录表';

-- 6. 网站收藏表
CREATE TABLE IF NOT EXISTS website_favorites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '收藏ID',
    website_id BIGINT NOT NULL COMMENT '网站ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    favorite_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    notes TEXT COMMENT '收藏备注',
    
    UNIQUE KEY uk_user_website (user_id, website_id),
    INDEX idx_favorite_website (website_id),
    INDEX idx_favorite_user (user_id),
    INDEX idx_favorite_time (favorite_time),
    
    FOREIGN KEY (website_id) REFERENCES websites(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='网站收藏表';

-- 7. 网站导入记录表
CREATE TABLE IF NOT EXISTS website_imports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '导入记录ID',
    import_name VARCHAR(200) NOT NULL COMMENT '导入名称',
    import_type ENUM('bookmark', 'csv', 'json', 'manual') NOT NULL COMMENT '导入类型',
    total_count INT DEFAULT 0 COMMENT '总数量',
    success_count INT DEFAULT 0 COMMENT '成功数量',
    failed_count INT DEFAULT 0 COMMENT '失败数量',
    import_file VARCHAR(500) COMMENT '导入文件路径',
    import_status ENUM('pending', 'processing', 'completed', 'failed') DEFAULT 'pending' COMMENT '导入状态',
    error_message TEXT COMMENT '错误信息',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    completed_at TIMESTAMP NULL COMMENT '完成时间',
    created_by BIGINT COMMENT '创建者ID',
    
    INDEX idx_import_status (import_status),
    INDEX idx_import_type (import_type),
    INDEX idx_import_created (created_at),
    INDEX idx_import_user (created_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='网站导入记录表';

-- 8. 网站统计表（用于缓存统计数据）
CREATE TABLE IF NOT EXISTS website_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '统计ID',
    stat_date DATE NOT NULL COMMENT '统计日期',
    total_websites BIGINT DEFAULT 0 COMMENT '总网站数',
    total_categories BIGINT DEFAULT 0 COMMENT '总分类数',

    total_visits BIGINT DEFAULT 0 COMMENT '总访问数',
    new_websites BIGINT DEFAULT 0 COMMENT '新增网站数',
    active_categories BIGINT DEFAULT 0 COMMENT '活跃分类数',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_stat_date (stat_date),
    INDEX idx_stat_date (stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='网站统计表';

-- 插入初始数据

-- 插入默认分类
INSERT INTO website_categories (name, description, color, sort_order) VALUES
('开发工具', '程序员开发必备的工具和资源', '#409EFF', 1),
('娱乐休闲', '放松娱乐的网站', '#67C23B0', 2),
('学习资源', '各类学习资料和教程', '#E6A23C', 3),
('设计创意', '设计灵感和创意资源', '#F56C6C', 4),
('新闻资讯', '最新新闻和资讯', '#909399', 5),
('购物消费', '在线购物和消费', '#9C27B0', 6);



-- 插入示例网站数据
INSERT INTO websites (name, url, description, category_id, icon, visit_count, is_favorite) VALUES
('GitHub', 'https://github.com', '全球最大的代码托管平台，支持Git版本控制', 1, 'https://github.githubassets.com/favicons/favicon.svg', 156, TRUE),
('Stack Overflow', 'https://stackoverflow.com', '程序员问答社区，解决各种编程问题', 1, 'https://cdn.sstatic.net/Sites/stackoverflow/Img/favicon.ico', 89, TRUE),
('MDN Web Docs', 'https://developer.mozilla.org', 'Web技术权威文档，学习前端开发必备', 3, 'https://developer.mozilla.org/favicon-48x48.cbbd161b.png', 234, TRUE),
('知乎', 'https://zhihu.com', '中文互联网最大的问答社区', 5, 'https://static.zhihu.com/heifetz/favicon.ico', 67, FALSE),
('B站', 'https://bilibili.com', '年轻人喜爱的视频网站，学习娱乐两不误', 2, 'https://www.bilibili.com/favicon.ico', 189, TRUE),
('掘金', 'https://juejin.cn', '优质的技术社区，分享技术文章', 3, 'https://lf3-cdn-tos.bytescm.com/obj/static/xitu_juejin_web/6c61ae65d1c41ae8221a670fa32d05aa.svg', 123, TRUE),
('Dribbble', 'https://dribbble.com', '设计师分享作品和灵感的平台', 4, 'https://cdn.dribbble.com/assets/favicon-63b2904a073c89b52b19aa08cebc95a155cde7d02fb76c2c6e54e163ae0c5aa4.ico', 78, FALSE),
('Unsplash', 'https://unsplash.com', '高质量免费图片素材网站', 4, 'https://images.unsplash.com/favicon-32x32.png', 145, TRUE);



-- 创建视图：网站详细信息视图
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
    wc.color as category_color,

FROM websites w
LEFT JOIN website_categories wc ON w.category_id = wc.id
WHERE w.is_active = TRUE;

-- 创建视图：分类统计视图
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
GROUP BY wc.id, wc.name, wc.description, wc.color, wc.icon, wc.sort_order
ORDER BY wc.sort_order, wc.name;



-- 创建存储过程：更新网站访问次数
DELIMITER //
CREATE PROCEDURE UpdateWebsiteVisitCount(IN website_id_param BIGINT)
BEGIN
    UPDATE websites 
    SET visit_count = visit_count + 1,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = website_id_param;
    
    INSERT INTO website_visits (website_id, visitor_ip, user_agent, referer)
    VALUES (website_id_param, @visitor_ip, @user_agent, @referer);
END //
DELIMITER ;

-- 创建存储过程：清理无效网站
DELIMITER //
CREATE PROCEDURE CleanupInactiveWebsites(IN days_threshold INT)
BEGIN
    UPDATE websites 
    SET is_active = FALSE,
        status = 'broken',
        updated_at = CURRENT_TIMESTAMP
    WHERE last_check_time < DATE_SUB(NOW(), INTERVAL days_threshold DAY)
    AND is_active = TRUE;
END //
DELIMITER ;



-- 创建索引优化查询性能
CREATE INDEX idx_websites_category_status ON websites(category_id, is_active, status);
CREATE INDEX idx_websites_favorite_active ON websites(is_favorite, is_active);
CREATE INDEX idx_websites_visit_created ON websites(visit_count DESC, created_at DESC);

CREATE INDEX idx_visits_website_time ON website_visits(website_id, visit_time DESC);

-- 插入初始统计数据
INSERT INTO website_statistics (stat_date, total_websites, total_categories, total_visits, new_websites, active_categories)
SELECT 
    CURDATE(),
    COUNT(*) as total_websites,
    (SELECT COUNT(*) FROM website_categories WHERE is_active = TRUE) as total_categories,
    SUM(visit_count) as total_visits,
    COUNT(CASE WHEN DATE(created_at) = CURDATE() THEN 1 END) as new_websites,
    (SELECT COUNT(*) FROM website_categories WHERE is_active = TRUE) as active_categories
FROM websites 
WHERE is_active = TRUE;

-- 显示创建结果
SELECT 'Database tables created successfully!' as message;
SELECT COUNT(*) as total_categories FROM website_categories;
SELECT COUNT(*) as total_websites FROM websites;
