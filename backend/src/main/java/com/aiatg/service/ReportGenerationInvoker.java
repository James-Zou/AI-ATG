package com.aiatg.service;

/**
 * 在新事务中触发报告生成，避免与回调主事务互相影响（如 rollback-only）
 */
public interface ReportGenerationInvoker {

    /**
     * 在新事务中生成报告，失败不影响主事务提交
     */
    void generateInNewTransaction(Long executionId);
}
