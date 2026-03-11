const { Builder, By, until, Key } = require('selenium-webdriver');
const chrome = require('selenium-webdriver/chrome');
const firefox = require('selenium-webdriver/firefox');
const path = require('path');
const fs = require('fs');

class TestExecutor {
  constructor(config) {
    this.config = config;
    this.driver = null;
  }
  
  async execute(testCase) {
    const { steps } = testCase;
    const logs = [];
    let status = 'passed';
    let errorMessage = null;
    let screenshot = null;
    
    const startTime = Date.now();
    console.log('[ATG-Client] 开始执行，正在启动浏览器...');
    
    try {
      // 创建WebDriver（Chrome/ChromeDriver 冷启动通常需数秒，此处为主要耗时）
      this.driver = await this.createDriver();
      logs.push('浏览器启动成功');
      console.log('[ATG-Client] 浏览器已就绪，开始执行步骤');
      
      // 执行测试步骤
      for (let i = 0; i < steps.length; i++) {
        const step = steps[i];
        logs.push(`步骤 ${i + 1}: ${step.action}`);
        
        try {
          await this.executeStep(step);
          logs.push(`  ✓ 执行成功`);
        } catch (error) {
          logs.push(`  ✗ 执行失败: ${error.message}`);
          throw error;
        }
      }
      
      logs.push('测试执行成功');
      
    } catch (error) {
      status = 'failed';
      errorMessage = error.message;
      logs.push(`测试执行失败: ${error.message}`);
    } finally {
      // 截图
      if (this.driver) {
        try {
          screenshot = await this.driver.takeScreenshot();
          logs.push('截图已保存');
        } catch (e) {
          logs.push('截图失败: ' + e.message);
        }
        
        // 关闭浏览器
        await this.driver.quit();
        this.driver = null;
        logs.push('浏览器已关闭');
      }
    }
    
    const duration = Date.now() - startTime;
    
    return {
      status,
      duration,
      logs: logs.join('\n'),
      errorMessage,
      screenshot
    };
  }
  
  async createDriver() {
    let driver;
    const startDriver = Date.now();

    if (this.config.browser === 'chrome') {
      const options = new chrome.Options();

      if (this.config.headless) {
        options.addArguments('--headless=new');
      }

      options.addArguments('--no-sandbox');
      options.addArguments('--disable-dev-shm-usage');
      options.addArguments('--disable-gpu');
      options.addArguments('--window-size=1920,1080');
      // 减少 Chrome 启动与后台工作，缩短启动耗时
      options.addArguments('--no-first-run');
      options.addArguments('--disable-extensions');
      options.addArguments('--disable-sync');
      options.addArguments('--disable-translate');
      options.addArguments('--disable-background-networking');
      options.addArguments('--disable-default-apps');
      options.addArguments('--disable-hang-monitor');
      options.addArguments('--disable-popup-blocking');
      options.addArguments('--disable-client-side-phishing-detection');
      options.addArguments('--disable-component-update');
      options.addArguments('--safebrowsing-disable-auto-update');

      // 配置 ChromeDriver 路径
      const service = new chrome.ServiceBuilder();
      if (this.config.chromeDriverPath) {
        if (!fs.existsSync(this.config.chromeDriverPath)) {
          throw new Error(`ChromeDriver 未找到: ${this.config.chromeDriverPath}\n请下载 ChromeDriver 并配置正确的路径`);
        }
        service.setPath(this.config.chromeDriverPath);
      } else {
        throw new Error('未配置 ChromeDriver 路径\n请在配置文件中设置 chromeDriverPath，或运行 install.sh 进行配置');
      }

      driver = await new Builder()
        .forBrowser('chrome')
        .setChromeOptions(options)
        .setChromeService(service)
        .build();
    } else if (this.config.browser === 'firefox') {
      const options = new firefox.Options();
      
      if (this.config.headless) {
        options.addArguments('-headless');
      }
      
      // 配置 GeckoDriver 路径
      const service = new firefox.ServiceBuilder();
      if (this.config.geckoDriverPath) {
        if (!fs.existsSync(this.config.geckoDriverPath)) {
          throw new Error(`GeckoDriver 未找到: ${this.config.geckoDriverPath}\n请下载 GeckoDriver 并配置正确的路径`);
        }
        service.setPath(this.config.geckoDriverPath);
      } else {
        throw new Error('未配置 GeckoDriver 路径\n请在配置文件中设置 geckoDriverPath，或运行 install.sh 进行配置');
      }
      
      driver = await new Builder()
        .forBrowser('firefox')
        .setFirefoxOptions(options)
        .setFirefoxService(service)
        .build();
    } else {
      throw new Error(`不支持的浏览器: ${this.config.browser}`);
    }
    
    await driver.manage().setTimeouts({
      implicit: 10000,
      pageLoad: 30000,
      script: 30000
    });

    await driver.manage().window().maximize();

    const elapsed = ((Date.now() - startDriver) / 1000).toFixed(1);
    console.log(`[ATG-Client] 浏览器启动耗时: ${elapsed} 秒`);
    if (parseFloat(elapsed) > 30) {
      console.log('[ATG-Client] 提示: 若启动过慢，请检查 Chrome 与 ChromeDriver 版本是否匹配、杀毒/安全软件是否排除 Chrome 与 ChromeDriver 进程');
    }

    return driver;
  }
  
  async executeStep(step) {
    const { action, locator, value, input, timeout } = step;
    
    switch (action.toLowerCase()) {
      case 'open':
      case 'navigate':
        await this.driver.get(input || value);
        break;
        
      case 'click':
        const clickElement = await this.findElement(locator, value, timeout);
        await clickElement.click();
        break;
        
      case 'input':
      case 'sendkeys':
        const inputElement = await this.findElement(locator, value, timeout);
        await inputElement.clear();
        await inputElement.sendKeys(input);
        break;

      case 'presskeys':
        const keyElement = await this.findElement(locator, value, timeout);
        const keyName = (input || 'Enter').trim();
        const key = Key[keyName.toUpperCase().replace(/\s/g, '_')] || keyName;
        await keyElement.sendKeys(key);
        break;
        
      case 'select':
        const selectElement = await this.findElement(locator, value, timeout);
        await selectElement.findElement(By.xpath(`//option[text()='${input}']`)).click();
        break;
        
      case 'wait':
        await this.driver.sleep((timeout || 1) * 1000);
        break;
        
      case 'asserttext':
      case 'verify':
        const textElement = await this.findElement(locator, value, timeout);
        const actualText = await textElement.getText();
        if (!actualText.includes(input)) {
          throw new Error(`文本断言失败：期望包含 "${input}"，实际为 "${actualText}"`);
        }
        break;
        
      case 'asserttitle':
        const title = await this.driver.getTitle();
        if (!title.includes(input)) {
          throw new Error(`标题断言失败：期望包含 "${input}"，实际为 "${title}"`);
        }
        break;
        
      case 'asserturl':
        const url = await this.driver.getCurrentUrl();
        if (!url.includes(input)) {
          throw new Error(`URL断言失败：期望包含 "${input}"，实际为 "${url}"`);
        }
        break;
        
      default:
        throw new Error(`未知的操作类型: ${action}`);
    }
  }
  
  async findElement(locator, value, timeout = 10) {
    const by = this.getLocator(locator, value);
    
    try {
      const element = await this.driver.wait(
        until.elementLocated(by),
        (timeout || 10) * 1000
      );
      
      await this.driver.wait(until.elementIsVisible(element), 5000);
      
      return element;
    } catch (error) {
      throw new Error(`元素定位失败: ${locator}=${value}`);
    }
  }
  
  getLocator(locator, value) {
    switch (locator.toLowerCase()) {
      case 'id':
        return By.id(value);
      case 'name':
        return By.name(value);
      case 'xpath':
        return By.xpath(value);
      case 'css':
      case 'cssselector':
        return By.css(value);
      case 'classname':
        return By.className(value);
      case 'linktext':
        return By.linkText(value);
      case 'partiallinktext':
        return By.partialLinkText(value);
      case 'tagname':
        return By.tagName(value);
      default:
        throw new Error(`不支持的定位器类型: ${locator}`);
    }
  }
  
  async stop() {
    if (this.driver) {
      try {
        await this.driver.quit();
        this.driver = null;
      } catch (e) {
        console.error('关闭浏览器失败:', e);
      }
    }
  }
}

module.exports = TestExecutor;
