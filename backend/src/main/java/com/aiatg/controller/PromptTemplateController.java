package com.aiatg.controller;

import com.aiatg.common.Result;
import com.aiatg.entity.PromptTemplate;
import com.aiatg.service.PromptTemplateService;
import com.aiatg.util.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 提示词模板控制器
 */
@RestController
@RequestMapping("/ai/template")
@CrossOrigin
public class PromptTemplateController {
    
    @Autowired
    private PromptTemplateService promptTemplateService;
    
    /**
     * 获取所有模板
     */
    @GetMapping("/list")
    public Result<List<PromptTemplate>> getAllTemplates() {
        try {
            List<PromptTemplate> templates = promptTemplateService.getAllTemplates();
            return Result.success(templates);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取模板详情
     */
    @GetMapping("/{id}")
    public Result<PromptTemplate> getTemplateById(@PathVariable Long id) {
        try {
            PromptTemplate template = promptTemplateService.getTemplateById(id);
            return Result.success(template);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 创建模板
     */
    @PostMapping
    public Result<PromptTemplate> createTemplate(@RequestBody PromptTemplate template) {
        try {
            Long userId = getCurrentUserId();
            PromptTemplate result = promptTemplateService.createTemplate(template, userId);
            return Result.success("创建成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新模板
     */
    @PutMapping("/{id}")
    public Result<PromptTemplate> updateTemplate(@PathVariable Long id, @RequestBody PromptTemplate template) {
        try {
            PromptTemplate result = promptTemplateService.updateTemplate(id, template);
            return Result.success("更新成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除模板
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteTemplate(@PathVariable Long id) {
        try {
            promptTemplateService.deleteTemplate(id);
            return Result.success("删除成功", null);
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
