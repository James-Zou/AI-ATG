-- 修复 test_report 表，添加缺失的 report_name 列
ALTER TABLE test_report ADD COLUMN  report_name VARCHAR(200) COMMENT '报告名称';
