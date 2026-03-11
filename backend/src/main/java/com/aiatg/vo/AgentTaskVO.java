package com.aiatg.vo;

import lombok.Data;

/**
 * 代理任务VO
 */
@Data
public class AgentTaskVO {
    
    private Long detailId;
    
    private Long executionId;
    
    private Long testCaseId;
    
    private String testCaseTitle;
    
    private String testCaseType;
    
    private String steps;
    
    private String testData;
    
    private Integer timeout;
}
