package com.aiatg.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.aiatg.common.PageResult;
import com.aiatg.dto.RequirementDTO;
import com.aiatg.dto.RequirementQueryDTO;
import com.aiatg.entity.Project;
import com.aiatg.entity.Requirement;
import com.aiatg.entity.User;
import com.aiatg.mapper.ProjectMapper;
import com.aiatg.mapper.RequirementMapper;
import com.aiatg.mapper.UserMapper;
import com.aiatg.service.RequirementService;
import com.aiatg.vo.RequirementVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 需求服务实现类
 */
@Service
public class RequirementServiceImpl implements RequirementService {
    
    @Autowired
    private RequirementMapper requirementMapper;
    
    @Autowired
    private ProjectMapper projectMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    public RequirementVO createRequirement(RequirementDTO dto, Long userId) {
        // 验证项目是否存在
        Project project = projectMapper.selectById(dto.getProjectId());
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }
        
        // 创建需求
        Requirement requirement = new Requirement();
        BeanUtil.copyProperties(dto, requirement);
        
        // 处理附件URLs（转为JSON字符串）
        if (dto.getAttachmentUrls() != null && !dto.getAttachmentUrls().isEmpty()) {
            requirement.setAttachmentUrls(JSONUtil.toJsonStr(dto.getAttachmentUrls()));
        }
        
        requirement.setCreatedBy(userId);
        requirement.setCreatedTime(LocalDateTime.now());
        requirement.setStatus(StringUtils.hasText(dto.getStatus()) ? dto.getStatus() : "draft");
        
        requirementMapper.insert(requirement);
        
        return convertToVO(requirement);
    }
    
    @Override
    public RequirementVO updateRequirement(Long id, RequirementDTO dto) {
        Requirement requirement = requirementMapper.selectById(id);
        if (requirement == null) {
            throw new RuntimeException("需求不存在");
        }
        
        // 更新字段
        if (StringUtils.hasText(dto.getTitle())) {
            requirement.setTitle(dto.getTitle());
        }
        if (dto.getContent() != null) {
            requirement.setContent(dto.getContent());
        }
        if (StringUtils.hasText(dto.getType())) {
            requirement.setType(dto.getType());
        }
        if (StringUtils.hasText(dto.getPriority())) {
            requirement.setPriority(dto.getPriority());
        }
        if (dto.getAttachmentUrls() != null) {
            requirement.setAttachmentUrls(JSONUtil.toJsonStr(dto.getAttachmentUrls()));
        }
        if (StringUtils.hasText(dto.getStatus())) {
            requirement.setStatus(dto.getStatus());
        }
        
        requirement.setUpdatedTime(LocalDateTime.now());
        requirementMapper.updateById(requirement);
        
        return convertToVO(requirement);
    }
    
    @Override
    public void deleteRequirement(Long id) {
        Requirement requirement = requirementMapper.selectById(id);
        if (requirement == null) {
            throw new RuntimeException("需求不存在");
        }
        requirementMapper.deleteById(id);
    }
    
    @Override
    public RequirementVO getRequirementById(Long id) {
        Requirement requirement = requirementMapper.selectById(id);
        if (requirement == null) {
            throw new RuntimeException("需求不存在");
        }
        return convertToVO(requirement);
    }
    
    @Override
    public PageResult<RequirementVO> getRequirementList(RequirementQueryDTO query) {
        // 构建查询条件
        LambdaQueryWrapper<Requirement> queryWrapper = new LambdaQueryWrapper<>();
        
        if (query.getProjectId() != null) {
            queryWrapper.eq(Requirement::getProjectId, query.getProjectId());
        }
        
        if (StringUtils.hasText(query.getKeyword())) {
            queryWrapper.and(wrapper -> wrapper
                .like(Requirement::getTitle, query.getKeyword())
                .or()
                .like(Requirement::getContent, query.getKeyword())
            );
        }
        
        if (StringUtils.hasText(query.getType())) {
            queryWrapper.eq(Requirement::getType, query.getType());
        }
        
        if (StringUtils.hasText(query.getPriority())) {
            queryWrapper.eq(Requirement::getPriority, query.getPriority());
        }
        
        if (StringUtils.hasText(query.getStatus())) {
            queryWrapper.eq(Requirement::getStatus, query.getStatus());
        }
        
        queryWrapper.orderByDesc(Requirement::getCreatedTime);
        
        // 分页查询
        Page<Requirement> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<Requirement> resultPage = requirementMapper.selectPage(page, queryWrapper);
        
        // 转换为VO
        List<RequirementVO> voList = resultPage.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        return new PageResult<>(
            resultPage.getTotal(),
            voList,
            query.getPageNum(),
            query.getPageSize()
        );
    }
    
    @Override
    public void updateStatus(Long id, String status) {
        Requirement requirement = requirementMapper.selectById(id);
        if (requirement == null) {
            throw new RuntimeException("需求不存在");
        }
        
        requirement.setStatus(status);
        requirement.setUpdatedTime(LocalDateTime.now());
        requirementMapper.updateById(requirement);
    }
    
    /**
     * 将Requirement转换为RequirementVO
     */
    private RequirementVO convertToVO(Requirement requirement) {
        RequirementVO vo = new RequirementVO();
        BeanUtil.copyProperties(requirement, vo);
        
        // 解析附件URLs
        if (StringUtils.hasText(requirement.getAttachmentUrls())) {
            try {
                List<String> urls = JSONUtil.toList(requirement.getAttachmentUrls(), String.class);
                vo.setAttachmentUrls(urls);
            } catch (Exception e) {
                vo.setAttachmentUrls(null);
            }
        }
        
        // 获取项目名称
        if (requirement.getProjectId() != null) {
            Project project = projectMapper.selectById(requirement.getProjectId());
            if (project != null) {
                vo.setProjectName(project.getName());
            }
        }
        
        // 获取创建人姓名
        if (requirement.getCreatedBy() != null) {
            User user = userMapper.selectById(requirement.getCreatedBy());
            if (user != null) {
                vo.setCreatedByName(user.getNickname() != null ? user.getNickname() : user.getUsername());
            }
        }
        
        return vo;
    }
}
