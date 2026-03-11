package com.aiatg.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 测试用例DTO
 */
@Data
public class TestCaseDTO {
    
    @NotNull(message = "项目ID不能为空")
    private Long projectId;
    
    private Long requirementId;
    
    private Long suiteId;
    
    @NotBlank(message = "用例标题不能为空")
    private String title;
    
    private String description;
    
    private String preconditions;
    
    @NotBlank(message = "用例类型不能为空")
    private String type;
    
    @NotBlank(message = "优先级不能为空")
    private String priority;
    
    private String status;
    
    private String source;
    
    /**
     * 测试步骤
     * - UI自动化：List<Map<String, Object>> 格式的可执行步骤
     * - 接口/性能：JMeter配置
     * - 传统测试：List<TestStepDTO> 格式的步骤描述
     */
    private List<?> steps;
}
