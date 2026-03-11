package com.aiatg.service;

import java.util.List;
import java.util.Map;

/**
 * AI服务接口
 */
public interface AIService {
    
    /**
     * 生成测试步骤（使用默认AI）
     * @param description 描述性语言
     * @return 测试步骤列表
     */
    List<Map<String, Object>> generateTestSteps(String description);
    
    /**
     * 生成测试步骤（指定AI提供商）
     * @param description 描述性语言
     * @param provider AI提供商（deepseek/qwen/zhipu），为空则使用默认
     * @return 测试步骤列表
     */
    List<Map<String, Object>> generateTestSteps(String description, String provider);
}
