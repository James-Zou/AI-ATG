-- 创建接口全集表
CREATE TABLE IF NOT EXISTS api_interface (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '接口ID',
    project_id BIGINT NOT NULL COMMENT '所属项目ID',
    interface_name VARCHAR(200) NOT NULL COMMENT '接口名称',
    method VARCHAR(20) NOT NULL COMMENT '请求方法(GET/POST/PUT/DELETE等)',
    url VARCHAR(500) NOT NULL COMMENT '接口URL',
    description TEXT COMMENT '接口描述',

    -- 请求配置
    headers JSON COMMENT '请求头（JSON格式）',
    params JSON COMMENT 'Query参数（JSON格式）',
    body TEXT COMMENT '请求体',
    body_type VARCHAR(50) COMMENT '请求体类型(raw/form-data/x-www-form-urlencoded)',

    -- 其他配置
    timeout INT DEFAULT 30000 COMMENT '超时时间(ms)',
    auth_type VARCHAR(50) COMMENT '认证类型(none/basic/bearer/api-key)',
    auth_config JSON COMMENT '认证配置（JSON格式）',

    -- 前置/后置脚本
    pre_request_script TEXT COMMENT '前置请求脚本',
    post_response_script TEXT COMMENT '后置响应脚本',

    -- 状态
    status VARCHAR(20) DEFAULT 'draft' COMMENT '状态(draft-草稿/published-已发布/archived-已归档)',
    category VARCHAR(100) COMMENT '接口分类',
    tags VARCHAR(200) COMMENT '标签（逗号分隔）',

    -- 元数据
    created_by BIGINT COMMENT '创建人ID',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT COMMENT '更新人ID',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX idx_project_id (project_id),
    INDEX idx_status (status),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API接口全集表';
