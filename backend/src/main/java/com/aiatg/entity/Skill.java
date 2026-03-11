package com.aiatg.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 技能实体类
 */
@Data
@TableName("skill")
public class Skill {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 技能名称
     */
    private String name;

    /**
     * 技能描述
     */
    private String description;

    /**
     * 技能类型: TESTSUITE(测试套件), SCRIPT(自定义脚本)
     */
    private String type;

    /**
     * 关联的测试套件ID（当type=TESTSUITE时）
     */
    private Long testSuiteId;

    /**
     * 脚本语言（当type=SCRIPT时）: python, javascript, shell
     */
    private String scriptLanguage;

    /**
     * 脚本内容（当type=SCRIPT时）
     */
    @TableField(value = "script_content", jdbcType = org.apache.ibatis.type.JdbcType.LONGVARCHAR)
    private String scriptContent;

    /**
     * 技能配置数据（JSON格式，TEXT类型）
     * - 对于TESTSUITE类型：存储从测试套件复制的测试步骤数组，可包含{参数名称}占位符
     *   示例: [{"action":"input","value":"#username","input":"admin"},{"action":"input","value":"#title","input":"{工单标题}"}]
     * - 对于SCRIPT类型：存储脚本参数的默认值配置
     *   示例: {"username":"admin","title":"{工单标题}"}
     */
    @TableField(value = "config_data", jdbcType = org.apache.ibatis.type.JdbcType.LONGVARCHAR)
    private String configData;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 创建人ID
     */
    private Long createBy;

    /**
     * 更新人ID
     */
    private Long updateBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标识
     */
    @TableLogic
    private Integer deleted;
}
