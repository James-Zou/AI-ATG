package com.aiatg.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报告查询DTO
 */
@Data
public class ReportQueryDTO {
    
    private Long projectId;
    
    private String reportType;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    /**
     * 确认状态：0-待确认，1-已确认通过，2-已确认失败
     */
    private Integer confirmStatus;
    
    private Integer pageNum = 1;
    
    private Integer pageSize = 10;
}
