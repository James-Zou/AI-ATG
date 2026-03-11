package com.aiatg.selenium;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * UI操作执行器
 */
@Slf4j
@Component
public class UiActionExecutor {
    
    /**
     * 执行单个操作步骤
     */
    public void executeAction(WebDriver driver, JSONObject step) {
        String action = step.getStr("action");
        String locator = step.getStr("locator");
        String locatorValue = step.getStr("value");
        String inputValue = step.getStr("input");
        Integer timeout = step.getInt("timeout", 10);
        
        log.info("执行操作: action={}, locator={}, value={}", action, locator, locatorValue);
        
        WebElement element = null;
        
        // 定位元素（如果需要）
        if (StrUtil.isNotBlank(locator) && StrUtil.isNotBlank(locatorValue)) {
            element = findElement(driver, locator, locatorValue, timeout);
        }
        
        // 执行操作
        switch (action.toLowerCase()) {
            case "open":
            case "navigate":
                driver.get(inputValue != null ? inputValue : locatorValue);
                break;
                
            case "click":
                if (element != null) {
                    element.click();
                }
                break;
                
            case "input":
            case "sendkeys":
                if (element != null && StrUtil.isNotBlank(inputValue)) {
                    element.clear();
                    element.sendKeys(inputValue);
                }
                break;
                
            case "select":
                if (element != null && StrUtil.isNotBlank(inputValue)) {
                    Select select = new Select(element);
                    select.selectByVisibleText(inputValue);
                }
                break;
                
            case "wait":
                try {
                    Thread.sleep(timeout * 1000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                break;
                
            case "asserttext":
            case "verify":
                if (element != null && StrUtil.isNotBlank(inputValue)) {
                    String actualText = element.getText();
                    if (!actualText.contains(inputValue)) {
                        throw new AssertionError("文本断言失败：期望包含 '" + inputValue + "'，实际为 '" + actualText + "'");
                    }
                }
                break;
                
            case "asserttitle":
                if (StrUtil.isNotBlank(inputValue)) {
                    String actualTitle = driver.getTitle();
                    if (!actualTitle.contains(inputValue)) {
                        throw new AssertionError("标题断言失败：期望包含 '" + inputValue + "'，实际为 '" + actualTitle + "'");
                    }
                }
                break;
                
            case "asserturl":
                if (StrUtil.isNotBlank(inputValue)) {
                    String actualUrl = driver.getCurrentUrl();
                    if (!actualUrl.contains(inputValue)) {
                        throw new AssertionError("URL断言失败：期望包含 '" + inputValue + "'，实际为 '" + actualUrl + "'");
                    }
                }
                break;
                
            default:
                log.warn("未知的操作类型: {}", action);
        }
    }
    
    /**
     * 查找元素
     */
    private WebElement findElement(WebDriver driver, String locator, String value, int timeout) {
        By by = getLocator(locator, value);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
        
        try {
            return wait.until(ExpectedConditions.presenceOfElementLocated(by));
        } catch (TimeoutException e) {
            throw new RuntimeException("元素定位超时: " + locator + "=" + value);
        }
    }
    
    /**
     * 获取定位器
     */
    private By getLocator(String locator, String value) {
        switch (locator.toLowerCase()) {
            case "id":
                return By.id(value);
            case "name":
                return By.name(value);
            case "xpath":
                return By.xpath(value);
            case "css":
            case "cssselector":
                return By.cssSelector(value);
            case "classname":
                return By.className(value);
            case "linktext":
                return By.linkText(value);
            case "partiallinktext":
                return By.partialLinkText(value);
            case "tagname":
                return By.tagName(value);
            default:
                throw new RuntimeException("不支持的定位器类型: " + locator);
        }
    }
    
    /**
     * 截图
     */
    public byte[] takeScreenshot(WebDriver driver) {
        try {
            if (driver instanceof TakesScreenshot) {
                return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            }
        } catch (Exception e) {
            log.error("截图失败", e);
        }
        return null;
    }
}
