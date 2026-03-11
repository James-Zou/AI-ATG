package com.aiatg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI配置实体类
 */
@Data
@TableName("ai_config")
public class AiConfig {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String provider;
    
    private String modelName;
    
    private String apiKey;
    
    private String apiUrl;
    
    private Integer maxTokens;
    
    private Double temperature;
    
    private Integer status;
    
    private Integer isDefault;
    
    private LocalDateTime createdTime;
    
    private LocalDateTime updatedTime;
}
