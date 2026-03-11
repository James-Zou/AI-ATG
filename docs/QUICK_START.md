# AI-ATG 快速启动指南

## 🎉 Phase 1 已完成！

用户和权限管理功能已全部实现，现在可以启动项目进行测试。

---

## 🚀 5分钟快速启动

### 步骤 1：启动基础设施（2分钟）

```bash
# 进入项目目录
cd /Users/roderickzou/Desktop/AI-ATG

# 启动 MySQL 和 Redis
docker-compose up -d mysql redis

# 等待服务启动（大约30秒）
sleep 30

# 验证服务状态
docker-compose ps
```

**预期输出**：
```
NAME              IMAGE         STATUS
ai-atg-mysql       mysql:8.0     Up 30 seconds (healthy)
ai-atg-redis       redis:7       Up 30 seconds
```

### 步骤 2：启动后端（1分钟）

```bash
# 进入后端目录
cd backend

# 编译并启动（首次需要下载依赖，可能需要2-3分钟）
mvn spring-boot:run

# 或者使用 IDE（IntelliJ IDEA）直接运行 AiAtgApplication
```

**预期输出**：
```
AI-ATG Backend Started Successfully!
```

**验证**：访问 http://localhost:8080/api （应该返回 403 或 404，表示服务正常）

### 步骤 3：启动前端（2分钟）

```bash
# 打开新终端，进入前端目录
cd frontend

# 安装依赖（首次运行）
npm install

# 启动开发服务器
npm run dev
```

**预期输出**：
```
VITE v5.0.0  ready in 500 ms

➜  Local:   http://localhost:3000/
➜  Network: use --host to expose
```

**验证**：访问 http://localhost:3000 （应该自动跳转到登录页）

---

## ✅ 功能验证清单

### 1. 用户注册 ✅

**步骤**：
1. 打开浏览器访问：http://localhost:3000/register
2. 填写表单：
   - 用户名：`testuser`
   - 密码：`123456`
   - 确认密码：`123456`
   - 昵称：`测试用户`（选填）
3. 点击"注册"按钮

**预期结果**：
- 显示"注册成功，请登录"
- 自动跳转到登录页面

### 2. 用户登录 ✅

**步骤**：
1. 在登录页面输入：
   - 用户名：`testuser`
   - 密码：`123456`
2. 点击"登录"按钮

**预期结果**：
- 显示"登录成功"
- 跳转到 Dashboard
- 右上角显示用户名

### 3. Dashboard 验证 ✅

**验证点**：
- 左侧显示导航菜单
- 顶部显示"AI-ATG 自动化测试平台"
- 右上角显示用户名和下拉菜单
- 中间显示欢迎信息和统计数据

### 4. 用户管理 ✅

**步骤**：
1. 点击左侧菜单"用户管理"
2. 查看用户列表
3. 点击"查看"按钮查看用户详情
4. 点击"刷新"按钮

**预期结果**：
- 显示所有注册用户
- 可以查看用户详细信息
- 列表可以刷新

### 5. 路由守卫验证 ✅

**步骤**：
1. 点击右上角用户名下拉菜单
2. 点击"退出登录"
3. 在浏览器地址栏直接访问：http://localhost:3000/dashboard

**预期结果**：
- 退出后清除登录状态
- 访问受保护页面自动跳转到登录页
- 提示"请先登录"

---

## 🎯 API 测试（可选）

使用 curl 或 Postman 测试 API：

### 1. 注册 API

```bash
curl -X POST http://localhost:8080/api/user/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "apitest",
    "password": "123456",
    "nickname": "API测试用户"
  }'
```

**预期响应**：
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "id": 2,
    "username": "apitest",
    "nickname": "API测试用户",
    "role": "tester",
    "status": 1
  }
}
```

### 2. 登录 API

```bash
curl -X POST http://localhost:8080/api/user/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "apitest",
    "password": "123456"
  }'
```

**预期响应**：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "userInfo": {
      "id": 2,
      "username": "apitest",
      ...
    }
  }
}
```

### 3. 获取用户列表（需要 Token）

```bash
# 将上一步获取的 token 替换 YOUR_TOKEN
curl -X GET http://localhost:8080/api/user/list \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 🐛 故障排查

### 问题1：后端启动失败

**错误信息**：`Cannot connect to database`

**解决方案**：
```bash
# 检查 MySQL 是否运行
docker ps | grep mysql

# 如果没有运行，启动它
docker-compose up -d mysql

# 查看 MySQL 日志
docker logs ai-atg-mysql
```

### 问题2：前端无法连接后端

**错误信息**：`Network Error` 或 `CORS Error`

**解决方案**：
1. 确认后端已启动（http://localhost:8080/api）
2. 检查前端端口是否为 3000 或 5173（已配置 CORS）
3. 检查浏览器控制台的错误信息

### 问题3：登录后立即退出

**原因**：Token 验证失败

**解决方案**：
1. 打开浏览器开发者工具（F12）
2. 查看 Application -> Local Storage
3. 确认 token 和 userInfo 是否存在
4. 清除缓存后重新登录

### 问题4：密码错误

**错误信息**：`用户名或密码错误`

**解决方案**：
- 确认密码至少6位
- 检查用户名是否正确（区分大小写）
- 如果忘记密码，可以重新注册新账号

---

## 📊 性能指标

### 启动时间
- MySQL: ~30秒
- Redis: ~5秒
- 后端: ~20秒（首次编译可能需要2-3分钟）
- 前端: ~5秒

### 响应时间
- 注册接口: <200ms
- 登录接口: <300ms
- 查询接口: <100ms

---

## 🎓 下一步学习

恭喜你完成了 Phase 1 的部署和测试！

接下来可以：

1. **阅读代码**
   - 后端：查看 `backend/src/main/java/com/aiatg/` 下的代码
   - 前端：查看 `frontend/src/` 下的代码

2. **修改配置**
   - 后端配置：`backend/src/main/resources/application.yml`
   - 前端配置：`frontend/vite.config.js`

3. **开发新功能**
   - 参考：`DEVELOPMENT_GUIDE.md`
   - 任务清单：`TODO.md`
   - 下一个 Phase：需求管理

4. **查看详细文档**
   - 完成报告：`PHASE1_COMPLETION_REPORT.md`
   - 项目说明：`README.md`
   - 架构设计：`PLATFORM_ARCHITECTURE.md`

---

## 💡 提示

### 开发模式
- 后端热重载：使用 `spring-boot-devtools`
- 前端热重载：Vite 自动支持

### 调试技巧
- 后端日志：查看控制台输出
- 前端调试：Chrome DevTools（F12）
- 网络请求：查看 Network 标签
- 状态管理：使用 Vue DevTools

### 数据库查看
```bash
# 进入 MySQL 容器
docker exec -it ai-atg-mysql mysql -uroot -pAiatg123456

# 查看数据库
USE z_atg;
SHOW TABLES;
SELECT * FROM user;
```

---

## 🎉 恭喜！

你已经成功启动了 AI-ATG 平台，并完成了 Phase 1 的功能验证！

现在可以开始开发 Phase 2（需求管理）的功能了。

如有问题，请查看：
- `DEVELOPMENT_GUIDE.md` - 开发指南
- `PHASE1_COMPLETION_REPORT.md` - 完成报告
- `TODO.md` - 功能清单

---

**祝你开发愉快！** 🚀

最后更新：2026-01-27
