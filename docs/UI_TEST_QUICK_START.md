# UI自动化测试快速开始指南

## 5分钟快速体验

### 步骤1: 导入示例测试用例 (1分钟)

执行SQL脚本创建示例测试用例：

```bash
cd /Users/roderickzou/Desktop/AI-ATG
mysql -u root -p ai_atg < docs/sample_ui_test_case.sql
```

输入密码后，会创建3个示例UI测试用例。

### 步骤2: 重启后端服务 (1分钟)

确保新的依赖和配置生效：

```bash
cd backend
mvn clean package
mvn spring-boot:run
```

或者在IDE中重启应用。

### 步骤3: 创建测试执行 (1分钟)

1. 打开前端页面: http://localhost:3000
2. 登录系统（admin / Admin@123）
3. 进入"测试用例"页面
4. 找到"UI测试-百度搜索功能"用例
5. 点击"执行"按钮
6. 选择测试环境（如: dev）
7. 点击"开始执行"

### 步骤4: 查看执行结果 (2分钟)

1. 进入"测试执行"页面
2. 找到刚才创建的执行记录
3. 查看执行状态（执行中 → 已完成）
4. 点击"查看详情"
5. 查看：
   - ✅ 执行状态（通过/失败）
   - 📋 详细日志
   - 📷 截图
   - ⏱️ 执行时间

## 创建自己的UI测试用例

### 示例：测试登录功能

#### 1. 创建测试用例

在"测试用例"页面点击"新建用例"：

- **标题**: 用户登录测试
- **类型**: UI测试
- **优先级**: P1
- **前置条件**: 系统已部署，测试账号已创建
- **测试步骤**: 

```json
[
  {
    "action": "open",
    "input": "http://localhost:3000/login"
  },
  {
    "action": "wait",
    "timeout": 2
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
    "input": "Admin@123"
  },
  {
    "action": "click",
    "locator": "css",
    "value": "button[type='submit']"
  },
  {
    "action": "wait",
    "timeout": 3
  },
  {
    "action": "assertUrl",
    "input": "/dashboard"
  }
]
```

- **预期结果**: 成功登录并跳转到首页

#### 2. 调试技巧

如果测试失败，可以：

1. **查看截图**: 了解失败时的页面状态
2. **查看日志**: 了解哪一步失败
3. **调整定位器**: 
   - 打开浏览器F12
   - 找到目标元素
   - 复制正确的id/css/xpath
4. **增加等待时间**: 如果页面加载慢
5. **设置无头模式为false**: 可以看到浏览器实际操作

修改 `application.yml`:
```yaml
selenium:
  headless: false  # 可以看到浏览器
```

#### 3. 获取元素定位器

**方法1: 使用Chrome DevTools**

1. 打开页面 F12
2. 点击左上角选择元素工具
3. 点击要定位的元素
4. 在Elements面板中右键元素
5. Copy → Copy selector (CSS选择器)
6. Copy → Copy XPath (XPath路径)

**方法2: 使用ID（推荐）**

查看元素的id属性：
```html
<input id="username" type="text">
```

使用：
```json
{
  "locator": "id",
  "value": "username"
}
```

**方法3: 使用CSS选择器**

```html
<button class="btn btn-primary" type="submit">登录</button>
```

使用：
```json
{
  "locator": "css",
  "value": "button[type='submit']"
}
```

或：
```json
{
  "locator": "css",
  "value": ".btn-primary"
}
```

## 常见场景示例

### 场景1: 表单填写

```json
[
  {
    "action": "open",
    "input": "http://example.com/form"
  },
  {
    "action": "input",
    "locator": "id",
    "value": "name",
    "input": "张三"
  },
  {
    "action": "input",
    "locator": "id",
    "value": "email",
    "input": "zhangsan@example.com"
  },
  {
    "action": "input",
    "locator": "id",
    "value": "phone",
    "input": "13800138000"
  },
  {
    "action": "select",
    "locator": "id",
    "value": "city",
    "input": "北京"
  },
  {
    "action": "click",
    "locator": "css",
    "value": "button[type='submit']"
  }
]
```

### 场景2: 搜索功能

```json
[
  {
    "action": "open",
    "input": "http://example.com"
  },
  {
    "action": "input",
    "locator": "css",
    "value": "input[type='search']",
    "input": "测试关键词"
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
    "value": ".result-title",
    "input": "测试关键词"
  }
]
```

### 场景3: 导航测试

```json
[
  {
    "action": "open",
    "input": "http://example.com"
  },
  {
    "action": "click",
    "locator": "linkText",
    "value": "产品中心"
  },
  {
    "action": "wait",
    "timeout": 2
  },
  {
    "action": "assertUrl",
    "input": "/products"
  },
  {
    "action": "assertTitle",
    "input": "产品中心"
  }
]
```

## 配置建议

### 本地开发环境

编辑 `backend/src/main/resources/application.yml`:

```yaml
selenium:
  browser: chrome
  headless: false      # 可以看到浏览器操作，方便调试
  implicit-wait: 10
  page-load-timeout: 30
  script-timeout: 30
  screenshot-path: ./screenshots  # 本地路径
  auto-download-driver: true
```

### Linux服务器环境

```yaml
selenium:
  browser: chrome
  headless: true       # 无头模式，节省资源
  implicit-wait: 10
  page-load-timeout: 30
  script-timeout: 30
  screenshot-path: /var/log/ai-atg/screenshots
  auto-download-driver: true
```

## 故障排查

### ❌ 浏览器无法启动

**检查清单**:
- [ ] Java版本是否为17+
- [ ] 是否有网络连接（首次需要下载驱动）
- [ ] Linux服务器是否安装了Chrome/Firefox

**Linux安装Chrome**:
```bash
# CentOS/RHEL
sudo yum install -y chromium chromium-driver

# Ubuntu/Debian
sudo apt-get install -y chromium-browser chromium-chromedriver
```

### ❌ 元素找不到

**检查清单**:
- [ ] 定位器是否正确（使用F12验证）
- [ ] 页面是否完全加载（增加wait时间）
- [ ] 元素是否在iframe中（需要切换iframe，当前版本暂不支持）
- [ ] 超时时间是否足够（增加timeout值）

### ❌ 断言失败

**检查清单**:
- [ ] 预期文本是否准确（可能是部分匹配）
- [ ] 页面是否加载完成
- [ ] URL是否跳转正确
- [ ] 查看截图确认实际页面状态

## 下一步

恭喜！您已经掌握了UI自动化测试的基本使用。

**进阶学习**:
1. 📖 阅读 [UI_TEST_GUIDE.md](./UI_TEST_GUIDE.md) 了解更多操作类型
2. 🔧 学习更多元素定位技巧
3. 🎯 创建完整的测试套件
4. 📊 使用测试报告分析功能

**最佳实践**:
- 为关键功能创建UI测试
- 定期执行回归测试
- 结合API测试和UI测试
- 使用AI辅助生成测试用例

## 获取帮助

遇到问题？

1. 📖 查看 [UI_TEST_GUIDE.md](./UI_TEST_GUIDE.md)
2. 📖 查看 [UI_TEST_IMPLEMENTATION_SUMMARY.md](./UI_TEST_IMPLEMENTATION_SUMMARY.md)
3. 🔍 查看执行日志和截图
4. 🐛 检查浏览器控制台错误

Happy Testing! 🚀
