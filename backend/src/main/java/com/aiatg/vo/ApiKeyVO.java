package com.aiatg.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * API Key 响应 VO
 */
@Data
public class ApiKeyVO {
    
    private String id;
    
    private String apiKey;
    
    private String appName;
    
    private String description;
    
    private Integer status;
    
    private String statusText;
    
    private LocalDateTime expireTime;
    
    private LocalDateTime lastUsedTime;
    
    private LocalDateTime createTime;
    
    /**
     * 是否已过期
     */
    private Boolean expired;
    
    /**
     * 脱敏后的 Secret Key（仅在生成时返回完整值）
     */
    private String secretKey;
}
