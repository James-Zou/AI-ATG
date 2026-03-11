package com.aiatg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 测试报告实体类
 */
@Data
@TableName("test_report")
public class TestReport {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long executionId;
    
    private String reportName;
    
    private String reportType;
    
    private String reportUrl;
    
    private String summary;
    
    private Long fileSize;
    
    private Long generatedBy;
    
    private LocalDateTime createdTime;
    
    /**
     * 是否需要人工确认：0-不需要，1-需要
     */
    private Integer needConfirm;
    
    /**
     * 确认状态：0-待确认，1-已确认通过，2-已确认失败
     */
    private Integer confirmStatus;
    
    /**
     * 确认人ID
     */
    private Long confirmedBy;
    
    /**
     * 确认时间
     */
    private LocalDateTime confirmedTime;
    
    /**
     * 确认备注
     */
    private String confirmRemark;
}
