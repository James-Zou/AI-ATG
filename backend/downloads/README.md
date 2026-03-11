# 下载文件目录

此目录用于存放用户可下载的文件，包括ATG-Client安装包和WebDriver驱动。

## 需要准备的文件

### ATG-Client 安装包

```
atg-client-windows.zip     - Windows版本
atg-client-macos.zip       - macOS版本
atg-client-linux.tar.gz    - Linux版本
```

### WebDriver 驱动（可选）

```
chromedriver-win64.zip
chromedriver-mac64.zip
chromedriver-mac-arm64.zip
chromedriver-linux64.zip
geckodriver-win64.zip
geckodriver-mac64.zip
geckodriver-linux64.zip
```

### 离线完整包（可选）

```
offline-package-windows.zip
offline-package-macos.zip
offline-package-linux.tar.gz
```

## 如何准备文件

### 1. 打包ATG-Client

```bash
cd agent-service

# 安装依赖
npm install

# 方式1: 使用pkg打包（推荐用于生产环境）
npm run build

# 方式2: 直接打包源码（开发/测试环境）
# Windows
cd agent-service
zip -r ../backend/downloads/atg-client-windows.zip .

# macOS
cd agent-service
zip -r ../backend/downloads/atg-client-macos.zip .

# Linux
cd agent-service
tar -czf ../backend/downloads/atg-client-linux.tar.gz .
```

### 2. 下载WebDriver（可选）

ATG-Client会自动下载WebDriver，但如果需要提供离线包：

#### ChromeDriver

访问：https://chromedriver.chromium.org/downloads

```bash
# 下载对应版本后重命名
mv chromedriver_win32.zip downloads/chromedriver-win64.zip
mv chromedriver_mac64.zip downloads/chromedriver-mac64.zip
mv chromedriver_mac_arm64.zip downloads/chromedriver-mac-arm64.zip
mv chromedriver_linux64.zip downloads/chromedriver-linux64.zip
```

#### GeckoDriver (Firefox)

访问：https://github.com/mozilla/geckodriver/releases

```bash
# 下载对应版本后重命名
mv geckodriver-v0.34.0-win64.zip downloads/geckodriver-win64.zip
mv geckodriver-v0.34.0-macos.tar.gz downloads/geckodriver-mac64.zip
mv geckodriver-v0.34.0-linux64.tar.gz downloads/geckodriver-linux64.zip
```

## 快速开始（开发测试）

如果只是测试下载功能，可以创建空文件：

```bash
cd backend/downloads

# 创建测试文件
touch atg-client-windows.zip
touch atg-client-macos.zip
touch atg-client-linux.tar.gz

# 或者创建带内容的测试文件
echo "ATG-Client Windows版本" > atg-client-windows.zip
echo "ATG-Client macOS版本" > atg-client-macos.zip
echo "ATG-Client Linux版本" > atg-client-linux.tar.gz
```

## 文件说明

### ATG-Client安装包应包含

```
agent-service/
├── src/
│   ├── index.js
│   ├── executor.js
│   ├── tray.js
│   └── installer.js
├── package.json
├── install.bat          (Windows)
├── install.sh           (Mac/Linux)
├── icons/
└── README.md
```

### 目录结构

```
backend/downloads/
├── README.md                        (本文件)
├── atg-client-windows.zip
├── atg-client-macos.zip
├── atg-client-linux.tar.gz
├── chromedriver-win64.zip           (可选)
├── chromedriver-mac64.zip           (可选)
├── chromedriver-mac-arm64.zip       (可选)
├── chromedriver-linux64.zip         (可选)
├── geckodriver-win64.zip            (可选)
├── geckodriver-mac64.zip            (可选)
├── geckodriver-linux64.zip          (可选)
├── offline-package-windows.zip      (可选)
├── offline-package-macos.zip        (可选)
└── offline-package-linux.tar.gz     (可选)
```

## 安全建议

1. **文件大小限制**：建议单个文件不超过100MB
2. **文件校验**：提供MD5/SHA256校验值
3. **访问控制**：考虑添加下载次数限制或登录验证
4. **CDN加速**：生产环境建议使用CDN分发

## 更新流程

当ATG-Client有新版本时：

1. 更新 `agent-service/package.json` 中的版本号
2. 重新打包生成新的安装包
3. 替换 `downloads/` 目录中的文件
4. 在平台发布更新公告
5. 提供版本更新说明

## 故障排查

### 下载失败

1. 检查文件是否存在：`ls -lh backend/downloads/`
2. 检查文件权限：`chmod 644 backend/downloads/*.zip`
3. 查看后端日志：检查是否有文件访问错误
4. 验证文件路径：确认 `DOWNLOAD_DIR` 配置正确

### 文件损坏

1. 检查文件完整性
2. 重新打包
3. 使用压缩工具测试文件是否可以正常解压

---

**提示**：开发测试阶段，可以先准备一个平台的安装包即可。生产环境需要准备所有平台的安装包。
