package com.aiatg.service;

import com.aiatg.vo.DashboardVO;

/**
 * 仪表盘服务接口
 */
public interface DashboardService {
    
    /**
     * 获取仪表盘统计数据
     * 
     * @param period 时间周期：week-本周, month-本月, year-本年
     * @return 仪表盘数据
     */
    DashboardVO getDashboardData(String period);
}
