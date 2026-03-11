package com.aiatg.executor.impl;

import cn.hutool.json.JSONUtil;
import com.aiatg.dto.jmeter.JMeterApiConfig;
import com.aiatg.dto.jmeter.JMeterResult;
import com.aiatg.dto.jmeter.JMeterSampleResult;
import com.aiatg.entity.TestCase;
import com.aiatg.entity.TestExecutionDetail;
import com.aiatg.executor.TestExecutor;
import com.aiatg.service.JMeterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API测试执行器（基于JMeter）
 */
@Slf4j
@Component
public class ApiTestExecutor implements TestExecutor {
    
    @Autowired
    private JMeterService jmeterService;
    
    @Override
    public TestExecutionDetail execute(TestCase testCase, Long executionId) {
        TestExecutionDetail detail = new TestExecutionDetail();
        detail.setExecutionId(executionId);
        detail.setTestCaseId(testCase.getId());
        detail.setStartTime(LocalDateTime.now());
        
        try {
            log.info("开始执行API测试用例: {}, ID: {}", testCase.getTitle(), testCase.getId());
            
            // 解析测试用例的steps字段（JSON格式的JMeter配置）
            List<JMeterApiConfig> apiConfigs = parseApiConfigs(testCase.getSteps());
            
            if (apiConfigs == null || apiConfigs.isEmpty()) {
                throw new RuntimeException("测试用例配置为空");
            }
            
            // 执行所有API测试
            StringBuilder logsBuilder = new StringBuilder();
            int totalSuccess = 0;
            int totalError = 0;
            long totalDuration = 0;
            
            for (JMeterApiConfig config : apiConfigs) {
                log.info("执行接口: {}", config.getName());
                
                // ===== 记录输入参数 =====
                logsBuilder.append("========================================\n");
                logsBuilder.append(String.format("【接口名称】: %s\n", config.getName()));
                logsBuilder.append(String.format("【请求方法】: %s\n", config.getMethod()));
                logsBuilder.append(String.format("【请求地址】: %s\n", config.getUrl()));
                
                // 记录请求头 - 使用解析方法处理Object类型
                log.info("🔍 当前接口配置的headers类型: {}, 内容: {}", 
                    config.getHeaders() != null ? config.getHeaders().getClass().getName() : "null",
                    config.getHeaders());
                
                Map<String, String> parsedHeaders = parseHeadersObject(config.getHeaders());
                log.info("🔍 解析后的headers数量: {}", parsedHeaders.size());
                    
                if (!parsedHeaders.isEmpty()) {
                    logsBuilder.append("【请求头】:\n");
                    parsedHeaders.forEach((key, value) -> 
                        logsBuilder.append(String.format("  %s: %s\n", key, value)));
                } else {
                    log.warn("⚠️ 接口 [{}] 未配置请求头或headers为空", config.getName());
                }
                
                // 记录请求体
                if (config.getBody() != null && !config.getBody().isEmpty()) {
                    logsBuilder.append("【请求体】:\n");
                    String bodyLog = config.getBody();
                    if (bodyLog.length() > 1000) {
                        bodyLog = bodyLog.substring(0, 1000) + "...(已截断)";
                    }
                    logsBuilder.append(bodyLog).append("\n");
                }
                
                // 记录超时设置
                if (config.getTimeout() != null) {
                    logsBuilder.append(String.format("【超时时间】: %dms\n", config.getTimeout()));
                }
                
                logsBuilder.append("\n");
                
                // 使用JMeter执行
                long execStartTime = System.currentTimeMillis();
                JMeterResult result = jmeterService.executeApiTest(config);
                long execDuration = System.currentTimeMillis() - execStartTime;
                
                // ===== 记录输出参数 =====
                logsBuilder.append("【执行结果】:\n");
                logsBuilder.append(String.format("  执行耗时: %dms\n", execDuration));
                
                // 累计结果 - 直接使用JMeter返回的统计数据
                int successSamples = result.getSuccessSamples() != null ? result.getSuccessSamples() : 0;
                int errorSamples = result.getErrorSamples() != null ? result.getErrorSamples() : 0;
                
                totalSuccess += successSamples;
                totalError += errorSamples;
                totalDuration += (result.getAvgResponseTime() != null ? result.getAvgResponseTime() : 0);
                
                logsBuilder.append(String.format("  成功样本数: %d\n", successSamples));
                logsBuilder.append(String.format("  失败样本数: %d\n", errorSamples));
                logsBuilder.append(String.format("  平均响应时间: %dms\n", 
                    result.getAvgResponseTime() != null ? result.getAvgResponseTime() : 0));
                logsBuilder.append(String.format("  错误率: %.2f%%\n", 
                    result.getErrorRate() != null ? result.getErrorRate() : 0.0));
                
                // 记录响应详情
                if (result.getSampleResults() != null && !result.getSampleResults().isEmpty()) {
                    logsBuilder.append("\n【响应详情】:\n");
                    for (JMeterSampleResult sample : result.getSampleResults()) {
                        logsBuilder.append(String.format("  - 响应码: %s\n", sample.getResponseCode()));
                        logsBuilder.append(String.format("    响应消息: %s\n", sample.getResponseMessage()));
                        logsBuilder.append(String.format("    响应时间: %dms\n", sample.getResponseTime()));
                        logsBuilder.append(String.format("    执行状态: %s\n", sample.getSuccess() ? "✓ 成功" : "✗ 失败"));
                        
                        if (sample.getResponseData() != null) {
                            String responseLog = sample.getResponseData();
                            if (responseLog.length() > 1000) {
                                responseLog = responseLog.substring(0, 1000) + "...(已截断)";
                            }
                            logsBuilder.append("    响应数据:\n    ").append(responseLog).append("\n");
                        }
                        
                        if (!sample.getSuccess() && sample.getErrorMessage() != null) {
                            logsBuilder.append("    错误信息: ").append(sample.getErrorMessage()).append("\n");
                        }
                    }
                }
                
                // 如果有错误消息，单独记录
                if (result.getErrorMessage() != null) {
                    logsBuilder.append("\n【错误信息】: ").append(result.getErrorMessage()).append("\n");
                }
                
                logsBuilder.append("========================================\n\n");
                
                // 简化的控制台日志输出
                if (errorSamples > 0) {
                    log.warn("接口 [{}] 执行失败: 成功 {}, 失败 {}, 平均响应时间: {}ms", 
                        config.getName(), successSamples, errorSamples, result.getAvgResponseTime());
                } else {
                    log.info("接口 [{}] 执行成功: 成功 {}, 平均响应时间: {}ms", 
                        config.getName(), successSamples, result.getAvgResponseTime());
                }
            }
            
            // 判断整体是否成功
            boolean allSuccess = totalError == 0;
            
            detail.setEndTime(LocalDateTime.now());
            detail.setDuration(totalDuration);
            detail.setStatus(allSuccess ? 1 : 2); // 1:通过 2:失败
            detail.setLogs(logsBuilder.toString());
            
            if (!allSuccess) {
                detail.setErrorMessage(String.format("共%d个接口，%d个失败", 
                    totalSuccess + totalError, totalError));
            }
            
            log.info("API测试执行完成: {}, 成功: {}, 失败: {}", 
                testCase.getTitle(), totalSuccess, totalError);
            
        } catch (Exception e) {
            log.error("API测试执行失败", e);
            detail.setEndTime(LocalDateTime.now());
            detail.setStatus(2); // 失败
            detail.setErrorMessage(e.getMessage());
            detail.setStackTrace(getStackTrace(e));
            detail.setLogs("API测试执行异常: " + e.getMessage());
        }
        
        return detail;
    }
    
    @Override
    public String getExecutorType() {
        return "api";
    }
    
    @Override
    public boolean isAvailable() {
        return jmeterService.isAvailable();
    }
    
    /**
     * 解析API配置
     */
    private List<JMeterApiConfig> parseApiConfigs(String stepsJson) {
        try {
            if (stepsJson == null || stepsJson.trim().isEmpty()) {
                return null;
            }
            // 解析JSON为JMeterApiConfig列表
            return JSONUtil.toList(stepsJson, JMeterApiConfig.class);
        } catch (Exception e) {
            log.error("解析API配置失败", e);
            throw new RuntimeException("测试用例配置格式错误: " + e.getMessage());
        }
    }
    
    /**
     * 获取堆栈跟踪
     */
    /**
     * 解析请求头，支持Map类型和字符串类型（换行分隔的格式）
     *
     * @param headersObj headers对象（可能是Map或String）
     * @return 解析后的headers Map
     */
    private Map<String, String> parseHeadersObject(Object headersObj) {
        Map<String, String> result = new HashMap<>();
        if (headersObj == null) { 
            return result; 
        }
        
        if (headersObj instanceof Map) {
            @SuppressWarnings("unchecked") 
            Map<String, Object> map = (Map<String, Object>) headersObj;
            map.forEach((key, value) -> result.put(key, value.toString()));
            return result;
        }
        
        if (headersObj instanceof String) {
            String headersStr = (String) headersObj;
            String[] lines = headersStr.split("\\n");
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) { 
                    continue; 
                }
                int colonIndex = line.indexOf(':');
                if (colonIndex > 0) {
                    String key = line.substring(0, colonIndex).trim();
                    String value = line.substring(colonIndex + 1).trim();
                    result.put(key, value);
                }
            }
        }
        
        return result;
    }
    
    private String getStackTrace(Exception e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}
