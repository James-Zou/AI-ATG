package com.aiatg.controller;

import com.aiatg.common.Result;
import com.aiatg.dto.GenerateApiKeyDTO;
import com.aiatg.entity.ApiKey;
import com.aiatg.service.ApiKeyService;
import com.aiatg.util.UserHolder;
import com.aiatg.vo.ApiKeyVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * API Key 管理 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api-key")
public class ApiKeyController {

    @Autowired
    private ApiKeyService apiKeyService;

    /**
     * 为当前登录用户生成 API Key
     */
    @PostMapping("/generate")
    public Result<ApiKeyVO> generateApiKey(@Validated @RequestBody GenerateApiKeyDTO dto) {
        String userId = UserHolder.getUserId();
        String accountId = UserHolder.getAccountId();
        
        if (userId == null || accountId == null) {
            return Result.error("用户未登录");
        }
        
        LocalDateTime expireTime = null;
        if (dto.getExpireDays() != null && dto.getExpireDays() > 0) {
            expireTime = LocalDateTime.now().plusDays(dto.getExpireDays());
        }
        
        ApiKey apiKey = apiKeyService.generateApiKey(
            userId, 
            accountId, 
            dto.getAppName(), 
            dto.getDescription(), 
            expireTime
        );
        
        ApiKeyVO vo = convertToVO(apiKey, false); // 生成时返回完整 Secret Key
        
        log.info("用户生成 API Key: userId={}, appName={}", userId, dto.getAppName());
        return Result.success(vo);
    }

    /**
     * 获取当前用户的所有 API Key
     */
    @GetMapping("/list")
    public Result<List<ApiKeyVO>> listMyApiKeys() {
        String userId = UserHolder.getUserId();
        
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        List<ApiKey> apiKeys = apiKeyService.lambdaQuery()
                .eq(ApiKey::getUserId, userId)
                .orderByDesc(ApiKey::getCreateTime)
                .list();
        
        List<ApiKeyVO> vos = apiKeys.stream()
                .map(apiKey -> convertToVO(apiKey, true)) // 列表查询时脱敏 Secret Key
                .collect(Collectors.toList());
        
        return Result.success(vos);
    }

    /**
     * 撤销（禁用）API Key
     */
    @PostMapping("/revoke")
    public Result<Void> revokeApiKey(@RequestParam String apiKey) {
        String userId = UserHolder.getUserId();
        
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        // 验证 API Key 是否属于当前用户
        ApiKey entity = apiKeyService.lambdaQuery()
                .eq(ApiKey::getApiKey, apiKey)
                .eq(ApiKey::getUserId, userId)
                .one();
        
        if (entity == null) {
            return Result.error("API Key 不存在或无权操作");
        }
        
        boolean success = apiKeyService.revokeApiKey(apiKey);
        
        if (success) {
            log.info("用户撤销 API Key: userId={}, apiKey={}", userId, apiKey);
            return Result.success();
        } else {
            return Result.error("撤销失败");
        }
    }

    /**
     * 删除 API Key
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteApiKey(@PathVariable String id) {
        String userId = UserHolder.getUserId();
        
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        // 验证 API Key 是否属于当前用户
        ApiKey entity = apiKeyService.getById(id);
        if (entity == null || !entity.getUserId().equals(userId)) {
            return Result.error("API Key 不存在或无权操作");
        }
        
        boolean success = apiKeyService.removeById(id);
        
        if (success) {
            log.info("用户删除 API Key: userId={}, apiKeyId={}", userId, id);
            return Result.success();
        } else {
            return Result.error("删除失败");
        }
    }

    /**
     * 转换为 VO 对象
     * @param apiKey 实体
     * @param maskSecretKey 是否脱敏 Secret Key
     */
    private ApiKeyVO convertToVO(ApiKey apiKey, boolean maskSecretKey) {
        ApiKeyVO vo = new ApiKeyVO();
        vo.setId(apiKey.getId());
        vo.setApiKey(apiKey.getApiKey());
        vo.setAppName(apiKey.getAppName());
        vo.setDescription(apiKey.getDescription());
        vo.setStatus(apiKey.getStatus());
        vo.setStatusText(apiKey.getStatus() == 1 ? "启用" : "禁用");
        vo.setExpireTime(apiKey.getExpireTime());
        vo.setLastUsedTime(apiKey.getLastUsedTime());
        vo.setCreateTime(apiKey.getCreateTime());
        
        // 判断是否过期
        vo.setExpired(apiKey.getExpireTime() != null && LocalDateTime.now().isAfter(apiKey.getExpireTime()));
        
        // Secret Key 脱敏处理
        if (maskSecretKey && apiKey.getSecretKey() != null) {
            String secret = apiKey.getSecretKey();
            vo.setSecretKey(secret.substring(0, 8) + "****" + secret.substring(secret.length() - 8));
        } else {
            vo.setSecretKey(apiKey.getSecretKey());
        }
        
        return vo;
    }
}
