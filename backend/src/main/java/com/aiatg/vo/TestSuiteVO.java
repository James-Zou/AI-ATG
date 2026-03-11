package com.aiatg.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 测试套件VO
 */
@Data
public class TestSuiteVO {
    
    private Long id;
    
    private Long projectId;
    
    private String projectName;
    
    private String name;
    
    private String description;
    
    private Integer status;
    
    private Integer caseCount;
    
    private Long createdBy;
    
    private String createdByName;
    
    private LocalDateTime createdTime;
    
    private LocalDateTime updatedTime;
}
