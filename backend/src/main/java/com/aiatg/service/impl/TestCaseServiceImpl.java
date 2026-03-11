package com.aiatg.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.aiatg.common.PageResult;
import com.aiatg.dto.TestCaseDTO;
import com.aiatg.dto.TestCaseQueryDTO;
import com.aiatg.dto.TestStepDTO;
import com.aiatg.entity.*;
import com.aiatg.mapper.*;
import com.aiatg.service.TestCaseService;
import com.aiatg.vo.TestCaseVO;
import com.aiatg.vo.TestStepVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 测试用例服务实现类
 */
@Slf4j
@Service
public class TestCaseServiceImpl implements TestCaseService {
    
    @Autowired
    private TestCaseMapper testCaseMapper;
    
    @Autowired
    private TestStepMapper testStepMapper;
    
    @Autowired
    private ProjectMapper projectMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private RequirementMapper requirementMapper;
    
    @Autowired
    private SuiteCaseRelationMapper suiteCaseRelationMapper;
    
    @Autowired
    private TestSuiteMapper testSuiteMapper;
    
    @Override
    @Transactional
    public TestCaseVO createTestCase(TestCaseDTO dto, Long userId) {
        // 验证项目是否存在
        Project project = projectMapper.selectById(dto.getProjectId());
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }
        
        // 创建测试用例
        TestCase testCase = new TestCase();
        BeanUtil.copyProperties(dto, testCase, "steps");
        
        // 生成用例编号：前缀+日期+唯一编号
        testCase.setCaseNo(generateCaseNo(dto.getType(), dto.getProjectId()));
        testCase.setCreatedBy(userId);
        testCase.setCreatedTime(LocalDateTime.now());
        testCase.setStatus(StringUtils.hasText(dto.getStatus()) ? dto.getStatus() : "draft");
        
        // 处理测试步骤：统一保存为可执行的 JSON 格式
        // 支持 AI 生成、导入脚本、手动添加三种方式
        if (dto.getSteps() != null && !dto.getSteps().isEmpty()) {
            String stepsJson = JSONUtil.toJsonStr(dto.getSteps());
            log.info("保存测试步骤，类型: {}, 数量: {}, JSON: {}", dto.getType(), dto.getSteps().size(), stepsJson);
            testCase.setSteps(stepsJson);
        } else {
            log.warn("测试步骤为空");
        }
        
        testCaseMapper.insert(testCase);
        
        // 处理套件关联
        if (dto.getSuiteId() != null) {
            // 查询当前套件中的最大执行顺序
            Integer maxOrder = suiteCaseRelationMapper.selectList(
                new LambdaQueryWrapper<SuiteCaseRelation>()
                    .eq(SuiteCaseRelation::getSuiteId, dto.getSuiteId())
                    .orderByDesc(SuiteCaseRelation::getExecuteOrder)
                    .last("LIMIT 1")
            ).stream()
                .findFirst()
                .map(SuiteCaseRelation::getExecuteOrder)
                .orElse(0);
            
            SuiteCaseRelation relation = new SuiteCaseRelation();
            relation.setSuiteId(dto.getSuiteId());
            relation.setCaseId(testCase.getId());
            relation.setExecuteOrder(maxOrder + 1);
            relation.setCreatedTime(LocalDateTime.now());
            suiteCaseRelationMapper.insert(relation);
            log.info("添加用例到套件，用例ID: {}, 套件ID: {}, 执行顺序: {}", testCase.getId(), dto.getSuiteId(), maxOrder + 1);
        }
        
        return convertToVO(testCase);
    }
    
    @Override
    @Transactional
    public TestCaseVO updateTestCase(Long id, TestCaseDTO dto) {
        TestCase testCase = testCaseMapper.selectById(id);
        if (testCase == null) {
            throw new RuntimeException("测试用例不存在");
        }
        
        // 更新字段
        if (StringUtils.hasText(dto.getTitle())) {
            testCase.setTitle(dto.getTitle());
        }
        if (dto.getPreconditions() != null) {
            testCase.setPreconditions(dto.getPreconditions());
        }
        if (StringUtils.hasText(dto.getType())) {
            testCase.setType(dto.getType());
        }
        if (StringUtils.hasText(dto.getPriority())) {
            testCase.setPriority(dto.getPriority());
        }
        if (StringUtils.hasText(dto.getStatus())) {
            testCase.setStatus(dto.getStatus());
        }
        if (dto.getRequirementId() != null) {
            testCase.setRequirementId(dto.getRequirementId());
        }
        
        // 更新测试步骤：统一保存为可执行的 JSON 格式
        if (dto.getSteps() != null) {
            if (!dto.getSteps().isEmpty()) {
                String stepsJson = JSONUtil.toJsonStr(dto.getSteps());
                log.info("更新测试步骤，类型: {}, 数量: {}, JSON: {}", dto.getType(), dto.getSteps().size(), stepsJson);
                testCase.setSteps(stepsJson);
            } else {
                log.warn("更新时测试步骤为空");
                testCase.setSteps(null);
            }
        }
        
        // 处理套件关联
        if (dto.getSuiteId() != null) {
            // 查询现有关联
            LambdaQueryWrapper<SuiteCaseRelation> relationWrapper = new LambdaQueryWrapper<>();
            relationWrapper.eq(SuiteCaseRelation::getCaseId, id);
            SuiteCaseRelation existingRelation = suiteCaseRelationMapper.selectOne(relationWrapper);
            
            if (existingRelation == null) {
                // 之前没有关联，新建 - 查询当前套件中的最大执行顺序
                Integer maxOrder = suiteCaseRelationMapper.selectList(
                    new LambdaQueryWrapper<SuiteCaseRelation>()
                        .eq(SuiteCaseRelation::getSuiteId, dto.getSuiteId())
                        .orderByDesc(SuiteCaseRelation::getExecuteOrder)
                        .last("LIMIT 1")
                ).stream()
                    .findFirst()
                    .map(SuiteCaseRelation::getExecuteOrder)
                    .orElse(0);
                
                SuiteCaseRelation newRelation = new SuiteCaseRelation();
                newRelation.setSuiteId(dto.getSuiteId());
                newRelation.setCaseId(id);
                newRelation.setExecuteOrder(maxOrder + 1);
                newRelation.setCreatedTime(LocalDateTime.now());
                suiteCaseRelationMapper.insert(newRelation);
                log.info("创建套件关联，用例ID: {}, 套件ID: {}, 执行顺序: {}", id, dto.getSuiteId(), maxOrder + 1);
            } else if (!existingRelation.getSuiteId().equals(dto.getSuiteId())) {
                // 套件变更，更新关联
                existingRelation.setSuiteId(dto.getSuiteId());
                suiteCaseRelationMapper.updateById(existingRelation);
                log.info("更新套件关联，用例ID: {}, 新套件ID: {}", id, dto.getSuiteId());
            }
        } else {
            // suiteId 为 null，删除现有关联
            LambdaQueryWrapper<SuiteCaseRelation> relationWrapper = new LambdaQueryWrapper<>();
            relationWrapper.eq(SuiteCaseRelation::getCaseId, id);
            suiteCaseRelationMapper.delete(relationWrapper);
            log.info("删除套件关联，用例ID: {}", id);
        }
        
        testCase.setUpdatedTime(LocalDateTime.now());
        testCaseMapper.updateById(testCase);
        
        return convertToVO(testCase);
    }
    
    @Override
    @Transactional
    public void deleteTestCase(Long id) {
        TestCase testCase = testCaseMapper.selectById(id);
        if (testCase == null) {
            throw new RuntimeException("测试用例不存在");
        }
        
        // 删除测试步骤
        LambdaQueryWrapper<TestStep> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TestStep::getTestCaseId, id);
        testStepMapper.delete(wrapper);
        
        // 删除测试用例
        testCaseMapper.deleteById(id);
    }
    
    @Override
    public TestCaseVO getTestCaseById(Long id) {
        TestCase testCase = testCaseMapper.selectById(id);
        if (testCase == null) {
            throw new RuntimeException("测试用例不存在");
        }
        return convertToVO(testCase);
    }
    
    @Override
    public PageResult<TestCaseVO> getTestCaseList(TestCaseQueryDTO query) {
        // 构建查询条件
        LambdaQueryWrapper<TestCase> queryWrapper = new LambdaQueryWrapper<>();
        
        if (query.getProjectId() != null) {
            queryWrapper.eq(TestCase::getProjectId, query.getProjectId());
        }
        
        if (query.getRequirementId() != null) {
            queryWrapper.eq(TestCase::getRequirementId, query.getRequirementId());
        }
        
        // 如果按套件ID查询，需要通过关联表查询
        if (query.getSuiteId() != null) {
            // 查询套件关联的用例ID列表
            List<Long> caseIds = suiteCaseRelationMapper.selectList(
                new LambdaQueryWrapper<SuiteCaseRelation>()
                    .eq(SuiteCaseRelation::getSuiteId, query.getSuiteId())
            ).stream()
            .map(SuiteCaseRelation::getCaseId)
            .collect(Collectors.toList());
            
            if (caseIds.isEmpty()) {
                // 如果套件没有关联任何用例，返回空列表
                return new PageResult<>(0L, new ArrayList<>(), query.getPageNum(), query.getPageSize());
            }
            
            queryWrapper.in(TestCase::getId, caseIds);
        }
        
        if (StringUtils.hasText(query.getKeyword())) {
            queryWrapper.and(wrapper -> wrapper
                .like(TestCase::getTitle, query.getKeyword())
                .or()
                .like(TestCase::getCaseNo, query.getKeyword())
            );
        }
        
        if (StringUtils.hasText(query.getType())) {
            queryWrapper.eq(TestCase::getType, query.getType());
        }
        
        if (StringUtils.hasText(query.getPriority())) {
            queryWrapper.eq(TestCase::getPriority, query.getPriority());
        }
        
        if (StringUtils.hasText(query.getStatus())) {
            queryWrapper.eq(TestCase::getStatus, query.getStatus());
        }
        
        queryWrapper.orderByDesc(TestCase::getCreatedTime);
        
        // 分页查询
        Page<TestCase> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<TestCase> resultPage = testCaseMapper.selectPage(page, queryWrapper);
        
        // 转换为VO
        List<TestCaseVO> voList = resultPage.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        return new PageResult<>(
            resultPage.getTotal(),
            voList,
            query.getPageNum(),
            query.getPageSize()
        );
    }
    
    @Override
    public void updateStatus(Long id, String status) {
        TestCase testCase = testCaseMapper.selectById(id);
        if (testCase == null) {
            throw new RuntimeException("测试用例不存在");
        }
        
        testCase.setStatus(status);
        testCase.setUpdatedTime(LocalDateTime.now());
        testCaseMapper.updateById(testCase);
    }
    
    @Override
    @Transactional
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        
        for (Long id : ids) {
            deleteTestCase(id);
        }
    }
    
    @Override
    @Transactional
    public int importTestCases(MultipartFile file, Long projectId, Long userId) {
        // TODO: 实现Excel导入功能
        // 需要使用POI或EasyExcel库解析Excel文件
        throw new RuntimeException("导入功能待实现");
    }
    
    @Override
    public byte[] exportTestCases(TestCaseQueryDTO query) {
        // TODO: 实现Excel导出功能
        // 需要使用POI或EasyExcel库生成Excel文件
        throw new RuntimeException("导出功能待实现");
    }
    
    /**
     * 保存测试步骤
     */
    private void saveTestSteps(Long testCaseId, List<TestStepDTO> stepDTOs) {
        for (TestStepDTO stepDTO : stepDTOs) {
            TestStep step = new TestStep();
            BeanUtil.copyProperties(stepDTO, step);
            step.setTestCaseId(testCaseId);
            step.setCreatedTime(LocalDateTime.now());
            testStepMapper.insert(step);
        }
    }
    
    /**
     * 将TestCase转换为TestCaseVO
     */
    /**
     * 生成用例编号：前缀+日期+唯一编号
     * 格式：UI20260128001、API20260128001、PERF20260128001
     */
    private String generateCaseNo(String type, Long projectId) {
        // 确定前缀
        String prefix;
        switch (type) {
            case "ui":
                prefix = "UI";
                break;
            case "api":
                prefix = "API";
                break;
            case "performance":
                prefix = "PERF";
                break;
            default:
                prefix = "TC";
        }
        
        // 获取当前日期
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        // 查询当天该类型的最大编号
        String likePattern = prefix + dateStr + "%";
        LambdaQueryWrapper<TestCase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TestCase::getProjectId, projectId);
        wrapper.eq(TestCase::getType, type);
        wrapper.like(TestCase::getCaseNo, likePattern);
        wrapper.orderByDesc(TestCase::getCaseNo);
        wrapper.last("LIMIT 1");
        
        TestCase lastCase = testCaseMapper.selectOne(wrapper);
        
        int nextNumber = 1;
        if (lastCase != null && lastCase.getCaseNo() != null) {
            String lastCaseNo = lastCase.getCaseNo();
            try {
                // 提取最后3位数字
                String numberPart = lastCaseNo.substring(lastCaseNo.length() - 3);
                nextNumber = Integer.parseInt(numberPart) + 1;
            } catch (Exception e) {
                nextNumber = 1;
            }
        }
        
        // 生成新编号，数字部分固定3位，不足补0
        return String.format("%s%s%03d", prefix, dateStr, nextNumber);
    }
    
    /**
     * 转换为VO对象
     */
    private TestCaseVO convertToVO(TestCase testCase) {
        TestCaseVO vo = new TestCaseVO();
        BeanUtil.copyProperties(testCase, vo, "steps");
        
        // 复制steps字段到stepsJson
        vo.setStepsJson(testCase.getSteps());
        
        // 获取项目名称
        if (testCase.getProjectId() != null) {
            Project project = projectMapper.selectById(testCase.getProjectId());
            if (project != null) {
                vo.setProjectName(project.getName());
            }
        }
        
        // 获取需求标题
        if (testCase.getRequirementId() != null) {
            Requirement requirement = requirementMapper.selectById(testCase.getRequirementId());
            if (requirement != null) {
                vo.setRequirementTitle(requirement.getTitle());
            }
        }
        
        // 获取创建人姓名
        if (testCase.getCreatedBy() != null) {
            User user = userMapper.selectById(testCase.getCreatedBy());
            if (user != null) {
                vo.setCreatedByName(user.getNickname() != null ? user.getNickname() : user.getUsername());
            }
        }
        
        // 获取所属套件信息
        LambdaQueryWrapper<SuiteCaseRelation> relationWrapper = new LambdaQueryWrapper<>();
        relationWrapper.eq(SuiteCaseRelation::getCaseId, testCase.getId());
        relationWrapper.last("LIMIT 1");
        SuiteCaseRelation relation = suiteCaseRelationMapper.selectOne(relationWrapper);
        
        if (relation != null && relation.getSuiteId() != null) {
            vo.setSuiteId(relation.getSuiteId());
            TestSuite suite = testSuiteMapper.selectById(relation.getSuiteId());
            if (suite != null) {
                vo.setSuiteName(suite.getName());
            }
        }
        
        // steps字段保持为null（用于传统测试步骤）
        // stepsJson字段存储可执行的JSON字符串格式
        vo.setSteps(null);
        
        return vo;
    }
}
