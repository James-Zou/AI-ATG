package com.aiatg.service;

import com.aiatg.common.PageResult;
import com.aiatg.dto.AiGenerateRequest;
import com.aiatg.entity.AiGenerateHistory;
import com.aiatg.vo.AiGenerateResponse;

/**
 * AI生成服务接口
 */
public interface AiGenerateService {
    
    /**
     * 从需求生成测试用例
     */
    AiGenerateResponse generateFromRequirement(AiGenerateRequest request, Long userId);
    
    /**
     * 获取生成历史
     */
    PageResult<AiGenerateHistory> getGenerateHistory(Long requirementId, Integer pageNum, Integer pageSize);
    
    /**
     * 获取历史详情
     */
    AiGenerateHistory getHistoryById(Long id);
}
