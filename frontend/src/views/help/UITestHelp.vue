<template>
  <div class="ui-test-help">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>📖 UI自动化测试帮助中心</span>
        </div>
      </template>

      <el-tabs v-model="activeTab" type="border-card">
        <!-- 快速开始 -->
        <el-tab-pane label="🚀 快速开始" name="quickstart">
          <div class="help-content">
            <h2>5分钟快速开始</h2>
            
            <el-steps :active="currentStep" finish-status="success" align-center>
              <el-step title="安装服务" description="一次性安装" />
              <el-step title="配置WebDriver" description="必需步骤" />
              <el-step title="创建测试" description="编写测试用例" />
              <el-step title="执行测试" description="点击执行" />
              <el-step title="查看结果" description="测试报告" />
            </el-steps>

            <div class="step-content">
              <div v-if="currentStep === 0" class="step-detail">
                <h3>步骤1：安装ATG-Client</h3>
                <p>选择您的操作系统下载并安装：</p>
                
                <el-row :gutter="20">
                  <el-col :span="8">
                    <el-card shadow="hover" class="download-card">
                      <div class="os-icon">🪟</div>
                      <h4>Windows</h4>
                      <p>Windows 10/11</p>
                      <el-button type="primary" @click="downloadService('windows')">
                        <el-icon><Download /></el-icon>
                        下载安装
                      </el-button>
                    </el-card>
                  </el-col>
                  
                  <el-col :span="8">
                    <el-card shadow="hover" class="download-card">
                      <div class="os-icon">🍎</div>
                      <h4>macOS</h4>
                      <p>macOS 10.14+</p>
                      <el-button type="primary" @click="downloadService('mac')">
                        <el-icon><Download /></el-icon>
                        下载安装
                      </el-button>
                    </el-card>
                  </el-col>
                  
                  <el-col :span="8">
                    <el-card shadow="hover" class="download-card">
                      <div class="os-icon">🐧</div>
                      <h4>Linux</h4>
                      <p>Ubuntu/CentOS</p>
                      <el-button type="primary" @click="downloadService('linux')">
                        <el-icon><Download /></el-icon>
                        下载安装
                      </el-button>
                    </el-card>
                  </el-col>
                </el-row>

                <el-alert 
                  title="提示" 
                  type="info" 
                  :closable="false"
                  style="margin-top: 20px;"
                >
                  安装后服务会自动启动并开机自启，无需手动操作！
                </el-alert>

                <div class="step-actions">
                  <el-button type="primary" @click="nextStep">下一步</el-button>
                </div>
              </div>

              <div v-if="currentStep === 1" class="step-detail">
                <h3>步骤2：配置WebDriver</h3>
                
                <el-alert 
                  title="⚠️ 必需步骤" 
                  type="warning" 
                  :closable="false"
                  style="margin-bottom: 20px;"
                >
                  <strong>WebDriver是UI自动化测试的核心组件</strong>，没有它测试无法运行！
                </el-alert>

                <h4>为什么需要WebDriver？</h4>
                <p>WebDriver是浏览器的驱动程序，ATG-Client通过它控制浏览器执行测试操作。</p>

                <h4>快速配置步骤</h4>
                <el-steps direction="vertical" style="margin: 20px 0;">
                  <el-step status="process">
                    <template #title>
                      <strong>1. 确认浏览器版本</strong>
                    </template>
                    <template #description>
                      <p>Chrome: 设置 → 关于Chrome → 查看版本号</p>
                      <p>Firefox: 帮助 → 关于Firefox → 查看版本号</p>
                    </template>
                  </el-step>
                  <el-step status="process">
                    <template #title>
                      <strong>2. 下载对应的WebDriver</strong>
                    </template>
                    <template #description>
                      <el-space wrap style="margin-top: 8px;">
                        <el-button 
                          size="small" 
                          type="primary" 
                          @click="openUrl('https://registry.npmmirror.com/binary.html?path=chromedriver/')"
                        >
                          ChromeDriver (推荐镜像)
                        </el-button>
                        <el-button 
                          size="small" 
                          type="primary" 
                          @click="openUrl('https://registry.npmmirror.com/binary.html?path=geckodriver/')"
                        >
                          GeckoDriver (推荐镜像)
                        </el-button>
                      </el-space>
                    </template>
                  </el-step>
                  <el-step status="process">
                    <template #title>
                      <strong>3. 配置到系统</strong>
                    </template>
                    <template #description>
                      <p>详细配置说明请查看 ATG-Client 安装包中的 <code>WEBDRIVER_SETUP.md</code></p>
                      <el-button size="small" @click="activeTab = 'webdriver'">查看详细配置指南</el-button>
                    </template>
                  </el-step>
                </el-steps>

                <el-alert 
                  title="💡 提示" 
                  type="info" 
                  :closable="false"
                  style="margin-top: 20px;"
                >
                  配置完成后重启ATG-Client服务即可使用
                </el-alert>

                <div class="step-actions">
                  <el-button @click="prevStep">上一步</el-button>
                  <el-button type="primary" @click="nextStep">下一步</el-button>
                </div>
              </div>

              <div v-if="currentStep === 2" class="step-detail">
                <h3>步骤3：创建测试用例</h3>
                
                <el-alert 
                  title="测试步骤格式" 
                  type="success" 
                  :closable="false"
                  style="margin-bottom: 20px;"
                >
                  测试步骤使用JSON格式，支持多种操作类型
                </el-alert>

                <h4>示例：登录测试</h4>
                <pre class="code-block">{{ exampleTestCase }}</pre>

                <h4>支持的操作类型</h4>
                <el-table :data="actionTypes" border style="margin-top: 10px;">
                  <el-table-column prop="action" label="操作" width="120" />
                  <el-table-column prop="description" label="说明" />
                  <el-table-column prop="example" label="示例" />
                </el-table>

                <div class="step-actions">
                  <el-button @click="prevStep">上一步</el-button>
                  <el-button type="primary" @click="nextStep">下一步</el-button>
                </div>
              </div>

              <div v-if="currentStep === 3" class="step-detail">
                <h3>步骤4：执行测试</h3>
                
                <div class="execute-guide">
                  <el-steps direction="vertical">
                    <el-step title="进入测试用例列表" />
                    <el-step title="选择要执行的测试用例" />
                    <el-step title="点击'执行'按钮" />
                    <el-step title="系统自动在本地浏览器执行" />
                  </el-steps>
                </div>

                <el-alert 
                  title="执行模式" 
                  type="info" 
                  :closable="false"
                  style="margin-top: 20px;"
                >
                  <ul>
                    <li><strong>有头模式</strong>：可以看到浏览器实际操作（推荐调试时使用）</li>
                    <li><strong>无头模式</strong>：后台运行，不显示浏览器（节省资源）</li>
                  </ul>
                </el-alert>

                <div class="step-actions">
                  <el-button @click="prevStep">上一步</el-button>
                  <el-button type="primary" @click="nextStep">下一步</el-button>
                </div>
              </div>

              <div v-if="currentStep === 4" class="step-detail">
                <h3>步骤5：查看测试结果</h3>
                
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-card>
                      <h4>📊 测试报告包含</h4>
                      <ul>
                        <li>✅ 执行状态（通过/失败）</li>
                        <li>📋 详细日志（每步执行情况）</li>
                        <li>📷 截图（失败时的页面状态）</li>
                        <li>⏱️ 执行时间</li>
                        <li>📈 统计数据</li>
                      </ul>
                    </el-card>
                  </el-col>
                  
                  <el-col :span="12">
                    <el-card>
                      <h4>🔍 问题排查</h4>
                      <ul>
                        <li>查看失败步骤的截图</li>
                        <li>分析执行日志</li>
                        <li>检查元素定位器</li>
                        <li>调整等待时间</li>
                      </ul>
                    </el-card>
                  </el-col>
                </el-row>

                <div class="step-actions">
                  <el-button @click="prevStep">上一步</el-button>
                  <el-button type="success" @click="goToTestCase">开始创建测试</el-button>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 安装指南 -->
        <el-tab-pane label="📦 安装指南" name="installation">
          <div class="help-content">
            <h2>安装ATG-Client</h2>

            <el-collapse v-model="activeCollapse" accordion>
              <!-- Windows安装 -->
              <el-collapse-item title="🪟 Windows 安装指南" name="windows">
                <h3>系统要求</h3>
                <ul>
                  <li>Windows 10 或更高版本</li>
                  <li>管理员权限</li>
                  <li>Chrome 或 Firefox 浏览器</li>
                </ul>

                <h3>安装步骤</h3>
                <ol>
                  <li>
                    下载安装包
                    <el-button size="small" type="primary" @click="downloadService('windows')">
                      下载 Windows 版本
                    </el-button>
                  </li>
                  <li>解压到任意目录（如：C:\atg-client）</li>
                  <li>右键 <code>install.bat</code> → 选择"以管理员身份运行"</li>
                  <li>等待安装完成（约1-2分钟）</li>
                  <li>验证：访问 <a href="http://localhost:9999/health" target="_blank">http://localhost:9999/health</a></li>
                </ol>

                <h3>验证安装</h3>
                <el-button @click="checkService">检测服务状态</el-button>
                <el-tag v-if="serviceStatus !== null" :type="serviceStatus ? 'success' : 'danger'" style="margin-left: 10px;">
                  {{ serviceStatus ? '✓ 服务正常运行' : '✗ 服务未运行' }}
                </el-tag>

                <h3>卸载服务</h3>
                <pre class="code-block"># 以管理员身份运行
npm run uninstall-service</pre>
              </el-collapse-item>

              <!-- macOS安装 -->
              <el-collapse-item title="🍎 macOS 安装指南" name="mac">
                <h3>系统要求</h3>
                <ul>
                  <li>macOS 10.14 或更高版本</li>
                  <li>Terminal 访问权限</li>
                  <li>Chrome 或 Firefox 浏览器</li>
                </ul>

                <h3>安装步骤</h3>
                <ol>
                  <li>
                    下载安装包
                    <el-button size="small" type="primary" @click="downloadService('mac')">
                      下载 macOS 版本
                    </el-button>
                  </li>
                  <li>解压到任意目录</li>
                  <li>打开终端，执行以下命令：</li>
                </ol>
                <pre class="code-block">cd /path/to/atg-client
chmod +x install.sh
./install.sh</pre>

                <h3>手动启动</h3>
                <pre class="code-block">launchctl load ~/Library/LaunchAgents/com.aiatg.testservice.plist</pre>

                <h3>卸载服务</h3>
                <pre class="code-block">npm run uninstall-service</pre>
              </el-collapse-item>

              <!-- Linux安装 -->
              <el-collapse-item title="🐧 Linux 安装指南" name="linux">
                <h3>系统要求</h3>
                <ul>
                  <li>Ubuntu 18.04+ / CentOS 7+</li>
                  <li>sudo 权限</li>
                  <li>Chrome 或 Firefox 浏览器</li>
                </ul>

                <h3>安装步骤</h3>
                <ol>
                  <li>
                    下载安装包
                    <el-button size="small" type="primary" @click="downloadService('linux')">
                      下载 Linux 版本
                    </el-button>
                  </li>
                  <li>解压并安装：</li>
                </ol>
                <pre class="code-block">tar -xzf atg-client-linux.tar.gz
cd atg-client
sudo ./install.sh</pre>

                <h3>服务管理</h3>
                <pre class="code-block"># 启动服务
sudo systemctl start atg-client

# 停止服务
sudo systemctl stop atg-client

# 查看状态
sudo systemctl status atg-client</pre>

                <h3>卸载服务</h3>
                <pre class="code-block">sudo npm run uninstall-service</pre>
              </el-collapse-item>
            </el-collapse>
          </div>
        </el-tab-pane>

        <!-- WebDriver指南 -->
        <el-tab-pane label="🔧 WebDriver" name="webdriver">
          <div class="help-content">
            <h2>WebDriver 配置指南</h2>

            <el-alert 
              title="⚠️ 必需配置" 
              type="warning" 
              :closable="false"
              style="margin-bottom: 20px;"
            >
              <strong>重要！</strong> WebDriver是UI自动化测试的核心组件，<strong>必须手动下载并配置</strong>才能进行测试。
              由于网络限制，无法自动下载，请按照以下步骤手动配置。
            </el-alert>

            <h3>步骤1：确认浏览器版本</h3>
            <p>首先确认您的浏览器版本，以便下载匹配的WebDriver：</p>
            <ul>
              <li><strong>Chrome</strong>：设置 → 关于Chrome → 查看版本号（如：120.0.6099.109）</li>
              <li><strong>Firefox</strong>：帮助 → 关于Firefox → 查看版本号</li>
            </ul>

            <h3>步骤2：下载对应的WebDriver</h3>
            <el-row :gutter="20" style="margin: 20px 0;">
              <el-col :span="12">
                <el-card shadow="hover">
                  <h4>🔵 ChromeDriver</h4>
                  <p><strong>推荐使用，兼容性最好</strong></p>
                  <el-space direction="vertical" style="width: 100%;">
                    <el-button type="primary" @click="openUrl('https://registry.npmmirror.com/binary.html?path=chromedriver/')">
                      淘宝镜像（推荐）
                    </el-button>
                    <el-button @click="openUrl('https://googlechromelabs.github.io/chrome-for-testing/')">
                      官方下载
                    </el-button>
                  </el-space>
                </el-card>
              </el-col>
              
              <el-col :span="12">
                <el-card shadow="hover">
                  <h4>🦊 GeckoDriver</h4>
                  <p><strong>Firefox 专用</strong></p>
                  <el-space direction="vertical" style="width: 100%;">
                    <el-button type="primary" @click="openUrl('https://registry.npmmirror.com/binary.html?path=geckodriver/')">
                      NPM 镜像（推荐）
                    </el-button>
                    <el-button @click="openUrl('https://github.com/mozilla/geckodriver/releases')">
                      GitHub 官方
                    </el-button>
                  </el-space>
                </el-card>
              </el-col>
            </el-row>

            <h3>步骤3：配置WebDriver</h3>
            <p>下载完成后，按照以下步骤配置：</p>

            <el-table :data="webDriverLinks" border style="margin-top: 10px;">
              <el-table-column prop="browser" label="浏览器" width="120" />
              <el-table-column prop="platform" label="平台" width="120" />
              <el-table-column label="下载" width="150">
                <template #default="scope">
                  <el-button size="small" @click="downloadDriver(scope.row)">
                    下载
                  </el-button>
                </template>
              </el-table-column>
              <el-table-column prop="officialUrl" label="官方地址">
                <template #default="scope">
                  <el-link :href="scope.row.officialUrl" target="_blank" type="primary">
                    {{ scope.row.officialUrl }}
                  </el-link>
                </template>
              </el-table-column>
            </el-table>

            <el-steps direction="vertical" style="margin-top: 20px;">
              <el-step title="解压文件" status="process">
                <template #description>
                  <p>将下载的压缩包解压，得到 <code>chromedriver</code> 或 <code>geckodriver</code> 可执行文件</p>
                </template>
              </el-step>
              <el-step title="放置到正确位置" status="process">
                <template #description>
                  <p><strong>Windows：</strong> 放到 ATG-Client 安装目录的 <code>drivers\</code> 文件夹</p>
                  <p><strong>Mac/Linux：</strong> 放到 <code>~/.atg-client/drivers/</code> 或系统 PATH 路径</p>
                  <p>或在 <code>config.json</code> 中指定路径</p>
                </template>
              </el-step>
              <el-step title="设置执行权限（Mac/Linux）" status="process">
                <template #description>
                  <pre class="code-block">chmod +x chromedriver
chmod +x geckodriver</pre>
                </template>
              </el-step>
              <el-step title="重启ATG-Client" status="process">
                <template #description>
                  <p>重启服务使配置生效</p>
                </template>
              </el-step>
              <el-step title="验证配置" status="process">
                <template #description>
                  <p>访问 <code>http://localhost:9999/health</code> 检查服务状态</p>
                </template>
              </el-step>
            </el-steps>

            <el-alert 
              title="💡 配置提示" 
              type="info" 
              :closable="false"
              style="margin-top: 20px;"
            >
              <p>详细配置说明请查看ATG-Client安装包中的 <strong>WEBDRIVER_SETUP.md</strong> 文档</p>
            </el-alert>
          </div>
        </el-tab-pane>

        <!-- 使用教程 -->
        <el-tab-pane label="📚 使用教程" name="tutorial">
          <div class="help-content">
            <h2>详细使用教程</h2>

            <!-- 创建测试用例 -->
            <el-card style="margin-bottom: 20px;">
              <template #header>
                <h3>1. 创建测试用例</h3>
              </template>
              
              <h4>基本信息</h4>
              <ul>
                <li><strong>标题</strong>：简明扼要的测试名称</li>
                <li><strong>类型</strong>：选择"UI测试"</li>
                <li><strong>优先级</strong>：P0（最高）到 P3（最低）</li>
                <li><strong>前置条件</strong>：执行测试前需要满足的条件</li>
              </ul>

              <h4>编写测试步骤</h4>
              <p>使用JSON数组格式，每个步骤包含：</p>
              <pre class="code-block">{{ tutorialExample }}</pre>

              <h4>元素定位方式</h4>
              <el-table :data="locatorTypes" border style="margin-top: 10px;">
                <el-table-column prop="locator" label="定位器" width="150" />
                <el-table-column prop="description" label="说明" />
                <el-table-column prop="example" label="示例" />
              </el-table>
            </el-card>

            <!-- 执行测试 -->
            <el-card style="margin-bottom: 20px;">
              <template #header>
                <h3>2. 执行测试</h3>
              </template>
              
              <h4>执行方式</h4>
              <el-row :gutter="20">
                <el-col :span="12">
                  <h5>单个执行</h5>
                  <ol>
                    <li>在测试用例列表找到目标用例</li>
                    <li>点击"执行"按钮</li>
                    <li>选择测试环境</li>
                    <li>开始执行</li>
                  </ol>
                </el-col>
                
                <el-col :span="12">
                  <h5>批量执行</h5>
                  <ol>
                    <li>创建测试套件</li>
                    <li>添加多个测试用例</li>
                    <li>执行整个测试套件</li>
                    <li>查看汇总报告</li>
                  </ol>
                </el-col>
              </el-row>
            </el-card>

            <!-- 查看结果 -->
            <el-card>
              <template #header>
                <h3>3. 查看测试结果</h3>
              </template>
              
              <h4>测试报告包含</h4>
              <ul>
                <li>✅ <strong>执行状态</strong>：通过、失败、跳过</li>
                <li>📋 <strong>详细日志</strong>：每步操作的执行情况</li>
                <li>📷 <strong>失败截图</strong>：失败时的页面状态</li>
                <li>⏱️ <strong>执行时间</strong>：总耗时和每步耗时</li>
                <li>📊 <strong>统计分析</strong>：通过率、失败率等</li>
              </ul>

              <h4>问题排查</h4>
              <ol>
                <li><strong>元素定位失败</strong>：检查定位器是否正确，使用浏览器F12验证</li>
                <li><strong>超时</strong>：增加 timeout 时间，添加 wait 步骤</li>
                <li><strong>断言失败</strong>：检查预期值是否正确，查看截图确认实际页面</li>
                <li><strong>页面未加载完成</strong>：添加等待步骤</li>
              </ol>
            </el-card>
          </div>
        </el-tab-pane>

        <!-- FAQ -->
        <el-tab-pane label="❓ 常见问题" name="faq">
          <div class="help-content">
            <h2>常见问题解答</h2>

            <el-collapse v-model="activeFaq">
              <el-collapse-item title="Q: 服务未运行怎么办？" name="1">
                <p><strong>解决方案：</strong></p>
                <ol>
                  <li>检查系统托盘是否有AI-ATG图标</li>
                  <li>访问 <a href="http://localhost:9999/health" target="_blank">http://localhost:9999/health</a></li>
                  <li>查看服务状态（见安装指南）</li>
                  <li>重启服务或重启电脑</li>
                </ol>
              </el-collapse-item>

              <el-collapse-item title="Q: 元素定位失败？" name="2">
                <p><strong>解决方案：</strong></p>
                <ol>
                  <li>使用浏览器F12开发者工具验证定位器</li>
                  <li>确认元素在页面加载完成后存在</li>
                  <li>尝试其他定位方式（id → css → xpath）</li>
                  <li>增加等待时间</li>
                </ol>
              </el-collapse-item>

              <el-collapse-item title="Q: 浏览器启动失败？" name="3">
                <p><strong>解决方案：</strong></p>
                <ol>
                  <li>确认已安装Chrome或Firefox</li>
                  <li>更新浏览器到最新版本</li>
                  <li>检查WebDriver版本是否匹配</li>
                  <li>查看服务日志了解详细错误</li>
                </ol>
              </el-collapse-item>

              <el-collapse-item title="Q: 端口被占用？" name="4">
                <p><strong>解决方案：</strong></p>
                <ol>
                  <li>找到占用端口的程序并关闭</li>
                  <li>或修改配置文件使用其他端口</li>
                  <li>配置文件位置：<code>~/.atg-client/config.json</code></li>
                </ol>
              </el-collapse-item>

              <el-collapse-item title="Q: 如何查看日志？" name="5">
                <p><strong>日志位置：</strong></p>
                <ul>
                  <li><strong>Windows</strong>: <code>C:\Users\{用户名}\.atg-client\logs\</code></li>
                  <li><strong>macOS</strong>: <code>~/Library/Logs/atg-client.log</code></li>
                  <li><strong>Linux</strong>: <code>/var/log/atg-client.log</code></li>
                </ul>
              </el-collapse-item>

              <el-collapse-item title="Q: 如何切换浏览器？" name="6">
                <p><strong>修改配置：</strong></p>
                <pre class="code-block">{
  "browser": "chrome",  // 改为 "firefox"
  "headless": false
}</pre>
                <p>修改后重启服务</p>
              </el-collapse-item>
            </el-collapse>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
import { Download } from '@element-plus/icons-vue';
import LocalTestService from '@/utils/localTestService';

export default {
  name: 'UITestHelp',
  components: { Download },
  data() {
    return {
      activeTab: 'quickstart',
      currentStep: 0,
      activeCollapse: '',
      activeFaq: '',
      serviceStatus: null,
      
      // 示例测试用例
      exampleTestCase: JSON.stringify([
  {
    action: 'open',
    input: 'http://localhost:3000/login'
  },
  {
    action: 'input',
    locator: 'id',
    value: 'username',
    input: 'admin'
  },
  {
    action: 'input',
    locator: 'id',
    value: 'password',
    input: 'admin123'
  },
  {
    action: 'click',
    locator: 'css',
    value: 'button[type="submit"]'
  },
  {
    action: 'assertUrl',
    input: '/dashboard'
  }
], null, 2),

      // 操作类型
      actionTypes: [
        { action: 'open', description: '打开URL', example: '{ action: "open", input: "http://..." }' },
        { action: 'click', description: '点击元素', example: '{ action: "click", locator: "id", value: "btn" }' },
        { action: 'input', description: '输入文本', example: '{ action: "input", locator: "id", value: "username", input: "admin" }' },
        { action: 'assertText', description: '验证文本', example: '{ action: "assertText", locator: "css", value: ".message", input: "成功" }' }
      ],

      // 教程示例
      tutorialExample: JSON.stringify({
  action: 'click',
  locator: 'id',
  value: 'submit-button',
  timeout: 10
}, null, 2),

      // 定位器类型
      locatorTypes: [
        { locator: 'id', description: '通过ID定位', example: '{ locator: "id", value: "username" }' },
        { locator: 'css', description: 'CSS选择器', example: '{ locator: "css", value: ".btn-primary" }' },
        { locator: 'xpath', description: 'XPath路径', example: '{ locator: "xpath", value: "//button[@type=\'submit\']" }' },
        { locator: 'name', description: '通过name属性', example: '{ locator: "name", value: "email" }' }
      ],

      // WebDriver下载链接
      webDriverLinks: [
        { browser: 'Chrome', platform: 'Windows', officialUrl: 'https://chromedriver.chromium.org/' },
        { browser: 'Chrome', platform: 'macOS', officialUrl: 'https://chromedriver.chromium.org/' },
        { browser: 'Chrome', platform: 'Linux', officialUrl: 'https://chromedriver.chromium.org/' },
        { browser: 'Firefox', platform: 'Windows', officialUrl: 'https://github.com/mozilla/geckodriver/releases' },
        { browser: 'Firefox', platform: 'macOS', officialUrl: 'https://github.com/mozilla/geckodriver/releases' },
        { browser: 'Firefox', platform: 'Linux', officialUrl: 'https://github.com/mozilla/geckodriver/releases' }
      ]
    };
  },
  methods: {
    nextStep() {
      if (this.currentStep < 4) {
        this.currentStep++;
      }
    },
    
    prevStep() {
      if (this.currentStep > 0) {
        this.currentStep--;
      }
    },
    
    downloadService(platform) {
      const urls = {
        windows: '/downloads/atg-client-windows.zip',
        mac: '/downloads/atg-client-macos.zip',
        linux: '/downloads/atg-client-linux.tar.gz'
      };
      
      window.open(urls[platform]);
      this.$message.success(`正在下载 ${platform} 版本`);
    },
    
    async checkService() {
      this.serviceStatus = await LocalTestService.isRunning();
      
      if (this.serviceStatus) {
        this.$message.success('服务运行正常');
      } else {
        this.$message.warning('服务未运行，请先安装并启动服务');
      }
    },
    
    downloadDriver(row) {
      this.$message.info('请访问官方地址下载最新版本');
      window.open(row.officialUrl, '_blank');
    },
    
    openUrl(url) {
      window.open(url, '_blank');
    },
    
    goToTestCase() {
      this.$router.push('/testcase/list');
    }
  }
};
</script>

<style scoped>
.ui-test-help {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.help-content {
  padding: 20px;
}

.help-content h2 {
  margin-bottom: 20px;
  color: #409eff;
}

.help-content h3 {
  margin-top: 25px;
  margin-bottom: 15px;
  color: #606266;
}

.help-content h4 {
  margin-top: 20px;
  margin-bottom: 10px;
  color: #606266;
}

.help-content ul,
.help-content ol {
  padding-left: 25px;
}

.help-content li {
  margin: 8px 0;
  line-height: 1.6;
}

.step-content {
  margin-top: 30px;
}

.step-detail {
  min-height: 400px;
}

.step-actions {
  margin-top: 30px;
  text-align: center;
}

.download-card {
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.download-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.os-icon {
  font-size: 48px;
  margin-bottom: 10px;
}

.code-block {
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 15px;
  margin: 10px 0;
  overflow-x: auto;
  font-family: 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.6;
}

.execute-guide {
  margin: 20px 0;
}

:deep(.el-steps--vertical) {
  height: auto;
}
</style>
