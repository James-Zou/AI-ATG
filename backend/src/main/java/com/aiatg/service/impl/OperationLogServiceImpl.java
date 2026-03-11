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

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.aiatg.common.PageResult;
import com.aiatg.entity.OperationLog;
import com.aiatg.mapper.OperationLogMapper;
import com.aiatg.service.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志服务实现类
 */
@Slf4j
@Service
public class OperationLogServiceImpl implements OperationLogService {
    
    @Autowired
    private OperationLogMapper logMapper;
    
    @Override
    @Async
    public void logOperation(Long userId, String username, String operation,
                            String method, String params, String ip, Long executionTime) {
        OperationLog operationLog = new OperationLog();
        operationLog.setUserId(userId);
        operationLog.setUsername(username);
        operationLog.setOperation(operation);
        operationLog.setMethod(method);
        operationLog.setParams(params);
        operationLog.setIp(ip);
        operationLog.setExecutionTime(executionTime);
        operationLog.setCreatedTime(LocalDateTime.now());
        
        logMapper.insert(operationLog);
        log.debug("操作日志记录成功: {} - {}", username, operation);
    }
    
    @Override
    public PageResult<OperationLog> getLogList(Integer pageNum, Integer pageSize) {
        Page<OperationLog> page = new Page<>(pageNum, pageSize);
        Page<OperationLog> resultPage = logMapper.selectPage(page, null);
        
        return new PageResult<>(
            resultPage.getTotal(),
            resultPage.getRecords(),
            pageNum,
            pageSize
        );
    }
}
