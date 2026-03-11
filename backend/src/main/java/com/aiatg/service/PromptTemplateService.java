package com.aiatg.service;

import com.aiatg.entity.PromptTemplate;

import java.util.List;

/**
 * 提示词模板服务接口
 */
public interface PromptTemplateService {
    
    /**
     * 创建模板
     */
    PromptTemplate createTemplate(PromptTemplate template, Long userId);
    
    /**
     * 更新模板
     */
    PromptTemplate updateTemplate(Long id, PromptTemplate template);
    
    /**
     * 删除模板
     */
    void deleteTemplate(Long id);
    
    /**
     * 获取模板详情
     */
    PromptTemplate getTemplateById(Long id);
    
    /**
     * 获取所有模板
     */
    List<PromptTemplate> getAllTemplates();
    
    /**
     * 获取默认模板
     */
    PromptTemplate getDefaultTemplate(String templateType);
    
    /**
     * 渲染模板
     */
    String renderTemplate(Long templateId, Object data);
}
