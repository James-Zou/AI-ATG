package com.aiatg.executor.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.aiatg.entity.TestCase;
import com.aiatg.entity.TestExecutionDetail;
import com.aiatg.executor.TestExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * UI测试执行器（通过 ATG-Client）
 */
@Slf4j
@Component
public class UiTestExecutor implements TestExecutor {
    
    @Value("${atg.client.url:http://localhost:9999}")
    private String atgClientUrl;
    
    @Value("${atg.client.timeout:10000}")
    private int timeout;  // 默认 10 秒超时（只是提交任务的超时，不是测试执行的超时）
    
    @Override
    public TestExecutionDetail execute(TestCase testCase, Long executionId) {
        TestExecutionDetail detail = new TestExecutionDetail();
        detail.setExecutionId(executionId);
        detail.setTestCaseId(testCase.getId());
        detail.setStartTime(LocalDateTime.now());
        
        StringBuilder logs = new StringBuilder();
        
        try {
            log.info("通过 ATG-Client 执行UI测试用例: {}", testCase.getTitle());
            logs.append("开始通过 ATG-Client 执行UI测试\n");
            logs.append("测试用例: ").append(testCase.getTitle()).append("\n");
            
            // 检查 ATG-Client 是否可用
            if (!checkAtgClientHealth()) {
                throw new RuntimeException("ATG-Client 不可用，请确保本地客户端已启动 (http://localhost:9999)");
            }
            
            logs.append("ATG-Client 连接成功\n");
            
            // 构建请求数据
            JSONObject requestData = new JSONObject();
            JSONObject testCaseData = new JSONObject();
            testCaseData.set("id", testCase.getId());
            testCaseData.set("title", testCase.getTitle());
            testCaseData.set("steps", testCase.getSteps());
            testCaseData.set("executionId", executionId);
            requestData.set("testCase", testCaseData);
            
            // 发送执行请求到 ATG-Client
            log.info("发送测试任务到 ATG-Client: {}", atgClientUrl);
            HttpResponse response = HttpRequest.post(atgClientUrl + "/execute")
                .body(requestData.toString())
                .contentType("application/json")
                .timeout(timeout)
                .execute();
            
            if (!response.isOk()) {
                throw new RuntimeException("ATG-Client 执行失败: " + response.getStatus());
            }
            
            JSONObject responseData = JSONUtil.parseObj(response.body());
            
            if (!responseData.getBool("success", false)) {
                throw new RuntimeException("ATG-Client 返回错误: " + responseData.getStr("error"));
            }
            
            logs.append("测试任务已提交到 ATG-Client\n");
            logs.append("ATG-Client 将在本地执行测试并自动上传结果\n");
            
            // 不再等待结果，ATG-Client 会主动推送结果
            // 这里返回待执行状态，结果由 ATG-Client 回调更新
            detail.setStatus(0); // 待执行（等待 ATG-Client 回调）
            detail.setLogs(logs.toString());
            
            log.info("UI测试任务已成功提交到 ATG-Client: executionId={}, testCaseId={}", 
                    executionId, testCase.getId());
            
        } catch (Exception e) {
            log.error("提交UI测试任务到 ATG-Client 失败: executionId={}, testCaseId={}", 
                    executionId, testCase.getId(), e);
            logs.append("\n提交失败: ").append(e.getMessage()).append("\n");
            logs.append("请检查 ATG-Client 是否正常运行\n");
            
            detail.setEndTime(LocalDateTime.now());
            detail.setStatus(2); // 失败
            detail.setErrorMessage("提交测试任务失败: " + e.getMessage());
            detail.setStackTrace(getStackTrace(e));
            detail.setLogs(logs.toString());
        }
        
        return detail;
    }
    
    @Override
    public String getExecutorType() {
        return "ui";
    }
    
    @Override
    public boolean isAvailable() {
        return checkAtgClientHealth();
    }
    
    /**
     * 检查 ATG-Client 健康状态
     */
    private boolean checkAtgClientHealth() {
        try {
            HttpResponse response = HttpRequest.get(atgClientUrl + "/health")
                .timeout(5000)
                .execute();
            
            if (response.isOk()) {
                JSONObject result = JSONUtil.parseObj(response.body());
                return "ok".equals(result.getStr("status"));
            }
            return false;
        } catch (Exception e) {
            log.warn("ATG-Client 健康检查失败: {}", e.getMessage());
            return false;
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
