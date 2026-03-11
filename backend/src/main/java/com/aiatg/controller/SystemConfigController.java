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
package com.aiatg.controller;

import com.aiatg.common.PageResult;
import com.aiatg.common.Result;
import com.aiatg.entity.OperationLog;
import com.aiatg.entity.SystemConfig;
import com.aiatg.service.OperationLogService;
import com.aiatg.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统配置控制器
 */
@RestController
@RequestMapping("/system")
@CrossOrigin
public class SystemConfigController {
    
    @Autowired
    private SystemConfigService configService;
    
    @Autowired
    private OperationLogService logService;
    
    /**
     * 保存配置
     */
    @PostMapping("/config")
    public Result<Void> saveConfig(@RequestBody Map<String, String> params) {
        try {
            String key = params.get("key");
            String value = params.get("value");
            String type = params.get("type");
            String description = params.get("description");
            
            configService.saveConfig(key, value, type, description);
            return Result.success("配置保存成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取配置
     */
    @GetMapping("/config/{key}")
    public Result<String> getConfig(@PathVariable String key) {
        try {
            String value = configService.getConfig(key);
            return Result.success(value);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取所有配置
     */
    @GetMapping("/config/list")
    public Result<List<SystemConfig>> getAllConfigs() {
        try {
            List<SystemConfig> configs = configService.getAllConfigs();
            return Result.success(configs);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除配置
     */
    @DeleteMapping("/config/{key}")
    public Result<Void> deleteConfig(@PathVariable String key) {
        try {
            configService.deleteConfig(key);
            return Result.success("配置删除成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取操作日志
     */
    @GetMapping("/logs")
    public Result<PageResult<OperationLog>> getLogs(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        try {
            PageResult<OperationLog> result = logService.getLogList(pageNum, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
