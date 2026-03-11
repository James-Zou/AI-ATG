package com.aiatg.controller;

import com.aiatg.common.PageResult;
import com.aiatg.common.Result;
import com.aiatg.dto.TestCaseDTO;
import com.aiatg.dto.TestCaseQueryDTO;
import com.aiatg.service.TestCaseService;
import com.aiatg.util.UserHolder;
import com.aiatg.vo.TestCaseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

/**
 * 测试用例控制器
 */
@RestController
@RequestMapping("/testcase")
@CrossOrigin
public class TestCaseController {
    
    @Autowired
    private TestCaseService testCaseService;
    
    /**
     * 创建测试用例
     */
    @PostMapping
    public Result<TestCaseVO> createTestCase(@Valid @RequestBody TestCaseDTO dto) {
        try {
            Long userId = getCurrentUserId();
            TestCaseVO vo = testCaseService.createTestCase(dto, userId);
            return Result.success("创建成功", vo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新测试用例
     */
    @PutMapping("/{id}")
    public Result<TestCaseVO> updateTestCase(
        @PathVariable Long id,
        @Valid @RequestBody TestCaseDTO dto
    ) {
        try {
            TestCaseVO vo = testCaseService.updateTestCase(id, dto);
            return Result.success("更新成功", vo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除测试用例
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteTestCase(@PathVariable Long id) {
        try {
            testCaseService.deleteTestCase(id);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取测试用例详情
     */
    @GetMapping("/{id}")
    public Result<TestCaseVO> getTestCaseById(@PathVariable Long id) {
        try {
            TestCaseVO vo = testCaseService.getTestCaseById(id);
            return Result.success(vo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 分页查询测试用例列表
     */
    @GetMapping("/list")
    public Result<PageResult<TestCaseVO>> getTestCaseList(TestCaseQueryDTO query) {
        try {
            PageResult<TestCaseVO> pageResult = testCaseService.getTestCaseList(query);
            return Result.success(pageResult);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新测试用例状态
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            testCaseService.updateStatus(id, status);
            return Result.success("状态更新成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 批量删除测试用例
     */
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        try {
            testCaseService.batchDelete(ids);
            return Result.success("批量删除成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 导入测试用例
     */
    @PostMapping("/import")
    public Result<Integer> importTestCases(
        @RequestParam("file") MultipartFile file,
        @RequestParam("projectId") Long projectId
    ) {
        try {
            Long userId = getCurrentUserId();
            int count = testCaseService.importTestCases(file, projectId, userId);
            return Result.success("导入成功，共导入" + count + "条用例", count);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 导出测试用例
     */
    @GetMapping("/export")
    public Result<byte[]> exportTestCases(TestCaseQueryDTO query) {
        try {
            byte[] data = testCaseService.exportTestCases(query);
            return Result.success("导出成功", data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        String userId = UserHolder.getUserId();
        return userId != null ? Long.valueOf(userId) : null;
    }
}
