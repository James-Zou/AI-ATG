package com.aiatg.controller;

import com.aiatg.common.PageResult;
import com.aiatg.common.Result;
import com.aiatg.dto.AiGenerateRequest;
import com.aiatg.entity.AiGenerateHistory;
import com.aiatg.service.AiGenerateService;
import com.aiatg.util.UserHolder;
import com.aiatg.vo.AiGenerateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * AI生成控制器
 */
@RestController
@RequestMapping("/ai/generate")
@CrossOrigin
public class AiGenerateController {
    
    @Autowired
    private AiGenerateService aiGenerateService;
    
    /**
     * 从需求生成测试用例
     */
    @PostMapping("/requirement")
    public Result<AiGenerateResponse> generateFromRequirement(@Valid @RequestBody AiGenerateRequest request) {
        try {
            Long userId = getCurrentUserId();
            AiGenerateResponse response = aiGenerateService.generateFromRequirement(request, userId);
            return Result.success("生成成功", response);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取生成历史
     */
    @GetMapping("/history")
    public Result<PageResult<AiGenerateHistory>> getGenerateHistory(
        @RequestParam(required = false) Long requirementId,
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        try {
            PageResult<AiGenerateHistory> result = aiGenerateService.getGenerateHistory(requirementId, pageNum, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取历史详情
     */
    @GetMapping("/history/{id}")
    public Result<AiGenerateHistory> getHistoryById(@PathVariable Long id) {
        try {
            AiGenerateHistory history = aiGenerateService.getHistoryById(id);
            return Result.success(history);
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
