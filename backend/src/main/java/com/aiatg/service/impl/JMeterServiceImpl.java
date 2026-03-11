package com.aiatg.service.impl;

import com.aiatg.dto.jmeter.*;
import com.aiatg.service.JMeterService;
import lombok.extern.slf4j.Slf4j;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.gui.HeaderPanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.ListedHashTree;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * JMeter执行服务实现
 */
@Slf4j
@Service
public class JMeterServiceImpl implements JMeterService {
    
    private boolean initialized = false;
    
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
        
        // 如果已经是Map类型，直接返回
        if (headersObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) headersObj;
            map.forEach((key, value) -> result.put(key, value.toString()));
            return result;
        }
        
        // 如果是String类型，按换行分隔解析
        if (headersObj instanceof String) {
            String headersStr = (String) headersObj;
            String[] lines = headersStr.split("\n");
            
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

    /**
     * 响应数据收集器 - 必须继承 ResultCollector 才能被 JMeter 正确识别
     */
    public static class ResponseCollector extends ResultCollector {
        private final List<JMeterSampleResult> results = new CopyOnWriteArrayList<>();
        
        public ResponseCollector() {
            super();
        }
        
        public ResponseCollector(Summariser summer) {
            super(summer);
        }
        
        @Override
        public void sampleOccurred(SampleEvent event) {
            super.sampleOccurred(event);
            
            SampleResult result = event.getResult();
            log.debug("收集到样本结果: {} - 成功: {}, 响应码: {}, 响应时间: {}ms", 
                result.getSampleLabel(), result.isSuccessful(), result.getResponseCode(), result.getTime());
            
            JMeterSampleResult sampleResult = new JMeterSampleResult();
            
            sampleResult.setSampleLabel(result.getSampleLabel());
            sampleResult.setResponseTime(result.getTime());
            sampleResult.setSuccess(result.isSuccessful());
            sampleResult.setResponseCode(result.getResponseCode());
            sampleResult.setResponseMessage(result.getResponseMessage());
            
            // 捕获响应数据
            String responseData = result.getResponseDataAsString();
            if (responseData != null && !responseData.isEmpty()) {
                // 限制响应数据大小，避免日志过长
                if (responseData.length() > 5000) {
                    responseData = responseData.substring(0, 5000) + "...(已截断)";
                }
                sampleResult.setResponseData(responseData);
            } else {
                sampleResult.setResponseData("无返回信息");
            }
            
            // 捕获请求数据
            String requestData = result.getSamplerData();
            if (requestData != null && !requestData.isEmpty()) {
                if (requestData.length() > 2000) {
                    requestData = requestData.substring(0, 2000) + "...(已截断)";
                }
                log.debug("请求数据: {}", requestData);
            }
            
            // 捕获错误信息和断言结果
            if (!result.isSuccessful()) {
                StringBuilder errorMsg = new StringBuilder();
                errorMsg.append(result.getResponseMessage());
                
                // 获取断言结果
                if (result.getAssertionResults() != null && result.getAssertionResults().length > 0) {
                    errorMsg.append(" | 断言失败: ");
                    for (org.apache.jmeter.assertions.AssertionResult assertionResult : result.getAssertionResults()) {
                        if (assertionResult.isFailure() || assertionResult.isError()) {
                            errorMsg.append(assertionResult.getFailureMessage()).append("; ");
                        }
                    }
                }
                
                sampleResult.setErrorMessage(errorMsg.toString());
                log.debug("错误信息: {}", errorMsg);
            }
            
            results.add(sampleResult);
            log.debug("当前已收集样本数: {}", results.size());
        }
        
        @Override
        public void sampleStarted(SampleEvent event) {
            super.sampleStarted(event);
        }
        
        @Override
        public void sampleStopped(SampleEvent event) {
            super.sampleStopped(event);
        }
        
        public List<JMeterSampleResult> getResults() {
            log.debug("获取收集的结果，总数: {}", results.size());
            return new ArrayList<>(results);
        }
    }
    
    @PostConstruct
    public void init() {
        try {
            // 初始化JMeter
            String jmeterHome = System.getProperty("jmeter.home");
            if (jmeterHome == null) {
                // 使用临时目录作为JMeter home
                jmeterHome = System.getProperty("java.io.tmpdir") + "/jmeter";
                File jmeterDir = new File(jmeterHome);
                if (!jmeterDir.exists()) {
                    jmeterDir.mkdirs();
                }
                System.setProperty("jmeter.home", jmeterHome);
            }
            
            // 初始化JMeter配置
            File jmeterProperties = new File(jmeterHome + "/bin/jmeter.properties");
            if (!jmeterProperties.exists()) {
                // 创建基本的jmeter.properties
                jmeterProperties.getParentFile().mkdirs();
                jmeterProperties.createNewFile();
            }
            
            JMeterUtils.loadJMeterProperties(jmeterProperties.getAbsolutePath());
            JMeterUtils.setJMeterHome(jmeterHome);
            JMeterUtils.initLocale();
            
            initialized = true;
            
            log.info("JMeter初始化成功，Home目录: {}", jmeterHome);
        } catch (Exception e) {
            log.error("JMeter初始化失败", e);
            initialized = false;
        }
    }
    
    @Override
    public JMeterResult executeApiTest(JMeterApiConfig config) {
        if (!initialized) {
            throw new RuntimeException("JMeter未初始化");
        }
        
        JMeterResult result = new JMeterResult();
        ResponseCollector responseCollector = new ResponseCollector();
        
        // 每次测试创建新的JMeter引擎实例，避免复用导致的状态问题
        StandardJMeterEngine jmeter = new StandardJMeterEngine();
        
        try {
            log.info("开始执行API测试: {}", config.getName());
            
            // 创建测试计划树
            ListedHashTree testPlanTree = new ListedHashTree();
            
            // 创建TestPlan
            TestPlan testPlan = new TestPlan("API Test Plan");
            testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
            testPlan.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());
            testPlan.setUserDefinedVariables(new Arguments());
            
            // 创建ThreadGroup
            ThreadGroup threadGroup = new ThreadGroup();
            threadGroup.setName("API Thread Group");
            threadGroup.setNumThreads(1);
            threadGroup.setRampUp(0);
            threadGroup.setProperty(TestElement.TEST_CLASS, ThreadGroup.class.getName());
            threadGroup.setProperty(TestElement.GUI_CLASS, ThreadGroupGui.class.getName());
            
            // 创建LoopController
            LoopController loopController = new LoopController();
            loopController.setLoops(1);
            loopController.setProperty(TestElement.TEST_CLASS, LoopController.class.getName());
            loopController.setProperty(TestElement.GUI_CLASS, LoopControlPanel.class.getName());
            loopController.initialize();
            threadGroup.setSamplerController(loopController);
            
            // 创建HTTP请求
            HTTPSamplerProxy httpSampler = new HTTPSamplerProxy();
            httpSampler.setName(config.getName());
            httpSampler.setMethod(config.getMethod());
            httpSampler.setPath(config.getUrl());
            httpSampler.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
            httpSampler.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());
            
            if (config.getTimeout() != null) {
                httpSampler.setConnectTimeout(config.getTimeout().toString());
                httpSampler.setResponseTimeout(config.getTimeout().toString());
            }
            
            // 添加请求体
            if (config.getBody() != null && !config.getBody().isEmpty()) {
                httpSampler.setPostBodyRaw(true);
                httpSampler.addNonEncodedArgument("", config.getBody(), "");
            }
            
            // 添加请求头
            // 处理请求头 - 支持Map和字符串格式
            Map<String, String> parsedHeaders = parseHeadersObject(config.getHeaders());
            log.info("🔍 原始headers对象类型: {}, 解析后的headers数量: {}", 
                config.getHeaders() != null ? config.getHeaders().getClass().getName() : "null", 
                parsedHeaders.size());
            
            // 创建 HeaderManager（如果有请求头）
            HeaderManager headerManager = null;
            if (!parsedHeaders.isEmpty()) {
                headerManager = new HeaderManager();
                headerManager.setName("HeaderManager");
                headerManager.setProperty(TestElement.TEST_CLASS, HeaderManager.class.getName());
                headerManager.setProperty(TestElement.GUI_CLASS, HeaderPanel.class.getName());
                
                for (Map.Entry<String, String> entry : parsedHeaders.entrySet()) {
                    headerManager.add(new Header(entry.getKey(), entry.getValue()));
                    log.info("✅ 添加请求头: {} = {}", entry.getKey(), entry.getValue());
                }
                
                log.info("✅ HeaderManager已创建，包含{}个请求头", parsedHeaders.size());
            } else {
                log.warn("⚠️ 未配置任何请求头，原始headers对象: {}", config.getHeaders());
            }
            
            // 构建测试树结构
            ListedHashTree threadGroupHashTree = (ListedHashTree) testPlanTree.add(testPlan, threadGroup);
            ListedHashTree httpSamplerHashTree = (ListedHashTree) threadGroupHashTree.add(httpSampler);
            
            // 将 HeaderManager 添加到 httpSampler 的 HashTree 中（正确的方式）
            if (headerManager != null) {
                httpSamplerHashTree.add(headerManager);
                log.info("✅ HeaderManager已添加到httpSampler的HashTree中");
            }
            
            // 添加控制台日志收集器
            Summariser summer = null;
            String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
            if (summariserName.length() > 0) {
                summer = new Summariser(summariserName);
            }
            
            // 将响应收集器添加到测试计划级别（确保能收集到所有结果）
            responseCollector = new ResponseCollector(summer);
            testPlanTree.add(testPlan, responseCollector);
            
            log.debug("响应收集器已添加到测试计划，准备执行测试");
            
            // 配置并运行测试
            jmeter.configure(testPlanTree);
            
            long startTime = System.currentTimeMillis();
            jmeter.run();
            long duration = System.currentTimeMillis() - startTime;
            
            log.debug("API测试JMeter执行完成，耗时: {}ms", duration);
            
            // 获取收集到的结果
            List<JMeterSampleResult> sampleResults = responseCollector.getResults();
            log.info("收集到的API测试样本结果数量: {}", sampleResults.size());
            
            // 统计结果
            int successCount = 0;
            int errorCount = 0;
            long totalResponseTime = 0;
            
            StringBuilder logsBuilder = new StringBuilder();
            for (JMeterSampleResult sampleResult : sampleResults) {
                if (sampleResult.getSuccess()) {
                    successCount++;
                } else {
                    errorCount++;
                }
                totalResponseTime += sampleResult.getResponseTime();
                
                // 构建日志
                logsBuilder.append(String.format("[%s] ", sampleResult.getSampleLabel()));
                if (sampleResult.getSuccess()) {
                    logsBuilder.append("执行成功");
                } else {
                    logsBuilder.append("执行失败: ").append(sampleResult.getErrorMessage());
                }
                logsBuilder.append(", 响应时间: ").append(sampleResult.getResponseTime()).append("ms");
                logsBuilder.append("\n响应数据: ").append(sampleResult.getResponseData());
                logsBuilder.append("\n\n");
            }
            
            // 设置结果
            result.setSuccess(errorCount == 0);
            result.setTotalSamples(sampleResults.size());
            result.setSuccessSamples(successCount);
            result.setErrorSamples(errorCount);
            result.setErrorRate(sampleResults.size() > 0 ? (double) errorCount / sampleResults.size() * 100 : 0.0);
            result.setAvgResponseTime(sampleResults.size() > 0 ? totalResponseTime / sampleResults.size() : 0);
            result.setLogs(logsBuilder.toString());
            result.setSampleResults(sampleResults);
            
            log.info("API测试执行完成: {}, 耗时: {}ms, 成功: {}, 失败: {}", 
                config.getName(), duration, successCount, errorCount);
            
        } catch (Exception e) {
            log.error("API测试执行失败", e);
            result.setSuccess(false);
            result.setErrorMessage("执行失败: " + e.getMessage());
            result.setLogs(e.toString());
            result.setSampleResults(responseCollector.getResults());
        } finally {
            // 清理JMeter引擎资源
            try {
                jmeter.exit();
            } catch (Exception e) {
                log.warn("清理JMeter引擎失败", e);
            }
        }
        
        return result;
    }
    
    @Override
    public JMeterResult executePerformanceTest(JMeterPerformanceConfig config) {
        if (!initialized) {
            throw new RuntimeException("JMeter未初始化");
        }
        
        JMeterResult result = new JMeterResult();
        ResponseCollector responseCollector = new ResponseCollector();
        
        // 每次测试创建新的JMeter引擎实例，避免复用导致的状态问题
        StandardJMeterEngine jmeter = new StandardJMeterEngine();
        
        try {
            log.info("开始执行性能测试: {}", config.getName());
            
            // 创建测试计划树
            ListedHashTree testPlanTree = new ListedHashTree();
            
            // 创建TestPlan
            TestPlan testPlan = new TestPlan("Performance Test Plan");
            testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
            testPlan.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());
            testPlan.setUserDefinedVariables(new Arguments());
            
            // 创建ThreadGroup
            ThreadGroup threadGroup = new ThreadGroup();
            threadGroup.setName("Performance Thread Group");
            threadGroup.setNumThreads(config.getThreads());
            threadGroup.setRampUp(config.getRampUp());
            threadGroup.setProperty(TestElement.TEST_CLASS, ThreadGroup.class.getName());
            threadGroup.setProperty(TestElement.GUI_CLASS, ThreadGroupGui.class.getName());
            
            // 设置持续时间
            if (config.getDuration() != null && config.getDuration() > 0) {
                threadGroup.setDuration(config.getDuration());
                threadGroup.setScheduler(true);
            }
            
            // 创建LoopController
            LoopController loopController = new LoopController();
            loopController.setLoops(config.getLoopCount());
            loopController.setProperty(TestElement.TEST_CLASS, LoopController.class.getName());
            loopController.setProperty(TestElement.GUI_CLASS, LoopControlPanel.class.getName());
            loopController.initialize();
            threadGroup.setSamplerController(loopController);
            
            // 创建HTTP请求
            HTTPSamplerProxy httpSampler = new HTTPSamplerProxy();
            httpSampler.setName(config.getName());
            httpSampler.setMethod(config.getMethod());
            httpSampler.setPath(config.getUrl());
            httpSampler.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
            httpSampler.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());
            
            if (config.getTimeout() != null) {
                httpSampler.setConnectTimeout(config.getTimeout().toString());
                httpSampler.setResponseTimeout(config.getTimeout().toString());
            }
            
            // 构建测试树结构
            ListedHashTree threadGroupHashTree = (ListedHashTree) testPlanTree.add(testPlan, threadGroup);
            threadGroupHashTree.add(httpSampler);
            
            // 添加控制台日志收集器
            Summariser summer = null;
            String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
            if (summariserName.length() > 0) {
                summer = new Summariser(summariserName);
            }
            
            // 将响应收集器添加到测试计划级别（确保能收集到所有结果）
            responseCollector = new ResponseCollector(summer);
            testPlanTree.add(testPlan, responseCollector);
            
            log.debug("性能测试响应收集器已添加，准备执行测试");
            
            // 配置并运行测试
            jmeter.configure(testPlanTree);
            
            long startTime = System.currentTimeMillis();
            jmeter.run();
            long duration = System.currentTimeMillis() - startTime;
            
            log.debug("性能测试JMeter执行完成，耗时: {}ms", duration);
            
            // 获取收集到的结果
            List<JMeterSampleResult> sampleResults = responseCollector.getResults();
            log.info("收集到的性能测试样本结果数量: {}", sampleResults.size());
            
            // 统计结果
            int successCount = 0;
            int errorCount = 0;
            long totalResponseTime = 0;
            long minResponseTime = Long.MAX_VALUE;
            long maxResponseTime = 0;
            
            StringBuilder logsBuilder = new StringBuilder();
            logsBuilder.append(String.format("性能测试完成：线程数=%d, 循环=%d\n\n", 
                config.getThreads(), config.getLoopCount()));
            
            // 只显示前10个样本的详细响应数据，避免日志过长
            int displayCount = Math.min(sampleResults.size(), 10);
            for (int i = 0; i < displayCount; i++) {
                JMeterSampleResult sampleResult = sampleResults.get(i);
                if (sampleResult.getSuccess()) {
                    successCount++;
                } else {
                    errorCount++;
                }
                long respTime = sampleResult.getResponseTime();
                totalResponseTime += respTime;
                minResponseTime = Math.min(minResponseTime, respTime);
                maxResponseTime = Math.max(maxResponseTime, respTime);
                
                // 构建日志
                logsBuilder.append(String.format("样本 %d [%s] ", i + 1, sampleResult.getSampleLabel()));
                if (sampleResult.getSuccess()) {
                    logsBuilder.append("✓ 成功");
                } else {
                    logsBuilder.append("✗ 失败: ").append(sampleResult.getErrorMessage());
                }
                logsBuilder.append(String.format(", 响应码: %s, 响应时间: %dms", 
                    sampleResult.getResponseCode(), sampleResult.getResponseTime()));
                logsBuilder.append("\n响应数据: ").append(sampleResult.getResponseData());
                logsBuilder.append("\n\n");
            }
            
            // 统计剩余样本
            for (int i = displayCount; i < sampleResults.size(); i++) {
                JMeterSampleResult sampleResult = sampleResults.get(i);
                if (sampleResult.getSuccess()) {
                    successCount++;
                } else {
                    errorCount++;
                }
                long respTime = sampleResult.getResponseTime();
                totalResponseTime += respTime;
                minResponseTime = Math.min(minResponseTime, respTime);
                maxResponseTime = Math.max(maxResponseTime, respTime);
            }
            
            if (sampleResults.size() > displayCount) {
                logsBuilder.append(String.format("... 还有 %d 个样本（已省略详细信息）\n\n", 
                    sampleResults.size() - displayCount));
            }
            
            // 添加统计摘要
            logsBuilder.append("=== 执行摘要 ===\n");
            logsBuilder.append(String.format("总样本数: %d\n", sampleResults.size()));
            logsBuilder.append(String.format("成功: %d (%.2f%%)\n", successCount, 
                sampleResults.size() > 0 ? (double) successCount / sampleResults.size() * 100 : 0));
            logsBuilder.append(String.format("失败: %d (%.2f%%)\n", errorCount,
                sampleResults.size() > 0 ? (double) errorCount / sampleResults.size() * 100 : 0));
            logsBuilder.append(String.format("平均响应时间: %dms\n", 
                sampleResults.size() > 0 ? totalResponseTime / sampleResults.size() : 0));
            logsBuilder.append(String.format("最小响应时间: %dms\n", 
                minResponseTime == Long.MAX_VALUE ? 0 : minResponseTime));
            logsBuilder.append(String.format("最大响应时间: %dms\n", maxResponseTime));
            logsBuilder.append(String.format("吞吐量: %.2f 请求/秒", 
                duration > 0 ? (double) sampleResults.size() / (duration / 1000.0) : 0));
            
            // 设置结果
            result.setSuccess(errorCount == 0);
            result.setTotalSamples(sampleResults.size());
            result.setSuccessSamples(successCount);
            result.setErrorSamples(errorCount);
            result.setErrorRate(sampleResults.size() > 0 ? (double) errorCount / sampleResults.size() * 100 : 0.0);
            result.setAvgResponseTime(sampleResults.size() > 0 ? totalResponseTime / sampleResults.size() : 0);
            result.setMinResponseTime(minResponseTime == Long.MAX_VALUE ? 0 : minResponseTime);
            result.setMaxResponseTime(maxResponseTime);
            result.setThroughput(duration > 0 ? (double) sampleResults.size() / (duration / 1000.0) : 0.0);
            result.setLogs(logsBuilder.toString());
            result.setSampleResults(sampleResults);
            
            log.info("性能测试执行完成: {}, 总样本数: {}, 耗时: {}ms, 成功: {}, 失败: {}", 
                config.getName(), sampleResults.size(), duration, successCount, errorCount);
            
        } catch (Exception e) {
            log.error("性能测试执行失败", e);
            result.setSuccess(false);
            result.setErrorMessage("执行失败: " + e.getMessage());
            result.setLogs(e.toString());
            result.setSampleResults(responseCollector.getResults());
        } finally {
            // 清理JMeter引擎资源
            try {
                jmeter.exit();
            } catch (Exception e) {
                log.warn("清理JMeter引擎失败", e);
            }
        }
        
        return result;
    }
    
    @Override
    public boolean isAvailable() {
        return initialized;
    }
}
