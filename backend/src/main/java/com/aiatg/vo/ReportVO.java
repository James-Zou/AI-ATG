package com.aiatg.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报告VO
 */
@Data
public class ReportVO {
    
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
    
    private String htmlUrl;
    
    private String pdfUrl;
    
    private Long createdBy;
    
    private String createdByName;
    
    private LocalDateTime createdTime;
    
    /**
     * 是否需要人工确认：0-不需要，1-需要
     */
    private Integer needConfirm;
    
    /**
     * 确认状态：0-待确认，1-已确认通过，2-已确认失败
     */
    private Integer confirmStatus;
    
    /**
     * 确认状态文本
     */
    private String confirmStatusText;
    
    /**
     * 确认人ID
     */
    private Long confirmedBy;
    
    /**
     * 确认人姓名
     */
    private String confirmedByName;
    
    /**
     * 确认时间
     */
    private LocalDateTime confirmedTime;
    
    /**
     * 确认备注
     */
    private String confirmRemark;
}
