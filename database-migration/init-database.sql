-- 数据库初始化脚本
-- 适用于MySQL和PostgreSQL

-- 创建数据库（如果不存在）
-- MySQL
-- CREATE DATABASE IF NOT EXISTS blogdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- PostgreSQL
-- CREATE DATABASE blogdb;

-- 使用数据库
-- USE blogdb; -- MySQL
-- \c blogdb; -- PostgreSQL

-- 创建分类表
CREATE TABLE IF NOT EXISTS category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建标签表
CREATE TABLE IF NOT EXISTS tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建文章表
CREATE TABLE IF NOT EXISTS post (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    summary TEXT,
    category_id BIGINT,
    status VARCHAR(20) DEFAULT 'draft',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE SET NULL
);

-- 创建文章标签关联表
CREATE TABLE IF NOT EXISTS post_tag (
    post_id BIGINT,
    tag_id BIGINT,
    PRIMARY KEY (post_id, tag_id),
    FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tag(id) ON DELETE CASCADE
);

-- 创建网站表
CREATE TABLE IF NOT EXISTS website (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    url VARCHAR(500) NOT NULL,
    description TEXT,
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建网站分类表
CREATE TABLE IF NOT EXISTS website_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建网站分类关联表
CREATE TABLE IF NOT EXISTS website_category_relation (
    website_id BIGINT,
    category_id BIGINT,
    PRIMARY KEY (website_id, category_id),
    FOREIGN KEY (website_id) REFERENCES website(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES website_category(id) ON DELETE CASCADE
);

-- 插入示例数据
INSERT INTO category (name, description) VALUES 
('技术', '技术相关文章'),
('生活', '生活感悟'),
('教程', '学习教程')
ON DUPLICATE KEY UPDATE description = VALUES(description);

INSERT INTO tag (name) VALUES 
('Vue.js'),
('Spring Boot'),
('MySQL'),
('JavaScript'),
('Java')
ON DUPLICATE KEY UPDATE name = name;

INSERT INTO website_category (name, description, sort_order) VALUES 
('开发工具', '各种开发工具和IDE', 1),
('学习资源', '在线学习平台和教程', 2),
('技术博客', '优秀的技术博客网站', 3),
('开源项目', '值得关注的开源项目', 4)
ON DUPLICATE KEY UPDATE description = VALUES(description), sort_order = VALUES(sort_order);

-- 创建索引
CREATE INDEX idx_post_category ON post(category_id);
CREATE INDEX idx_post_status ON post(status);
CREATE INDEX idx_post_created_at ON post(created_at);
CREATE INDEX idx_website_status ON website(status);
CREATE INDEX idx_website_category_sort ON website_category(sort_order);
