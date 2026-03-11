-- 修改test_execution_detail表，允许test_case_id为NULL（用于技能执行场景）
ALTER TABLE `test_execution_detail` 
MODIFY COLUMN `test_case_id` BIGINT NULL COMMENT '测试用例ID（技能执行时可为NULL）';

-- 删除原有的外键约束
ALTER TABLE `test_execution_detail` 
DROP FOREIGN KEY `test_execution_detail_ibfk_2`;

-- 重新添加外键约束（允许NULL）
ALTER TABLE `test_execution_detail` 
ADD CONSTRAINT `test_execution_detail_ibfk_2` 
FOREIGN KEY (`test_case_id`) REFERENCES `test_case`(`id`) ON DELETE CASCADE;
