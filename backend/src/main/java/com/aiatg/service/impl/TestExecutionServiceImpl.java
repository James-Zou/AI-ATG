package com.aiatg.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.aiatg.common.PageResult;
import com.aiatg.dto.ExecutionRequest;
import com.aiatg.entity.*;
import com.aiatg.mapper.*;
import com.aiatg.service.AsyncTestExecutionService;
import com.aiatg.service.TestExecutionService;
import com.aiatg.vo.ExecutionDetailVO;
import com.aiatg.vo.ExecutionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 测试执行服务实现类
 */
@Slf4j
@Service
public class TestExecutionServiceImpl implements TestExecutionService {
    
    @Autowired
    private TestExecutionMapper executionMapper;
    
    @Autowired
    private TestExecutionDetailMapper detailMapper;
    
    @Autowired
    private TestCaseMapper testCaseMapper;
    
    @Autowired
    private TestSuiteMapper testSuiteMapper;
    
    @Autowired
    private ProjectMapper projectMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private SuiteCaseRelationMapper suiteCaseRelationMapper;
    
    @Autowired
    private AsyncTestExecutionService asyncTestExecutionService;
    
    @Override
    @Transactional
    public ExecutionVO createAndExecute(ExecutionRequest request, Long userId) {
        // 创建执行记录
        TestExecution execution = new TestExecution();
        execution.setProjectId(request.getProjectId());
        execution.setSuiteId(request.getSuiteId());
        execution.setExecutionName(
            request.getExecutionName() != null ? 
            request.getExecutionName() : 
            "执行-" + LocalDateTime.now()
        );
        execution.setExecutionType(request.getExecutionType());
        execution.setEnvironment(request.getEnvironment());
        execution.setStatus(1); // 执行中
        execution.setTriggerType(request.getTriggerType());
        execution.setExecutedBy(userId);
        execution.setStartTime(LocalDateTime.now());
        execution.setCreatedTime(LocalDateTime.now());
        
        executionMapper.insert(execution);
        
        // 获取测试用例列表
        List<TestCase> testCases = getTestCases(request);
        execution.setTotalCases(testCases.size());
        executionMapper.updateById(execution);
        
        // 异步执行测试（通过独立的异步服务）
        asyncTestExecutionService.executeTestsAsync(execution.getId(), testCases, request.getExecutionType());
        
        return convertToVO(execution);
    }
    
    @Override
    public ExecutionVO getExecutionById(Long id) {
        TestExecution execution = executionMapper.selectById(id);
        if (execution == null) {
            throw new RuntimeException("执行记录不存在");
        }
        
        ExecutionVO vo = convertToVO(execution);
        
        // 获取执行明细
        LambdaQueryWrapper<TestExecutionDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TestExecutionDetail::getExecutionId, id);
        List<TestExecutionDetail> details = detailMapper.selectList(wrapper);
        
        List<ExecutionDetailVO> detailVOs = details.stream()
            .map(this::convertDetailToVO)
            .collect(Collectors.toList());
        
        vo.setDetails(detailVOs);
        
        return vo;
    }
    
    @Override
    public PageResult<ExecutionVO> getExecutionList(Long projectId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<TestExecution> wrapper = new LambdaQueryWrapper<>();
        
        if (projectId != null) {
            wrapper.eq(TestExecution::getProjectId, projectId);
        }
        
        wrapper.orderByDesc(TestExecution::getCreatedTime);
        
        Page<TestExecution> page = new Page<>(pageNum, pageSize);
        Page<TestExecution> resultPage = executionMapper.selectPage(page, wrapper);
        
        List<TestExecution> records = resultPage.getRecords();
        
        // 批量查询关联数据，避免N+1查询问题
        List<Long> projectIds = records.stream()
            .map(TestExecution::getProjectId)
            .filter(id -> id != null)
            .distinct()
            .collect(Collectors.toList());
        
        List<Long> suiteIds = records.stream()
            .map(TestExecution::getSuiteId)
            .filter(id -> id != null)
            .distinct()
            .collect(Collectors.toList());
        
        List<Long> userIds = records.stream()
            .map(TestExecution::getExecutedBy)
            .filter(id -> id != null)
            .distinct()
            .collect(Collectors.toList());
        
        // 批量查询并构建Map
        List<Project> projects = projectIds.isEmpty() ? new ArrayList<>() : projectMapper.selectBatchIds(projectIds);
        List<TestSuite> suites = suiteIds.isEmpty() ? new ArrayList<>() : testSuiteMapper.selectBatchIds(suiteIds);
        List<User> users = userIds.isEmpty() ? new ArrayList<>() : userMapper.selectBatchIds(userIds);
        
        // 转换为Map以便快速查找
        Map<Long, Project> projectMap = projects.stream().collect(Collectors.toMap(Project::getId, p -> p));
        Map<Long, TestSuite> suiteMap = suites.stream().collect(Collectors.toMap(TestSuite::getId, s -> s));
        Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
        
        // 转换为VO，使用Map避免重复查询
        List<ExecutionVO> voList = records.stream()
            .map(execution -> convertToVOWithMaps(execution, projectMap, suiteMap, userMap))
            .collect(Collectors.<ExecutionVO>toList());
        
        return new PageResult<>(
            resultPage.getTotal(),
            voList,
            pageNum,
            pageSize
        );
    }
    
    @Override
    public void stopExecution(Long id) {
        TestExecution execution = executionMapper.selectById(id);
        if (execution == null) {
            throw new RuntimeException("执行记录不存在");
        }
        
        execution.setStatus(3); // 已停止
        execution.setEndTime(LocalDateTime.now());
        executionMapper.updateById(execution);
    }
    
    /**
     * 获取测试用例列表
     */
    private List<TestCase> getTestCases(ExecutionRequest request) {
        List<TestCase> testCases = new ArrayList<>();
        
        if (request.getTestCaseIds() != null && !request.getTestCaseIds().isEmpty()) {
            // 按指定的用例ID列表
            testCases = testCaseMapper.selectBatchIds(request.getTestCaseIds());
        } else if (request.getSuiteId() != null) {
            // 按测试套件，需要通过关联表查询
            List<Long> caseIds = suiteCaseRelationMapper.selectList(
                new LambdaQueryWrapper<SuiteCaseRelation>()
                    .eq(SuiteCaseRelation::getSuiteId, request.getSuiteId())
                    .orderByAsc(SuiteCaseRelation::getExecuteOrder)
            ).stream()
            .map(SuiteCaseRelation::getCaseId)
            .collect(Collectors.toList());
            
            if (!caseIds.isEmpty()) {
                testCases = testCaseMapper.selectBatchIds(caseIds);
            }
        } else {
            // 按项目
            LambdaQueryWrapper<TestCase> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TestCase::getProjectId, request.getProjectId());
            testCases = testCaseMapper.selectList(wrapper);
        }
        
        // 🔒 过滤：只执行状态为"已通过"的用例
        testCases = testCases.stream()
            .filter(testCase -> "approved".equals(testCase.getStatus()))
            .collect(Collectors.toList());
        
        log.info("执行用例总数: {}, 状态为已通过的用例数: {}", testCases.size(), testCases.size());
        
        return testCases;
    }
    
    /**
     * 转换为VO（使用预查询的Map数据，避免N+1查询）
     */
    private ExecutionVO convertToVOWithMaps(
        TestExecution execution,
        Map<Long, Project> projectMap,
        Map<Long, TestSuite> suiteMap,
        Map<Long, User> userMap
    ) {
        ExecutionVO vo = new ExecutionVO();
        BeanUtil.copyProperties(execution, vo);
        
        // 从Map中获取项目名称
        if (execution.getProjectId() != null) {
            Project project = projectMap.get(execution.getProjectId());
            if (project != null) {
                vo.setProjectName(project.getName());
            }
        }
        
        // 从Map中获取套件名称
        if (execution.getSuiteId() != null) {
            TestSuite suite = suiteMap.get(execution.getSuiteId());
            if (suite != null) {
                vo.setSuiteName(suite.getName());
            }
        }
        
        // 从Map中获取执行人姓名
        if (execution.getExecutedBy() != null) {
            User user = userMap.get(execution.getExecutedBy());
            if (user != null) {
                vo.setExecutedByName(user.getNickname() != null ? user.getNickname() : user.getUsername());
            }
        }
        
        return vo;
    }
    
    /**
     * 转换为VO（单个查询时使用）
     */
    private ExecutionVO convertToVO(TestExecution execution) {
        ExecutionVO vo = new ExecutionVO();
        BeanUtil.copyProperties(execution, vo);
        
        // 获取项目名称
        if (execution.getProjectId() != null) {
            Project project = projectMapper.selectById(execution.getProjectId());
            if (project != null) {
                vo.setProjectName(project.getName());
            }
        }
        
        // 获取套件名称
        if (execution.getSuiteId() != null) {
            TestSuite suite = testSuiteMapper.selectById(execution.getSuiteId());
            if (suite != null) {
                vo.setSuiteName(suite.getName());
            }
        }
        
        // 获取执行人姓名
        if (execution.getExecutedBy() != null) {
            User user = userMapper.selectById(execution.getExecutedBy());
            if (user != null) {
                vo.setExecutedByName(user.getNickname() != null ? user.getNickname() : user.getUsername());
            }
        }
        
        return vo;
    }
    
    /**
     * 转换明细为VO
     */
    private ExecutionDetailVO convertDetailToVO(TestExecutionDetail detail) {
        ExecutionDetailVO vo = new ExecutionDetailVO();
        BeanUtil.copyProperties(detail, vo);
        
        // 获取测试用例标题
        if (detail.getTestCaseId() != null) {
            TestCase testCase = testCaseMapper.selectById(detail.getTestCaseId());
            if (testCase != null) {
                vo.setTestCaseTitle(testCase.getTitle());
            }
        }
        
        return vo;
    }
}
