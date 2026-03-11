package com.aiatg.vo;

import lombok.Data;

/**
 * 套件用例视图对象
 */
@Data
public class SuiteCaseVO {
    
    private Long id;
    
    private Long caseId;
    
    private String caseTitle;
    
    private String caseType;
    
    private String casePriority;
    
    private String caseStatus;
    
    private Integer executeOrder;
}
