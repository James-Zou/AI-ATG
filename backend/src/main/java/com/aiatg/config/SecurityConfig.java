package com.aiatg.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security 配置（基于 Session + Cookie 认证）
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF（使用 Cookie 认证，生产环境建议启用 CSRF）
            .csrf().disable()
            
            // 配置 CORS
            .cors().configurationSource(corsConfigurationSource())
            
            .and()
            // 启用 Session 管理（不再使用 STATELESS）
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1) // 同一用户只允许一个会话
                .maxSessionsPreventsLogin(false) // 新登录会踢掉旧会话
            
            .and()
            .and()
            // 配置请求授权
            .authorizeRequests()
                // 允许所有请求通过 Spring Security 层
                // 实际的认证逻辑完全由 ATGAuthInterceptor 拦截器处理
                // 在 WebMvcConfig 中配置的 excludePathPatterns 路径不需要认证，其他路径需要认证
                .anyRequest().permitAll();
            // 认证说明：
            // - Spring Security 不再处理认证，仅处理 CORS、CSRF、Session 管理
            // - ATGAuthInterceptor 负责所有认证逻辑（Session/Cookie 或 API Key）
            // - WebMvcConfig 中的 excludePathPatterns 定义了不需要认证的路径
        
        return http.build();
    }
    
    /**
     * CORS 配置（支持本地开发与 ngrok 等代理访问）
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 使用 Origin 模式以同时支持固定域名与 ngrok 动态域名
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:3000",   // 前端（Vue）
            "http://localhost:5173",  // 前端（Vite 默认端口）
            "http://localhost:9999",  // ATG-Client
            "http://127.0.0.1:9999",  // ATG-Client（备用）
            "http://10.0.13.217:19081", // 生产环境前端地址（Nginx）
            "http://10.0.13.217",       // 生产环境前端地址（Nginx 80端口）
            "https://*.ngrok-free.app",
            "https://*.ngrok.io",
            "http://*.ngrok-free.app",
            "http://*.ngrok.io"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
