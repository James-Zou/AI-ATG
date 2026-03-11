package com.aiatg.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 脚本执行记录实体类
 */
@Data
@TableName("script_execution")
public class ScriptExecution {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联的技能ID
     */
    private Long skillId;

    /**
     * 执行名称
     */
    private String executionName;

    /**
     * 脚本语言
     */
    private String scriptLanguage;

    /**
     * 执行的脚本内容快照
     */
    @TableField(value = "script_content", jdbcType = org.apache.ibatis.type.JdbcType.LONGVARCHAR)
    private String scriptContent;

    /**
     * 执行状态: PENDING(待执行), RUNNING(执行中), SUCCESS(成功), FAILED(失败)
     */
    private String status;

    /**
     * 执行输出
     */
    @TableField(value = "output", jdbcType = org.apache.ibatis.type.JdbcType.LONGVARCHAR)
    private String output;

    /**
     * 错误信息
     */
    @TableField(value = "error_message", jdbcType = org.apache.ibatis.type.JdbcType.LONGVARCHAR)
    private String errorMessage;

    /**
     * 退出码
     */
    private Integer exitCode;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 执行持续时间（毫秒）
     */
    private Long durationMs;

    /**
     * 创建人ID
     */
    private Long createBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 逻辑删除标识
     */
    @TableLogic
    private Integer deleted;
}
