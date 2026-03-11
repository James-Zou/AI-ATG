/*
 * Copyright 2026 James Zou
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aiatg.controller;

import com.aiatg.common.PageResult;
import com.aiatg.common.Result;
import com.aiatg.dto.ProjectDTO;
import com.aiatg.service.ProjectService;
import com.aiatg.util.UserHolder;
import com.aiatg.vo.ProjectVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 项目管理控制器
 */
@RestController
@RequestMapping("/project")
@CrossOrigin
public class ProjectController {
    
    @Autowired
    private ProjectService projectService;
    
    /**
     * 创建项目
     */
    @PostMapping
    public Result<ProjectVO> createProject(@Valid @RequestBody ProjectDTO dto) {
        try {
            Long userId = getCurrentUserId();
            ProjectVO vo = projectService.createProject(dto, userId);
            return Result.success("项目创建成功", vo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新项目
     */
    @PutMapping("/{id}")
    public Result<ProjectVO> updateProject(
        @PathVariable Long id,
        @Valid @RequestBody ProjectDTO dto
    ) {
        try {
            ProjectVO vo = projectService.updateProject(id, dto);
            return Result.success("项目更新成功", vo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取项目详情
     */
    @GetMapping("/{id}")
    public Result<ProjectVO> getProject(@PathVariable Long id) {
        try {
            ProjectVO vo = projectService.getProject(id);
            return Result.success(vo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取项目列表
     */
    @GetMapping("/list")
    public Result<PageResult<ProjectVO>> getProjectList(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        try {
            PageResult<ProjectVO> result = projectService.getProjectList(pageNum, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除项目
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteProject(@PathVariable Long id) {
        try {
            projectService.deleteProject(id);
            return Result.success("项目删除成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 添加项目成员
     */
    @PostMapping("/{projectId}/members")
    public Result<Void> addMember(
        @PathVariable Long projectId,
        @RequestBody Map<String, Object> params
    ) {
        try {
            Long userId = Long.valueOf(params.get("userId").toString());
            String role = (String) params.get("role");
            projectService.addMember(projectId, userId, role);
            return Result.success("成员添加成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 移除项目成员
     */
    @DeleteMapping("/{projectId}/members/{userId}")
    public Result<Void> removeMember(
        @PathVariable Long projectId,
        @PathVariable Long userId
    ) {
        try {
            projectService.removeMember(projectId, userId);
            return Result.success("成员移除成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取项目成员列表
     */
    @GetMapping("/{projectId}/members")
    public Result<List<Object>> getProjectMembers(@PathVariable Long projectId) {
        try {
            List<Object> members = projectService.getProjectMembers(projectId);
            return Result.success(members);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    private Long getCurrentUserId() {
        String userId = UserHolder.getUserId();
        return userId != null ? Long.valueOf(userId) : null;
    }
}
