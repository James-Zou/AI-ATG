package com.aiatg.controller;

import com.aiatg.common.PageResult;
import com.aiatg.common.Result;
import com.aiatg.dto.ReportQueryDTO;
import com.aiatg.service.TestReportService;
import com.aiatg.util.UserHolder;
import com.aiatg.vo.ReportDetailVO;
import com.aiatg.vo.ReportStatisticsVO;
import com.aiatg.vo.ReportVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 测试报告控制器
 */
@RestController
@RequestMapping("/report")
@CrossOrigin
public class TestReportController {
    
    @Autowired
    private TestReportService reportService;
    
    /**
     * 生成测试报告
     */
    @PostMapping("/generate")
    public Result<ReportVO> generateReport(
        @RequestParam Long executionId,
        @RequestParam(defaultValue = "html") String reportType
    ) {
        try {
            Long userId = getCurrentUserId();
            ReportVO vo = reportService.generateReport(executionId, reportType, userId);
            return Result.success("报告生成成功", vo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取报告详情
     */
    @GetMapping("/{id}")
    public Result<ReportDetailVO> getReportDetail(@PathVariable Long id) {
        try {
            ReportDetailVO vo = reportService.getReportDetail(id);
            return Result.success(vo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取报告列表
     */
    @GetMapping("/list")
    public Result<PageResult<ReportVO>> getReportList(ReportQueryDTO queryDTO) {
        try {
            PageResult<ReportVO> result = reportService.getReportList(queryDTO);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 导出HTML报告
     */
    @GetMapping("/{id}/export/html")
    public void exportHtml(@PathVariable Long id, HttpServletResponse response) {
        try {
            String html = reportService.exportHtml(id);
            
            response.setContentType("text/html;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=report_" + id + ".html");
            
            PrintWriter writer = response.getWriter();
            writer.write(html);
            writer.flush();
            
        } catch (IOException e) {
            throw new RuntimeException("导出HTML报告失败: " + e.getMessage());
        }
    }
    
    /**
     * 导出PDF报告
     */
    @GetMapping("/{id}/export/pdf")
    public Result<String> exportPdf(@PathVariable Long id) {
        try {
            String pdfUrl = reportService.exportPdf(id);
            return Result.success("PDF报告导出成功", pdfUrl);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取统计数据
     */
    @GetMapping("/statistics")
    public Result<ReportStatisticsVO> getStatistics(
        @RequestParam(required = false) Long projectId,
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate
    ) {
        try {
            ReportStatisticsVO vo = reportService.getStatistics(projectId, startDate, endDate);
            return Result.success(vo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除报告
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteReport(@PathVariable Long id) {
        try {
            reportService.deleteReport(id);
            return Result.success("报告删除成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 人工确认报告
     */
    @PostMapping("/{id}/confirm")
    public Result<Void> confirmReport(
        @PathVariable Long id,
        @RequestParam Integer confirmStatus,
        @RequestParam(required = false) String confirmRemark
    ) {
        try {
            Long userId = getCurrentUserId();
            reportService.confirmReport(id, confirmStatus, confirmRemark, userId);
            
            String statusText = confirmStatus == 1 ? "通过" : "失败";
            return Result.success("报告确认成功: " + statusText, null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取待确认的报告列表
     */
    @GetMapping("/pending-confirm")
    public Result<PageResult<ReportVO>> getPendingConfirmReports(ReportQueryDTO queryDTO) {
        try {
            // 只查询待确认的报告
            queryDTO.setConfirmStatus(0);
            PageResult<ReportVO> result = reportService.getReportList(queryDTO);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        String userId = UserHolder.getUserId();
        return userId != null ? Long.valueOf(userId) : null;
    }
}
