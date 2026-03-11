package com.aiatg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 测试执行明细实体类
 */
@Data
@TableName("test_execution_detail")
public class TestExecutionDetail {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long executionId;
    
    private Long testCaseId;
    
    private Integer status;
    
    private String errorMessage;
    
    private String stackTrace;
    
    private String screenshotUrl;
    
    private String videoUrl;
    
    private String logs;
    
    private Long duration;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
}
