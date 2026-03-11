package com.aiatg.ai;

import com.aiatg.ai.impl.DeepSeekClient;
import com.aiatg.ai.impl.QwenClient;
import com.aiatg.ai.impl.ZhipuClient;
import com.aiatg.entity.AiConfig;
import com.aiatg.util.AesUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * AI客户端工厂
 */
@Slf4j
@Component
public class AiClientFactory {
    
    @Autowired
    private DeepSeekClient deepSeekClient;
    
    @Autowired
    private QwenClient qwenClient;
    
    @Autowired
    private ZhipuClient zhipuClient;
    
    /**
     * 根据配置获取AI客户端
     */
    public AiClient getClient(AiConfig config) {
        log.info("AiClientFactory.getClient 开始，config: {}", config != null ? config.getProvider() : "null");
        
        if (config == null) {
            throw new RuntimeException("AI配置不能为空");
        }
        
        log.info("开始解密 API Key，provider: {}, apiKey长度: {}", 
            config.getProvider(), 
            config.getApiKey() != null ? config.getApiKey().length() : 0);
        
        // 解密 API Key
        String decryptedApiKey;
        try {
            if (config.getApiKey() == null || config.getApiKey().isEmpty()) {
                throw new RuntimeException("API Key 不能为空");
            }
            
            // 对加密的 API Key 进行解密
            decryptedApiKey = AesUtil.decrypt(config.getApiKey());
            log.info("API Key 解密成功，provider={}, 解密后长度: {}", 
                config.getProvider(), 
                decryptedApiKey != null ? decryptedApiKey.length() : 0);
            
        } catch (Exception e) {
            log.error("API Key 解密失败", e);
            throw new RuntimeException("API Key 解密失败: " + e.getMessage());
        }
        
        AiClient client = null;
        String provider = config.getProvider().toLowerCase();
        
        switch (provider) {
            case "deepseek":
                deepSeekClient.configure(decryptedApiKey, config.getApiUrl(), config.getModelName());
                client = deepSeekClient;
                break;
            case "qwen":
                qwenClient.configure(decryptedApiKey, config.getApiUrl(), config.getModelName());
                client = qwenClient;
                break;
            case "zhipu":
                zhipuClient.configure(decryptedApiKey, config.getApiUrl(), config.getModelName());
                client = zhipuClient;
                break;
            default:
                throw new RuntimeException("不支持的AI提供商: " + provider);
        }
        
        log.info("检查AI客户端可用性");
        if (!client.isAvailable()) {
            throw new RuntimeException("AI客户端不可用，请检查配置");
        }
        
        log.info("AI客户端创建成功并可用，provider: {}", config.getProvider());
        return client;
    }
    
    /**
     * 根据提供商名称获取客户端
     * 注意：此方法接收的 apiKey 应该已经是加密后的密文
     */
    public AiClient getClient(String provider, String apiKey, String apiUrl, String modelName) {
        AiConfig config = new AiConfig();
        config.setProvider(provider);
        config.setApiKey(apiKey);  // 假设传入的是加密后的 API Key
        config.setApiUrl(apiUrl);
        config.setModelName(modelName);
        
        return getClient(config);
    }
}
