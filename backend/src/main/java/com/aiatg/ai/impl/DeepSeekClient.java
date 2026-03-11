package com.aiatg.ai.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.aiatg.ai.AiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * DeepSeek AI客户端实现
 */
@Slf4j
@Component
public class DeepSeekClient implements AiClient {
    
    private static final String DEFAULT_API_URL = "https://api.deepseek.com/v1/chat/completions";
    private static final String DEFAULT_MODEL = "deepseek-chat";
    
    private String apiKey;
    private String apiUrl;
    private String modelName;
    
    public void configure(String apiKey, String apiUrl, String modelName) {
        this.apiKey = apiKey;
        
        // 智能处理API URL：如果只配置了基础URL，自动补全完整端点路径
        if (apiUrl != null) {
            // 如果URL以 /v1 结尾但没有 /chat/completions，自动补全
            if (apiUrl.endsWith("/v1") && !apiUrl.contains("/chat/completions")) {
                this.apiUrl = apiUrl + "/chat/completions";
                log.info("自动补全DeepSeek API URL: {} -> {}", apiUrl, this.apiUrl);
            } else {
                this.apiUrl = apiUrl;
            }
        } else {
            this.apiUrl = DEFAULT_API_URL;
        }
        
        this.modelName = modelName != null ? modelName : DEFAULT_MODEL;
    }
    
    @Override
    public String generate(String prompt, Double temperature, Integer maxTokens) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("DeepSeek API Key未配置");
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
            
            log.info("发送DeepSeek API请求: URL={}, Model={}", apiUrl, modelName);
            
            // 发送请求并获取完整响应
            cn.hutool.http.HttpResponse httpResponse = HttpRequest.post(apiUrl)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(requestBody.toString())
                .timeout(60000)
                .execute();
            
            int statusCode = httpResponse.getStatus();
            String responseBody = httpResponse.body();
            
            log.info("DeepSeek API响应: StatusCode={}, Body={}", statusCode, 
                    responseBody.length() > 500 ? responseBody.substring(0, 500) + "..." : responseBody);
            
            // 检查HTTP状态码
            if (statusCode != 200) {
                log.error("DeepSeek API返回非200状态码: {}, 响应内容: {}", statusCode, responseBody);
                throw new RuntimeException("DeepSeek API调用失败，状态码: " + statusCode + ", 错误信息: " + responseBody);
            }
            
            // 检查响应是否为空
            if (responseBody == null || responseBody.trim().isEmpty()) {
                log.error("DeepSeek API返回空响应");
                throw new RuntimeException("DeepSeek API返回空响应");
            }
            
            // 解析响应
            JSONObject responseJson = JSONUtil.parseObj(responseBody);
            String content = responseJson.getByPath("choices[0].message.content", String.class);
            
            // 检查内容是否为null或空
            if (content == null || content.trim().isEmpty()) {
                log.error("DeepSeek API返回的content为空或null，完整响应: {}", responseBody);
                throw new RuntimeException("DeepSeek API返回的响应格式异常，未找到有效内容");
            }
            
            return content;
            
        } catch (Exception e) {
            log.error("DeepSeek API调用失败", e);
            throw new RuntimeException("DeepSeek API调用失败: " + e.getMessage());
        }
    }
    
    @Override
    public String getProvider() {
        return "deepseek";
    }
    
    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isEmpty();
    }
}
