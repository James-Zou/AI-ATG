package com.aiatg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 提示词模板实体类
 */
@Data
@TableName("prompt_template")
public class PromptTemplate {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private String description;
    
    private String templateType;
    
    private String content;
    
    private Integer status;
    
    private Integer isDefault;
    
    private Long createdBy;
    
    private LocalDateTime createdTime;
    
    private LocalDateTime updatedTime;
}
