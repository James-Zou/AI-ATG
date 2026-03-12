# AI-ATG Automated Testing Platform

**AI-ATG (AI-Automatic Test Generation)** is an intelligent testing platform based on AI, designed to help testing teams automatically generate test cases and execute automated tests.

<div align="center">

![Development Progress](https://img.shields.io/badge/Development%20Progress-100%25-brightgreen)
![Version](https://img.shields.io/badge/Version-v1.0-blue)
![Status](https://img.shields.io/badge/Status-Production%20Ready-success)
![Code](https://img.shields.io/badge/Code-21300+%20Lines-informational)
![Tech Stack](https://img.shields.io/badge/Technical%20Stack-Spring%20Boot%20%2B%20Vue%203-blue)
![AI Integration](https://img.shields.io/badge/AI-DeepSeek%20%7C%20Alibaba%20Qwen%20%7C%20Zhipu-orange)
![Visualization](https://img.shields.io/badge/Visualization-ECharts-red)
![CI/CD](https://img.shields.io/badge/CI/CD-GitLab-orange)
![License](https://img.shields.io/badge/License-Apache%202.0-green)

</div>

---

## 🎉 All Phases 1-10 Complete! A Complete Intelligent Testing Platform!

✅ **Phase 1: User and Permission Management**
- User registration/login, JWT authentication, user management, route guards

✅ **Phase 2: Requirements Management**
- Requirements CRUD, file upload (MinIO), search and filtering, status workflow

✅ **Phase 3: Test Case Management**
- Test case CRUD, test suites, test steps, batch operations, import/export

✅ **Phase 4: AI Test Case Generation**
- AI integration (DeepSeek/Alibaba Qwen/Zhipu), generate cases from requirements, generation history, AI configuration management

✅ **Phase 5: Test Execution**
- Test execution engine, API/UI executors, asynchronous execution, execution history, log viewing

✅ **Phase 6: Test Reports**
- Report generation, data statistics, chart visualization, trend analysis, HTML export

✅ **Phase 7: GitLab Integration**
- Webhook reception, signature verification, configuration management, record tracking, code change analysis

✅ **Phase 8: Project Management**
- Project CRUD, member management, project statistics, multi-project support

✅ **Phase 9: System Management**
- System configuration, operation logs, parameter management

✅ **Phase 10: Performance Optimization**
- Database optimization, caching strategy, API documentation, user manual

**👉 [Get Started](QUICK_START.md)** | **🔧 [Installation Guide](INSTALL.md)** | **📖 [User Manual](USER_MANUAL.md)** | **📡 [API Documentation](API_DOCUMENTATION.md)** | **🔄 [Rename Report](FINAL_RENAME_REPORT.md)**

---

## 📋 Project Overview

This project adopts a frontend-backend separation architecture:
- **Backend**: Spring Boot 3.x + MySQL + Redis
- **Frontend**: Vue 3 + Element Plus + Vite
- **Infrastructure**: Docker Compose one-click deployment

---

## 🏗️ Project Structure

```
AI-ATG/
├── backend/                    # Backend service (Spring Boot)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/aiatg/ # Java source code
│   │   │   └── resources/      # Configuration files
│   │   └── test/               # Test code
│   └── pom.xml                 # Maven configuration
│
├── frontend/                   # Frontend application (Vue 3)
│   ├── src/
│   │   ├── views/              # Page components
│   │   ├── api/                # API interfaces
│   │   ├── stores/             # State management
│   │   ├── router/             # Route configuration
│   │   └── main.js             # Entry file
│   ├── index.html
│   ├── vite.config.js          # Vite configuration
│   └── package.json            # npm configuration
│
├── docs/
│   └── database/
│       └── init.sql            # Database initialization script
│
├── docker-compose.yml          # Docker compose configuration
├── README.md                   # This file
├── TODO.md                     # Feature development checklist
├── QUICK_START.md              # Quick start guide
├── DEVELOPMENT_GUIDE.md        # Development guide
├── PHASE1_COMPLETION_REPORT.md # Phase 1 completion report
└── PLATFORM_ARCHITECTURE.md    # Architecture design document
```

---

## 🚀 Quick Start

### Prerequisites

- Docker & Docker Compose
- JDK 17+
- Maven 3.8+
- Node.js 18+

### One-Click Startup (Recommended)

```bash
# 1. Start infrastructure
docker-compose up -d mysql redis

# 2. Start backend (new terminal)
cd backend && mvn spring-boot:run

# 3. Start frontend (new terminal)
cd frontend && npm install && npm run dev
```

### Access Application

- **Frontend Application**: http://localhost:5173
- **Backend API**: http://localhost:8080/api

**For detailed steps please refer to**: [QUICK_START.md](QUICK_START.md)

---

## 🎯 Feature Checklist

### ✅ Completed

#### Phase 1: User and Permission Management
- [x] User registration (form validation, password encryption)
- [x] User login (JWT Token authentication)
- [x] User information management (view, update, delete)
- [x] User list display
- [x] Route guards (auto-redirect)
- [x] Permission control (Token verification)
- [x] Login state persistence

#### Phase 2: Requirements Management
- [x] Requirements CRUD API
- [x] Requirements file upload (MinIO integration)
- [x] Requirements search and filtering
- [x] Requirements status workflow
- [x] Requirements list page
- [x] Requirements create/edit page
- [x] Requirements details page
- [x] File upload component

#### Phase 3: Test Case Management
- [x] Test case CRUD API
- [x] Test step management
- [x] Test suite management
- [x] Test case search and filtering
- [x] Batch operations
- [x] Import/export API
- [x] Test case list page
- [x] Test case create/edit page
- [x] Test case details page
- [x] Suite management page

#### Phase 4: AI Test Case Generation
- [x] AI client (DeepSeek/Alibaba Qwen/Zhipu)
- [x] AI configuration management
- [x] Prompt template management
- [x] Generate test cases from requirements
- [x] AI generation history
- [x] AI generation page
- [x] AI configuration management page

#### Phase 5: Test Execution
- [x] Test execution engine
- [x] API test executor
- [x] UI test executor interface
- [x] Asynchronous execution tasks
- [x] Execution history records
- [x] Execution list page
- [x] Execution details and log viewing

#### Phase 6: Test Reports
- [x] Report generation service
- [x] Data statistics and aggregation
- [x] HTML report export
- [x] Report list and details pages
- [x] Chart visualization (ECharts)
- [x] Trend analysis page

#### Phase 7: GitLab Integration
- [x] GitLab configuration management
- [x] Webhook reception and verification
- [x] Code change analysis framework
- [x] Webhook record retrieval
- [x] Configuration and record pages

#### Phase 8: Project Management
- [x] Project CRUD operations
- [x] Project member management
- [x] Project statistics information
- [x] Member role management

#### Phase 9: System Management
- [x] System configuration management
- [x] Operation log recording
- [x] Parameter management

#### Phase 10: Performance Optimization and Enhancement
- [x] Database index optimization
- [x] Redis caching strategy
- [x] Query optimization suggestions
- [x] Swagger API documentation
- [x] User usage manual
- [x] Project documentation enhancement

---

## 🎊 All Features Complete!

**All 10 core modules completed! A complete intelligent testing platform is ready!**

---

## 📊 Technology Stack

### Backend
- **Framework**: Spring Boot 3.2.1
- **Security**: Spring Security + JWT
- **Data Access**: MyBatis Plus 3.5.5
- **Database**: MySQL 8.0
- **Cache**: Redis 7.x
- **Tools**: Hutool 5.8.24

### Frontend
- **Framework**: Vue 3.4
- **UI Library**: Element Plus 2.5
- **State Management**: Pinia 2.1
- **Routing**: Vue Router 4.2
- **HTTP**: Axios 1.6
- **Build Tool**: Vite 5.0

### Infrastructure
- **Database**: MySQL 8.0
- **Cache**: Redis 7.x
- **Object Storage**: MinIO
- **Message Queue**: RabbitMQ 3.12
- **Search Engine**: Elasticsearch 8.10
- **Test Engine**: Selenium Grid 4.15

---

## 📖 Documentation Navigation

| Document | Description | Target Audience |
|----------|-------------|-----------------|
| [README.md](README.md) | Project overview | Everyone |
| [QUICK_START.md](QUICK_START.md) | 5-minute quick start | Beginners |
| [INSTALL.md](INSTALL.md) | Detailed installation guide | Operations |
| [DEPLOYMENT_CHECKLIST.md](DEPLOYMENT_CHECKLIST.md) | Deployment checklist | Operations |
| [CONTRIBUTING.md](CONTRIBUTING.md) | Contributing guide | Contributors |
| [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md) | Project summary | Everyone |
| [PLATFORM_ARCHITECTURE.md](PLATFORM_ARCHITECTURE.md) | Architecture design | Architects |
| [USER_MANUAL.md](USER_MANUAL.md) | User manual | Users |
| [API_DOCUMENTATION.md](API_DOCUMENTATION.md) | API documentation | Developers |
| [PERFORMANCE_GUIDE.md](PERFORMANCE_GUIDE.md) | Performance guide | Operations |
| [CHANGELOG.md](CHANGELOG.md) | Change log | Everyone |
| [COPYRIGHT.md](COPYRIGHT.md) | Copyright statement | Everyone |
| [LICENSE](LICENSE) | Open source license | Everyone |
| [PROJECT_STATS.md](PROJECT_STATS.md) | Project statistics | Project Management |
| [FINAL_COMPLETION_REPORT.md](FINAL_COMPLETION_REPORT.md) | Final completion report | Project Management |
| [NOTICE](NOTICE) | Third-party component declaration | Everyone |
| [LICENSE_HEADER.txt](LICENSE_HEADER.txt) | License header template | Developers |
| [PROJECT_RENAME_SUMMARY.md](PROJECT_RENAME_SUMMARY.md) | Rename summary | Developers |
| [FINAL_RENAME_REPORT.md](FINAL_RENAME_REPORT.md) | Rename report | Developers |
| [docs/database/init.sql](docs/database/init.sql) | Database script | DBA |

---

## 🔗 Related Links

### Access Addresses

After starting all services, you can access:

- **Frontend Application**: http://localhost:5173
- **Backend API**: http://localhost:8080/api
- **MySQL**: localhost:3306 (root/Aiatg123456)
- **Redis**: localhost:6379 (password: Aiatg123456)
- **MinIO Console**: http://localhost:9001 (admin/Aiatg123456)
- **RabbitMQ Management**: http://localhost:15672 (admin/Aiatg123456)
- **Selenium Grid**: http://localhost:4444
- **Portainer**: https://localhost:9443

### Database Management

```bash
# Enter MySQL container
docker exec -it ai-atg-mysql mysql -uroot -paiatg123456

# View data
USE z_atg;
SHOW TABLES;
SELECT * FROM user;
```

---

## 🎓 Learning Path

### For Testers
1. Check [QUICK_START.md](QUICK_START.md) to quickly start the project
2. Experience user registration and login functionality
3. Create requirements and use AI to generate test cases
4. Execute tests and view results

### For Developers
1. Read [PLATFORM_ARCHITECTURE.md](PLATFORM_ARCHITECTURE.md) to understand the architecture
2. Check [DEVELOPMENT_GUIDE.md](DEVELOPMENT_GUIDE.md) to understand development standards
3. Read Phase completion reports to understand implementation details
4. Refer to code implementation to extend new features

### For Project Managers
1. Check [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md) to understand the overall project
2. Read Phase completion reports to understand deliverables
3. Check [TODO.md](TODO.md) to understand future extension plans

---

## 🤝 Contributing

Welcome to submit Issues and Pull Requests!

### Development Standards
- Code style: Follow existing project style
- Commit messages: Use semantic commits (feat/fix/docs/style/refactor)
- Testing: Write tests for new features
- Documentation: Update related documentation

---

## 📄 License

This project is released under the **Apache License 2.0** open source license.

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

For complete license details, please see the [LICENSE](LICENSE) file.

**License Key Points**:
- ✅ Can freely use, copy, modify and distribute
- ✅ Can be used for commercial purposes
- ✅ Must retain copyright and license notices
- ✅ Software is provided "as is", with no warranty

---

## 📞 Contact Information

- **Author**: James Zou
- **Email**: 18301545237@163.com
- **Project**: AI-ATG Intelligent Testing Platform
- **Version**: v1.0

---

<div align="center">

**AI-ATG: Making Testing More Intelligent, Making Quality More Reliable**

Made with ❤️ by James Zou | 2026

**⭐ If this project helps you, please give it a Star!**

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)

</div>
