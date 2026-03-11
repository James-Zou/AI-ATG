package com.aiatg.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.aiatg.common.PageResult;
import com.aiatg.dto.GitlabConfigDTO;
import com.aiatg.dto.WebhookPayload;
import com.aiatg.entity.*;
import com.aiatg.mapper.*;
import com.aiatg.service.GitlabService;
import com.aiatg.vo.GitlabConfigVO;
import com.aiatg.vo.WebhookRecordVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * GitLab服务实现类
 */
@Slf4j
@Service
public class GitlabServiceImpl implements GitlabService {
    
    @Autowired
    private GitlabConfigMapper gitlabConfigMapper;
    
    @Autowired
    private WebhookRecordMapper webhookRecordMapper;
    
    @Autowired
    private ProjectMapper projectMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Value("${server.port:8080}")
    private String serverPort;
    
    @Override
    @Transactional
    public GitlabConfigVO createConfig(GitlabConfigDTO dto, Long userId) {
        // 检查项目是否已经配置过
        LambdaQueryWrapper<GitlabConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GitlabConfig::getProjectId, dto.getProjectId());
        GitlabConfig existing = gitlabConfigMapper.selectOne(wrapper);
        
        if (existing != null) {
            throw new RuntimeException("该项目已经配置过GitLab集成");
        }
        
        GitlabConfig config = new GitlabConfig();
        BeanUtil.copyProperties(dto, config);
        config.setCreatedBy(userId);
        config.setCreatedTime(LocalDateTime.now());
        config.setUpdatedTime(LocalDateTime.now());
        
        gitlabConfigMapper.insert(config);
        
        return convertToVO(config);
    }
    
    @Override
    @Transactional
    public GitlabConfigVO updateConfig(Long id, GitlabConfigDTO dto) {
        GitlabConfig config = gitlabConfigMapper.selectById(id);
        if (config == null) {
            throw new RuntimeException("配置不存在");
        }
        
        BeanUtil.copyProperties(dto, config, "id", "createdBy", "createdTime");
        config.setUpdatedTime(LocalDateTime.now());
        
        gitlabConfigMapper.updateById(config);
        
        return convertToVO(config);
    }
    
    @Override
    public GitlabConfigVO getConfig(Long id) {
        GitlabConfig config = gitlabConfigMapper.selectById(id);
        if (config == null) {
            throw new RuntimeException("配置不存在");
        }
        return convertToVO(config);
    }
    
    @Override
    public GitlabConfigVO getConfigByProject(Long projectId) {
        LambdaQueryWrapper<GitlabConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GitlabConfig::getProjectId, projectId);
        GitlabConfig config = gitlabConfigMapper.selectOne(wrapper);
        
        if (config == null) {
            return null;
        }
        
        return convertToVO(config);
    }
    
    @Override
    @Transactional
    public void deleteConfig(Long id) {
        GitlabConfig config = gitlabConfigMapper.selectById(id);
        if (config == null) {
            throw new RuntimeException("配置不存在");
        }
        
        gitlabConfigMapper.deleteById(id);
    }
    
    @Override
    @Transactional
    public void handleWebhook(String signature, WebhookPayload payload) {
        log.info("收到GitLab Webhook: {}", payload.getObjectKind());
        
        // 创建Webhook记录
        WebhookRecord record = new WebhookRecord();
        record.setEventType(payload.getEventName());
        record.setObjectKind(payload.getObjectKind());
        record.setRef(payload.getBranchName());
        record.setReceivedTime(LocalDateTime.now());
        record.setStatus(0); // 处理中
        
        // 获取提交信息
        Map<String, Object> lastCommit = payload.getLastCommit();
        if (lastCommit != null) {
            record.setCommitId((String) lastCommit.get("id"));
            record.setCommitMessage((String) lastCommit.get("message"));
            
            Map<String, Object> author = (Map<String, Object>) lastCommit.get("author");
            if (author != null) {
                record.setCommitAuthor((String) author.get("name"));
            }
        }
        
        webhookRecordMapper.insert(record);
        
        // 异步处理Webhook
        processWebhookAsync(record.getId(), payload);
    }
    
    @Override
    public boolean verifySignature(String signature, String payload, String secret) {
        if (signature == null || secret == null) {
            return false;
        }
        
        try {
            // GitLab使用 X-Gitlab-Token 头或 HMAC-SHA256
            // 这里简化处理，实际应该根据GitLab的签名算法
            HMac hMac = new HMac(HmacAlgorithm.HmacSHA256, secret.getBytes());
            String computed = hMac.digestHex(payload);
            
            return signature.equals(computed);
        } catch (Exception e) {
            log.error("验证签名失败", e);
            return false;
        }
    }
    
    @Override
    public PageResult<WebhookRecordVO> getWebhookRecords(Long projectId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<WebhookRecord> wrapper = new LambdaQueryWrapper<>();
        
        if (projectId != null) {
            wrapper.eq(WebhookRecord::getProjectId, projectId);
        }
        
        wrapper.orderByDesc(WebhookRecord::getReceivedTime);
        
        Page<WebhookRecord> page = new Page<>(pageNum, pageSize);
        Page<WebhookRecord> resultPage = webhookRecordMapper.selectPage(page, wrapper);
        
        List<WebhookRecordVO> voList = resultPage.getRecords().stream()
            .map(this::convertRecordToVO)
            .collect(Collectors.toList());
        
        return new PageResult<>(
            resultPage.getTotal(),
            voList,
            pageNum,
            pageSize
        );
    }
    
    @Override
    public WebhookRecordVO getWebhookRecord(Long id) {
        WebhookRecord record = webhookRecordMapper.selectById(id);
        if (record == null) {
            throw new RuntimeException("Webhook记录不存在");
        }
        return convertRecordToVO(record);
    }
    
    /**
     * 异步处理Webhook
     */
    @Async
    public void processWebhookAsync(Long recordId, WebhookPayload payload) {
        WebhookRecord record = webhookRecordMapper.selectById(recordId);
        
        try {
            // 只处理push事件
            if (!"push".equals(payload.getObjectKind())) {
                record.setStatus(3); // 已跳过
                record.setProcessedTime(LocalDateTime.now());
                webhookRecordMapper.updateById(record);
                return;
            }
            
            // 这里可以实现：
            // 1. 获取代码变更（通过GitLab API）
            // 2. 分析影响范围
            // 3. 自动生成测试用例
            // 4. 触发测试执行
            
            // 目前简化处理
            log.info("处理Push事件: 分支={}, 提交={}", payload.getBranchName(), record.getCommitId());
            
            // 模拟处理
            Thread.sleep(2000);
            
            record.setStatus(1); // 处理成功
            record.setProcessedTime(LocalDateTime.now());
            record.setGeneratedCases(0); // 实际应该是生成的用例数
            
        } catch (Exception e) {
            log.error("处理Webhook失败", e);
            record.setStatus(2); // 处理失败
            record.setErrorMessage(e.getMessage());
            record.setProcessedTime(LocalDateTime.now());
        }
        
        webhookRecordMapper.updateById(record);
    }
    
    /**
     * 转换为VO
     */
    private GitlabConfigVO convertToVO(GitlabConfig config) {
        GitlabConfigVO vo = new GitlabConfigVO();
        BeanUtil.copyProperties(config, vo);
        
        // 获取项目名称
        if (config.getProjectId() != null) {
            Project project = projectMapper.selectById(config.getProjectId());
            if (project != null) {
                vo.setProjectName(project.getName());
            }
        }
        
        // 获取创建人名称
        if (config.getCreatedBy() != null) {
            User user = userMapper.selectById(config.getCreatedBy());
            if (user != null) {
                vo.setCreatedByName(user.getNickname() != null ? user.getNickname() : user.getUsername());
            }
        }
        
        // 生成Webhook URL
        vo.setWebhookUrl("http://localhost:" + serverPort + "/api/gitlab/webhook");
        
        return vo;
    }
    
    /**
     * 转换记录为VO
     */
    private WebhookRecordVO convertRecordToVO(WebhookRecord record) {
        WebhookRecordVO vo = new WebhookRecordVO();
        BeanUtil.copyProperties(record, vo);
        
        // 获取项目名称
        if (record.getProjectId() != null) {
            Project project = projectMapper.selectById(record.getProjectId());
            if (project != null) {
                vo.setProjectName(project.getName());
            }
        }
        
        // 状态标签
        vo.setStatusLabel(getStatusLabel(record.getStatus()));
        
        // 计算处理时长
        if (record.getReceivedTime() != null && record.getProcessedTime() != null) {
            Duration duration = Duration.between(record.getReceivedTime(), record.getProcessedTime());
            vo.setProcessingTime(duration.toMillis());
        }
        
        return vo;
    }
    
    /**
     * 获取状态标签
     */
    private String getStatusLabel(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "处理中";
            case 1: return "成功";
            case 2: return "失败";
            case 3: return "已跳过";
            default: return "未知";
        }
    }
}
