package com.aiatg.service.impl;

import com.aiatg.dto.SkillDTO;
import com.aiatg.entity.Skill;
import com.aiatg.entity.SuiteCaseRelation;
import com.aiatg.entity.TestCase;
import com.aiatg.entity.TestExecution;
import com.aiatg.entity.TestSuite;
import com.aiatg.mapper.SkillMapper;
import com.aiatg.mapper.SuiteCaseRelationMapper;
import com.aiatg.mapper.TestCaseMapper;
import com.aiatg.mapper.TestExecutionMapper;
import com.aiatg.mapper.TestSuiteMapper;
import com.aiatg.service.AsyncTestExecutionService;
import com.aiatg.service.ScriptExecutionService;
import com.aiatg.service.SkillService;
import com.aiatg.service.TestExecutionService;
import com.aiatg.vo.SkillVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 技能服务实现类
 */
@Slf4j
@Service
public class SkillServiceImpl implements SkillService {

    @Resource
    private SkillMapper skillMapper;

    @Resource
    private TestSuiteMapper testSuiteMapper;
    
    @Resource
    private SuiteCaseRelationMapper suiteCaseRelationMapper;
    
    @Resource
    private TestCaseMapper testCaseMapper;

    @Resource
    private TestExecutionService testExecutionService;
    
    @Resource
    private TestExecutionMapper executionMapper;
    
    @Resource
    private AsyncTestExecutionService asyncTestExecutionService;
    
    @Resource
    private ScriptExecutionService scriptExecutionService;

    @Override
    public Page<SkillVO> listSkills(String name, String type, Integer pageNum, Integer pageSize) {
        Page<Skill> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Skill> wrapper = new LambdaQueryWrapper<>();
        
        if (name != null && !name.isEmpty()) {
            wrapper.like(Skill::getName, name);
        }
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Skill::getType, type);
        }
        
        wrapper.orderByDesc(Skill::getCreateTime);
        
        Page<Skill> skillPage = skillMapper.selectPage(page, wrapper);
        Page<SkillVO> voPage = new Page<>(skillPage.getCurrent(), skillPage.getSize(), skillPage.getTotal());
        
        voPage.setRecords(skillPage.getRecords().stream().map(skill -> {
            SkillVO vo = new SkillVO();
            BeanUtils.copyProperties(skill, vo);
            
            // 如果是测试套件类型,加载套件名称
            if ("TESTSUITE".equals(skill.getType()) && skill.getTestSuiteId() != null) {
                TestSuite testSuite = testSuiteMapper.selectById(skill.getTestSuiteId());
                if (testSuite != null) {
                    vo.setTestSuiteName(testSuite.getName());
                }
            }
            
            return vo;
        }).collect(Collectors.toList()));
        
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSkill(SkillDTO skillDTO, Long userId) {
        Skill skill = new Skill();
        BeanUtils.copyProperties(skillDTO, skill);
        skill.setCreateBy(userId);
        skill.setUpdateBy(userId);
        
        skillMapper.insert(skill);
        log.info("创建技能成功, id: {}, name: {}", skill.getId(), skill.getName());
        
        return skill.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long importFromTestSuite(Long testSuiteId, String name, String description, Long userId, String configData) {
        TestSuite testSuite = testSuiteMapper.selectById(testSuiteId);
        if (testSuite == null) {
            throw new RuntimeException("测试套件不存在");
        }
        
        // 验证必填参数
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("技能名称不能为空");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new RuntimeException("技能描述不能为空");
        }
        
        // 移除重复导入检查：同一个套件可以导入多次成为不同的技能
        // testSuiteId 只是记录"来源"，不是强关联约束
        
        // 如果前端传递了 configData，使用传递的值；否则自动获取
        String finalConfigData = configData;
        if (finalConfigData == null || finalConfigData.trim().isEmpty()) {
            // 从测试套件中自动复制测试步骤配置数据
            // 1. 查询测试套件关联的所有测试用例ID
            LambdaQueryWrapper<SuiteCaseRelation> relationWrapper = new LambdaQueryWrapper<>();
            relationWrapper.eq(SuiteCaseRelation::getSuiteId, testSuiteId)
                           .orderByAsc(SuiteCaseRelation::getExecuteOrder);
            List<SuiteCaseRelation> relations = suiteCaseRelationMapper.selectList(relationWrapper);
            
            if (relations == null || relations.isEmpty()) {
                log.warn("测试套件下没有关联的测试用例, testSuiteId: {}", testSuiteId);
                throw new RuntimeException("测试套件下没有测试用例，无法导入");
            }
            
            // 2. 获取所有测试用例ID
            List<Long> caseIds = relations.stream()
                    .map(SuiteCaseRelation::getCaseId)
                    .collect(Collectors.toList());
            
            // 3. 查询测试用例
            List<TestCase> testCases = testCaseMapper.selectBatchIds(caseIds);
            
            // 4. 提取所有测试用例的steps字段（JSON格式），合并成统一的步骤数组
            JSONArray allSteps = new JSONArray();
            for (TestCase testCase : testCases) {
                if (testCase.getSteps() != null && !testCase.getSteps().isEmpty()) {
                    try {
                        JSONArray caseSteps = JSONUtil.parseArray(testCase.getSteps());
                        allSteps.addAll(caseSteps);
                    } catch (Exception e) {
                        log.warn("解析测试用例步骤失败, testCaseId: {}, error: {}", testCase.getId(), e.getMessage());
                    }
                }
            }
            finalConfigData = allSteps.toString();
        }
        
        Skill skill = new Skill();
        skill.setName(name.trim());
        skill.setDescription(description.trim());
        skill.setType("TESTSUITE");
        skill.setTestSuiteId(testSuiteId);
        skill.setConfigData(finalConfigData); // 保存测试步骤配置（来自前端或自动获取）
        skill.setEnabled(true);
        skill.setCreateBy(userId);
        skill.setUpdateBy(userId);
        
        skillMapper.insert(skill);
        log.info("从测试套件导入技能成功, skillId: {}, testSuiteId: {}", skill.getId(), testSuiteId);
        
        return skill.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSkill(Long id, SkillDTO skillDTO, Long userId) {
        Skill skill = skillMapper.selectById(id);
        if (skill == null) {
            throw new RuntimeException("技能不存在");
        }
        
        BeanUtils.copyProperties(skillDTO, skill);
        skill.setId(id);
        skill.setUpdateBy(userId);
        
        skillMapper.updateById(skill);
        log.info("更新技能成功, id: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSkill(Long id) {
        Skill skill = skillMapper.selectById(id);
        if (skill == null) {
            throw new RuntimeException("技能不存在");
        }
        
        skillMapper.deleteById(id);
        log.info("删除技能成功, id: {}", id);
    }

    @Override
    public SkillVO getSkillById(Long id) {
        Skill skill = skillMapper.selectById(id);
        if (skill == null) {
            throw new RuntimeException("技能不存在");
        }
        
        SkillVO vo = new SkillVO();
        BeanUtils.copyProperties(skill, vo);
        
        // 如果是测试套件类型，加载套件名称
        if ("TESTSUITE".equals(skill.getType()) && skill.getTestSuiteId() != null) {
            TestSuite testSuite = testSuiteMapper.selectById(skill.getTestSuiteId());
            if (testSuite != null) {
                vo.setTestSuiteName(testSuite.getName());
            }
        }
        
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long executeSkill(Long id, Long userId, java.util.Map<String, Object> parameters) {
        Skill skill = skillMapper.selectById(id);
        if (skill == null) {
            throw new RuntimeException("技能不存在");
        }
        
        if (!skill.getEnabled()) {
            throw new RuntimeException("技能未启用");
        }
        
        // 如果有参数需要替换，先处理参数替换
        if (parameters != null && !parameters.isEmpty()) {
            applyParametersToSkill(skill, parameters);
        }
        
        if ("TESTSUITE".equals(skill.getType())) {
            // 对于测试套件类型技能,使用 configData 中的步骤直接执行
            return executeTestSuiteSkill(skill, userId);
        } else if ("SCRIPT".equals(skill.getType())) {
            // 执行脚本
            Long executionId = scriptExecutionService.executeScript(skill, userId);
            log.info("执行脚本类型技能, skillId: {}, scriptExecutionId: {}", id, executionId);
            return executionId;
        }
        
        throw new RuntimeException("未知的技能类型");
    }
    
    /**
     * 执行测试套件类型的技能
     * 直接使用 configData 中的步骤，而不是查询数据库中的原始测试用例
     */
    private Long executeTestSuiteSkill(Skill skill, Long userId) {
        String configData = skill.getConfigData();
        if (configData == null || configData.trim().isEmpty()) {
            throw new RuntimeException("技能配置数据为空，无法执行");
        }
        
        // 获取关联的测试套件信息（用于获取项目ID等元数据）
        TestSuite testSuite = testSuiteMapper.selectById(skill.getTestSuiteId());
        if (testSuite == null) {
            throw new RuntimeException("关联的测试套件不存在");
        }
        
        try {
            // 解析 configData 判断测试类型（UI 还是 API）
            JSONArray steps = JSONUtil.parseArray(configData);
            String testType = detectTestType(steps);
            
            log.info("检测到技能测试类型: {}, skillId: {}", testType, skill.getId());
            
            // 创建临时测试用例对象（包含替换后的步骤）
            // 注意: 对于技能执行，不设置 testCaseId（保持为null），以区别于正常的测试用例执行
            TestCase tempTestCase = new TestCase();
            tempTestCase.setId(null); // 技能执行时使用null，不关联具体的测试用例
            tempTestCase.setProjectId(testSuite.getProjectId());
            tempTestCase.setTitle(skill.getName());
            tempTestCase.setSteps(configData); // 使用替换后的步骤
            tempTestCase.setType(testType);
            tempTestCase.setStatus("approved"); // 设置为已通过，确保可以执行
            
            // 创建执行记录
            TestExecution execution = new TestExecution();
            execution.setProjectId(testSuite.getProjectId());
            execution.setSuiteId(skill.getTestSuiteId());
            execution.setExecutionName("Skill执行: " + skill.getName());
            execution.setExecutionType("SKILL");
            execution.setEnvironment("test");
            execution.setStatus(1); // 执行中
            execution.setTotalCases(1);
            execution.setTriggerType("MANUAL");
            execution.setExecutedBy(userId);
            execution.setStartTime(java.time.LocalDateTime.now());
            execution.setCreatedTime(java.time.LocalDateTime.now());
            
            executionMapper.insert(execution);
            
            // 异步执行测试（使用正确的测试类型）
            asyncTestExecutionService.executeTestsAsync(
                execution.getId(), 
                Collections.singletonList(tempTestCase), 
                testType // 使用检测到的测试类型(UI/API)，而不是 "SKILL"
            );
            
            log.info("提交技能执行任务成功, skillId: {}, executionId: {}, testType: {}", 
                     skill.getId(), execution.getId(), testType);
            
            return execution.getId();
            
        } catch (Exception e) {
            log.error("执行测试套件类型技能失败, skillId: {}", skill.getId(), e);
            throw new RuntimeException("技能执行失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 从测试步骤中检测测试类型（UI 或 API）
     */
    private String detectTestType(JSONArray steps) {
        if (steps == null || steps.isEmpty()) {
            return "UI"; // 默认返回 UI
        }
        
        // 检查第一个步骤的字段来判断类型
        JSONObject firstStep = steps.getJSONObject(0);
        
        // 如果包含 action 字段（如 open, click, input 等），则是 UI 测试
        if (firstStep.containsKey("action")) {
            return "UI";
        }
        
        // 如果包含 method, url 等字段，则是 API 测试
        if (firstStep.containsKey("method") || firstStep.containsKey("url")) {
            return "API";
        }
        
        // 默认返回 UI
        return "UI";
    }
    
    /**
     * 根据AI识别的参数动态替换技能配置
     * @param skill 技能对象
     * @param parameters AI识别的参数，格式如: {"工单标题": "测试申请", "用户名": "admin"}
     */
    private void applyParametersToSkill(Skill skill, Map<String, Object> parameters) {
        if ("TESTSUITE".equals(skill.getType())) {
            // 对于测试套件类型，替换 configData 中的 {参数名称} 占位符
            String configData = skill.getConfigData();
            if (configData != null && !configData.trim().isEmpty()) {
                try {
                    // 替换 JSON 配置中的所有占位符
                    String updatedConfig = replaceParameterPlaceholders(configData, parameters);
                    skill.setConfigData(updatedConfig);
                    log.info("成功替换测试套件技能配置中的参数, skillId: {}", skill.getId());
                } catch (Exception e) {
                    log.error("替换测试套件技能配置参数失败, skillId: {}", skill.getId(), e);
                }
            }
        } else if ("SCRIPT".equals(skill.getType())) {
            // 对于脚本类型，替换 configData（脚本参数配置）和 scriptContent 中的占位符
            String configData = skill.getConfigData();
            if (configData != null && !configData.trim().isEmpty()) {
                try {
                    String updatedConfig = replaceParameterPlaceholders(configData, parameters);
                    skill.setConfigData(updatedConfig);
                    log.info("成功替换脚本参数配置中的参数, skillId: {}", skill.getId());
                } catch (Exception e) {
                    log.error("替换脚本参数配置失败, skillId: {}", skill.getId(), e);
                }
            }
            
            // 替换脚本内容中的占位符
            String scriptContent = skill.getScriptContent();
            if (scriptContent != null && !scriptContent.isEmpty()) {
                String updatedContent = replaceParameterPlaceholders(scriptContent, parameters);
                skill.setScriptContent(updatedContent);
                log.info("成功替换脚本内容中的参数, skillId: {}", skill.getId());
            }
        }
    }
    
    /**
     * 替换字符串中的 {参数名称} 占位符
     * @param content 原始内容
     * @param parameters 参数映射
     * @return 替换后的内容
     */
    private String replaceParameterPlaceholders(String content, Map<String, Object> parameters) {
        String result = content;
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String paramName = entry.getKey();
            String paramValue = entry.getValue() != null ? entry.getValue().toString() : "";
            
            // 方式1: 精确匹配 {参数名称}
            String exactPlaceholder = "{" + paramName + "}";
            if (result.contains(exactPlaceholder)) {
                result = result.replace(exactPlaceholder, paramValue);
                log.info("精确替换占位符: {} -> {}", exactPlaceholder, paramValue);
            } else {
                // 方式2: 模糊匹配 {*参数名称*} - 查找包含该参数名的所有占位符
                // 例如: paramName="标题" 可以匹配 {工单标题}, {标题}, {工单的标题} 等
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{[^}]*" + java.util.regex.Pattern.quote(paramName) + "[^}]*\\}");
                java.util.regex.Matcher matcher = pattern.matcher(result);
                
                if (matcher.find()) {
                    // 找到至少一个匹配的占位符，执行替换
                    String originalResult = result;
                    result = matcher.replaceAll(paramValue);
                    log.info("模糊替换占位符: 参数名={}, 原文={}, 替换后={}", paramName, originalResult, result);
                } else {
                    log.warn("未找到匹配的占位符: 参数名={}, 内容={}", paramName, content);
                }
            }
        }
        return result;
    }
}
