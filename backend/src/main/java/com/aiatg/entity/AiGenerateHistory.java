package com.aiatg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI生成历史实体类
 */
@Data
@TableName("ai_generate_history")
public class AiGenerateHistory {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long requirementId;
    
    private String provider;
    
    private String modelName;
    
    private String prompt;
    
    private String response;
    
    private Integer generatedCount;
    
    private Integer tokens;
    
    private Long duration;
    
    private Integer status;
    
    private String errorMessage;
    
    private Long createdBy;
    
    private LocalDateTime createdTime;
}
