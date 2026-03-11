package com.aiatg.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 报告详情VO
 */
@Data
public class ReportDetailVO {
    
    private Long id;
    
    private Long executionId;
    
    private Long projectId;
    
    private String projectName;
    
    private String reportName;
    
    private String reportType;
    
    private Integer totalCases;
    
    private Integer passedCases;
    
    private Integer failedCases;
    
    private Integer skippedCases;
    
    private Double passRate;
    
    private Long duration;
    
    private String summary;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private Long createdBy;
    
    private String createdByName;
    
    private LocalDateTime createdTime;
    
    /**
     * 执行明细列表
     */
    private List<ExecutionDetailVO> executionDetails;
    
    /**
     * 统计数据
     */
    private Map<String, Object> statistics;
}
