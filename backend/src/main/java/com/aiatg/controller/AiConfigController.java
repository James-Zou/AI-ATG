package com.aiatg.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.aiatg.common.Result;
import com.aiatg.entity.AiConfig;
import com.aiatg.mapper.AiConfigMapper;
import com.aiatg.util.AesUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI配置控制器
 */
@Slf4j
@RestController
@RequestMapping("/ai/config")
@CrossOrigin
public class AiConfigController {
    
    @Autowired
    private AiConfigMapper aiConfigMapper;
    
    /**
     * 获取所有配置
     */
    @GetMapping("/list")
    public Result<List<AiConfig>> getAllConfigs() {
        try {
            LambdaQueryWrapper<AiConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(AiConfig::getIsDefault);
            wrapper.orderByDesc(AiConfig::getCreatedTime);
            List<AiConfig> configs = aiConfigMapper.selectList(wrapper);
            
            // 脱敏显示 API Key
            for (AiConfig config : configs) {
                if (config.getApiKey() != null && !config.getApiKey().isEmpty()) {
                    config.setApiKey(AesUtil.maskApiKey(config.getApiKey()));
                }
            }
            
            return Result.success(configs);
        } catch (Exception e) {
            log.error("查询AI配置列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 创建配置
     */
    @PostMapping
    public Result<AiConfig> createConfig(@RequestBody AiConfig config) {
        try {
            // API Key 已经在前端加密，直接存储密文
            if (config.getApiKey() != null && !config.getApiKey().isEmpty()) {
                log.info("接收到加密的 API Key，直接存储");
            }
            
            config.setCreatedTime(LocalDateTime.now());
            if (config.getStatus() == null) {
                config.setStatus(1);
            }
            if (config.getIsDefault() == null) {
                config.setIsDefault(0);
            }
            
            aiConfigMapper.insert(config);
            
            // 脱敏返回
            if (config.getApiKey() != null && !config.getApiKey().isEmpty()) {
                config.setApiKey(AesUtil.maskApiKey(config.getApiKey()));
            }
            
            log.info("创建AI配置成功: provider={}, model={}", config.getProvider(), config.getModelName());
            return Result.success("创建成功", config);
        } catch (Exception e) {
            log.error("创建AI配置失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新配置
     */
    @PutMapping("/{id}")
    public Result<AiConfig> updateConfig(@PathVariable Long id, @RequestBody AiConfig config) {
        try {
            AiConfig existing = aiConfigMapper.selectById(id);
            if (existing == null) {
                return Result.error("配置不存在");
            }
            
            if (config.getProvider() != null) {
                existing.setProvider(config.getProvider());
            }
            if (config.getModelName() != null) {
                existing.setModelName(config.getModelName());
            }
            // API Key 更新处理：前端已加密或未传递
            if (config.getApiKey() != null && !config.getApiKey().isEmpty()) {
                existing.setApiKey(config.getApiKey());
                log.info("更新 API Key，已接收加密数据");
            }
            if (config.getApiUrl() != null) {
                existing.setApiUrl(config.getApiUrl());
            }
            if (config.getMaxTokens() != null) {
                existing.setMaxTokens(config.getMaxTokens());
            }
            if (config.getTemperature() != null) {
                existing.setTemperature(config.getTemperature());
            }
            if (config.getStatus() != null) {
                existing.setStatus(config.getStatus());
            }
            if (config.getIsDefault() != null) {
                existing.setIsDefault(config.getIsDefault());
            }
            
            existing.setUpdatedTime(LocalDateTime.now());
            aiConfigMapper.updateById(existing);
            
            // 脱敏返回
            if (existing.getApiKey() != null && !existing.getApiKey().isEmpty()) {
                existing.setApiKey(AesUtil.maskApiKey(existing.getApiKey()));
            }
            
            log.info("更新AI配置成功: id={}, provider={}, model={}", id, existing.getProvider(), existing.getModelName());
            return Result.success("更新成功", existing);
        } catch (Exception e) {
            log.error("更新AI配置失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除配置
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteConfig(@PathVariable Long id) {
        try {
            aiConfigMapper.deleteById(id);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
