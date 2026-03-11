package com.aiatg.service.impl;

import com.aiatg.entity.OperationLog;
import com.aiatg.entity.TestExecutionDetail;
import com.aiatg.mapper.OperationLogMapper;
import com.aiatg.mapper.ProjectMapper;
import com.aiatg.mapper.TestCaseMapper;
import com.aiatg.mapper.TestExecutionDetailMapper;
import com.aiatg.service.DashboardService;
import com.aiatg.vo.DashboardVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 仪表盘服务实现类
 */
@Slf4j
@Service
public class DashboardServiceImpl implements DashboardService {
    
    @Autowired
    private ProjectMapper projectMapper;
    
    @Autowired
    private TestCaseMapper testCaseMapper;
    
    @Autowired
    private TestExecutionDetailMapper testExecutionDetailMapper;
    
    @Autowired
    private OperationLogMapper operationLogMapper;
    
    @Override
    public DashboardVO getDashboardData(String period) {
        return DashboardVO.builder()
                .stats(getStatsData())
                .trendData(getTrendData(period))
                .statusDistribution(getStatusDistribution())
                .recentActivities(getRecentActivities())
                .build();
    }
    
    /**
     * 获取统计数据
     */
    private DashboardVO.StatsData getStatsData() {
        // 项目总数
        Long projectCount = projectMapper.selectCount(null);
        
        // 测试用例总数
        Long testCaseCount = testCaseMapper.selectCount(null);
        
        // 执行次数（统计所有执行详情记录）
        Long executionCount = testExecutionDetailMapper.selectCount(null);
        
        // 通过率计算（status=1 表示通过）
        QueryWrapper<TestExecutionDetail> passWrapper = new QueryWrapper<>();
        passWrapper.eq("status", 1);
        Long passCount = testExecutionDetailMapper.selectCount(passWrapper);
        
        Double passRate = 0.0;
        if (executionCount > 0) {
            passRate = Math.round((passCount * 10000.0 / executionCount)) / 100.0;
        }
        
        return DashboardVO.StatsData.builder()
                .projectCount(projectCount)
                .testCaseCount(testCaseCount)
                .executionCount(executionCount)
                .passRate(passRate)
                .build();
    }
    
    /**
     * 获取趋势数据
     */
    private DashboardVO.TrendData getTrendData(String period) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime;
        List<String> dates = new ArrayList<>();
        DateTimeFormatter formatter;
        
        // 根据周期确定开始时间和日期格式
        switch (period) {
            case "week":
                startTime = endTime.minusDays(6);
                formatter = DateTimeFormatter.ofPattern("MM-dd");
                for (int i = 0; i < 7; i++) {
                    LocalDateTime date = startTime.plusDays(i);
                    dates.add(date.format(formatter));
                }
                break;
            case "month":
                startTime = endTime.minusDays(29);
                formatter = DateTimeFormatter.ofPattern("MM-dd");
                for (int i = 0; i < 30; i++) {
                    LocalDateTime date = startTime.plusDays(i);
                    dates.add(date.format(formatter));
                }
                break;
            case "year":
                startTime = endTime.minusMonths(11);
                formatter = DateTimeFormatter.ofPattern("yyyy-MM");
                for (int i = 0; i < 12; i++) {
                    LocalDateTime date = startTime.plusMonths(i);
                    dates.add(date.format(formatter));
                }
                break;
            default:
                startTime = endTime.minusDays(6);
                formatter = DateTimeFormatter.ofPattern("MM-dd");
                for (int i = 0; i < 7; i++) {
                    LocalDateTime date = startTime.plusDays(i);
                    dates.add(date.format(formatter));
                }
        }
        
        // 查询该时间段内的所有执行详情（使用 start_time 字段）
        QueryWrapper<TestExecutionDetail> wrapper = new QueryWrapper<>();
        wrapper.ge("start_time", startTime)
               .le("start_time", endTime);
        List<TestExecutionDetail> details = testExecutionDetailMapper.selectList(wrapper);
        
        // 初始化数据数组
        List<Long> executionCounts = new ArrayList<>();
        List<Long> passCounts = new ArrayList<>();
        List<Long> failCounts = new ArrayList<>();
        
        for (int i = 0; i < dates.size(); i++) {
            executionCounts.add(0L);
            passCounts.add(0L);
            failCounts.add(0L);
        }
        
        // 按日期分组统计
        for (TestExecutionDetail detail : details) {
            LocalDateTime startTimeDetail = detail.getStartTime();
            if (startTimeDetail == null) {
                continue;
            }
            
            int index = getDateIndex(startTime, startTimeDetail, period);
            
            if (index >= 0 && index < dates.size()) {
                executionCounts.set(index, executionCounts.get(index) + 1);
                
                Integer status = detail.getStatus();
                if (status != null) {
                    if (status == 1) { // 通过
                        passCounts.set(index, passCounts.get(index) + 1);
                    } else if (status == 2) { // 失败
                        failCounts.set(index, failCounts.get(index) + 1);
                    }
                }
            }
        }
        
        return DashboardVO.TrendData.builder()
                .dates(dates)
                .executionCounts(executionCounts)
                .passCounts(passCounts)
                .failCounts(failCounts)
                .build();
    }
    
    /**
     * 计算日期在数组中的索引
     */
    private int getDateIndex(LocalDateTime startTime, LocalDateTime currentTime, String period) {
        switch (period) {
            case "week":
            case "month":
                return (int) ChronoUnit.DAYS.between(
                        startTime.toLocalDate(), 
                        currentTime.toLocalDate()
                );
            case "year":
                return (int) ChronoUnit.MONTHS.between(
                        startTime.toLocalDate().withDayOfMonth(1), 
                        currentTime.toLocalDate().withDayOfMonth(1)
                );
            default:
                return (int) ChronoUnit.DAYS.between(
                        startTime.toLocalDate(), 
                        currentTime.toLocalDate()
                );
        }
    }
    
    /**
     * 获取状态分布数据
     * 状态码: 1-通过, 2-失败, 3-阻塞, 4-跳过
     */
    private DashboardVO.StatusDistribution getStatusDistribution() {
        QueryWrapper<TestExecutionDetail> passWrapper = new QueryWrapper<>();
        passWrapper.eq("status", 1);
        Long passCount = testExecutionDetailMapper.selectCount(passWrapper);
        
        QueryWrapper<TestExecutionDetail> failWrapper = new QueryWrapper<>();
        failWrapper.eq("status", 2);
        Long failCount = testExecutionDetailMapper.selectCount(failWrapper);
        
        QueryWrapper<TestExecutionDetail> blockWrapper = new QueryWrapper<>();
        blockWrapper.eq("status", 3);
        Long blockCount = testExecutionDetailMapper.selectCount(blockWrapper);
        
        QueryWrapper<TestExecutionDetail> skipWrapper = new QueryWrapper<>();
        skipWrapper.eq("status", 4);
        Long skipCount = testExecutionDetailMapper.selectCount(skipWrapper);
        
        return DashboardVO.StatusDistribution.builder()
                .passCount(passCount)
                .failCount(failCount)
                .blockCount(blockCount)
                .skipCount(skipCount)
                .build();
    }
    
    /**
     * 获取最近活动
     */
    private List<DashboardVO.RecentActivity> getRecentActivities() {
        QueryWrapper<OperationLog> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("created_time")
               .last("LIMIT 10");
        List<OperationLog> logs = operationLogMapper.selectList(wrapper);
        
        return logs.stream().map(log -> {
            String timeAgo = getTimeAgo(log.getCreatedTime());
            String type = getActivityType(log.getOperation());
            
            // 组合操作描述
            String content = String.format("用户 %s %s", 
                    log.getUsername() != null ? log.getUsername() : "未知", 
                    log.getOperation() != null ? log.getOperation() : "执行了操作");
            
            return DashboardVO.RecentActivity.builder()
                    .id(log.getId())
                    .content(content)
                    .time(timeAgo)
                    .type(type)
                    .build();
        }).collect(Collectors.toList());
    }
    
    /**
     * 计算相对时间
     */
    private String getTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "未知时间";
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        long minutes = ChronoUnit.MINUTES.between(dateTime, now);
        if (minutes < 1) {
            return "刚刚";
        }
        if (minutes < 60) {
            return minutes + " 分钟前";
        }
        
        long hours = ChronoUnit.HOURS.between(dateTime, now);
        if (hours < 24) {
            return hours + " 小时前";
        }
        
        long days = ChronoUnit.DAYS.between(dateTime, now);
        if (days < 30) {
            return days + " 天前";
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return dateTime.format(formatter);
    }
    
    /**
     * 根据操作类型返回活动类型
     */
    private String getActivityType(String operation) {
        if (operation == null) {
            return "info";
        }
        
        if (operation.contains("创建") || operation.contains("新增")) {
            return "primary";
        }
        if (operation.contains("执行") || operation.contains("生成")) {
            return "success";
        }
        if (operation.contains("删除")) {
            return "danger";
        }
        if (operation.contains("更新") || operation.contains("修改")) {
            return "warning";
        }
        
        return "info";
    }
}
