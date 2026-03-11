package com.aiatg.vo;

import lombok.Data;

/**
 * 失败用例VO
 */
@Data
public class FailedCaseVO {
    
    private Long testCaseId;
    
    private String testCaseTitle;
    
    private Integer failedCount;
    
    private String lastErrorMessage;
}
