-- 创建测试环境表
CREATE TABLE IF NOT EXISTS test_environment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    env_name VARCHAR(100) NOT NULL COMMENT '环境名称',
    env_code VARCHAR(50) NOT NULL COMMENT '环境编码',
    base_url VARCHAR(255) COMMENT '基础URL',
    description VARCHAR(500) COMMENT '描述',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    INDEX idx_project_id (project_id),
    INDEX idx_env_code (env_code),
    UNIQUE KEY uk_project_env (project_id, env_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试环境表';

-- 插入默认测试环境数据
INSERT INTO test_environment (project_id, env_name, env_code, base_url, description, created_by) VALUES
(1, '开发环境', 'dev', 'http://dev.example.com', '开发环境', 1),
(1, '测试环境', 'test', 'http://test.example.com', '测试环境', 1),
(1, '预发布环境', 'staging', 'http://staging.example.com', '预发布环境', 1),
(1, '生产环境', 'prod', 'http://prod.example.com', '生产环境', 1);
