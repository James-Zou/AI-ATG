-- API Key 管理表
CREATE TABLE IF NOT EXISTS api_key (
    id VARCHAR(32) PRIMARY KEY COMMENT 'API Key ID',
    user_id VARCHAR(32) NOT NULL COMMENT '关联用户ID',
    account_id VARCHAR(32) NOT NULL COMMENT '账户ID',
    api_key VARCHAR(64) NOT NULL UNIQUE COMMENT 'API Key（唯一）',
    secret_key VARCHAR(128) NOT NULL COMMENT 'Secret Key（加密存储）',
    app_name VARCHAR(100) COMMENT '应用名称',
    description VARCHAR(500) COMMENT 'API Key 描述',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    expire_time DATETIME COMMENT '过期时间（NULL 表示永不过期）',
    last_used_time DATETIME COMMENT '最后使用时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_api_key (api_key),
    INDEX idx_account_id (account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API Key 管理表';

