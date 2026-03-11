package com.aiatg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 需求实体类
 */
@Data
@TableName("requirement")
public class Requirement {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long projectId;
    
    private String title;
    
    private String content;
    
    private String type;
    
    private String priority;
    
    private String source;
    
    private String sourceId;
    
    private String attachmentUrls;
    
    private String status;
    
    private Long createdBy;
    
    private LocalDateTime createdTime;
    
    private LocalDateTime updatedTime;
}
