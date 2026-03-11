package com.aiatg.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 技能数据传输对象
 */
@Data
public class SkillDTO {

    /**
     * 技能名称
     */
    @NotBlank(message = "技能名称不能为空")
    private String name;

    /**
     * 技能描述
     */
    @NotBlank(message = "技能描述不能为空")
    private String description;

    /**
     * 技能类型: TESTSUITE, SCRIPT
     */
    @NotBlank(message = "技能类型不能为空")
    private String type;

    /**
     * 关联的测试套件ID（当type=TESTSUITE时）
     */
    private Long testSuiteId;

    /**
     * 脚本语言（当type=SCRIPT时）
     */
    private String scriptLanguage;

    /**
     * 脚本内容（当type=SCRIPT时）
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
    private Boolean enabled = true;
}
