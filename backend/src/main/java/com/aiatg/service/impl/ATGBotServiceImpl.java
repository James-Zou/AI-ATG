package com.aiatg.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.aiatg.ai.AiClient;
import com.aiatg.ai.AiClientFactory;
import com.aiatg.dto.ExecutionRequest;
import com.aiatg.entity.AiConfig;
import com.aiatg.entity.ChatMessage;
import com.aiatg.entity.Skill;
import com.aiatg.entity.TestSuite;
import com.aiatg.mapper.AiConfigMapper;
import com.aiatg.mapper.ChatMessageMapper;
import com.aiatg.mapper.SkillMapper;
import com.aiatg.mapper.TestSuiteMapper;
import com.aiatg.service.ATGBotService;
import com.aiatg.service.SkillService;
import com.aiatg.service.TestExecutionService;
import com.aiatg.vo.ChatResponseVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ATGBot服务实现类
 */
@Slf4j
@Service
public class ATGBotServiceImpl implements ATGBotService {
    
    @Autowired
    private AiClientFactory aiClientFactory;
    
    @Autowired
    private AiConfigMapper aiConfigMapper;
    
    @Autowired
    private TestSuiteMapper testSuiteMapper;
    
    @Autowired
    private ChatMessageMapper chatMessageMapper;
    
    @Autowired
    private TestExecutionService testExecutionService;
    
    @Autowired
    private SkillMapper skillMapper;
    
    @Autowired
    private SkillService skillService;
    
    @Override
    public ChatResponseVO processMessage(String message, Long userId) {
        log.info("ATGBot处理消息: {}, 用户: {}", message, userId);
        
        // 保存用户消息
        saveChatMessage(userId, "user", message, null, null);
        
        try {
            // 获取默认AI配置
            AiConfig config = getDefaultAiConfig();
            if (config == null) {
                return buildErrorResponse("AI配置未找到，请先配置AI服务");
            }
            
            // 调用AI分析用户意图
            log.info("开始构建AI提示");
            String aiPrompt = buildAnalysisPrompt(message);
            log.info("AI提示构建完成，长度: {}", aiPrompt != null ? aiPrompt.length() : 0);
            
            log.info("开始获取AI客户端，提供商: {}", config.getProvider());
            AiClient client = aiClientFactory.getClient(config);
            log.info("AI客户端获取成功，开始调用AI");
            
            String aiResponse = client.generate(aiPrompt, config.getTemperature(), config.getMaxTokens());
            log.info("AI调用完成，响应长度: {}", aiResponse != null ? aiResponse.length() : 0);
            
            // 检查AI响应
            if (aiResponse == null) {
                log.error("AI返回null响应，AI配置: provider={}, model={}", config.getProvider(), config.getModelName());
            }
            log.info("AI分析结果: {}", aiResponse);
            
            // 解析AI响应（现在parseAnalysisResult已能处理null）
            AnalysisResult analysisResult = parseAnalysisResult(aiResponse);
            
            // 根据分析结果构建响应
            ChatResponseVO response = buildChatResponse(analysisResult, userId);
            
            // 保存机器人回复
            saveChatMessage(userId, "assistant", response.getReply(), 
                analysisResult.suiteId, null);
            
            return response;
            
        } catch (Exception e) {
            log.error("处理消息失败", e);
            // 确保错误消息不为null
            String errorReply = "抱歉，我遇到了一些问题";
            if (e.getMessage() != null && !e.getMessage().isEmpty()) {
                errorReply += ": " + e.getMessage();
            }
            saveChatMessage(userId, "assistant", errorReply, null, null);
            return ChatResponseVO.builder()
                .reply(errorReply)
                .build();
        }
    }
    
    @Override
    public Long executeSuite(Long suiteId, Long userId) {
        log.info("执行测试套件: {}, 用户: {}", suiteId, userId);
        
        try {
            // 构建执行请求
            ExecutionRequest request = new ExecutionRequest();
            request.setSuiteId(suiteId);
            
            // 调用测试执行服务
            com.aiatg.vo.ExecutionVO executionVO = testExecutionService.createAndExecute(request, userId);
            Long executionId = executionVO.getId();
            
            // 更新聊天消息记录
            LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ChatMessage::getUserId, userId)
                   .eq(ChatMessage::getSuiteId, suiteId)
                   .orderByDesc(ChatMessage::getCreatedTime)
                   .last("LIMIT 1");
            
            ChatMessage chatMessage = chatMessageMapper.selectOne(wrapper);
            if (chatMessage != null) {
                chatMessage.setExecutionId(executionId);
                chatMessageMapper.updateById(chatMessage);
            }
            
            return executionId;
            
        } catch (Exception e) {
            log.error("执行测试套件失败", e);
            throw new RuntimeException("执行测试套件失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取默认AI配置
     */
    private AiConfig getDefaultAiConfig() {
        LambdaQueryWrapper<AiConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiConfig::getStatus, 1)
               .eq(AiConfig::getIsDefault, 1)
               .orderByDesc(AiConfig::getCreatedTime)
               .last("LIMIT 1");
        
        return aiConfigMapper.selectOne(wrapper);
    }
    
    /**
     * 构建分析Prompt
     */
    private String buildAnalysisPrompt(String userMessage) {
        log.info("开始构建分析提示，用户消息: {}", userMessage);
        
        // 获取所有可用的技能（Skills）
        log.info("开始查询启用的技能");
        LambdaQueryWrapper<Skill> skillWrapper = new LambdaQueryWrapper<>();
        skillWrapper.eq(Skill::getEnabled, true);
        List<Skill> allSkills = skillMapper.selectList(skillWrapper);
        log.info("查询到 {} 个启用的技能", allSkills != null ? allSkills.size() : 0);
        
        // 根据用户输入匹配最相关的前5个技能
        List<Skill> topSkills = findTopMatchingSkills(userMessage, allSkills, 5);
        
        String skillsInfo = topSkills.stream()
            .map(skill -> String.format("- ID: %d, 名称: %s, 描述: %s, 类型: %s", 
                skill.getId(), skill.getName(), 
                skill.getDescription() != null ? skill.getDescription() : "无描述",
                skill.getType()))
            .collect(Collectors.joining("\n"));
        
        return String.format(
            "你是一个企业级自动化AI助手ATGBot。用户发送了一条消息，请分析用户的意图。\n\n" +
            "用户消息：%s\n\n" +
            "根据用户消息内容，以下是最相关的前5个技能（Skills）：\n%s\n\n" +
            "请分析用户的意图并返回JSON格式：\n\n" +
            "**情况1**: 用户明确想执行某个技能(置信度 >= 0.8)\n" +
            "{\"intent\": \"execute_skill\", \"skillId\": 技能ID, \"confidence\": 置信度(0-1), \"parameters\": {参数键值对}, \"reply\": \"回复内容\"}\n\n" +
            "**情况2**: 用户可能想执行技能，但有多个相关技能无法精确判断\n" +
            "{\"intent\": \"select_skill\", \"candidateSkillIds\": [技能ID1, 技能ID2, 技能ID3], \"reply\": \"提示用户选择的内容\"}\n\n" +
            "**情况3**: 用户只是在询问或聊天\n" +
            "{\"intent\": \"chat\", \"reply\": \"回复内容\"}\n\n" +
            "分析规则：\n" +
            "1. 关键词匹配：运行、执行、启动、触发、调用等词汇\n" +
            "2. 技能名称/描述匹配：用户描述是否与某个技能相关\n" +
            "3. 优先从提供的前5个技能中选择，它们已经是最相关的\n" +
            "4. 如果有2-5个技能都符合用户描述，使用select_skill返回候选列表(candidateSkillIds最多返回5个)\n" +
            "5. 置信度低于0.8或有多个合适技能时，使用select_skill而不是直接执行\n" +
            "6. 完全不相关时使用chat询问更多信息\n\n" +
            "**参数提取规则**：\n" +
            "7. 当intent为execute_skill时，分析用户消息中是否包含需要传递给技能的参数值\n" +
            "8. 提取用户明确指定的参数值，例如：\n" +
            "   - \"工单标题是测试申请\" -> {\"工单标题\": \"测试申请\"}\n" +
            "   - \"使用手机号13800138000\" -> {\"手机号\": \"13800138000\"}\n" +
            "   - \"标题改为新测试，描述是自动化\" -> {\"标题\": \"新测试\", \"描述\": \"自动化\"}\n" +
            "9. 参数键名应使用中文，与技能配置中的字段描述对应\n" +
            "10. 如果没有明确的参数值，parameters可以为空对象{}\n\n" +
            "请只返回JSON对象，不要包含其他说明文字。",
            userMessage,
            skillsInfo
        );
    }
    
    /**
     * 查找与用户输入最匹配的前N个技能
     */
    private List<Skill> findTopMatchingSkills(String userMessage, List<Skill> allSkills, int topN) {
        if (allSkills == null || allSkills.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 将用户消息转换为小写以进行不区分大小写的匹配
        String lowerMessage = userMessage.toLowerCase();
        
        // 为每个技能计算匹配分数
        return allSkills.stream()
            .map(skill -> {
                double score = calculateMatchScore(lowerMessage, skill);
                return new SkillScore(skill, score);
            })
            .sorted((a, b) -> Double.compare(b.score, a.score))
            .limit(topN)
            .map(ss -> ss.skill)
            .collect(Collectors.toList());
    }
    
    /**
     * 计算技能与用户消息的匹配分数
     */
    private double calculateMatchScore(String lowerMessage, Skill skill) {
        double score = 0.0;
        
        String skillName = skill.getName() != null ? skill.getName().toLowerCase() : "";
        String skillDesc = skill.getDescription() != null ? skill.getDescription().toLowerCase() : "";
        
        // 1. 完全匹配技能名称（最高分）
        if (lowerMessage.contains(skillName) && !skillName.isEmpty()) {
            score += 100.0;
        }
        
        // 2. 技能名称的关键词匹配
        if (!skillName.isEmpty()) {
            String[] nameWords = skillName.split("[\\s\\-_]+");
            for (String word : nameWords) {
                if (!word.isEmpty() && lowerMessage.contains(word)) {
                    score += 30.0;
                }
            }
        }
        
        // 3. 技能描述的关键词匹配
        if (!skillDesc.isEmpty()) {
            String[] descWords = skillDesc.split("[\\s\\-_,，。.]+");
            for (String word : descWords) {
                if (word.length() > 1 && lowerMessage.contains(word)) {
                    score += 10.0;
                }
            }
        }
        
        // 4. 常见执行关键词匹配（提高相关性）
        if (lowerMessage.matches(".*(运行|执行|启动|触发|调用|开始).*")) {
            score += 5.0;
        }
        
        return score;
    }
    
    /**
     * 技能评分内部类
     */
    private static class SkillScore {
        Skill skill;
        double score;
        
        SkillScore(Skill skill, double score) {
            this.skill = skill;
            this.score = score;
        }
    }
    
    /**
     * 解析AI分析结果
     */
    private AnalysisResult parseAnalysisResult(String aiResponse) {
        // 处理null或空响应
        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            log.warn("AI返回结果为空");
            AnalysisResult result = new AnalysisResult();
            result.intent = "chat";
            result.reply = "抱歉，AI服务暂时无法响应，请稍后再试";
            return result;
        }
        
        try {
            // 清理markdown标记
            String jsonStr = aiResponse.trim();
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
            
            JSONObject json = JSONUtil.parseObj(jsonStr);
            
            AnalysisResult result = new AnalysisResult();
            result.intent = json.getStr("intent", "chat");
            result.reply = json.getStr("reply", "我理解了您的意思");
            result.suiteId = json.getLong("suiteId");
            result.skillId = json.getLong("skillId");
            result.confidence = json.getDouble("confidence", 0.0);
            
            // 解析候选技能ID列表
            if (json.containsKey("candidateSkillIds")) {
                result.candidateSkillIds = json.getJSONArray("candidateSkillIds")
                    .stream()
                    .map(obj -> ((Number) obj).longValue())
                    .collect(Collectors.toList());
            }
            
            // 解析参数
            if (json.containsKey("parameters")) {
                result.parameters = json.getJSONObject("parameters");
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("解析AI分析结果失败", e);
            AnalysisResult result = new AnalysisResult();
            result.intent = "chat";
            result.reply = aiResponse != null ? aiResponse : "抱歉，AI服务响应异常，请稍后再试";
            return result;
        }
    }
    
    /**
     * 构建聊天响应
     */
    private ChatResponseVO buildChatResponse(AnalysisResult analysisResult, Long userId) {
        ChatResponseVO.ChatResponseVOBuilder builder = ChatResponseVO.builder()
            .reply(analysisResult.reply);
        
        // 如果识别到执行技能意图
        if ("execute_skill".equals(analysisResult.intent) && analysisResult.skillId != null) {
            Skill skill = skillMapper.selectById(analysisResult.skillId);
            if (skill != null && skill.getEnabled()) {
                log.info("开始执行技能: ID={}, 名称={}, 参数={}", skill.getId(), skill.getName(), analysisResult.parameters);
                
                try {
                    // 调用技能执行服务
                    Long executionId = skillService.executeSkill(skill.getId(), userId, analysisResult.parameters);
                    log.info("技能执行完成，executionId={}", executionId);
                    
                    // 构建技能信息（包含执行ID
                    ChatResponseVO.SkillInfoVO skillInfo = ChatResponseVO.SkillInfoVO.builder()
                        .id(skill.getId())
                        .name(skill.getName())
                        .description(skill.getDescription())
                        .type(skill.getType())
                        .parameters(analysisResult.parameters)
                        .build();
                    builder.skillInfo(skillInfo);
                    
                    // 更新回复消息，添加执行成功信息
                    if (executionId != null) {
                        builder.reply(analysisResult.reply + "\n✅ 技能已执行，执行ID: " + executionId);
                    }
                    
                } catch (Exception e) {
                    log.error("执行技能失败: skillId={}, error={}", skill.getId(), e.getMessage(), e);
                    // 更新回复消息，添加执行失败信息
                    builder.reply("抱歉，执行技能时出现错误: " + e.getMessage());
                }
            } else {
                log.warn("技能不存在或未启用: skillId={}", analysisResult.skillId);
                builder.reply("抱歉，该技能不存在或未启用");
            }
        }
        
        // 如果AI返回了候选技能列表
        if ("select_skill".equals(analysisResult.intent) && 
            analysisResult.candidateSkillIds != null && 
            !analysisResult.candidateSkillIds.isEmpty()) {
            
            List<ChatResponseVO.SkillInfoVO> candidateSkillList = new ArrayList<>();
            for (Long skillId : analysisResult.candidateSkillIds) {
                Skill skill = skillMapper.selectById(skillId);
                if (skill != null && skill.getEnabled()) {
                    ChatResponseVO.SkillInfoVO skillInfo = ChatResponseVO.SkillInfoVO.builder()
                        .id(skill.getId())
                        .name(skill.getName())
                        .description(skill.getDescription())
                        .type(skill.getType())
                        .build();
                    candidateSkillList.add(skillInfo);
                }
            }
            
            if (!candidateSkillList.isEmpty()) {
                builder.candidateSkills(candidateSkillList);
            }
        }
        
        // 兼容旧的execute_suite意图（如果AI返回的是旧格式）
        if ("execute_suite".equals(analysisResult.intent) && analysisResult.suiteId != null) {
            TestSuite suite = testSuiteMapper.selectById(analysisResult.suiteId);
            if (suite != null) {
                ChatResponseVO.TestSuiteInfoVO suiteInfo = ChatResponseVO.TestSuiteInfoVO.builder()
                    .id(suite.getId())
                    .name(suite.getName())
                    .description(suite.getDescription())
                    .build();
                builder.suiteInfo(suiteInfo);
            }
        }
        
        return builder.build();
    }
    
    /**
     * 构建错误响应
     */
    private ChatResponseVO buildErrorResponse(String errorMessage) {
        return ChatResponseVO.builder()
            .reply(errorMessage)
            .build();
    }
    
    /**
     * 保存聊天消息
     */
    private void saveChatMessage(Long userId, String role, String content, 
                                 Long suiteId, Long executionId) {
        ChatMessage message = new ChatMessage();
        message.setUserId(userId);
        message.setRole(role);
        // 确保content不为null，避免数据库插入错误
        message.setContent(content != null && !content.trim().isEmpty() ? content : "[空消息]");
        message.setSuiteId(suiteId);
        message.setExecutionId(executionId);
        message.setCreatedTime(LocalDateTime.now());
        
        chatMessageMapper.insert(message);
    }
    
    /**
     * AI分析结果内部类
     */
    private static class AnalysisResult {
        String intent;                          // 意图：chat(聊天) / execute_skill(执行技能) / select_skill(选择技能) / execute_suite(执行套件-兼容)
        String reply;                           // 回复内容
        Long skillId;                           // 技能ID（如果是执行技能意图）
        List<Long> candidateSkillIds;           // 候选技能ID列表（如果是选择技能意图）
        Long suiteId;                           // 套件ID（如果是执行套件意图-兼容）
        Double confidence;                      // 置信度
        java.util.Map<String, Object> parameters; // 从用户消息中提取的参数（用于替换技能配置）
    }
}
