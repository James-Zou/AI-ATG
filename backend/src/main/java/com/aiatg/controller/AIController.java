package com.aiatg.controller;

import com.aiatg.common.Result;
import com.aiatg.dto.AiGenerateRequest;
import com.aiatg.service.AIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI控制器
 */
@Slf4j
@RestController
@RequestMapping("/ai")
@CrossOrigin
public class AIController {
    
    @Autowired
    private AIService aiService;
    
    /**
     * AI生成测试步骤
     */
    @PostMapping("/generate-steps")
    public Result<List<Map<String, Object>>> generateSteps(@RequestBody AiGenerateRequest request) {
        try {
            log.info("AI生成测试步骤，描述: {}, 提供商: {}", request.getDescription(), request.getProvider());
            List<Map<String, Object>> steps = aiService.generateTestSteps(
                request.getDescription(), 
                request.getProvider()
            );
            return Result.success("生成成功", steps);
        } catch (Exception e) {
            log.error("AI生成失败", e);
            return Result.error("AI生成失败: " + e.getMessage());
        }
    }
}
