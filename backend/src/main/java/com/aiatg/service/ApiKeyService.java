package com.aiatg.service;

import com.aiatg.entity.ApiKey;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDateTime;

/**
 * API Key Service 接口
 */
public interface ApiKeyService extends IService<ApiKey> {
    
    /**
     * 验证 API Key 是否有效
     * @param apiKey API Key 字符串
     * @return ApiKey 实体，无效则返回 null
     */
    ApiKey validateApiKey(String apiKey);
    
    /**
     * 为用户生成新的 API Key
     * @param userId 用户ID
     * @param accountId 账户ID
     * @param appName 应用名称
     * @param description 描述
     * @param expireTime 过期时间（null 表示永不过期）
     * @return 生成的 API Key
     */
    ApiKey generateApiKey(String userId, String accountId, String appName, String description, LocalDateTime expireTime);
    
    /**
     * 撤销（禁用）API Key
     * @param apiKey API Key 字符串
     * @return 是否成功
     */
    boolean revokeApiKey(String apiKey);
    
    /**
     * 更新 API Key 最后使用时间
     * @param apiKey API Key 字符串
     */
    void updateLastUsedTime(String apiKey);
}
