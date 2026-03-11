package com.aiatg.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 需求VO
 */
@Data
public class RequirementVO {
    
    private Long id;
    
    private Long projectId;
    
    private String projectName;
    
    private String title;
    
    private String content;
    
    private String type;
    
    private String priority;
    
    private String source;
    
    private String sourceId;
    
    private List<String> attachmentUrls;
    
    private String status;
    
    private Long createdBy;
    
    private String createdByName;
    
    private LocalDateTime createdTime;
    
    private LocalDateTime updatedTime;
}
