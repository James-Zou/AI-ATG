<template>
  <div class="product-guide">
    <div class="guide-hero">
      <h1>AI-ATG 智能测试平台</h1>
      <p class="subtitle">基于 AI 的自动化测试生成平台</p>
      <div class="version-badges">
        <el-tag type="success" effect="dark">v1.0</el-tag>
        <el-tag type="primary" effect="dark">生产就绪</el-tag>
        <el-tag type="warning" effect="dark">AI 驱动</el-tag>
      </div>
    </div>

    <el-row :gutter="20">
      <el-col :xs="24" :lg="16">
        <el-card class="content-card" shadow="hover">
          <template #header>
            <h2>
              <el-icon><InfoFilled /></el-icon>
              什么是 AI-ATG？
            </h2>
          </template>
          <div class="content-section">
            <p class="intro-text">
              AI-ATG（AI-Automatic Test Generation）是一个创新的智能测试平台，通过将 <strong>AI 大模型</strong> 与 <strong>自动测试生成（ATG）技术</strong> 深度融合，
              实现从需求到测试用例的端到端智能化生成，帮助测试团队大幅提升效率和测试覆盖率。
            </p>

            <el-divider content-position="left">
              <el-icon><TrendCharts /></el-icon>
              核心价值
            </el-divider>

            <el-row :gutter="16" class="value-cards">
              <el-col :xs="24" :sm="12" :lg="6">
                <div class="value-item">
                  <div class="value-icon blue">
                    <el-icon><Timer /></el-icon>
                  </div>
                  <div class="value-title">效率提升</div>
                  <div class="value-desc">测试用例生成从天缩短到分钟</div>
                </div>
              </el-col>
              <el-col :xs="24" :sm="12" :lg="6">
                <div class="value-item">
                  <div class="value-icon green">
                    <el-icon><CircleCheck /></el-icon>
                  </div>
                  <div class="value-title">质量提升</div>
                  <div class="value-desc">覆盖率提升 30%+</div>
                </div>
              </el-col>
              <el-col :xs="24" :sm="12" :lg="6">
                <div class="value-item">
                  <div class="value-icon purple">
                    <el-icon><MagicStick /></el-icon>
                  </div>
                  <div class="value-title">AI 驱动</div>
                  <div class="value-desc">自然语言驱动测试生成</div>
                </div>
              </el-col>
              <el-col :xs="24" :sm="12" :lg="6">
                <div class="value-item">
                  <div class="value-icon orange">
                    <el-icon><Connection /></el-icon>
                  </div>
                  <div class="value-title">无缝集成</div>
                  <div class="value-desc">DevOps 流程深度融合</div>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>

        <el-card class="content-card" shadow="hover">
          <template #header>
            <h2>
              <el-icon><Operation /></el-icon>
              核心功能模块
            </h2>
          </template>
          <div class="content-section">
            <el-collapse v-model="activeModules" accordion>
              <el-collapse-item name="1">
                <template #title>
                  <div class="module-title">
                    <el-icon color="#409eff"><User /></el-icon>
                    <span>用户和权限管理</span>
                  </div>
                </template>
                <div class="module-content">
                  <el-tag size="small" type="success">✓ 用户注册/登录</el-tag>
                  <el-tag size="small" type="success">✓ JWT 认证</el-tag>
                  <el-tag size="small" type="success">✓ 权限控制</el-tag>
                  <el-tag size="small" type="success">✓ 路由守卫</el-tag>
                  <p class="module-desc">完善的用户身份认证与权限管理体系</p>
                </div>
              </el-collapse-item>

              <el-collapse-item name="2">
                <template #title>
                  <div class="module-title">
                    <el-icon color="#67c23a"><Document /></el-icon>
                    <span>需求管理</span>
                  </div>
                </template>
                <div class="module-content">
                  <el-tag size="small" type="success">✓ 需求 CRUD</el-tag>
                  <el-tag size="small" type="success">✓ 文件上传</el-tag>
                  <el-tag size="small" type="success">✓ 搜索筛选</el-tag>
                  <el-tag size="small" type="success">✓ 状态流转</el-tag>
                  <p class="module-desc">支持从需求文档到测试用例的全流程管理</p>
                </div>
              </el-collapse-item>

              <el-collapse-item name="3">
                <template #title>
                  <div class="module-title">
                    <el-icon color="#e6a23c"><Files /></el-icon>
                    <span>测试用例管理</span>
                  </div>
                </template>
                <div class="module-content">
                  <el-tag size="small" type="success">✓ 用例 CRUD</el-tag>
                  <el-tag size="small" type="success">✓ 测试套件</el-tag>
                  <el-tag size="small" type="success">✓ 批量操作</el-tag>
                  <el-tag size="small" type="success">✓ 导入导出</el-tag>
                  <p class="module-desc">灵活的测试用例组织与管理能力</p>
                </div>
              </el-collapse-item>

              <el-collapse-item name="4">
                <template #title>
                  <div class="module-title">
                    <el-icon color="#f56c6c"><MagicStick /></el-icon>
                    <span>AI 测试用例生成（核心功能）</span>
                  </div>
                </template>
                <div class="module-content">
                  <el-tag size="small" type="danger">★ DeepSeek 集成</el-tag>
                  <el-tag size="small" type="danger">★ 千问集成</el-tag>
                  <el-tag size="small" type="danger">★ 智谱 AI</el-tag>
                  <el-tag size="small" type="danger">★ 智能生成</el-tag>
                  <p class="module-desc">
                    <strong>平台核心能力：</strong>支持需求文档、Bug报告、代码提交等多种输入源，AI 自动推理并生成完整测试用例、测试脚本和测试数据，
                    覆盖正常场景和各类边界异常场景。
                  </p>
                  <el-alert type="info" :closable="false" show-icon>
                    <template #title>
                      <span style="font-weight: bold;">示例</span>
                    </template>
                    输入："电商订单创建接口，需要验证订单号、商品ID、数量和金额"<br>
                    输出：自动生成 20+ 个测试用例，包含正常下单、参数为空、数量非法、库存不足等全场景覆盖
                  </el-alert>
                </div>
              </el-collapse-item>

              <el-collapse-item name="5">
                <template #title>
                  <div class="module-title">
                    <el-icon color="#909399"><VideoPlay /></el-icon>
                    <span>测试执行</span>
                  </div>
                </template>
                <div class="module-content">
                  <el-tag size="small" type="success">✓ 测试执行引擎</el-tag>
                  <el-tag size="small" type="success">✓ API 测试</el-tag>
                  <el-tag size="small" type="success">✓ UI 测试</el-tag>
                  <el-tag size="small" type="success">✓ 异步执行</el-tag>
                  <p class="module-desc">强大的测试执行引擎，支持多种测试类型的自动化执行</p>
                </div>
              </el-collapse-item>

              <el-collapse-item name="6">
                <template #title>
                  <div class="module-title">
                    <el-icon color="#409eff"><DataAnalysis /></el-icon>
                    <span>测试报告</span>
                  </div>
                </template>
                <div class="module-content">
                  <el-tag size="small" type="success">✓ 报告生成</el-tag>
                  <el-tag size="small" type="success">✓ 数据统计</el-tag>
                  <el-tag size="small" type="success">✓ 图表可视化</el-tag>
                  <el-tag size="small" type="success">✓ 趋势分析</el-tag>
                  <p class="module-desc">专业的测试报告体系，支持数据统计和趋势分析</p>
                </div>
              </el-collapse-item>

              <el-collapse-item name="7">
                <template #title>
                  <div class="module-title">
                    <el-icon color="#67c23a"><Link /></el-icon>
                    <span>GitLab 集成</span>
                  </div>
                </template>
                <div class="module-content">
                  <el-tag size="small" type="success">✓ Webhook 接收</el-tag>
                  <el-tag size="small" type="success">✓ 代码变更分析</el-tag>
                  <el-tag size="small" type="success">✓ 自动触发测试</el-tag>
                  <p class="module-desc">与 GitLab 深度集成，实现 CI/CD 自动化测试</p>
                </div>
              </el-collapse-item>

              <el-collapse-item name="8">
                <template #title>
                  <div class="module-title">
                    <el-icon color="#e6a23c"><Grid /></el-icon>
                    <span>项目管理</span>
                  </div>
                </template>
                <div class="module-content">
                  <el-tag size="small" type="success">✓ 项目 CRUD</el-tag>
                  <el-tag size="small" type="success">✓ 成员管理</el-tag>
                  <el-tag size="small" type="success">✓ 项目统计</el-tag>
                  <p class="module-desc">支持多项目管理，团队协作更高效</p>
                </div>
              </el-collapse-item>
            </el-collapse>
          </div>
        </el-card>

        <el-card class="content-card" shadow="hover">
          <template #header>
            <h2>
              <el-icon><Guide /></el-icon>
              ATG 技术原理
            </h2>
          </template>
          <div class="content-section">
            <h3>什么是 ATG？</h3>
            <p>
              ATG（Automatic Test Generation，自动测试生成）是一类以"自动生成测试用例/脚本/数据"为核心的技术体系。
              传统 ATG 依赖人工定义精准规则才能生成测试，而 <strong>AI+ATG</strong> 通过大模型的自然语言理解和业务逻辑推理能力，
              支持从<strong>需求文档、Bug报告、代码提交</strong>等多种输入源到测试资产的端到端智能化生成。
            </p>

            <h3>AI+ATG 融合的核心优势</h3>
            <el-table :data="advantageData" border stripe>
              <el-table-column prop="traditional" label="传统 ATG" min-width="180" />
              <el-table-column prop="ai" label="AI+ATG" min-width="180" />
            </el-table>

            <h3 style="margin-top: 30px;">技术工作流</h3>
            <div class="workflow">
              <div class="workflow-step">
                <div class="step-number">1</div>
                <div class="step-content">
                  <div class="step-title">多源输入</div>
                  <div class="step-desc">需求文档/Bug报告/代码提交</div>
                </div>
              </div>
              <el-icon class="arrow-icon"><Right /></el-icon>
              <div class="workflow-step">
                <div class="step-number">2</div>
                <div class="step-content">
                  <div class="step-title">AI 推理</div>
                  <div class="step-desc">大模型智能分析并转化为结构化规则</div>
                </div>
              </div>
              <el-icon class="arrow-icon"><Right /></el-icon>
              <div class="workflow-step">
                <div class="step-number">3</div>
                <div class="step-content">
                  <div class="step-title">智能生成</div>
                  <div class="step-desc">生成测试用例/脚本/数据</div>
                </div>
              </div>
              <el-icon class="arrow-icon"><Right /></el-icon>
              <div class="workflow-step">
                <div class="step-number">4</div>
                <div class="step-content">
                  <div class="step-title">自动执行</div>
                  <div class="step-desc">对接测试工具自动化执行</div>
                </div>
              </div>
              <el-icon class="arrow-icon"><Right /></el-icon>
              <div class="workflow-step">
                <div class="step-number">5</div>
                <div class="step-content">
                  <div class="step-title">结果分析</div>
                  <div class="step-desc">智能分析并持续优化</div>
                </div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 第四部分：核心价值 -->
        <el-card class="content-card" shadow="hover">
          <template #header>
            <h2>
              <el-icon><Medal /></el-icon>
              核心价值
            </h2>
          </template>
          <div class="content-section">
            <el-tabs v-model="activeValueTab" type="border-card">
              <!-- 4.1 覆盖五大场景 -->
              <el-tab-pane label="覆盖五大场景" name="scenarios">
                <el-collapse v-model="activeScenarios" accordion>
                  <el-collapse-item name="1">
                    <template #title>
                      <div class="scenario-title">
                        <el-icon color="#409eff"><Document /></el-icon>
                        <span>代码变更影响范围生成用例并自动化测试</span>
                      </div>
                    </template>
                    <div class="scenario-content">
                      <p><strong>工作流程：</strong>Git Push → GitLab Webhook → 分析代码变更 → AI生成回归测试用例 → 自动执行测试 → 反馈结果</p>
                      <el-alert type="success" :closable="false">
                        <template #title>价值：快速反馈（5分钟内）、精准测试、降低风险</template>
                      </el-alert>
                    </div>
                  </el-collapse-item>

                  <el-collapse-item name="2">
                    <template #title>
                      <div class="scenario-title">
                        <el-icon color="#67c23a"><Files /></el-icon>
                        <span>需求文档生成测试用例并自动化测试</span>
                      </div>
                    </template>
                    <div class="scenario-content">
                      <p><strong>支持格式：</strong>Word、Markdown、飞书文档、腾讯文档、在线文档链接</p>
                      <p><strong>工作流程：</strong>上传需求文档 → AI解析 → 生成测试用例 → 人工审核（可选）→ 生成自动化脚本 → 执行测试</p>
                      <el-alert type="info" :closable="false">
                        <template #title>实际案例：10页需求文档 → 3分钟生成83个测试用例</template>
                      </el-alert>
                    </div>
                  </el-collapse-item>

                  <el-collapse-item name="3">
                    <template #title>
                      <div class="scenario-title">
                        <el-icon color="#e6a23c"><Warning /></el-icon>
                        <span>JIRA等平台Bug生成测试用例并自动化测试</span>
                      </div>
                    </template>
                    <div class="scenario-content">
                      <p><strong>支持平台：</strong>JIRA、禅道、Bugzilla、Redmine、自定义工单系统</p>
                      <p><strong>工作流程：</strong>监听Bug状态 → Bug修复 → AI生成回归测试用例 → 执行自动化测试 → 更新Bug状态</p>
                      <el-alert type="warning" :closable="false">
                        <template #title>价值：闭环管理、防止复现、积累测试资产</template>
                      </el-alert>
                    </div>
                  </el-collapse-item>

                  <el-collapse-item name="4">
                    <template #title>
                      <div class="scenario-title">
                        <el-icon color="#f56c6c"><Upload /></el-icon>
                        <span>外部导入用例或人工编写用例进行自动化测试</span>
                      </div>
                    </template>
                    <div class="scenario-content">
                      <p><strong>支持格式：</strong>Excel、CSV、TestLink XML、Xmind、JSON、Markdown</p>
                      <p><strong>价值：</strong>资产复用、快速转型、灵活性强</p>
                    </div>
                  </el-collapse-item>

                  <el-collapse-item name="5">
                    <template #title>
                      <div class="scenario-title">
                        <el-icon color="#909399"><Setting /></el-icon>
                        <span>Cursor等AI平台生成的用例或脚本进行自动化测试</span>
                      </div>
                    </template>
                    <div class="scenario-content">
                      <p><strong>支持工具：</strong>Cursor、GitHub Copilot、Tabnine、Amazon CodeWhisperer</p>
                      <p><strong>价值：</strong>AI生态融合、提升开发效率、代码质量保证</p>
                    </div>
                  </el-collapse-item>
                </el-collapse>
              </el-tab-pane>

              <!-- 4.2 实现三个自动化 -->
              <el-tab-pane label="三个自动化" name="automation">
                <div class="automation-grid">
                  <el-card class="automation-card">
                    <div class="auto-icon blue">
                      <el-icon><Edit /></el-icon>
                    </div>
                    <h3>自动化生成用例</h3>
                    <p>基于AI技术，从需求、代码、Bug等多种输入源自动生成高质量测试用例</p>
                    <el-tag type="info">效率提升：25-50倍</el-tag>
                  </el-card>

                  <el-card class="automation-card">
                    <div class="auto-icon green">
                      <el-icon><DocumentCopy /></el-icon>
                    </div>
                    <h3>自动化生成可执行脚本</h3>
                    <p>将自然语言描述的测试用例，转换为可执行的自动化脚本（UI/API/性能）</p>
                    <el-tag type="success">语法正确、元素准确、异常处理完善</el-tag>
                  </el-card>

                  <el-card class="automation-card">
                    <div class="auto-icon purple">
                      <el-icon><VideoPlay /></el-icon>
                    </div>
                    <h3>自动化执行测试并生成报告</h3>
                    <p>一键执行测试，实时监控进度，自动生成专业测试报告，支持人工确认</p>
                    <el-tag type="warning">执行效率提升：16倍</el-tag>
                  </el-card>
                </div>
              </el-tab-pane>

              <!-- 4.3 三种测试模式 -->
              <el-tab-pane label="三种测试模式" name="modes">
                <el-row :gutter="20">
                  <el-col :span="8">
                    <el-card class="mode-card" shadow="hover">
                      <div class="mode-header">
                        <el-icon class="mode-icon"><Monitor /></el-icon>
                        <h3>UI自动化测试</h3>
                      </div>
                      <ul class="mode-features">
                        <li>🌐 跨浏览器支持</li>
                        <li>📱 跨平台支持</li>
                        <li>🎯 智能元素定位</li>
                        <li>📸 自动截图录屏</li>
                      </ul>
                      <p class="mode-tech">技术：Selenium WebDriver、Appium</p>
                    </el-card>
                  </el-col>

                  <el-col :span="8">
                    <el-card class="mode-card" shadow="hover">
                      <div class="mode-header">
                        <el-icon class="mode-icon"><Connection /></el-icon>
                        <h3>接口自动化测试</h3>
                      </div>
                      <ul class="mode-features">
                        <li>🔌 多协议支持</li>
                        <li>🔐 认证支持</li>
                        <li>📊 数据驱动测试</li>
                        <li>✅ 智能断言</li>
                      </ul>
                      <p class="mode-tech">支持：HTTP/HTTPS、WebSocket、gRPC</p>
                    </el-card>
                  </el-col>

                  <el-col :span="8">
                    <el-card class="mode-card" shadow="hover">
                      <div class="mode-header">
                        <el-icon class="mode-icon"><TrendCharts /></el-icon>
                        <h3>性能压测自动化</h3>
                      </div>
                      <ul class="mode-features">
                        <li>📈 负载测试</li>
                        <li>🔥 压力测试</li>
                        <li>⏱️ 持续测试</li>
                        <li>📊 实时监控</li>
                      </ul>
                      <p class="mode-tech">监控：TPS、响应时间、错误率</p>
                    </el-card>
                  </el-col>
                </el-row>
              </el-tab-pane>

              <!-- 4.4 DevOps闭环 -->
              <el-tab-pane label="DevOps闭环" name="devops">
                <div class="devops-section">
                  <el-alert type="success" :closable="false" show-icon>
                    <template #title>
                      <span style="font-size: 16px; font-weight: bold;">将测试深度融入软件开发全生命周期</span>
                    </template>
                  </el-alert>

                  <div class="devops-flow">
                    <div class="devops-stage">
                      <h4>📝 需求阶段 - 测试左移</h4>
                      <p>需求评审 → AI生成测试用例 → 需求验证 → 开发编码（带测试）</p>
                      <el-tag type="success">提前发现需求问题、减少后期返工</el-tag>
                    </div>

                    <div class="devops-stage">
                      <h4>💻 开发阶段 - TDD实践</h4>
                      <p>编写测试用例 → 运行测试 → 编写代码 → 重构 → 持续迭代</p>
                      <el-tag type="primary">AI辅助生成单元测试、实时覆盖率反馈</el-tag>
                    </div>

                    <div class="devops-stage">
                      <h4>🧪 测试阶段 - 自动化测试</h4>
                      <p>代码提交 → CI/CD Pipeline → AI生成用例 → 多层次测试 → 生成报告</p>
                      <el-tag type="warning">15分钟内发现问题</el-tag>
                    </div>

                    <div class="devops-stage">
                      <h4>🚀 部署阶段 - 自动验证</h4>
                      <p>应用部署 → 冒烟测试 → 健康检查 → 性能基线 → 安全扫描</p>
                      <el-tag type="danger">失败自动回滚 + 告警</el-tag>
                    </div>

                    <div class="devops-stage">
                      <h4>📊 运维阶段 - 监控联动</h4>
                      <p>生产监控 → 发现异常 → 自动触发回归测试 → 定位问题 → 通知相关人员</p>
                      <el-tag type="info">持续改进、数据驱动</el-tag>
                    </div>
                  </div>
                </div>
              </el-tab-pane>
            </el-tabs>
          </div>
        </el-card>

        <!-- 第五部分：万物互联 -->
        <el-card class="content-card" shadow="hover">
          <template #header>
            <h2>
              <el-icon><Connection /></el-icon>
              万物互联
            </h2>
          </template>
          <div class="content-section">
            <el-tabs v-model="activeIntegrationTab" type="card">
              <!-- 5.1 GitLab集成 -->
              <el-tab-pane label="GitLab集成" name="gitlab">
                <h3>集成方式</h3>
                <el-steps :active="2" finish-status="success">
                  <el-step title="配置Webhook" description="在GitLab项目设置中添加Webhook" />
                  <el-step title="配置令牌" description="在AI-ATG中配置GitLab Access Token" />
                  <el-step title="配置规则" description="设置触发条件和执行套件" />
                  <el-step title="自动测试" description="代码提交自动触发测试" />
                </el-steps>

                <h3 style="margin-top: 30px;">代码变更分析</h3>
                <el-alert type="info" :closable="false">
                  <template #title>AI智能分析维度</template>
                  <ul style="margin: 10px 0 0 20px;">
                    <li>📝 代码行级 - 具体哪些行发生变化</li>
                    <li>🏗️ 方法级 - 哪些方法被修改</li>
                    <li>📦 类级 - 哪些类被修改</li>
                    <li>🔗 依赖级 - 影响了哪些调用方</li>
                    <li>🎯 功能级 - 影响了哪些业务功能</li>
                  </ul>
                </el-alert>

                <h3 style="margin-top: 20px;">自动化反馈</h3>
                <p>测试完成后，AI-ATG会自动：</p>
                <ul>
                  <li>✅ 更新GitLab Commit Status</li>
                  <li>💬 在Merge Request中添加测试报告评论</li>
                  <li>📊 提供详细的测试结果链接</li>
                </ul>
              </el-tab-pane>

              <!-- 5.2 JIRA集成 -->
              <el-tab-pane label="JIRA/Bug平台" name="jira">
                <h3>支持的Bug管理平台</h3>
                <el-row :gutter="16">
                  <el-col :span="6">
                    <el-tag type="primary">JIRA</el-tag>
                  </el-col>
                  <el-col :span="6">
                    <el-tag type="success">禅道</el-tag>
                  </el-col>
                  <el-col :span="6">
                    <el-tag type="warning">Bugzilla</el-tag>
                  </el-col>
                  <el-col :span="6">
                    <el-tag type="danger">Redmine</el-tag>
                  </el-col>
                </el-row>

                <h3 style="margin-top: 30px;">工作流程</h3>
                <div class="integration-flow">
                  <div class="flow-item">
                    <el-icon class="flow-icon"><Warning /></el-icon>
                    <p>Bug修复，状态改为"已解决"</p>
                  </div>
                  <el-icon class="arrow"><Right /></el-icon>
                  <div class="flow-item">
                    <el-icon class="flow-icon"><Bell /></el-icon>
                    <p>Webhook通知AI-ATG</p>
                  </div>
                  <el-icon class="arrow"><Right /></el-icon>
                  <div class="flow-item">
                    <el-icon class="flow-icon"><MagicStick /></el-icon>
                    <p>AI分析生成回归测试用例</p>
                  </div>
                  <el-icon class="arrow"><Right /></el-icon>
                  <div class="flow-item">
                    <el-icon class="flow-icon"><VideoPlay /></el-icon>
                    <p>执行自动化测试</p>
                  </div>
                  <el-icon class="arrow"><Right /></el-icon>
                  <div class="flow-item">
                    <el-icon class="flow-icon"><CircleCheck /></el-icon>
                    <p>更新Bug状态和测试结果</p>
                  </div>
                </div>

                <el-alert type="success" :closable="false" style="margin-top: 20px;">
                  <template #title>价值：闭环管理、防止Bug复现、积累测试资产</template>
                </el-alert>
              </el-tab-pane>

              <!-- 5.3 飞书集成 -->
              <el-tab-pane label="飞书/协作平台" name="lark">
                <h3>支持的协作平台</h3>
                <el-space wrap>
                  <el-tag type="primary">飞书</el-tag>
                  <el-tag type="success">钉钉</el-tag>
                  <el-tag type="warning">企业微信</el-tag>
                  <el-tag type="info">Slack</el-tag>
                  <el-tag type="danger">Microsoft Teams</el-tag>
                </el-space>

                <h3 style="margin-top: 30px;">需求文档读取</h3>
                <p>支持直接读取飞书文档、电子表格、知识库、多维表格</p>
                <el-alert type="info" :closable="false">
                  <template #title>工作流程</template>
                  <p>PM编写需求文档 → 分享链接给AI-ATG → AI读取并解析 → 生成测试用例 → 在文档下方评论</p>
                </el-alert>

                <h3 style="margin-top: 30px;">消息推送场景</h3>
                <el-collapse>
                  <el-collapse-item title="🎉 测试完成通知" name="1">
                    <p>包含：项目、套件、执行人、测试结果、通过率、执行时长、报告链接</p>
                  </el-collapse-item>
                  <el-collapse-item title="⚠️ 测试失败告警" name="2">
                    <p>包含：失败原因、失败截图、失败日志、详情链接、@相关人员</p>
                  </el-collapse-item>
                  <el-collapse-item title="🚀 代码变更测试通知" name="3">
                    <p>包含：提交者、分支、影响范围、生成的测试用例数、测试结果</p>
                  </el-collapse-item>
                  <el-collapse-item title="📋 人工确认提醒" name="4">
                    <p>包含：报告名称、测试摘要、等待时长、确认链接、@相关人员</p>
                  </el-collapse-item>
                </el-collapse>
              </el-tab-pane>
            </el-tabs>
          </div>
        </el-card>

        <!-- 第六部分：实用技巧 -->
        <el-card class="content-card" shadow="hover">
          <template #header>
            <h2>
              <el-icon><Promotion /></el-icon>
              实用技巧
            </h2>
          </template>
          <div class="content-section">
            <el-tabs v-model="activeTipsTab" type="border-card">
              <!-- 6.1 UI自动化技巧 -->
              <el-tab-pane label="UI自动化提高准确率" name="ui">
                <h3>没有前端代码的情况</h3>
                <el-collapse>
                  <el-collapse-item title="方法一：通过审查元素复制源代码" name="1">
                    <el-steps direction="vertical">
                      <el-step title="打开开发者工具" description="Chrome: F12 或 右键→检查" />
                      <el-step title="定位目标元素" description="使用元素选择器（Ctrl+Shift+C）点击页面元素" />
                      <el-step title="复制元素代码" description="右键→Copy→Copy selector / Copy XPath" />
                      <el-step title="提供给AI生成脚本" description="将代码和测试需求一起提供给AI" />
                    </el-steps>
                  </el-collapse-item>

                  <el-collapse-item title="方法二：通过截图生成脚本" name="2">
                    <p>截取页面截图，上传给AI并描述操作步骤，AI会根据截图识别元素并生成脚本</p>
                    <el-alert type="warning" :closable="false">
                      <template #title>注意：截图需包含要操作的元素</template>
                    </el-alert>
                  </el-collapse-item>

                  <el-collapse-item title="方法三：使用浏览器录制工具" name="3">
                    <p><strong>推荐工具：</strong></p>
                    <ul>
                      <li>Selenium IDE - Chrome/Firefox扩展</li>
                      <li>Katalon Recorder - 功能更强大</li>
                      <li>TestCafe Studio - 商业工具</li>
                    </ul>
                  </el-collapse-item>
                </el-collapse>

                <h3 style="margin-top: 30px;">有前端代码的情况</h3>
                <el-alert type="success" :closable="false" show-icon>
                  <template #title>
                    <span style="font-weight: bold;">推荐方式：使用Cursor AI直接生成</span>
                  </template>
                  <ol style="margin: 10px 0 0 20px;">
                    <li>在Cursor中打开前端工程</li>
                    <li>使用Prompt：请根据 @LoginForm.vue 生成UI自动化测试脚本</li>
                    <li>Cursor基于代码分析生成精确脚本</li>
                    <li>复制脚本到AI-ATG执行</li>
                  </ol>
                </el-alert>

                <h3 style="margin-top: 20px;">最佳实践 - 添加测试标记</h3>
                <pre style="background: #f5f7fa; padding: 15px; border-radius: 8px; overflow-x: auto;">
&lt;input data-testid="username-input" data-test-label="用户名输入框" /&gt;
&lt;button data-testid="login-button" data-test-label="登录按钮"&gt;登录&lt;/button&gt;</pre>
                <p style="margin-top: 10px;"><strong>好处：</strong>定位更准确、不受样式变化影响、语义化更强、易于维护</p>
              </el-tab-pane>

              <!-- 6.2 接口/性能自动化技巧 -->
              <el-tab-pane label="接口/性能自动化" name="api">
                <h3>没有代码的情况</h3>
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-card class="tip-card">
                      <h4>方法一：根据接口文档生成</h4>
                      <p>支持的文档格式：</p>
                      <ul>
                        <li>Swagger/OpenAPI (JSON/YAML)</li>
                        <li>Postman Collection</li>
                        <li>API Blueprint</li>
                        <li>RAML</li>
                      </ul>
                      <el-tag type="primary">直接导入AI-ATG，AI自动解析生成测试</el-tag>
                    </el-card>
                  </el-col>

                  <el-col :span="12">
                    <el-card class="tip-card">
                      <h4>方法二：使用AILM工具</h4>
                      <p>AILM可以：</p>
                      <ul>
                        <li>🔍 自动扫描后端工程</li>
                        <li>📋 列出所有接口（URL、Method、参数）</li>
                        <li>📊 分析接口依赖关系</li>
                        <li>🔗 导出接口文档</li>
                      </ul>
                      <el-tag type="success">直接获取当前后端工程所有接口</el-tag>
                    </el-card>
                  </el-col>
                </el-row>

                <h3 style="margin-top: 30px;">有代码的情况</h3>
                <el-alert type="info" :closable="false" show-icon>
                  <template #title>
                    <span style="font-weight: bold;">推荐方式：Cursor直接生成接口测试</span>
                  </template>
                  <p style="margin: 10px 0;">在Cursor中打开后端工程，使用Prompt：</p>
                  <pre style="background: #fff; padding: 10px; border: 1px solid #ddd; border-radius: 4px; overflow-x: auto;">
请根据 @UserController.java 生成API自动化测试脚本

要求：
1. 覆盖所有接口
2. 包含正常和异常场景
3. 使用AI-ATG的JSON格式
4. 添加参数校验测试</pre>
                  <p style="margin-top: 10px;">Cursor会自动分析Controller代码，生成完整的接口测试脚本</p>
                </el-alert>

                <h3 style="margin-top: 30px;">效率对比</h3>
                <el-table :data="efficiencyData" border stripe>
                  <el-table-column prop="type" label="测试类型" width="150" />
                  <el-table-column prop="traditional" label="传统方式" width="120" />
                  <el-table-column prop="aiAtg" label="使用AI-ATG" width="120" />
                  <el-table-column prop="improvement" label="效率提升" />
                </el-table>
              </el-tab-pane>
            </el-tabs>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="8">
        <el-card class="sidebar-card" shadow="hover">
          <template #header>
            <h2>
              <el-icon><Platform /></el-icon>
              技术栈
            </h2>
          </template>
          <div class="tech-section">
            <h3>后端技术</h3>
            <el-tag v-for="tech in backendTech" :key="tech" class="tech-tag">{{ tech }}</el-tag>

            <h3>前端技术</h3>
            <el-tag v-for="tech in frontendTech" :key="tech" class="tech-tag" type="success">{{ tech }}</el-tag>

            <h3>AI 模型</h3>
            <el-tag v-for="tech in aiTech" :key="tech" class="tech-tag" type="danger">{{ tech }}</el-tag>

            <h3>基础设施</h3>
            <el-tag v-for="tech in infraTech" :key="tech" class="tech-tag" type="warning">{{ tech }}</el-tag>
          </div>
        </el-card>

        <el-card class="sidebar-card" shadow="hover">
          <template #header>
            <h2>
              <el-icon><Odometer /></el-icon>
              项目统计
            </h2>
          </template>
          <div class="stats-section">
            <div class="stat-item">
              <div class="stat-label">代码量</div>
              <div class="stat-value">21,300+ 行</div>
            </div>
            <el-divider />
            <div class="stat-item">
              <div class="stat-label">开发进度</div>
              <div class="stat-value">100%</div>
            </div>
            <el-divider />
            <div class="stat-item">
              <div class="stat-label">核心模块</div>
              <div class="stat-value">10 个</div>
            </div>
            <el-divider />
            <div class="stat-item">
              <div class="stat-label">当前版本</div>
              <div class="stat-value">v1.0</div>
            </div>
          </div>
        </el-card>

        <el-card class="sidebar-card" shadow="hover">
          <template #header>
            <h2>
              <el-icon><QuestionFilled /></el-icon>
              快速开始
            </h2>
          </template>
          <div class="quick-start">
            <el-steps direction="vertical" :active="3">
              <el-step title="注册登录" description="创建账户并登录系统" />
              <el-step title="创建项目" description="建立您的测试项目" />
              <el-step title="添加需求" description="上传或创建测试需求" />
              <el-step title="AI 生成" description="使用 AI 生成测试用例" />
              <el-step title="执行测试" description="运行测试并查看报告" />
            </el-steps>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import {
  InfoFilled, TrendCharts, Timer, CircleCheck, MagicStick,
  Connection, Operation, User, Document, Files, VideoPlay,
  DataAnalysis, Link, Grid, Guide, Right, Platform,
  Odometer, QuestionFilled, Medal, Warning, Upload,
  Setting, Edit, DocumentCopy, Monitor, Bell, Promotion
} from '@element-plus/icons-vue'

const activeModules = ref(['4'])
const activeValueTab = ref('scenarios')
const activeScenarios = ref(['1'])
const activeIntegrationTab = ref('gitlab')
const activeTipsTab = ref('ui')

const advantageData = ref([
  {
    traditional: '需人工定义精准规则',
    ai: '支持自然语言输入，自动推理规则'
  },
  {
    traditional: '对复杂业务适配性差',
    ai: '通过业务推理自动挖掘场景'
  },
  {
    traditional: '生成的资产需人工修改',
    ai: '识别项目上下文，零二次修改'
  },
  {
    traditional: '跨场景适配性差',
    ai: '多场景自动泛化，全栈支持'
  }
])

const efficiencyData = ref([
  {
    type: 'UI测试脚本编写',
    traditional: '2小时',
    aiAtg: '10分钟',
    improvement: '12倍'
  },
  {
    type: '接口测试脚本编写',
    traditional: '1小时',
    aiAtg: '5分钟',
    improvement: '12倍'
  },
  {
    type: '性能测试配置',
    traditional: '4小时',
    aiAtg: '15分钟',
    improvement: '16倍'
  }
])

const backendTech = ref([
  'Spring Boot 3.2',
  'Spring Security',
  'MyBatis Plus',
  'MySQL 8.0',
  'Redis',
  'JWT'
])

const frontendTech = ref([
  'Vue 3.4',
  'Element Plus',
  'Pinia',
  'Vue Router',
  'Axios',
  'Vite 5.0',
  'ECharts'
])

const aiTech = ref([
  'DeepSeek',
  '千问',
  '智谱 AI',
  'GPT-4'
])

const infraTech = ref([
  'Docker',
  'MinIO',
  'RabbitMQ',
  'Elasticsearch',
  'Selenium Grid'
])
</script>

<style scoped>
.product-guide {
  padding: 0;
}

.guide-hero {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 60px 30px;
  border-radius: 12px;
  margin-bottom: 20px;
  text-align: center;
  color: white;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.3);
}

.guide-hero h1 {
  margin: 0 0 16px 0;
  font-size: 42px;
  font-weight: 700;
}

.subtitle {
  font-size: 18px;
  margin: 0 0 24px 0;
  opacity: 0.95;
}

.version-badges {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.version-badges :deep(.el-tag) {
  font-size: 14px;
  padding: 8px 16px;
}

.content-card,
.sidebar-card {
  margin-bottom: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.content-card :deep(.el-card__header),
.sidebar-card :deep(.el-card__header) {
  border-bottom: 2px solid #f0f0f0;
  padding: 20px;
}

.content-card h2,
.sidebar-card h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 10px;
}

.content-section {
  padding: 10px;
}

.intro-text {
  font-size: 16px;
  line-height: 1.8;
  color: #606266;
  margin-bottom: 30px;
}

.value-cards {
  margin-top: 20px;
}

.value-item {
  text-align: center;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 12px;
  transition: all 0.3s ease;
  margin-bottom: 16px;
}

.value-item:hover {
  background: #ecf5ff;
  transform: translateY(-4px);
}

.value-icon {
  width: 60px;
  height: 60px;
  margin: 0 auto 12px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
}

.value-icon.blue {
  background: linear-gradient(135deg, #409eff, #66b1ff);
}

.value-icon.green {
  background: linear-gradient(135deg, #67c23a, #85ce61);
}

.value-icon.purple {
  background: linear-gradient(135deg, #a855f7, #c084fc);
}

.value-icon.orange {
  background: linear-gradient(135deg, #e6a23c, #f39c12);
}

.value-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.value-desc {
  font-size: 14px;
  color: #909399;
}

.module-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
}

.module-content {
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.module-content .el-tag {
  margin: 4px 8px 4px 0;
}

.module-desc {
  margin-top: 12px;
  color: #606266;
  line-height: 1.6;
}

.content-section h3 {
  color: #303133;
  margin: 24px 0 16px 0;
  font-size: 18px;
  font-weight: 600;
}

.content-section p {
  line-height: 1.8;
  color: #606266;
  margin-bottom: 16px;
}

.workflow {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 20px;
}

.workflow-step {
  flex: 1;
  min-width: 140px;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8eaed 100%);
  border-radius: 12px;
  transition: all 0.3s ease;
}

.workflow-step:hover {
  background: linear-gradient(135deg, #ecf5ff 0%, #d9ecff 100%);
  transform: translateY(-4px);
}

.step-number {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #409eff, #66b1ff);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 12px;
}

.step-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
  text-align: center;
}

.step-desc {
  font-size: 13px;
  color: #909399;
  text-align: center;
}

.arrow-icon {
  font-size: 24px;
  color: #409eff;
}

.tech-section h3 {
  color: #303133;
  margin: 20px 0 12px 0;
  font-size: 16px;
  font-weight: 600;
}

.tech-section h3:first-child {
  margin-top: 0;
}

.tech-tag {
  margin: 4px 8px 4px 0;
}

.stats-section {
  padding: 10px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
}

.quick-start {
  padding: 10px;
}

.quick-start :deep(.el-step__title) {
  font-size: 15px;
}

.quick-start :deep(.el-step__description) {
  font-size: 13px;
}

/* 场景样式 */
.scenario-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  font-weight: 600;
}

.scenario-content {
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.scenario-content p {
  margin: 12px 0;
  line-height: 1.6;
}

.scenario-content ul {
  margin: 10px 0 0 20px;
}

.scenario-content ul li {
  margin: 6px 0;
}

/* 自动化卡片网格 */
.automation-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.automation-card {
  text-align: center;
  padding: 30px 20px;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.automation-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.auto-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 16px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  color: white;
}

.auto-icon.blue {
  background: linear-gradient(135deg, #409eff, #66b1ff);
}

.auto-icon.green {
  background: linear-gradient(135deg, #67c23a, #85ce61);
}

.auto-icon.purple {
  background: linear-gradient(135deg, #a855f7, #c084fc);
}

.automation-card h3 {
  font-size: 18px;
  color: #303133;
  margin: 0 0 12px 0;
}

.automation-card p {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin: 0 0 16px 0;
}

/* 测试模式卡片 */
.mode-card {
  height: 100%;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.mode-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.mode-header {
  text-align: center;
  margin-bottom: 20px;
}

.mode-icon {
  font-size: 48px;
  color: #409eff;
  margin-bottom: 12px;
}

.mode-card h3 {
  font-size: 16px;
  color: #303133;
  margin: 12px 0;
}

.mode-features {
  list-style: none;
  padding: 0;
  margin: 20px 0;
}

.mode-features li {
  padding: 8px 0;
  font-size: 14px;
  color: #606266;
  border-bottom: 1px dashed #ebeef5;
}

.mode-features li:last-child {
  border-bottom: none;
}

.mode-tech {
  text-align: center;
  font-size: 13px;
  color: #909399;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

/* DevOps流程 */
.devops-section {
  padding: 20px;
}

.devops-flow {
  margin-top: 30px;
}

.devops-stage {
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
  border-left: 4px solid #409eff;
  border-radius: 8px;
  margin-bottom: 20px;
  transition: all 0.3s ease;
}

.devops-stage:hover {
  background: linear-gradient(135deg, #ecf5ff 0%, #ffffff 100%);
  border-left-color: #67c23a;
  transform: translateX(4px);
}

.devops-stage h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 16px;
}

.devops-stage p {
  margin: 0 0 12px 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
}

/* 集成流程 */
.integration-flow {
  display: flex;
  align-items: center;
  justify-content: space-around;
  flex-wrap: wrap;
  gap: 16px;
  margin: 30px 0;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 12px;
}

.flow-item {
  text-align: center;
  flex: 1;
  min-width: 140px;
}

.flow-icon {
  font-size: 36px;
  color: #409eff;
  margin-bottom: 12px;
}

.flow-item p {
  font-size: 13px;
  color: #606266;
  margin: 0;
}

.flow-item .arrow {
  font-size: 20px;
  color: #909399;
}

/* 提示卡片 */
.tip-card {
  height: 100%;
  border-radius: 8px;
}

.tip-card h4 {
  color: #303133;
  font-size: 16px;
  margin: 0 0 16px 0;
}

.tip-card p {
  color: #606266;
  font-size: 14px;
  margin: 8px 0;
}

.tip-card ul {
  margin: 12px 0;
  padding-left: 20px;
}

.tip-card ul li {
  margin: 8px 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
}

@media (max-width: 992px) {
  .guide-hero h1 {
    font-size: 32px;
  }

  .workflow {
    flex-direction: column;
  }

  .arrow-icon {
    transform: rotate(90deg);
  }

  .workflow-step {
    width: 100%;
  }

  .integration-flow {
    flex-direction: column;
  }

  .integration-flow .arrow {
    transform: rotate(90deg);
  }

  .automation-grid {
    grid-template-columns: 1fr;
  }
}
</style>
