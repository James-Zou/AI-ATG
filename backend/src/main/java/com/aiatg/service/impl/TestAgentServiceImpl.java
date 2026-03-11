package com.aiatg.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.aiatg.dto.AgentRegisterDTO;
import com.aiatg.dto.TaskResultDTO;
import com.aiatg.entity.TestAgent;
import com.aiatg.entity.TestCase;
import com.aiatg.entity.TestExecutionDetail;
import com.aiatg.mapper.TestAgentMapper;
import com.aiatg.mapper.TestCaseMapper;
import com.aiatg.mapper.TestExecutionDetailMapper;
import com.aiatg.service.TestAgentService;
import com.aiatg.vo.AgentTaskVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 测试代理服务实现类
 */
@Slf4j
@Service
public class TestAgentServiceImpl implements TestAgentService {
    
    @Autowired
    private TestAgentMapper agentMapper;
    
    @Autowired
    private TestExecutionDetailMapper detailMapper;
    
    @Autowired
    private TestCaseMapper testCaseMapper;
    
    @Override
    public TestAgent register(AgentRegisterDTO dto) {
        // 查找是否已存在该代理
        LambdaQueryWrapper<TestAgent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestAgent::getAgentId, dto.getAgentId());
        TestAgent existAgent = agentMapper.selectOne(queryWrapper);
        
        LocalDateTime now = LocalDateTime.now();
        
        if (existAgent != null) {
            // 更新现有代理信息
            existAgent.setAgentName(dto.getAgentName());
            existAgent.setHostname(dto.getHostname());
            existAgent.setOs(dto.getOs());
            existAgent.setOsVersion(dto.getOsVersion());
            existAgent.setBrowser(dto.getBrowser());
            existAgent.setBrowserVersion(dto.getBrowserVersion());
            existAgent.setIp(dto.getIp());
            existAgent.setStatus(1); // 在线
            existAgent.setLastHeartbeat(now);
            existAgent.setUpdatedTime(now);
            agentMapper.updateById(existAgent);
            
            log.info("代理重新注册: agentId={}", dto.getAgentId());
            return existAgent;
        } else {
            // 创建新代理
            TestAgent agent = new TestAgent();
            agent.setAgentId(dto.getAgentId());
            agent.setAgentName(dto.getAgentName());
            agent.setHostname(dto.getHostname());
            agent.setOs(dto.getOs());
            agent.setOsVersion(dto.getOsVersion());
            agent.setBrowser(dto.getBrowser());
            agent.setBrowserVersion(dto.getBrowserVersion());
            agent.setIp(dto.getIp());
            agent.setStatus(1); // 在线
            agent.setToken(IdUtil.simpleUUID());
            agent.setLastHeartbeat(now);
            agent.setCreatedTime(now);
            agent.setUpdatedTime(now);
            agentMapper.insert(agent);
            
            log.info("代理注册成功: agentId={}", dto.getAgentId());
            return agent;
        }
    }
    
    @Override
    public void heartbeat(String agentId) {
        LambdaQueryWrapper<TestAgent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestAgent::getAgentId, agentId);
        TestAgent agent = agentMapper.selectOne(queryWrapper);
        
        if (agent == null) {
            throw new RuntimeException("代理不存在: " + agentId);
        }
        
        agent.setLastHeartbeat(LocalDateTime.now());
        agent.setStatus(1); // 在线
        agentMapper.updateById(agent);
        
        log.debug("代理心跳: agentId={}", agentId);
    }
    
    @Override
    public AgentTaskVO getPendingTask(String agentId) {
        // 查找待执行的任务（状态为0-待执行）
        LambdaQueryWrapper<TestExecutionDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestExecutionDetail::getStatus, 0)
                    .orderByAsc(TestExecutionDetail::getId)
                    .last("LIMIT 1");
        TestExecutionDetail detail = detailMapper.selectOne(queryWrapper);
        
        if (detail == null) {
            return null;
        }
        
        // 获取测试用例信息
        TestCase testCase = testCaseMapper.selectById(detail.getTestCaseId());
        if (testCase == null) {
            log.error("测试用例不存在: testCaseId={}", detail.getTestCaseId());
            return null;
        }
        
        // 标记任务为执行中（状态为1）
        detail.setStatus(1);
        detail.setStartTime(LocalDateTime.now());
        detailMapper.updateById(detail);
        
        // 构建任务VO
        AgentTaskVO taskVO = new AgentTaskVO();
        taskVO.setDetailId(detail.getId());
        taskVO.setExecutionId(detail.getExecutionId());
        taskVO.setTestCaseId(testCase.getId());
        taskVO.setTestCaseTitle(testCase.getTitle());
        taskVO.setTestCaseType(testCase.getType());
        taskVO.setSteps(testCase.getSteps());
        taskVO.setTestData(testCase.getTestData());
        taskVO.setTimeout(30); // 默认超时时间30秒
        
        log.info("分配任务给代理: agentId={}, detailId={}, testCaseTitle={}", 
                 agentId, detail.getId(), testCase.getTitle());
        
        return taskVO;
    }
    
    @Override
    public void updateTaskStatus(Long detailId, String agentId, Integer status) {
        TestExecutionDetail detail = detailMapper.selectById(detailId);
        if (detail == null) {
            throw new RuntimeException("任务明细不存在: " + detailId);
        }
        
        detail.setStatus(status);
        if (status == 2 || status == 3) {
            detail.setEndTime(LocalDateTime.now());
        }
        detailMapper.updateById(detail);
        
        log.info("更新任务状态: agentId={}, detailId={}, status={}", agentId, detailId, status);
    }
    
    @Override
    public void uploadTaskResult(Long detailId, String agentId, TaskResultDTO result) {
        TestExecutionDetail detail = detailMapper.selectById(detailId);
        if (detail == null) {
            throw new RuntimeException("任务明细不存在: " + detailId);
        }
        
        detail.setStatus(result.getStatus());
        detail.setDuration(result.getDuration());
        detail.setLogs(result.getLogs());
        detail.setErrorMessage(result.getErrorMessage());
        detail.setStackTrace(result.getStackTrace());
        detail.setEndTime(LocalDateTime.now());
        
        // 如果有截图，保存为Base64（后续可以改为上传到文件服务器）
        if (result.getScreenshotBase64() != null && !result.getScreenshotBase64().isEmpty()) {
            detail.setScreenshotUrl("data:image/png;base64," + result.getScreenshotBase64());
        }
        
        detailMapper.updateById(detail);
        
        log.info("上传任务结果: agentId={}, detailId={}, status={}, duration={}ms", 
                 agentId, detailId, result.getStatus(), result.getDuration());
    }
    
    @Override
    public List<TestAgent> getOnlineAgents() {
        // 查询5分钟内有心跳的代理
        LambdaQueryWrapper<TestAgent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestAgent::getStatus, 1)
                    .ge(TestAgent::getLastHeartbeat, LocalDateTime.now().minusMinutes(5))
                    .orderByDesc(TestAgent::getLastHeartbeat);
        return agentMapper.selectList(queryWrapper);
    }
    
    @Override
    public void offline(String agentId) {
        LambdaQueryWrapper<TestAgent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestAgent::getAgentId, agentId);
        TestAgent agent = agentMapper.selectOne(queryWrapper);
        
        if (agent == null) {
            throw new RuntimeException("代理不存在: " + agentId);
        }
        
        agent.setStatus(0); // 离线
        agent.setUpdatedTime(LocalDateTime.now());
        agentMapper.updateById(agent);
        
        log.info("代理下线: agentId={}", agentId);
    }
}
