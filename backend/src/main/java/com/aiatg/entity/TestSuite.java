package com.aiatg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 测试套件实体类
 */
@Data
@TableName("test_suite")
public class TestSuite {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long projectId;
    
    private String name;
    
    private String description;
    
    private Integer status;
    
    private Long createdBy;
    
    private LocalDateTime createdTime;
    
    private LocalDateTime updatedTime;
}
