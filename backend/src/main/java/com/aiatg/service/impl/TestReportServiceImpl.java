package com.aiatg.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.aiatg.common.PageResult;
import com.aiatg.dto.ReportQueryDTO;
import com.aiatg.entity.*;
import com.aiatg.mapper.*;
import com.aiatg.service.TestReportService;
import com.aiatg.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 测试报告服务实现类
 */
@Slf4j
@Service
public class TestReportServiceImpl implements TestReportService {
    
    @Autowired
    private TestReportMapper reportMapper;
    
    @Autowired
    private TestExecutionMapper executionMapper;
    
    @Autowired
    private TestExecutionDetailMapper detailMapper;
    
    @Autowired
    private TestCaseMapper testCaseMapper;
    
    @Autowired
    private ProjectMapper projectMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    @Transactional
    public ReportVO generateReport(Long executionId, String reportType, Long userId) {
        // 获取执行记录
        TestExecution execution = executionMapper.selectById(executionId);
        if (execution == null) {
            throw new RuntimeException("执行记录不存在");
        }
        
        // 创建报告
        TestReport report = new TestReport();
        report.setExecutionId(executionId);
        report.setReportName("测试报告-" + DateUtil.now());
        report.setReportType(reportType != null ? reportType : "html");
        report.setGeneratedBy(userId);
        report.setCreatedTime(LocalDateTime.now());
        
        // 生成报告摘要（summary 列为 JSON 类型，需写入合法 JSON）
        String summaryText = generateSummary(execution);
        report.setSummary(toSummaryJson(summaryText));
        
        // 设置需要人工确认
        report.setNeedConfirm(1);
        report.setConfirmStatus(0); // 待确认
        
        reportMapper.insert(report);
        
        log.info("测试报告已生成: reportId={}, executionId={}, 待人工确认", report.getId(), executionId);
        
        return convertToVO(report);
    }
    
    @Override
    public ReportDetailVO getReportDetail(Long id) {
        TestReport report = reportMapper.selectById(id);
        if (report == null) {
            throw new RuntimeException("报告不存在");
        }
        
        ReportDetailVO vo = new ReportDetailVO();
        BeanUtil.copyProperties(report, vo);
        vo.setSummary(summaryJsonToText(report.getSummary()));
        
        // 获取执行信息
        TestExecution execution = executionMapper.selectById(report.getExecutionId());
        if (execution != null) {
            vo.setExecutionId(execution.getId());
            vo.setProjectId(execution.getProjectId());
            vo.setTotalCases(execution.getTotalCases());
            vo.setPassedCases(execution.getPassedCases());
            vo.setFailedCases(execution.getFailedCases());
            vo.setSkippedCases(execution.getSkippedCases());
            vo.setDuration(execution.getDuration());
            vo.setStartTime(execution.getStartTime());
            vo.setEndTime(execution.getEndTime());
            
            // 计算通过率
            if (execution.getTotalCases() != null && execution.getTotalCases() > 0) {
                double passRate = (execution.getPassedCases() != null ? execution.getPassedCases() : 0) * 100.0 / execution.getTotalCases();
                vo.setPassRate(Math.round(passRate * 100.0) / 100.0);
            }
            
            // 获取项目名称
            if (execution.getProjectId() != null) {
                Project project = projectMapper.selectById(execution.getProjectId());
                if (project != null) {
                    vo.setProjectName(project.getName());
                }
            }
            
            // 获取执行明细
            LambdaQueryWrapper<TestExecutionDetail> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TestExecutionDetail::getExecutionId, execution.getId());
            List<TestExecutionDetail> details = detailMapper.selectList(wrapper);
            
            List<ExecutionDetailVO> detailVOs = details.stream()
                .map(this::convertDetailToVO)
                .collect(Collectors.toList());
            vo.setExecutionDetails(detailVOs);
            
            // 生成统计数据
            Map<String, Object> statistics = generateStatistics(execution, details);
            vo.setStatistics(statistics);
        }
        
        // 获取创建人名称
        if (report.getGeneratedBy() != null) {
            User user = userMapper.selectById(report.getGeneratedBy());
            if (user != null) {
                vo.setCreatedByName(user.getNickname() != null ? user.getNickname() : user.getUsername());
            }
        }
        
        return vo;
    }
    
    @Override
    public PageResult<ReportVO> getReportList(ReportQueryDTO queryDTO) {
        log.info("查询报告列表，参数: projectId={}, reportType={}, pageNum={}, pageSize={}", 
                queryDTO.getProjectId(), queryDTO.getReportType(), queryDTO.getPageNum(), queryDTO.getPageSize());
        
        LambdaQueryWrapper<TestReport> wrapper = new LambdaQueryWrapper<>();
        
        // 如果指定项目，需要关联查询
        if (queryDTO.getProjectId() != null) {
            log.info("查询projectId={}的执行记录", queryDTO.getProjectId());
            List<TestExecution> executions = executionMapper.selectList(
                new LambdaQueryWrapper<TestExecution>()
                    .eq(TestExecution::getProjectId, queryDTO.getProjectId())
            );
            
            log.info("找到{}条执行记录", executions.size());
            
            if (!executions.isEmpty()) {
                List<Long> executionIds = executions.stream()
                    .map(TestExecution::getId)
                    .collect(Collectors.toList());
                log.info("执行ID列表: {}", executionIds);
                wrapper.in(TestReport::getExecutionId, executionIds);
            } else {
                // 没有执行记录，返回空结果
                log.warn("没有找到projectId={}的执行记录，返回空列表", queryDTO.getProjectId());
                return new PageResult<>(0L, new ArrayList<>(), queryDTO.getPageNum(), queryDTO.getPageSize());
            }
        }
        
        // 只在reportType有实际值时才添加过滤条件
        if (StringUtils.hasText(queryDTO.getReportType())) {
            wrapper.eq(TestReport::getReportType, queryDTO.getReportType());
        }
        
        if (queryDTO.getStartTime() != null) {
            wrapper.ge(TestReport::getCreatedTime, queryDTO.getStartTime());
        }
        
        if (queryDTO.getEndTime() != null) {
            wrapper.le(TestReport::getCreatedTime, queryDTO.getEndTime());
        }
        
        // 按确认状态过滤
        if (queryDTO.getConfirmStatus() != null) {
            wrapper.eq(TestReport::getConfirmStatus, queryDTO.getConfirmStatus());
        }
        
        wrapper.orderByDesc(TestReport::getCreatedTime);
        
        log.info("开始查询test_report表，pageNum={}, pageSize={}", queryDTO.getPageNum(), queryDTO.getPageSize());
        
        Page<TestReport> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<TestReport> resultPage = reportMapper.selectPage(page, wrapper);
        
        log.info("查询结果：total={}, records={}", resultPage.getTotal(), resultPage.getRecords().size());
        
        List<ReportVO> voList = resultPage.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        log.info("转换VO完成，返回{}条记录", voList.size());
        
        return new PageResult<>(
            resultPage.getTotal(),
            voList,
            queryDTO.getPageNum(),
            queryDTO.getPageSize()
        );
    }
    
    @Override
    public String exportHtml(Long reportId) {
        ReportDetailVO report = getReportDetail(reportId);
        
        // 生成HTML报告
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("<meta charset='UTF-8'>\n");
        html.append("<title>").append(report.getReportName()).append("</title>\n");
        html.append("<style>\n");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; }\n");
        html.append("h1 { color: #333; }\n");
        html.append("table { border-collapse: collapse; width: 100%; margin: 20px 0; }\n");
        html.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n");
        html.append("th { background-color: #4CAF50; color: white; }\n");
        html.append(".summary { background-color: #f9f9f9; padding: 15px; margin: 20px 0; }\n");
        html.append(".pass { color: #67C23A; }\n");
        html.append(".fail { color: #F56C6C; }\n");
        html.append("</style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        
        // 标题
        html.append("<h1>").append(report.getReportName()).append("</h1>\n");
        
        // 摘要
        html.append("<div class='summary'>\n");
        html.append("<h2>测试摘要</h2>\n");
        html.append("<p>项目：").append(report.getProjectName()).append("</p>\n");
        html.append("<p>总用例数：").append(report.getTotalCases()).append("</p>\n");
        html.append("<p class='pass'>通过：").append(report.getPassedCases()).append("</p>\n");
        html.append("<p class='fail'>失败：").append(report.getFailedCases()).append("</p>\n");
        html.append("<p>通过率：").append(report.getPassRate()).append("%</p>\n");
        html.append("<p>执行时长：").append(report.getDuration()).append("ms</p>\n");
        html.append("<p>开始时间：").append(report.getStartTime()).append("</p>\n");
        html.append("<p>结束时间：").append(report.getEndTime()).append("</p>\n");
        html.append("</div>\n");
        
        // 明细表格
        html.append("<h2>测试明细</h2>\n");
        html.append("<table>\n");
        html.append("<tr><th>用例</th><th>状态</th><th>耗时(ms)</th><th>错误信息</th></tr>\n");
        
        if (report.getExecutionDetails() != null) {
            for (ExecutionDetailVO detail : report.getExecutionDetails()) {
                html.append("<tr>");
                html.append("<td>").append(detail.getTestCaseTitle()).append("</td>");
                html.append("<td>").append(getStatusLabel(detail.getStatus())).append("</td>");
                html.append("<td>").append(detail.getDuration()).append("</td>");
                html.append("<td>").append(detail.getErrorMessage() != null ? detail.getErrorMessage() : "-").append("</td>");
                html.append("</tr>\n");
            }
        }
        
        html.append("</table>\n");
        html.append("</body>\n");
        html.append("</html>");
        
        return html.toString();
    }
    
    @Override
    public String exportPdf(Long reportId) {
        // PDF导出需要使用 iText 或其他PDF库
        // 这里返回一个提示，实际项目中需要集成PDF生成库
        throw new RuntimeException("PDF导出功能需要集成iText库，暂未实现");
    }
    
    @Override
    public ReportStatisticsVO getStatistics(Long projectId, String startDate, String endDate) {
        ReportStatisticsVO vo = new ReportStatisticsVO();
        
        // 构建查询条件
        LambdaQueryWrapper<TestExecution> wrapper = new LambdaQueryWrapper<>();
        
        if (projectId != null) {
            wrapper.eq(TestExecution::getProjectId, projectId);
        }
        
        if (startDate != null) {
            LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
            wrapper.ge(TestExecution::getCreatedTime, start);
        }
        
        if (endDate != null) {
            LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
            wrapper.le(TestExecution::getCreatedTime, end);
        }
        
        wrapper.eq(TestExecution::getStatus, 2); // 只统计已完成的
        
        List<TestExecution> executions = executionMapper.selectList(wrapper);
        
        // 统计总数
        vo.setTotalExecutions(executions.size());
        vo.setTotalCases(executions.stream().mapToInt(e -> e.getTotalCases() != null ? e.getTotalCases() : 0).sum());
        vo.setTotalPassed(executions.stream().mapToInt(e -> e.getPassedCases() != null ? e.getPassedCases() : 0).sum());
        vo.setTotalFailed(executions.stream().mapToInt(e -> e.getFailedCases() != null ? e.getFailedCases() : 0).sum());
        
        // 计算平均通过率
        if (vo.getTotalCases() > 0) {
            vo.setAvgPassRate(vo.getTotalPassed() * 100.0 / vo.getTotalCases());
        } else {
            vo.setAvgPassRate(0.0);
        }
        
        // 生成趋势数据
        List<TrendDataVO> trendData = generateTrendData(executions);
        vo.setTrendData(trendData);
        
        // 用例分布（按类型）
        Map<String, Integer> distribution = new HashMap<>();
        for (TestExecution execution : executions) {
            String type = execution.getExecutionType() != null ? execution.getExecutionType() : "unknown";
            distribution.put(type, distribution.getOrDefault(type, 0) + (execution.getTotalCases() != null ? execution.getTotalCases() : 0));
        }
        vo.setCaseDistribution(distribution);
        
        // Top失败用例
        List<FailedCaseVO> topFailed = getTopFailedCases(projectId);
        vo.setTopFailedCases(topFailed);
        
        return vo;
    }
    
    @Override
    @Transactional
    public void deleteReport(Long id) {
        TestReport report = reportMapper.selectById(id);
        if (report == null) {
            throw new RuntimeException("报告不存在");
        }
        
        reportMapper.deleteById(id);
    }
    
    @Override
    @Transactional
    public void confirmReport(Long reportId, Integer confirmStatus, String confirmRemark, Long userId) {
        log.info("开始人工确认报告: reportId={}, confirmStatus={}, userId={}", reportId, confirmStatus, userId);
        
        // 查询报告
        TestReport report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new RuntimeException("报告不存在");
        }
        
        // 检查报告是否需要确认
        if (report.getNeedConfirm() == null || report.getNeedConfirm() != 1) {
            throw new RuntimeException("该报告不需要人工确认");
        }
        
        // 检查报告是否已确认
        if (report.getConfirmStatus() != null && report.getConfirmStatus() != 0) {
            throw new RuntimeException("该报告已经确认过了");
        }
        
        // 验证确认状态
        if (confirmStatus != 1 && confirmStatus != 2) {
            throw new RuntimeException("确认状态无效，必须是1(通过)或2(失败)");
        }
        
        // 更新报告确认状态
        report.setConfirmStatus(confirmStatus);
        report.setConfirmedBy(userId);
        report.setConfirmedTime(LocalDateTime.now());
        report.setConfirmRemark(confirmRemark);
        
        int updateCount = reportMapper.updateById(report);
        
        log.info("报告确认完成: reportId={}, confirmStatus={}, updateCount={}", 
                reportId, confirmStatus == 1 ? "通过" : "失败", updateCount);
    }
    
    /**
     * 生成报告摘要
     */
    private String generateSummary(TestExecution execution) {
        StringBuilder summary = new StringBuilder();
        summary.append("执行了 ").append(execution.getTotalCases()).append(" 个测试用例，");
        summary.append("通过 ").append(execution.getPassedCases()).append(" 个，");
        summary.append("失败 ").append(execution.getFailedCases()).append(" 个。");
        
        if (execution.getTotalCases() != null && execution.getTotalCases() > 0) {
            double passRate = (execution.getPassedCases() != null ? execution.getPassedCases() : 0) * 100.0 / execution.getTotalCases();
            summary.append("通过率：").append(String.format("%.2f", passRate)).append("%");
        }
        
        return summary.toString();
    }

    /**
     * 将摘要文本转为 JSON 字符串（test_report.summary 列为 JSON 类型）
     */
    private String toSummaryJson(String text) {
        Map<String, Object> map = new HashMap<>();
        map.put("text", text != null ? text : "");
        try {
            return new ObjectMapper().writeValueAsString(map);
        } catch (JsonProcessingException e) {
            log.warn("摘要序列化为 JSON 失败，使用空对象: {}", e.getMessage());
            return "{\"text\":\"\"}";
        }
    }

    /**
     * 从 summary JSON 中解析出展示用文本（供 VO 返回给前端）
     */
    private String summaryJsonToText(String summaryJson) {
        if (summaryJson == null || summaryJson.trim().isEmpty()) {
            return "";
        }
        try {
            Map<?, ?> map = new ObjectMapper().readValue(summaryJson, Map.class);
            Object text = map != null ? map.get("text") : null;
            return text != null ? text.toString() : summaryJson;
        } catch (JsonProcessingException e) {
            return summaryJson;
        }
    }
    
    /**
     * 生成统计数据
     */
    private Map<String, Object> generateStatistics(TestExecution execution, List<TestExecutionDetail> details) {
        Map<String, Object> stats = new HashMap<>();
        
        // 状态分布
        Map<String, Integer> statusDist = new HashMap<>();
        statusDist.put("passed", 0);
        statusDist.put("failed", 0);
        statusDist.put("skipped", 0);
        
        for (TestExecutionDetail detail : details) {
            if (detail.getStatus() == 1) {
                statusDist.put("passed", statusDist.get("passed") + 1);
            } else if (detail.getStatus() == 2) {
                statusDist.put("failed", statusDist.get("failed") + 1);
            } else {
                statusDist.put("skipped", statusDist.get("skipped") + 1);
            }
        }
        stats.put("statusDistribution", statusDist);
        
        // 平均执行时间
        long avgDuration = (long) details.stream()
            .filter(d -> d.getDuration() != null)
            .mapToLong(TestExecutionDetail::getDuration)
            .average()
            .orElse(0);
        stats.put("avgDuration", avgDuration);
        
        return stats;
    }
    
    /**
     * 生成趋势数据
     */
    private List<TrendDataVO> generateTrendData(List<TestExecution> executions) {
        Map<String, TrendDataVO> trendMap = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (TestExecution execution : executions) {
            if (execution.getCreatedTime() == null) continue;
            
            String date = execution.getCreatedTime().format(formatter);
            TrendDataVO trend = trendMap.getOrDefault(date, new TrendDataVO());
            trend.setDate(date);
            
            int total = trend.getTotalCases() != null ? trend.getTotalCases() : 0;
            int passed = trend.getPassedCases() != null ? trend.getPassedCases() : 0;
            int failed = trend.getFailedCases() != null ? trend.getFailedCases() : 0;
            
            trend.setTotalCases(total + (execution.getTotalCases() != null ? execution.getTotalCases() : 0));
            trend.setPassedCases(passed + (execution.getPassedCases() != null ? execution.getPassedCases() : 0));
            trend.setFailedCases(failed + (execution.getFailedCases() != null ? execution.getFailedCases() : 0));
            
            if (trend.getTotalCases() > 0) {
                trend.setPassRate(trend.getPassedCases() * 100.0 / trend.getTotalCases());
            }
            
            trendMap.put(date, trend);
        }
        
        return new ArrayList<>(trendMap.values());
    }
    
    /**
     * 获取Top失败用例
     */
    private List<FailedCaseVO> getTopFailedCases(Long projectId) {
        // 查询所有失败的执行明细
        LambdaQueryWrapper<TestExecutionDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TestExecutionDetail::getStatus, 2); // 失败状态
        
        List<TestExecutionDetail> failedDetails = detailMapper.selectList(wrapper);
        
        // 按用例ID分组统计
        Map<Long, FailedCaseVO> failedMap = new HashMap<>();
        
        for (TestExecutionDetail detail : failedDetails) {
            Long caseId = detail.getTestCaseId();
            FailedCaseVO vo = failedMap.getOrDefault(caseId, new FailedCaseVO());
            vo.setTestCaseId(caseId);
            vo.setFailedCount(vo.getFailedCount() != null ? vo.getFailedCount() + 1 : 1);
            vo.setLastErrorMessage(detail.getErrorMessage());
            
            // 获取用例标题
            if (vo.getTestCaseTitle() == null) {
                TestCase testCase = testCaseMapper.selectById(caseId);
                if (testCase != null) {
                    vo.setTestCaseTitle(testCase.getTitle());
                }
            }
            
            failedMap.put(caseId, vo);
        }
        
        // 按失败次数排序，取前10
        return failedMap.values().stream()
            .sorted((a, b) -> b.getFailedCount().compareTo(a.getFailedCount()))
            .limit(10)
            .collect(Collectors.toList());
    }
    
    /**
     * 转换为VO
     */
    private ReportVO convertToVO(TestReport report) {
        ReportVO vo = new ReportVO();
        BeanUtil.copyProperties(report, vo);
        vo.setSummary(summaryJsonToText(report.getSummary()));
        
        // 获取执行信息
        if (report.getExecutionId() != null) {
            TestExecution execution = executionMapper.selectById(report.getExecutionId());
            if (execution != null) {
                vo.setProjectId(execution.getProjectId());
                vo.setTotalCases(execution.getTotalCases());
                vo.setPassedCases(execution.getPassedCases());
                vo.setFailedCases(execution.getFailedCases());
                vo.setSkippedCases(execution.getSkippedCases());
                vo.setDuration(execution.getDuration());
                
                // 计算通过率
                if (execution.getTotalCases() != null && execution.getTotalCases() > 0) {
                    double passRate = (execution.getPassedCases() != null ? execution.getPassedCases() : 0) * 100.0 / execution.getTotalCases();
                    vo.setPassRate(Math.round(passRate * 100.0) / 100.0);
                }
                
                // 获取项目名称
                if (execution.getProjectId() != null) {
                    Project project = projectMapper.selectById(execution.getProjectId());
                    if (project != null) {
                        vo.setProjectName(project.getName());
                    }
                }
            }
        }
        
        // 获取创建人名称
        if (report.getGeneratedBy() != null) {
            User user = userMapper.selectById(report.getGeneratedBy());
            if (user != null) {
                vo.setCreatedByName(user.getNickname() != null ? user.getNickname() : user.getUsername());
            }
        }
        
        // 设置确认状态文本
        if (report.getConfirmStatus() != null) {
            switch (report.getConfirmStatus()) {
                case 0:
                    vo.setConfirmStatusText("待确认");
                    break;
                case 1:
                    vo.setConfirmStatusText("已确认通过");
                    break;
                case 2:
                    vo.setConfirmStatusText("已确认失败");
                    break;
                default:
                    vo.setConfirmStatusText("未知");
            }
        }
        
        // 获取确认人名称
        if (report.getConfirmedBy() != null) {
            User user = userMapper.selectById(report.getConfirmedBy());
            if (user != null) {
                vo.setConfirmedByName(user.getNickname() != null ? user.getNickname() : user.getUsername());
            }
        }
        
        return vo;
    }
    
    /**
     * 转换明细为VO
     */
    private ExecutionDetailVO convertDetailToVO(TestExecutionDetail detail) {
        ExecutionDetailVO vo = new ExecutionDetailVO();
        BeanUtil.copyProperties(detail, vo);
        
        // 获取测试用例标题
        if (detail.getTestCaseId() != null) {
            TestCase testCase = testCaseMapper.selectById(detail.getTestCaseId());
            if (testCase != null) {
                vo.setTestCaseTitle(testCase.getTitle());
            }
        }
        
        return vo;
    }
    
    /**
     * 获取状态标签
     */
    private String getStatusLabel(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 1: return "通过";
            case 2: return "失败";
            case 3: return "跳过";
            default: return "未知";
        }
    }
}
