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

import com.aiatg.common.PageResult;
import com.aiatg.entity.OperationLog;

/**
 * 操作日志服务接口
 */
public interface OperationLogService {
    
    /**
     * 记录操作日志
     */
    void logOperation(Long userId, String username, String operation, 
                     String method, String params, String ip, Long executionTime);
    
    /**
     * 获取操作日志列表
     */
    PageResult<OperationLog> getLogList(Integer pageNum, Integer pageSize);
}
