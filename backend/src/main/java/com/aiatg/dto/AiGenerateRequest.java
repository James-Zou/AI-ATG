package com.aiatg.dto;

import lombok.Data;

/**
 * AI生成请求DTO
 */
@Data
public class AiGenerateRequest {
    /**
     * 需求ID（用于从需求生成测试用例）
     */
    private Long requirementId;
    
    /**
     * 描述性语言（用于生成测试步骤）
     */
    private String description;
    
    /**
     * AI提供商（deepseek/qwen/zhipu），为空则使用默认
     */
    private String provider;
    
    /**
     * 生成数量
     */
    private Integer count;
    
    /**
     * 温度参数
     */
    private Double temperature;
    
    /**
     * 模板ID
     */
    private Long templateId;
    
    /**
     * 自定义提示词
     */
    private String customPrompt;
}
