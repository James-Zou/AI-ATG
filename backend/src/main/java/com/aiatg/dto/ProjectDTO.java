package com.aiatg.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 项目DTO
 */
@Data
public class ProjectDTO {
    
    @NotBlank(message = "项目名称不能为空")
    private String name;
    
    private String description;
    
    private Integer status = 1;
}
