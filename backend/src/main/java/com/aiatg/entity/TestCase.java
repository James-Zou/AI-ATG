package com.aiatg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 测试用例实体类
 */
@Data
@TableName("test_case")
public class TestCase {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long projectId;
    
    private Long requirementId;
    
    private String caseNo;
    
    private String title;
    
    private String preconditions;
    
    private String steps;
    
    private String type;
    
    private String priority;
    
    private String level;
    
    private String expectedResult;
    
    private String testData;
    
    private String tags;
    
    private String status;
    
    private String source;
    
    private String aiModel;
    
    private Long aiGenerationId;
    
    private Long assignee;
    
    private Long createdBy;
    
    private LocalDateTime createdTime;
    
    private LocalDateTime updatedTime;
}
