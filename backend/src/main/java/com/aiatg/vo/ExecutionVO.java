package com.aiatg.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 执行VO
 */
@Data
public class ExecutionVO {
    
    private Long id;
    
    private Long projectId;
    
    private String projectName;
    
    private Long suiteId;
    
    private String suiteName;
    
    private String executionName;
    
    private String executionType;
    
    private String environment;
    
    private Integer totalCases;
    
    private Integer passedCases;
    
    private Integer failedCases;
    
    private Integer skippedCases;
    
    private Integer status;
    
    private Long duration;
    
    private String triggerType;
    
    private Long executedBy;
    
    private String executedByName;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private LocalDateTime createdTime;
    
    private List<ExecutionDetailVO> details;
}
