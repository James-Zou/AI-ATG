package com.aiatg.controller;

import com.aiatg.common.PageResult;
import com.aiatg.common.Result;
import com.aiatg.dto.GitlabConfigDTO;
import com.aiatg.dto.WebhookPayload;
import com.aiatg.service.GitlabService;
import com.aiatg.util.UserHolder;
import com.aiatg.vo.GitlabConfigVO;
import com.aiatg.vo.WebhookRecordVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * GitLab集成控制器
 */
@Slf4j
@RestController
@RequestMapping("/gitlab")
@CrossOrigin
public class GitlabController {
    
    @Autowired
    private GitlabService gitlabService;
    
    /**
     * 创建GitLab配置
     */
    @PostMapping("/config")
    public Result<GitlabConfigVO> createConfig(@Valid @RequestBody GitlabConfigDTO dto) {
        try {
            Long userId = getCurrentUserId();
            GitlabConfigVO vo = gitlabService.createConfig(dto, userId);
            return Result.success("配置创建成功", vo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新GitLab配置
     */
    @PutMapping("/config/{id}")
    public Result<GitlabConfigVO> updateConfig(
        @PathVariable Long id,
        @Valid @RequestBody GitlabConfigDTO dto
    ) {
        try {
            GitlabConfigVO vo = gitlabService.updateConfig(id, dto);
            return Result.success("配置更新成功", vo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取配置详情
     */
    @GetMapping("/config/{id}")
    public Result<GitlabConfigVO> getConfig(@PathVariable Long id) {
        try {
            GitlabConfigVO vo = gitlabService.getConfig(id);
            return Result.success(vo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取项目的GitLab配置
     */
    @GetMapping("/config/project/{projectId}")
    public Result<GitlabConfigVO> getConfigByProject(@PathVariable Long projectId) {
        try {
            GitlabConfigVO vo = gitlabService.getConfigByProject(projectId);
            if (vo == null) {
                return Result.error("该项目未配置GitLab集成");
            }
            return Result.success(vo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除配置
     */
    @DeleteMapping("/config/{id}")
    public Result<Void> deleteConfig(@PathVariable Long id) {
        try {
            gitlabService.deleteConfig(id);
            return Result.success("配置删除成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 接收GitLab Webhook
     * 注意：这个接口不需要认证，因为是GitLab回调
     */
    @PostMapping("/webhook")
    public Result<Void> handleWebhook(
        @RequestHeader(value = "X-Gitlab-Token", required = false) String token,
        @RequestHeader(value = "X-Gitlab-Event", required = false) String event,
        @RequestBody WebhookPayload payload
    ) {
        try {
            log.info("收到GitLab Webhook: event={}, objectKind={}", event, payload.getObjectKind());
            
            // 这里应该验证token，但简化处理
            gitlabService.handleWebhook(token, payload);
            
            return Result.success("Webhook处理成功", null);
        } catch (Exception e) {
            log.error("处理Webhook失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取Webhook记录列表
     */
    @GetMapping("/webhook/records")
    public Result<PageResult<WebhookRecordVO>> getWebhookRecords(
        @RequestParam(required = false) Long projectId,
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        try {
            PageResult<WebhookRecordVO> result = gitlabService.getWebhookRecords(projectId, pageNum, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取Webhook记录详情
     */
    @GetMapping("/webhook/records/{id}")
    public Result<WebhookRecordVO> getWebhookRecord(@PathVariable Long id) {
        try {
            WebhookRecordVO vo = gitlabService.getWebhookRecord(id);
            return Result.success(vo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        String userId = UserHolder.getUserId();
        return userId != null ? Long.valueOf(userId) : null;
    }
}
