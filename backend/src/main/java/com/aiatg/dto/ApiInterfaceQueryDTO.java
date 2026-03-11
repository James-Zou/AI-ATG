package com.aiatg.dto;

import lombok.Data;

/**
 * API接口查询DTO
 */
@Data
public class ApiInterfaceQueryDTO {
    
    private Long projectId;
    
    private String keyword;
    
    private String method;
    
    private String status;
    
    private String category;
    
    private Integer pageNum = 1;
    
    private Integer pageSize = 10;
}
