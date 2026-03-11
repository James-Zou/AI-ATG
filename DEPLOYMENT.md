# AI-ATG 前后端分离部署指南

## 📦 打包方式说明

### 后端打包（输出 tar.gz）

```bash
cd backend
mvn clean package
```

**打包产物**：
- `target/ai-atg-backend-1.0.0.tar.gz` - 完整部署包

**包含内容**：
```
ai-atg-backend-1.0.0/
├── bin/
│   └── ai-atg.sh           # 服务启动/停止脚本
├── config/
│   ├── application.yml      # 主配置文件
│   └── logback-spring.xml   # 日志配置
└── lib/
    ├── ai-atg-backend-1.0.0.jar  # 主程序 JAR
    └── *.jar                     # 所有依赖 JAR
```

### 前端打包（输出 dist 目录）

```bash
cd frontend
npm install
npm run build
```

**打包产物**：
- `dist/` 目录 - 包含所有静态资源（HTML、CSS、JS、图片等）

---

## 🚀 部署步骤

### 1. 后端部署

#### 1.1 上传并解压
```bash
# 上传 tar.gz 到服务器
scp target/ai-atg-backend-1.0.0.tar.gz user@10.0.13.217:/data/app/

# 登录服务器
ssh user@10.0.13.217

# 解压
cd /data/app
tar -xzf ai-atg-backend-1.0.0.tar.gz
cd ai-atg-backend-1.0.0
```

#### 1.2 修改配置（可选）
```bash
vim config/application.yml
```

#### 1.3 启动服务
```bash
# 启动
./bin/ai-atg.sh start

# 查看状态
./bin/ai-atg.sh status

# 查看日志
tail -f logs/ai-atg.log

# 停止服务
./bin/ai-atg.sh stop
```

**后端访问地址**：`http://10.0.13.217:19080/agt/api`

---

### 2. 前端部署（Nginx）

#### 2.1 上传前端静态文件
```bash
# 上传 dist 目录到服务器
cd frontend
scp -r dist/* user@10.0.13.217:/usr/share/nginx/html/ai-atg/
```

#### 2.2 配置 Nginx

创建或编辑 Nginx 配置文件 `/etc/nginx/conf.d/ai-atg.conf`：

```nginx
server {
    listen 19081;
    server_name 10.0.13.217;  # 或您的域名

    # 前端静态文件目录
    root /data/app/ai-atg-front;
    index index.html;

    # Gzip 压缩
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript;

    # 前端路由 - SPA 支持
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 代理后端 API 请求
    location /api/ {
        proxy_pass http://127.0.0.1:19080/agt/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # WebSocket 支持（如果需要）
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        
        # 超时设置
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # 下载文件代理
    location /downloads/ {
        proxy_pass http://127.0.0.1:19080/agt/api/downloads/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

#### 2.3 重启 Nginx
```bash
# 测试配置
sudo nginx -t

# 重启 Nginx
sudo systemctl restart nginx

# 或
sudo nginx -s reload
```

**前端访问地址**：`http://10.0.13.217`

---

## 📝 快速验证

### 验证后端
```bash
# 测试后端健康
curl http://10.0.13.217:19080/agt/api/

# 测试登录接口
curl -X POST http://10.0.13.217:19080/agt/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"your_password"}'
```

### 验证前端
```bash
# 在浏览器访问
http://10.0.13.217
```

---

## 🔧 本地开发模式

### 后端
```bash
cd backend
mvn spring-boot:run
```

### 前端
```bash
cd frontend
npm run dev
```

访问：`http://localhost:3000`

---

## 📂 目录结构对比

### 打包前
```
AI-ATG/
├── backend/          # 后端代码
├── frontend/         # 前端代码
└── DEPLOYMENT.md     # 本文档
```

### 打包后
```
打包产物/
├── ai-atg-backend-1.0.0.tar.gz  # 后端部署包
└── frontend/dist/                # 前端静态资源
    ├── index.html
    ├── assets/
    └── ...
```

---

## ⚙️ 环境变量配置

### 前端环境变量（可选）

如果需要配置不同环境的后端地址，可以创建：

**`.env.production`** (生产环境)：
```bash
VITE_API_BASE_URL=http://10.0.13.217:19080/agt/api
```

然后修改 `src/api/request.js`：
```javascript
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000
})
```

---

## 🎯 总结

| 组件 | 打包命令 | 产物 | 部署位置 |
|------|----------|------|----------|
| 后端 | `mvn clean package` | `ai-atg-backend-1.0.0.tar.gz` | `/data/app/` |
| 前端 | `npm run build` | `dist/` 目录 | `/usr/share/nginx/html/ai-atg/` |

**访问地址**：
- 前端：`http://10.0.13.217`
- 后端：`http://10.0.13.217:19080/agt/api`

---

## ❓ 常见问题

### Q1: 403 Forbidden 错误？
**A**: 检查 Nginx 文件权限和 SELinux 配置：
```bash
sudo chown -R nginx:nginx /usr/share/nginx/html/ai-atg
sudo chmod -R 755 /usr/share/nginx/html/ai-atg

# 如果启用了 SELinux
sudo setenforce 0  # 临时关闭
```

### Q2: API 请求失败？
**A**: 检查后端服务是否运行，以及 Nginx 代理配置是否正确：
```bash
# 检查后端服务
curl http://127.0.0.1:19080/agt/api/

# 检查 Nginx 配置
sudo nginx -t
```

### Q3: 路由刷新 404？
**A**: 确保 Nginx 配置了 `try_files $uri $uri/ /index.html;` 来支持 Vue Router 的 History 模式。
