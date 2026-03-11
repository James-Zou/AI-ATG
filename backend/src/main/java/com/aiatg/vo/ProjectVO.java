package com.aiatg.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目VO
 */
@Data
public class ProjectVO {
    
    private Long id;
    
    private String name;
    
    private String description;
    
    private Integer status;
    
    private Integer memberCount;
    
    private Integer requirementCount;
    
    private Integer testCaseCount;
    
    private Long createdBy;
    
    private String createdByName;
    
    private LocalDateTime createdTime;
}
