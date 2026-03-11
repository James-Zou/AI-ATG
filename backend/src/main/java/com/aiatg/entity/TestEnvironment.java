package com.aiatg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 测试环境实体
 */
@Data
@TableName("test_environment")
public class TestEnvironment {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long projectId;
    
    private String envName;
    
    private String envCode;
    
    private String baseUrl;
    
    private String description;
    
    private Integer status;
    
    private LocalDateTime createdTime;
    
    private LocalDateTime updatedTime;
    
    private Long createdBy;
}
