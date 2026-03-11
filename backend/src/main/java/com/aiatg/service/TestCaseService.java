package com.aiatg.service;

import com.aiatg.common.PageResult;
import com.aiatg.dto.TestCaseDTO;
import com.aiatg.dto.TestCaseQueryDTO;
import com.aiatg.vo.TestCaseVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 测试用例服务接口
 */
public interface TestCaseService {
    
    /**
     * 创建测试用例
     */
    TestCaseVO createTestCase(TestCaseDTO dto, Long userId);
    
    /**
     * 更新测试用例
     */
    TestCaseVO updateTestCase(Long id, TestCaseDTO dto);
    
    /**
     * 删除测试用例
     */
    void deleteTestCase(Long id);
    
    /**
     * 根据ID获取测试用例
     */
    TestCaseVO getTestCaseById(Long id);
    
    /**
     * 分页查询测试用例列表
     */
    PageResult<TestCaseVO> getTestCaseList(TestCaseQueryDTO query);
    
    /**
     * 更新用例状态
     */
    void updateStatus(Long id, String status);
    
    /**
     * 批量删除测试用例
     */
    void batchDelete(List<Long> ids);
    
    /**
     * 导入测试用例（Excel）
     */
    int importTestCases(MultipartFile file, Long projectId, Long userId);
    
    /**
     * 导出测试用例（Excel）
     */
    byte[] exportTestCases(TestCaseQueryDTO query);
}
