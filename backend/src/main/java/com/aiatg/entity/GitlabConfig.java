package com.aiatg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * GitLab配置实体类
 */
@Data
@TableName("gitlab_config")
public class GitlabConfig {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long projectId;
    
    private String gitlabUrl;
    
    private String gitlabToken;
    
    private String webhookSecret;
    
    private String repositoryUrl;
    
    private String defaultBranch;
    
    private Integer autoTrigger;
    
    private Integer status;
    
    private Long createdBy;
    
    private LocalDateTime createdTime;
    
    private LocalDateTime updatedTime;
}
