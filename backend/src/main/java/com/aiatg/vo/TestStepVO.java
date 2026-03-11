package com.aiatg.vo;

import lombok.Data;

/**
 * 测试步骤VO
 */
@Data
public class TestStepVO {
    
    private Long id;
    
    private Long testCaseId;
    
    private Integer stepOrder;
    
    private String stepDescription;
    
    private String expectedResult;
}
