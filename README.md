# AI-ATG 自动化测试平台

**AI-ATG (AI-Automatic Test Generation)** 是一个基于 AI 的智能测试平台，旨在帮助测试团队自动生成测试用例并执行自动化测试。

<div align="center">

![开发进度](https://img.shields.io/badge/开发进度-100%25-brightgreen)
![版本](https://img.shields.io/badge/版本-v1.0-blue)
![状态](https://img.shields.io/badge/状态-Production%20Ready-success)
![代码](https://img.shields.io/badge/代码-21300+行-informational)
![技术栈](https://img.shields.io/badge/技术栈-Spring%20Boot%20%2B%20Vue%203-blue)
![AI集成](https://img.shields.io/badge/AI-DeepSeek%20%7C%20千问%20%7C%20智谱-orange)
![可视化](https://img.shields.io/badge/可视化-ECharts-red)
![CI/CD](https://img.shields.io/badge/CI/CD-GitLab-orange)
![许可证](https://img.shields.io/badge/许可证-Apache%202.0-green)

</div>

---

## 🎉 Phase 1-10 全部完成！完整的智能测试平台！

✅ **Phase 1: 用户和权限管理**
- 用户注册/登录、JWT认证、用户管理、路由守卫

✅ **Phase 2: 需求管理**
- 需求 CRUD、文件上传（MinIO）、搜索筛选、状态流转

✅ **Phase 3: 测试用例管理**
- 用例 CRUD、测试套件、测试步骤、批量操作、导入导出

✅ **Phase 4: AI 测试用例生成**
- AI 集成（DeepSeek/千问/智谱）、从需求生成用例、生成历史、AI 配置管理

✅ **Phase 5: 测试执行**
- 测试执行引擎、API/UI执行器、异步执行、执行历史、日志查看

✅ **Phase 6: 测试报告**
- 报告生成、数据统计、图表可视化、趋势分析、HTML导出

✅ **Phase 7: GitLab 集成**
- Webhook接收、签名验证、配置管理、记录追踪、代码变更分析

✅ **Phase 8: 项目管理**
- 项目CRUD、成员管理、项目统计、多项目支持

✅ **Phase 9: 系统管理**
- 系统配置、操作日志、参数管理

✅ **Phase 10: 性能优化**
- 数据库优化、缓存策略、API文档、用户手册

**👉 [立即开始](QUICK_START.md)** | **🔧 [安装指南](INSTALL.md)** | **📖 [用户手册](USER_MANUAL.md)** | **📡 [API文档](API_DOCUMENTATION.md)** | **🔄 [重命名报告](FINAL_RENAME_REPORT.md)**


---

## 📋 项目简介

本项目采用前后端分离架构：
- **后端**：Spring Boot 3.x + MySQL + Redis
- **前端**：Vue 3 + Element Plus + Vite
- **基础设施**：Docker Compose 一键部署

---

## 🏗️ 项目结构

```
AI-ATG/
├── backend/                    # 后端服务（Spring Boot）
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/aiatg/ # Java 源码
│   │   │   └── resources/      # 配置文件
│   │   └── test/               # 测试代码
│   └── pom.xml                 # Maven 配置
│
├── frontend/                   # 前端应用（Vue 3）
│   ├── src/
│   │   ├── views/              # 页面组件
│   │   ├── api/                # API 接口
│   │   ├── stores/             # 状态管理
│   │   ├── router/             # 路由配置
│   │   └── main.js             # 入口文件
│   ├── index.html
│   ├── vite.config.js          # Vite 配置
│   └── package.json            # npm 配置
│
├── docs/
│   └── database/
│       └── init.sql            # 数据库初始化脚本
│
├── docker-compose.yml          # Docker 编排配置
├── README.md                   # 本文件
├── TODO.md                     # 功能开发清单
├── QUICK_START.md              # 快速启动指南
├── DEVELOPMENT_GUIDE.md        # 开发指南
├── PHASE1_COMPLETION_REPORT.md # Phase 1 完成报告
└── PLATFORM_ARCHITECTURE.md    # 架构设计文档
```

---

## 🚀 快速开始

### 前置要求

- Docker & Docker Compose
- JDK 17+
- Maven 3.8+
- Node.js 18+

### 一键启动（推荐）

```bash
# 1. 启动基础设施
docker-compose up -d mysql redis

# 2. 启动后端（新终端）
cd backend && mvn spring-boot:run

# 3. 启动前端（新终端）
cd frontend && npm install && npm run dev
```

### 访问应用

- **前端应用**: http://localhost:5173
- **后端API**: http://localhost:8080/api

**详细步骤请查看**: [QUICK_START.md](QUICK_START.md)

---

## 🎯 功能清单

### ✅ 已完成

#### Phase 1: 用户和权限管理
- [x] 用户注册（表单验证、密码加密）
- [x] 用户登录（JWT Token 认证）
- [x] 用户信息管理（查看、更新、删除）
- [x] 用户列表展示
- [x] 路由守卫（自动跳转）
- [x] 权限控制（Token 验证）
- [x] 登录状态持久化

#### Phase 2: 需求管理
- [x] 需求 CRUD 接口
- [x] 需求文件上传（MinIO集成）
- [x] 需求搜索和筛选
- [x] 需求状态流转
- [x] 需求列表页面
- [x] 需求创建/编辑页面
- [x] 需求详情页面
- [x] 文件上传组件

#### Phase 3: 测试用例管理
- [x] 测试用例 CRUD 接口
- [x] 测试步骤管理
- [x] 测试套件管理
- [x] 用例搜索和筛选
- [x] 批量操作
- [x] 导入导出接口
- [x] 用例列表页面
- [x] 用例创建/编辑页面
- [x] 用例详情页面
- [x] 套件管理页面

#### Phase 4: AI 测试用例生成
- [x] AI 客户端（DeepSeek/千问/智谱）
- [x] AI 配置管理
- [x] 提示词模板管理
- [x] 从需求生成测试用例
- [x] AI 生成历史记录
- [x] AI 生成页面
- [x] AI 配置管理页面

#### Phase 5: 测试执行
- [x] 测试执行引擎
- [x] API 测试执行器
- [x] UI 测试执行器接口
- [x] 异步执行任务
- [x] 执行历史记录
- [x] 执行列表页面
- [x] 执行详情和日志查看

#### Phase 6: 测试报告
- [x] 报告生成服务
- [x] 数据统计和聚合
- [x] HTML报告导出
- [x] 报告列表和详情页面
- [x] 图表可视化（ECharts）
- [x] 趋势分析页面

#### Phase 7: GitLab集成
- [x] GitLab配置管理
- [x] Webhook接收和验证
- [x] 代码变更分析框架
- [x] Webhook记录查询
- [x] 配置页面和记录页面

#### Phase 8: 项目管理
- [x] 项目CRUD操作
- [x] 项目成员管理
- [x] 项目统计信息
- [x] 成员角色管理

#### Phase 9: 系统管理
- [x] 系统配置管理
- [x] 操作日志记录
- [x] 参数管理

#### Phase 10: 性能优化和完善
- [x] 数据库索引优化
- [x] Redis缓存策略
- [x] 查询优化建议
- [x] Swagger API文档
- [x] 用户使用手册
- [x] 项目文档完善

---

## 🎊 全部功能已完成！

**10大核心模块全部完成！完整的智能测试平台已经就绪！**

---

## 📊 技术栈

### 后端
- **框架**: Spring Boot 3.2.1
- **安全**: Spring Security + JWT
- **数据访问**: MyBatis Plus 3.5.5
- **数据库**: MySQL 8.0
- **缓存**: Redis 7.x
- **工具**: Hutool 5.8.24

### 前端
- **框架**: Vue 3.4
- **UI库**: Element Plus 2.5
- **状态管理**: Pinia 2.1
- **路由**: Vue Router 4.2
- **HTTP**: Axios 1.6
- **构建**: Vite 5.0

### 基础设施
- **数据库**: MySQL 8.0
- **缓存**: Redis 7.x
- **对象存储**: MinIO
- **消息队列**: RabbitMQ 3.12
- **搜索引擎**: Elasticsearch 8.10
- **测试引擎**: Selenium Grid 4.15

---

## 📖 文档导航

| 文档 | 说明 | 适用对象 |
|------|------|---------|
| [README.md](README.md) | 项目概览 | 所有人 |
| [QUICK_START.md](QUICK_START.md) | 5分钟快速启动 | 新手 |
| [INSTALL.md](INSTALL.md) | 详细安装指南 | 运维 |
| [DEPLOYMENT_CHECKLIST.md](DEPLOYMENT_CHECKLIST.md) | 部署检查清单 | 运维 |
| [CONTRIBUTING.md](CONTRIBUTING.md) | 贡献指南 | 贡献者 |
| [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md) | 项目总结 | 所有人 |
| [PLATFORM_ARCHITECTURE.md](PLATFORM_ARCHITECTURE.md) | 架构设计 | 架构师 |
| [USER_MANUAL.md](USER_MANUAL.md) | 用户手册 | 用户 |
| [API_DOCUMENTATION.md](API_DOCUMENTATION.md) | API文档 | 开发者 |
| [PERFORMANCE_GUIDE.md](PERFORMANCE_GUIDE.md) | 性能指南 | 运维 |
| [CHANGELOG.md](CHANGELOG.md) | 更新日志 | 所有人 |
| [COPYRIGHT.md](COPYRIGHT.md) | 版权说明 | 所有人 |
| [LICENSE](LICENSE) | 开源许可证 | 所有人 |
| [PROJECT_STATS.md](PROJECT_STATS.md) | 项目统计 | 项目管理 |
| [FINAL_COMPLETION_REPORT.md](FINAL_COMPLETION_REPORT.md) | 最终完成报告 | 项目管理 |
| [NOTICE](NOTICE) | 第三方组件声明 | 所有人 |
| [LICENSE_HEADER.txt](LICENSE_HEADER.txt) | 许可证头部模板 | 开发者 |
| [PROJECT_RENAME_SUMMARY.md](PROJECT_RENAME_SUMMARY.md) | 重命名总结 | 开发者 |
| [FINAL_RENAME_REPORT.md](FINAL_RENAME_REPORT.md) | 重命名报告 | 开发者 |
| [docs/database/init.sql](docs/database/init.sql) | 数据库脚本 | DBA |

---

## 🔗 相关链接

### 访问地址

启动所有服务后，可访问：

- **前端应用**: http://localhost:5173
- **后端API**: http://localhost:8080/api
- **MySQL**: localhost:3306 (root/Aiatg123456)
- **Redis**: localhost:6379 (密码: Aiatg123456)
- **MinIO Console**: http://localhost:9001 (admin/Aiatg123456)
- **RabbitMQ Management**: http://localhost:15672 (admin/Aiatg123456)
- **Selenium Grid**: http://localhost:4444
- **Portainer**: https://localhost:9443

### 数据库管理

```bash
# 进入 MySQL 容器
docker exec -it ai-atg-mysql mysql -uroot -paiatg123456

# 查看数据
USE z_atg;
SHOW TABLES;
SELECT * FROM user;
```

---

## 🎓 学习路径

### 对于测试人员
1. 查看 [QUICK_START.md](QUICK_START.md) 快速启动项目
2. 体验用户注册和登录功能
3. 创建需求并使用AI生成测试用例
4. 执行测试并查看结果

### 对于开发人员
1. 阅读 [PLATFORM_ARCHITECTURE.md](PLATFORM_ARCHITECTURE.md) 了解架构
2. 查看 [DEVELOPMENT_GUIDE.md](DEVELOPMENT_GUIDE.md) 了解开发规范
3. 阅读各Phase的完成报告了解实现细节
4. 参考代码实现扩展新功能

### 对于项目管理者
1. 查看 [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md) 了解项目全貌
2. 阅读各Phase完成报告了解交付内容
3. 查看 [TODO.md](TODO.md) 了解后续扩展计划

---

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

### 开发规范
- 代码风格：遵循项目现有风格
- 提交信息：使用语义化提交（feat/fix/docs/style/refactor）
- 测试：为新功能编写测试
- 文档：更新相关文档

---

## 📄 许可证

本项目基于 **Apache License 2.0** 开源许可证发布。

```
Copyright 2026 James Zou

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

完整许可证内容请查看 [LICENSE](LICENSE) 文件。

**许可证要点**:
- ✅ 可以自由使用、复制、修改和分发
- ✅ 可以用于商业用途
- ✅ 需要保留版权和许可证声明
- ✅ 提供"按原样"的软件，不提供任何担保

---

## 📞 联系方式

- **作者**: James Zou
- **邮箱**: 18301545237@163.com
- **项目**: AI-ATG 智能测试平台
- **版本**: v1.0

---

<div align="center">

**AI-ATG: 让测试更智能，让质量更可靠**

Made with ❤️ by James Zou | 2026

**⭐ 如果这个项目对你有帮助，请给个 Star！**

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)

</div>
