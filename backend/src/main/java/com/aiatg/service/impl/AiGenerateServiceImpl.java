package com.aiatg.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.aiatg.ai.AiClient;
import com.aiatg.ai.AiClientFactory;
import com.aiatg.common.PageResult;
import com.aiatg.dto.AiGenerateRequest;
import com.aiatg.dto.TestCaseDTO;
import com.aiatg.dto.TestStepDTO;
import com.aiatg.entity.AiConfig;
import com.aiatg.entity.AiGenerateHistory;
import com.aiatg.entity.PromptTemplate;
import com.aiatg.entity.Requirement;
import com.aiatg.mapper.AiConfigMapper;
import com.aiatg.mapper.AiGenerateHistoryMapper;
import com.aiatg.mapper.RequirementMapper;
import com.aiatg.service.AiGenerateService;
import com.aiatg.service.PromptTemplateService;
import com.aiatg.service.TestCaseService;
import com.aiatg.vo.AiGenerateResponse;
import com.aiatg.vo.TestCaseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI生成服务实现类
 */
@Slf4j
@Service
public class AiGenerateServiceImpl implements AiGenerateService {
    
    @Autowired
    private AiClientFactory aiClientFactory;
    
    @Autowired
    private AiConfigMapper aiConfigMapper;
    
    @Autowired
    private AiGenerateHistoryMapper historyMapper;
    
    @Autowired
    private RequirementMapper requirementMapper;
    
    @Autowired
    private PromptTemplateService promptTemplateService;
    
    @Autowired
    private TestCaseService testCaseService;
    
    @Override
    @Transactional
    public AiGenerateResponse generateFromRequirement(AiGenerateRequest request, Long userId) {
        long startTime = System.currentTimeMillis();
        
        // 获取需求信息
        Requirement requirement = requirementMapper.selectById(request.getRequirementId());
        if (requirement == null) {
            throw new RuntimeException("需求不存在");
        }
        
        // 获取AI配置
        AiConfig config = getAiConfig(request.getProvider());
        
        // 构建提示词
        String prompt = buildPrompt(request, requirement);
        
        // 创建历史记录
        AiGenerateHistory history = new AiGenerateHistory();
        history.setRequirementId(request.getRequirementId());
        history.setProvider(config.getProvider());
        history.setModelName(config.getModelName());
        history.setPrompt(prompt);
        history.setCreatedBy(userId);
        history.setCreatedTime(LocalDateTime.now());
        history.setStatus(0); // 处理中
        
        historyMapper.insert(history);
        
        try {
            // 调用AI生成
            AiClient client = aiClientFactory.getClient(config);
            Double temperature = request.getTemperature() != null ? request.getTemperature() : 0.7;
            Integer maxTokens = config.getMaxTokens();
            
            String response = client.generate(prompt, temperature, maxTokens);
            
            // 解析响应并保存测试用例
            List<TestCaseVO> testCases = parseAndSaveTestCases(response, requirement, userId);
            
            // 更新历史记录
            long duration = System.currentTimeMillis() - startTime;
            history.setResponse(response);
            history.setGeneratedCount(testCases.size());
            history.setDuration(duration);
            history.setStatus(1); // 成功
            historyMapper.updateById(history);
            
            // 构建响应
            AiGenerateResponse aiResponse = new AiGenerateResponse();
            aiResponse.setHistoryId(history.getId());
            aiResponse.setGeneratedCount(testCases.size());
            aiResponse.setTestCases(testCases);
            aiResponse.setDuration(duration);
            aiResponse.setProvider(config.getProvider());
            aiResponse.setModelName(config.getModelName());
            
            return aiResponse;
            
        } catch (Exception e) {
            log.error("AI生成失败", e);
            
            // 更新历史记录为失败
            history.setStatus(2); // 失败
            history.setErrorMessage(e.getMessage());
            history.setDuration(System.currentTimeMillis() - startTime);
            historyMapper.updateById(history);
            
            throw new RuntimeException("AI生成失败: " + e.getMessage());
        }
    }
    
    @Override
    public PageResult<AiGenerateHistory> getGenerateHistory(Long requirementId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<AiGenerateHistory> wrapper = new LambdaQueryWrapper<>();
        
        if (requirementId != null) {
            wrapper.eq(AiGenerateHistory::getRequirementId, requirementId);
        }
        
        wrapper.orderByDesc(AiGenerateHistory::getCreatedTime);
        
        Page<AiGenerateHistory> page = new Page<>(pageNum, pageSize);
        Page<AiGenerateHistory> resultPage = historyMapper.selectPage(page, wrapper);
        
        return new PageResult<>(
            resultPage.getTotal(),
            resultPage.getRecords(),
            pageNum,
            pageSize
        );
    }
    
    @Override
    public AiGenerateHistory getHistoryById(Long id) {
        return historyMapper.selectById(id);
    }
    
    /**
     * 获取AI配置
     */
    private AiConfig getAiConfig(String provider) {
        LambdaQueryWrapper<AiConfig> wrapper = new LambdaQueryWrapper<>();
        
        if (provider != null && !provider.isEmpty()) {
            wrapper.eq(AiConfig::getProvider, provider);
        } else {
            wrapper.eq(AiConfig::getIsDefault, 1);
        }
        
        wrapper.eq(AiConfig::getStatus, 1);
        wrapper.last("LIMIT 1");
        
        AiConfig config = aiConfigMapper.selectOne(wrapper);
        if (config == null) {
            throw new RuntimeException("AI配置不存在");
        }
        
        return config;
    }
    
    /**
     * 构建提示词
     */
    private String buildPrompt(AiGenerateRequest request, Requirement requirement) {
        // 如果有自定义提示词，直接使用
        if (request.getCustomPrompt() != null && !request.getCustomPrompt().isEmpty()) {
            return request.getCustomPrompt();
        }
        
        // 使用模板
        PromptTemplate template;
        if (request.getTemplateId() != null) {
            template = promptTemplateService.getTemplateById(request.getTemplateId());
        } else {
            template = promptTemplateService.getDefaultTemplate("requirement");
        }
        
        if (template != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("title", requirement.getTitle());
            data.put("content", requirement.getContent());
            data.put("type", requirement.getType());
            data.put("count", request.getCount());
            
            return promptTemplateService.renderTemplate(template.getId(), data);
        }
        
        // 默认提示词
        return buildDefaultPrompt(requirement, request.getCount());
    }
    
    /**
     * 构建默认提示词
     */
    private String buildDefaultPrompt(Requirement requirement, Integer count) {
        return String.format(
            "请根据以下需求生成 %d 个测试用例，以JSON数组格式返回。\n\n" +
            "需求标题: %s\n" +
            "需求内容: %s\n" +
            "需求类型: %s\n\n" +
            "请返回JSON数组格式，每个测试用例包含以下字段:\n" +
            "- title: 用例标题\n" +
            "- description: 用例描述\n" +
            "- preconditions: 前置条件\n" +
            "- type: 用例类型（functional/ui/api/performance）\n" +
            "- priority: 优先级（P0/P1/P2/P3）\n" +
            "- steps: 测试步骤数组，每个步骤包含stepOrder、stepDescription、expectedResult\n\n" +
            "请只返回JSON数组，不要包含其他文字说明。",
            count,
            requirement.getTitle(),
            requirement.getContent() != null ? requirement.getContent() : "",
            requirement.getType()
        );
    }
    
    /**
     * 解析响应并保存测试用例
     */
    private List<TestCaseVO> parseAndSaveTestCases(String response, Requirement requirement, Long userId) {
        List<TestCaseVO> result = new ArrayList<>();
        
        try {
            // 提取JSON数组（去掉可能的markdown代码块标记）
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
            JSONArray testCasesJson = JSONUtil.parseArray(jsonStr);
            
            for (int i = 0; i < testCasesJson.size(); i++) {
                JSONObject caseJson = testCasesJson.getJSONObject(i);
                
                // 创建测试用例DTO
                TestCaseDTO dto = new TestCaseDTO();
                dto.setProjectId(requirement.getProjectId());
                dto.setRequirementId(requirement.getId());
                dto.setTitle(caseJson.getStr("title"));
                dto.setPreconditions(caseJson.getStr("preconditions"));
                dto.setType(caseJson.getStr("type", "functional"));
                dto.setPriority(caseJson.getStr("priority", "P2"));
                dto.setStatus("draft");
                dto.setSource("ai");
                
                // 解析测试步骤
                JSONArray stepsJson = caseJson.getJSONArray("steps");
                if (stepsJson != null && !stepsJson.isEmpty()) {
                    List<TestStepDTO> steps = new ArrayList<>();
                    for (int j = 0; j < stepsJson.size(); j++) {
                        JSONObject stepJson = stepsJson.getJSONObject(j);
                        TestStepDTO step = new TestStepDTO();
                        step.setStepOrder(j + 1);
                        step.setStepDescription(stepJson.getStr("stepDescription"));
                        step.setExpectedResult(stepJson.getStr("expectedResult"));
                        steps.add(step);
                    }
                    dto.setSteps(steps);
                }
                
                // 保存测试用例
                TestCaseVO vo = testCaseService.createTestCase(dto, userId);
                result.add(vo);
            }
            
        } catch (Exception e) {
            log.error("解析AI响应失败", e);
            throw new RuntimeException("解析AI响应失败: " + e.getMessage());
        }
        
        return result;
    }
}
