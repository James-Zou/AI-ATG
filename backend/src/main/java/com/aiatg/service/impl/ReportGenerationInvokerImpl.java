package com.aiatg.service.impl;

import com.aiatg.service.ReportGenerationInvoker;
import com.aiatg.service.TestReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 在新事务中触发报告生成，避免与 ATG-Client 回调主事务互相影响（如 rollback-only）
 */
@Slf4j
@Service
public class ReportGenerationInvokerImpl implements ReportGenerationInvoker {

    @Autowired
    private TestReportService reportService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void generateInNewTransaction(Long executionId) {
        reportService.generateReport(executionId, "html", null);
    }
}
