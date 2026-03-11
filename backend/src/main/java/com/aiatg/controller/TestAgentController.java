package com.aiatg.controller;

import com.aiatg.common.Result;
import com.aiatg.dto.AgentRegisterDTO;
import com.aiatg.dto.TaskResultDTO;
import com.aiatg.entity.TestAgent;
import com.aiatg.service.TestAgentService;
import com.aiatg.vo.AgentTaskVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 测试代理控制器
 */
@Slf4j
@RestController
@RequestMapping("/agent")
public class TestAgentController {
    
    @Autowired
    private TestAgentService agentService;
    
    /**
     * 代理注册
     */
    @PostMapping("/register")
    public Result<TestAgent> register(@RequestBody AgentRegisterDTO dto) {
        log.info("代理注册: agentId={}, hostname={}", dto.getAgentId(), dto.getHostname());
        TestAgent agent = agentService.register(dto);
        return Result.success(agent);
    }
    
    /**
     * 心跳检测
     */
    @PostMapping("/heartbeat")
    public Result<Void> heartbeat(@RequestParam String agentId) {
        agentService.heartbeat(agentId);
        return Result.success();
    }
    
    /**
     * 获取待执行任务
     */
    @GetMapping("/tasks/pending")
    public Result<AgentTaskVO> getPendingTask(@RequestParam String agentId) {
        AgentTaskVO task = agentService.getPendingTask(agentId);
        return Result.success(task);
    }
    
    /**
     * 更新任务状态
     */
    @PutMapping("/tasks/{detailId}/status")
    public Result<Void> updateTaskStatus(
            @PathVariable Long detailId,
            @RequestParam String agentId,
            @RequestParam Integer status) {
        agentService.updateTaskStatus(detailId, agentId, status);
        return Result.success();
    }
    
    /**
     * 上传任务结果
     */
    @PostMapping("/tasks/{detailId}/result")
    public Result<Void> uploadTaskResult(
            @PathVariable Long detailId,
            @RequestParam String agentId,
            @RequestBody TaskResultDTO result) {
        agentService.uploadTaskResult(detailId, agentId, result);
        return Result.success();
    }
    
    /**
     * 获取在线代理列表
     */
    @GetMapping("/list")
    public Result<List<TestAgent>> getOnlineAgents() {
        List<TestAgent> agents = agentService.getOnlineAgents();
        return Result.success(agents);
    }
    
    /**
     * 代理下线
     */
    @PostMapping("/offline")
    public Result<Void> offline(@RequestParam String agentId) {
        agentService.offline(agentId);
        return Result.success();
    }
}
