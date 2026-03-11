package com.aiatg.controller;

import cn.hutool.json.JSONObject;
import com.aiatg.common.Result;
import com.aiatg.service.AtgClientResultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * ATG-Client 回调控制器
 * 用于接收 ATG-Client 主动推送的执行结果
 */
@Slf4j
@RestController
@RequestMapping("/atg-client")
@CrossOrigin
public class AtgClientController {
    
    @Autowired
    private AtgClientResultService atgClientResultService;
    
    /**
     * 接收 ATG-Client 推送的执行结果
     */
    @PostMapping("/callback/result")
    public Result<Void> receiveResult(@RequestBody JSONObject data) {
        log.info("========================================");
        log.info("收到 ATG-Client 回调请求");
        log.info("请求数据: {}", data.toString());
        log.info("========================================");
        
        try {
            Long executionId = data.getLong("executionId");
            Long testCaseId = data.getLong("testCaseId");
            String status = data.getStr("status");
            Long duration = data.getLong("duration");
            String logs = data.getStr("logs");
            String errorMessage = data.getStr("errorMessage");
            String screenshot = data.getStr("screenshot");
            
            // 数据验证
            if (executionId == null || testCaseId == null || status == null) {
                String errMsg = String.format("缺少必要参数: executionId=%s, testCaseId=%s, status=%s", 
                        executionId, testCaseId, status);
                log.error(errMsg);
                return Result.error(errMsg);
            }
            
            log.info("接收到 ATG-Client 推送的结果: executionId={}, testCaseId={}, status={}, duration={}ms", 
                    executionId, testCaseId, status, duration);
            
            atgClientResultService.saveResult(executionId, testCaseId, status, duration, 
                    logs, errorMessage, screenshot);
            
            log.info("✓ 结果处理成功: executionId={}, testCaseId={}", executionId, testCaseId);
            return Result.success("结果已接收并处理", null);
            
        } catch (Exception e) {
            log.error("✗ 接收 ATG-Client 结果失败", e);
            log.error("错误详情: {}", e.getMessage());
            e.printStackTrace();
            return Result.error("处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 诊断接口：检查执行状态
     */
    @GetMapping("/diagnosis/{executionId}")
    public Result<JSONObject> diagnosis(@PathVariable Long executionId) {
        try {
            JSONObject result = new JSONObject();
            result.set("executionId", executionId);
            result.set("timestamp", System.currentTimeMillis());
            result.set("message", "诊断接口工作正常");
            
            log.info("诊断请求: executionId={}", executionId);
            return Result.success("诊断成功", result);
        } catch (Exception e) {
            log.error("诊断失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 测试接口：验证回调是否可达
     */
    @PostMapping("/callback/test")
    public Result<String> testCallback(@RequestBody JSONObject data) {
        log.info("收到测试回调: {}", data.toString());
        return Result.success("回调接口工作正常", data.toString());
    }
}
