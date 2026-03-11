package com.aiatg.service.impl;

import cn.hutool.core.util.StrUtil;
import com.aiatg.config.SeleniumConfig;
import com.aiatg.entity.TestExecution;
import com.aiatg.entity.TestExecutionDetail;
import com.aiatg.mapper.TestExecutionDetailMapper;
import com.aiatg.mapper.TestExecutionMapper;
import com.aiatg.service.AtgClientResultService;
import com.aiatg.service.ReportGenerationInvoker;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ATG-Client 结果处理服务实现
 */
@Slf4j
@Service
public class AtgClientResultServiceImpl implements AtgClientResultService {
    
    @Autowired
    private TestExecutionDetailMapper detailMapper;
    
    @Autowired
    private TestExecutionMapper executionMapper;
    
    @Autowired
    private SeleniumConfig seleniumConfig;
    
    @Autowired
    private ReportGenerationInvoker reportGenerationInvoker;
    
    @Override
    @Transactional
    public void saveResult(Long executionId, Long testCaseId, String status, Long duration,
                          String logs, String errorMessage, String screenshot) {
        
        log.info("收到ATG-Client回调: executionId={}, testCaseId={}, status={}, duration={}ms", 
                executionId, testCaseId, status, duration);
        
        // 查找对应的执行明细
        LambdaQueryWrapper<TestExecutionDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TestExecutionDetail::getExecutionId, executionId)
               .eq(TestExecutionDetail::getTestCaseId, testCaseId)
               .orderByDesc(TestExecutionDetail::getId)
               .last("LIMIT 1");
        
        TestExecutionDetail detail = detailMapper.selectOne(wrapper);
        
        if (detail == null) {
            log.error("未找到对应的执行明细: executionId={}, testCaseId={}", executionId, testCaseId);
            throw new RuntimeException("未找到对应的执行明细");
        }
        
        log.info("找到执行明细: detailId={}, 原状态={}", detail.getId(), detail.getStatus());
        
        // 更新执行明细
        detail.setEndTime(LocalDateTime.now());
        detail.setDuration(duration);
        detail.setLogs(logs);
        detail.setErrorMessage(errorMessage);
        
        // 状态映射：passed -> 1, failed -> 2
        Integer newStatus = "passed".equalsIgnoreCase(status) ? 1 : 2;
        detail.setStatus(newStatus);
        
        log.info("更新状态: detailId={}, 旧状态={} -> 新状态={}", 
                detail.getId(), detail.getStatus(), newStatus);
        
        // 保存截图
        if (StrUtil.isNotBlank(screenshot)) {
            String screenshotPath = saveScreenshot(screenshot, testCaseId, executionId);
            detail.setScreenshotUrl(screenshotPath);
            log.info("截图已保存: {}", screenshotPath);
        }
        
        int updateCount = detailMapper.updateById(detail);
        log.info("执行明细更新结果: detailId={}, updateCount={}, status={}", 
                detail.getId(), updateCount, detail.getStatus());
        
        // 检查所有用例是否执行完成，更新执行状态
        updateExecutionStatus(executionId);
    }
    
    /**
     * 更新执行状态
     */
    private void updateExecutionStatus(Long executionId) {
        log.info("开始更新执行状态: executionId={}", executionId);
        
        TestExecution execution = executionMapper.selectById(executionId);
        if (execution == null) {
            log.error("未找到执行记录: executionId={}", executionId);
            return;
        }
        
        log.info("当前执行状态: executionId={}, status={}, passed={}, failed={}", 
                executionId, execution.getStatus(), execution.getPassedCases(), execution.getFailedCases());
        
        // 查询所有执行明细
        LambdaQueryWrapper<TestExecutionDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TestExecutionDetail::getExecutionId, executionId);
        List<TestExecutionDetail> details = detailMapper.selectList(wrapper);
        
        if (details.isEmpty()) {
            log.warn("未找到执行明细: executionId={}", executionId);
            return;
        }
        
        log.info("查询到执行明细数量: {}", details.size());
        
        // 统计结果
        int passed = 0;
        int failed = 0;
        int running = 0;
        
        for (TestExecutionDetail detail : details) {
            log.debug("明细状态: detailId={}, testCaseId={}, status={}", 
                    detail.getId(), detail.getTestCaseId(), detail.getStatus());
            
            // 状态：0-待执行，1-通过，2-失败，3-跳过
            if (detail.getStatus() == null || detail.getStatus() == 0) {
                running++;
            } else if (detail.getStatus() == 1) {
                passed++;
            } else if (detail.getStatus() == 2) {
                failed++;
            } else if (detail.getStatus() == 3) {
                // 跳过的用例也算已完成
                log.debug("跳过的用例: testCaseId={}", detail.getTestCaseId());
            } else {
                log.warn("未知状态: detailId={}, status={}", detail.getId(), detail.getStatus());
                running++;
            }
        }
        
        log.info("统计结果: executionId={}, passed={}, failed={}, running={}", 
                executionId, passed, failed, running);
        
        execution.setPassedCases(passed);
        execution.setFailedCases(failed);
        
        // 如果所有用例都执行完成，更新执行状态为已完成
        if (running == 0) {
            execution.setStatus(2); // 已完成
            execution.setEndTime(LocalDateTime.now());
            
            if (execution.getStartTime() != null) {
                long totalDuration = java.time.Duration.between(
                    execution.getStartTime(),
                    execution.getEndTime()
                ).toMillis();
                execution.setDuration(totalDuration);
            }
            
            log.info("所有用例执行完成: executionId={}, passed={}, failed={}, duration={}ms", 
                    executionId, passed, failed, execution.getDuration());
            
            // 自动生成测试报告
            autoGenerateReport(executionId);
        } else {
            log.info("仍有{}个用例未完成，保持执行中状态", running);
        }
        
        int updateCount = executionMapper.updateById(execution);
        log.info("执行记录更新结果: executionId={}, updateCount={}, status={}, passed={}, failed={}", 
                executionId, updateCount, execution.getStatus(), execution.getPassedCases(), execution.getFailedCases());
    }
    
    /**
     * 保存截图
     */
    private String saveScreenshot(String base64Data, Long caseId, Long executionId) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String filename = String.format("screenshot_%d_%d_%s.png", executionId, caseId, timestamp);
            
            Path dir = Paths.get(seleniumConfig.getScreenshotPath());
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            
            Path filepath = dir.resolve(filename);
            
            // 解码 Base64 并保存
            byte[] decodedBytes = java.util.Base64.getDecoder().decode(base64Data);
            Files.write(filepath, decodedBytes);
            
            return filepath.toString();
        } catch (Exception e) {
            log.error("保存截图失败", e);
            return null;
        }
    }
    
    /**
     * 自动生成测试报告
     */
    private void autoGenerateReport(Long executionId) {
        try {
            log.info("========================================");
            log.info("开始自动生成测试报告: executionId={}", executionId);
            
            // 在新事务中生成报告，失败不影响主事务（避免 rollback-only）
            reportGenerationInvoker.generateInNewTransaction(executionId);
            
            log.info("✓ 测试报告自动生成成功: executionId={}", executionId);
            log.info("========================================");
        } catch (Exception e) {
            log.error("========================================");
            log.error("✗ 自动生成测试报告失败: executionId={}", executionId, e);
            log.error("错误详情: {}", e.getMessage());
            log.error("========================================");
        }
    }
}
