package com.aiatg.dto;

import lombok.Data;

/**
 * 测试用例查询DTO
 */
@Data
public class TestCaseQueryDTO {
    
    private Long projectId;
    
    private Long requirementId;
    
    private Long suiteId;
    
    private String keyword;
    
    private String type;
    
    private String priority;
    
    private String status;
    
    private Integer pageNum = 1;
    
    private Integer pageSize = 10;
}
