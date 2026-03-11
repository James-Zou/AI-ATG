package com.aiatg.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * GitLab配置VO
 */
@Data
public class GitlabConfigVO {
    
    private Long id;
    
    private Long projectId;
    
    private String projectName;
    
    private String gitlabUrl;
    
    private String gitlabToken;
    
    private String webhookSecret;
    
    private String repositoryUrl;
    
    private String defaultBranch;
    
    private Integer autoTrigger;
    
    private Integer status;
    
    private String webhookUrl;
    
    private Long createdBy;
    
    private String createdByName;
    
    private LocalDateTime createdTime;
    
    private LocalDateTime updatedTime;
}
