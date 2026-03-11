-- 创建测试代理表
CREATE TABLE IF NOT EXISTS test_agent (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    agent_id VARCHAR(100) UNIQUE NOT NULL COMMENT '代理ID',
    agent_name VARCHAR(200) COMMENT '代理名称',
    hostname VARCHAR(200) COMMENT '主机名',
    os VARCHAR(50) COMMENT '操作系统',
    os_version VARCHAR(50) COMMENT '操作系统版本',
    browser VARCHAR(50) COMMENT '浏览器类型',
    browser_version VARCHAR(50) COMMENT '浏览器版本',
    ip VARCHAR(50) COMMENT 'IP地址',
    status TINYINT DEFAULT 1 COMMENT '状态：0-离线，1-在线，2-忙碌',
    token VARCHAR(500) COMMENT '认证Token',
    last_heartbeat DATETIME COMMENT '最后心跳时间',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_agent_id (agent_id),
    INDEX idx_status (status),
    INDEX idx_last_heartbeat (last_heartbeat)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试代理表';

-- 修改test_execution表，添加agent_id字段
ALTER TABLE test_execution ADD COLUMN IF NOT EXISTS agent_id VARCHAR(100) COMMENT '执行代理ID';
ALTER TABLE test_execution ADD INDEX IF NOT EXISTS idx_agent_id (agent_id);

-- 添加执行模式字段
ALTER TABLE test_execution ADD COLUMN IF NOT EXISTS execution_mode VARCHAR(20) DEFAULT 'server' COMMENT '执行模式：server-服务端，agent-客户端代理';
