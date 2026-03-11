package com.aiatg.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.aiatg.common.PageResult;
import com.aiatg.dto.ProjectDTO;
import com.aiatg.entity.*;
import com.aiatg.mapper.*;
import com.aiatg.service.ProjectService;
import com.aiatg.vo.ProjectVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 项目服务实现类
 */
@Slf4j
@Service
public class ProjectServiceImpl implements ProjectService {
    
    @Autowired
    private ProjectMapper projectMapper;
    
    @Autowired
    private ProjectMemberMapper memberMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private RequirementMapper requirementMapper;
    
    @Autowired
    private TestCaseMapper testCaseMapper;
    
    @Override
    @Transactional
    public ProjectVO createProject(ProjectDTO dto, Long userId) {
        Project project = new Project();
        BeanUtil.copyProperties(dto, project);
        project.setCreatedBy(userId);
        project.setCreatedTime(LocalDateTime.now());
        
        projectMapper.insert(project);
        
        // 自动添加创建者为项目管理员
        addMember(project.getId(), userId, "admin");
        
        return convertToVO(project);
    }
    
    @Override
    @Transactional
    public ProjectVO updateProject(Long id, ProjectDTO dto) {
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }
        
        BeanUtil.copyProperties(dto, project, "id", "createdBy", "createdTime");
        projectMapper.updateById(project);
        
        return convertToVO(project);
    }
    
    @Override
    public ProjectVO getProject(Long id) {
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }
        return convertToVO(project);
    }
    
    @Override
    public PageResult<ProjectVO> getProjectList(Integer pageNum, Integer pageSize) {
        Page<Project> page = new Page<>(pageNum, pageSize);
        Page<Project> resultPage = projectMapper.selectPage(page, null);
        
        List<ProjectVO> voList = resultPage.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        return new PageResult<>(
            resultPage.getTotal(),
            voList,
            pageNum,
            pageSize
        );
    }
    
    @Override
    @Transactional
    public void deleteProject(Long id) {
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }
        
        // 删除项目成员
        LambdaQueryWrapper<ProjectMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMember::getProjectId, id);
        memberMapper.delete(wrapper);
        
        // 删除项目
        projectMapper.deleteById(id);
    }
    
    @Override
    @Transactional
    public void addMember(Long projectId, Long userId, String role) {
        // 检查是否已经是成员
        LambdaQueryWrapper<ProjectMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMember::getProjectId, projectId)
               .eq(ProjectMember::getUserId, userId);
        ProjectMember existing = memberMapper.selectOne(wrapper);
        
        if (existing != null) {
            throw new RuntimeException("用户已经是项目成员");
        }
        
        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setUserId(userId);
        member.setRole(role != null ? role : "member");
        member.setJoinedTime(LocalDateTime.now());
        
        memberMapper.insert(member);
    }
    
    @Override
    @Transactional
    public void removeMember(Long projectId, Long userId) {
        LambdaQueryWrapper<ProjectMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMember::getProjectId, projectId)
               .eq(ProjectMember::getUserId, userId);
        
        memberMapper.delete(wrapper);
    }
    
    @Override
    public List<Object> getProjectMembers(Long projectId) {
        LambdaQueryWrapper<ProjectMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMember::getProjectId, projectId);
        List<ProjectMember> members = memberMapper.selectList(wrapper);
        
        return members.stream().map(member -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", member.getId());
            map.put("userId", member.getUserId());
            map.put("role", member.getRole());
            map.put("joinedTime", member.getJoinedTime());
            
            User user = userMapper.selectById(member.getUserId());
            if (user != null) {
                map.put("username", user.getUsername());
                map.put("nickname", user.getNickname());
                map.put("email", user.getEmail());
            }
            
            return map;
        }).collect(Collectors.toList());
    }
    
    private ProjectVO convertToVO(Project project) {
        ProjectVO vo = new ProjectVO();
        BeanUtil.copyProperties(project, vo);
        
        // 统计成员数
        LambdaQueryWrapper<ProjectMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(ProjectMember::getProjectId, project.getId());
        vo.setMemberCount(Math.toIntExact(memberMapper.selectCount(memberWrapper)));
        
        // 统计需求数
        LambdaQueryWrapper<Requirement> reqWrapper = new LambdaQueryWrapper<>();
        reqWrapper.eq(Requirement::getProjectId, project.getId());
        vo.setRequirementCount(Math.toIntExact(requirementMapper.selectCount(reqWrapper)));
        
        // 统计用例数
        LambdaQueryWrapper<TestCase> caseWrapper = new LambdaQueryWrapper<>();
        caseWrapper.eq(TestCase::getProjectId, project.getId());
        vo.setTestCaseCount(Math.toIntExact(testCaseMapper.selectCount(caseWrapper)));
        
        // 获取创建人名称
        if (project.getCreatedBy() != null) {
            User user = userMapper.selectById(project.getCreatedBy());
            if (user != null) {
                vo.setCreatedByName(user.getNickname() != null ? user.getNickname() : user.getUsername());
            }
        }
        
        return vo;
    }
}
