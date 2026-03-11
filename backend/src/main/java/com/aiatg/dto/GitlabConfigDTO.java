package com.aiatg.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * GitLab配置DTO
 */
@Data
public class GitlabConfigDTO {
    
    @NotNull(message = "项目ID不能为空")
    private Long projectId;
    
    @NotBlank(message = "GitLab地址不能为空")
    private String gitlabUrl;
    
    @NotBlank(message = "GitLab Token不能为空")
    private String gitlabToken;
    
    private String webhookSecret;
    
    @NotBlank(message = "仓库地址不能为空")
    private String repositoryUrl;
    
    private String defaultBranch = "main";
    
    private Integer autoTrigger = 1;
    
    private Integer status = 1;
}
