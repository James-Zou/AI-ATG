package com.aiatg.service;

import com.aiatg.entity.Skill;

/**
 * 脚本执行服务接口
 */
public interface ScriptExecutionService {

    /**
     * 异步执行脚本
     * 
     * @param skill 技能信息
     * @param userId 用户ID
     * @return 执行记录ID
     */
    Long executeScript(Skill skill, Long userId);
    
    /**
     * 获取脚本执行状态
     * 
     * @param executionId 执行记录ID
     * @return 执行状态
     */
    String getExecutionStatus(Long executionId);
    
    /**
     * 获取脚本执行输出
     * 
     * @param executionId 执行记录ID
     * @return 执行输出
     */
    String getExecutionOutput(Long executionId);
}
