package com.aiatg.handler;

import cn.hutool.core.util.IdUtil;
import com.aiatg.domain.RequestDomain;
import com.aiatg.entity.ApiKey;
import com.aiatg.entity.User;
import com.aiatg.service.ApiKeyService;
import com.aiatg.service.UserService;
import com.aiatg.util.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;

/**
 * 认证拦截器，支持双模式认证：
 * 1. Session/Cookie 认证（浏览器端）
 * 2. API Key 认证（第三方 API 调用）
 * 
 * @author roderick.zou
 * @Description: 从 Session 或 API Key 中获取用户信息，并存储到 ThreadLocal 中供后续使用
 * @date 2026/2/5 10:22
 */
@Slf4j
@Component("connectorAuthInterceptor")
public class ATGAuthInterceptor implements HandlerInterceptor {
    
    /**
     * Session 中存储用户信息的 key
     */
    private static final String SESSION_USER_KEY = "USER_INFO";
    
    @Autowired
    private ApiKeyService apiKeyService;
    
    @Autowired
    private UserService userService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        
        RequestDomain requestDomain = new RequestDomain();
        
        // 设置基础信息
        requestDomain.setLanguage(getFromHeader("Accept-Language", request, "zh-CN"));
        String timestamp = getFromHeader("timestamp", request, null);
        if (timestamp != null && !timestamp.isEmpty()) {
            requestDomain.setReqTime(Long.parseLong(timestamp));
        } else {
            requestDomain.setReqTime(System.currentTimeMillis());
        }
        
        String traceId = getFromHeader("traceId", request, null);
        if (StringUtils.isBlank(traceId)) {
            traceId = IdUtil.fastSimpleUUID();
        }
        requestDomain.setTraceId(traceId);

        // 【双模式认证】优先级顺序：Session > API Key > JWT（可选）
        
        // 优先级 1: 尝试从 Session 获取用户信息（浏览器端）
        HttpSession session = request.getSession(false);
        if (session != null) {
            RequestDomain sessionUser = (RequestDomain) session.getAttribute(SESSION_USER_KEY);
            if (sessionUser != null) {
                log.info("✅ 通过 Session 认证成功, userId={}, path={}", sessionUser.getUserId(), request.getServletPath());
                populateRequestDomain(requestDomain, sessionUser);
                UserHolder.set(requestDomain);
                return true;
            }
        }

        // 优先级 2: 尝试从 Header 获取 API Key（第三方 API 调用）
        String apiKey = getFromHeader("X-API-Key", request, null);
        if (StringUtils.isNotBlank(apiKey)) {
            return handleApiKeyAuth(apiKey, requestDomain, request, response);
        }

        // 优先级 3: 尝试从 Header 获取 JWT Token（兼容旧版 API，可选）
        String authHeader = getFromHeader("Authorization", request, null);
        if (StringUtils.isNotBlank(authHeader) && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return handleJwtTokenAuth(token, requestDomain, request, response);
        }

        // 所有认证方式都失败
        log.warn("❌ 认证失败: 未找到有效的认证信息, uri={}, IP={}", 
                 request.getRequestURI(), getRemoteIP(request));
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"未授权访问\"}");
        return false;
    }
    
    /**
     * 处理 API Key 认证
     */
    private boolean handleApiKeyAuth(String apiKey, RequestDomain requestDomain, 
                                     HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            // 验证 API Key
            ApiKey apiKeyEntity = apiKeyService.validateApiKey(apiKey);
            if (apiKeyEntity == null) {
                log.warn("❌ API Key 认证失败: 无效的 API Key, path={}", request.getServletPath());
                sendUnauthorizedResponse(response, "无效的 API Key");
                return false;
            }

            // 检查过期时间
            if (apiKeyEntity.getExpireTime() != null && LocalDateTime.now().isAfter(apiKeyEntity.getExpireTime())) {
                log.warn("❌ API Key 认证失败: API Key 已过期, apiKey={}", apiKey);
                sendUnauthorizedResponse(response, "API Key 已过期");
                return false;
            }

            // 检查状态
            if (apiKeyEntity.getStatus() == 0) {
                log.warn("❌ API Key 认证失败: API Key 已被禁用, apiKey={}", apiKey);
                sendUnauthorizedResponse(response, "API Key 已被禁用");
                return false;
            }

                // 获取关联的用户信息
                User user = userService.getUserEntityById(Long.valueOf(apiKeyEntity.getUserId()));
                if (user == null || user.getStatus() == 0) {
                    log.warn("❌ API Key 认证失败: 关联用户不存在或已禁用, userId={}", apiKeyEntity.getUserId());
                    sendUnauthorizedResponse(response, "关联用户不可用");
                    return false;
                }

            // 填充 RequestDomain
            requestDomain.setUserId(String.valueOf(user.getId()));
            requestDomain.setAccountId(apiKeyEntity.getAccountId());
            requestDomain.setUsername(user.getUsername());
            requestDomain.setTopAccountId(apiKeyEntity.getAccountId());

            // 存入 UserHolder
            UserHolder.set(requestDomain);
            
            log.info("✅ 通过 API Key 认证成功, appName={}, userId={}, path={}", 
                     apiKeyEntity.getAppName(), user.getId(), request.getServletPath());
            return true;
            
        } catch (Exception e) {
            log.error("❌ API Key 认证异常, path={}", request.getServletPath(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":500,\"message\":\"认证服务异常\"}");
            return false;
        }
    }

    /**
     * 处理 JWT Token 认证（兼容旧版 API，可选）
     * 如果不需要兼容，可以删除此方法或直接返回 false
     */
    private boolean handleJwtTokenAuth(String token, RequestDomain requestDomain,
                                       HttpServletRequest request, HttpServletResponse response) throws Exception {
        // TODO: 根据实际需求实现 JWT 验证逻辑
        // 示例：解析 token，验证签名，提取用户信息
        log.warn("⚠️ JWT Token 认证暂未实现, path={}", request.getServletPath());
        sendUnauthorizedResponse(response, "JWT Token 认证暂不支持");
        return false;
    }
    
    /**
     * 发送未授权响应
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(String.format("{\"code\":401,\"message\":\"%s\"}", message));
    }
    
    /**
     * 将 Session 用户信息填充到 RequestDomain
     */
    private void populateRequestDomain(RequestDomain target, RequestDomain source) {
        target.setUserId(source.getUserId());
        target.setAccountId(source.getAccountId());
        target.setUsername(source.getUsername());
        target.setTopAccountId(source.getTopAccountId());
        
        // 语言优先使用当前请求的，如果没有则用 Session 中的
        if (StringUtils.isBlank(target.getLanguage())) {
            target.setLanguage(source.getLanguage());
        }
    }
    
    /**
     * 获取真实IP 不看代理
     * @param request
     * @return
     */
    public String getRemoteIP(HttpServletRequest request) {
        if (request.getHeader("x-forwarded-for") == null) {
            return request.getRemoteAddr();
        }
        return request.getHeader("x-forwarded-for");
    }
    
    /**
     * 获取请求body参数数据
     * @param request
     * @return
     */
    public String getRequestBody(HttpServletRequest request){
        if(request instanceof ContentCachingRequestWrapper){
            ContentCachingRequestWrapper requestWrapper=(ContentCachingRequestWrapper)request;
            return new String(requestWrapper.getContentAsByteArray());
        }
        return null;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.remove();
    }
    
    
    /**
     * 从 Header 或 Parameter 中获取值
     * @param param 参数名
     * @param request 请求对象
     * @param defaultValue 默认值
     */
    private String getFromHeader(String param, HttpServletRequest request, String defaultValue) {
        String result = request.getHeader(param);
        if (result == null || result.isEmpty()) {
            result = request.getParameter(param);
        }
        return (result == null || result.isEmpty()) ? defaultValue : result;
    }
    
}