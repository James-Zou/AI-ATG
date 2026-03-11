package com.aiatg.dto;

import lombok.Data;

/**
 * 任务结果DTO
 */
@Data
public class TaskResultDTO {
    
    private Integer status;
    
    private Long duration;
    
    private String logs;
    
    private String errorMessage;
    
    private String stackTrace;
    
    private String screenshotBase64;
}
