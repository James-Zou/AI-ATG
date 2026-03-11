package com.aiatg.controller;

import com.aiatg.common.PageResult;
import com.aiatg.common.Result;
import com.aiatg.dto.ExecutionRequest;
import com.aiatg.service.TestExecutionService;
import com.aiatg.util.UserHolder;
import com.aiatg.vo.ExecutionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 测试执行控制器
 */
@RestController
@RequestMapping("/execution")
@CrossOrigin
public class TestExecutionController {
    
    @Autowired
    private TestExecutionService executionService;
    
    /**
     * 创建并执行测试
     */
    @PostMapping
    public Result<ExecutionVO> createAndExecute(@Valid @RequestBody ExecutionRequest request) {
        try {
            Long userId = getCurrentUserId();
            ExecutionVO vo = executionService.createAndExecute(request, userId);
            return Result.success("执行已开始", vo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取执行详情
     */
    @GetMapping("/{id}")
    public Result<ExecutionVO> getExecutionById(@PathVariable Long id) {
        try {
            ExecutionVO vo = executionService.getExecutionById(id);
            return Result.success(vo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取执行列表
     */
    @GetMapping("/list")
    public Result<PageResult<ExecutionVO>> getExecutionList(
        @RequestParam(required = false) Long projectId,
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        try {
            PageResult<ExecutionVO> result = executionService.getExecutionList(projectId, pageNum, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 停止执行
     */
    @PutMapping("/{id}/stop")
    public Result<Void> stopExecution(@PathVariable Long id) {
        try {
            executionService.stopExecution(id);
            return Result.success("执行已停止", null);
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
