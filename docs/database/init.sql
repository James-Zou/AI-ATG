-- ===================================
-- AI-ATG 自动化测试平台数据库完整初始化脚本
-- 版本：v2.0 (已修复所有缺失的表)
-- ===================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS ai_atg DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE ai_atg;

-- ===================================
-- 1. 项目表
-- ===================================
CREATE TABLE IF NOT EXISTS `project` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(100) NOT NULL COMMENT '项目名称',
  `description` TEXT COMMENT '项目描述',
  `gitlab_project_id` INT COMMENT 'GitLab项目ID',
  `gitlab_webhook_url` VARCHAR(500) COMMENT 'Webhook URL',
  `gitlab_webhook_secret` VARCHAR(100) COMMENT 'Webhook密钥',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-活跃，0-归档',
  `created_by` BIGINT COMMENT '创建人ID',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_name` (`name`),
  INDEX `idx_status` (`status`),
  INDEX `idx_gitlab_project_id` (`gitlab_project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目表';

-- ===================================
-- 2. 用户表
-- ===================================
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `username` VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
  `password` VARCHAR(200) NOT NULL COMMENT '密码（加密）',
  `nickname` VARCHAR(100) COMMENT '昵称',
  `email` VARCHAR(100) COMMENT '邮箱',
  `phone` VARCHAR(20) COMMENT '手机号',
  `avatar_url` VARCHAR(500) COMMENT '头像URL',
  `role` VARCHAR(50) DEFAULT 'tester' COMMENT '角色：admin/test_lead/tester/developer',
  `department` VARCHAR(100) COMMENT '部门',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
  `last_login_time` DATETIME COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(50) COMMENT '最后登录IP',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_username` (`username`),
  INDEX `idx_email` (`email`),
  INDEX `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ===================================
-- 3. 项目成员表
-- ===================================
CREATE TABLE IF NOT EXISTS `project_member` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `project_id` BIGINT NOT NULL COMMENT '项目ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `role` VARCHAR(50) DEFAULT 'member' COMMENT '角色：owner/admin/developer/tester/member',
  `joined_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  UNIQUE KEY `uk_project_user` (`project_id`, `user_id`),
  INDEX `idx_project_id` (`project_id`),
  INDEX `idx_user_id` (`user_id`),
  FOREIGN KEY (`project_id`) REFERENCES `project`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目成员表';

-- ===================================
-- 4. 需求表
-- ===================================
CREATE TABLE IF NOT EXISTS `requirement` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `project_id` BIGINT NOT NULL COMMENT '项目ID',
  `title` VARCHAR(200) NOT NULL COMMENT '需求标题',
  `content` LONGTEXT COMMENT '需求内容（富文本）',
  `type` VARCHAR(50) COMMENT '需求类型：user_story/feature/bug_fix/improvement',
  `priority` VARCHAR(20) DEFAULT 'P2' COMMENT '优先级：P0/P1/P2/P3',
  `source` VARCHAR(50) DEFAULT 'manual' COMMENT '来源：manual/gitlab/jira/api',
  `source_id` VARCHAR(100) COMMENT '来源ID（如GitLab MR ID）',
  `attachment_urls` JSON COMMENT '附件URLs',
  `status` VARCHAR(50) DEFAULT 'draft' COMMENT '状态：draft/reviewing/approved/testing/completed',
  `assignee` BIGINT COMMENT '负责人ID',
  `reviewer` BIGINT COMMENT '评审人ID',
  `created_by` BIGINT,
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_project` (`project_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_assignee` (`assignee`),
  FOREIGN KEY (`project_id`) REFERENCES `project`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='需求表';

-- ===================================
-- 5. 测试用例表
-- ===================================
CREATE TABLE IF NOT EXISTS `test_case` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `project_id` BIGINT NOT NULL,
  `requirement_id` BIGINT COMMENT '关联需求ID',
  `case_no` VARCHAR(50) UNIQUE COMMENT '用例编号：TC001',
  `title` VARCHAR(200) NOT NULL COMMENT '用例标题',
  `type` VARCHAR(50) DEFAULT 'functional' COMMENT '类型：functional/ui/api/performance/security',
  `priority` VARCHAR(20) DEFAULT 'P2' COMMENT '优先级：P0/P1/P2/P3',
  `level` VARCHAR(20) DEFAULT 'medium' COMMENT '级别：high/medium/low',
  `preconditions` TEXT COMMENT '前置条件',
  `steps` JSON COMMENT '测试步骤',
  `expected_result` TEXT COMMENT '预期结果',
  `test_data` JSON COMMENT '测试数据',
  `tags` VARCHAR(500) COMMENT '标签（逗号分隔）',
  `source` VARCHAR(50) DEFAULT 'manual' COMMENT '来源：manual/ai_generated',
  `ai_model` VARCHAR(50) COMMENT 'AI模型：deepseek/qwen/chatglm',
  `ai_generation_id` BIGINT COMMENT 'AI生成记录ID',
  `status` VARCHAR(50) DEFAULT 'draft' COMMENT '状态：draft/reviewing/approved/deprecated',
  `assignee` BIGINT COMMENT '负责人ID',
  `created_by` BIGINT,
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_project` (`project_id`),
  INDEX `idx_requirement` (`requirement_id`),
  INDEX `idx_case_no` (`case_no`),
  INDEX `idx_type` (`type`),
  INDEX `idx_status` (`status`),
  FOREIGN KEY (`project_id`) REFERENCES `project`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`requirement_id`) REFERENCES `requirement`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='测试用例表';

-- ===================================
-- 6. 测试步骤表
-- ===================================
CREATE TABLE IF NOT EXISTS `test_step` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `test_case_id` BIGINT NOT NULL COMMENT '测试用例ID',
  `step_order` INT NOT NULL COMMENT '步骤顺序',
  `step_description` TEXT NOT NULL COMMENT '步骤描述',
  `expected_result` TEXT COMMENT '预期结果',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_test_case_id` (`test_case_id`),
  INDEX `idx_step_order` (`step_order`),
  FOREIGN KEY (`test_case_id`) REFERENCES `test_case`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='测试步骤表';

-- ===================================
-- 7. 测试套件表
-- ===================================
CREATE TABLE IF NOT EXISTS `test_suite` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `project_id` BIGINT NOT NULL,
  `name` VARCHAR(100) NOT NULL,
  `description` TEXT,
  `type` VARCHAR(50) DEFAULT 'custom' COMMENT '类型：smoke/regression/full/custom',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `created_by` BIGINT,
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_project` (`project_id`),
  INDEX `idx_type` (`type`),
  FOREIGN KEY (`project_id`) REFERENCES `project`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='测试套件表';

-- ===================================
-- 8. 测试套件用例关联表
-- ===================================
CREATE TABLE IF NOT EXISTS `suite_case_relation` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `suite_id` BIGINT NOT NULL,
  `case_id` BIGINT NOT NULL,
  `execute_order` INT DEFAULT 0 COMMENT '执行顺序',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_suite` (`suite_id`),
  INDEX `idx_case` (`case_id`),
  UNIQUE KEY `uk_suite_case` (`suite_id`, `case_id`),
  FOREIGN KEY (`suite_id`) REFERENCES `test_suite`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`case_id`) REFERENCES `test_case`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='测试套件用例关联表';

-- ===================================
-- 9. 测试执行记录表
-- ===================================
CREATE TABLE IF NOT EXISTS `test_execution` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `project_id` BIGINT NOT NULL,
  `suite_id` BIGINT COMMENT '测试套件ID',
  `execution_name` VARCHAR(200) COMMENT '执行名称',
  `execution_type` VARCHAR(50) DEFAULT 'manual' COMMENT '执行类型：manual/scheduled/ci/webhook',
  `environment` VARCHAR(50) DEFAULT 'test' COMMENT '测试环境：dev/test/staging/prod',
  `status` VARCHAR(50) DEFAULT 'pending' COMMENT '状态：pending/running/completed/failed/cancelled',
  `total_cases` INT DEFAULT 0 COMMENT '总用例数',
  `passed_cases` INT DEFAULT 0 COMMENT '通过用例数',
  `failed_cases` INT DEFAULT 0 COMMENT '失败用例数',
  `skipped_cases` INT DEFAULT 0 COMMENT '跳过用例数',
  `error_cases` INT DEFAULT 0 COMMENT '错误用例数',
  `pass_rate` DECIMAL(5,2) COMMENT '通过率',
  `duration` BIGINT COMMENT '执行时长（毫秒）',
  `trigger_type` VARCHAR(50) COMMENT '触发类型',
  `executed_by` BIGINT COMMENT '执行人ID',
  `start_time` DATETIME COMMENT '开始时间',
  `end_time` DATETIME COMMENT '结束时间',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_project` (`project_id`),
  INDEX `idx_suite` (`suite_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_start_time` (`start_time`),
  FOREIGN KEY (`project_id`) REFERENCES `project`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='测试执行记录表';

-- ===================================
-- 10. 测试执行明细表
-- ===================================
CREATE TABLE IF NOT EXISTS `test_execution_detail` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `execution_id` BIGINT NOT NULL COMMENT '执行ID',
  `test_case_id` BIGINT NOT NULL COMMENT '测试用例ID',
  `status` INT DEFAULT 0 COMMENT '状态：0-待执行，1-通过，2-失败，3-跳过',
  `error_message` TEXT COMMENT '错误信息',
  `stack_trace` LONGTEXT COMMENT '堆栈跟踪',
  `screenshot_url` VARCHAR(500) COMMENT '截图URL',
  `video_url` VARCHAR(500) COMMENT '视频URL',
  `logs` LONGTEXT COMMENT '执行日志',
  `duration` BIGINT COMMENT '执行时长（毫秒）',
  `start_time` DATETIME COMMENT '开始时间',
  `end_time` DATETIME COMMENT '结束时间',
  INDEX `idx_execution_id` (`execution_id`),
  INDEX `idx_test_case_id` (`test_case_id`),
  INDEX `idx_status` (`status`),
  FOREIGN KEY (`execution_id`) REFERENCES `test_execution`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`test_case_id`) REFERENCES `test_case`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='测试执行明细表';

-- ===================================
-- 11. 测试报告表
-- ===================================
CREATE TABLE IF NOT EXISTS `test_report` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `execution_id` BIGINT NOT NULL,
  `report_type` VARCHAR(50) DEFAULT 'html' COMMENT '报告类型：html/pdf/excel',
  `report_url` VARCHAR(500) COMMENT '报告URL',
  `summary` JSON COMMENT '报告摘要',
  `file_size` BIGINT COMMENT '文件大小（字节）',
  `generated_by` BIGINT COMMENT '生成人ID',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_execution` (`execution_id`),
  INDEX `idx_type` (`report_type`),
  FOREIGN KEY (`execution_id`) REFERENCES `test_execution`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='测试报告表';

-- ===================================
-- 12. AI配置表
-- ===================================
CREATE TABLE IF NOT EXISTS `ai_config` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `provider` VARCHAR(50) NOT NULL COMMENT 'AI提供商：deepseek/qwen/zhipu/openai',
  `model_name` VARCHAR(100) NOT NULL COMMENT '模型名称',
  `api_key` VARCHAR(500) COMMENT 'API密钥',
  `api_url` VARCHAR(500) COMMENT 'API地址',
  `max_tokens` INT DEFAULT 4000 COMMENT '最大Token数',
  `temperature` DOUBLE DEFAULT 0.7 COMMENT '温度参数（0-1）',
  `status` INT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `is_default` INT DEFAULT 0 COMMENT '是否默认：1-是，0-否',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_provider` (`provider`),
  INDEX `idx_status` (`status`),
  INDEX `idx_is_default` (`is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI配置表';

-- ===================================
-- 13. AI生成历史表
-- ===================================
CREATE TABLE IF NOT EXISTS `ai_generate_history` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `requirement_id` BIGINT COMMENT '需求ID',
  `provider` VARCHAR(50) COMMENT 'AI提供商：deepseek/qwen/zhipu/openai',
  `model_name` VARCHAR(100) COMMENT '模型名称',
  `prompt` LONGTEXT COMMENT '提示词',
  `response` LONGTEXT COMMENT 'AI响应内容',
  `generated_count` INT DEFAULT 0 COMMENT '生成用例数量',
  `tokens` INT COMMENT 'Token消耗',
  `duration` BIGINT COMMENT '耗时（毫秒）',
  `status` INT DEFAULT 1 COMMENT '状态：1-成功，0-失败',
  `error_message` TEXT COMMENT '错误信息',
  `created_by` BIGINT COMMENT '创建人ID',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX `idx_requirement` (`requirement_id`),
  INDEX `idx_provider` (`provider`),
  INDEX `idx_status` (`status`),
  INDEX `idx_created_time` (`created_time`),
  FOREIGN KEY (`requirement_id`) REFERENCES `requirement`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI生成历史表';

-- ===================================
-- 14. 提示词模板表
-- ===================================
CREATE TABLE IF NOT EXISTS `prompt_template` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(100) NOT NULL COMMENT '模板名称',
  `description` TEXT COMMENT '模板描述',
  `template_type` VARCHAR(50) COMMENT '模板类型：requirement/code_review/test_generation',
  `content` LONGTEXT NOT NULL COMMENT '模板内容',
  `status` INT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `is_default` INT DEFAULT 0 COMMENT '是否默认：1-是，0-否',
  `created_by` BIGINT COMMENT '创建人ID',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_template_type` (`template_type`),
  INDEX `idx_status` (`status`),
  INDEX `idx_is_default` (`is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提示词模板表';

-- ===================================
-- 15. GitLab配置表
-- ===================================
CREATE TABLE IF NOT EXISTS `gitlab_config` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `project_id` BIGINT NOT NULL COMMENT '项目ID',
  `gitlab_url` VARCHAR(500) COMMENT 'GitLab地址',
  `gitlab_token` VARCHAR(500) COMMENT 'GitLab访问令牌',
  `webhook_secret` VARCHAR(200) COMMENT 'Webhook密钥',
  `repository_url` VARCHAR(500) COMMENT '仓库地址',
  `default_branch` VARCHAR(100) DEFAULT 'main' COMMENT '默认分支',
  `auto_trigger` INT DEFAULT 0 COMMENT '自动触发：1-是，0-否',
  `status` INT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `created_by` BIGINT COMMENT '创建人ID',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_project_id` (`project_id`),
  INDEX `idx_status` (`status`),
  FOREIGN KEY (`project_id`) REFERENCES `project`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GitLab配置表';

-- ===================================
-- 16. Webhook记录表
-- ===================================
CREATE TABLE IF NOT EXISTS `webhook_record` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `project_id` BIGINT NOT NULL COMMENT '项目ID',
  `event_type` VARCHAR(50) COMMENT '事件类型：push/merge_request/tag',
  `object_kind` VARCHAR(50) COMMENT '对象类型',
  `ref` VARCHAR(200) COMMENT '引用（分支/标签）',
  `commit_id` VARCHAR(100) COMMENT '提交ID',
  `commit_message` TEXT COMMENT '提交信息',
  `commit_author` VARCHAR(100) COMMENT '提交作者',
  `diff_content` LONGTEXT COMMENT '差异内容',
  `file_count` INT DEFAULT 0 COMMENT '变更文件数',
  `status` INT DEFAULT 0 COMMENT '处理状态：0-待处理，1-处理成功，2-处理失败',
  `error_message` TEXT COMMENT '错误信息',
  `generated_cases` INT DEFAULT 0 COMMENT '生成用例数',
  `received_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '接收时间',
  `processed_time` DATETIME COMMENT '处理时间',
  INDEX `idx_project_id` (`project_id`),
  INDEX `idx_event_type` (`event_type`),
  INDEX `idx_status` (`status`),
  INDEX `idx_received_time` (`received_time`),
  FOREIGN KEY (`project_id`) REFERENCES `project`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Webhook记录表';

-- ===================================
-- 17. 系统配置表
-- ===================================
CREATE TABLE IF NOT EXISTS `system_config` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `config_key` VARCHAR(100) UNIQUE NOT NULL COMMENT '配置键',
  `config_value` TEXT COMMENT '配置值',
  `config_type` VARCHAR(50) COMMENT '配置类型：ai/gitlab/notification/storage/system',
  `description` VARCHAR(500) COMMENT '描述',
  `is_encrypted` TINYINT DEFAULT 0 COMMENT '是否加密：0-否，1-是',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_key` (`config_key`),
  INDEX `idx_type` (`config_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- ===================================
-- 18. 操作日志表
-- ===================================
CREATE TABLE IF NOT EXISTS `operation_log` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT COMMENT '操作用户ID',
  `username` VARCHAR(50) COMMENT '用户名',
  `operation` VARCHAR(100) COMMENT '操作类型',
  `method` VARCHAR(200) COMMENT '请求方法',
  `params` TEXT COMMENT '请求参数',
  `result` TEXT COMMENT '返回结果',
  `ip` VARCHAR(50) COMMENT 'IP地址',
  `location` VARCHAR(100) COMMENT '操作地点',
  `browser` VARCHAR(100) COMMENT '浏览器',
  `os` VARCHAR(100) COMMENT '操作系统',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-成功，0-失败',
  `error_message` TEXT COMMENT '错误信息',
  `duration_ms` INT COMMENT '执行时长（毫秒）',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_user` (`user_id`),
  INDEX `idx_operation` (`operation`),
  INDEX `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ===================================
-- 插入初始数据
-- ===================================

-- 插入默认管理员用户（密码：Admin@123）
INSERT IGNORE INTO `user` (`id`, `username`, `password`, `nickname`, `email`, `role`, `status`)
VALUES (1, 'admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHODYRz6zXPL6EauAy', '系统管理员', 'admin@aiatg.com', 'admin', 1);

-- 插入默认测试用户
INSERT IGNORE INTO `user` (`id`, `username`, `password`, `nickname`, `email`, `role`, `status`)
VALUES (2, 'tester', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHODYRz6zXPL6EauAy', '测试人员', 'tester@aiatg.com', 'tester', 1);

-- 插入示例项目
INSERT IGNORE INTO `project` (`id`, `name`, `description`, `status`, `created_by`)
VALUES (1, '示例项目', '这是一个示例项目，用于演示AI-ATG平台的功能', 1, 1);

-- 插入默认 AI 配置
INSERT IGNORE INTO `ai_config` (`id`, `provider`, `model_name`, `api_key`, `api_url`, `max_tokens`, `temperature`, `status`, `is_default`)
VALUES 
(1, 'deepseek', 'deepseek-chat', '', 'https://api.deepseek.com/v1', 4000, 0.7, 1, 1),
(2, 'qwen', 'qwen-max', '', 'https://dashscope.aliyuncs.com/api/v1', 4000, 0.7, 1, 0),
(3, 'zhipu', 'glm-4', '', 'https://open.bigmodel.cn/api/paas/v4', 4000, 0.7, 1, 0);

-- 插入默认提示词模板
INSERT IGNORE INTO `prompt_template` (`id`, `name`, `description`, `template_type`, `content`, `status`, `is_default`)
VALUES 
(1, '默认测试用例生成模板', '从需求生成测试用例的默认模板', 'test_generation', '请根据以下需求生成详细的测试用例：\n\n{requirement}\n\n要求：\n1. 生成功能测试用例\n2. 包含边界值测试\n3. 包含异常场景测试\n4. 每个用例包含：用例标题、前置条件、测试步骤、预期结果', 1, 1);

-- 插入系统默认配置
INSERT IGNORE INTO `system_config` (`config_key`, `config_value`, `config_type`, `description`)
VALUES 
('ai.deepseek.enabled', 'true', 'ai', '是否启用DeepSeek'),
('ai.qwen.enabled', 'true', 'ai', '是否启用通义千问'),
('gitlab.enabled', 'false', 'gitlab', '是否启用GitLab集成'),
('notification.email.enabled', 'false', 'notification', '是否启用邮件通知'),
('storage.type', 'local', 'storage', '存储类型：local/minio/oss');

-- ===================================
-- 创建视图
-- ===================================

-- 测试用例统计视图
CREATE OR REPLACE VIEW `v_testcase_statistics` AS
SELECT 
    p.id AS project_id,
    p.name AS project_name,
    COUNT(tc.id) AS total_cases,
    SUM(CASE WHEN tc.type = 'ui' THEN 1 ELSE 0 END) AS ui_cases,
    SUM(CASE WHEN tc.type = 'api' THEN 1 ELSE 0 END) AS api_cases,
    SUM(CASE WHEN tc.type = 'performance' THEN 1 ELSE 0 END) AS performance_cases,
    SUM(CASE WHEN tc.source = 'ai_generated' THEN 1 ELSE 0 END) AS ai_generated_cases,
    SUM(CASE WHEN tc.status = 'approved' THEN 1 ELSE 0 END) AS approved_cases
FROM project p
LEFT JOIN test_case tc ON p.id = tc.project_id
GROUP BY p.id, p.name;

-- 测试执行统计视图
CREATE OR REPLACE VIEW `v_execution_statistics` AS
SELECT 
    p.id AS project_id,
    p.name AS project_name,
    COUNT(te.id) AS total_executions,
    SUM(CASE WHEN te.status = 'completed' THEN 1 ELSE 0 END) AS completed_executions,
    AVG(CASE WHEN te.status = 'completed' THEN te.pass_rate ELSE NULL END) AS avg_pass_rate,
    SUM(te.total_cases) AS total_cases_executed,
    SUM(te.passed_cases) AS total_passed_cases,
    SUM(te.failed_cases) AS total_failed_cases
FROM project p
LEFT JOIN test_execution te ON p.id = te.project_id
GROUP BY p.id, p.name;

-- ===================================
-- 完成
-- ===================================

SELECT '✅ AI-ATG 数据库初始化完成！' AS message;
SELECT '✅ 数据库版本：v2.0（包含所有18个表）' AS version;
SELECT '✅ 默认管理员账号: admin / Admin@123' AS admin_info;
SELECT '✅ 默认测试账号: tester / Admin@123' AS tester_info;
SELECT '✅ 已配置3个AI提供商：DeepSeek、通义千问、智谱AI' AS ai_info;
