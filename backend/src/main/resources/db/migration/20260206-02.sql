-- 创建技能表
CREATE TABLE IF NOT EXISTS skill (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '技能ID',
    name VARCHAR(100) NOT NULL COMMENT '技能名称',
    description VARCHAR(500) COMMENT '技能描述',
    type VARCHAR(20) NOT NULL COMMENT '技能类型: TESTSUITE-测试套件, SCRIPT-自定义脚本',
    test_suite_id BIGINT COMMENT '关联的测试套件ID',
    script_language VARCHAR(20) COMMENT '脚本语言: python, javascript, shell',
    script_content TEXT COMMENT '脚本内容',
    enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用: 1-启用, 0-禁用',
    create_by BIGINT COMMENT '创建人ID',
    update_by BIGINT COMMENT '更新人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    INDEX idx_type (type),
    INDEX idx_test_suite_id (test_suite_id),
    INDEX idx_enabled (enabled),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='技能表';

-- 添加唯一索引，防止同一测试套件被重复导入
CREATE UNIQUE INDEX uk_testsuite_skill ON skill(test_suite_id, deleted) WHERE type = 'TESTSUITE';
