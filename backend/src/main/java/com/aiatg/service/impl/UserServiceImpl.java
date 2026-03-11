package com.aiatg.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.aiatg.dto.LoginDTO;
import com.aiatg.dto.RegisterDTO;
import com.aiatg.entity.User;
import com.aiatg.mapper.UserMapper;
import com.aiatg.service.UserService;
import com.aiatg.vo.LoginVO;
import com.aiatg.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    public UserVO register(RegisterDTO registerDTO) {
        // 邀请码校验：必须与当前小时匹配，格式 AIATG-yyyyMMddHH-RDZ
        String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHH"));
        String expectedCode = "AIATG-" + timeStr + "-RDZ";
        if (!expectedCode.equals(registerDTO.getInvitationCode().trim())) {
            throw new RuntimeException("邀请码无效或已过期");
        }

        // 检查用户名是否已存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, registerDTO.getUsername());
        User existUser = userMapper.selectOne(queryWrapper);
        
        if (existUser != null) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(BCrypt.hashpw(registerDTO.getPassword())); // 加密密码
        user.setNickname(registerDTO.getNickname());
        user.setEmail(registerDTO.getEmail());
        user.setPhone(registerDTO.getPhone());
        user.setRole("tester"); // 默认角色
        user.setStatus(1); // 默认启用
        user.setCreatedTime(LocalDateTime.now());
        
        userMapper.insert(user);
        
        return convertToVO(user);
    }
    
    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, loginDTO.getUsername());
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 验证密码
        if (!BCrypt.checkpw(loginDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new RuntimeException("用户已被禁用");
        }

        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        // 返回登录结果（不再生成 Token）
        return new LoginVO(convertToVO(user));
    }
    
    @Override
    public UserVO getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return convertToVO(user);
    }
    
    @Override
    public List<UserVO> getAllUsers() {
        List<User> users = userMapper.selectList(null);
        return users.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    
    @Override
    public UserVO updateUser(Long id, RegisterDTO updateDTO) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 更新字段
        if (updateDTO.getNickname() != null) {
            user.setNickname(updateDTO.getNickname());
        }
        if (updateDTO.getEmail() != null) {
            user.setEmail(updateDTO.getEmail());
        }
        if (updateDTO.getPhone() != null) {
            user.setPhone(updateDTO.getPhone());
        }
        if (updateDTO.getPassword() != null) {
            user.setPassword(BCrypt.hashpw(updateDTO.getPassword()));
        }
        user.setUpdatedTime(LocalDateTime.now());
        
        userMapper.updateById(user);
        
        return convertToVO(user);
    }
    
    @Override
    public void deleteUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        userMapper.deleteById(id);
    }
    
    @Override
    public User getUserEntityById(Long id) {
        return userMapper.selectById(id);
    }
    
    /**
     * 将User实体转换为UserVO
     */
    private UserVO convertToVO(User user) {
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }
}
