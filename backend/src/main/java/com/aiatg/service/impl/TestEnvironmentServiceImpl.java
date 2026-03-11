package com.aiatg.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.aiatg.common.PageResult;
import com.aiatg.dto.EnvironmentRequest;
import com.aiatg.entity.Project;
import com.aiatg.entity.TestEnvironment;
import com.aiatg.entity.User;
import com.aiatg.mapper.ProjectMapper;
import com.aiatg.mapper.TestEnvironmentMapper;
import com.aiatg.mapper.UserMapper;
import com.aiatg.service.TestEnvironmentService;
import com.aiatg.vo.EnvironmentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 测试环境服务实现类
 */
@Slf4j
@Service
public class TestEnvironmentServiceImpl implements TestEnvironmentService {
    
    @Autowired
    private TestEnvironmentMapper environmentMapper;
    
    @Autowired
    private ProjectMapper projectMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    @Transactional
    public EnvironmentVO createEnvironment(EnvironmentRequest request, Long userId) {
        // 检查环境编码是否已存在
        LambdaQueryWrapper<TestEnvironment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TestEnvironment::getProjectId, request.getProjectId())
               .eq(TestEnvironment::getEnvCode, request.getEnvCode());
        
        if (environmentMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("环境编码已存在");
        }
        
        TestEnvironment environment = new TestEnvironment();
        BeanUtil.copyProperties(request, environment);
        environment.setStatus(1); // 默认启用
        environment.setCreatedBy(userId);
        environment.setCreatedTime(LocalDateTime.now());
        environment.setUpdatedTime(LocalDateTime.now());
        
        environmentMapper.insert(environment);
        
        return convertToVO(environment);
    }
    
    @Override
    @Transactional
    public EnvironmentVO updateEnvironment(Long id, EnvironmentRequest request) {
        TestEnvironment environment = environmentMapper.selectById(id);
        if (environment == null) {
            throw new RuntimeException("测试环境不存在");
        }
        
        // 如果修改了环境编码，检查是否已存在
        if (!environment.getEnvCode().equals(request.getEnvCode())) {
            LambdaQueryWrapper<TestEnvironment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TestEnvironment::getProjectId, request.getProjectId())
                   .eq(TestEnvironment::getEnvCode, request.getEnvCode());
            
            if (environmentMapper.selectCount(wrapper) > 0) {
                throw new RuntimeException("环境编码已存在");
            }
        }
        
        BeanUtil.copyProperties(request, environment, "id", "createdBy", "createdTime");
        environment.setUpdatedTime(LocalDateTime.now());
        
        environmentMapper.updateById(environment);
        
        return convertToVO(environment);
    }
    
    @Override
    @Transactional
    public void deleteEnvironment(Long id) {
        TestEnvironment environment = environmentMapper.selectById(id);
        if (environment == null) {
            throw new RuntimeException("测试环境不存在");
        }
        
        environmentMapper.deleteById(id);
    }
    
    @Override
    public EnvironmentVO getEnvironmentById(Long id) {
        TestEnvironment environment = environmentMapper.selectById(id);
        if (environment == null) {
            throw new RuntimeException("测试环境不存在");
        }
        
        return convertToVO(environment);
    }
    
    @Override
    public PageResult<EnvironmentVO> getEnvironmentList(Long projectId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<TestEnvironment> wrapper = new LambdaQueryWrapper<>();
        
        if (projectId != null) {
            wrapper.eq(TestEnvironment::getProjectId, projectId);
        }
        
        wrapper.orderByDesc(TestEnvironment::getCreatedTime);
        
        Page<TestEnvironment> page = new Page<>(pageNum, pageSize);
        Page<TestEnvironment> resultPage = environmentMapper.selectPage(page, wrapper);
        
        List<EnvironmentVO> voList = resultPage.getRecords().stream()
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
    public List<EnvironmentVO> getAllEnvironments(Long projectId) {
        LambdaQueryWrapper<TestEnvironment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TestEnvironment::getProjectId, projectId)
               .eq(TestEnvironment::getStatus, 1) // 只返回启用的环境
               .orderByAsc(TestEnvironment::getEnvCode);
        
        List<TestEnvironment> environments = environmentMapper.selectList(wrapper);
        
        return environments.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
    }
    
    /**
     * 转换为VO
     */
    private EnvironmentVO convertToVO(TestEnvironment environment) {
        EnvironmentVO vo = new EnvironmentVO();
        BeanUtil.copyProperties(environment, vo);
        
        // 获取项目名称
        if (environment.getProjectId() != null) {
            Project project = projectMapper.selectById(environment.getProjectId());
            if (project != null) {
                vo.setProjectName(project.getName());
            }
        }
        
        // 获取创建人姓名
        if (environment.getCreatedBy() != null) {
            User user = userMapper.selectById(environment.getCreatedBy());
            if (user != null) {
                vo.setCreatedByName(user.getNickname() != null ? user.getNickname() : user.getUsername());
            }
        }
        
        return vo;
    }
}
