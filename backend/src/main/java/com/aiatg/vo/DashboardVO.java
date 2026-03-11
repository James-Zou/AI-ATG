package com.aiatg.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 仪表盘统计数据VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardVO {
    
    /**
     * 统计数据
     */
    private StatsData stats;
    
    /**
     * 趋势数据
     */
    private TrendData trendData;
    
    /**
     * 状态分布数据
     */
    private StatusDistribution statusDistribution;
    
    /**
     * 最近活动
     */
    private List<RecentActivity> recentActivities;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatsData {
        private Long projectCount;
        private Long testCaseCount;
        private Long executionCount;
        private Double passRate;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendData {
        private List<String> dates;
        private List<Long> executionCounts;
        private List<Long> passCounts;
        private List<Long> failCounts;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusDistribution {
        private Long passCount;
        private Long failCount;
        private Long blockCount;
        private Long skipCount;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentActivity {
        private Long id;
        private String content;
        private String time;
        private String type;
    }
}
