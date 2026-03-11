# AI-ATG UI自动化测试完整指南

## 目录

1. [快速开始](#快速开始)
2. [安装本地测试服务](#安装本地测试服务)
3. [创建测试用例](#创建测试用例)
4. [执行测试](#执行测试)
5. [查看测试报告](#查看测试报告)
6. [故障排查](#故障排查)
7. [最佳实践](#最佳实践)

---

## 快速开始

### 5分钟快速体验

#### 步骤1：安装本地测试服务（一次性）

1. 在AI-ATG平台点击 **下载中心**
2. 下载对应系统的安装包：
   - Windows: `agent-service-windows.zip`
   - macOS: `agent-service-macos.zip`
   - Linux: `agent-service-linux.tar.gz`
3. 解压到任意目录
4. 运行安装脚本：
   - Windows: 右键 `install.bat` → "以管理员身份运行"
   - Mac/Linux: `chmod +x install.sh && ./install.sh`
5. 等待安装完成（约1-2分钟）

✅ **安装完成！**服务会自动启动并开机自启，无需任何手动操作。

#### 步骤2：创建测试用例

1. 登录AI-ATG平台
2. 进入 **测试用例** → **新建用例**
3. 填写基本信息：
   - 标题：登录功能测试
   - 类型：UI测试
4. 编写测试步骤（JSON格式）：

```json
[
  {
    "action": "open",
    "input": "http://localhost:3000/login"
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
    "action": "assertUrl",
    "input": "/dashboard"
  }
]
```

5. 保存用例

#### 步骤3：执行测试

1. 在测试用例列表找到刚创建的用例
2. 点击 **执行** 按钮
3. ✨ 系统自动在您的浏览器执行测试
4. 可以实时看到浏览器操作

#### 步骤4：查看结果

1. 进入 **测试执行** 页面
2. 找到执行记录
3. 点击 **查看详情**
4. 查看：
   - ✅ 执行状态
   - 📋 详细日志
   - 📷 截图
   - ⏱️ 执行时间

---

## 安装本地测试服务

### 为什么需要本地服务？

本地测试服务是一个轻量级程序，运行在您的电脑上，负责：
- 接收来自Web平台的测试任务
- 在本地浏览器执行UI自动化操作
- 截图并上传测试结果

### 特点

- ✅ **一次安装，永久使用** - 安装后自动开机启动
- ✅ **后台运行** - 用户完全无感知
- ✅ **Web直接调用** - 在平台页面直接点击执行
- ✅ **系统托盘** - 显示运行状态
- ✅ **自动下载驱动** - 无需手动配置WebDriver

### Windows安装

#### 系统要求
- Windows 10/11
- 管理员权限
- Chrome或Firefox浏览器

#### 安装步骤

1. **下载安装包**
   
   访问平台 **下载中心** → 下载 Windows 版本

2. **解压文件**
   
   解压到任意目录，例如：`C:\ai-atg-service`

3. **运行安装脚本**
   
   右键 `install.bat` → 选择 **"以管理员身份运行"**

4. **等待完成**
   
   安装脚本会自动：
   - 检查系统环境
   - 安装Node.js依赖
   - 注册为Windows系统服务
   - 启动服务

5. **验证安装**
   
   打开浏览器访问：http://localhost:9999/health
   
   应该看到：
   ```json
   {
     "status": "ok",
     "version": "1.0.0"
   }
   ```

#### 服务管理

```cmd
# 启动服务
net start "AI-ATG-Test-Service"

# 停止服务
net stop "AI-ATG-Test-Service"

# 查看状态
sc query "AI-ATG-Test-Service"
```

#### 卸载

```cmd
# 以管理员身份运行
npm run uninstall-service
```

### macOS安装

#### 系统要求
- macOS 10.14+
- Terminal访问权限
- Chrome或Firefox浏览器

#### 安装步骤

1. **下载安装包**
   
   访问平台 **下载中心** → 下载 macOS 版本

2. **解压并进入目录**
   
   ```bash
   unzip agent-service-macos.zip
   cd agent-service
   ```

3. **运行安装脚本**
   
   ```bash
   chmod +x install.sh
   ./install.sh
   ```

4. **验证安装**
   
   ```bash
   curl http://localhost:9999/health
   ```

#### 服务管理

```bash
# 启动服务
launchctl load ~/Library/LaunchAgents/com.aiatg.testservice.plist

# 停止服务
launchctl unload ~/Library/LaunchAgents/com.aiatg.testservice.plist

# 查看日志
tail -f ~/Library/Logs/ai-atg-service.log
```

#### 卸载

```bash
npm run uninstall-service
```

### Linux安装

#### 系统要求
- Ubuntu 18.04+ / CentOS 7+
- sudo权限
- Chrome或Firefox浏览器

#### 安装步骤

1. **安装Node.js**（如果未安装）
   
   ```bash
   # Ubuntu/Debian
   sudo apt update
   sudo apt install nodejs npm
   
   # CentOS/RHEL
   sudo yum install nodejs npm
   ```

2. **下载并安装服务**
   
   ```bash
   wget http://your-server/downloads/agent-service-linux.tar.gz
   tar -xzf agent-service-linux.tar.gz
   cd agent-service
   sudo ./install.sh
   ```

3. **验证安装**
   
   ```bash
   curl http://localhost:9999/health
   ```

#### 服务管理

```bash
# 启动服务
sudo systemctl start ai-atg-service

# 停止服务
sudo systemctl stop ai-atg-service

# 查看状态
sudo systemctl status ai-atg-service

# 查看日志
sudo journalctl -u ai-atg-service -f
```

#### 卸载

```bash
sudo npm run uninstall-service
```

### 配置说明

配置文件位置：
- Windows: `C:\Users\<用户名>\.ai-atg-service\config.json`
- macOS: `~/.ai-atg-service/config.json`
- Linux: `~/.ai-atg-service/config.json`

配置示例：
```json
{
  "serverUrl": "http://localhost:19080",
  "browser": "chrome",
  "headless": false,
  "autoStart": true
}
```

配置项说明：
- `serverUrl`: 服务器地址
- `browser`: 浏览器类型（chrome/firefox）
- `headless`: 无头模式（false=显示浏览器，true=后台运行）
- `autoStart`: 开机自启动

---

## 创建测试用例

### 测试步骤格式

测试步骤使用JSON数组格式，每个步骤是一个对象：

```json
{
  "action": "操作类型",
  "locator": "定位器类型",
  "value": "定位器值",
  "input": "输入内容",
  "timeout": 10
}
```

### 支持的操作类型

#### 1. 导航操作

**open / navigate** - 打开URL

```json
{
  "action": "open",
  "input": "https://www.example.com"
}
```

#### 2. 元素交互

**click** - 点击元素

```json
{
  "action": "click",
  "locator": "id",
  "value": "submit-button"
}
```

**input / sendkeys** - 输入文本

```json
{
  "action": "input",
  "locator": "id",
  "value": "username",
  "input": "admin"
}
```

**select** - 选择下拉框

```json
{
  "action": "select",
  "locator": "id",
  "value": "country",
  "input": "中国"
}
```

#### 3. 等待操作

**wait** - 等待指定秒数

```json
{
  "action": "wait",
  "timeout": 3
}
```

#### 4. 断言验证

**assertText** - 验证文本

```json
{
  "action": "assertText",
  "locator": "css",
  "value": ".message",
  "input": "登录成功"
}
```

**assertTitle** - 验证标题

```json
{
  "action": "assertTitle",
  "input": "首页"
}
```

**assertUrl** - 验证URL

```json
{
  "action": "assertUrl",
  "input": "/dashboard"
}
```

### 元素定位方式

| 定位器 | 说明 | 示例 |
|--------|------|------|
| id | 通过ID定位 | `{"locator": "id", "value": "username"}` |
| name | 通过name属性 | `{"locator": "name", "value": "email"}` |
| css | CSS选择器 | `{"locator": "css", "value": ".btn-primary"}` |
| xpath | XPath路径 | `{"locator": "xpath", "value": "//button[@type='submit']"}` |
| className | 通过class | `{"locator": "className", "value": "submit-btn"}` |
| linkText | 链接文本 | `{"locator": "linkText", "value": "立即注册"}` |

### 获取定位器

#### 使用Chrome DevTools

1. 打开页面，按F12
2. 点击左上角选择元素工具
3. 点击目标元素
4. 在Elements面板右键元素
5. Copy → Copy selector (CSS)
6. Copy → Copy XPath

#### 最佳实践

- **优先使用 id**：最快最稳定
- **其次使用 css**：灵活且性能好
- **避免复杂xpath**：难以维护

### 完整示例

#### 示例1：用户登录

```json
[
  {
    "action": "open",
    "input": "http://localhost:3000/login"
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
    "timeout": 2
  },
  {
    "action": "assertUrl",
    "input": "/dashboard"
  }
]
```

#### 示例2：表单填写

```json
[
  {
    "action": "open",
    "input": "http://localhost:3000/register"
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
    "action": "assertText",
    "locator": "css",
    "value": ".success-message",
    "input": "注册成功"
  }
]
```

---

## 执行测试

### 执行方式

#### 单个执行

1. 在 **测试用例列表** 找到目标用例
2. 点击 **执行** 按钮
3. 选择测试环境
4. 开始执行

#### 批量执行

1. 创建 **测试套件**
2. 添加多个测试用例
3. 执行整个套件

### 执行模式

#### 有头模式（推荐调试时使用）

```json
{
  "headless": false
}
```

**特点**：
- 可以看到浏览器实际操作
- 方便调试和问题定位
- 适合开发和调试

#### 无头模式（推荐自动化执行）

```json
{
  "headless": true
}
```

**特点**：
- 后台运行，不显示浏览器
- 节省资源
- 适合批量执行

### 查看执行过程

在有头模式下，可以实时看到：
- ✅ 浏览器自动打开
- ✅ 自动导航到目标页面
- ✅ 自动填写表单
- ✅ 自动点击按钮
- ✅ 自动验证结果

---

## 查看测试报告

### 测试报告包含

1. **执行状态**
   - ✅ 通过
   - ❌ 失败
   - ⏸️ 跳过

2. **详细日志**
   - 每步操作记录
   - 执行时间
   - 错误信息

3. **截图**
   - 失败时自动截图
   - 显示页面状态

4. **统计数据**
   - 总用例数
   - 通过数
   - 失败数
   - 通过率

### 问题排查

#### 元素定位失败

**症状**：日志显示"Element not found"

**解决方案**：
1. 使用F12验证定位器
2. 检查元素是否在页面加载完成后存在
3. 增加等待时间
4. 尝试其他定位方式

#### 超时

**症状**：执行超时

**解决方案**：
1. 增加 `timeout` 值
2. 添加 `wait` 步骤
3. 检查网络速度

#### 断言失败

**症状**：断言不通过

**解决方案**：
1. 查看截图确认实际页面
2. 检查预期值是否正确
3. 确认页面是否完全加载

---

## 故障排查

### 常见问题

#### Q1: 服务未运行

**现象**：Web页面显示"本地测试服务未运行"

**解决方案**：
1. 检查系统托盘是否有AI-ATG图标
2. 访问 http://localhost:9999/health
3. 查看服务状态
4. 重启服务

#### Q2: 端口被占用

**现象**：服务启动失败，提示端口占用

**解决方案**：
1. 找到占用端口的程序：
   ```bash
   # Windows
   netstat -ano | findstr :9999
   
   # Mac/Linux
   lsof -i :9999
   ```
2. 关闭占用程序
3. 或修改配置使用其他端口

#### Q3: 浏览器启动失败

**现象**：测试失败，日志显示WebDriver错误

**解决方案**：
1. 确认已安装Chrome或Firefox
2. 更新浏览器到最新版本
3. 检查WebDriver版本
4. 查看服务日志

### 日志位置

- **Windows**: `C:\Users\{用户名}\.ai-atg-service\logs\`
- **macOS**: `~/Library/Logs/ai-atg-service.log`
- **Linux**: `/var/log/ai-atg-service.log`

### 获取帮助

1. 查看在线文档
2. 访问帮助中心
3. 联系管理员

---

## 最佳实践

### 1. 合理使用等待

```json
// ✅ 好的做法
[
  {"action": "click", "locator": "id", "value": "submit"},
  {"action": "wait", "timeout": 2},  // 等待页面跳转
  {"action": "assertUrl", "input": "/success"}
]

// ❌ 不好的做法
[
  {"action": "click", "locator": "id", "value": "submit"},
  {"action": "assertUrl", "input": "/success"}  // 可能还未跳转
]
```

### 2. 元素定位优先级

```
id > css > xpath
```

### 3. 合理分组测试用例

- 按功能模块分组
- 使用测试套件管理
- 设置合理的执行顺序

### 4. 定期维护

- 更新失效的定位器
- 调整等待时间
- 删除废弃的测试用例

### 5. 充分利用断言

```json
[
  {"action": "click", "locator": "id", "value": "delete"},
  {"action": "assertText", "locator": "css", "value": ".message", "input": "删除成功"},
  {"action": "assertUrl", "input": "/list"}
]
```

---

## 总结

恭喜！您已经掌握了AI-ATG UI自动化测试的完整流程。

### 关键要点

✅ **一次安装** - 本地服务自动启动，永久可用  
✅ **简单创建** - JSON格式，支持丰富的操作类型  
✅ **一键执行** - Web页面直接点击，自动化运行  
✅ **详细报告** - 日志、截图、统计数据一应俱全

### 下一步

- 创建更多测试用例
- 组织测试套件
- 定期执行回归测试
- 持续优化测试覆盖率

Happy Testing! 🎉
