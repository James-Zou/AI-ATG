package com.aiatg.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.aiatg.ai.AiClient;
import com.aiatg.ai.AiClientFactory;
import com.aiatg.entity.AiConfig;
import com.aiatg.mapper.AiConfigMapper;
import com.aiatg.service.AIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI服务实现类
 */
@Slf4j
@Service
public class AIServiceImpl implements AIService {
    
    @Autowired
    private AiClientFactory aiClientFactory;
    
    @Autowired
    private AiConfigMapper aiConfigMapper;
    
    @Override
    public List<Map<String, Object>> generateTestSteps(String description) {
        return generateTestSteps(description, null);
    }
    
    @Override
    public List<Map<String, Object>> generateTestSteps(String description, String provider) {
        log.info("开始生成测试步骤，描述: {}, 提供商: {}", description, provider);
        
        List<Map<String, Object>> steps = new ArrayList<>();
        
        try {
            // 获取AI配置
            AiConfig config = getAiConfig(provider);
            
            // 如果配置可用，使用真实AI生成
            if (config != null) {
                log.info("使用AI模型生成: {} - {}", config.getProvider(), config.getModelName());
                steps = generateWithAI(description, config);
            } else {
                log.warn("未找到可用的AI配置，使用规则解析");
                steps = parseDescriptionToSteps(description);
            }
        } catch (Exception e) {
            log.error("AI生成失败，回退到规则解析", e);
            try {
                steps = parseDescriptionToSteps(description);
            } catch (Exception e2) {
                log.error("规则解析也失败，返回默认步骤", e2);
                steps = getDefaultSteps();
            }
        }
        
        return steps;
    }
    
    /**
     * 使用AI生成测试步骤
     */
    private List<Map<String, Object>> generateWithAI(String description, AiConfig config) {
        try {
            // 构建Prompt
            String prompt = buildStepGenerationPrompt(description);
            
            // 调用AI生成
            AiClient client = aiClientFactory.getClient(config);
            String response = client.generate(prompt, config.getTemperature(), config.getMaxTokens());
            
            log.info("AI生成原始响应: {}", response);
            
            // 解析JSON响应
            List<Map<String, Object>> steps = parseAIResponse(response);
            
            if (steps == null || steps.isEmpty()) {
                log.warn("AI返回空结果，使用规则解析");
                return parseDescriptionToSteps(description);
            }
            
            return steps;
            
        } catch (Exception e) {
            log.error("AI生成失败", e);
            throw e;
        }
    }
    
    /**
     * 构建步骤生成的Prompt
     */
    private String buildStepGenerationPrompt(String description) {
        return String.format(
            "你是一个Web UI自动化测试专家。请将以下测试场景描述转换为Selenium可执行的JSON步骤数组。\n\n" +
            "测试场景描述：\n%s\n\n" +
            "要求：\n" +
            "1. 严格按照JSON数组格式输出，不要包含markdown代码块标记\n" +
            "2. 每个步骤必须包含 action 字段\n" +
            "3. 优先使用ID或CSS选择器定位元素\n" +
            "4. 添加适当的等待和断言步骤\n\n" +
            "支持的操作类型和格式：\n" +
            "- 打开URL: {\"action\": \"open\", \"input\": \"https://example.com\"}\n" +
            "- 点击元素: {\"action\": \"click\", \"locator\": \"id\", \"value\": \"btn-login\"}\n" +
            "- 输入文本: {\"action\": \"input\", \"locator\": \"id\", \"value\": \"username\", \"input\": \"admin\"}\n" +
            "- 等待: {\"action\": \"wait\", \"timeout\": 2}\n" +
            "- 选择下拉: {\"action\": \"select\", \"locator\": \"id\", \"value\": \"country\", \"input\": \"中国\"}\n" +
            "- 验证URL: {\"action\": \"assertUrl\", \"input\": \"https://example.com/success\"}\n" +
            "- 验证标题: {\"action\": \"assertTitle\", \"input\": \"登录成功\"}\n" +
            "- 验证文本: {\"action\": \"assertText\", \"locator\": \"css\", \"value\": \".message\", \"input\": \"欢迎\"}\n" +
            "- 验证可见: {\"action\": \"assertVisible\", \"locator\": \"id\", \"value\": \"dashboard\"}\n\n" +
            "定位器类型（locator）：id, css, xpath, name, className, linkText\n\n" +
            "请只返回JSON数组，不要包含任何其他文字说明。",
            description
        );
    }
    
    /**
     * 解析AI响应
     */
    private List<Map<String, Object>> parseAIResponse(String response) {
        try {
            // 清理可能的markdown标记
            String jsonStr = response.trim();
            if (jsonStr.startsWith("```json")) {
                jsonStr = jsonStr.substring(7);
            }
            if (jsonStr.startsWith("```")) {
                jsonStr = jsonStr.substring(3);
            }
            if (jsonStr.endsWith("```")) {
                jsonStr = jsonStr.substring(0, jsonStr.length() - 3);
            }
            jsonStr = jsonStr.trim();
            
            // 解析JSON数组
            JSONArray jsonArray = JSONUtil.parseArray(jsonStr);
            List<Map<String, Object>> steps = new ArrayList<>();
            
            for (int i = 0; i < jsonArray.size(); i++) {
                Map<String, Object> step = jsonArray.get(i, Map.class);
                if (step.containsKey("action")) {
                    steps.add(step);
                }
            }
            
            return steps;
            
        } catch (Exception e) {
            log.error("解析AI响应失败", e);
            return null;
        }
    }
    
    /**
     * 获取AI配置
     */
    private AiConfig getAiConfig(String provider) {
        LambdaQueryWrapper<AiConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiConfig::getStatus, 1); // 只查启用的
        
        if (provider != null && !provider.isEmpty()) {
            // 指定提供商
            wrapper.eq(AiConfig::getProvider, provider);
        } else {
            // 使用默认配置
            wrapper.eq(AiConfig::getIsDefault, 1);
        }
        
        wrapper.orderByDesc(AiConfig::getIsDefault);
        wrapper.orderByDesc(AiConfig::getCreatedTime);
        wrapper.last("LIMIT 1");
        
        AiConfig config = aiConfigMapper.selectOne(wrapper);
        
        if (config == null) {
            log.warn("未找到可用的AI配置");
        }
        
        return config;
    }
    
    /**
     * 解析描述为步骤
     */
    private List<Map<String, Object>> parseDescriptionToSteps(String description) {
        List<Map<String, Object>> steps = new ArrayList<>();
        
        // 分割成句子
        String[] sentences = description.split("[，。；,;]");
        
        for (String sentence : sentences) {
            sentence = sentence.trim();
            if (sentence.isEmpty()) continue;
            
            Map<String, Object> step = parseSentence(sentence);
            if (step != null) {
                steps.add(step);
            }
        }
        
        return steps;
    }
    
    /**
     * 解析单个句子
     */
    private Map<String, Object> parseSentence(String sentence) {
        Map<String, Object> step = new HashMap<>();
        
        // 打开URL
        if (matchesPattern(sentence, "打开|访问|进入")) {
            step.put("action", "open");
            String url = extractUrl(sentence);
            if (url != null) {
                step.put("input", url);
            } else {
                step.put("input", "https://www.example.com");
            }
            return step;
        }
        
        // 点击操作
        if (matchesPattern(sentence, "点击|单击")) {
            step.put("action", "click");
            String[] locatorInfo = extractLocator(sentence);
            step.put("locator", locatorInfo[0]);
            step.put("value", locatorInfo[1]);
            return step;
        }
        
        // 输入操作
        if (matchesPattern(sentence, "输入|填写|写入")) {
            step.put("action", "input");
            String[] locatorInfo = extractLocator(sentence);
            step.put("locator", locatorInfo[0]);
            step.put("value", locatorInfo[1]);
            
            // 提取输入内容
            String inputText = extractInputText(sentence);
            step.put("input", inputText);
            return step;
        }
        
        // 悬停操作
        if (matchesPattern(sentence, "悬停|鼠标移动|hover")) {
            step.put("action", "hover");
            String[] locatorInfo = extractLocator(sentence);
            step.put("locator", locatorInfo[0]);
            step.put("value", locatorInfo[1]);
            return step;
        }
        
        // 等待操作
        if (matchesPattern(sentence, "等待|延时|暂停")) {
            step.put("action", "wait");
            int timeout = extractTimeout(sentence);
            step.put("timeout", timeout);
            return step;
        }
        
        // 验证操作
        if (matchesPattern(sentence, "验证|检查|断言|确认")) {
            if (matchesPattern(sentence, "URL|地址|链接")) {
                step.put("action", "assertUrl");
                step.put("input", extractUrl(sentence));
            } else if (matchesPattern(sentence, "标题|title")) {
                step.put("action", "assertTitle");
                step.put("input", extractQuotedText(sentence));
            } else if (matchesPattern(sentence, "文本|内容|显示")) {
                step.put("action", "assertText");
                String[] locatorInfo = extractLocator(sentence);
                step.put("locator", locatorInfo[0]);
                step.put("value", locatorInfo[1]);
                step.put("input", extractQuotedText(sentence));
            } else if (matchesPattern(sentence, "可见|显示|出现")) {
                step.put("action", "assertVisible");
                String[] locatorInfo = extractLocator(sentence);
                step.put("locator", locatorInfo[0]);
                step.put("value", locatorInfo[1]);
            }
            return step;
        }
        
        // 选择下拉框
        if (matchesPattern(sentence, "选择|下拉")) {
            step.put("action", "select");
            String[] locatorInfo = extractLocator(sentence);
            step.put("locator", locatorInfo[0]);
            step.put("value", locatorInfo[1]);
            step.put("input", extractQuotedText(sentence));
            return step;
        }
        
        // 刷新页面
        if (matchesPattern(sentence, "刷新|重载")) {
            step.put("action", "refresh");
            return step;
        }
        
        return null;
    }
    
    /**
     * 匹配模式
     */
    private boolean matchesPattern(String text, String pattern) {
        String[] patterns = pattern.split("\\|");
        for (String p : patterns) {
            if (text.contains(p)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 提取URL
     */
    private String extractUrl(String text) {
        // 匹配http/https URL
        Pattern pattern = Pattern.compile("(https?://[^\\s]+)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        // 匹配域名
        pattern = Pattern.compile("([a-zA-Z0-9-]+\\.[a-zA-Z]{2,})");
        matcher = pattern.matcher(text);
        if (matcher.find()) {
            return "https://" + matcher.group(1);
        }
        
        return null;
    }
    
    /**
     * 提取定位器信息
     */
    private String[] extractLocator(String text) {
        String locator = "css";
        String value = "";
        
        // 提取ID定位
        Pattern idPattern = Pattern.compile("id\\s*[:：=]?\\s*['\"]([^'\"]+)['\"]");
        Matcher idMatcher = idPattern.matcher(text);
        if (idMatcher.find()) {
            return new String[]{"id", idMatcher.group(1)};
        }
        
        // 提取class定位
        Pattern classPattern = Pattern.compile("class\\s*[:：=]?\\s*['\"]([^'\"]+)['\"]");
        Matcher classMatcher = classPattern.matcher(text);
        if (classMatcher.find()) {
            return new String[]{"className", classMatcher.group(1)};
        }
        
        // 提取CSS选择器
        Pattern cssPattern = Pattern.compile("([.#][a-zA-Z0-9_-]+)");
        Matcher cssMatcher = cssPattern.matcher(text);
        if (cssMatcher.find()) {
            return new String[]{"css", cssMatcher.group(1)};
        }
        
        // 提取带引号的文本作为selector
        String quoted = extractQuotedText(text);
        if (quoted != null && !quoted.isEmpty()) {
            // 判断是class还是id
            if (quoted.startsWith(".") || quoted.startsWith("#")) {
                return new String[]{"css", quoted};
            }
            return new String[]{"css", "." + quoted};
        }
        
        // 默认返回
        return new String[]{"css", ".btn-submit"};
    }
    
    /**
     * 提取带引号的文本
     */
    private String extractQuotedText(String text) {
        Pattern pattern = Pattern.compile("['\"]([^'\"]+)['\"]");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }
    
    /**
     * 提取输入文本
     */
    private String extractInputText(String text) {
        // 优先提取引号中的内容
        String quoted = extractQuotedText(text);
        if (quoted != null && !quoted.isEmpty()) {
            return quoted;
        }
        
        // 提取"输入"后的内容
        Pattern pattern = Pattern.compile("输入\\s*[:：]?\\s*(.+)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        return "测试文本";
    }
    
    /**
     * 提取超时时间
     */
    private int extractTimeout(String text) {
        Pattern pattern = Pattern.compile("(\\d+)\\s*秒");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 1;
    }
    
    /**
     * 获取默认步骤
     */
    private List<Map<String, Object>> getDefaultSteps() {
        List<Map<String, Object>> steps = new ArrayList<>();
        
        Map<String, Object> step1 = new HashMap<>();
        step1.put("action", "open");
        step1.put("input", "https://www.baidu.com");
        steps.add(step1);
        
        Map<String, Object> step2 = new HashMap<>();
        step2.put("action", "wait");
        step2.put("timeout", 1);
        steps.add(step2);
        
        return steps;
    }
}
