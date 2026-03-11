package com.aiatg.dto.jmeter;

import lombok.Data;

/**
 * JMeter断言配置
 */
@Data
public class JMeterAssertion {
    
    /**
     * 断言类型：response_code, response_message, response_data
     */
    private String type;
    
    /**
     * 断言条件：equals, contains, matches
     */
    private String condition;
    
    /**
     * 期望值
     */
    private String expectedValue;
}
