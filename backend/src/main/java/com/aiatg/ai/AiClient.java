package com.aiatg.ai;

/**
 * AI客户端接口
 */
public interface AiClient {
    
    /**
     * 生成文本
     * @param prompt 提示词
     * @param temperature 温度参数（0.0-1.0）
     * @param maxTokens 最大令牌数
     * @return 生成的文本
     */
    String generate(String prompt, Double temperature, Integer maxTokens);
    
    /**
     * 获取提供商名称
     */
    String getProvider();
    
    /**
     * 检查连接是否可用
     */
    boolean isAvailable();
}
