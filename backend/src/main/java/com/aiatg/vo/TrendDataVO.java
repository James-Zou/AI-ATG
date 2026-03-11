package com.aiatg.vo;

import lombok.Data;

/**
 * 趋势数据VO
 */
@Data
public class TrendDataVO {
    
    private String date;
    
    private Integer totalCases;
    
    private Integer passedCases;
    
    private Integer failedCases;
    
    private Double passRate;
}
