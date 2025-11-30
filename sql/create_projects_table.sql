-- 创建项目表
CREATE TABLE IF NOT EXISTS `projects` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '项目ID',
  `name` VARCHAR(200) NOT NULL COMMENT '项目名称',
  `description` TEXT COMMENT '项目描述',
  `cover_image` VARCHAR(500) COMMENT '封面图片URL',
  `status` VARCHAR(20) DEFAULT 'in_progress' COMMENT '项目状态：进行中/已完成/暂停/计划中',
  `progress` INT DEFAULT 0 COMMENT '项目进度 0-100',
  `tech_stack` TEXT COMMENT '技术栈（JSON格式）',
  `local_path` VARCHAR(500) COMMENT '本地路径',
  `repo_url` VARCHAR(500) COMMENT '仓库URL',
  `readme_content` LONGTEXT COMMENT 'README.md内容',
  `git_commits` LONGTEXT COMMENT 'Git提交记录（JSON格式）',
  `is_favorite` BOOLEAN DEFAULT FALSE COMMENT '是否收藏',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_status` (`status`),
  INDEX `idx_created_at` (`created_at`),
  INDEX `idx_is_favorite` (`is_favorite`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目表';

-- 插入示例数据
INSERT INTO `projects` (`name`, `description`, `status`, `progress`, `tech_stack`, `local_path`, `repo_url`, `is_favorite`) VALUES
('AI博客系统', '基于Vue3+Spring Boot的个人博客系统，支持Markdown编辑、分类标签管理、AI问答等功能', '进行中', 75, '["Vue 3","TypeScript","Spring Boot","MySQL","Redis"]', 'E:/ai博客', 'https://github.com/username/ai-blog', TRUE),
('项目管理系统', '个人项目管理与进度追踪系统，类似Git的时间轴记录功能', '进行中', 20, '["Vue 3","TypeScript","Spring Boot","MySQL"]', 'E:/project-manager', '', TRUE),
('电商后台管理', '电商平台后台管理系统，包含商品、订单、用户管理等模块', '已完成', 100, '["React","TypeScript","Node.js","MongoDB"]', 'E:/ecommerce-admin', 'https://github.com/username/ecommerce', FALSE),
('在线学习平台', '在线课程学习平台，支持视频播放、课程管理、作业提交', '暂停', 45, '["Vue 3","Spring Boot","MySQL","FFmpeg"]', 'E:/online-learning', '', FALSE),
('数据可视化看板', '企业数据可视化大屏展示系统', '计划中', 0, '["React","ECharts","WebSocket"]', '', '', FALSE);
