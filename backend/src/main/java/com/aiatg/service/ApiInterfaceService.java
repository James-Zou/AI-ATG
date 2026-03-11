package com.aiatg.service;

import com.aiatg.common.PageResult;
import com.aiatg.dto.ApiInterfaceDTO;
import com.aiatg.dto.ApiInterfaceQueryDTO;
import com.aiatg.vo.ApiInterfaceVO;

import java.util.List;

/**
 * API接口服务接口
 */
public interface ApiInterfaceService {
    
    /**
     * 创建接口
     */
    ApiInterfaceVO createInterface(ApiInterfaceDTO dto, Long userId);
    
    /**
     * 更新接口
     */
    ApiInterfaceVO updateInterface(Long id, ApiInterfaceDTO dto, Long userId);
    
    /**
     * 删除接口
     */
    void deleteInterface(Long id);
    
    /**
     * 根据ID获取接口详情
     */
    ApiInterfaceVO getInterfaceById(Long id);
    
    /**
     * 分页查询接口列表
     */
    PageResult<ApiInterfaceVO> getInterfaceList(ApiInterfaceQueryDTO query);
    
    /**
     * 从cURL导入接口
     */
    ApiInterfaceVO importFromCurl(String curl, Long projectId, Long userId);
    
    /**
     * 发布接口
     */
    void publishInterface(Long id, Long userId);
    
    /**
     * 获取已发布的接口列表（供测试用例导入）
     */
    List<ApiInterfaceVO> getPublishedInterfaces(Long projectId);
}
