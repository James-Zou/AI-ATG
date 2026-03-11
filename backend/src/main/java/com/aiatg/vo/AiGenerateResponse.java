package com.aiatg.vo;

import lombok.Data;

import java.util.List;

/**
 * AI生成响应VO
 */
@Data
public class AiGenerateResponse {
    
    private Long historyId;
    
    private Integer generatedCount;
    
    private List<TestCaseVO> testCases;
    
    private Long duration;
    
    private Integer tokens;
    
    private String provider;
    
    private String modelName;
}
