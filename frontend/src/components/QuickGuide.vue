<template>
  <el-card class="quick-guide" v-if="showGuide">
    <template #header>
      <div class="guide-header">
        <span>{{ getGuideTitle() }}</span>
        <el-button type="text" @click="closeGuide">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
    </template>

    <!-- UI自动化测试指南 -->
    <el-steps v-if="testType === 'ui'" :active="clientStatus === 'running' ? 2 : 1" direction="vertical">
      <el-step 
        title="安装ATG-Client" 
        :status="clientStatus === 'running' ? 'success' : 'wait'"
      >
        <template #description>
          <div class="step-content">
            <!-- 运行中状态 -->
            <div v-if="clientStatus === 'running'" class="status-card running">
              <el-icon class="status-icon"><CircleCheck /></el-icon>
              <div>
                <p class="status-text">ATG-Client 已运行</p>
                <p class="status-detail">服务地址: http://localhost:9999</p>
              </div>
            </div>
            
            <!-- 检测中状态 -->
            <div v-else-if="clientStatus === 'checking'" class="status-card checking">
              <el-icon class="status-icon loading"><Loading /></el-icon>
              <p class="status-text">正在检测客户端状态...</p>
            </div>
            
            <!-- 未安装状态 -->
            <div v-else class="status-card not-installed">
              <el-icon class="status-icon"><WarningFilled /></el-icon>
              <div>
                <p class="status-text">ATG-Client 未运行</p>
                <p class="status-detail">首次使用需要安装ATG-Client客户端</p>
                <el-button size="small" type="primary" @click="goToDownload" style="margin-top: 8px;">
                  前往下载
                </el-button>
              </div>
            </div>
          </div>
        </template>
      </el-step>

      <el-step title="配置WebDriver" status="wait">
        <template #description>
          <div class="step-content">
            <el-alert 
              type="warning" 
              :closable="false"
              show-icon
            >
              <template #title>
                <strong>⚠️ 必需步骤</strong>
              </template>
              <p style="margin: 8px 0;">没有WebDriver插件，UI自动化测试无法进行</p>
            </el-alert>
            
            <div style="margin-top: 12px;">
              <p>请根据您的浏览器下载对应的WebDriver：</p>
              <ul style="margin: 8px 0; padding-left: 20px;">
                <li>Chrome → ChromeDriver</li>
                <li>Firefox → GeckoDriver</li>
              </ul>
              <el-space wrap style="margin-top: 8px;">
                <el-button size="small" type="primary" @click="goToWebDriverGuide">
                  <el-icon><Tools /></el-icon>
                  查看配置指南
                </el-button>
                <el-button size="small" @click="goToDownload">
                  <el-icon><Download /></el-icon>
                  下载中心
                </el-button>
              </el-space>
            </div>
          </div>
        </template>
      </el-step>

      <el-step title="创建测试用例" status="wait">
        <template #description>
          <div class="step-content">
            <p>编写测试步骤（JSON格式）</p>
            <el-link type="primary" @click="viewExample">查看示例</el-link>
          </div>
        </template>
      </el-step>

      <el-step title="执行测试" status="wait">
        <template #description>
          <div class="step-content">
            <p>点击"开始执行"按钮，系统自动在本地浏览器执行</p>
          </div>
        </template>
      </el-step>

      <el-step title="查看报告" status="wait">
        <template #description>
          <div class="step-content">
            <p>查看执行日志、截图和统计数据</p>
          </div>
        </template>
      </el-step>
    </el-steps>

    <!-- 接口自动化测试指南 -->
    <el-steps v-else-if="testType === 'api'" :active="1" direction="vertical">
      <el-step title="配置接口请求" status="process">
        <template #description>
          <div class="step-content">
            <el-alert type="success" :closable="false" show-icon>
              <template #title>
                <strong>✨ 无需本地环境</strong>
              </template>
              <p style="margin: 8px 0;">接口测试在服务端执行，无需安装ATG-Client</p>
            </el-alert>
            
            <div style="margin-top: 12px;">
              <p><strong>配置内容包括：</strong></p>
              <ul style="margin: 8px 0; padding-left: 20px;">
                <li>HTTP请求方法（GET/POST/PUT/DELETE）</li>
                <li>请求URL和参数</li>
                <li>请求头（Headers）</li>
                <li>请求体（Body）</li>
                <li>断言规则（状态码、响应内容）</li>
              </ul>
            </div>
          </div>
        </template>
      </el-step>

      <el-step title="执行测试" status="wait">
        <template #description>
          <div class="step-content">
            <p>点击"开始执行"按钮，JMeter将在服务端自动执行</p>
            <el-tag size="small" type="success" style="margin-top: 8px;">执行速度快</el-tag>
            <el-tag size="small" type="info" style="margin-left: 8px;">支持并发</el-tag>
          </div>
        </template>
      </el-step>

      <el-step title="查看结果" status="wait">
        <template #description>
          <div class="step-content">
            <p>查看请求响应、断言结果和执行统计</p>
          </div>
        </template>
      </el-step>
    </el-steps>

    <!-- 性能自动化测试指南 -->
    <el-steps v-else-if="testType === 'performance'" :active="1" direction="vertical">
      <el-step title="配置压测场景" status="process">
        <template #description>
          <div class="step-content">
            <el-alert type="warning" :closable="false" show-icon>
              <template #title>
                <strong>⚡ 服务端执行</strong>
              </template>
              <p style="margin: 8px 0;">性能测试在服务端执行，支持高并发压测</p>
            </el-alert>
            
            <div style="margin-top: 12px;">
              <p><strong>关键配置：</strong></p>
              <ul style="margin: 8px 0; padding-left: 20px;">
                <li><strong>并发用户数：</strong>模拟的虚拟用户数量</li>
                <li><strong>Ramp-Up时间：</strong>逐步加压的时间（秒）</li>
                <li><strong>持续时间：</strong>压测持续的总时间</li>
                <li><strong>目标TPS：</strong>期望的每秒事务数</li>
                <li><strong>响应时间阈值：</strong>判定成功的时间标准</li>
              </ul>
            </div>
          </div>
        </template>
      </el-step>

      <el-step title="执行压测" status="wait">
        <template #description>
          <div class="step-content">
            <p>点击"开始执行"按钮，JMeter将在服务端启动压力测试</p>
            <div style="margin-top: 8px;">
              <el-tag size="small" type="warning">高并发</el-tag>
              <el-tag size="small" type="info" style="margin-left: 8px;">实时监控</el-tag>
              <el-tag size="small" type="success" style="margin-left: 8px;">详细报告</el-tag>
            </div>
            <el-alert type="info" :closable="false" style="margin-top: 12px;" show-icon>
              <p style="margin: 0;">⚠️ 注意：高并发压测可能对目标系统造成较大压力，请谨慎配置</p>
            </el-alert>
          </div>
        </template>
      </el-step>

      <el-step title="分析报告" status="wait">
        <template #description>
          <div class="step-content">
            <p><strong>查看性能指标：</strong></p>
            <ul style="margin: 8px 0; padding-left: 20px;">
              <li>TPS（每秒事务数）</li>
              <li>响应时间（平均值、中位数、95线、99线）</li>
              <li>错误率</li>
              <li>吞吐量</li>
              <li>并发用户数变化</li>
            </ul>
          </div>
        </template>
      </el-step>
    </el-steps>

    <!-- 通用指南（未选择类型） -->
    <div v-else class="no-type-selected">
      <el-alert type="info" :closable="false" show-icon>
        <template #title>
          <strong>请先选择测试类型</strong>
        </template>
        <p style="margin: 8px 0;">AI-ATG 支持三种自动化测试类型：</p>
      </el-alert>
      
      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="8">
          <el-card shadow="hover" class="type-card">
            <div class="type-icon">🖥️</div>
            <h3>UI自动化测试</h3>
            <p>通过ATG-Client在本地浏览器执行UI操作</p>
            <ul>
              <li>需要安装ATG-Client</li>
              <li>需要配置WebDriver</li>
              <li>支持真实浏览器环境</li>
            </ul>
          </el-card>
        </el-col>
        
        <el-col :span="8">
          <el-card shadow="hover" class="type-card">
            <div class="type-icon">🔌</div>
            <h3>接口自动化测试</h3>
            <p>通过JMeter在服务端执行接口测试</p>
            <ul>
              <li>无需本地环境</li>
              <li>执行速度快</li>
              <li>支持并发测试</li>
            </ul>
          </el-card>
        </el-col>
        
        <el-col :span="8">
          <el-card shadow="hover" class="type-card">
            <div class="type-icon">⚡</div>
            <h3>性能自动化测试</h3>
            <p>通过JMeter在服务端执行性能压测</p>
            <ul>
              <li>高并发支持</li>
              <li>详细性能指标</li>
              <li>实时监控</li>
            </ul>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <el-divider />

    <div class="quick-links">
      <el-link type="primary" @click="goToHelp">
        <el-icon><Document /></el-icon>
        完整文档
      </el-link>
      <el-link type="primary" @click="goToDownload">
        <el-icon><Download /></el-icon>
        下载中心
      </el-link>
      <el-link type="primary" @click="goToWebDriverGuide">
        <el-icon><Tools /></el-icon>
        WebDriver配置
      </el-link>
      <el-link type="primary" @click="goToFAQ">
        <el-icon><QuestionFilled /></el-icon>
        常见问题
      </el-link>
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { Close, Document, Download, QuestionFilled, CircleCheck, WarningFilled, Loading, Tools } from '@element-plus/icons-vue';
import axios from 'axios';

const props = defineProps({
  testType: {
    type: String,
    default: '', // ui, api, performance
    validator: (value) => ['', 'ui', 'api', 'performance'].includes(value)
  }
});

const router = useRouter();
const showGuide = ref(true);
const clientStatus = ref('checking'); // checking, running, not-installed
let statusCheckInterval = null;

// 检测ATG-Client运行状态
const checkClientStatus = async () => {
  try {
    const response = await axios.get('http://localhost:9999/health', {
      timeout: 2000
    });
    
    if (response.data && response.data.status === 'ok') {
      clientStatus.value = 'running';
    } else {
      clientStatus.value = 'not-installed';
    }
  } catch (error) {
    clientStatus.value = 'not-installed';
  }
};

// 从localStorage读取显示状态
onMounted(() => {
  const hideGuide = localStorage.getItem('hideQuickGuide');
  if (hideGuide === 'true') {
    showGuide.value = false;
  }
  
  // 只有UI自动化测试才需要检测ATG-Client状态
  if (props.testType === 'ui') {
    // 初始检测
    checkClientStatus();
    
    // 每10秒检测一次状态
    statusCheckInterval = setInterval(checkClientStatus, 10000);
  } else {
    // 其他类型不需要检测
    clientStatus.value = 'not-required';
  }
});

onUnmounted(() => {
  if (statusCheckInterval) {
    clearInterval(statusCheckInterval);
  }
});

const closeGuide = () => {
  showGuide.value = false;
  localStorage.setItem('hideQuickGuide', 'true');
};

const goToHelp = () => {
  router.push('/help/ui-test');
};

const goToDownload = () => {
  router.push('/help/downloads');
};

const goToFAQ = () => {
  router.push('/help/ui-test?tab=faq');
};

const viewExample = () => {
  router.push('/help/ui-test?tab=tutorial');
};

const goToWebDriverGuide = () => {
  router.push('/help/ui-test?tab=webdriver');
};

// 获取指南标题
const getGuideTitle = () => {
  const titles = {
    'ui': '💡 UI自动化测试快速指南',
    'api': '💡 接口自动化测试快速指南',
    'performance': '💡 性能自动化测试快速指南'
  };
  return titles[props.testType] || '💡 快速使用指南';
};
</script>

<style scoped>
.quick-guide {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.guide-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: white;
}

.step-content {
  color: #606266;
  padding: 10px 0;
}

.step-content p {
  margin-bottom: 10px;
}

.quick-links {
  display: flex;
  gap: 20px;
  justify-content: center;
}

:deep(.el-card__header) {
  background: transparent;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  color: white;
}

:deep(.el-card__body) {
  background: white;
  border-radius: 0 0 4px 4px;
}

:deep(.el-step__title) {
  color: #303133;
}

:deep(.el-step__description) {
  color: #606266;
}

.status-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 6px;
  margin-bottom: 8px;
}

.status-card.running {
  background: #f0f9ff;
  border: 1px solid #67c23a;
}

.status-card.checking {
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
}

.status-card.not-installed {
  background: #fef0f0;
  border: 1px solid #f56c6c;
}

.status-icon {
  font-size: 24px;
  flex-shrink: 0;
}

.status-card.running .status-icon {
  color: #67c23a;
}

.status-card.checking .status-icon {
  color: #909399;
}

.status-card.not-installed .status-icon {
  color: #f56c6c;
}

.status-text {
  font-weight: 600;
  margin: 0;
  color: #303133;
}

.status-detail {
  font-size: 12px;
  color: #606266;
  margin: 4px 0 0 0;
}

.loading {
  animation: rotating 1s linear infinite;
}

@keyframes rotating {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.no-type-selected {
  padding: 20px;
}

.type-card {
  text-align: center;
  height: 100%;
}

.type-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.type-card h3 {
  margin: 12px 0;
  color: #303133;
  font-size: 16px;
}

.type-card p {
  margin: 8px 0;
  color: #606266;
  font-size: 14px;
}

.type-card ul {
  text-align: left;
  margin: 12px 0;
  padding-left: 20px;
  color: #909399;
  font-size: 13px;
}

.type-card ul li {
  margin: 6px 0;
}
</style>
