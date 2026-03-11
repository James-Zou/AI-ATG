# AI-ATG API 文档

## 📚 API 概览

**基础URL**: `http://localhost:8080/api`

**认证方式**: JWT Token (Header: `Authorization: Bearer {token}`)

**响应格式**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

---

## 🔐 认证接口

### 1. 用户注册

**POST** `/auth/register`

**请求体**:
```json
{
  "username": "testuser",
  "password": "password123",
  "email": "test@example.com",
  "nickname": "测试用户"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "注册成功",
  "data": null
}
```

### 2. 用户登录

**POST** `/auth/login`

**请求体**:
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "username": "admin",
    "nickname": "管理员"
  }
}
```

---

## 👥 用户管理接口

### 1. 获取用户列表

**GET** `/user/list?pageNum=1&pageSize=10`

### 2. 更新用户状态

**PUT** `/user/{id}/status?status=1`

### 3. 删除用户

**DELETE** `/user/{id}`

---

## 📦 项目管理接口

### 1. 创建项目

**POST** `/project`

**请求体**:
```json
{
  "name": "测试项目",
  "description": "项目描述",
  "status": 1
}
```

### 2. 获取项目列表

**GET** `/project/list?pageNum=1&pageSize=10`

**响应**:
```json
{
  "code": 200,
  "data": {
    "total": 10,
    "records": [
      {
        "id": 1,
        "name": "测试项目",
        "description": "描述",
        "memberCount": 5,
        "requirementCount": 10,
        "testCaseCount": 50,
        "status": 1
      }
    ]
  }
}
```

### 3. 获取项目详情

**GET** `/project/{id}`

### 4. 更新项目

**PUT** `/project/{id}`

### 5. 删除项目

**DELETE** `/project/{id}`

### 6. 添加项目成员

**POST** `/project/{projectId}/members`

**请求体**:
```json
{
  "userId": 2,
  "role": "member"
}
```

### 7. 移除项目成员

**DELETE** `/project/{projectId}/members/{userId}`

### 8. 获取项目成员

**GET** `/project/{projectId}/members`

---

## 📋 需求管理接口

### 1. 创建需求

**POST** `/requirement`

**请求体**:
```json
{
  "projectId": 1,
  "title": "需求标题",
  "description": "需求描述",
  "priority": "high",
  "status": "pending"
}
```

### 2. 获取需求列表

**GET** `/requirement/list?projectId=1&pageNum=1&pageSize=10`

### 3. 获取需求详情

**GET** `/requirement/{id}`

### 4. 更新需求

**PUT** `/requirement/{id}`

### 5. 删除需求

**DELETE** `/requirement/{id}`

### 6. 上传附件

**POST** `/requirement/{id}/upload`

**Content-Type**: `multipart/form-data`

---

## ✅ 测试用例接口

### 1. 创建测试用例

**POST** `/testcase`

**请求体**:
```json
{
  "projectId": 1,
  "suiteId": 1,
  "requirementId": 1,
  "title": "测试用例标题",
  "description": "用例描述",
  "priority": "high",
  "type": "functional",
  "steps": [
    {
      "stepOrder": 1,
      "stepDescription": "步骤1",
      "expectedResult": "预期结果1"
    }
  ]
}
```

### 2. 获取测试用例列表

**GET** `/testcase/list?projectId=1&pageNum=1&pageSize=10`

### 3. 批量删除

**DELETE** `/testcase/batch`

**请求体**:
```json
{
  "ids": [1, 2, 3]
}
```

---

## 📦 测试套件接口

### 1. 创建测试套件

**POST** `/testsuite`

**请求体**:
```json
{
  "projectId": 1,
  "name": "回归测试套件",
  "description": "套件描述"
}
```

### 2. 获取套件列表

**GET** `/testsuite/list?projectId=1`

---

## 🤖 AI 生成接口

### 1. AI配置管理

**POST** `/ai/config`

**请求体**:
```json
{
  "provider": "deepseek",
  "apiKey": "sk-xxxx",
  "apiUrl": "https://api.deepseek.com/v1/chat/completions",
  "model": "deepseek-chat",
  "isEnabled": true
}
```

### 2. 生成测试用例

**POST** `/ai/generate`

**请求体**:
```json
{
  "requirementId": 1,
  "provider": "deepseek"
}
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "historyId": 123,
    "generatedContent": "生成的测试用例内容",
    "testCases": [
      {
        "title": "用例1",
        "description": "描述",
        "steps": [...]
      }
    ]
  }
}
```

### 3. 获取生成历史

**GET** `/ai/generate/history?requirementId=1`

---

## ▶️ 测试执行接口

### 1. 创建并执行测试

**POST** `/execution/create`

**请求体**:
```json
{
  "projectId": 1,
  "suiteId": 1,
  "executionType": "api",
  "environment": "test"
}
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "executionId": 1,
    "status": "running"
  }
}
```

### 2. 获取执行详情

**GET** `/execution/{id}`

**响应**:
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "status": "completed",
    "totalCases": 10,
    "passedCases": 8,
    "failedCases": 2,
    "duration": 120,
    "details": [
      {
        "caseId": 1,
        "caseTitle": "用例1",
        "status": "passed",
        "duration": 10,
        "log": "执行日志"
      }
    ]
  }
}
```

### 3. 停止执行

**POST** `/execution/{id}/stop`

---

## 📊 测试报告接口

### 1. 生成报告

**POST** `/report/generate`

**请求体**:
```json
{
  "executionId": 1
}
```

### 2. 获取报告详情

**GET** `/report/{id}`

### 3. 导出HTML报告

**GET** `/report/{id}/export/html`

**响应**: HTML内容

### 4. 获取统计数据

**GET** `/report/statistics?projectId=1&days=30`

**响应**:
```json
{
  "code": 200,
  "data": {
    "totalExecutions": 100,
    "totalPassed": 80,
    "totalFailed": 20,
    "passRate": 80.0,
    "trendData": [
      {
        "date": "2026-01-01",
        "total": 10,
        "passed": 8,
        "failed": 2,
        "passRate": 80.0
      }
    ],
    "topFailedCases": [
      {
        "caseId": 1,
        "caseTitle": "用例1",
        "failedCount": 5
      }
    ]
  }
}
```

---

## 🔗 GitLab 集成接口

### 1. 保存GitLab配置

**POST** `/gitlab/config`

**请求体**:
```json
{
  "gitlabUrl": "https://gitlab.com",
  "accessToken": "glpat-xxxx",
  "webhookSecret": "secret123",
  "branchName": "main",
  "autoTrigger": true
}
```

### 2. Webhook接收

**POST** `/gitlab/webhook`

**Headers**:
- `X-Gitlab-Token`: webhook密钥
- `X-Gitlab-Event`: Push Hook

**请求体**: GitLab Webhook Payload

### 3. 获取Webhook记录

**GET** `/gitlab/webhook-records?pageNum=1&pageSize=20`

---

## ⚙️ 系统管理接口

### 1. 保存系统配置

**POST** `/system/config`

**请求体**:
```json
{
  "key": "system.name",
  "value": "AI-ATG",
  "type": "string",
  "description": "系统名称"
}
```

### 2. 获取配置

**GET** `/system/config/{key}`

### 3. 获取所有配置

**GET** `/system/config/list`

### 4. 获取操作日志

**GET** `/system/logs?pageNum=1&pageSize=20`

**响应**:
```json
{
  "code": 200,
  "data": {
    "total": 100,
    "records": [
      {
        "id": 1,
        "userId": 1,
        "username": "admin",
        "operation": "创建项目",
        "method": "POST /api/project",
        "params": "{}",
        "ip": "127.0.0.1",
        "executionTime": 150,
        "createdTime": "2026-01-27T10:00:00"
      }
    ]
  }
}
```

---

## 📝 通用参数

### 分页参数

| 参数 | 类型 | 必填 | 说明 | 默认值 |
|------|------|------|------|--------|
| pageNum | Integer | 否 | 页码 | 1 |
| pageSize | Integer | 否 | 每页数量 | 10 |

### 状态码

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器错误 |

---

## 🔒 认证说明

### JWT Token

**获取Token**: 通过登录接口获取

**使用Token**: 在请求头中添加
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Token过期**: 24小时

**刷新Token**: 重新登录获取

---

## 🎯 Swagger 文档

访问交互式API文档：

**URL**: `http://localhost:8080/swagger-ui.html`

**功能**:
- 📖 查看所有API接口
- 🧪 在线测试接口
- 📝 查看请求/响应模型
- 💡 查看接口说明

---

## 💡 最佳实践

### 1. 错误处理

```javascript
try {
  const res = await api.createProject(data)
  if (res.code === 200) {
    // 成功处理
  } else {
    // 错误处理
    ElMessage.error(res.message)
  }
} catch (error) {
  ElMessage.error('网络错误')
}
```

### 2. 请求拦截

```javascript
// 添加Token
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})
```

### 3. 响应拦截

```javascript
// 统一错误处理
request.interceptors.response.use(
  response => response.data,
  error => {
    if (error.response?.status === 401) {
      // Token过期，跳转登录
      router.push('/login')
    }
    return Promise.reject(error)
  }
)
```

---

## 🔄 数据流转

### 测试用例生命周期

```
1. 创建需求 (POST /requirement)
   ↓
2. AI生成用例 (POST /ai/generate)
   ↓
3. 保存用例 (POST /testcase)
   ↓
4. 组织套件 (POST /testsuite)
   ↓
5. 执行测试 (POST /execution/create)
   ↓
6. 生成报告 (POST /report/generate)
   ↓
7. 查看统计 (GET /report/statistics)
```

---

## 📊 批量操作

### 批量创建测试用例

**POST** `/testcase/batch`

**请求体**:
```json
{
  "testCases": [
    {
      "title": "用例1",
      "description": "描述1",
      "steps": [...]
    },
    {
      "title": "用例2",
      "description": "描述2",
      "steps": [...]
    }
  ]
}
```

### 批量删除

**DELETE** `/testcase/batch`

**请求体**:
```json
{
  "ids": [1, 2, 3, 4, 5]
}
```

---

## 🚨 限流说明

**API限流策略**:
- 普通接口: 100次/分钟
- AI生成: 10次/分钟
- 文件上传: 20次/分钟

**超出限流**:
```json
{
  "code": 429,
  "message": "请求过于频繁，请稍后再试"
}
```

---

## 📖 示例代码

### JavaScript/Axios

```javascript
import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000
})

// 登录
const login = async (username, password) => {
  const res = await api.post('/auth/login', {
    username,
    password
  })
  
  if (res.data.code === 200) {
    localStorage.setItem('token', res.data.data.token)
    return res.data.data
  }
  throw new Error(res.data.message)
}

// 获取需求列表
const getRequirements = async (projectId, pageNum = 1) => {
  api.defaults.headers.common['Authorization'] = 
    `Bearer ${localStorage.getItem('token')}`
  
  const res = await api.get('/requirement/list', {
    params: { projectId, pageNum, pageSize: 10 }
  })
  
  return res.data.data
}
```

### Java/RestTemplate

```java
RestTemplate restTemplate = new RestTemplate();

// 登录
LoginRequest loginRequest = new LoginRequest("admin", "admin123");
ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
    "http://localhost:8080/api/auth/login",
    loginRequest,
    LoginResponse.class
);

String token = response.getBody().getData().getToken();

// 获取需求列表
HttpHeaders headers = new HttpHeaders();
headers.set("Authorization", "Bearer " + token);
HttpEntity<String> entity = new HttpEntity<>(headers);

ResponseEntity<PageResult> requirements = restTemplate.exchange(
    "http://localhost:8080/api/requirement/list?projectId=1",
    HttpMethod.GET,
    entity,
    PageResult.class
);
```

### Python/Requests

```python
import requests

BASE_URL = "http://localhost:8080/api"

# 登录
response = requests.post(f"{BASE_URL}/auth/login", json={
    "username": "admin",
    "password": "admin123"
})
token = response.json()["data"]["token"]

# 获取需求列表
headers = {"Authorization": f"Bearer {token}"}
response = requests.get(
    f"{BASE_URL}/requirement/list",
    params={"projectId": 1, "pageNum": 1},
    headers=headers
)
requirements = response.json()["data"]
```

---

## 🔍 调试技巧

### 1. 查看请求日志

后端日志会记录所有请求：
```
2026-01-27 10:00:00 INFO  [http-nio-8080-exec-1] c.z.c.UserController - 用户登录: admin
```

### 2. 使用Postman

1. 导入API集合
2. 设置环境变量 `{{baseUrl}}` = `http://localhost:8080/api`
3. 设置Token变量 `{{token}}`
4. 测试接口

### 3. 使用Swagger UI

访问 `http://localhost:8080/swagger-ui.html`:
1. 点击接口
2. 点击"Try it out"
3. 填写参数
4. 点击"Execute"
5. 查看响应

---

## 📞 技术支持

遇到API问题：

1. 查看Swagger文档
2. 查看后端日志
3. 查看浏览器控制台
4. 联系技术支持

**联系方式**:
- 邮箱: 18301545237@163.com
- 文档: [USER_MANUAL.md](USER_MANUAL.md)

---

**最后更新：2026-01-27**

**版本：v1.0**

**许可证：Apache License 2.0**
