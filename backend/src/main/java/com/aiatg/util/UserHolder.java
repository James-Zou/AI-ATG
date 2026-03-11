package com.aiatg.util;

import com.aiatg.domain.RequestDomain;

/**
 * 用户信息持有者，基于 ThreadLocal 实现线程级别的用户信息存储
 * @author roderick.zou
 * @date 2026/2/5
 */
public class UserHolder {
    
    private static final ThreadLocal<RequestDomain> USER_THREAD_LOCAL = new ThreadLocal<>();
    
    /**
     * 设置当前线程的用户信息
     * @param requestDomain 请求域对象
     */
    public static void set(RequestDomain requestDomain) {
        USER_THREAD_LOCAL.set(requestDomain);
    }
    
    /**
     * 获取当前线程的用户信息
     * @return 请求域对象
     */
    public static RequestDomain get() {
        return USER_THREAD_LOCAL.get();
    }
    
    /**
     * 获取当前用户ID
     * @return 用户ID
     */
    public static String getUserId() {
        RequestDomain requestDomain = get();
        return requestDomain != null ? requestDomain.getUserId() : null;
    }
    
    /**
     * 获取当前租户ID
     * @return 租户ID
     */
    public static String getAccountId() {
        RequestDomain requestDomain = get();
        return requestDomain != null ? requestDomain.getAccountId() : null;
    }
    
    /**
     * 清除当前线程的用户信息
     */
    public static void remove() {
        USER_THREAD_LOCAL.remove();
    }
}
