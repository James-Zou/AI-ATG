package com.aiatg.dto;

import lombok.Data;

/**
 * 需求查询DTO
 */
@Data
public class RequirementQueryDTO {
    
    private Long projectId;
    
    private String keyword;
    
    private String type;
    
    private String priority;
    
    private String status;
    
    private Integer pageNum = 1;
    
    private Integer pageSize = 10;
}
