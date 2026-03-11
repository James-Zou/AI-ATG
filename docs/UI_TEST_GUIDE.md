# UI自动化测试使用指南

## 简介

AI-ATG平台现已集成Selenium WebDriver，支持真实的UI自动化测试。系统会自动启动浏览器，执行定义的测试步骤，并生成测试报告和截图。

## 功能特性

- ✅ 支持Chrome、Firefox等主流浏览器
- ✅ 自动下载和管理WebDriver
- ✅ 支持无头模式运行
- ✅ 自动截图保存
- ✅ 详细的执行日志
- ✅ 多种元素定位方式
- ✅ 断言验证功能

## 测试步骤格式

测试用例的步骤（steps字段）使用JSON数组格式，每个步骤包含以下字段：

```json
{
  "action": "操作类型",
  "locator": "定位器类型",
  "value": "定位器值",
  "input": "输入内容（可选）",
  "timeout": 10
}
```

### 字段说明

| 字段 | 说明 | 必填 | 示例 |
|------|------|------|------|
| action | 操作类型 | 是 | click, input, open等 |
| locator | 元素定位器类型 | 部分操作需要 | id, xpath, css等 |
| value | 定位器的值 | 部分操作需要 | #username, //button[@id='submit'] |
| input | 需要输入的内容 | 部分操作需要 | admin@123 |
| timeout | 超时时间（秒） | 否 | 10 |

## 支持的操作类型

### 1. 导航操作

#### open / navigate
打开指定URL

```json
{
  "action": "open",
  "input": "https://www.example.com"
}
```

### 2. 元素交互

#### click
点击元素

```json
{
  "action": "click",
  "locator": "id",
  "value": "loginButton"
}
```

#### input / sendkeys
输入文本（会先清空原有内容）

```json
{
  "action": "input",
  "locator": "id",
  "value": "username",
  "input": "admin"
}
```

#### select
选择下拉框选项

```json
{
  "action": "select",
  "locator": "id",
  "value": "country",
  "input": "中国"
}
```

### 3. 等待操作

#### wait
等待指定秒数

```json
{
  "action": "wait",
  "timeout": 3
}
```

### 4. 断言验证

#### assertText / verify
验证元素文本包含指定内容

```json
{
  "action": "assertText",
  "locator": "xpath",
  "value": "//div[@class='message']",
  "input": "登录成功"
}
```

#### assertTitle
验证页面标题包含指定内容

```json
{
  "action": "assertTitle",
  "input": "首页"
}
```

#### assertUrl
验证当前URL包含指定内容

```json
{
  "action": "assertUrl",
  "input": "/dashboard"
}
```

## 元素定位方式

| 定位器 | 说明 | 示例 |
|--------|------|------|
| id | 通过元素ID定位 | `{"locator": "id", "value": "username"}` |
| name | 通过name属性定位 | `{"locator": "name", "value": "email"}` |
| xpath | 通过XPath定位 | `{"locator": "xpath", "value": "//button[@type='submit']"}` |
| css | 通过CSS选择器定位 | `{"locator": "css", "value": "#login-form .submit-btn"}` |
| className | 通过class名定位 | `{"locator": "className", "value": "btn-primary"}` |
| linkText | 通过链接文本定位 | `{"locator": "linkText", "value": "立即注册"}` |
| partialLinkText | 通过部分链接文本定位 | `{"locator": "partialLinkText", "value": "注册"}` |
| tagName | 通过标签名定位 | `{"locator": "tagName", "value": "button"}` |

## 完整示例

### 示例1：用户登录测试

```json
[
  {
    "action": "open",
    "input": "http://localhost:19080/login"
  },
  {
    "action": "input",
    "locator": "id",
    "value": "username",
    "input": "admin"
  },
  {
    "action": "input",
    "locator": "id",
    "value": "password",
    "input": "admin123"
  },
  {
    "action": "click",
    "locator": "css",
    "value": "button[type='submit']"
  },
  {
    "action": "wait",
    "timeout": 2
  },
  {
    "action": "assertUrl",
    "input": "/dashboard"
  },
  {
    "action": "assertText",
    "locator": "css",
    "value": ".welcome-message",
    "input": "欢迎"
  }
]
```

### 示例2：表单填写测试

```json
[
  {
    "action": "open",
    "input": "http://localhost:19080/register"
  },
  {
    "action": "input",
    "locator": "name",
    "value": "username",
    "input": "newuser"
  },
  {
    "action": "input",
    "locator": "name",
    "value": "email",
    "input": "user@example.com"
  },
  {
    "action": "input",
    "locator": "name",
    "value": "password",
    "input": "password123"
  },
  {
    "action": "select",
    "locator": "id",
    "value": "role",
    "input": "测试人员"
  },
  {
    "action": "click",
    "locator": "xpath",
    "value": "//button[text()='注册']"
  },
  {
    "action": "wait",
    "timeout": 2
  },
  {
    "action": "assertText",
    "locator": "css",
    "value": ".success-message",
    "input": "注册成功"
  }
]
```

### 示例3：搜索功能测试

```json
[
  {
    "action": "open",
    "input": "http://localhost:19080"
  },
  {
    "action": "input",
    "locator": "css",
    "value": "input[type='search']",
    "input": "测试用例"
  },
  {
    "action": "click",
    "locator": "css",
    "value": ".search-button"
  },
  {
    "action": "wait",
    "timeout": 2
  },
  {
    "action": "assertText",
    "locator": "css",
    "value": ".result-count",
    "input": "找到"
  }
]
```

## 配置说明

在 `application.yml` 中可以配置Selenium相关参数：

```yaml
selenium:
  browser: chrome              # 浏览器类型：chrome, firefox
  headless: true              # 是否无头模式
  implicit-wait: 10           # 隐式等待时间（秒）
  page-load-timeout: 30       # 页面加载超时（秒）
  script-timeout: 30          # 脚本执行超时（秒）
  screenshot-path: /tmp/screenshots  # 截图保存路径
  auto-download-driver: true  # 是否自动下载WebDriver
```

## 最佳实践

### 1. 合理使用等待
- 使用适当的 `timeout` 值
- 在页面跳转后添加 `wait` 操作
- 避免过长的等待时间

### 2. 元素定位
- 优先使用 `id` 定位（最快最稳定）
- 其次使用 `css` 选择器
- 尽量避免复杂的 `xpath`
- 确保定位器的唯一性

### 3. 断言验证
- 每个关键步骤后添加断言
- 验证页面跳转
- 验证关键文本或状态

### 4. 错误处理
- 系统会自动截图保存失败场景
- 查看执行日志了解失败原因
- 适当增加超时时间

## 常见问题

### Q: 浏览器无法启动？
A: 
1. 检查 `auto-download-driver` 是否为 `true`
2. 确保服务器可以访问互联网（下载WebDriver）
3. 如果是Linux服务器，可能需要安装浏览器

### Q: 元素定位失败？
A:
1. 检查定位器是否正确
2. 增加 `timeout` 值
3. 确认元素在页面加载完成后才出现
4. 使用浏览器开发者工具验证定位器

### Q: 无头模式下测试失败？
A:
1. 尝试设置 `headless: false` 查看浏览器实际执行情况
2. 某些网站可能检测无头模式，需要添加额外配置
3. 检查页面渲染是否依赖JavaScript

### Q: 如何查看测试执行过程？
A:
1. 设置 `headless: false` 可以看到浏览器操作
2. 查看执行日志了解每步执行情况
3. 查看截图了解失败时的页面状态

## 技术架构

```
UiTestExecutor
  ↓
WebDriverFactory (创建和管理WebDriver)
  ↓
UiActionExecutor (执行具体的UI操作)
  ↓
Selenium WebDriver (实际的浏览器控制)
```

## 扩展开发

如需添加新的操作类型，可以在 `UiActionExecutor.java` 的 `executeAction` 方法中添加新的 case 分支。

示例：添加双击操作

```java
case "doubleclick":
    if (element != null) {
        Actions actions = new Actions(driver);
        actions.doubleClick(element).perform();
    }
    break;
```

## 总结

通过本指南，您可以创建强大的UI自动化测试用例。系统会自动执行所有步骤，生成详细的测试报告和截图，帮助您快速发现和定位问题。
