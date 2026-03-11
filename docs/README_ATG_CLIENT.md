# ATG-Client - 专业的UI自动化测试客户端

> **重要更新**: "轻量级本地测试服务"已正式更名为 **ATG-Client**

---

## 📢 命名更新公告

为提升专业性和品牌识别度，**本地测试服务**正式更名为：

### **ATG-Client**

**ATG-Client** = **A**I **T**est **G**eneration - **Client**

---

## 🎯 什么是ATG-Client？

**ATG-Client** 是AI-ATG平台的专业客户端程序，运行在测试人员的本地电脑上，负责执行UI自动化测试。

### 核心特性

```
✅ 一次安装，开机自启动
✅ 后台运行，用户无感知  
✅ Web页面直接点击执行
✅ 自动下载WebDriver驱动
✅ 系统托盘显示状态
✅ 桌面通知实时提醒
✅ 支持所有Selenium操作
✅ 跨平台 (Windows/Mac/Linux)
```

---

## 🚀 快速开始

### 1. 下载安装

访问 **AI-ATG平台** → **帮助中心** → **下载中心**

下载对应系统的安装包：
- Windows: `atg-client-windows.zip`
- macOS: `atg-client-macos.zip`
- Linux: `atg-client-linux.tar.gz`

### 2. 安装

#### Windows
```cmd
# 解压后，右键"以管理员身份运行"
install.bat
```

#### macOS/Linux
```bash
chmod +x install.sh
./install.sh
```

### 3. 验证

```bash
curl http://localhost:9999/health
```

看到 `{"status":"ok"}` 表示安装成功！

### 4. 使用

1. 打开AI-ATG平台
2. 创建/选择测试用例
3. 点击"执行测试" ✨
4. ATG-Client自动执行
5. 查看测试报告

**无需任何手动启动操作！**

---

## 📁 项目结构

```
AI-ATG/
├── agent-service/         ← ATG-Client源代码
│   ├── src/
│   │   ├── index.js      - 主服务(HTTP服务器)
│   │   ├── executor.js   - Selenium执行器
│   │   ├── tray.js       - 系统托盘
│   │   └── installer.js  - 服务安装器
│   ├── install.bat       - Windows安装脚本
│   ├── install.sh        - Mac/Linux安装脚本
│   └── README.md         - ATG-Client详细说明
│
├── frontend/             - AI-ATG Web界面
│   └── src/
│       ├── views/help/
│       │   ├── UITestHelp.vue      - UI测试帮助
│       │   └── DownloadCenter.vue  - 下载中心
│       └── components/
│           ├── LocalServiceChecker.vue  - 服务状态检查
│           └── QuickGuide.vue          - 快速指南
│
├── backend/              - AI-ATG后端服务
│
├── docs/                 - 文档目录
│   ├── ATG_CLIENT_GUIDE.md           - ATG-Client完整指南 ⭐
│   ├── CHANGELOG_ATG_CLIENT.md       - 重命名详细说明
│   ├── UI_AUTOMATION_COMPLETE_GUIDE.md
│   └── ...
│
├── ATG_CLIENT_RENAME_SUMMARY.md  - 重命名总结 ⭐
└── README_ATG_CLIENT.md          - 本文件
```

---

## 📖 文档指引

### 开始使用

1. **快速总结** (本文件)
   - 快速了解ATG-Client
   - 5分钟快速安装

2. **完整指南** (`docs/ATG_CLIENT_GUIDE.md`) ⭐ 推荐
   - 详细安装步骤
   - 配置说明
   - API文档
   - 故障排查

3. **重命名说明** (`CHANGELOG_ATG_CLIENT.md`)
   - 详细变更清单
   - 升级指南
   - 兼容性说明

4. **开发文档** (`agent-service/README.md`)
   - 开发环境搭建
   - 技术栈说明
   - 打包发布

### 在线帮助

AI-ATG平台内置帮助：
- **帮助中心** → **UI测试指南**
- **帮助中心** → **下载中心**

---

## 🔄 升级说明

### 新用户

直接安装最新版ATG-Client即可。

### 老用户（已安装旧版本）

#### 选项1: 继续使用旧版本 ✅

- 功能完全相同
- API完全兼容
- 随时可以升级

#### 选项2: 升级到ATG-Client 🆕

详细升级步骤见: `CHANGELOG_ATG_CLIENT.md`

简要步骤：
1. 卸载旧版本
2. 安装ATG-Client
3. (可选) 迁移配置

---

## 🎨 界面预览

### 系统托盘

```
🟢 ATG-Client
━━━━━━━━━━━━━
✓ 服务运行中
📊 查看状态
⚙️ 配置
❌ 退出
```

### 桌面通知

```
┌─────────────────────────┐
│ ATG-Client              │
│ 开始执行测试：登录功能   │
└─────────────────────────┘
```

### Web平台集成

```
AI-ATG平台
  ├── 测试用例
  │   └── [执行] ← 点击此处
  │
  ├── 测试执行
  │   ├── 🟢 ATG-Client运行中
  │   └── 💡 快速使用指南
  │
  └── 帮助中心
      ├── UI测试指南
      └── 下载中心
```

---

## ⚙️ 技术栈

### ATG-Client

- **Node.js** - 运行环境
- **Express** - HTTP服务器
- **Selenium WebDriver** - 浏览器自动化
- **node-windows/node-mac** - 系统服务
- **systray2** - 系统托盘
- **node-notifier** - 桌面通知

### AI-ATG平台

- **后端**: Spring Boot + Java 17
- **前端**: Vue 3 + Element Plus
- **数据库**: MySQL + Redis

---

## 📊 架构图

```
┌────────────────────────────────┐
│     用户浏览器                  │
│   (访问AI-ATG平台)             │
└────────────┬───────────────────┘
             │
             │ 1. 点击"执行测试"
             ↓
┌────────────────────────────────┐
│   AI-ATG后端服务器              │
│   (Spring Boot)                │
└────────────┬───────────────────┘
             │
             │ 2. 前端直接调用本地服务
             ↓
┌────────────────────────────────┐
│   ATG-Client                   │  ← 运行在测试人员电脑
│   (Node.js HTTP服务)           │
│   http://localhost:9999        │
└────────────┬───────────────────┘
             │
             │ 3. 启动浏览器执行测试
             ↓
┌────────────────────────────────┐
│   本地浏览器                    │
│   (Chrome/Firefox)             │
│   自动化操作                    │
└────────────┬───────────────────┘
             │
             │ 4. 上传测试结果
             ↓
┌────────────────────────────────┐
│   AI-ATG后端服务器              │
│   (保存结果和报告)              │
└────────────────────────────────┘
```

---

## 💡 常见问题

### Q: ATG-Client和浏览器扩展有什么区别？

A: ATG-Client是独立程序，功能更强大：
- ✅ 支持所有Selenium操作
- ✅ 可以启动独立浏览器
- ✅ 不受浏览器限制
- ✅ 更稳定可靠

### Q: 需要手动启动ATG-Client吗？

A: **不需要！** ATG-Client安装后会：
- ✅ 自动开机启动
- ✅ 后台静默运行
- ✅ Web页面直接调用

### Q: ATG-Client安全吗？

A: 完全安全：
- ✅ 只监听本机(127.0.0.1)
- ✅ 不对外网开放
- ✅ 开源可审计

### Q: 占用多少资源？

A: 非常轻量：
- 💾 内存: ~50MB
- 💽 磁盘: ~500MB
- 🔋 CPU: 空闲时几乎为0

### Q: 支持哪些浏览器？

A: 
- ✅ Chrome (推荐)
- ✅ Firefox
- ✅ Edge (使用ChromeDriver)

### Q: 可以多台电脑同时用吗？

A: 可以！每台电脑安装自己的ATG-Client，互不干扰，支持并发执行。

---

## 🎯 核心优势

### 对比其他方案

| 特性 | ATG-Client ⭐ | Java Agent | 浏览器扩展 |
|------|-------------|-----------|----------|
| **自动启动** | ✅ 开机自启 | ❌ 需手动 | ⚠️ 受限 |
| **Web直调** | ✅ 是 | ❌ 否 | ✅ 是 |
| **功能完整** | ✅ 全部 | ✅ 全部 | ⚠️ 受限 |
| **独立浏览器** | ✅ 支持 | ✅ 支持 | ❌ 不支持 |
| **系统托盘** | ✅ 有 | ❌ 无 | ❌ 无 |
| **桌面通知** | ✅ 有 | ❌ 无 | ⚠️ 有限 |
| **资源占用** | ✅ 低 | ⚠️ 中等 | ✅ 低 |

**ATG-Client 是最佳选择！** 🏆

---

## 📞 获取帮助

### 文档

- **ATG-Client完整指南**: `docs/ATG_CLIENT_GUIDE.md`
- **重命名说明**: `CHANGELOG_ATG_CLIENT.md`
- **开发文档**: `agent-service/README.md`

### 在线帮助

- AI-ATG平台 → 帮助中心
- 下载中心 → 文档资源

### 技术支持

- 查看日志: `~/.atg-client/logs/`
- 联系管理员: 通过平台提交
- GitHub Issues: (如果开源)

---

## 🚀 立即开始

### 3步开始使用

```bash
# 1. 下载
访问AI-ATG平台 → 下载中心 → 下载ATG-Client

# 2. 安装
运行 install.bat (Windows) 或 install.sh (Mac/Linux)

# 3. 使用
打开AI-ATG平台 → 点击"执行测试" → 完成！
```

---

## 📜 许可证

MIT License

---

## 🌟 总结

**ATG-Client** 是AI-ATG平台UI自动化测试的核心：

- ✅ **专业命名** - 品牌统一，易于识别
- ✅ **简单易用** - 一次安装，永久使用
- ✅ **功能强大** - 支持所有Selenium操作
- ✅ **高效稳定** - 自动启动，自动恢复
- ✅ **用户友好** - 托盘图标，桌面通知

**现在就开始使用ATG-Client，体验专业的UI自动化测试！** 🎉

---

**更新时间**: 2026-01-28  
**版本**: 1.0.0  
**官方文档**: `docs/ATG_CLIENT_GUIDE.md`
