package com.aiatg.selenium;

import com.aiatg.config.SeleniumConfig;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * WebDriver工厂类
 */
@Slf4j
@Component
public class WebDriverFactory {
    
    @Autowired
    private SeleniumConfig seleniumConfig;
    
    /**
     * 创建WebDriver实例
     */
    public WebDriver createDriver() {
        log.info("创建WebDriver，浏览器类型: {}", seleniumConfig.getBrowser());
        
        // 初始化WebDriverManager
        seleniumConfig.initWebDriverManager();
        
        WebDriver driver;
        String browser = seleniumConfig.getBrowser().toLowerCase();
        
        switch (browser) {
            case "chrome":
                driver = createChromeDriver();
                break;
            case "firefox":
                driver = createFirefoxDriver();
                break;
            default:
                driver = createChromeDriver();
        }
        
        // 设置超时时间
        driver.manage().timeouts()
            .implicitlyWait(Duration.ofSeconds(seleniumConfig.getImplicitWait()))
            .pageLoadTimeout(Duration.ofSeconds(seleniumConfig.getPageLoadTimeout()))
            .scriptTimeout(Duration.ofSeconds(seleniumConfig.getScriptTimeout()));
        
        // 最大化窗口
        driver.manage().window().maximize();
        
        return driver;
    }
    
    /**
     * 创建Chrome驱动
     */
    private WebDriver createChromeDriver() {
        ChromeOptions options = new ChromeOptions();
        
        if (seleniumConfig.isHeadless()) {
            options.addArguments("--headless");
        }
        
        // 添加常用选项
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--window-size=1920,1080");
        
        return new ChromeDriver(options);
    }
    
    /**
     * 创建Firefox驱动
     */
    private WebDriver createFirefoxDriver() {
        FirefoxOptions options = new FirefoxOptions();
        
        if (seleniumConfig.isHeadless()) {
            options.addArguments("--headless");
        }
        
        return new FirefoxDriver(options);
    }
    
    /**
     * 安全关闭WebDriver
     */
    public void quitDriver(WebDriver driver) {
        if (driver != null) {
            try {
                driver.quit();
                log.info("WebDriver已关闭");
            } catch (Exception e) {
                log.error("关闭WebDriver失败", e);
            }
        }
    }
}
