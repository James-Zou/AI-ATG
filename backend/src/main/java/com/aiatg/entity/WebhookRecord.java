package com.aiatg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Webhook记录实体类
 */
@Data
@TableName("webhook_record")
public class WebhookRecord {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long projectId;
    
    private String eventType;
    
    private String objectKind;
    
    private String ref;
    
    private String commitId;
    
    private String commitMessage;
    
    private String commitAuthor;
    
    private String diffContent;
    
    private Integer fileCount;
    
    private Integer status;
    
    private String errorMessage;
    
    private Integer generatedCases;
    
    private LocalDateTime receivedTime;
    
    private LocalDateTime processedTime;
}
