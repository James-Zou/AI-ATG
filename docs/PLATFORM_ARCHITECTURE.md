# AI-ATG 平台架构设计

## 🎯 平台定位

**AI-ATG (AI-Automatic Test Generation)** 是一个基于 AI 的智能测试平台，采用前后端分离架构，支持多人协作的测试管理和自动化执行

---

## 📐 整体架构

### 系统架构图

```
┌─────────────────────────────────────────────────────────────┐
│                        用户层                                 │
│  测试人员 | 测试经理 | 开发人员 | 项目经理                    │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                     前端层 (Vue 3)                            │
│  ┌──────────┬──────────┬──────────┬──────────┬──────────┐   │
│  │需求管理  │用例管理  │测试执行  │报告查看  │系统管理  │   │
│  └──────────┴──────────┴──────────┴──────────┴──────────┘   │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                   API网关层 (Spring Cloud Gateway)            │
│  认证 | 鉴权 | 限流 | 日志 | 监控                             │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                  后端服务层 (Spring Boot)                      │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ 需求服务 | 用例服务 | 执行服务 | 报告服务 | 用户服务 │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    AI服务层                                   │
│  DeepSeek API | 千问API | 智谱AI | 其他AI服务                │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                   测试执行层                                  │
│  Selenium Grid | Playwright | JMeter | K6                   │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    数据层                                     │
│  MySQL | Redis | MinIO/OSS | Elasticsearch                  │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                   外部集成层                                  │
│  GitLab Webhook | Jenkins | 钉钉/企业微信 | JIRA            │
└─────────────────────────────────────────────────────────────┘
```

---

## 🏗️ 技术栈选型

### 前端技术栈

```yaml
框架: Vue 3 + TypeScript
UI组件库: Element Plus / Ant Design Vue
状态管理: Pinia
路由: Vue Router
HTTP客户端: Axios
构建工具: Vite
代码编辑器: Monaco Editor (在线编辑测试用例)
图表: ECharts (测试报告可视化)
富文本编辑: TinyMCE / Quill (需求文档编辑)
```

### 后端技术栈

```yaml
核心框架: Spring Boot 3.x
微服务: Spring Cloud (可选)
数据库: MySQL 8.0
缓存: Redis 7.x
对象存储: MinIO / 阿里云OSS (存储截图、视频)
搜索引擎: Elasticsearch (日志、报告搜索)
消息队列: RabbitMQ / Kafka (异步任务)
定时任务: Quartz / XXL-Job
API文档: Knife4j / Swagger
认证授权: Spring Security + JWT
```

### 测试执行引擎

```yaml
UI测试: 
  - Selenium WebDriver
  - Playwright
  
接口测试:
  - RestAssured
  - HttpClient
  
性能测试:
  - JMeter
  - K6
```

### AI集成

```yaml
国产AI:
  - DeepSeek API
  - 阿里千问 (通义千问)
  - 智谱AI (ChatGLM)
  - 百度文心一言

国际AI (可选):
  - OpenAI GPT-4
  - Claude API
```

---

## 📊 数据库设计

### 核心表结构

```sql
-- 1. 项目表
CREATE TABLE project (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '项目名称',
    description TEXT COMMENT '项目描述',
    gitlab_project_id INT COMMENT 'GitLab项目ID',
    gitlab_webhook_url VARCHAR(500) COMMENT 'Webhook URL',
    status TINYINT DEFAULT 1 COMMENT '状态：1-活跃，0-归档',
    created_by BIGINT COMMENT '创建人',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目表';

-- 2. 需求表
CREATE TABLE requirement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id BIGINT NOT NULL COMMENT '项目ID',
    title VARCHAR(200) NOT NULL COMMENT '需求标题',
    content TEXT COMMENT '需求内容',
    type VARCHAR(50) COMMENT '需求类型：user_story/feature/bug_fix',
    priority VARCHAR(20) COMMENT '优先级：P0/P1/P2/P3',
    source VARCHAR(50) COMMENT '来源：manual/gitlab/jira',
    source_id VARCHAR(100) COMMENT '来源ID（如GitLab MR ID）',
    attachment_urls TEXT COMMENT '附件URLs（JSON数组）',
    status VARCHAR(50) DEFAULT 'draft' COMMENT '状态：draft/reviewing/approved/testing',
    created_by BIGINT,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_project (project_id),
    INDEX idx_status (status),
    FOREIGN KEY (project_id) REFERENCES project(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='需求表';

-- 3. 测试用例表
CREATE TABLE test_case (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    requirement_id BIGINT COMMENT '关联需求ID',
    case_no VARCHAR(50) UNIQUE COMMENT '用例编号：TC001',
    title VARCHAR(200) NOT NULL COMMENT '用例标题',
    type VARCHAR(50) COMMENT '类型：ui/api/performance',
    priority VARCHAR(20) COMMENT '优先级：P0/P1/P2/P3',
    preconditions TEXT COMMENT '前置条件',
    steps TEXT COMMENT '测试步骤（JSON）',
    expected_result TEXT COMMENT '预期结果',
    test_data TEXT COMMENT '测试数据（JSON）',
    tags VARCHAR(500) COMMENT '标签（逗号分隔）',
    source VARCHAR(50) DEFAULT 'manual' COMMENT '来源：manual/ai_generated',
    ai_model VARCHAR(50) COMMENT 'AI模型：deepseek/qwen',
    status VARCHAR(50) DEFAULT 'draft' COMMENT '状态：draft/reviewing/approved',
    created_by BIGINT,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_project (project_id),
    INDEX idx_requirement (requirement_id),
    INDEX idx_case_no (case_no),
    INDEX idx_type (type),
    FOREIGN KEY (project_id) REFERENCES project(id),
    FOREIGN KEY (requirement_id) REFERENCES requirement(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试用例表';

-- 4. 测试套件表
CREATE TABLE test_suite (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    type VARCHAR(50) COMMENT '类型：smoke/regression/full',
    status TINYINT DEFAULT 1,
    created_by BIGINT,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_project (project_id),
    FOREIGN KEY (project_id) REFERENCES project(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试套件表';

-- 5. 测试套件用例关联表
CREATE TABLE suite_case_relation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    suite_id BIGINT NOT NULL,
    case_id BIGINT NOT NULL,
    execute_order INT DEFAULT 0 COMMENT '执行顺序',
    INDEX idx_suite (suite_id),
    INDEX idx_case (case_id),
    UNIQUE KEY uk_suite_case (suite_id, case_id),
    FOREIGN KEY (suite_id) REFERENCES test_suite(id),
    FOREIGN KEY (case_id) REFERENCES test_case(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试套件用例关联表';

-- 6. 测试执行记录表
CREATE TABLE test_execution (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    suite_id BIGINT COMMENT '测试套件ID',
    name VARCHAR(100) COMMENT '执行名称',
    type VARCHAR(50) COMMENT '执行类型：manual/scheduled/ci',
    environment VARCHAR(50) COMMENT '测试环境：dev/test/staging/prod',
    status VARCHAR(50) DEFAULT 'pending' COMMENT '状态：pending/running/completed/failed',
    total_cases INT DEFAULT 0,
    passed_cases INT DEFAULT 0,
    failed_cases INT DEFAULT 0,
    skipped_cases INT DEFAULT 0,
    pass_rate DECIMAL(5,2) COMMENT '通过率',
    start_time DATETIME,
    end_time DATETIME,
    duration_seconds INT COMMENT '执行时长（秒）',
    executor BIGINT COMMENT '执行人',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_project (project_id),
    INDEX idx_status (status),
    FOREIGN KEY (project_id) REFERENCES project(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试执行记录表';

-- 7. 测试执行结果详情表
CREATE TABLE test_execution_result (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    execution_id BIGINT NOT NULL,
    case_id BIGINT NOT NULL,
    status VARCHAR(50) COMMENT '结果：passed/failed/skipped/error',
    error_message TEXT COMMENT '错误信息',
    stack_trace TEXT COMMENT '堆栈信息',
    screenshot_urls TEXT COMMENT '截图URLs（JSON）',
    video_url VARCHAR(500) COMMENT '视频URL',
    log_url VARCHAR(500) COMMENT '日志URL',
    start_time DATETIME,
    end_time DATETIME,
    duration_seconds INT,
    INDEX idx_execution (execution_id),
    INDEX idx_case (case_id),
    INDEX idx_status (status),
    FOREIGN KEY (execution_id) REFERENCES test_execution(id),
    FOREIGN KEY (case_id) REFERENCES test_case(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试执行结果详情表';

-- 8. AI生成记录表
CREATE TABLE ai_generation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id BIGINT,
    requirement_id BIGINT,
    model VARCHAR(50) COMMENT 'AI模型：deepseek/qwen/chatglm',
    input_type VARCHAR(50) COMMENT '输入类型：requirement/code_diff/api_spec',
    input_content TEXT COMMENT '输入内容',
    output_content TEXT COMMENT '输出内容（生成的测试用例）',
    tokens_used INT COMMENT 'Token消耗',
    cost DECIMAL(10,4) COMMENT '费用',
    duration_ms INT COMMENT '耗时（毫秒）',
    status VARCHAR(50) COMMENT '状态：success/failed',
    error_message TEXT,
    created_by BIGINT,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_project (project_id),
    INDEX idx_model (model)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI生成记录表';

-- 9. GitLab集成记录表
CREATE TABLE gitlab_integration (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    event_type VARCHAR(50) COMMENT '事件类型：push/merge_request/tag',
    event_data TEXT COMMENT '事件数据（JSON）',
    commit_id VARCHAR(100),
    branch VARCHAR(100),
    author VARCHAR(100),
    commit_message TEXT,
    files_changed TEXT COMMENT '变更文件列表（JSON）',
    requirement_id BIGINT COMMENT '关联需求ID',
    test_cases_generated INT DEFAULT 0 COMMENT '生成测试用例数',
    processed TINYINT DEFAULT 0 COMMENT '是否已处理',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_project (project_id),
    INDEX idx_commit (commit_id),
    FOREIGN KEY (project_id) REFERENCES project(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='GitLab集成记录表';

-- 10. 用户表
CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(200) NOT NULL COMMENT '加密后的密码',
    nickname VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20),
    avatar_url VARCHAR(500),
    role VARCHAR(50) DEFAULT 'tester' COMMENT '角色：admin/test_lead/tester/developer',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
    last_login_time DATETIME,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 11. 测试报告表
CREATE TABLE test_report (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    execution_id BIGINT NOT NULL,
    report_type VARCHAR(50) COMMENT '报告类型：html/pdf/excel',
    report_url VARCHAR(500) COMMENT '报告URL',
    summary TEXT COMMENT '报告摘要（JSON）',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_execution (execution_id),
    FOREIGN KEY (execution_id) REFERENCES test_execution(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试报告表';

-- 12. 系统配置表
CREATE TABLE system_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(100) UNIQUE NOT NULL,
    config_value TEXT,
    config_type VARCHAR(50) COMMENT '配置类型：ai/gitlab/notification/storage',
    description VARCHAR(500),
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';
```

---

## 🔧 后端服务设计

### 服务模块划分

```
ai-atg-backend/
├── ai-atg-common/              # 公共模块
│   ├── common-core/           # 核心工具类
│   ├── common-redis/          # Redis封装
│   └── common-security/       # 安全认证
│
├── ai-atg-gateway/             # API网关
│   └── src/main/java/
│       └── com/aiatg/gateway/
│
├── ai-atg-system/              # 系统服务
│   └── src/main/java/
│       └── com/aiatg/system/
│           ├── controller/    # 用户、权限管理
│           ├── service/
│           └── mapper/
│
├── ai-atg-requirement/         # 需求管理服务
│   └── src/main/java/
│       └── com/aiatg/requirement/
│           ├── controller/
│           │   ├── RequirementController.java
│           │   └── RequirementAIController.java
│           ├── service/
│           │   ├── RequirementService.java
│           │   └── RequirementAIService.java
│           ├── mapper/
│           └── domain/
│
├── ai-atg-testcase/            # 测试用例服务
│   └── src/main/java/
│       └── com/aiatg/testcase/
│           ├── controller/
│           │   ├── TestCaseController.java
│           │   ├── TestCaseAIController.java
│           │   └── TestSuiteController.java
│           ├── service/
│           │   ├── TestCaseService.java
│           │   ├── TestCaseGenerateService.java
│           │   └── TestSuiteService.java
│           └── ai/
│               ├── DeepSeekClient.java
│               ├── QwenClient.java
│               └── AIPromptBuilder.java
│
├── ai-atg-execution/           # 测试执行服务
│   └── src/main/java/
│       └── com/aiatg/execution/
│           ├── controller/
│           │   ├── ExecutionController.java
│           │   └── ExecutionResultController.java
│           ├── service/
│           │   ├── ExecutionService.java
│           │   └── ExecutionEngine.java
│           ├── engine/
│           │   ├── UITestEngine.java      # UI测试引擎
│           │   ├── APITestEngine.java     # API测试引擎
│           │   └── PerformanceTestEngine.java
│           └── runner/
│               ├── SeleniumRunner.java
│               ├── PlaywrightRunner.java
│               └── JMeterRunner.java
│
├── ai-atg-report/              # 测试报告服务
│   └── src/main/java/
│       └── com/aiatg/report/
│           ├── controller/
│           │   └── ReportController.java
│           ├── service/
│           │   ├── ReportService.java
│           │   └── ReportGenerateService.java
│           └── generator/
│               ├── HTMLReportGenerator.java
│               ├── PDFReportGenerator.java
│               └── ExcelReportGenerator.java
│
├── ai-atg-integration/         # 外部集成服务
│   └── src/main/java/
│       └── com/aiatg/integration/
│           ├── controller/
│           │   ├── GitLabWebhookController.java
│           │   └── JiraIntegrationController.java
│           ├── service/
│           │   ├── GitLabService.java
│           │   └── CodeAnalyzer.java
│           └── webhook/
│               └── GitLabWebhookHandler.java
│
└── ai-atg-file/                # 文件服务
    └── src/main/java/
        └── com/aiatg/file/
            ├── controller/
            │   └── FileController.java
            └── service/
                ├── MinIOService.java
                └── OSSService.java
```

---

## 🎨 前端页面设计

### 页面结构

```
ai-atg-frontend/
├── src/
│   ├── views/
│   │   ├── login/                    # 登录页
│   │   │   └── index.vue
│   │   │
│   │   ├── dashboard/                # 仪表盘
│   │   │   └── index.vue
│   │   │
│   │   ├── project/                  # 项目管理
│   │   │   ├── index.vue             # 项目列表
│   │   │   ├── detail.vue            # 项目详情
│   │   │   └── settings.vue          # 项目配置
│   │   │
│   │   ├── requirement/              # 需求管理
│   │   │   ├── list.vue              # 需求列表
│   │   │   ├── create.vue            # 创建需求
│   │   │   ├── edit.vue              # 编辑需求
│   │   │   └── detail.vue            # 需求详情
│   │   │
│   │   ├── testcase/                 # 测试用例管理
│   │   │   ├── list.vue              # 用例列表
│   │   │   ├── create.vue            # 创建用例
│   │   │   ├── edit.vue              # 编辑用例
│   │   │   ├── detail.vue            # 用例详情
│   │   │   └── ai-generate.vue       # AI生成用例
│   │   │
│   │   ├── suite/                    # 测试套件
│   │   │   ├── list.vue              # 套件列表
│   │   │   └── manage.vue            # 套件管理
│   │   │
│   │   ├── execution/                # 测试执行
│   │   │   ├── list.vue              # 执行历史
│   │   │   ├── execute.vue           # 执行测试
│   │   │   ├── monitor.vue           # 执行监控
│   │   │   └── result.vue            # 执行结果
│   │   │
│   │   ├── report/                   # 测试报告
│   │   │   ├── list.vue              # 报告列表
│   │   │   └── view.vue              # 查看报告
│   │   │
│   │   ├── integration/              # 集成配置
│   │   │   ├── gitlab.vue            # GitLab配置
│   │   │   └── ai.vue                # AI配置
│   │   │
│   │   └── system/                   # 系统管理
│   │       ├── user.vue              # 用户管理
│   │       ├── role.vue              # 角色管理
│   │       └── config.vue            # 系统配置
│   │
│   ├── components/                   # 公共组件
│   │   ├── TestCaseEditor/          # 用例编辑器
│   │   ├── CodeDiffViewer/          # 代码差异查看器
│   │   ├── ReportChart/             # 报告图表
│   │   └── AIGenerateDialog/        # AI生成对话框
│   │
│   ├── api/                          # API接口
│   │   ├── requirement.js
│   │   ├── testcase.js
│   │   ├── execution.js
│   │   └── report.js
│   │
│   ├── store/                        # 状态管理
│   │   ├── modules/
│   │   │   ├── user.js
│   │   │   ├── project.js
│   │   │   └── testcase.js
│   │   └── index.js
│   │
│   └── router/                       # 路由配置
│       └── index.js
```

### 核心页面功能

#### 1. 需求管理页面

**需求列表页 (requirement/list.vue)**
```vue
<template>
  <div class="requirement-list">
    <!-- 搜索栏 -->
    <el-form :inline="true">
      <el-form-item label="项目">
        <el-select v-model="queryForm.projectId">
          <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryForm.status">
          <el-option label="全部" value=""/>
          <el-option label="草稿" value="draft"/>
          <el-option label="评审中" value="reviewing"/>
          <el-option label="已通过" value="approved"/>
        </el-select>
      </el-form-item>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button type="success" @click="handleCreate">创建需求</el-button>
      <el-button type="warning" @click="handleAIGenerate">AI生成测试用例</el-button>
    </el-form>

    <!-- 需求表格 -->
    <el-table :data="requirements" stripe>
      <el-table-column prop="title" label="需求标题"/>
      <el-table-column prop="type" label="类型"/>
      <el-table-column prop="priority" label="优先级"/>
      <el-table-column prop="status" label="状态"/>
      <el-table-column label="操作" width="300">
        <template #default="scope">
          <el-button size="small" @click="handleView(scope.row)">查看</el-button>
          <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="small" type="warning" @click="handleGenerateTestCase(scope.row)">
            生成用例
          </el-button>
          <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
```

#### 2. AI生成用例对话框

**AI生成组件 (components/AIGenerateDialog.vue)**
```vue
<template>
  <el-dialog v-model="visible" title="AI生成测试用例" width="800px">
    <!-- AI模型选择 -->
    <el-form label-width="100px">
      <el-form-item label="AI模型">
        <el-radio-group v-model="form.aiModel">
          <el-radio label="deepseek">DeepSeek</el-radio>
          <el-radio label="qwen">通义千问</el-radio>
          <el-radio label="chatglm">智谱AI</el-radio>
        </el-radio-group>
      </el-form-item>
      
      <el-form-item label="输入类型">
        <el-radio-group v-model="form.inputType">
          <el-radio label="requirement">需求文档</el-radio>
          <el-radio label="code_diff">代码变更</el-radio>
          <el-radio label="api_spec">API规格</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="生成策略">
        <el-checkbox-group v-model="form.strategies">
          <el-checkbox label="boundary">边界值分析</el-checkbox>
          <el-checkbox label="equivalence">等价类划分</el-checkbox>
          <el-checkbox label="scenario">场景法</el-checkbox>
          <el-checkbox label="error">错误推测</el-checkbox>
        </el-checkbox-group>
      </el-form-item>
    </el-form>

    <!-- 生成进度 -->
    <el-progress v-if="generating" :percentage="progress" :status="progressStatus"/>

    <!-- 生成结果预览 -->
    <div v-if="generatedCases.length > 0" class="generated-cases">
      <h4>生成的测试用例（{{ generatedCases.length }}个）</h4>
      <el-table :data="generatedCases" max-height="400">
        <el-table-column type="selection" width="55"/>
        <el-table-column prop="title" label="用例标题"/>
        <el-table-column prop="priority" label="优先级" width="80"/>
        <el-table-column prop="type" label="类型" width="100"/>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button size="small" @click="handlePreview(scope.row)">预览</el-button>
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="handleGenerate" :loading="generating">
        开始生成
      </el-button>
      <el-button type="success" @click="handleSave" :disabled="generatedCases.length === 0">
        保存用例
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'
import { generateTestCases } from '@/api/testcase'

const visible = ref(false)
const generating = ref(false)
const progress = ref(0)
const generatedCases = ref([])

const handleGenerate = async () => {
  generating.value = true
  progress.value = 0
  
  try {
    // 调用后端API生成测试用例
    const result = await generateTestCases({
      requirementId: props.requirementId,
      aiModel: form.value.aiModel,
      inputType: form.value.inputType,
      strategies: form.value.strategies
    })
    
    generatedCases.value = result.testCases
    progress.value = 100
  } catch (error) {
    ElMessage.error('生成失败：' + error.message)
  } finally {
    generating.value = false
  }
}
</script>
```

#### 3. 测试执行监控页面

**执行监控页 (execution/monitor.vue)**
```vue
<template>
  <div class="execution-monitor">
    <!-- 执行概览 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card>
          <div class="stat-card">
            <div class="stat-value">{{ execution.totalCases }}</div>
            <div class="stat-label">总用例数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-card passed">
            <div class="stat-value">{{ execution.passedCases }}</div>
            <div class="stat-label">通过</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-card failed">
            <div class="stat-value">{{ execution.failedCases }}</div>
            <div class="stat-label">失败</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-card">
            <div class="stat-value">{{ execution.passRate }}%</div>
            <div class="stat-label">通过率</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 执行进度 -->
    <el-card class="progress-card">
      <template #header>
        <span>执行进度</span>
        <el-tag :type="statusTagType">{{ execution.status }}</el-tag>
      </template>
      <el-progress 
        :percentage="executionProgress" 
        :status="progressStatus"
        :stroke-width="20"
      />
      <div class="progress-info">
        <span>已执行: {{ execution.completedCases }} / {{ execution.totalCases }}</span>
        <span>预计剩余时间: {{ estimatedTimeRemaining }}</span>
      </div>
    </el-card>

    <!-- 实时日志 -->
    <el-card class="log-card">
      <template #header>
        <span>实时日志</span>
        <el-button size="small" @click="handleClearLog">清空</el-button>
      </template>
      <div class="log-container" ref="logContainer">
        <div v-for="(log, index) in logs" :key="index" :class="['log-item', log.level]">
          <span class="log-time">{{ log.time }}</span>
          <span class="log-level">[{{ log.level }}]</span>
          <span class="log-message">{{ log.message }}</span>
        </div>
      </div>
    </el-card>

    <!-- 用例执行详情表格 -->
    <el-card>
      <template #header>用例执行详情</template>
      <el-table :data="caseResults" stripe max-height="400">
        <el-table-column prop="caseNo" label="用例编号" width="120"/>
        <el-table-column prop="title" label="用例标题"/>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="耗时" width="100"/>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="handleViewResult(scope.row)">查看</el-button>
            <el-button size="small" v-if="scope.row.screenshotUrl" @click="handleViewScreenshot(scope.row)">
              截图
            </el-button>
            <el-button size="small" v-if="scope.row.videoUrl" @click="handleViewVideo(scope.row)">
              视频
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { io } from 'socket.io-client'

// WebSocket连接，实时接收执行状态
const socket = io('ws://localhost:8080/execution')

onMounted(() => {
  socket.on('execution-update', (data) => {
    // 更新执行状态
    execution.value = data
  })
  
  socket.on('log', (log) => {
    // 添加日志
    logs.value.push(log)
    scrollToBottom()
  })
})

onUnmounted(() => {
  socket.disconnect()
})
</script>
```

---

## 🔌 核心API接口设计

### 需求管理API

```java
@RestController
@RequestMapping("/api/v1/requirements")
public class RequirementController {
    
    /**
     * 创建需求
     */
    @PostMapping
    public Result<RequirementVO> create(@RequestBody @Valid RequirementDTO dto) {
        return Result.success(requirementService.create(dto));
    }
    
    /**
     * 从需求生成测试用例
     */
    @PostMapping("/{id}/generate-testcases")
    public Result<TestCaseGenerateResult> generateTestCases(
        @PathVariable Long id,
        @RequestBody AIGenerateRequest request
    ) {
        return Result.success(requirementAIService.generateTestCases(id, request));
    }
    
    /**
     * 获取需求列表
     */
    @GetMapping
    public Result<PageResult<RequirementVO>> list(@ModelAttribute RequirementQuery query) {
        return Result.success(requirementService.list(query));
    }
}
```

### AI生成API

```java
@RestController
@RequestMapping("/api/v1/ai")
public class AIGenerateController {
    
    @Autowired
    private DeepSeekClient deepSeekClient;
    
    @Autowired
    private QwenClient qwenClient;
    
    /**
     * AI生成测试用例
     */
    @PostMapping("/generate/testcases")
    public Result<List<TestCaseVO>> generateTestCases(@RequestBody AIGenerateRequest request) {
        // 1. 构建提示词
        String prompt = promptBuilder.buildPrompt(request);
        
        // 2. 调用AI服务
        String aiResponse;
        switch (request.getAiModel()) {
            case "deepseek":
                aiResponse = deepSeekClient.chat(prompt);
                break;
            case "qwen":
                aiResponse = qwenClient.chat(prompt);
                break;
            default:
                throw new BusinessException("不支持的AI模型");
        }
        
        // 3. 解析AI响应
        List<TestCaseVO> testCases = responseParser.parse(aiResponse);
        
        // 4. 保存生成记录
        aiLogService.saveLog(request, aiResponse, testCases);
        
        return Result.success(testCases);
    }
}
```

### 测试执行API

```java
@RestController
@RequestMapping("/api/v1/execution")
public class ExecutionController {
    
    /**
     * 执行测试套件
     */
    @PostMapping("/suite/{suiteId}")
    public Result<ExecutionVO> executeSuite(
        @PathVariable Long suiteId,
        @RequestBody ExecutionRequest request
    ) {
        // 异步执行测试
        Long executionId = executionService.startExecution(suiteId, request);
        return Result.success(executionService.getById(executionId));
    }
    
    /**
     * 获取执行状态（WebSocket推送）
     */
    @MessageMapping("/execution/{id}/status")
    @SendTo("/topic/execution/{id}")
    public ExecutionStatusMessage getStatus(@DestinationVariable Long id) {
        return executionService.getExecutionStatus(id);
    }
    
    /**
     * 停止执行
     */
    @PostMapping("/{id}/stop")
    public Result<Void> stop(@PathVariable Long id) {
        executionService.stopExecution(id);
        return Result.success();
    }
}
```

### GitLab Webhook API

```java
@RestController
@RequestMapping("/api/v1/webhook/gitlab")
public class GitLabWebhookController {
    
    /**
     * 接收GitLab Push事件
     */
    @PostMapping("/push")
    public Result<Void> handlePush(@RequestBody GitLabPushEvent event) {
        // 1. 验证签名
        if (!gitLabService.verifySignature(event)) {
            return Result.error("签名验证失败");
        }
        
        // 2. 解析变更
        List<String> changedFiles = event.getCommits().stream()
            .flatMap(c -> c.getModified().stream())
            .collect(Collectors.toList());
        
        // 3. 分析代码影响
        CodeImpactAnalysis analysis = codeAnalyzer.analyze(changedFiles);
        
        // 4. AI生成测试用例
        aiService.generateTestCasesFromCodeChange(analysis);
        
        // 5. 保存记录
        gitLabService.saveIntegrationLog(event);
        
        return Result.success();
    }
}
```

---

## 🚀 部署架构

### Docker Compose部署

```yaml
version: '3.8'

services:
  # MySQL数据库
  mysql:
    image: mysql:8.0
    container_name: ai-atg-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root123
      MYSQL_DATABASE: z_atg
    ports:
      - "3306:3306"
    volumes:
      - ./data/mysql:/var/lib/mysql

  # Redis缓存
  redis:
    image: redis:7-alpine
    container_name: ai-atg-redis
    ports:
      - "6379:6379"
    volumes:
      - ./data/redis:/data

  # MinIO对象存储
  minio:
    image: minio/minio
    container_name: ai-atg-minio
    command: server /data --console-address ":9001"
    environment:
      MINIO_ROOT_USER: admin
      MINIO_ROOT_PASSWORD: admin123
    ports:
      - "9000:9000"
      - "9001:9001"
    volumes:
      - ./data/minio:/data

  # Elasticsearch
  elasticsearch:
    image: elasticsearch:8.10.0
    container_name: ai-atg-es
    environment:
      - discovery.type=single-node
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
    ports:
      - "9200:9200"
    volumes:
      - ./data/es:/usr/share/elasticsearch/data

  # RabbitMQ
  rabbitmq:
    image: rabbitmq:3-management-alpine
    container_name: ai-atg-rabbitmq
    ports:
      - "5672:5672"
      - "15672:15672"
    volumes:
      - ./data/rabbitmq:/var/lib/rabbitmq

  # API Gateway
  gateway:
    build: ./ai-atg-gateway
    container_name: ai-atg-gateway
    ports:
      - "8080:8080"
    depends_on:
      - mysql
      - redis
    environment:
      SPRING_PROFILES_ACTIVE: prod

  # 后端服务（示例）
  requirement-service:
    build: ./ai-atg-requirement
    container_name: ai-atg-requirement
    depends_on:
      - mysql
      - redis
      - rabbitmq
    environment:
      SPRING_PROFILES_ACTIVE: prod

  # Selenium Grid Hub
  selenium-hub:
    image: selenium/hub:latest
    container_name: ai-atg-selenium-hub
    ports:
      - "4444:4444"

  # Selenium Chrome Node
  selenium-chrome:
    image: selenium/node-chrome:latest
    container_name: ai-atg-selenium-chrome
    depends_on:
      - selenium-hub
    environment:
      SE_EVENT_BUS_HOST: selenium-hub
      SE_EVENT_BUS_PUBLISH_PORT: 4442
      SE_EVENT_BUS_SUBSCRIBE_PORT: 4443

  # 前端
  frontend:
    build: ./ai-atg-frontend
    container_name: ai-atg-frontend
    ports:
      - "80:80"
    depends_on:
      - gateway
```

---

## 📋 开发计划

### Phase 1: 基础架构搭建 (2-3周)

**Week 1-2: 后端基础**
- ✅ 数据库设计和建表
- ✅ 后端项目骨架搭建
- ✅ 基础CRUD接口
- ✅ 认证授权系统
- ✅ 文件上传服务

**Week 3: 前端基础**
- ✅ 前端项目搭建
- ✅ 路由和布局
- ✅ 基础页面框架
- ✅ 用户登录注册

### Phase 2: 核心功能开发 (4-6周)

**Week 4-5: 需求和用例管理**
- 📝 需求CRUD
- 📝 用例CRUD
- 📝 测试套件管理

**Week 6-7: AI集成**
- 📝 DeepSeek API集成
- 📝 千问API集成
- 📝 提示词工程
- 📝 AI生成测试用例

**Week 8-9: 测试执行**
- 📝 UI测试引擎（Selenium/Playwright）
- 📝 API测试引擎
- 📝 执行调度和监控
- 📝 实时日志推送（WebSocket）

### Phase 3: 高级功能 (3-4周)

**Week 10-11: GitLab集成**
- 📝 Webhook接收
- 📝 代码分析
- 📝 自动生成用例

**Week 12-13: 测试报告**
- 📝 HTML报告生成
- 📝 PDF报告生成
- 📝 报告可视化
- 📝 趋势分析

### Phase 4: 优化和上线 (2-3周)

**Week 14-15: 性能优化**
- 📝 接口性能优化
- 📝 前端性能优化
- 📝 数据库优化

**Week 16: 上线准备**
- 📝 文档完善
- 📝 部署脚本
- 📝 监控告警
- 📝 用户培训

---

## 💡 技术亮点

### 1. AI智能生成
```
传统方式：手工编写测试用例（2-4小时）
AI方式：智能生成测试用例（2-5分钟）
效率提升：24-48倍
```

### 2. 多引擎支持
```
UI测试：Selenium + Playwright
API测试：RestAssured + HttpClient
性能测试：JMeter + K6
```

### 3. 实时监控
```
WebSocket推送执行状态
实时日志输出
动态进度更新
```

### 4. GitLab集成
```
代码提交 → Webhook触发 → 代码分析 → AI生成用例 → 自动执行
```

---

## 📚 相关文档

完成后将提供：
- 📖 系统设计文档
- 📖 API接口文档
- 📖 数据库设计文档
- 📖 部署运维文档
- 📖 用户使用手册
- 📖 开发者文档

---

<div align="center">

**AI-ATG 平台架构设计完成**

从CLI工具到企业级测试平台的完整升级方案

Made with ❤️ by James Zou | 2026

</div>
