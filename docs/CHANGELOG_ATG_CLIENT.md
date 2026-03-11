# ATG-Client 重命名说明

## 更新时间: 2026-01-28

## 重命名原因

为了提升专业性和品牌识别度，将"轻量级本地测试服务"正式命名为 **ATG-Client**。

---

## 名称变更清单

### 之前的名称
- ❌ 轻量级本地测试服务
- ❌ 本地测试服务
- ❌ AI-ATG测试服务
- ❌ agent-service

### 现在的名称
- ✅ **ATG-Client**

---

## 更新的文件

### 后端 (agent-service/)

| 文件 | 更新内容 |
|------|---------|
| `package.json` | 项目名称: `ai-atg-agent-service` → `atg-client` |
| `src/index.js` | 配置目录: `.ai-atg-service` → `.atg-client` |
| `src/index.js` | 通知标题: `AI-ATG 测试服务` → `ATG-Client` |
| `src/installer.js` | 服务名称: `AI-ATG-Test-Service` → `ATG-Client` |
| `src/installer.js` | macOS标识: `com.aiatg.testservice` → `com.atgclient` |
| `src/installer.js` | Linux服务: `ai-atg-service` → `atg-client` |
| `src/installer.js` | 日志文件: `ai-atg-service.log` → `atg-client.log` |
| `README.md` | 完全重写，使用新名称 |

### 前端 (frontend/)

| 文件 | 更新内容 |
|------|---------|
| `views/execution/ExecutionList.vue` | 提示信息更新 |
| `components/QuickGuide.vue` | 步骤标题更新 |
| `components/LocalServiceChecker.vue` | 警告和帮助文本更新 |
| `views/help/DownloadCenter.vue` | 卡片标题和描述更新 |
| `views/help/DownloadCenter.vue` | 下载文件名: `agent-service-*.zip` → `atg-client-*.zip` |
| `views/help/UITestHelp.vue` | 所有页面标题和说明更新 |
| `views/help/UITestHelp.vue` | 配置路径: `.ai-atg-service` → `.atg-client` |
| `views/help/UITestHelp.vue` | 系统服务命令更新 |

### 文档 (docs/)

| 文件 | 说明 |
|------|------|
| `ATG_CLIENT_GUIDE.md` | **新增** - ATG-Client完整使用指南 |
| `CHANGELOG_ATG_CLIENT.md` | **新增** - 本文件，重命名说明 |

---

## 系统服务名称变更

### Windows

- **之前**: `AI-ATG-Test-Service`
- **现在**: `ATG-Client`

```cmd
# 管理命令
net start ATG-Client
net stop ATG-Client
sc query ATG-Client
```

### macOS

- **之前**: `com.aiatg.testservice`
- **现在**: `com.atgclient`

```bash
# plist位置
~/Library/LaunchAgents/com.atgclient.plist

# 日志位置
~/Library/Logs/atg-client.log
```

### Linux

- **之前**: `ai-atg-service`
- **现在**: `atg-client`

```bash
# 服务文件
/etc/systemd/system/atg-client.service

# 管理命令
sudo systemctl start atg-client
sudo systemctl status atg-client
```

---

## 配置文件路径变更

### 配置目录

- **之前**: `~/.ai-atg-service/`
- **现在**: `~/.atg-client/`

### 配置文件

- **之前**: `~/.ai-atg-service/config.json`
- **现在**: `~/.atg-client/config.json`

### 日志文件

| 平台 | 之前 | 现在 |
|------|------|------|
| Windows | `.ai-atg-service\logs\` | `.atg-client\logs\` |
| macOS | `~/Library/Logs/ai-atg-service.log` | `~/Library/Logs/atg-client.log` |
| Linux | `/var/log/ai-atg-service.log` | `/var/log/atg-client.log` |

---

## 下载文件名称变更

### 安装包

| 平台 | 之前 | 现在 |
|------|------|------|
| Windows | `agent-service-windows.zip` | `atg-client-windows.zip` |
| macOS | `agent-service-macos.zip` | `atg-client-macos.zip` |
| Linux | `agent-service-linux.tar.gz` | `atg-client-linux.tar.gz` |

### 可执行文件

| 平台 | 之前 | 现在 |
|------|------|------|
| Windows | `ai-atg-service-win.exe` | `atg-client-win.exe` |
| macOS | `ai-atg-service-macos` | `atg-client-macos` |
| Linux | `ai-atg-service-linux` | `atg-client-linux` |

---

## 升级指南

### 对于已安装的用户

如果您已经安装了旧版本（使用旧名称），建议按以下步骤升级：

#### 1. 卸载旧版本

**Windows:**
```cmd
# 停止服务
net stop "AI-ATG-Test-Service"

# 卸载
sc delete "AI-ATG-Test-Service"

# 删除配置(可选)
rmdir /s C:\Users\<用户名>\.ai-atg-service
```

**macOS:**
```bash
# 卸载服务
launchctl unload ~/Library/LaunchAgents/com.aiatg.testservice.plist
rm ~/Library/LaunchAgents/com.aiatg.testservice.plist

# 删除配置(可选)
rm -rf ~/.ai-atg-service
```

**Linux:**
```bash
# 卸载服务
sudo systemctl stop ai-atg-service
sudo systemctl disable ai-atg-service
sudo rm /etc/systemd/system/ai-atg-service.service
sudo systemctl daemon-reload

# 删除配置(可选)
rm -rf ~/.ai-atg-service
```

#### 2. 安装新版本

按照 `ATG_CLIENT_GUIDE.md` 的安装说明安装ATG-Client。

#### 3. 迁移配置(可选)

如果需要保留旧的配置：

```bash
# macOS/Linux
cp ~/.ai-atg-service/config.json ~/.atg-client/config.json

# Windows (PowerShell)
Copy-Item C:\Users\<用户名>\.ai-atg-service\config.json C:\Users\<用户名>\.atg-client\config.json
```

---

## 对用户的影响

### ✅ 无影响的部分

- HTTP端口号不变(仍为9999)
- API接口路径不变
- 功能完全一致
- 配置格式不变

### ⚠️ 需要注意的部分

- 系统服务名称变更(需要使用新命令)
- 配置文件位置变更(如有自定义配置需要迁移)
- 下载文件名称变更(下载新版本时注意)
- 系统托盘图标可能显示新名称

---

## 兼容性

### 向后兼容

ATG-Client的API接口与之前完全兼容，无需修改AI-ATG平台的调用代码。

### 共存问题

**不建议**新旧版本同时安装，如果发现两个服务都在运行，请卸载旧版本。

---

## 文档更新

### 新增文档

- `docs/ATG_CLIENT_GUIDE.md` - ATG-Client完整使用指南

### 更新文档

- `agent-service/README.md` - 完全重写
- `frontend/src/views/help/UITestHelp.vue` - 帮助页面更新
- `frontend/src/views/help/DownloadCenter.vue` - 下载中心更新

### 待更新文档

以下文档仍使用旧名称，但不影响使用，将逐步更新：

- `docs/LOCAL_SERVICE_GUIDE.md`
- `docs/UI_AUTOMATION_COMPLETE_GUIDE.md`
- `docs/DEPLOYMENT_SUMMARY.md`
- 其他技术文档

---

## 品牌识别

### 名称含义

**ATG-Client** = **A**I **T**est **G**eneration - **Client**

- **专业性**: 作为AI-ATG平台的官方客户端
- **明确性**: 清晰表明这是一个客户端程序
- **一致性**: 与AI-ATG平台命名风格统一
- **国际化**: 使用通用的英文名称

### 视觉标识

- 系统托盘图标保持不变
- 桌面通知显示"ATG-Client"
- 控制台输出使用"ATG-Client"
- 文档中统一使用"ATG-Client"

---

## 总结

本次重命名主要目的是:

1. ✅ **提升专业性** - 使用统一的专业名称
2. ✅ **增强识别度** - ATG-Client更易于记忆和识别
3. ✅ **保持一致性** - 与AI-ATG平台命名风格统一
4. ✅ **便于沟通** - 在文档和讨论中使用统一名称

**ATG-Client** 是AI-ATG平台UI自动化测试的专业客户端！

---

## 相关文档

- [ATG-Client使用指南](./ATG_CLIENT_GUIDE.md)
- [ATG-Client README](../agent-service/README.md)
- [UI自动化完整指南](./UI_AUTOMATION_COMPLETE_GUIDE.md)
- [部署检查清单](./DEPLOYMENT_CHECKLIST.md)

---

**更新日期**: 2026-01-28  
**版本**: 1.0.0
