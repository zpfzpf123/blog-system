-- 给projects表添加api_access_token字段
-- 用于存储API访问令牌，用于接口测试时的Authorization

USE blogdb;

-- 添加api_access_token字段
ALTER TABLE projects 
ADD COLUMN api_access_token VARCHAR(1000) NULL COMMENT 'API访问令牌，用于接口测试时的Authorization';

-- 查看修改结果
DESC projects;
