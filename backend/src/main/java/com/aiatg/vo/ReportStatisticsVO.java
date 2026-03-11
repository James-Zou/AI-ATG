package com.aiatg.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 报告统计VO
 */
@Data
public class ReportStatisticsVO {
    
    /**
     * 总执行次数
     */
    private Integer totalExecutions;
    
    /**
     * 总用例数
     */
    private Integer totalCases;
    
    /**
     * 总通过数
     */
    private Integer totalPassed;
    
    /**
     * 总失败数
     */
    private Integer totalFailed;
    
    /**
     * 平均通过率
     */
    private Double avgPassRate;
    
    /**
     * 趋势数据（按日期）
     */
    private List<TrendDataVO> trendData;
    
    /**
     * 用例分布（按类型）
     */
    private Map<String, Integer> caseDistribution;
    
    /**
     * 失败用例Top10
     */
    private List<FailedCaseVO> topFailedCases;
}
