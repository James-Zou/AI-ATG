package com.aiatg.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * API Key 实体类
 * 用于第三方 API 认证
 */
@Data
@TableName("api_key")
public class ApiKey implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    @TableField("user_id")
    private String userId;
    
    @TableField("account_id")
    private String accountId;
    
    @TableField("api_key")
    private String apiKey;
    
    @TableField("secret_key")
    private String secretKey;
    
    @TableField("app_name")
    private String appName;
    
    @TableField("description")
    private String description;
    
    @TableField("status")
    private Integer status;
    
    @TableField("expire_time")
    private LocalDateTime expireTime;
    
    @TableField("last_used_time")
    private LocalDateTime lastUsedTime;
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    @TableField("update_time")
    private LocalDateTime updateTime;
}
