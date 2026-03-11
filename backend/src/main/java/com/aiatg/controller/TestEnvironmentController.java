package com.aiatg.controller;

import com.aiatg.common.PageResult;
import com.aiatg.common.Result;
import com.aiatg.dto.EnvironmentRequest;
import com.aiatg.service.TestEnvironmentService;
import com.aiatg.util.UserHolder;
import com.aiatg.vo.EnvironmentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 测试环境控制器
 */
@RestController
@RequestMapping("/environment")
@CrossOrigin
public class TestEnvironmentController {
    
    @Autowired
    private TestEnvironmentService environmentService;
    
    /**
     * 创建测试环境
     */
    @PostMapping
    public Result<EnvironmentVO> createEnvironment(@Valid @RequestBody EnvironmentRequest request) {
        try {
            Long userId = getCurrentUserId();
            EnvironmentVO vo = environmentService.createEnvironment(request, userId);
            return Result.success("创建成功", vo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新测试环境
     */
    @PutMapping("/{id}")
    public Result<EnvironmentVO> updateEnvironment(
        @PathVariable Long id,
        @Valid @RequestBody EnvironmentRequest request
    ) {
        try {
            EnvironmentVO vo = environmentService.updateEnvironment(id, request);
            return Result.success("更新成功", vo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除测试环境
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteEnvironment(@PathVariable Long id) {
        try {
            environmentService.deleteEnvironment(id);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取环境详情
     */
    @GetMapping("/{id}")
    public Result<EnvironmentVO> getEnvironmentById(@PathVariable Long id) {
        try {
            EnvironmentVO vo = environmentService.getEnvironmentById(id);
            return Result.success(vo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取环境列表（分页）
     */
    @GetMapping("/list")
    public Result<PageResult<EnvironmentVO>> getEnvironmentList(
        @RequestParam(required = false) Long projectId,
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        try {
            PageResult<EnvironmentVO> result = environmentService.getEnvironmentList(projectId, pageNum, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取项目的所有环境（不分页）
     */
    @GetMapping("/all")
    public Result<List<EnvironmentVO>> getAllEnvironments(@RequestParam Long projectId) {
        try {
            List<EnvironmentVO> list = environmentService.getAllEnvironments(projectId);
            return Result.success(list);
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
