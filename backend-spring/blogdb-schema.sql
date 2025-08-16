-- 创建数据库
CREATE DATABASE IF NOT EXISTS blogdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE blogdb;

-- 创建分类表
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    created_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建标签表
CREATE TABLE tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建文章表
CREATE TABLE posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    `desc` VARCHAR(500),
    content TEXT NOT NULL,
    author VARCHAR(255),
    created_at DATETIME,
    category_id BIGINT,
    FOREIGN KEY (category_id) REFERENCES categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建文章标签关联表
CREATE TABLE post_tags (
    post_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (post_id, tag_id),
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 插入一些示例数据
INSERT INTO categories (name, created_at) VALUES 
('Technology', NOW()),
('Life', NOW()),
('Travel', NOW());

INSERT INTO tags (name) VALUES 
('Java'),
('Spring Boot'),
('Vue.js'),
('MySQL'),
('Tutorial');

INSERT INTO posts (title, `desc`, content, author, created_at, category_id) VALUES 
('First Blog Post', 'This is the summary of the first blog post', '# Welcome to my blog\nThis is the content of the first post.', 'John Doe', NOW(), 1),
('MySQL Tutorial', 'Learn MySQL basics', 'This is a comprehensive MySQL tutorial for beginners.', 'Jane Smith', NOW(), 1);

-- 为文章添加标签
INSERT INTO post_tags (post_id, tag_id) VALUES 
(1, 1), -- First Blog Post -> Java
(1, 2), -- First Blog Post -> Spring Boot
(2, 4); -- MySQL Tutorial -> MySQL