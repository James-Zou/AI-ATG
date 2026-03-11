package com.aiatg.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 测试用例VO
 */
@Data
public class TestCaseVO {
    
    private Long id;
    
    private Long projectId;
    
    private String projectName;
    
    private Long requirementId;
    
    private String requirementTitle;
    
    private Long suiteId;
    
    private String suiteName;
    
    private String caseNo;
    
    private String title;
    
    private String preconditions;
    
    private String type;
    
    private String priority;
    
    private String status;
    
    private String source;
    
    private Integer expectedResult;
    
    private Long createdBy;
    
    private String createdByName;
    
    private LocalDateTime createdTime;
    
    private LocalDateTime updatedTime;
    
    private List<TestStepVO> steps;
    
    private String stepsJson;
}
