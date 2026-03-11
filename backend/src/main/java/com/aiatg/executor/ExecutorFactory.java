package com.aiatg.executor;

import com.aiatg.executor.impl.ApiTestExecutor;
import com.aiatg.executor.impl.PerformanceTestExecutor;
import com.aiatg.executor.impl.UiTestExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 执行器工厂
 */
@Slf4j
@Component
public class ExecutorFactory {
    
    @Autowired
    private ApiTestExecutor apiTestExecutor;
    
    @Autowired
    private UiTestExecutor uiTestExecutor;
    
    @Autowired
    private PerformanceTestExecutor performanceTestExecutor;
    
    /**
     * 根据类型获取执行器
     */
    public TestExecutor getExecutor(String executionType) {
        if (executionType == null) {
            throw new RuntimeException("执行类型不能为空");
        }
        
        TestExecutor executor = null;
        String type = executionType.toLowerCase();
        
        switch (type) {
            case "api":
                executor = apiTestExecutor;
                break;
            case "performance":
                executor = performanceTestExecutor;
                break;
            case "ui":
            case "functional":
                executor = uiTestExecutor;
                break;
            default:
                log.warn("未知的执行类型: {}, 使用API执行器", executionType);
                executor = apiTestExecutor;
        }
        
        if (!executor.isAvailable()) {
            throw new RuntimeException("执行器不可用: " + executionType);
        }
        
        return executor;
    }
}

