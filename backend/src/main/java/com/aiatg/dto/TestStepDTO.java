package com.aiatg.dto;

import lombok.Data;

/**
 * 测试步骤DTO
 */
@Data
public class TestStepDTO {
    
    private Integer stepOrder;
    
    private String stepDescription;
    
    private String expectedResult;
}
