package com.aiatg.service;

/**
 * ATG-Client 结果处理服务接口
 */
public interface AtgClientResultService {
    
    /**
     * 保存 ATG-Client 推送的执行结果
     */
    void saveResult(Long executionId, Long testCaseId, String status, Long duration,
                   String logs, String errorMessage, String screenshot);
}
