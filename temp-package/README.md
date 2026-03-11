# ATG-Client - AI-ATG客户端测试服务

## 简介

**ATG-Client** 是AI-ATG平台的专业客户端程序，运行在测试人员的本地电脑上，负责执行UI自动化测试。

## 特点

- ✅ **一次安装，开机自启动** - 安装后自动随系统启动
- ✅ **后台常驻** - 静默运行，用户无感知
- ✅ **Web直接调用** - 在平台页面直接点击执行
- ✅ **手动配置WebDriver** - 适应网络限制环境，灵活配置
- ✅ **系统托盘图标** - 显示运行状态，右键菜单管理
- ✅ **桌面通知** - 测试开始/完成实时提醒
- ✅ **支持所有Selenium操作** - click, input, select, wait, assert等
- ✅ **跨平台支持** - Windows, macOS, Linux

---

## 快速安装

### Windows

```cmd
# 1. 解压到目录
unzip atg-client-windows.zip -d C:\atg-client
cd C:\atg-client

# 2. 以管理员身份运行
install.bat

# 3. 验证
curl http://localhost:9999/health
```

### macOS

```bash
# 1. 解压并进入目录
unzip atg-client-macos.zip
cd atg-client

# 2. 安装
chmod +x install.sh
./install.sh

# 3. 验证
curl http://localhost:9999/health
```

### Linux

```bash
# 1. 解压
tar -xzf atg-client-linux.tar.gz
cd atg-client

# 2. 安装
sudo ./install.sh

# 3. 验证
curl http://localhost:9999/health
```

---

## 系统要求

- Node.js 18+ (或使用打包版本，无需Node.js)
- Chrome 或 Firefox 浏览器
- **对应的 WebDriver** (ChromeDriver 或 GeckoDriver) - [下载配置指南](./WEBDRIVER_SETUP.md)
- 500MB 可用磁盘空间
- 管理员权限(安装时)

> ⚠️ **重要提示**：首次使用前，请先按照 [WebDriver 配置指南](./WEBDRIVER_SETUP.md) 下载并配置 WebDriver

---

## 开发

### 安装依赖

```bash
npm install
```

### 开发模式

```bash
npm run dev
```

服务会在 `http://localhost:9999` 启动，支持热重载。

### 生产环境

```bash
npm start
```

### 打包可执行文件

```bash
npm run build
```

会生成以下文件：
- `dist/atg-client-win.exe` (Windows)
- `dist/atg-client-macos` (macOS)
- `dist/atg-client-linux` (Linux)

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

# 卸载服务
npm run uninstall-service
```

### macOS

```bash
# 启动服务
launchctl load ~/Library/LaunchAgents/com.atgclient.plist

# 停止服务
launchctl unload ~/Library/LaunchAgents/com.atgclient.plist

# 查看日志
tail -f ~/Library/Logs/atg-client.log

# 卸载服务
npm run uninstall-service
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

# 卸载服务
sudo npm run uninstall-service
```

---

## 配置

### 配置文件位置

✅ **自动创建**：安装时会自动创建配置文件，无需手动创建！

- Windows: `C:\Users\<用户名>\.atg-client\config.json`
- macOS: `~/.atg-client/config.json`
- Linux: `~/.atg-client/config.json`

> 💡 如果配置文件不存在，服务首次启动时也会自动创建默认配置文件。

### 配置示例

```json
{
  "serverUrl": "http://localhost:19080",
  "browser": "chrome",
  "headless": false,
  "autoStart": true,
  "chromeDriverPath": "/usr/local/bin/chromedriver",
  "geckoDriverPath": "/usr/local/bin/geckodriver"
}
```

### 配置项说明

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| serverUrl | string | http://localhost:19080 | AI-ATG平台服务器地址 |
| browser | string | chrome | 浏览器类型(chrome/firefox) |
| headless | boolean | false | 无头模式(true=后台运行) |
| autoStart | boolean | true | 开机自启动 |
| chromeDriverPath | string | "" | ChromeDriver路径 **(必填)** |
| geckoDriverPath | string | "" | GeckoDriver路径(使用Firefox时) |

> 📖 **WebDriver 配置详细说明**：请查看 [WebDriver 配置指南](./WEBDRIVER_SETUP.md)

---

## API接口

### 健康检查

```
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

### 获取配置

```
GET http://localhost:9999/config
```

### 更新配置

```
POST http://localhost:9999/config
Content-Type: application/json

{
  "browser": "firefox",
  "headless": true
}
```

### 执行测试

```
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

```
GET http://localhost:9999/status
```

### 停止测试

```
POST http://localhost:9999/stop
```

---

## 目录结构

```
atg-client/
├── package.json          - 项目配置
├── src/
│   ├── index.js         - 主服务(Express HTTP服务)
│   ├── executor.js      - Selenium测试执行器
│   ├── tray.js          - 系统托盘管理
│   └── installer.js     - 系统服务安装器
├── install.bat          - Windows安装脚本
├── install.sh           - Mac/Linux安装脚本
├── icons/               - 托盘图标
└── README.md
```

---

## 日志

### 日志位置

- Windows: `C:\Users\<用户名>\.atg-client\logs\`
- macOS: `~/Library/Logs/atg-client.log`
- Linux: `/var/log/atg-client.log` 或 `journalctl -u atg-client`

### 日志级别

- **INFO**: 正常信息
- **WARN**: 警告信息
- **ERROR**: 错误信息

---

## 故障排查

### 服务未运行

1. 检查系统托盘是否有ATG-Client图标
2. 访问 http://localhost:9999/health
3. 查看服务状态(见上方"服务管理")
4. 查看日志文件

### 端口被占用

```bash
# Windows
netstat -ano | findstr :9999

# Mac/Linux
lsof -i :9999
```

找到占用进程并关闭，或修改配置使用其他端口。

### 浏览器启动失败

1. 确认已安装Chrome或Firefox
2. 更新浏览器到最新版本
3. **检查 WebDriver 配置** - 查看 [WebDriver 配置指南](./WEBDRIVER_SETUP.md)
4. 验证 WebDriver 版本与浏览器版本是否匹配
5. 检查 WebDriver 路径和执行权限
6. 重启ATG-Client

### WebDriver 相关问题

**错误：未配置 ChromeDriver 路径**
- 请按照 [WebDriver 配置指南](./WEBDRIVER_SETUP.md) 下载并配置 WebDriver

**错误：ChromeDriver 版本不匹配**
- 检查 Chrome 浏览器版本（chrome://version/）
- 下载对应版本的 ChromeDriver

**错误：找不到 ChromeDriver 文件**
- 检查配置文件中的路径是否正确
- 验证文件是否存在且有执行权限

---

## 技术栈

- **Node.js** - 运行环境
- **Express** - HTTP服务器
- **Selenium WebDriver** - 浏览器自动化
- **node-windows/node-mac** - 系统服务注册
- **systray2** - 系统托盘
- **node-notifier** - 桌面通知

---

## 许可证

MIT

---

## 获取帮助

- **WebDriver 配置**: 见 [WebDriver 配置指南](./WEBDRIVER_SETUP.md)
- 在线文档: AI-ATG平台 → 帮助中心 → UI测试指南
- 下载中心: AI-ATG平台 → 帮助中心 → 下载中心
- 详细文档: 见 `docs/ATG_CLIENT_GUIDE.md`

---

**ATG-Client - 专业的UI自动化测试客户端** 🚀
