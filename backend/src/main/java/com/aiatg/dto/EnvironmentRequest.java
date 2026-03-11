package com.aiatg.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 测试环境请求DTO
 */
@Data
public class EnvironmentRequest {
    
    @NotNull(message = "项目ID不能为空")
    private Long projectId;
    
    @NotBlank(message = "环境名称不能为空")
    private String envName;
    
    @NotBlank(message = "环境编码不能为空")
    private String envCode;
    
    private String baseUrl;
    
    private String description;
    
    private Integer status;
}
