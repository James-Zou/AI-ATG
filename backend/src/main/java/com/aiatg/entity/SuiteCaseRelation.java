package com.aiatg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 测试套件用例关联实体类
 */
@Data
@TableName("suite_case_relation")
public class SuiteCaseRelation {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long suiteId;
    
    private Long caseId;
    
    private Integer executeOrder;
    
    private LocalDateTime createdTime;
}
