package com.aiatg.service;

import com.aiatg.common.PageResult;
import com.aiatg.dto.ReportQueryDTO;
import com.aiatg.vo.ReportDetailVO;
import com.aiatg.vo.ReportStatisticsVO;
import com.aiatg.vo.ReportVO;

/**
 * 测试报告服务接口
 */
public interface TestReportService {
    
    /**
     * 生成测试报告
     */
    ReportVO generateReport(Long executionId, String reportType, Long userId);
    
    /**
     * 获取报告详情
     */
    ReportDetailVO getReportDetail(Long id);
    
    /**
     * 获取报告列表
     */
    PageResult<ReportVO> getReportList(ReportQueryDTO queryDTO);
    
    /**
     * 导出HTML报告
     */
    String exportHtml(Long reportId);
    
    /**
     * 导出PDF报告
     */
    String exportPdf(Long reportId);
    
    /**
     * 获取统计数据
     */
    ReportStatisticsVO getStatistics(Long projectId, String startDate, String endDate);
    
    /**
     * 删除报告
     */
    void deleteReport(Long id);
    
    /**
     * 人工确认报告
     * @param reportId 报告ID
     * @param confirmStatus 确认状态：1-通过，2-失败
     * @param confirmRemark 确认备注
     * @param userId 确认人ID
     */
    void confirmReport(Long reportId, Integer confirmStatus, String confirmRemark, Long userId);
}
