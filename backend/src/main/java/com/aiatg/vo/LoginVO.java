package com.aiatg.vo;

import lombok.Data;

/**
 * 登录响应VO
 * 基于 Session 认证，不再返回 Token
 */
@Data
public class LoginVO {
    
    private UserVO userInfo;
    
    public LoginVO(UserVO userInfo) {
        this.userInfo = userInfo;
    }
}
