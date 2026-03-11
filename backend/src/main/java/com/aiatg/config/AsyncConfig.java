package com.aiatg.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 异步执行配置
 * 启用Spring的异步方法支持
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    // 使用Spring默认的异步配置
    // 如需自定义线程池，可以创建Executor bean
}
