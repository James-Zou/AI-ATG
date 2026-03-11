package com.aiatg.util;

import com.aiatg.domain.RequestDomain;
import lombok.extern.slf4j.Slf4j;

/**
 * Security工具类
 */
@Slf4j
public class SecurityUtil {

    /**
     * 获取当前登录用户ID
     */
    public static Long getCurrentUserId() {
        try {
            String userId = UserHolder.getUserId();
            if (userId == null) {
                log.warn("用户信息为空");
                return null;
            }
            return Long.valueOf(userId);
        } catch (NumberFormatException e) {
            log.error("无法将用户ID转换为Long: {}", UserHolder.getUserId());
            return null;
        } catch (Exception e) {
            log.error("获取当前用户ID失败", e);
            return null;
        }
    }

    /**
     * 获取当前登录用户名
     */
    public static String getCurrentUsername() {
        try {
            RequestDomain requestDomain = UserHolder.get();
            return requestDomain != null ? requestDomain.getUsername() : null;
        } catch (Exception e) {
            log.error("获取当前用户名失败", e);
            return null;
        }
    }

    /**
     * 判断是否已认证
     */
    public static boolean isAuthenticated() {
        return UserHolder.get() != null;
    }
}
