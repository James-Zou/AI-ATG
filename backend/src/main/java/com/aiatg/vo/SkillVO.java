package com.aiatg.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 技能视图对象
 */
@Data
public class SkillVO {

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
     * 技能类型
     */
    private String type;

    /**
     * 关联的测试套件ID
     */
    private Long testSuiteId;

    /**
     * 关联的测试套件名称
     */
    private String testSuiteName;

    /**
     * 脚本语言
     */
    private String scriptLanguage;

    /**
     * 脚本内容
     */
    private String scriptContent;

    /**
     * 技能配置数据（JSON格式）
     * - 对于TESTSUITE类型：存储从测试套件复制的测试步骤数组
     * - 对于SCRIPT类型：存储脚本参数的默认值配置
     */
    private String configData;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
