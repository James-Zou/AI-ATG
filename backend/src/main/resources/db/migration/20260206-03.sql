-- 创建脚本执行记录表
CREATE TABLE IF NOT EXISTS script_execution (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '执行记录ID',
    skill_id BIGINT NOT NULL COMMENT '关联的技能ID',
    execution_name VARCHAR(200) NOT NULL COMMENT '执行名称',
    script_language VARCHAR(20) NOT NULL COMMENT '脚本语言: python, javascript, shell',
    script_content TEXT NOT NULL COMMENT '执行的脚本内容快照',
    status VARCHAR(20) NOT NULL COMMENT '执行状态: PENDING-待执行, RUNNING-执行中, SUCCESS-成功, FAILED-失败',
    output LONGTEXT COMMENT '执行输出',
    error_message LONGTEXT COMMENT '错误信息',
    exit_code INT COMMENT '退出码',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    duration_ms BIGINT COMMENT '执行持续时间（毫秒）',
    create_by BIGINT COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    INDEX idx_skill_id (skill_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (skill_id) REFERENCES skill(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='脚本执行记录表';
