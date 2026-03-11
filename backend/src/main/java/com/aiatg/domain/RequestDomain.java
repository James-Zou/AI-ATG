package com.aiatg.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 请求域对象，用于存储当前请求的用户信息和上下文信息
 * @author roderick.zou
 * @date 2026/2/5
 */
@Data
public class RequestDomain implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 租户ID
     */
    private String accountId;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 语言设置
     */
    private String language;
    
    /**
     * 顶级租户ID
     */
    private String topAccountId;
    
    /**
     * 请求时间戳
     */
    private Long reqTime;
    
    /**
     * 追踪ID，用于日志追踪
     */
    private String traceId;
}
