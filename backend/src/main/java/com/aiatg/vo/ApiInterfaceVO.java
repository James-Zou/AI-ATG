package com.aiatg.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * API接口VO
 */
@Data
public class ApiInterfaceVO {
    
    private Long id;
    
    private Long projectId;
    
    private String projectName;
    
    private String interfaceName;
    
    private String method;
    
    private String url;
    
    private String description;
    
    private Map<String, String> headers;
    
    private Map<String, String> params;
    
    private String body;
    
    private String bodyType;
    
    private Integer timeout;
    
    private String authType;
    
    private Map<String, String> authConfig;
    
    private String preRequestScript;
    
    private String postResponseScript;
    
    private String status;
    
    private String category;
    
    private String tags;
    
    private Long createdBy;
    
    private String createdByName;
    
    private LocalDateTime createdTime;
    
    private Long updatedBy;
    
    private String updatedByName;
    
    private LocalDateTime updatedTime;
}
