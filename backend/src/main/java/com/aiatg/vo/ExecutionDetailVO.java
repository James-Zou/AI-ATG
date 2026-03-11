package com.aiatg.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 执行明细VO
 */
@Data
public class ExecutionDetailVO {
    
    private Long id;
    
    private Long executionId;
    
    private Long testCaseId;
    
    private String testCaseTitle;
    
    private Integer status;
    
    private String errorMessage;
    
    private String stackTrace;
    
    private String screenshotUrl;
    
    private String videoUrl;
    
    private String logs;
    
    private Long duration;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
}
