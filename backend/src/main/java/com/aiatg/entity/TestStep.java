package com.aiatg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 测试步骤实体类
 */
@Data
@TableName("test_step")
public class TestStep {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long testCaseId;
    
    private Integer stepOrder;
    
    private String stepDescription;
    
    private String expectedResult;
    
    private LocalDateTime createdTime;
    
    private LocalDateTime updatedTime;
}
