-- 为技能表添加配置数据字段
ALTER TABLE skill ADD COLUMN config_data TEXT COMMENT '技能配置数据(JSON格式)，用于存储可编辑的测试步骤或脚本参数模板';
