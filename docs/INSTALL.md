# AI-ATG 安装指南

## 📋 系统要求

### 必需组件

| 组件 | 版本要求 | 说明 |
|------|---------|------|
| JDK | 17+ | Java运行环境 |
| Maven | 3.8+ | 后端构建工具 |
| Node.js | 16+ | 前端运行环境 |
| npm | 8+ | 前端包管理器 |
| Docker | 20+ | 容器运行环境（可选） |
| Docker Compose | 2.0+ | 服务编排（可选） |

### 推荐配置

- **CPU**: 4核心或更多
- **内存**: 8GB或更多
- **磁盘**: 20GB可用空间
- **操作系统**: macOS/Linux/Windows

---

## 🚀 安装步骤

### 方式一：Docker Compose（推荐）

#### 1. 克隆项目

```bash
git clone https://github.com/your-repo/AI-ATG.git
cd AI-ATG
```

#### 2. 启动基础设施

```bash
docker-compose up -d mysql redis minio
```

等待服务启动（约30秒）。

#### 3. 验证服务

```bash
docker-compose ps
```

确保所有服务状态为 `Up`。

#### 4. 初始化数据库

```bash
# 连接MySQL
docker exec -it aiatg mysql -uroot -pAiatg123456

# 执行初始化脚本
source /docker-entrypoint-initdb.d/init.sql

# 退出
exit
```

或者使用后端自动初始化（推荐）。

#### 5. 启动后端

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

等待后端启动成功（看到 "AI-ATG Backend Started Successfully!"）。

#### 6. 安装前端依赖

```bash
cd frontend
npm install

# 安装额外依赖
npm install echarts --save
npm install @vueuse/core --save
```

#### 7. 启动前端

```bash
npm run dev
```

#### 8. 访问系统

打开浏览器访问：
- **前端**: http://localhost:5173
- **后端**: http://localhost:8080
- **Swagger**: http://localhost:8080/swagger-ui.html

#### 9. 登录系统

使用默认管理员账号：
- 用户名: `admin`
- 密码: `admin123`

---

### 方式二：本地安装

#### 1. 安装MySQL

**macOS**:
```bash
brew install mysql@8.0
brew services start mysql@8.0
```

**Linux**:
```bash
sudo apt-get install mysql-server
sudo systemctl start mysql
```

**Windows**:
下载并安装 MySQL 8.0 安装包。

#### 2. 安装Redis

**macOS**:
```bash
brew install redis
brew services start redis
```

**Linux**:
```bash
sudo apt-get install redis-server
sudo systemctl start redis
```

**Windows**:
下载并安装 Redis for Windows。

#### 3. 安装MinIO（可选）

**macOS/Linux**:
```bash
wget https://dl.min.io/server/minio/release/linux-amd64/minio
chmod +x minio
./minio server /data --console-address ":9001"
```

**或使用Docker**:
```bash
docker run -d \
  -p 9000:9000 \
  -p 9001:9001 \
  -e "MINIO_ROOT_USER=minioadmin" \
  -e "MINIO_ROOT_PASSWORD=minioadmin" \
  minio/minio server /data --console-address ":9001"
```

#### 4. 配置数据库

```sql
-- 创建数据库
CREATE DATABASE aiatg CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户
CREATE USER 'aiatg'@'%' IDENTIFIED BY 'Aiatg123456';
GRANT ALL PRIVILEGES ON aiatg.* TO 'aiatg'@'%';
FLUSH PRIVILEGES;
```

#### 5. 修改配置文件

编辑 `backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/aiatg
    username: aiatg
    password: Aiatg123456
  
  redis:
    host: localhost
    port: 6379
    password: Aiatg123456
```

#### 6-9. 同方式一的步骤5-8

---

## 🔧 配置说明

### 后端配置

**application.yml 主要配置项**:

```yaml
# 服务器配置
server:
  port: 8080

# 数据源配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/aiatg
    username: aiatg
    password: Aiatg123456
    driver-class-name: com.mysql.cj.jdbc.Driver

# Redis配置
spring:
  redis:
    host: localhost
    port: 6379
    password: Aiatg123456
    database: 0

# MinIO配置
minio:
  endpoint: http://localhost:9000
  accessKey: minioadmin
  secretKey: minioadmin
  bucketName: aiatg-files

# JWT配置
jwt:
  secret: your-secret-key-change-in-production
  expiration: 86400000  # 24小时
```

### 前端配置

**vite.config.js 主要配置**:

```javascript
export default {
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
}
```

---

## 🔐 AI配置

### 获取AI API密钥

#### DeepSeek

1. 访问 https://platform.deepseek.com
2. 注册并登录
3. 创建API密钥
4. 在系统中配置

#### 阿里千问

1. 访问阿里云控制台
2. 开通千问服务
3. 创建API密钥
4. 在系统中配置

#### 智谱AI

1. 访问 https://open.bigmodel.cn
2. 注册并登录
3. 获取API密钥
4. 在系统中配置

---

## ✅ 安装验证

### 1. 检查后端

访问 http://localhost:8080/actuator/health

应返回：
```json
{
  "status": "UP"
}
```

### 2. 检查前端

访问 http://localhost:5173

应显示登录页面。

### 3. 检查Swagger

访问 http://localhost:8080/swagger-ui.html

应显示API文档页面。

### 4. 测试登录

使用默认账号登录：
- 用户名: admin
- 密码: admin123

登录成功应跳转到Dashboard。

---

## 🐛 常见问题

### Q1: 后端启动失败

**错误**: `Cannot connect to MySQL`

**解决**:
```bash
# 检查MySQL是否运行
docker ps | grep mysql
# 或
mysql -uroot -p

# 检查数据库是否存在
mysql -uroot -p -e "SHOW DATABASES LIKE 'aiatg';"
```

### Q2: 前端无法连接后端

**错误**: `Network Error`

**解决**:
1. 确认后端已启动（http://localhost:8080）
2. 检查 `vite.config.js` 代理配置
3. 检查浏览器控制台错误

### Q3: npm install 失败

**错误**: `EPERM` 或权限问题

**解决**:
```bash
# macOS/Linux
sudo npm install

# 或使用cnpm
npm install -g cnpm --registry=https://registry.npmmirror.com
cnpm install
```

### Q4: ECharts 图表不显示

**原因**: 未安装 echarts

**解决**:
```bash
cd frontend
npm install echarts --save
```

### Q5: MinIO 无法访问

**解决**:
```bash
# 检查MinIO是否运行
docker ps | grep minio

# 访问MinIO控制台
# http://localhost:9001
# 用户名: minioadmin
# 密码: minioadmin
```

---

## 🔄 升级指南

### 从源码更新

```bash
git pull origin main
cd backend
mvn clean install
cd ../frontend
npm install
```

### 数据库迁移

```bash
# 执行新的迁移脚本
mysql -uaiatg -pAiatg123456 aiatg < backend/src/main/resources/db/migration/V*.sql
```

---

## 🗑️ 卸载

### 停止服务

```bash
# 停止Docker服务
docker-compose down

# 删除数据（可选）
docker-compose down -v
```

### 删除数据库

```sql
DROP DATABASE aiatg;
DROP USER 'aiatg'@'%';
```

---

## 📞 技术支持

遇到安装问题：

1. 查看 [QUICK_START.md](QUICK_START.md)
2. 查看 [USER_MANUAL.md](USER_MANUAL.md) 常见问题
3. 检查后端日志
4. 检查浏览器控制台
5. 联系技术支持

**联系方式**:
- 邮箱: 18301545237@163.com

---

## ✨ 安装检查清单

安装完成后，请检查以下项目：

- [ ] MySQL服务运行正常
- [ ] Redis服务运行正常
- [ ] MinIO服务运行正常（可选）
- [ ] 后端服务启动成功
- [ ] 前端页面可以访问
- [ ] 可以成功登录
- [ ] Dashboard页面正常显示
- [ ] 可以创建项目/需求/用例
- [ ] AI配置页面可访问
- [ ] Swagger文档可访问

---

**最后更新：2026-01-27**

**版本：v1.0**

**许可证：Apache License 2.0**
