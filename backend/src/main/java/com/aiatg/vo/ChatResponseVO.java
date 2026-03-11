package com.aiatg.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 聊天响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseVO {
    
    private String reply;
    
    private TestSuiteInfoVO suiteInfo;
    
    private SkillInfoVO skillInfo;
    
    /**
     * 候选技能列表（当AI无法精确识别时返回）
     */
    private List<SkillInfoVO> candidateSkills;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestSuiteInfoVO {
        private Long id;
        private String name;
        private String description;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillInfoVO {
        private Long id;
        private String name;
        private String description;
        private String type;
        /**
         * 需要替换的参数（从用户自然语言中提取）
         * 例如：{"工单标题": "测试申请", "手机号": "13800138000"}
         */
        private java.util.Map<String, Object> parameters;
    }
}
