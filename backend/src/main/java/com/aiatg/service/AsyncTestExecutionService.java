package com.aiatg.service;

import com.aiatg.entity.TestCase;

import java.util.List;

/**
 * 异步测试执行服务接口
 */
public interface AsyncTestExecutionService {
    
    /**
     * 异步执行测试用例列表
     * @param executionId 执行ID
     * @param testCases 测试用例列表
     * @param executionType 执行类型
     */
    void executeTestsAsync(Long executionId, List<TestCase> testCases, String executionType);
}
