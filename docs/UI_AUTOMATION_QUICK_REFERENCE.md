# UI自动化测试快速参考指南

## 📝 一句话总结

**安装一次，永久可用！在Web页面直接点击执行UI自动化测试，无需任何手动操作。**

---

## 🚀 快速开始（3步骤）

### 步骤1：安装服务（一次性，3分钟）

1. 访问平台 → 点击 **下载中心**
2. 下载对应系统的安装包
3. 运行 `install.bat` (Windows) 或 `./install.sh` (Mac/Linux)

✅ **完成！** 服务自动启动并开机自启

### 步骤2：创建测试（1分钟）

在 **测试用例** 页面创建用例，测试步骤格式：

```json
[
  {"action": "open", "input": "http://example.com"},
  {"action": "input", "locator": "id", "value": "username", "input": "admin"},
  {"action": "click", "locator": "id", "value": "submit"}
]
```

### 步骤3：执行测试（1秒）

点击 **执行** 按钮 → ✨ 浏览器自动执行

---

## 📖 常用操作

### 导航
```json
{"action": "open", "input": "http://example.com"}
```

### 输入
```json
{"action": "input", "locator": "id", "value": "username", "input": "admin"}
```

### 点击
```json
{"action": "click", "locator": "id", "value": "submit-btn"}
```

### 等待
```json
{"action": "wait", "timeout": 3}
```

### 验证
```json
{"action": "assertText", "locator": "css", "value": ".message", "input": "成功"}
```

---

## 🔍 元素定位

| 定位器 | 示例 |
|--------|------|
| id | `{"locator": "id", "value": "username"}` |
| css | `{"locator": "css", "value": ".btn-primary"}` |
| xpath | `{"locator": "xpath", "value": "//button[@type='submit']"}` |

**获取定位器**：按F12 → 选择元素 → 右键 → Copy selector

---

## ⚙️ 配置

配置文件：`~/.ai-atg-service/config.json`

```json
{
  "browser": "chrome",
  "headless": false,  // false=可见，true=后台
  "serverUrl": "http://localhost:19080"
}
```

---

## 🔧 故障排查

| 问题 | 解决方案 |
|------|---------|
| 服务未运行 | 检查托盘图标，访问 localhost:9999/health |
| 元素找不到 | 用F12验证定位器，增加等待时间 |
| 浏览器启动失败 | 确认已安装Chrome/Firefox |

---

## 📱 快速链接

- **帮助文档**: 平台 → 帮助中心 → UI测试指南
- **下载中心**: 平台 → 帮助中心 → 下载中心
- **服务状态**: http://localhost:9999/health

---

## 💡 最佳实践

1. **优先使用 id 定位器** - 最快最稳定
2. **合理添加等待** - 页面跳转后加 wait
3. **充分使用断言** - 验证关键步骤
4. **保持步骤简洁** - 一个步骤一个操作

---

## 📊 完整示例

```json
[
  {"action": "open", "input": "http://localhost:3000/login"},
  {"action": "wait", "timeout": 2},
  {"action": "input", "locator": "id", "value": "username", "input": "admin"},
  {"action": "input", "locator": "id", "value": "password", "input": "Admin@123"},
  {"action": "click", "locator": "css", "value": "button[type='submit']"},
  {"action": "wait", "timeout": 3},
  {"action": "assertUrl", "input": "/dashboard"},
  {"action": "assertText", "locator": "css", "value": ".welcome", "input": "欢迎"}
]
```

---

## 🎯 核心优势

✅ **零手动操作** - 安装后自动启动，永久可用  
✅ **Web直接调用** - 页面点击即执行  
✅ **功能完整** - 支持所有Selenium操作  
✅ **用户友好** - 托盘图标、桌面通知  
✅ **服务器零负担** - 不需要安装浏览器  

---

## 📞 获取帮助

1. 平台 → 帮助中心 → UI测试指南
2. 平台 → 帮助中心 → FAQ
3. 查看服务日志
4. 联系管理员

---

**Happy Testing!** 🎉
