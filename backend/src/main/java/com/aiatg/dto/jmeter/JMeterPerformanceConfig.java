package com.aiatg.dto.jmeter;

import lombok.Data;

/**
 * JMeter性能测试配置
 */
@Data
public class JMeterPerformanceConfig {
    
    /**
     * 测试名称
     */
    private String name;
    
    /**
     * 线程数
     */
    private Integer threads;
    
    /**
     * 启动时间（秒）
     */
    private Integer rampUp;
    
    /**
     * 循环次数（0表示无限循环）
     */
    private Integer loopCount;
    
    /**
     * 持续时间（秒，0表示不限制）
     */
    private Integer duration;
    
    /**
     * 请求URL
     */
    private String url;
    
    /**
     * 请求方法
     */
    private String method;
    
    /**
     * 超时时间（毫秒）
     */
    private Integer timeout;
    
    /**
     * 目标TPS（0表示不限制）
     */
    private Integer targetTPS;
    
    /**
     * 响应时间阈值（毫秒）
     */
    private Integer responseTimeThreshold;
}
