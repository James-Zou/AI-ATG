package com.aiatg.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * GitLab Webhook载荷DTO
 */
@Data
public class WebhookPayload {
    
    private String objectKind;
    
    private String eventName;
    
    private String ref;
    
    private String before;
    
    private String after;
    
    private Long projectId;
    
    private Map<String, Object> project;
    
    private List<Map<String, Object>> commits;
    
    private Map<String, Object> repository;
    
    /**
     * 获取分支名称
     */
    public String getBranchName() {
        if (ref != null && ref.startsWith("refs/heads/")) {
            return ref.substring("refs/heads/".length());
        }
        return ref;
    }
    
    /**
     * 获取最后一次提交
     */
    public Map<String, Object> getLastCommit() {
        if (commits != null && !commits.isEmpty()) {
            return commits.get(commits.size() - 1);
        }
        return null;
    }
}
