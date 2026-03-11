# ATG-Client 配置指南

## 📍 配置文件位置

安装 ATG-Client 后，配置文件会自动创建在以下位置：

- **Windows**: `C:\Users\<用户名>\.atg-client\config.json`
- **macOS**: `~/.atg-client/config.json`
- **Linux**: `~/.atg-client/config.json`

## 📝 配置文件内容

默认的配置文件内容：

```json
{
  "serverUrl": "http://localhost:19080",
  "browser": "chrome",
  "headless": false,
  "autoStart": true,
  "chromeDriverPath": "",
  "geckoDriverPath": ""
}
```

## ⚙️ 配置项说明

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `serverUrl` | string | `http://localhost:19080` | AI-ATG 后端服务地址 |
| `browser` | string | `chrome` | 使用的浏览器：`chrome` 或 `firefox` |
| `headless` | boolean | `false` | 是否使用无头模式（不显示浏览器窗口） |
| `autoStart` | boolean | `true` | 是否开机自动启动服务 |
| `chromeDriverPath` | string | `""` | ChromeDriver 可执行文件的完整路径 |
| `geckoDriverPath` | string | `""` | GeckoDriver 可执行文件的完整路径 |

## 🔧 配置 WebDriver

### 方法1：指定完整路径（推荐）

下载 WebDriver 后，在配置文件中指定完整路径：

#### macOS 示例

```json
{
  "browser": "chrome",
  "chromeDriverPath": "/usr/local/bin/chromedriver",
  "geckoDriverPath": "/usr/local/bin/geckodriver"
}
```

#### Windows 示例

```json
{
  "browser": "chrome",
  "chromeDriverPath": "C:\\WebDrivers\\chromedriver.exe",
  "geckoDriverPath": "C:\\WebDrivers\\geckodriver.exe"
}
```

#### Linux 示例

```json
{
  "browser": "chrome",
  "chromeDriverPath": "/usr/local/bin/chromedriver",
  "geckoDriverPath": "/usr/local/bin/geckodriver"
}
```

### 方法2：放到系统 PATH

如果将 WebDriver 放到系统 PATH 路径中，可以留空配置项，程序会自动查找。

## 📖 如何编辑配置文件

### macOS

```bash
# 使用默认文本编辑器打开
open -e ~/.atg-client/config.json

# 或使用 nano 编辑器
nano ~/.atg-client/config.json

# 或使用 vim 编辑器
vim ~/.atg-client/config.json
```

### Windows

```cmd
# 使用记事本打开
notepad %USERPROFILE%\.atg-client\config.json

# 或使用 VSCode 打开
code %USERPROFILE%\.atg-client\config.json
```

### Linux

```bash
# 使用 nano 编辑器
nano ~/.atg-client/config.json

# 或使用 vim 编辑器
vim ~/.atg-client/config.json
```

## 🔄 使配置生效

修改配置文件后，需要重启 ATG-Client 服务：

### macOS

```bash
# 重启服务
launchctl unload ~/Library/LaunchAgents/com.atgclient.plist
launchctl load ~/Library/LaunchAgents/com.atgclient.plist

# 或使用 restart 命令
launchctl restart com.atgclient
```

### Windows

```cmd
# 重启服务
net stop ATG-Client
net start ATG-Client
```

### Linux

```bash
# 重启服务
sudo systemctl restart atg-client
```

## ✅ 验证配置

配置完成后，可以通过健康检查接口验证：

```bash
curl http://localhost:9999/health
```

成功响应示例：

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

## 💡 常见配置示例

### 开发环境配置（有头模式，便于调试）

```json
{
  "browser": "chrome",
  "headless": false,
  "chromeDriverPath": "/usr/local/bin/chromedriver"
}
```

### 生产环境配置（无头模式，节省资源）

```json
{
  "browser": "chrome",
  "headless": true,
  "chromeDriverPath": "/usr/local/bin/chromedriver"
}
```

### 使用 Firefox

```json
{
  "browser": "firefox",
  "headless": false,
  "geckoDriverPath": "/usr/local/bin/geckodriver"
}
```

## ❓ 故障排查

### 配置文件不存在

如果配置文件不存在，服务会自动创建默认配置文件。

### WebDriver 找不到

如果提示找不到 WebDriver：

1. 检查路径是否正确（注意 Windows 路径需要使用双反斜杠 `\\`）
2. 确认 WebDriver 文件有执行权限（macOS/Linux）
3. 验证 WebDriver 版本与浏览器版本匹配

### 配置不生效

确保：
1. 配置文件格式正确（有效的 JSON）
2. 重启了服务
3. 检查服务日志：
   - macOS: `~/Library/Logs/atg-client.log`
   - Windows: `%USERPROFILE%\.ai-atg-service\logs\`
   - Linux: `journalctl -u atg-client`
