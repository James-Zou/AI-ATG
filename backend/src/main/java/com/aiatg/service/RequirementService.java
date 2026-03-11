package com.aiatg.service;

import com.aiatg.common.PageResult;
import com.aiatg.dto.RequirementDTO;
import com.aiatg.dto.RequirementQueryDTO;
import com.aiatg.vo.RequirementVO;

/**
 * 需求服务接口
 */
public interface RequirementService {
    
    /**
     * 创建需求
     */
    RequirementVO createRequirement(RequirementDTO dto, Long userId);
    
    /**
     * 更新需求
     */
    RequirementVO updateRequirement(Long id, RequirementDTO dto);
    
    /**
     * 删除需求
     */
    void deleteRequirement(Long id);
    
    /**
     * 根据ID获取需求
     */
    RequirementVO getRequirementById(Long id);
    
    /**
     * 分页查询需求列表
     */
    PageResult<RequirementVO> getRequirementList(RequirementQueryDTO query);
    
    /**
     * 更新需求状态
     */
    void updateStatus(Long id, String status);
}
