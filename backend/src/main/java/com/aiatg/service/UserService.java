package com.aiatg.service;

import com.aiatg.dto.LoginDTO;
import com.aiatg.dto.RegisterDTO;
import com.aiatg.entity.User;
import com.aiatg.vo.LoginVO;
import com.aiatg.vo.UserVO;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 用户注册
     */
    UserVO register(RegisterDTO registerDTO);
    
    /**
     * 用户登录
     */
    LoginVO login(LoginDTO loginDTO);
    
    /**
     * 根据ID获取用户信息
     */
    UserVO getUserById(Long id);
    
    /**
     * 获取所有用户列表
     */
    List<UserVO> getAllUsers();
    
    /**
     * 更新用户信息
     */
    UserVO updateUser(Long id, RegisterDTO updateDTO);
    
    /**
     * 删除用户
     */
    void deleteUser(Long id);
    
    /**
     * 根据ID获取用户实体（用于内部认证）
     */
    User getUserEntityById(Long id);
}
