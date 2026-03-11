package com.aiatg.dto;

import lombok.Data;

import java.util.Map;

/**
 * API接口DTO
 */
@Data
public class ApiInterfaceDTO {
    
    private Long id;
    
    private Long projectId;
    
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
}
