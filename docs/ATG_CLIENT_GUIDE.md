# ATG-Client 使用指南

## 什么是ATG-Client？

**ATG-Client** 是AI-ATG平台的专业客户端程序，运行在测试人员的本地电脑上，负责执行UI自动化测试。

### 核心特性

- ✅ **一次安装，永久使用** - 安装后自动开机启动
- ✅ **后台运行** - 用户完全无感知
- ✅ **Web直接调用** - 在平台页面直接点击执行
- ✅ **自动下载驱动** - 无需手动配置WebDriver
- ✅ **系统托盘** - 显示运行状态
- ✅ **桌面通知** - 测试开始/完成实时提醒

---

## 快速安装

### Windows

1. 下载 `atg-client-windows.zip`
2. 解压到任意目录（如：`C:\atg-client`）
3. 右键 `install.bat` → 选择"以管理员身份运行"
4. 等待安装完成（约1-2分钟）
5. 验证：访问 http://localhost:9999/health

### macOS

1. 下载 `atg-client-macos.zip`
2. 解压并打开终端
3. 执行安装：
   ```bash
   cd /path/to/atg-client
   chmod +x install.sh
   ./install.sh
   ```
4. 验证：`curl http://localhost:9999/health`

### Linux

1. 下载 `atg-client-linux.tar.gz`
2. 解压并安装：
   ```bash
   tar -xzf atg-client-linux.tar.gz
   cd atg-client
   sudo ./install.sh
   ```
3. 验证：`curl http://localhost:9999/health`

---

## 系统要求

- **Node.js**: 18+ (或使用打包版本，无需Node.js)
- **浏览器**: Chrome 或 Firefox
- **磁盘空间**: 500MB
- **权限**: 管理员权限(安装时)

---

## 服务管理

### Windows

```cmd
# 启动服务
net start ATG-Client

# 停止服务
net stop ATG-Client

# 查看状态
sc query ATG-Client
```

### macOS

```bash
# 启动服务
launchctl load ~/Library/LaunchAgents/com.atgclient.plist

# 停止服务
launchctl unload ~/Library/LaunchAgents/com.atgclient.plist

# 查看日志
tail -f ~/Library/Logs/atg-client.log
```

### Linux

```bash
# 启动服务
sudo systemctl start atg-client

# 停止服务
sudo systemctl stop atg-client

# 查看状态
sudo systemctl status atg-client

# 查看日志
sudo journalctl -u atg-client -f
```

---

## 配置文件

### 配置文件位置

- **Windows**: `C:\Users\{用户名}\.atg-client\config.json`
- **macOS**: `~/.atg-client/config.json`
- **Linux**: `~/.atg-client/config.json`

### 配置示例

```json
{
  "serverUrl": "http://localhost:19080",
  "browser": "chrome",
  "headless": false,
  "autoStart": true
}
```

### 配置项说明

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| serverUrl | string | http://localhost:19080 | AI-ATG平台服务器地址 |
| browser | string | chrome | 浏览器类型(chrome/firefox) |
| headless | boolean | false | 无头模式(true=后台运行) |
| autoStart | boolean | true | 开机自启动 |

---

## 使用流程

### 第一次使用

```
1. 下载ATG-Client安装包
2. 运行安装脚本
3. 等待安装完成(服务自动启动)
4. 打开AI-ATG平台
5. 创建测试用例
6. 点击"执行"按钮
7. 浏览器自动执行测试
8. 查看测试报告
```

### 日常使用

```
1. 打开AI-ATG平台(ATG-Client已在后台运行)
2. 点击"执行测试"
3. 完成！
```

**无需任何手动启动操作！**

---

## API接口

ATG-Client提供HTTP接口供Web平台调用：

### 健康检查

```bash
GET http://localhost:9999/health
```

响应：
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

### 执行测试

```bash
POST http://localhost:9999/execute
Content-Type: application/json

{
  "testCase": {
    "id": 1,
    "title": "登录测试",
    "steps": "[...]",
    "executionId": 123
  }
}
```

### 获取执行状态

```bash
GET http://localhost:9999/status
```

### 停止测试

```bash
POST http://localhost:9999/stop
```

### 获取/更新配置

```bash
# 获取配置
GET http://localhost:9999/config

# 更新配置
POST http://localhost:9999/config
Content-Type: application/json

{
  "browser": "firefox",
  "headless": true
}
```

---

## 故障排查

### Q1: ATG-Client未运行？

**检查方法**：
1. 检查系统托盘是否有ATG-Client图标
2. 访问 http://localhost:9999/health
3. 查看服务状态(见上方"服务管理")

**解决方案**：
- 重启服务
- 重启电脑
- 查看日志文件
- 重新安装

### Q2: 端口被占用？

**现象**: 服务启动失败，提示端口9999被占用

**解决方案**：
1. 找到占用端口的程序：
   ```bash
   # Windows
   netstat -ano | findstr :9999
   
   # Mac/Linux
   lsof -i :9999
   ```
2. 关闭占用程序
3. 或修改配置文件使用其他端口

### Q3: 浏览器启动失败？

**现象**: 测试失败，日志显示WebDriver错误

**解决方案**：
1. 确认已安装Chrome或Firefox
2. 更新浏览器到最新版本
3. 清除WebDriver缓存：删除 `drivers/` 目录
4. 重启ATG-Client

### Q4: 测试执行超时？

**解决方案**：
1. 检查网络连接
2. 增加步骤的timeout值
3. 添加wait步骤
4. 检查元素定位器是否正确

---

## 日志文件

### 日志位置

- **Windows**: `C:\Users\{用户名}\.atg-client\logs\`
- **macOS**: `~/Library/Logs/atg-client.log`
- **Linux**: `/var/log/atg-client.log` 或 `journalctl -u atg-client`

### 日志级别

- **INFO**: 正常信息
- **WARN**: 警告信息
- **ERROR**: 错误信息

---

## 卸载

### Windows

```cmd
# 以管理员身份运行
npm run uninstall-service

# 或手动删除
sc delete ATG-Client
```

### macOS

```bash
npm run uninstall-service

# 或手动删除
launchctl unload ~/Library/LaunchAgents/com.atgclient.plist
rm ~/Library/LaunchAgents/com.atgclient.plist
```

### Linux

```bash
sudo npm run uninstall-service

# 或手动删除
sudo systemctl stop atg-client
sudo systemctl disable atg-client
sudo rm /etc/systemd/system/atg-client.service
sudo systemctl daemon-reload
```

### 清理数据

```bash
# 删除配置和日志
rm -rf ~/.atg-client
```

---

## 更新升级

### 检查更新

在AI-ATG平台会显示可用的新版本。

### 升级步骤

1. 停止ATG-Client服务
2. 下载新版本安装包
3. 解压并覆盖原文件
4. 重新运行安装脚本
5. 验证版本：访问 http://localhost:9999/health

---

## 最佳实践

### 1. 定期检查服务状态

在平台首页或测试执行页面，会自动检查ATG-Client状态。

### 2. 使用有头模式调试

开发测试用例时，设置 `headless: false`，可以看到浏览器实际操作，方便调试。

### 3. 批量执行时使用无头模式

正式运行大量测试时，设置 `headless: true`，节省系统资源。

### 4. 定期清理日志

日志文件可能会变大，建议定期清理或设置日志轮转。

### 5. 保持浏览器最新

浏览器版本更新后，ATG-Client会自动下载匹配的WebDriver。

---

## 技术架构

```
┌─────────────────┐
│  Web浏览器       │
│  (AI-ATG平台)    │
└────────┬────────┘
         │ HTTP请求
         ↓
┌─────────────────┐
│  ATG-Client     │  (localhost:9999)
│  (Node.js)      │
└────────┬────────┘
         │ Selenium WebDriver
         ↓
┌─────────────────┐
│ 本地浏览器       │
│ (Chrome/Firefox)│
└─────────────────┘
```

---

## 常见问题

**Q: 为什么要在本地电脑运行ATG-Client？**

A: 因为UI自动化需要真实的浏览器环境，在本地执行可以：
- 使用本地已安装的浏览器
- 不需要服务器安装图形界面
- 可以实时看到测试过程
- 支持多台电脑并发执行

**Q: ATG-Client会占用很多资源吗？**

A: 不会。ATG-Client本身只占用约50MB内存，只有在执行测试时才会启动浏览器。

**Q: 可以多台电脑同时使用吗？**

A: 可以。每台电脑安装自己的ATG-Client，都可以执行测试，互不干扰。

**Q: ATG-Client安全吗？**

A: 安全。ATG-Client只监听 `127.0.0.1`，不对外网开放，只接受本机的连接。

**Q: 可以自定义端口吗？**

A: 可以。修改配置文件中的端口号，然后重启服务即可。

---

## 获取帮助

1. **在线帮助**: AI-ATG平台 → 帮助中心 → UI测试指南
2. **下载中心**: AI-ATG平台 → 帮助中心 → 下载中心
3. **查看日志**: 见上方"日志文件"章节
4. **联系管理员**: 通过平台提交问题

---

## 总结

**ATG-Client** 是AI-ATG平台UI自动化测试的核心组件：

✅ **专业** - 专为UI自动化设计  
✅ **简单** - 一次安装，永久使用  
✅ **高效** - Web直接调用，无需手动操作  
✅ **稳定** - 自动启动，自动恢复  
✅ **友好** - 托盘图标，桌面通知  

**现在就开始使用ATG-Client，体验专业的UI自动化测试！** 🚀
