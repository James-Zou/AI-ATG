package com.aiatg.service;

import com.aiatg.dto.SkillDTO;
import com.aiatg.vo.SkillVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 技能服务接口
 */
public interface SkillService {

    /**
     * 分页查询技能列表
     */
    Page<SkillVO> listSkills(String name, String type, Integer pageNum, Integer pageSize);

    /**
     * 创建技能
     */
    Long createSkill(SkillDTO skillDTO, Long userId);

    /**
     * 从测试套件导入技能
     * @param testSuiteId 测试套件ID
     * @param name 技能名称
     * @param description 技能描述
     * @param userId 用户ID
     * @param configData 配置数据（可选，如果为空则自动获取）
     * @return 技能ID
     */
    Long importFromTestSuite(Long testSuiteId, String name, String description, Long userId, String configData);

    /**
     * 更新技能
     */
    void updateSkill(Long id, SkillDTO skillDTO, Long userId);

    /**
     * 删除技能
     */
    void deleteSkill(Long id);

    /**
     * 获取技能详情
     */
    SkillVO getSkillById(Long id);

    /**
     * 执行技能
     * @param id 技能ID
     * @param userId 用户ID
     * @param parameters 从AI识别的用户参数（可选，用于动态替换配置）
     * @return 执行记录ID
     */
    Long executeSkill(Long id, Long userId, java.util.Map<String, Object> parameters);
}
