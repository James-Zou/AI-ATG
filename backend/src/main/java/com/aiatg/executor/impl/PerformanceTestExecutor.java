package com.aiatg.executor.impl;

import cn.hutool.json.JSONUtil;
import com.aiatg.dto.jmeter.JMeterPerformanceConfig;
import com.aiatg.dto.jmeter.JMeterResult;
import com.aiatg.entity.TestCase;
import com.aiatg.entity.TestExecutionDetail;
import com.aiatg.executor.TestExecutor;
import com.aiatg.service.JMeterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 性能测试执行器（基于JMeter）
 */
@Slf4j
@Component
public class PerformanceTestExecutor implements TestExecutor {
    
    @Autowired
    private JMeterService jmeterService;
    
    @Override
    public TestExecutionDetail execute(TestCase testCase, Long executionId) {
        TestExecutionDetail detail = new TestExecutionDetail();
        detail.setExecutionId(executionId);
        detail.setTestCaseId(testCase.getId());
        detail.setStartTime(LocalDateTime.now());
        
        try {
            log.info("开始执行性能测试用例: {}, ID: {}", testCase.getTitle(), testCase.getId());
            
            // 解析性能测试配置
            List<JMeterPerformanceConfig> perfConfigs = parsePerformanceConfigs(testCase.getSteps());
            
            if (perfConfigs == null || perfConfigs.isEmpty()) {
                throw new RuntimeException("性能测试配置为空");
            }
            
            // 取第一个配置（性能测试通常只有一个配置）
            JMeterPerformanceConfig config = perfConfigs.get(0);
            
            log.info("性能测试配置: 线程数={}, 循环次数={}, 持续时间={}秒", 
                config.getThreads(), config.getLoopCount(), config.getDuration());
            
            // 使用JMeter执行性能测试
            JMeterResult result = jmeterService.executePerformanceTest(config);
            
            // 设置执行结果
            detail.setEndTime(LocalDateTime.now());
            detail.setDuration(result.getAvgResponseTime() != null ? result.getAvgResponseTime() : 0L);
            detail.setStatus(result.getSuccess() ? 1 : 2); // 1:通过 2:失败
            
            // 构建详细日志
            StringBuilder logsBuilder = new StringBuilder();
            logsBuilder.append(String.format("=== 性能测试结果 ===\n"));
            logsBuilder.append(String.format("总样本数: %d\n", result.getTotalSamples()));
            logsBuilder.append(String.format("成功数: %d\n", result.getSuccessSamples()));
            logsBuilder.append(String.format("失败数: %d\n", result.getErrorSamples()));
            logsBuilder.append(String.format("错误率: %.2f%%\n", result.getErrorRate()));
            logsBuilder.append(String.format("平均响应时间: %dms\n", result.getAvgResponseTime()));
            logsBuilder.append(String.format("吞吐量: %.2f TPS\n", result.getThroughput()));
            logsBuilder.append("\n").append(result.getLogs());
            
            detail.setLogs(logsBuilder.toString());
            
            if (!result.getSuccess()) {
                detail.setErrorMessage(result.getErrorMessage());
            }
            
            log.info("性能测试执行完成: {}, 状态: {}", testCase.getTitle(), 
                result.getSuccess() ? "成功" : "失败");
            
        } catch (Exception e) {
            log.error("性能测试执行失败", e);
            detail.setEndTime(LocalDateTime.now());
            detail.setStatus(2); // 失败
            detail.setErrorMessage(e.getMessage());
            detail.setStackTrace(getStackTrace(e));
            detail.setLogs("性能测试执行异常: " + e.getMessage());
        }
        
        return detail;
    }
    
    @Override
    public String getExecutorType() {
        return "performance";
    }
    
    @Override
    public boolean isAvailable() {
        return jmeterService.isAvailable();
    }
    
    /**
     * 解析性能测试配置
     */
    private List<JMeterPerformanceConfig> parsePerformanceConfigs(String stepsJson) {
        try {
            if (stepsJson == null || stepsJson.trim().isEmpty()) {
                return null;
            }
            // 解析JSON为JMeterPerformanceConfig列表
            return JSONUtil.toList(stepsJson, JMeterPerformanceConfig.class);
        } catch (Exception e) {
            log.error("解析性能测试配置失败", e);
            throw new RuntimeException("测试用例配置格式错误: " + e.getMessage());
        }
    }
    
    /**
     * 获取堆栈跟踪
     */
    private String getStackTrace(Exception e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}
