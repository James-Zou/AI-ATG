package com.aiatg.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 测试环境VO
 */
@Data
public class EnvironmentVO {
    
    private Long id;
    
    private Long projectId;
    
    private String projectName;
    
    private String envName;
    
    private String envCode;
    
    private String baseUrl;
    
    private String description;
    
    private Integer status;
    
    private LocalDateTime createdTime;
    
    private LocalDateTime updatedTime;
    
    private Long createdBy;
    
    private String createdByName;
}
