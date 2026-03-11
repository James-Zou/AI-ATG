/*
 * Copyright 2026 James Zou
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aiatg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.aiatg.entity.SystemConfig;
import com.aiatg.mapper.SystemConfigMapper;
import com.aiatg.service.SystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统配置服务实现类
 */
@Slf4j
@Service
public class SystemConfigServiceImpl implements SystemConfigService {
    
    @Autowired
    private SystemConfigMapper configMapper;
    
    @Override
    @CacheEvict(value = "systemConfig", key = "#key")
    public void saveConfig(String key, String value, String type, String description) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, key);
        SystemConfig existing = configMapper.selectOne(wrapper);
        
        if (existing != null) {
            existing.setConfigValue(value);
            existing.setConfigType(type);
            existing.setDescription(description);
            existing.setUpdatedTime(LocalDateTime.now());
            configMapper.updateById(existing);
        } else {
            SystemConfig config = new SystemConfig();
            config.setConfigKey(key);
            config.setConfigValue(value);
            config.setConfigType(type);
            config.setDescription(description);
            config.setCreatedTime(LocalDateTime.now());
            configMapper.insert(config);
        }
        
        log.info("系统配置保存成功: {} = {}", key, value);
    }
    
    @Override
    @Cacheable(value = "systemConfig", key = "#key")
    public String getConfig(String key) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, key);
        SystemConfig config = configMapper.selectOne(wrapper);
        return config != null ? config.getConfigValue() : null;
    }
    
    @Override
    public List<SystemConfig> getAllConfigs() {
        return configMapper.selectList(null);
    }
    
    @Override
    @CacheEvict(value = "systemConfig", key = "#key")
    public void deleteConfig(String key) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, key);
        configMapper.delete(wrapper);
        
        log.info("系统配置删除成功: {}", key);
    }
}
