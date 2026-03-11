package com.aiatg.service;

import com.aiatg.dto.AgentRegisterDTO;
import com.aiatg.dto.TaskResultDTO;
import com.aiatg.entity.TestAgent;
import com.aiatg.vo.AgentTaskVO;

import java.util.List;

/**
 * 测试代理服务接口
 */
public interface TestAgentService {
    
    /**
     * 代理注册
     */
    TestAgent register(AgentRegisterDTO dto);
    
    /**
     * 心跳检测
     */
    void heartbeat(String agentId);
    
    /**
     * 获取待执行任务
     */
    AgentTaskVO getPendingTask(String agentId);
    
    /**
     * 更新任务状态
     */
    void updateTaskStatus(Long detailId, String agentId, Integer status);
    
    /**
     * 上传任务结果
     */
    void uploadTaskResult(Long detailId, String agentId, TaskResultDTO result);
    
    /**
     * 获取在线代理列表
     */
    List<TestAgent> getOnlineAgents();
    
    /**
     * 代理下线
     */
    void offline(String agentId);
}
