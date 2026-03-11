package com.aiatg.executor;

import com.aiatg.entity.TestCase;
import com.aiatg.entity.TestExecutionDetail;

/**
 * 测试执行器接口
 */
public interface TestExecutor {
    
    /**
     * 执行测试用例
     * @param testCase 测试用例
     * @param executionId 执行ID
     * @return 执行明细
     */
    TestExecutionDetail execute(TestCase testCase, Long executionId);
    
    /**
     * 获取执行器类型
     */
    String getExecutorType();
    
    /**
     * 检查执行器是否可用
     */
    boolean isAvailable();
}
