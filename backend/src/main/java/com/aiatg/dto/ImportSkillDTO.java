package com.aiatg.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 从测试套件导入技能的数据传输对象
 */
@Data
public class ImportSkillDTO {

    /**
     * 测试套件ID
     */
    @NotNull(message = "测试套件ID不能为空")
    private Long testSuiteId;

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
     * 配置数据（测试步骤的 JSON 配置，可包含 {参数名称} 占位符）
     */
    private String configData;
}
