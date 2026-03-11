package com.aiatg.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Webhook记录VO
 */
@Data
public class WebhookRecordVO {
    
    private Long id;
    
    private Long projectId;
    
    private String projectName;
    
    private String eventType;
    
    private String objectKind;
    
    private String ref;
    
    private String commitId;
    
    private String commitMessage;
    
    private String commitAuthor;
    
    private Integer fileCount;
    
    private Integer status;
    
    private String statusLabel;
    
    private String errorMessage;
    
    private Integer generatedCases;
    
    private LocalDateTime receivedTime;
    
    private LocalDateTime processedTime;
    
    private Long processingTime;
}
