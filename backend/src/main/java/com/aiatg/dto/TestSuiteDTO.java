package com.aiatg.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 测试套件DTO
 */
@Data
public class TestSuiteDTO {
    
    @NotNull(message = "项目ID不能为空")
    private Long projectId;
    
    @NotBlank(message = "套件名称不能为空")
    private String name;
    
    private String description;
    
    private Integer status;
}
