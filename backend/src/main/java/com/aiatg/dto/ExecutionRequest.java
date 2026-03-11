package com.aiatg.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * 执行请求DTO
 */
@Data
public class ExecutionRequest {
    
    @NotNull(message = "项目ID不能为空")
    private Long projectId;
    
    private Long suiteId;
    
    private List<Long> testCaseIds;
    
    @NotNull(message = "执行类型不能为空")
    private String executionType;
    
    private String executionName;
    
    private String environment;
    
    private String triggerType;
    
    /**
     * 动态参数（用于运行时替换测试步骤或脚本中的占位符）
     */
    private Map<String, Object> parameters;
}
