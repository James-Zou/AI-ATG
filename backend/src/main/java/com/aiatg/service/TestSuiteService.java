package com.aiatg.service;

import com.aiatg.common.PageResult;
import com.aiatg.dto.SuiteCaseOrderDTO;
import com.aiatg.dto.TestSuiteDTO;
import com.aiatg.vo.SuiteCaseVO;
import com.aiatg.vo.TestSuiteVO;

import java.util.List;

/**
 * 测试套件服务接口
 */
public interface TestSuiteService {
    
    /**
     * 创建测试套件
     */
    TestSuiteVO createTestSuite(TestSuiteDTO dto, Long userId);
    
    /**
     * 更新测试套件
     */
    TestSuiteVO updateTestSuite(Long id, TestSuiteDTO dto);
    
    /**
     * 删除测试套件
     */
    void deleteTestSuite(Long id);
    
    /**
     * 根据ID获取测试套件
     */
    TestSuiteVO getTestSuiteById(Long id);
    
    /**
     * 获取项目下的所有测试套件
     */
    PageResult<TestSuiteVO> getTestSuiteList(Long projectId, Integer pageNum, Integer pageSize);
    
    /**
     * 获取套件下的用例列表（按执行顺序）
     */
    List<SuiteCaseVO> getSuiteCases(Long suiteId);
    
    /**
     * 获取套件的合并测试步骤（用于技能编辑）
     */
    String getSuiteSteps(Long suiteId);
    
    /**
     * 更新套件用例执行顺序
     */
    void updateCaseOrder(SuiteCaseOrderDTO dto);
}
