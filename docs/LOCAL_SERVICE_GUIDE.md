# 轻量级自启动服务完整指南

## 概述

轻量级自启动服务是一个运行在用户本地的测试服务程序，特点：

### ✅ 核心优势

1. **一次安装，永久使用**
   - 安装后自动开机启动
   - 无需手动启动任何程序
   - 用户完全无感知

2. **Web页面直接调用**
   - 打开AI-ATG平台
   - 点击"执行测试"
   - 立即在本地浏览器执行
   - 无需任何额外操作

3. **功能完整**
   - 支持所有Selenium操作
   - 可以启动独立浏览器
   - 支持截图和日志
   - 自动上传结果

4. **用户友好**
   - 系统托盘图标显示状态
   - 桌面通知测试进度
   - 轻量级，资源占用低（~50MB内存）
   - 跨平台支持

## 架构设计

```
┌─────────────────────────────────────────────┐
│  用户操作流程                                  │
├─────────────────────────────────────────────┤
│  1. 打开AI-ATG Web页面                        │
│  2. 点击"执行测试"按钮                         │
│  3. ✨ 自动在本地浏览器执行                    │
│  4. 实时看到浏览器操作                         │
│  5. 查看测试报告                               │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│  技术架构                                      │
├─────────────────────────────────────────────┤
│  AI-ATG Web页面                               │
│    ↓ (HTTP请求: localhost:9999)              │
│  本地测试服务 (后台运行)                       │
│    ↓                                          │
│  Selenium WebDriver                          │
│    ↓                                          │
│  Chrome/Firefox 浏览器                        │
│    ↓                                          │
│  测试结果 → 上传到服务器                       │
└─────────────────────────────────────────────┘
```

## 安装指南

### Windows

#### 方式1：自动安装（推荐）

1. 下载安装包：`agent-service-windows.zip`
2. 解压到任意目录
3. 右键 `install.bat` → "以管理员身份运行"
4. 等待安装完成

#### 方式2：手动安装

```cmd
# 1. 安装Node.js 18+
# 下载: https://nodejs.org/

# 2. 安装依赖
cd agent-service
npm install

# 3. 安装为系统服务（管理员权限）
npm run install-service

# 4. 验证
curl http://localhost:9999/health
```

### macOS

#### 方式1：自动安装（推荐）

1. 下载安装包：`agent-service-macos.zip`
2. 解压到任意目录
3. 打开终端，执行：
   ```bash
   cd agent-service
   chmod +x install.sh
   ./install.sh
   ```

#### 方式2：手动安装

```bash
# 1. 安装Node.js
brew install node

# 2. 安装依赖
cd agent-service
npm install

# 3. 安装为LaunchAgent
npm run install-service

# 4. 验证
curl http://localhost:9999/health
```

### Linux

```bash
# 1. 安装Node.js
sudo apt install nodejs npm  # Ubuntu/Debian
# 或
sudo yum install nodejs npm   # CentOS/RHEL

# 2. 安装依赖
cd agent-service
npm install

# 3. 安装为systemd服务
sudo npm run install-service

# 4. 验证
curl http://localhost:9999/health
```

## 使用方法

### 第一次使用

1. 安装服务（上面的步骤）
2. 重启电脑（或手动启动服务）
3. 打开AI-ATG平台
4. 创建UI测试用例
5. 点击"执行测试"
6. ✨ 浏览器自动打开并执行测试

### 日常使用

1. 打开AI-ATG平台
2. 点击"执行测试"
3. 完成！

**无需任何额外操作！**

## Web页面集成

### 前端代码示例

```vue
<template>
  <div>
    <!-- 服务状态检查 -->
    <LocalServiceChecker @service-status="onServiceStatus" />
    
    <!-- 执行测试按钮 -->
    <el-button
      type="primary"
      :disabled="!serviceRunning"
      @click="executeTest"
    >
      执行测试
    </el-button>
  </div>
</template>

<script>
import LocalServiceChecker from '@/components/LocalServiceChecker.vue';
import LocalTestService from '@/utils/localTestService';

export default {
  components: { LocalServiceChecker },
  data() {
    return {
      serviceRunning: false,
      testCase: {
        id: 1,
        title: '登录测试',
        executionId: 123,
        steps: [...]
      }
    };
  },
  methods: {
    onServiceStatus(running) {
      this.serviceRunning = running;
    },
    
    async executeTest() {
      try {
        const result = await LocalTestService.executeTest(this.testCase);
        
        if (result.success) {
          this.$message.success('测试已开始执行');
          // 轮询查询测试状态
          this.pollTestStatus();
        } else {
          this.$message.error(result.error);
        }
      } catch (error) {
        this.$message.error('执行失败：' + error.message);
      }
    },
    
    async pollTestStatus() {
      const timer = setInterval(async () => {
        const status = await LocalTestService.getStatus();
        
        if (!status.executing) {
          clearInterval(timer);
          // 刷新测试结果
          this.fetchTestResult();
        }
      }, 2000);
    }
  }
};
</script>
```

### JavaScript集成

```javascript
// 1. 导入工具类
import LocalTestService from '@/utils/localTestService';

// 2. 检查服务状态
const isRunning = await LocalTestService.isRunning();

if (!isRunning) {
  // 显示安装提示
  showInstallDialog();
  return;
}

// 3. 执行测试
const testCase = {
  id: 1,
  title: '登录测试',
  executionId: 123,
  steps: [
    { action: 'open', input: 'http://example.com' },
    { action: 'input', locator: 'id', value: 'username', input: 'admin' },
    { action: 'click', locator: 'id', value: 'submit' }
  ]
};

const result = await LocalTestService.executeTest(testCase);

if (result.success) {
  notification.success('测试执行成功');
}
```

## API文档

### GET /health

**功能**: 健康检查

**响应**:
```json
{
  "status": "ok",
  "version": "1.0.0",
  "config": {
    "browser": "chrome",
    "headless": false
  },
  "executing": false
}
```

### POST /execute

**功能**: 执行测试

**请求体**:
```json
{
  "testCase": {
    "id": 1,
    "title": "登录测试",
    "executionId": 123,
    "steps": [
      {
        "action": "open",
        "input": "http://example.com"
      },
      {
        "action": "input",
        "locator": "id",
        "value": "username",
        "input": "admin"
      }
    ]
  }
}
```

**响应**:
```json
{
  "success": true,
  "message": "测试已开始执行"
}
```

### GET /status

**功能**: 获取执行状态

**响应**:
```json
{
  "executing": true,
  "current": {
    "testCaseId": 1,
    "testCaseTitle": "登录测试",
    "startTime": 1706414400000
  }
}
```

### POST /stop

**功能**: 停止当前测试

**响应**:
```json
{
  "success": true,
  "message": "测试已停止"
}
```

### GET /config

**功能**: 获取配置

**响应**:
```json
{
  "serverUrl": "http://localhost:19080",
  "browser": "chrome",
  "headless": false,
  "autoStart": true
}
```

### POST /config

**功能**: 更新配置

**请求体**:
```json
{
  "browser": "firefox",
  "headless": true
}
```

## 配置说明

### 配置文件位置

- **Windows**: `C:\Users\<用户名>\.ai-atg-service\config.json`
- **macOS**: `~/.ai-atg-service/config.json`
- **Linux**: `~/.ai-atg-service/config.json`

### 配置项说明

```json
{
  "serverUrl": "http://localhost:19080",  // 服务器地址
  "browser": "chrome",                    // 浏览器: chrome/firefox
  "headless": false,                      // 无头模式
  "autoStart": true                       // 开机自启动
}
```

## 系统托盘

服务运行时会在系统托盘显示图标（AI-ATG logo）

### 托盘菜单

右键托盘图标：

- **AI-ATG 测试服务** - 显示状态
- **打开配置** - 查看当前配置
- **访问控制台** - 打开浏览器管理界面
- **退出** - 停止服务

### 状态指示

- 🟢 绿色：服务正常运行
- 🟡 黄色：正在执行测试
- 🔴 红色：服务异常

## 桌面通知

服务会发送桌面通知：

### 启动通知
```
标题: AI-ATG 测试服务
内容: 服务已启动，可以开始执行UI测试
```

### 测试开始
```
标题: AI-ATG 测试服务
内容: 开始执行测试：登录功能测试
```

### 测试完成
```
标题: AI-ATG 测试服务
内容: 测试完成：登录功能测试 - ✓ 通过
```

## 日志文件

### 日志位置

- **Windows**: `%USERPROFILE%\.ai-atg-service\logs\service.log`
- **macOS**: `~/Library/Logs/ai-atg-service.log`
- **Linux**: `/var/log/ai-atg-service.log`

### 查看日志

```bash
# Windows
type %USERPROFILE%\.ai-atg-service\logs\service.log

# macOS/Linux
tail -f ~/Library/Logs/ai-atg-service.log
```

## 故障排查

### 问题1: 服务未启动

**现象**: Web页面显示"本地测试服务未运行"

**解决方案**:

1. 检查托盘图标是否存在
2. 访问 http://localhost:9999/health
3. 查看服务状态：

```bash
# Windows
sc query "AI-ATG-Test-Service"

# macOS
launchctl list | grep aiatg

# Linux
systemctl status ai-atg-service
```

4. 手动启动服务

### 问题2: 端口被占用

**现象**: 服务启动失败，日志显示"端口9999已被占用"

**解决方案**:

1. 找到占用端口的程序：
```bash
# Windows
netstat -ano | findstr :9999

# macOS/Linux
lsof -i :9999
```

2. 修改配置使用其他端口
3. 或关闭占用端口的程序

### 问题3: 浏览器启动失败

**现象**: 测试执行失败，日志显示"WebDriver error"

**解决方案**:

1. 确认已安装Chrome或Firefox
2. 更新浏览器到最新版本
3. 检查WebDriver是否正确

```bash
# Chrome
chromedriver --version

# Firefox
geckodriver --version
```

### 问题4: 权限不足

**现象**: 安装失败，提示权限错误

**解决方案**:

- **Windows**: 以管理员身份运行安装脚本
- **macOS/Linux**: 使用sudo执行安装命令

## 卸载服务

### Windows

```cmd
# 以管理员身份运行
npm run uninstall-service
```

或者：

```cmd
sc stop "AI-ATG-Test-Service"
sc delete "AI-ATG-Test-Service"
```

### macOS

```bash
npm run uninstall-service
```

或者：

```bash
launchctl unload ~/Library/LaunchAgents/com.aiatg.testservice.plist
rm ~/Library/LaunchAgents/com.aiatg.testservice.plist
```

### Linux

```bash
sudo npm run uninstall-service
```

或者：

```bash
sudo systemctl stop ai-atg-service
sudo systemctl disable ai-atg-service
sudo rm /etc/systemd/system/ai-atg-service.service
sudo systemctl daemon-reload
```

## 性能对比

| 特性 | Java Agent | 浏览器扩展 | 轻量级服务 |
|------|-----------|-----------|-----------|
| 需要手动启动 | ❌ 是 | ✅ 否 | ✅ 否 |
| 开机自启动 | ❌ 否 | ✅ 是 | ✅ 是 |
| 内存占用 | ~200MB | ~20MB | ~50MB |
| 功能完整性 | ✅ 完整 | ⚠️ 受限 | ✅ 完整 |
| 独立浏览器 | ✅ 是 | ❌ 否 | ✅ 是 |
| 系统托盘 | ❌ 否 | ❌ 否 | ✅ 是 |
| 桌面通知 | ❌ 否 | ⚠️ 有限 | ✅ 是 |
| 用户体验 | 一般 | 好 | ✅ 优秀 |

## 最佳实践

### 1. 开发环境

```json
{
  "browser": "chrome",
  "headless": false,  // 可以看到浏览器操作
  "serverUrl": "http://localhost:8080"
}
```

### 2. 测试环境

```json
{
  "browser": "chrome",
  "headless": true,   // 后台运行，节省资源
  "serverUrl": "http://test-server:19080"
}
```

### 3. 企业部署

1. 打包为可执行文件（含依赖）
2. 制作安装程序（NSIS/Electron Builder）
3. 企业内网分发
4. 组策略自动安装

## 安全建议

1. **限制访问**
   - 服务只监听 127.0.0.1（本地）
   - 不对外网开放

2. **权限控制**
   - 以普通用户权限运行
   - 仅安装时需要管理员权限

3. **数据安全**
   - 测试数据只在本地处理
   - 结果上传使用HTTPS

## 总结

轻量级自启动服务提供了**最佳的用户体验**：

✅ **无感安装** - 一次安装，永久使用  
✅ **自动启动** - 开机即用，无需手动操作  
✅ **Web直调** - 在页面直接点击执行  
✅ **功能完整** - 支持所有Selenium操作  
✅ **资源友好** - 内存占用低，性能优秀  
✅ **用户友好** - 托盘图标，桌面通知

**这是推荐的UI自动化测试执行方案！** 🎉
