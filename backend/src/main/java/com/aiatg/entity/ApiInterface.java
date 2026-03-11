package com.aiatg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * API接口实体
 */
@Data
@TableName("api_interface")
public class ApiInterface {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long projectId;
    
    private String interfaceName;
    
    private String method;
    
    private String url;
    
    private String description;
    
    private String headers;
    
    private String params;
    
    private String body;
    
    private String bodyType;
    
    private Integer timeout;
    
    private String authType;
    
    private String authConfig;
    
    private String preRequestScript;
    
    private String postResponseScript;
    
    private String status;
    
    private String category;
    
    private String tags;
    
    private Long createdBy;
    
    private LocalDateTime createdTime;
    
    private Long updatedBy;
    
    private LocalDateTime updatedTime;
}
