package com.aiatg.dto;

import lombok.Data;

/**
 * 代理注册DTO
 */
@Data
public class AgentRegisterDTO {
    
    private String agentId;
    
    private String agentName;
    
    private String hostname;
    
    private String os;
    
    private String osVersion;
    
    private String browser;
    
    private String browserVersion;
    
    private String ip;
}
