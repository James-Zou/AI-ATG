package com.aiatg.service;

import com.aiatg.common.PageResult;
import com.aiatg.dto.ProjectDTO;
import com.aiatg.vo.ProjectVO;

import java.util.List;

/**
 * 项目服务接口
 */
public interface ProjectService {
    
    /**
     * 创建项目
     */
    ProjectVO createProject(ProjectDTO dto, Long userId);
    
    /**
     * 更新项目
     */
    ProjectVO updateProject(Long id, ProjectDTO dto);
    
    /**
     * 获取项目详情
     */
    ProjectVO getProject(Long id);
    
    /**
     * 获取项目列表
     */
    PageResult<ProjectVO> getProjectList(Integer pageNum, Integer pageSize);
    
    /**
     * 删除项目
     */
    void deleteProject(Long id);
    
    /**
     * 添加项目成员
     */
    void addMember(Long projectId, Long userId, String role);
    
    /**
     * 移除项目成员
     */
    void removeMember(Long projectId, Long userId);
    
    /**
     * 获取项目成员列表
     */
    List<Object> getProjectMembers(Long projectId);
}
