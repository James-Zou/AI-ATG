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

import com.aiatg.common.Result;
import com.aiatg.domain.RequestDomain;
import com.aiatg.dto.LoginDTO;
import com.aiatg.dto.RegisterDTO;
import com.aiatg.service.UserService;
import com.aiatg.vo.LoginVO;
import com.aiatg.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;

/**
 * 用户控制器
 */
@Slf4j
@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        try {
            UserVO userVO = userService.register(registerDTO);
            return Result.success("注册成功", userVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO, HttpServletRequest request, HttpServletResponse response) {
        try {
            LoginVO loginVO = userService.login(loginDTO);
            
            // 创建或获取 Session
            HttpSession session = request.getSession(true);
            
            // 创建 RequestDomain 对象并存储用户信息
            RequestDomain requestDomain = new RequestDomain();
            requestDomain.setUserId(String.valueOf(loginVO.getUserInfo().getId()));
            requestDomain.setUsername(loginVO.getUserInfo().getUsername());
            requestDomain.setAccountId(String.valueOf(loginVO.getUserInfo().getId())); // 如有租户ID，修改此处
            requestDomain.setLanguage("zh_CN"); // 默认语言，可从登录参数获取
            
            // 将用户信息存储到 Session
            session.setAttribute("USER_INFO", requestDomain);
            
            // 设置 Session 超时时间（30分钟）
            session.setMaxInactiveInterval(1800);
            
            // 设置 Cookie（可选，用于前端识别登录状态）
            Cookie sessionCookie = new Cookie("JSESSIONID", session.getId());
            sessionCookie.setHttpOnly(true); // 防止 XSS 攻击
            sessionCookie.setSecure(false); // 生产环境建议设为 true（需要 HTTPS）
            sessionCookie.setPath("/");
            sessionCookie.setMaxAge(1800); // 30分钟
            response.addCookie(sessionCookie);
            
            log.info("用户 {} 登录成功，SessionID: {}", loginVO.getUserInfo().getUsername(), session.getId());
            
            return Result.success("登录成功", loginVO);
        } catch (Exception e) {
            log.error("登录失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取用户信息
     */
    @GetMapping("/{id}")
    public Result<UserVO> getUserById(@PathVariable Long id) {
        try {
            UserVO userVO = userService.getUserById(id);
            return Result.success(userVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取所有用户
     */
    @GetMapping("/list")
    public Result<List<UserVO>> getAllUsers() {
        try {
            List<UserVO> users = userService.getAllUsers();
            return Result.success(users);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    public Result<UserVO> updateUser(@PathVariable Long id, @Valid @RequestBody RegisterDTO updateDTO) {
        try {
            UserVO userVO = userService.updateUser(id, updateDTO);
            return Result.success("更新成功", userVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
