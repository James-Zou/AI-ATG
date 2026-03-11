package com.aiatg.dto.jmeter;

import lombok.Data;

/**
 * JMeter样本结果
 */
@Data
public class JMeterSampleResult {
    
    /**
     * 样本名称
     */
    private String sampleLabel;
    
    /**
     * 是否成功
     */
    private Boolean success;
    
    /**
     * 响应码
     */
    private String responseCode;
    
    /**
     * 响应消息
     */
    private String responseMessage;
    
    /**
     * 响应时间（毫秒）
     */
    private Long responseTime;
    
    /**
     * 响应数据大小（字节）
     */
    private Long bytes;
    
    /**
     * 响应数据（响应体内容）
     */
    private String responseData;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 断言结果
     */
    private String assertionResults;
}
