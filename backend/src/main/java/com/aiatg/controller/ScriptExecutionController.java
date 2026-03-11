package com.aiatg.controller;

import com.aiatg.common.Result;
import com.aiatg.entity.ScriptExecution;
import com.aiatg.mapper.ScriptExecutionMapper;
import com.aiatg.service.ScriptExecutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 脚本执行记录控制器
 */
@Tag(name = "脚本执行管理")
@Slf4j
@RestController
@RequestMapping("/script-executions")
public class ScriptExecutionController {

    @Resource
    private ScriptExecutionService scriptExecutionService;
    
    @Resource
    private ScriptExecutionMapper scriptExecutionMapper;

    @Operation(summary = "获取脚本执行状态")
    @GetMapping("/{id}/status")
    public Result<String> getStatus(
            @Parameter(description = "执行记录ID") @PathVariable Long id) {
        try {
            String status = scriptExecutionService.getExecutionStatus(id);
            if (status == null) {
                return Result.error("执行记录不存在");
            }
            return Result.success(status);
        } catch (Exception e) {
            log.error("获取脚本执行状态失败", e);
            return Result.error("获取执行状态失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取脚本执行输出")
    @GetMapping("/{id}/output")
    public Result<String> getOutput(
            @Parameter(description = "执行记录ID") @PathVariable Long id) {
        try {
            String output = scriptExecutionService.getExecutionOutput(id);
            if (output == null) {
                return Result.error("执行记录不存在");
            }
            return Result.success(output);
        } catch (Exception e) {
            log.error("获取脚本执行输出失败", e);
            return Result.error("获取执行输出失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取脚本执行详情")
    @GetMapping("/{id}")
    public Result<ScriptExecution> getDetail(
            @Parameter(description = "执行记录ID") @PathVariable Long id) {
        try {
            ScriptExecution execution = scriptExecutionMapper.selectById(id);
            if (execution == null) {
                return Result.error("执行记录不存在");
            }
            return Result.success(execution);
        } catch (Exception e) {
            log.error("获取脚本执行详情失败", e);
            return Result.error("获取执行详情失败: " + e.getMessage());
        }
    }
}
