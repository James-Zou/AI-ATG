package com.aiatg.service;

import com.aiatg.common.PageResult;
import com.aiatg.dto.EnvironmentRequest;
import com.aiatg.vo.EnvironmentVO;

import java.util.List;

/**
 * 测试环境服务接口
 */
public interface TestEnvironmentService {
    
    /**
     * 创建测试环境
     */
    EnvironmentVO createEnvironment(EnvironmentRequest request, Long userId);
    
    /**
     * 更新测试环境
     */
    EnvironmentVO updateEnvironment(Long id, EnvironmentRequest request);
    
    /**
     * 删除测试环境
     */
    void deleteEnvironment(Long id);
    
    /**
     * 获取环境详情
     */
    EnvironmentVO getEnvironmentById(Long id);
    
    /**
     * 获取环境列表（分页）
     */
    PageResult<EnvironmentVO> getEnvironmentList(Long projectId, Integer pageNum, Integer pageSize);
    
    /**
     * 获取项目的所有环境（不分页）
     */
    List<EnvironmentVO> getAllEnvironments(Long projectId);
}
