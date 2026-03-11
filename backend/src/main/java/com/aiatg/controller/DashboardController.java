package com.aiatg.controller;

import com.aiatg.common.Result;
import com.aiatg.service.DashboardService;
import com.aiatg.vo.DashboardVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 仪表盘控制器
 */
@Tag(name = "仪表盘管理")
@RestController
@RequestMapping("/dashboard")
@CrossOrigin
public class DashboardController {
    
    @Autowired
    private DashboardService dashboardService;
    
    /**
     * 获取仪表盘数据
     *
     * @param period 时间周期：week-本周, month-本月, year-本年
     * @return 仪表盘统计数据
     */
    @Operation(summary = "获取仪表盘数据")
    @GetMapping("/data")
    public Result<DashboardVO> getDashboardData(
            @RequestParam(defaultValue = "week") String period
    ) {
        DashboardVO data = dashboardService.getDashboardData(period);
        return Result.success(data);
    }
}
