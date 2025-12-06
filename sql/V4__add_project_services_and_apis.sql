-- 项目服务配置表（开发环境管理）
CREATE TABLE IF NOT EXISTS project_services (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL COMMENT '服务名称',
    type VARCHAR(50) DEFAULT 'other' COMMENT '服务类型: frontend, backend, database, other',
    start_command VARCHAR(500) COMMENT '启动命令',
    stop_command VARCHAR(500) COMMENT '停止命令',
    working_directory VARCHAR(500) COMMENT '工作目录',
    port INT COMMENT '端口号',
    health_check_url VARCHAR(500) COMMENT '健康检查URL',
    env_variables TEXT COMMENT '环境变量(JSON格式)',
    auto_restart BOOLEAN DEFAULT FALSE COMMENT '是否自动重启',
    start_order INT DEFAULT 0 COMMENT '启动顺序',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    description TEXT COMMENT '描述',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_project_id (project_id),
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目服务配置表';

-- 项目API接口表
CREATE TABLE IF NOT EXISTS project_apis (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL COMMENT '接口名称',
    method VARCHAR(20) DEFAULT 'GET' COMMENT '请求方法',
    path VARCHAR(500) NOT NULL COMMENT '接口路径',
    description TEXT COMMENT '接口描述',
    request_headers TEXT COMMENT '请求头(JSON)',
    request_params TEXT COMMENT '请求参数(JSON)',
    request_body TEXT COMMENT '请求体(JSON)',
    response_body TEXT COMMENT '响应示例(JSON)',
    mock_enabled BOOLEAN DEFAULT FALSE COMMENT '是否启用Mock',
    mock_data LONGTEXT COMMENT 'Mock响应数据',
    mock_delay INT DEFAULT 0 COMMENT 'Mock延迟(ms)',
    mock_status_code INT DEFAULT 200 COMMENT 'Mock状态码',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '开发状态: pending, developing, testing, completed',
    category VARCHAR(100) COMMENT '接口分类',
    tags VARCHAR(500) COMMENT '标签(逗号分隔)',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_project_id (project_id),
    INDEX idx_category (category),
    INDEX idx_status (status),
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目API接口表';

-- 示例数据（可选，根据需要取消注释）
-- INSERT INTO project_services (project_id, name, type, start_command, port, description, start_order) VALUES
-- (1, 'Frontend Dev Server', 'frontend', 'npm run dev', 5173, 'Vite dev server', 1),
-- (1, 'Backend Service', 'backend', 'mvn spring-boot:run', 8080, 'Spring Boot backend', 2);
