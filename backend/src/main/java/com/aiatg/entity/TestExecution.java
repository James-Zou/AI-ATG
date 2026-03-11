package com.aiatg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 测试执行实体类
 */
@Data
@TableName("test_execution")
public class TestExecution {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long projectId;
    
    private Long suiteId;
    
    private String executionName;
    
    private String executionType;
    
    private String environment;
    
    private Integer totalCases;
    
    private Integer passedCases;
    
    private Integer failedCases;
    
    private Integer skippedCases;
    
    private Integer status;
    
    private Long duration;
    
    private String triggerType;
    
    private Long executedBy;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private LocalDateTime createdTime;
}
