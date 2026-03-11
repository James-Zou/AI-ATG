# UI 测试 ATG-Client 集成说明

## 概述

本次修改将 UI 自动化测试从后端服务器直接执行 Selenium 的模式，改为通过本地 ATG-Client 执行的模式。这样更加合理，因为：

1. **浏览器在用户本地运行**：测试在用户的真实环境中执行，更接近实际使用场景
2. **资源占用合理**：服务器不需要安装浏览器和驱动，节省资源
3. **跨平台支持**：用户可以在 Windows、macOS、Linux 上运行测试
4. **可视化调试**：用户可以看到浏览器执行过程（非 headless 模式）

## 修改内容

### 1. 后端修改

#### 1.1 创建异步执行服务

**新增文件：**
- `AsyncTestExecutionService.java` - 异步执行服务接口
- `AsyncTestExecutionServiceImpl.java` - 异步执行服务实现

**原因：** 解决 Spring `@Async` 注解在同类方法调用时不生效的问题。

#### 1.2 修改 UiTestExecutor

**文件：** `backend/src/main/java/com/aiatg/executor/impl/UiTestExecutor.java`

**主要变更：**
```java
// 从直接使用 Selenium
@Autowired
private WebDriverFactory webDriverFactory;
@Autowired
private UiActionExecutor actionExecutor;

// 改为调用 ATG-Client API
@Value("${atg.client.url:http://localhost:9999}")
private String atgClientUrl;
@Value("${atg.client.timeout:60000}")
private int timeout;
```

**执行流程：**
1. 检查 ATG-Client 健康状态 (`/health`)
2. 发送测试任务到 ATG-Client (`POST /execute`)
3. 轮询执行状态 (`GET /status`)
4. 获取执行结果（包括截图、日志等）
5. 保存结果到数据库

#### 1.3 配置文件修改

**文件：** `backend/src/main/resources/application.yml`

**新增配置：**
```yaml
# ATG-Client 配置
atg:
  client:
    url: http://localhost:9999
    timeout: 60000  # 60秒超时
```

### 2. ATG-Client 修改

#### 2.1 修改 index.js

**文件：** `agent-service/src/index.js`

**主要变更：**

1. **解析测试步骤：** 支持从字符串解析 JSON 格式的测试步骤
   ```javascript
   let steps = testCase.steps;
   if (typeof steps === 'string') {
     steps = JSON.parse(steps);
   }
   ```

2. **保存执行结果：** 将结果保存到 `currentExecution.result`，供后端轮询获取
   ```javascript
   currentExecution.result = result;
   ```

3. **增强状态接口：** `/status` 接口返回更详细的状态信息
   ```javascript
   {
     executing: false,
     completed: true,
     result: { status, duration, logs, screenshot }
   }
   ```

## 部署说明

### 1. 后端部署

```bash
cd backend
mvn clean package
java -jar target/ai-atg-backend.jar
```

### 2. ATG-Client 部署

#### 方式 1：开发环境（直接运行）

```bash
cd agent-service
npm install
node src/index.js
```

#### 方式 2：打包安装

```bash
# macOS/Linux
cd agent-service
sh install.sh

# Windows
cd agent-service
install.bat
```

### 3. 配置 WebDriver

ATG-Client 需要 ChromeDriver 或 GeckoDriver，首次运行时需要配置：

```bash
# 配置文件位置
~/.atg-client/config.json

# 配置示例
{
  "serverUrl": "http://localhost:19080",
  "browser": "chrome",
  "headless": false,
  "chromeDriverPath": "/path/to/chromedriver",
  "geckoDriverPath": "/path/to/geckodriver"
}
```

**下载 ChromeDriver：**
- 官网：https://chromedriver.chromium.org/downloads
- 或使用安装脚本自动下载

## 使用流程

### 1. 启动服务

```bash
# 1. 启动后端（端口 19080）
cd backend && java -jar target/ai-atg-backend.jar

# 2. 启动 ATG-Client（端口 9999）
cd agent-service && node src/index.js
```

### 2. 创建 UI 测试用例

在前端界面中创建 UI 测试用例，配置测试步骤：

```json
[
  {
    "action": "open",
    "value": "https://example.com"
  },
  {
    "action": "input",
    "locator": "id",
    "value": "username",
    "input": "testuser"
  },
  {
    "action": "click",
    "locator": "css",
    "value": "button[type=submit]"
  },
  {
    "action": "asserttitle",
    "input": "Welcome"
  }
]
```

### 3. 执行测试

1. 确保 ATG-Client 已启动（可以访问 http://localhost:9999/health）
2. 在前端选择 UI 测试用例
3. 点击"执行"按钮
4. 系统会：
   - 检查 ATG-Client 是否在线
   - 发送测试任务到本地客户端
   - 本地浏览器自动打开并执行测试
   - 收集执行结果（日志、截图等）
   - 显示执行结果

### 4. 查看结果

- **执行列表：** 查看所有测试执行记录
- **执行详情：** 查看单次执行的详细信息
  - 执行日志
  - 截图
  - 每个步骤的执行状态
  - 执行时间统计

## 故障排查

### 1. ATG-Client 连接失败

**错误信息：**
```
ATG-Client 不可用，请确保本地客户端已启动 (http://localhost:9999)
```

**解决方法：**
1. 检查 ATG-Client 是否启动
   ```bash
   curl http://localhost:9999/health
   ```
2. 检查防火墙是否阻止端口 9999
3. 检查 backend 配置的 `atg.client.url` 是否正确

### 2. WebDriver 配置错误

**错误信息：**
```
ChromeDriver 未找到: /path/to/chromedriver
```

**解决方法：**
1. 下载对应版本的 ChromeDriver
2. 配置正确的路径到 `~/.atg-client/config.json`
3. 确保文件有执行权限
   ```bash
   chmod +x /path/to/chromedriver
   ```

### 3. 测试步骤解析失败

**错误信息：**
```
测试步骤格式错误
```

**解决方法：**
1. 检查测试用例的 `steps` 字段是否是有效的 JSON 数组
2. 确保每个步骤包含必要的字段（action、locator、value 等）
3. 使用 JSON 验证工具检查格式

### 4. 执行超时

**错误信息：**
```
执行超时，超过 60 秒未完成
```

**解决方法：**
1. 增加超时配置
   ```yaml
   atg:
     client:
       timeout: 120000  # 改为 120 秒
   ```
2. 优化测试步骤，减少等待时间
3. 检查网络连接是否正常

## API 参考

### ATG-Client API

#### 1. 健康检查

```http
GET /health
```

**响应：**
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

#### 2. 执行测试

```http
POST /execute
Content-Type: application/json

{
  "testCase": {
    "id": 1,
    "title": "登录测试",
    "steps": [...],
    "executionId": 123
  }
}
```

**响应：**
```json
{
  "success": true,
  "message": "测试已开始执行"
}
```

#### 3. 查询状态

```http
GET /status
```

**响应（执行中）：**
```json
{
  "executing": true,
  "completed": false,
  "current": {
    "testCaseId": 1,
    "testCaseTitle": "登录测试",
    "startTime": 1234567890
  }
}
```

**响应（已完成）：**
```json
{
  "executing": false,
  "completed": true,
  "result": {
    "status": "passed",
    "duration": 5000,
    "logs": "测试执行日志...",
    "screenshot": "base64_encoded_image",
    "errorMessage": null
  }
}
```

#### 4. 停止执行

```http
POST /stop
```

**响应：**
```json
{
  "success": true,
  "message": "测试已停止"
}
```

## 性能优化建议

### 1. 使用 Headless 模式

对于 CI/CD 环境，建议使用 headless 模式：

```json
{
  "headless": true
}
```

### 2. 复用浏览器实例

对于连续执行多个测试用例，可以考虑复用浏览器实例，减少启动时间。

### 3. 并行执行

如果有多台机器，可以部署多个 ATG-Client，实现分布式并行执行。

## 后续改进计划

1. **结果上传：** ATG-Client 主动上传结果到服务器，而不是被动轮询
2. **WebSocket 通信：** 使用 WebSocket 实现实时状态推送
3. **多客户端管理：** 支持管理多个 ATG-Client，实现负载均衡
4. **录屏功能：** 支持录制测试执行过程的视频
5. **远程调试：** 支持在浏览器中远程查看测试执行画面

## 版本信息

- **修改日期：** 2026-01-28
- **版本：** v1.0.0
- **修改人：** AI Assistant

---

**相关文档：**
- [ATG-Client 部署指南](./AGENT_DEPLOYMENT_GUIDE.md)
- [UI 自动化测试完整指南](./UI_AUTOMATION_COMPLETE_GUIDE.md)
- [浏览器插件方案](./BROWSER_EXTENSION_SOLUTION.md)
