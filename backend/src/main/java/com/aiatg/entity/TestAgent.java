package com.aiatg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 测试代理实体类
 */
@Data
@TableName("test_agent")
public class TestAgent {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String agentId;
    
    private String agentName;
    
    private String hostname;
    
    private String os;
    
    private String osVersion;
    
    private String browser;
    
    private String browserVersion;
    
    private String ip;
    
    private Integer status;
    
    private String token;
    
    private LocalDateTime lastHeartbeat;
    
    private LocalDateTime createdTime;
    
    private LocalDateTime updatedTime;
}
