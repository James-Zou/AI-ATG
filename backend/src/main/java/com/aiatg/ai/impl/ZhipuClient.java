package com.aiatg.ai.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.aiatg.ai.AiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 智谱AI客户端实现
 */
@Slf4j
@Component
public class ZhipuClient implements AiClient {
    
    private static final String DEFAULT_API_URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
    private static final String DEFAULT_MODEL = "glm-4";
    
    private String apiKey;
    private String apiUrl;
    private String modelName;
    
    public void configure(String apiKey, String apiUrl, String modelName) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl != null ? apiUrl : DEFAULT_API_URL;
        this.modelName = modelName != null ? modelName : DEFAULT_MODEL;
    }
    
    @Override
    public String generate(String prompt, Double temperature, Integer maxTokens) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("智谱 API Key未配置");
        }
        
        try {
            // 构建请求体
            JSONObject requestBody = new JSONObject();
            requestBody.set("model", modelName);
            requestBody.set("messages", JSONUtil.createArray()
                .put(JSONUtil.createObj()
                    .set("role", "user")
                    .set("content", prompt)
                )
            );
            
            if (temperature != null) {
                requestBody.set("temperature", temperature);
            }
            if (maxTokens != null) {
                requestBody.set("max_tokens", maxTokens);
            }
            
            // 发送请求
            String response = HttpRequest.post(apiUrl)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(requestBody.toString())
                .timeout(60000)
                .execute()
                .body();
            
            // 解析响应
            JSONObject responseJson = JSONUtil.parseObj(response);
            String content = responseJson.getByPath("choices[0].message.content", String.class);
            
            // 检查内容是否为null或空
            if (content == null || content.trim().isEmpty()) {
                log.error("智谱API返回的content为空或null，完整响应: {}", response);
                throw new RuntimeException("智谱API返回的响应格式异常，未找到有效内容");
            }
            
            return content;
            
        } catch (Exception e) {
            log.error("智谱 API调用失败", e);
            throw new RuntimeException("智谱 API调用失败: " + e.getMessage());
        }
    }
    
    @Override
    public String getProvider() {
        return "zhipu";
    }
    
    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isEmpty();
    }
}
