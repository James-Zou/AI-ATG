package com.aiatg.config;

import com.aiatg.handler.ATGAuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * Web MVC 配置
 * 用于处理前端静态资源、SPA 路由和拦截器
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private ATGAuthInterceptor atgAuthInterceptor;

    /**
     * 配置静态资源处理
     * 将所有非API请求转发到前端的index.html，支持Vue Router的History模式
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requestedResource = location.createRelative(resourcePath);
                        // 如果请求的资源存在且可读，直接返回
                        if (requestedResource.exists() && requestedResource.isReadable()) {
                            return requestedResource;
                        }
                        // 否则返回 index.html，让前端路由接管
                        return new ClassPathResource("/static/index.html");
                    }
                });
    }

    /**
     * 配置视图控制器
     * 将根路径重定向到index.html
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
    }

    /**
     * 配置拦截器
     * 注册认证拦截器，并排除不需要认证的路径
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(atgAuthInterceptor)
                .addPathPatterns("/**")  // 拦截所有请求
                .excludePathPatterns(    // 排除不需要认证的路径（与 SecurityConfig 保持一致）
                        "/user/register",
                        "/user/login",
                        "/static/**",
                        "/favicon.ico",
                        "/error"
                );
    }
}
