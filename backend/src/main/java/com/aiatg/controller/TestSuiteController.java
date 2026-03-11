package com.aiatg.controller;

import com.aiatg.common.PageResult;
import com.aiatg.common.Result;
import com.aiatg.dto.SuiteCaseOrderDTO;
import com.aiatg.dto.TestSuiteDTO;
import com.aiatg.service.TestSuiteService;
import com.aiatg.util.SecurityUtil;
import com.aiatg.vo.SuiteCaseVO;
import com.aiatg.vo.TestSuiteVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 测试套件控制器
 */
@Slf4j
@RestController
@RequestMapping("/testsuite")
@CrossOrigin
public class TestSuiteController {
    
    @Autowired
    private TestSuiteService testSuiteService;
    
    /**
     * 创建测试套件
     */
    @PostMapping
    public Result<TestSuiteVO> createTestSuite(@Valid @RequestBody TestSuiteDTO dto) {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            log.info("创建测试套件，用户ID: {}, 套件名称: {}", userId, dto.getName());
            TestSuiteVO vo = testSuiteService.createTestSuite(dto, userId);
            return Result.success("创建成功", vo);
        } catch (Exception e) {
            log.error("创建测试套件失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新测试套件
     */
    @PutMapping("/{id}")
    public Result<TestSuiteVO> updateTestSuite(
        @PathVariable Long id,
        @Valid @RequestBody TestSuiteDTO dto
    ) {
        try {
            TestSuiteVO vo = testSuiteService.updateTestSuite(id, dto);
            return Result.success("更新成功", vo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除测试套件
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteTestSuite(@PathVariable Long id) {
        try {
            testSuiteService.deleteTestSuite(id);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取测试套件详情
     */
    @GetMapping("/{id}")
    public Result<TestSuiteVO> getTestSuiteById(@PathVariable Long id) {
        try {
            TestSuiteVO vo = testSuiteService.getTestSuiteById(id);
            return Result.success(vo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 分页查询测试套件列表
     */
    @GetMapping("/list")
    public Result<PageResult<TestSuiteVO>> getTestSuiteList(
        @RequestParam(required = false) Long projectId,
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        try {
            PageResult<TestSuiteVO> pageResult = testSuiteService.getTestSuiteList(projectId, pageNum, pageSize);
            return Result.success(pageResult);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取套件用例列表（按执行顺序）
     */
    @GetMapping("/{id}/cases")
    public Result<List<SuiteCaseVO>> getSuiteCases(@PathVariable Long id) {
        try {
            List<SuiteCaseVO> cases = testSuiteService.getSuiteCases(id);
            return Result.success(cases);
        } catch (Exception e) {
            log.error("获取套件用例列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取套件的合并测试步骤（用于技能编辑）
     */
    @GetMapping("/{id}/steps")
    public Result<String> getSuiteSteps(@PathVariable Long id) {
        try {
            String steps = testSuiteService.getSuiteSteps(id);
            return Result.success(steps);
        } catch (Exception e) {
            log.error("获取套件测试步骤失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新套件用例执行顺序
     */
    @PutMapping("/cases/order")
    public Result<Void> updateCaseOrder(@Valid @RequestBody SuiteCaseOrderDTO dto) {
        try {
            testSuiteService.updateCaseOrder(dto);
            return Result.success("更新顺序成功", null);
        } catch (Exception e) {
            log.error("更新用例执行顺序失败", e);
            return Result.error(e.getMessage());
        }
    }
    
}
