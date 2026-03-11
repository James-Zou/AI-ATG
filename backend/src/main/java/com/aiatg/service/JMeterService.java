package com.aiatg.service;

import com.aiatg.dto.jmeter.JMeterApiConfig;
import com.aiatg.dto.jmeter.JMeterPerformanceConfig;
import com.aiatg.dto.jmeter.JMeterResult;

/**
 * JMeter执行服务接口
 */
public interface JMeterService {
    
    /**
     * 执行API测试
     * 
     * @param config API测试配置
     * @return 执行结果
     */
    JMeterResult executeApiTest(JMeterApiConfig config);
    
    /**
     * 执行性能测试
     * 
     * @param config 性能测试配置
     * @return 执行结果
     */
    JMeterResult executePerformanceTest(JMeterPerformanceConfig config);
    
    /**
     * 检查JMeter是否可用
     * 
     * @return 是否可用
     */
    boolean isAvailable();
}
