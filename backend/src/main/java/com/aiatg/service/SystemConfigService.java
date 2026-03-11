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
package com.aiatg.service;

import com.aiatg.entity.SystemConfig;

import java.util.List;

/**
 * 系统配置服务接口
 */
public interface SystemConfigService {
    
    /**
     * 保存或更新配置
     */
    void saveConfig(String key, String value, String type, String description);
    
    /**
     * 获取配置
     */
    String getConfig(String key);
    
    /**
     * 获取所有配置
     */
    List<SystemConfig> getAllConfigs();
    
    /**
     * 删除配置
     */
    void deleteConfig(String key);
}
