# WebDriver 配置指南

ATG-Client 需要 WebDriver 才能执行浏览器自动化测试。由于网络限制，您需要手动下载并配置 WebDriver。

## 📥 下载 WebDriver

### Chrome 浏览器 - ChromeDriver

**下载地址：**
- 官方地址：https://chromedriver.chromium.org/downloads
- 淘宝镜像：https://registry.npmmirror.com/binary.html?path=chromedriver/
- 百度网盘：请联系管理员获取

**版本选择：**
1. 打开 Chrome 浏览器，在地址栏输入 `chrome://version/`
2. 查看版本号（例如：120.0.6099.109）
3. 下载对应主版本号的 ChromeDriver（例如：120.x.x.x）

**支持的操作系统：**
- Windows: `chromedriver_win32.zip`
- macOS (Intel): `chromedriver_mac64.zip`
- macOS (Apple Silicon): `chromedriver_mac_arm64.zip`
- Linux: `chromedriver_linux64.zip`

### Firefox 浏览器 - GeckoDriver

**下载地址：**
- 官方地址：https://github.com/mozilla/geckodriver/releases
- 淘宝镜像：https://registry.npmmirror.com/binary.html?path=geckodriver/
- 百度网盘：请联系管理员获取

**版本选择：**
- 建议下载最新稳定版本（如 v0.34.0）

**支持的操作系统：**
- Windows: `geckodriver-vX.XX.X-win64.zip`
- macOS: `geckodriver-vX.XX.X-macos.tar.gz`
- Linux: `geckodriver-vX.XX.X-linux64.tar.gz`

## 📦 安装步骤

### Windows

1. **下载 ChromeDriver**
   ```
   https://registry.npmmirror.com/binary.html?path=chromedriver/
   ```

2. **解压到指定目录**
   ```
   C:\Program Files\WebDriver\chromedriver.exe
   ```

3. **配置 ATG-Client**
   
   编辑配置文件 `%USERPROFILE%\.atg-client\config.json`：
   ```json
   {
     "serverUrl": "http://localhost:19080",
     "browser": "chrome",
     "headless": false,
     "chromeDriverPath": "C:\\Program Files\\WebDriver\\chromedriver.exe"
   }
   ```

### macOS

1. **下载 ChromeDriver**
   ```bash
   # Intel Mac
   curl -O https://registry.npmmirror.com/-/binary/chromedriver/120.0.6099.109/chromedriver_mac64.zip
   
   # Apple Silicon Mac
   curl -O https://registry.npmmirror.com/-/binary/chromedriver/120.0.6099.109/chromedriver_mac_arm64.zip
   ```

2. **解压并移动到系统目录**
   ```bash
   unzip chromedriver_mac*.zip
   sudo mv chromedriver /usr/local/bin/
   sudo chmod +x /usr/local/bin/chromedriver
   ```

3. **移除隔离属性（重要！）**
   ```bash
   sudo xattr -d com.apple.quarantine /usr/local/bin/chromedriver
   ```

4. **配置 ATG-Client**
   
   编辑配置文件 `~/.atg-client/config.json`：
   ```json
   {
     "serverUrl": "http://localhost:19080",
     "browser": "chrome",
     "headless": false,
     "chromeDriverPath": "/usr/local/bin/chromedriver"
   }
   ```

### Linux

1. **下载 ChromeDriver**
   ```bash
   wget https://registry.npmmirror.com/-/binary/chromedriver/120.0.6099.109/chromedriver_linux64.zip
   ```

2. **解压并移动到系统目录**
   ```bash
   unzip chromedriver_linux64.zip
   sudo mv chromedriver /usr/local/bin/
   sudo chmod +x /usr/local/bin/chromedriver
   ```

3. **配置 ATG-Client**
   
   编辑配置文件 `~/.atg-client/config.json`：
   ```json
   {
     "serverUrl": "http://localhost:19080",
     "browser": "chrome",
     "headless": false,
     "chromeDriverPath": "/usr/local/bin/chromedriver"
   }
   ```

## ✅ 验证安装

### 验证 ChromeDriver

**Windows:**
```cmd
"C:\Program Files\WebDriver\chromedriver.exe" --version
```

**macOS/Linux:**
```bash
chromedriver --version
```

应该输出类似：
```
ChromeDriver 120.0.6099.109 (xxx)
```

### 验证 GeckoDriver

**Windows:**
```cmd
"C:\Program Files\WebDriver\geckodriver.exe" --version
```

**macOS/Linux:**
```bash
geckodriver --version
```

应该输出类似：
```
geckodriver 0.34.0
```

## 🔧 配置文件说明

配置文件位置：
- **Windows**: `%USERPROFILE%\.atg-client\config.json`
- **macOS/Linux**: `~/.atg-client/config.json`

完整配置示例：
```json
{
  "serverUrl": "http://localhost:8080",
  "browser": "chrome",
  "headless": false,
  "autoStart": true,
  "chromeDriverPath": "/usr/local/bin/chromedriver",
  "geckoDriverPath": "/usr/local/bin/geckodriver"
}
```

配置项说明：
- `serverUrl`: ATG 后端服务地址
- `browser`: 浏览器类型（chrome 或 firefox）
- `headless`: 是否使用无头模式
- `autoStart`: 是否开机自启动
- `chromeDriverPath`: ChromeDriver 可执行文件路径
- `geckoDriverPath`: GeckoDriver 可执行文件路径

## ❓ 常见问题

### 1. ChromeDriver 版本不匹配

**错误信息：**
```
This version of ChromeDriver only supports Chrome version XXX
```

**解决方案：**
- 检查 Chrome 浏览器版本
- 下载匹配的 ChromeDriver 版本

### 2. macOS 提示"无法验证开发者"

**错误信息：**
```
"chromedriver" cannot be opened because the developer cannot be verified
```

**解决方案：**
```bash
sudo xattr -d com.apple.quarantine /usr/local/bin/chromedriver
```

### 3. Windows 提示"找不到文件"

**解决方案：**
- 检查路径是否正确
- 使用双反斜杠 `\\` 或正斜杠 `/`
- 确保文件有执行权限

### 4. Linux 提示"权限被拒绝"

**解决方案：**
```bash
sudo chmod +x /usr/local/bin/chromedriver
```

## 📚 相关资源

- [ChromeDriver 官方文档](https://chromedriver.chromium.org/)
- [GeckoDriver 官方文档](https://firefox-source-docs.mozilla.org/testing/geckodriver/)
- [Selenium WebDriver 文档](https://www.selenium.dev/documentation/webdriver/)
- [淘宝 NPM 镜像](https://registry.npmmirror.com/)

## 🆘 获取帮助

如果遇到问题，请：
1. 检查配置文件路径是否正确
2. 验证 WebDriver 可执行权限
3. 查看 ATG-Client 日志输出
4. 联系技术支持
