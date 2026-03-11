# 客户端测试代理部署指南

## 概述

客户端测试代理（Agent）是一个轻量级的Java程序，运行在测试人员的电脑上，负责：
- 连接到AI-ATG服务器
- 接收测试任务
- 在本地浏览器执行UI测试
- 上传测试结果

## 系统要求

### 硬件要求
- CPU: 双核及以上
- 内存: 4GB及以上
- 硬盘: 500MB可用空间

### 软件要求
- **Java 17或更高版本**（必需）
- **浏览器**：Chrome 或 Firefox（必需）
- 操作系统：Windows 10+、macOS 10.14+、Ubuntu 18.04+

## 安装步骤

### 步骤1: 安装Java环境

#### Windows
1. 下载Java JDK: https://www.oracle.com/java/technologies/downloads/
2. 双击安装包，按提示安装
3. 验证安装：
   ```cmd
   java -version
   ```

#### Mac
```bash
# 使用Homebrew安装
brew install openjdk@17

# 验证安装
java -version
```

#### Linux (Ubuntu/Debian)
```bash
sudo apt update
sudo apt install openjdk-17-jdk

# 验证安装
java -version
```

### 步骤2: 下载Agent程序

#### 方式1: 从平台下载

1. 登录AI-ATG平台
2. 进入 "下载中心"
3. 下载 "测试代理程序"
4. 解压到任意目录

#### 方式2: 从Release下载

访问：http://your-server:19080/downloads/agent-latest.zip

### 步骤3: 下载浏览器驱动（可选）

Agent程序会**自动下载**浏览器驱动，无需手动下载。

如果网络受限，可以手动下载：

#### Chrome浏览器驱动
- Windows: http://your-server:19080/downloads/chromedriver-win64.zip
- Mac: http://your-server:19080/downloads/chromedriver-mac64.zip
- Linux: http://your-server:19080/downloads/chromedriver-linux64.zip

#### Firefox浏览器驱动
- Windows: http://your-server:19080/downloads/geckodriver-win64.zip
- Mac: http://your-server:19080/downloads/geckodriver-mac64.zip
- Linux: http://your-server:19080/downloads/geckodriver-linux64.zip

手动下载后，解压到Agent目录的 `drivers/` 文件夹。

### 步骤4: 配置Agent

复制配置文件：
```bash
cp agent.properties.example agent.properties
```

编辑 `agent.properties`：
```properties
# 服务器地址（替换为实际地址）
server.url=http://your-server:19080/api

# 浏览器类型
browser=chrome

# 是否无头模式（false=可以看到浏览器操作）
headless=false
```

### 步骤5: 启动Agent

#### Windows
双击运行 `start.bat`

或命令行：
```cmd
start.bat
```

#### Mac/Linux
```bash
chmod +x start.sh
./start.sh
```

### 步骤6: 验证运行状态

启动后，您应该看到类似输出：
```
====================================
  AI-ATG 测试代理启动中...
====================================
配置加载成功
服务器地址: http://your-server:19080/api
代理ID: agent-DESKTOP-123-a1b2c3d4
浏览器: chrome
无头模式: false
初始化WebDriver...
ChromeDriver已就绪
正在注册到服务器...
注册成功
代理启动成功，开始监听任务
```

在AI-ATG Web界面，进入 "系统管理" → "测试代理"，应该能看到您的代理在线。

## 目录结构

```
ai-atg-agent/
├── ai-atg-agent-1.0.0-jar-with-dependencies.jar  # 主程序
├── agent.properties.example  # 配置文件示例
├── agent.properties         # 配置文件（需创建）
├── start.bat               # Windows启动脚本
├── start.sh                # Mac/Linux启动脚本
├── drivers/                # 浏览器驱动（可选，自动下载）
│   ├── chromedriver.exe
│   └── geckodriver.exe
└── logs/                   # 日志文件（自动生成）
    └── agent.log
```

## 配置说明

### 完整配置项

```properties
# 服务器地址（必填）
server.url=http://localhost:19080/api

# 代理ID（可选，不填自动生成）
# 格式：agent-{hostname}-{random}
agent.id=

# 代理名称（可选，不填使用主机名）
agent.name=测试代理-张三电脑

# 浏览器类型：chrome, firefox
browser=chrome

# 是否无头模式：true, false
# false - 可以看到浏览器操作（推荐开发和调试）
# true - 后台运行，不显示浏览器（节省资源）
headless=false

# 任务轮询间隔（秒）
# Agent每隔N秒向服务器查询一次新任务
poll.interval=5

# 心跳间隔（秒）
# Agent每隔N秒向服务器发送一次心跳
heartbeat.interval=30
```

### 推荐配置

**开发/调试环境**：
```properties
server.url=http://localhost:19080/api
browser=chrome
headless=false  # 可以看到浏览器操作
poll.interval=5
```

**生产环境**：
```properties
server.url=http://production-server:19080/api
browser=chrome
headless=true   # 后台运行
poll.interval=10
```

## 使用说明

### 执行测试

1. 启动Agent程序
2. 确保Agent状态为"在线"
3. 在Web界面创建UI测试执行
4. 选择 "执行模式" 为 "客户端代理"
5. 选择可用的Agent
6. 开始执行

Agent会自动：
- 接收测试任务
- 启动浏览器
- 执行测试步骤
- 截图
- 上传结果

### 查看执行过程

- **有头模式**（headless=false）：可以看到浏览器实际操作
- **查看日志**：`logs/agent.log` 文件
- **Web界面**：实时查看执行状态

### 停止Agent

- 按 `Ctrl+C` 停止程序
- 或关闭命令行窗口

Agent会自动：
- 完成当前正在执行的任务
- 通知服务器下线
- 关闭浏览器
- 清理资源

## 故障排查

### 问题1: Java版本不匹配

**现象**：
```
Error: A JNI error has occurred
Unsupported class file major version 61
```

**解决方案**：
- 安装Java 17或更高版本
- 检查版本：`java -version`

### 问题2: 无法连接服务器

**现象**：
```
注册失败: Connection refused
```

**解决方案**：
1. 检查server.url配置是否正确
2. 检查服务器是否启动
3. 检查防火墙设置
4. 测试连接：`curl http://server-url/api/health`

### 问题3: WebDriver下载失败

**现象**：
```
WebDriver初始化失败
Could not download chromedriver
```

**解决方案**：
1. 检查网络连接
2. 手动下载驱动到 `drivers/` 目录
3. 配置代理（如果需要）

### 问题4: 浏览器启动失败

**现象**：
```
Session not created: This version of ChromeDriver only supports Chrome version 120
```

**解决方案**：
1. 更新浏览器到最新版本
2. 或下载对应版本的WebDriver

### 问题5: 元素定位失败

**现象**：
测试失败，日志显示 "Element not found"

**解决方案**：
1. 使用有头模式（headless=false）查看实际页面
2. 检查元素定位器是否正确
3. 增加等待时间
4. 检查页面加载速度

## 高级配置

### 配置代理服务器

如果需要通过代理访问网络：

```properties
# HTTP代理
http.proxyHost=proxy.company.com
http.proxyPort=19080

# HTTPS代理
https.proxyHost=proxy.company.com
https.proxyPort=19080
```

### 配置日志级别

编辑 `logback.xml`（如果存在）：

```xml
<logger name="com.aiatg.agent" level="DEBUG"/>
```

### 后台运行（Linux/Mac）

```bash
# 使用nohup后台运行
nohup ./start.sh > agent.out 2>&1 &

# 查看进程
ps aux | grep ai-atg-agent

# 停止进程
kill <PID>
```

### 开机自启动

#### Windows
1. 创建 `start.vbs` 文件：
```vbscript
Set WshShell = CreateObject("WScript.Shell")
WshShell.Run "cmd /c start.bat", 0
Set WshShell = Nothing
```
2. 将快捷方式放到启动文件夹

#### Linux (systemd)
创建服务文件 `/etc/systemd/system/ai-atg-agent.service`：
```ini
[Unit]
Description=AI-ATG Test Agent
After=network.target

[Service]
Type=simple
User=yourusername
WorkingDirectory=/path/to/agent
ExecStart=/usr/bin/java -jar ai-atg-agent-1.0.0-jar-with-dependencies.jar
Restart=always

[Install]
WantedBy=multi-user.target
```

启用服务：
```bash
sudo systemctl enable ai-atg-agent
sudo systemctl start ai-atg-agent
```

## 最佳实践

### 1. 专用测试机

- 建议使用专门的电脑运行Agent
- 避免在执行测试时使用该电脑进行其他工作

### 2. 环境一致性

- 确保测试机环境与目标环境一致
- 统一浏览器版本
- 统一屏幕分辨率

### 3. 资源管理

- 定期清理截图和日志
- 监控Agent运行状态
- 及时更新Agent版本

### 4. 安全性

- 不要在公共网络运行Agent
- 定期更新Token
- 限制Agent权限

## WebDriver下载地址

### 官方下载地址

**ChromeDriver**:
- https://chromedriver.chromium.org/downloads
- https://googlechromelabs.github.io/chrome-for-testing/

**GeckoDriver (Firefox)**:
- https://github.com/mozilla/geckodriver/releases

### 平台内置下载

AI-ATG平台提供WebDriver离线包：

```
访问：http://your-server:19080/downloads

可下载：
- chromedriver-win64.zip
- chromedriver-mac64.zip
- chromedriver-linux64.zip
- geckodriver-win64.zip
- geckodriver-mac64.zip
- geckodriver-linux64.zip
- agent-offline.zip (包含Agent程序和所有驱动)
```

## 更新Agent

### 在线更新
1. 停止当前Agent
2. 下载最新版本
3. 覆盖jar文件
4. 重新启动

### 配置文件兼容
- 配置文件向后兼容
- 新版本可能添加新配置项
- 参考新的 `agent.properties.example`

## 获取帮助

遇到问题？

1. 查看日志文件 `logs/agent.log`
2. 查看本文档的 "故障排查" 部分
3. 访问平台帮助中心
4. 联系管理员

## 总结

客户端Agent模式的优势：

✅ **服务器无需安装浏览器**
✅ **分散执行负载**
✅ **测试真实客户端环境**
✅ **支持Windows/Mac/Linux**
✅ **简单易部署**
✅ **自动化程度高**

Happy Testing! 🚀
