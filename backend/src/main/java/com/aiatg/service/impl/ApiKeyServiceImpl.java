package com.aiatg.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;
import com.aiatg.entity.ApiKey;
import com.aiatg.mapper.ApiKeyMapper;
import com.aiatg.service.ApiKeyService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * API Key Service 实现类
 */
@Slf4j
@Service
public class ApiKeyServiceImpl extends ServiceImpl<ApiKeyMapper, ApiKey> implements ApiKeyService {

    @Override
    public ApiKey validateApiKey(String apiKey) {
        LambdaQueryWrapper<ApiKey> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiKey::getApiKey, apiKey)
               .eq(ApiKey::getStatus, 1); // 只查询启用状态的 API Key
        
        ApiKey entity = this.getOne(wrapper);
        
        if (entity != null) {
            // 验证通过后，异步更新最后使用时间
            updateLastUsedTime(apiKey);
        }
        
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiKey generateApiKey(String userId, String accountId, String appName, String description, LocalDateTime expireTime) {
        ApiKey apiKey = new ApiKey();
        apiKey.setId(IdUtil.fastSimpleUUID());
        apiKey.setUserId(userId);
        apiKey.setAccountId(accountId);
        apiKey.setAppName(appName);
        apiKey.setDescription(description);
        apiKey.setExpireTime(expireTime);
        apiKey.setStatus(1);
        
        // 生成 API Key（带前缀，32字符）
        String apiKeyStr = "atg_" + IdUtil.fastSimpleUUID();
        apiKey.setApiKey(apiKeyStr);
        
        // 生成 Secret Key（64字符，SHA256加密）
        String secretKey = SecureUtil.sha256(IdUtil.fastSimpleUUID() + System.currentTimeMillis());
        apiKey.setSecretKey(secretKey);
        
        this.save(apiKey);
        log.info("生成新的 API Key: appName={}, userId={}, apiKey={}", appName, userId, apiKeyStr);
        
        return apiKey;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean revokeApiKey(String apiKey) {
        LambdaUpdateWrapper<ApiKey> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ApiKey::getApiKey, apiKey)
               .set(ApiKey::getStatus, 0); // 设置为禁用状态
        
        boolean success = this.update(wrapper);
        
        if (success) {
            log.info("API Key 已撤销: apiKey={}", apiKey);
        } else {
            log.warn("API Key 撤销失败: apiKey={}", apiKey);
        }
        
        return success;
    }

    @Override
    public void updateLastUsedTime(String apiKey) {
        try {
            LambdaUpdateWrapper<ApiKey> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(ApiKey::getApiKey, apiKey)
                   .set(ApiKey::getLastUsedTime, LocalDateTime.now());
            
            this.update(wrapper);
        } catch (Exception e) {
            // 更新最后使用时间失败不影响主流程，只记录日志
            log.error("更新 API Key 最后使用时间失败: apiKey={}", apiKey, e);
        }
    }
}
