package com.aiatg.dto.jmeter;

import lombok.Data;

import java.util.List;

/**
 * JMeter执行结果
 */
@Data
public class JMeterResult {
    
    /**
     * 是否成功
     */
    private Boolean success;
    
    /**
     * 总样本数
     */
    private Integer totalSamples;
    
    /**
     * 成功样本数
     */
    private Integer successSamples;
    
    /**
     * 失败样本数
     */
    private Integer errorSamples;
    
    /**
     * 错误率（%）
     */
    private Double errorRate;
    
    /**
     * 平均响应时间（毫秒）
     */
    private Long avgResponseTime;
    
    /**
     * 最小响应时间（毫秒）
     */
    private Long minResponseTime;
    
    /**
     * 最大响应时间（毫秒）
     */
    private Long maxResponseTime;
    
    /**
     * 吞吐量（TPS）
     */
    private Double throughput;
    
    /**
     * 执行日志
     */
    private String logs;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 详细结果列表
     */
    private List<JMeterSampleResult> sampleResults;
}
