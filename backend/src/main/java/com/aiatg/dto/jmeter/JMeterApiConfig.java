package com.aiatg.dto.jmeter;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * JMeter API测试配置
 */
@Data
public class JMeterApiConfig {
    
    /**
     * 接口名称
     */
    private String name;
    
    /**
     * 请求方法
     */
    private String method;
    
    /**
     * 请求URL
     */
    private String url;
    
    /**
     * 请求头
     * 支持两种格式：
     * 1. Map<String, String> - 键值对映射
     * 2. String - 换行分隔的格式，如："Key1: Value1\nKey2: Value2"
     */
    private Object headers;
    
    /**
     * 请求体
     */
    private String body;
    
    /**
     * 超时时间（毫秒）
     */
    private Integer timeout;
    
    /**
     * 断言配置
     */
    private List<JMeterAssertion> assertions;
}
