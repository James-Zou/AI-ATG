package com.aiatg.service;

import com.aiatg.common.PageResult;
import com.aiatg.dto.ExecutionRequest;
import com.aiatg.vo.ExecutionVO;

/**
 * 测试执行服务接口
 */
public interface TestExecutionService {
    
    /**
     * 创建并执行测试
     */
    ExecutionVO createAndExecute(ExecutionRequest request, Long userId);
    
    /**
     * 获取执行详情
     */
    ExecutionVO getExecutionById(Long id);
    
    /**
     * 获取执行列表
     */
    PageResult<ExecutionVO> getExecutionList(Long projectId, Integer pageNum, Integer pageSize);
    
    /**
     * 停止执行
     */
    void stopExecution(Long id);
}
