package com.aiatg.service.impl;

import com.aiatg.entity.TestCase;
import com.aiatg.entity.TestExecution;
import com.aiatg.entity.TestExecutionDetail;
import com.aiatg.executor.ExecutorFactory;
import com.aiatg.executor.TestExecutor;
import com.aiatg.mapper.TestExecutionDetailMapper;
import com.aiatg.mapper.TestExecutionMapper;
import com.aiatg.service.AsyncTestExecutionService;
import com.aiatg.service.TestReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 异步测试执行服务实现类
 */
@Slf4j
@Service
public class AsyncTestExecutionServiceImpl implements AsyncTestExecutionService {
    
    @Autowired
    private TestExecutionMapper executionMapper;
    
    @Autowired
    private TestExecutionDetailMapper detailMapper;
    
    @Autowired
    private ExecutorFactory executorFactory;
    
    @Autowired
    private TestReportService reportService;
    
    @Override
    @Async
    public void executeTestsAsync(Long executionId, List<TestCase> testCases, String executionType) {
        // 等待主事务提交（避免事务隔离问题）
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        TestExecution execution = executionMapper.selectById(executionId);
        
        if (execution == null) {
            log.error("未找到执行记录: executionId={}", executionId);
            return;
        }
        
        try {
            log.info("开始异步执行测试，执行ID: {}, 用例数量: {}, 类型: {}", 
                    executionId, testCases.size(), executionType);
            
            // 获取执行器
            TestExecutor executor = executorFactory.getExecutor(executionType);
            
            int passed = 0;
            int failed = 0;
            int skipped = 0;
            
            // 执行每个测试用例
            for (TestCase testCase : testCases) {
                try {
                    log.info("执行测试用例: {} - {}", testCase.getId(), testCase.getTitle());
                    TestExecutionDetail detail = executor.execute(testCase, executionId);
                    detailMapper.insert(detail);
                    
                    // UI测试的结果由ATG-Client回调更新，此处不统计
                    // 状态码：0-待执行，1-通过，2-失败，3-跳过
                    if ("ui".equals(executionType)) {
                        // UI测试结果由回调更新，这里不统计
                        // status=0 表示等待 ATG-Client 执行
                        // status=2 表示提交失败（ATG-Client 不可用等）
                        if (detail.getStatus() == 2) {
                            failed++; // 提交失败算作失败
                        }
                        log.info("UI测试用例已提交到ATG-Client，状态: {}", detail.getStatus());
                    } else {
                        // 非UI测试立即统计
                        if (detail.getStatus() == 1) {
                            passed++;
                        } else if (detail.getStatus() == 2) {
                            failed++;
                        } else if (detail.getStatus() == 3) {
                            skipped++;
                        }
                    }
                    
                } catch (Exception e) {
                    log.error("执行测试用例失败: {}", testCase.getId(), e);
                    failed++;
                }
            }
            
            // 更新执行结果
            if ("ui".equals(executionType)) {
                // UI测试保持执行中状态，等待ATG-Client回调完成
                // 如果有提交失败的用例，需要更新失败数
                if (failed > 0) {
                    execution.setFailedCases(failed);
                }
                execution.setStatus(1); // 执行中
                log.info("UI测试已提交到ATG-Client，保持执行中状态，等待回调");
            } else {
                // 非UI测试立即完成
                execution.setPassedCases(passed);
                execution.setFailedCases(failed);
                execution.setSkippedCases(skipped);
                execution.setStatus(2); // 已完成
                execution.setEndTime(java.time.LocalDateTime.now());
                
                if (execution.getStartTime() != null) {
                    long duration = java.time.Duration.between(
                        execution.getStartTime(),
                        execution.getEndTime()
                    ).toMillis();
                    execution.setDuration(duration);
                }
            }
            
            int updateResult = executionMapper.updateById(execution);
            log.info("执行记录更新结果: executionId={}, updateResult={}, status={}", 
                    executionId, updateResult, execution.getStatus());
            
            if ("ui".equals(executionType)) {
                log.info("UI测试已提交，执行ID: {}, 等待ATG-Client回调结果", executionId);
            } else {
                log.info("测试执行完成，执行ID: {}, 通过: {}, 失败: {}, 跳过: {}", 
                        executionId, passed, failed, skipped);
                
                // 自动生成测试报告
                try {
                    Long userId = execution.getExecutedBy() != null ? execution.getExecutedBy() : 1L;
                    reportService.generateReport(executionId, "html", userId);
                    log.info("测试报告生成成功，执行ID: {}", executionId);
                } catch (Exception reportEx) {
                    log.error("生成测试报告失败，执行ID: {}", executionId, reportEx);
                    // 报告生成失败不影响测试执行结果
                }
            }
            
        } catch (Exception e) {
            log.error("执行测试失败，执行ID: {}", executionId, e);
            
            try {
                // 重新查询以确保能拿到最新数据
                TestExecution failedExecution = executionMapper.selectById(executionId);
                if (failedExecution != null) {
                    failedExecution.setStatus(4); // 失败
                    failedExecution.setEndTime(java.time.LocalDateTime.now());
                    executionMapper.updateById(failedExecution);
                    log.info("已将执行状态更新为失败: executionId={}", executionId);
                } else {
                    log.error("无法查询到执行记录，无法更新状态: executionId={}", executionId);
                }
            } catch (Exception updateEx) {
                log.error("更新执行失败状态时出错: executionId={}", executionId, updateEx);
            }
        }
    }
}
