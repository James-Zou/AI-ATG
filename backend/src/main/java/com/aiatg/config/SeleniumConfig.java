package com.aiatg.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Selenium配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "selenium")
public class SeleniumConfig {
    
    /**
     * 浏览器类型：chrome/firefox/edge
     */
    private String browser = "chrome";
    
    /**
     * 是否启用无头模式
     */
    private boolean headless = true;
    
    /**
     * 隐式等待时间（秒）
     */
    private int implicitWait = 10;
    
    /**
     * 页面加载超时时间（秒）
     */
    private int pageLoadTimeout = 30;
    
    /**
     * 脚本执行超时时间（秒）
     */
    private int scriptTimeout = 30;
    
    /**
     * 截图保存路径
     */
    private String screenshotPath = "/tmp/screenshots";
    
    /**
     * 是否自动下载驱动
     */
    private boolean autoDownloadDriver = true;
    
    /**
     * 初始化WebDriverManager
     */
    public void initWebDriverManager() {
        if (!autoDownloadDriver) {
            return;
        }
        
        try {
            Class<?> wdmClass = Class.forName("io.github.bonigarcia.wdm.WebDriverManager");
            Object wdm;
            
            switch (browser.toLowerCase()) {
                case "chrome":
                    wdm = wdmClass.getMethod("chromedriver").invoke(null);
                    wdm.getClass().getMethod("setup").invoke(wdm);
                    break;
                case "firefox":
                    wdm = wdmClass.getMethod("firefoxdriver").invoke(null);
                    wdm.getClass().getMethod("setup").invoke(wdm);
                    break;
                case "edge":
                    wdm = wdmClass.getMethod("edgedriver").invoke(null);
                    wdm.getClass().getMethod("setup").invoke(wdm);
                    break;
                default:
                    wdm = wdmClass.getMethod("chromedriver").invoke(null);
                    wdm.getClass().getMethod("setup").invoke(wdm);
            }
        } catch (Exception e) {
            // WebDriverManager不可用时静默失败，假设手动配置了驱动路径
        }
    }
}
