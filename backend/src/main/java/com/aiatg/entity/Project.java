package com.aiatg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目实体类
 */
@Data
@TableName("project")
public class Project {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private String description;
    
    private Integer gitlabProjectId;
    
    private String gitlabWebhookUrl;
    
    private Integer status;
    
    private Long createdBy;
    
    private LocalDateTime createdTime;
    
    private LocalDateTime updatedTime;
}
