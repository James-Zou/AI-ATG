package com.aiatg.service;

import com.aiatg.common.PageResult;
import com.aiatg.dto.GitlabConfigDTO;
import com.aiatg.dto.WebhookPayload;
import com.aiatg.vo.GitlabConfigVO;
import com.aiatg.vo.WebhookRecordVO;

/**
 * GitLab服务接口
 */
public interface GitlabService {
    
    /**
     * 创建GitLab配置
     */
    GitlabConfigVO createConfig(GitlabConfigDTO dto, Long userId);
    
    /**
     * 更新GitLab配置
     */
    GitlabConfigVO updateConfig(Long id, GitlabConfigDTO dto);
    
    /**
     * 获取配置详情
     */
    GitlabConfigVO getConfig(Long id);
    
    /**
     * 获取项目的GitLab配置
     */
    GitlabConfigVO getConfigByProject(Long projectId);
    
    /**
     * 删除配置
     */
    void deleteConfig(Long id);
    
    /**
     * 处理Webhook
     */
    void handleWebhook(String signature, WebhookPayload payload);
    
    /**
     * 验证Webhook签名
     */
    boolean verifySignature(String signature, String payload, String secret);
    
    /**
     * 获取Webhook记录列表
     */
    PageResult<WebhookRecordVO> getWebhookRecords(Long projectId, Integer pageNum, Integer pageSize);
    
    /**
     * 获取Webhook记录详情
     */
    WebhookRecordVO getWebhookRecord(Long id);
}
