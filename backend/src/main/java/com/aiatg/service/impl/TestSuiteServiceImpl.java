package com.aiatg.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.aiatg.common.PageResult;
import com.aiatg.dto.SuiteCaseOrderDTO;
import com.aiatg.dto.TestSuiteDTO;
import com.aiatg.entity.Project;
import com.aiatg.entity.SuiteCaseRelation;
import com.aiatg.entity.TestCase;
import com.aiatg.entity.TestSuite;
import com.aiatg.entity.User;
import com.aiatg.mapper.ProjectMapper;
import com.aiatg.mapper.SuiteCaseRelationMapper;
import com.aiatg.mapper.TestCaseMapper;
import com.aiatg.mapper.TestSuiteMapper;
import com.aiatg.mapper.UserMapper;
import com.aiatg.service.TestSuiteService;
import com.aiatg.vo.SuiteCaseVO;
import com.aiatg.vo.TestSuiteVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 测试套件服务实现类
 */
@Slf4j
@Service
public class TestSuiteServiceImpl implements TestSuiteService {
    
    @Autowired
    private TestSuiteMapper testSuiteMapper;
    
    @Autowired
    private ProjectMapper projectMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private SuiteCaseRelationMapper suiteCaseRelationMapper;
    
    @Autowired
    private TestCaseMapper testCaseMapper;
    
    @Override
    public TestSuiteVO createTestSuite(TestSuiteDTO dto, Long userId) {
        // 验证项目是否存在
        Project project = projectMapper.selectById(dto.getProjectId());
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }
        
        // 创建测试套件
        TestSuite testSuite = new TestSuite();
        BeanUtil.copyProperties(dto, testSuite);
        testSuite.setCreatedBy(userId);
        testSuite.setCreatedTime(LocalDateTime.now());
        testSuite.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        
        testSuiteMapper.insert(testSuite);
        
        return convertToVO(testSuite);
    }
    
    @Override
    public TestSuiteVO updateTestSuite(Long id, TestSuiteDTO dto) {
        TestSuite testSuite = testSuiteMapper.selectById(id);
        if (testSuite == null) {
            throw new RuntimeException("测试套件不存在");
        }
        
        // 更新字段
        if (StringUtils.hasText(dto.getName())) {
            testSuite.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            testSuite.setDescription(dto.getDescription());
        }
        if (dto.getStatus() != null) {
            testSuite.setStatus(dto.getStatus());
        }
        
        testSuite.setUpdatedTime(LocalDateTime.now());
        testSuiteMapper.updateById(testSuite);
        
        return convertToVO(testSuite);
    }
    
    @Override
    public void deleteTestSuite(Long id) {
        TestSuite testSuite = testSuiteMapper.selectById(id);
        if (testSuite == null) {
            throw new RuntimeException("测试套件不存在");
        }
        
        // 检查是否有关联的测试用例（通过关联表）
        LambdaQueryWrapper<SuiteCaseRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SuiteCaseRelation::getSuiteId, id);
        Long count = suiteCaseRelationMapper.selectCount(wrapper);
        
        if (count > 0) {
            throw new RuntimeException("该套件下还有测试用例，无法删除");
        }
        
        testSuiteMapper.deleteById(id);
    }
    
    @Override
    public TestSuiteVO getTestSuiteById(Long id) {
        TestSuite testSuite = testSuiteMapper.selectById(id);
        if (testSuite == null) {
            throw new RuntimeException("测试套件不存在");
        }
        return convertToVO(testSuite);
    }
    
    @Override
    public PageResult<TestSuiteVO> getTestSuiteList(Long projectId, Integer pageNum, Integer pageSize) {
        // 构建查询条件
        LambdaQueryWrapper<TestSuite> queryWrapper = new LambdaQueryWrapper<>();
        
        if (projectId != null) {
            queryWrapper.eq(TestSuite::getProjectId, projectId);
        }
        
        queryWrapper.orderByDesc(TestSuite::getCreatedTime);
        
        // 分页查询
        Page<TestSuite> page = new Page<>(pageNum, pageSize);
        Page<TestSuite> resultPage = testSuiteMapper.selectPage(page, queryWrapper);
        
        // 转换为VO
        List<TestSuiteVO> voList = resultPage.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        return new PageResult<>(
            resultPage.getTotal(),
            voList,
            pageNum,
            pageSize
        );
    }
    
    /**
     * 将TestSuite转换为TestSuiteVO
     */
    private TestSuiteVO convertToVO(TestSuite testSuite) {
        TestSuiteVO vo = new TestSuiteVO();
        BeanUtil.copyProperties(testSuite, vo);
        
        // 获取项目名称
        if (testSuite.getProjectId() != null) {
            Project project = projectMapper.selectById(testSuite.getProjectId());
            if (project != null) {
                vo.setProjectName(project.getName());
            }
        }
        
        // 获取创建人姓名
        if (testSuite.getCreatedBy() != null) {
            log.debug("查询创建人信息，ID: {}", testSuite.getCreatedBy());
            User user = userMapper.selectById(testSuite.getCreatedBy());
            if (user != null) {
                String createdByName = user.getNickname() != null ? user.getNickname() : user.getUsername();
                vo.setCreatedByName(createdByName);
                log.debug("创建人姓名: {}", createdByName);
            } else {
                log.warn("未找到创建人信息，用户ID: {}", testSuite.getCreatedBy());
            }
        } else {
            log.warn("测试套件的createdBy字段为空，套件ID: {}", testSuite.getId());
        }
        
        // 统计用例数量（通过关联表）
        LambdaQueryWrapper<SuiteCaseRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SuiteCaseRelation::getSuiteId, testSuite.getId());
        Long count = suiteCaseRelationMapper.selectCount(wrapper);
        vo.setCaseCount(count.intValue());
        
        return vo;
    }
    
    @Override
    public List<SuiteCaseVO> getSuiteCases(Long suiteId) {
        // 查询套件用例关联（按执行顺序）
        List<SuiteCaseRelation> relations = suiteCaseRelationMapper.selectList(
            new LambdaQueryWrapper<SuiteCaseRelation>()
                .eq(SuiteCaseRelation::getSuiteId, suiteId)
                .orderByAsc(SuiteCaseRelation::getExecuteOrder)
        );
        
        if (relations.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 批量查询用例信息
        List<Long> caseIds = relations.stream()
            .map(SuiteCaseRelation::getCaseId)
            .collect(Collectors.toList());
        
        List<TestCase> cases = testCaseMapper.selectBatchIds(caseIds);
        Map<Long, TestCase> caseMap = cases.stream()
            .collect(Collectors.toMap(TestCase::getId, c -> c));
        
        // 组装VO
        return relations.stream()
            .map(relation -> {
                SuiteCaseVO vo = new SuiteCaseVO();
                vo.setId(relation.getId());
                vo.setCaseId(relation.getCaseId());
                vo.setExecuteOrder(relation.getExecuteOrder());
                
                TestCase testCase = caseMap.get(relation.getCaseId());
                if (testCase != null) {
                    vo.setCaseTitle(testCase.getTitle());
                    vo.setCaseType(testCase.getType());
                    vo.setCasePriority(testCase.getPriority());
                    vo.setCaseStatus(testCase.getStatus());
                }
                
                return vo;
            })
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void updateCaseOrder(SuiteCaseOrderDTO dto) {
        // 批量更新执行顺序
        for (int i = 0; i < dto.getCaseIds().size(); i++) {
            Long caseId = dto.getCaseIds().get(i);
            Integer newOrder = i + 1;
            
            suiteCaseRelationMapper.update(null,
                new LambdaUpdateWrapper<SuiteCaseRelation>()
                    .eq(SuiteCaseRelation::getSuiteId, dto.getSuiteId())
                    .eq(SuiteCaseRelation::getCaseId, caseId)
                    .set(SuiteCaseRelation::getExecuteOrder, newOrder)
            );
        }
        
        log.info("更新套件 {} 的用例执行顺序成功", dto.getSuiteId());
    }
    
    @Override
    public String getSuiteSteps(Long suiteId) {
        // 验证套件是否存在
        TestSuite testSuite = testSuiteMapper.selectById(suiteId);
        if (testSuite == null) {
            throw new RuntimeException("测试套件不存在");
        }
        
        // 查询套件关联的用例（按执行顺序排序）
        List<SuiteCaseRelation> relations = suiteCaseRelationMapper.selectList(
            new LambdaQueryWrapper<SuiteCaseRelation>()
                .eq(SuiteCaseRelation::getSuiteId, suiteId)
                .orderByAsc(SuiteCaseRelation::getExecuteOrder)
        );
        
        if (relations.isEmpty()) {
            return "[]"; // 返回空数组
        }
        
        // 获取用例ID列表
        List<Long> caseIds = relations.stream()
            .map(SuiteCaseRelation::getCaseId)
            .collect(Collectors.toList());
        
        // 批量查询测试用例
        List<TestCase> testCases = testCaseMapper.selectBatchIds(caseIds);
        
        // 合并所有测试步骤
        JSONArray allSteps = new JSONArray();
        for (TestCase testCase : testCases) {
            String steps = testCase.getSteps();
            if (steps != null && !steps.trim().isEmpty()) {
                JSONArray caseSteps = JSONUtil.parseArray(steps);
                allSteps.addAll(caseSteps);
            }
        }
        
        // 返回格式化的 JSON 字符串
        return JSONUtil.toJsonPrettyStr(allSteps);
    }
}
