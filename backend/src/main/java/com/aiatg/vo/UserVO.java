package com.aiatg.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息VO（不包含密码）
 */
@Data
public class UserVO {
    
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String avatarUrl;
    private String role;
    private Integer status;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createdTime;
}
