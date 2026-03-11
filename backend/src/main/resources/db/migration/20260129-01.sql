-- 为test_report表添加人工确认相关字段
ALTER TABLE test_report
ADD COLUMN need_confirm TINYINT DEFAULT 1 COMMENT '是否需要人工确认：0-不需要，1-需要',
ADD COLUMN confirm_status TINYINT DEFAULT 0 COMMENT '确认状态：0-待确认，1-已确认通过，2-已确认失败',
ADD COLUMN confirmed_by BIGINT COMMENT '确认人ID',
ADD COLUMN confirmed_time DATETIME COMMENT '确认时间',
ADD COLUMN confirm_remark VARCHAR(500) COMMENT '确认备注';

-- 添加索引以提高查询性能
CREATE INDEX idx_confirm_status ON test_report(confirm_status);
CREATE INDEX idx_execution_id ON test_report(execution_id);
