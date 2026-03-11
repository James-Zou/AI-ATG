# 浏览器扩展解决方案

## 概述

通过开发Chrome/Firefox扩展，测试人员只需：
1. 安装扩展（一次性）
2. 在Web页面点击"执行测试"
3. 扩展自动在当前浏览器执行测试

**无需单独的Java Agent程序！**

## 架构设计

```
AI-ATG Web页面
    ↓ (点击"执行测试")
浏览器扩展（后台脚本）
    ↓
当前浏览器标签页
    ↓ (执行UI操作)
Web页面 → API服务器 → 保存结果
```

## 工作流程

### 1. 用户操作
```
1. 测试人员打开AI-ATG Web页面
2. 创建UI测试用例
3. 点击"执行测试"按钮
4. 扩展收到消息
5. 扩展打开新标签页
6. 在新标签页执行测试步骤
7. 自动截图
8. 上传结果到服务器
9. 在原页面显示测试结果
```

### 2. 技术实现

#### Chrome扩展结构

```
ai-atg-extension/
├── manifest.json          # 扩展配置
├── background.js          # 后台脚本（核心逻辑）
├── content.js             # 内容脚本（注入到页面）
├── popup.html             # 扩展弹窗
├── popup.js               # 弹窗逻辑
└── icons/                 # 图标
    ├── icon16.png
    ├── icon48.png
    └── icon128.png
```

#### manifest.json
```json
{
  "manifest_version": 3,
  "name": "AI-ATG Test Runner",
  "version": "1.0.0",
  "description": "AI-ATG UI自动化测试执行器",
  "permissions": [
    "tabs",
    "storage",
    "scripting",
    "activeTab"
  ],
  "host_permissions": [
    "http://localhost:19080/*",
    "http://*/*",
    "https://*/*"
  ],
  "background": {
    "service_worker": "background.js"
  },
  "content_scripts": [
    {
      "matches": ["http://localhost:3000/*"],
      "js": ["content.js"]
    }
  ],
  "action": {
    "default_popup": "popup.html",
    "default_icon": {
      "16": "icons/icon16.png",
      "48": "icons/icon48.png",
      "128": "icons/icon128.png"
    }
  }
}
```

#### background.js (核心逻辑)
```javascript
// 监听来自Web页面的消息
chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
  if (request.action === 'executeTest') {
    executeUITest(request.testCase)
      .then(result => sendResponse({ success: true, result }))
      .catch(error => sendResponse({ success: false, error: error.message }));
    return true; // 异步响应
  }
});

// 执行UI测试
async function executeUITest(testCase) {
  const { steps, testCaseId, executionId } = testCase;
  
  // 1. 创建新标签页
  const tab = await chrome.tabs.create({ url: 'about:blank' });
  
  // 2. 等待标签页加载
  await waitForTabReady(tab.id);
  
  // 3. 执行测试步骤
  const logs = [];
  let status = 'passed';
  
  try {
    for (let i = 0; i < steps.length; i++) {
      const step = steps[i];
      logs.push(`Step ${i + 1}: ${step.action}`);
      
      await executeStep(tab.id, step);
      logs.push(`  ✓ Success`);
    }
  } catch (error) {
    status = 'failed';
    logs.push(`  ✗ Failed: ${error.message}`);
  }
  
  // 4. 截图
  const screenshot = await chrome.tabs.captureVisibleTab(tab.windowId, {
    format: 'png'
  });
  
  // 5. 关闭标签页
  await chrome.tabs.remove(tab.id);
  
  // 6. 上传结果
  await uploadResult({
    executionId,
    testCaseId,
    status,
    logs: logs.join('\n'),
    screenshot
  });
  
  return { status, logs };
}

// 执行单个步骤
async function executeStep(tabId, step) {
  const { action, locator, value, input } = step;
  
  switch (action) {
    case 'open':
    case 'navigate':
      await chrome.tabs.update(tabId, { url: input || value });
      await waitForTabReady(tabId);
      break;
      
    case 'click':
      await chrome.scripting.executeScript({
        target: { tabId },
        func: clickElement,
        args: [locator, value]
      });
      break;
      
    case 'input':
      await chrome.scripting.executeScript({
        target: { tabId },
        func: inputText,
        args: [locator, value, input]
      });
      break;
      
    case 'assertText':
      const result = await chrome.scripting.executeScript({
        target: { tabId },
        func: verifyText,
        args: [locator, value, input]
      });
      if (!result[0].result) {
        throw new Error(`Text assertion failed: expected "${input}"`);
      }
      break;
      
    case 'wait':
      await sleep(step.timeout * 1000);
      break;
  }
}

// 在页面中执行的函数
function clickElement(locator, value) {
  const element = findElement(locator, value);
  if (element) {
    element.click();
    return true;
  }
  throw new Error(`Element not found: ${locator}=${value}`);
}

function inputText(locator, value, text) {
  const element = findElement(locator, value);
  if (element) {
    element.value = text;
    element.dispatchEvent(new Event('input', { bubbles: true }));
    return true;
  }
  throw new Error(`Element not found: ${locator}=${value}`);
}

function verifyText(locator, value, expectedText) {
  const element = findElement(locator, value);
  if (element) {
    const actualText = element.textContent || element.value;
    return actualText.includes(expectedText);
  }
  throw new Error(`Element not found: ${locator}=${value}`);
}

function findElement(locator, valu[UI_TEST_GUIDE.md](UI_TEST_GUIDE.md)e) {
  switch (locator) {
    case 'id':
      return document.getElementById(value);
    case 'css':
      return document.querySelector(value);
    case 'xpath':
      return document.evaluate(value, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
    case 'name':
      return document.querySelector(`[name="${value}"]`);
    default:
      return null;
  }
}

// 上传结果到服务器
async function uploadResult(result) {
  const response = await fetch('http://localhost:19080/api/test/result', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(result)
  });
  return response.json();
}

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

function waitForTabReady(tabId) {
  return new Promise((resolve) => {
    chrome.tabs.onUpdated.addListener(function listener(tabIdUpdated, info) {
      if (tabIdUpdated === tabId && info.status === 'complete') {
        chrome.tabs.onUpdated.removeListener(listener);
        resolve();
      }
    });
  });
}
```

#### content.js (Web页面集成)
```javascript
// 监听Web页面的按钮点击
document.addEventListener('click', (e) => {
  if (e.target.classList.contains('execute-test-btn')) {
    e.preventDefault();
    
    // 获取测试用例数据
    const testCase = JSON.parse(e.target.dataset.testcase);
    
    // 发送消息给扩展
    chrome.runtime.sendMessage({
      action: 'executeTest',
      testCase
    }, (response) => {
      if (response.success) {
        showSuccess('测试执行成功！');
        refreshTestResults();
      } else {
        showError('测试执行失败：' + response.error);
      }
    });
  }
});

function showSuccess(message) {
  // 显示成功提示
  alert(message);
}

function showError(message) {
  // 显示错误提示
  alert(message);
}

function refreshTestResults() {
  // 刷新测试结果列表
  window.location.reload();
}
```

## 优势

### ✅ 用户体验
- 只需安装扩展（一次性）
- 无需单独启动Agent程序
- 在Web页面直接点击执行
- 立即看到测试结果

### ✅ 技术实现
- 无需单独的Java程序
- 无需WebDriver
- 纯JavaScript实现
- 跨平台（Windows/Mac/Linux）

### ✅ 部署简单
- 从Chrome应用商店安装
- 或者内网分发crx文件
- 自动更新

## 限制

### ⚠️ 功能限制
- 只能在当前浏览器执行
- 无法启动独立的浏览器实例
- 受浏览器扩展API限制
- 无法执行复杂的浏览器操作（如拖拽）

### ⚠️ 兼容性
- 需要为Chrome和Firefox分别开发
- Safari扩展API不同

### ⚠️ 安全性
- 需要请求较高的权限
- 需要用户信任

## 前端集成

### Web页面代码

```javascript
// 检查扩展是否已安装
function checkExtensionInstalled() {
  return new Promise((resolve) => {
    chrome.runtime.sendMessage(
      EXTENSION_ID,
      { action: 'ping' },
      (response) => {
        resolve(!!response);
      }
    );
  });
}

// 执行测试按钮
async function handleExecuteTest(testCase) {
  // 检查扩展
  const installed = await checkExtensionInstalled();
  
  if (!installed) {
    showInstallExtensionDialog();
    return;
  }
  
  // 发送测试任务
  chrome.runtime.sendMessage({
    action: 'executeTest',
    testCase: {
      testCaseId: testCase.id,
      executionId: execution.id,
      steps: JSON.parse(testCase.steps)
    }
  }, (response) => {
    if (response.success) {
      notification.success('测试执行成功');
      refreshResults();
    } else {
      notification.error('测试执行失败：' + response.error);
    }
  });
}

function showInstallExtensionDialog() {
  Modal.confirm({
    title: '需要安装浏览器扩展',
    content: '执行UI测试需要安装AI-ATG浏览器扩展，是否立即下载？',
    onOk: () => {
      window.open('http://localhost:19080/downloads/extension');
    }
  });
}
```

## 部署流程

### 1. 开发扩展
```bash
cd extension
npm install
npm run build
```

### 2. 打包扩展
```bash
# Chrome
zip -r ai-atg-extension.zip dist/

# Firefox
cd dist
zip -r ../ai-atg-extension.xpi *
```

### 3. 分发扩展

#### 方式1：Chrome应用商店
1. 注册开发者账号
2. 上传扩展
3. 等待审核
4. 发布

#### 方式2：企业内网分发
1. 将crx文件放到下载中心
2. 用户下载安装
3. 或者通过企业策略推送

### 4. 用户安装

#### Chrome
1. 访问 chrome://extensions/
2. 开启"开发者模式"
3. 拖拽crx文件到页面
4. 点击"添加扩展程序"

#### Firefox
1. 访问 about:addons
2. 点击齿轮图标
3. 选择"从文件安装附加组件"
4. 选择xpi文件

## 对比总结

| 特性 | Java Agent | 浏览器扩展 |
|------|-----------|-----------|
| 需要单独程序 | ❌ 需要 | ✅ 不需要 |
| 需要手动启动 | ❌ 需要 | ✅ 不需要 |
| 安装步骤 | 复杂 | ✅ 简单 |
| 用户体验 | 一般 | ✅ 优秀 |
| 功能完整性 | ✅ 完整 | ⚠️ 受限 |
| 独立浏览器 | ✅ 支持 | ❌ 不支持 |
| 复杂操作 | ✅ 支持 | ⚠️ 受限 |
| 跨浏览器 | ✅ 统一 | ❌ 需分别开发 |

## 建议

### 混合方案（推荐）

提供两种模式：

1. **浏览器扩展模式**（适合简单测试）
   - 快速执行
   - 无需额外程序
   - 适合日常测试

2. **Java Agent模式**（适合复杂测试）
   - 功能完整
   - 独立浏览器
   - 适合完整测试套件

用户可以根据需求选择：

```javascript
// 前端代码
if (testCase.complexity === 'simple' && extensionInstalled) {
  // 使用浏览器扩展
  executeViaExtension(testCase);
} else {
  // 使用Java Agent
  executeViaAgent(testCase);
}
```

## 下一步

您希望：
1. ✅ 开发浏览器扩展方案？
2. ✅ 保留Java Agent作为高级选项？
3. ✅ 两种方案都支持？

请告诉我您的选择，我将继续实现相应的方案。
