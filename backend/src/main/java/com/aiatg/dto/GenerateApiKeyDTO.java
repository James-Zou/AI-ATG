package com.aiatg.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

/**
 * 生成 API Key 请求 DTO
 */
@Data
public class GenerateApiKeyDTO {
    
    @NotBlank(message = "应用名称不能为空")
    private String appName;
    
    private String description;
    
    @Positive(message = "过期天数必须为正数")
    private Integer expireDays; // null 表示永不过期
}
