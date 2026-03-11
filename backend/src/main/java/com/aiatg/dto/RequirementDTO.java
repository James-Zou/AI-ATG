package com.aiatg.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 需求DTO
 */
@Data
public class RequirementDTO {
    
    @NotNull(message = "项目ID不能为空")
    private Long projectId;
    
    @NotBlank(message = "需求标题不能为空")
    private String title;
    
    private String content;
    
    private String type;
    
    private String priority;
    
    private String source;
    
    private String sourceId;
    
    private List<String> attachmentUrls;
    
    private String status;
}
