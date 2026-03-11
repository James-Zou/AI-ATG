package com.aiatg.service;

import com.aiatg.vo.ChatResponseVO;

/**
 * ATGBot服务接口
 */
public interface ATGBotService {
    
    /**
     * 处理聊天消息并分析指令
     * @param message 用户消息
     * @param userId 用户ID
     * @return 聊天响应
     */
    ChatResponseVO processMessage(String message, Long userId);
    
    /**
     * 执行测试套件
     * @param suiteId 套件ID
     * @param userId 用户ID
     * @return 执行ID
     */
    Long executeSuite(Long suiteId, Long userId);
}
