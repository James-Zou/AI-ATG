<template>
  <div class="download-center">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>💾 下载中心</span>
        </div>
      </template>

      <!-- ATG-Client -->
      <el-card shadow="hover" style="margin-bottom: 20px;">
        <template #header>
          <h3>🚀 ATG-Client（推荐）</h3>
        </template>
        
        <el-row :gutter="20">
          <el-col :span="6">
            <el-statistic title="版本" :value="serviceVersion" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="大小" value="~25MB" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="下载次数" :value="1234" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="更新时间" value="2026-01-28" />
          </el-col>
        </el-row>

        <el-divider />

        <h4>特性</h4>
        <ul>
          <li>✅ 一次安装，开机自启动</li>
          <li>✅ Web页面直接调用，无需手动启动</li>
          <li>✅ 系统托盘显示状态</li>
          <li>✅ 手动配置WebDriver，适应网络限制</li>
          <li>✅ 支持所有Selenium操作</li>
        </ul>

        <el-divider />

        <h4>系统要求</h4>
        <ul>
          <li>Node.js 18+ （或使用打包版本，无需Node.js）</li>
          <li>Chrome 或 Firefox 浏览器</li>
          <li><strong>对应的 WebDriver（需手动下载配置）</strong></li>
          <li>500MB 可用磁盘空间</li>
        </ul>

        <el-divider />

        <h4>下载</h4>
        <el-space wrap>
          <el-button type="primary" size="large" @click="downloadFile('service-windows')">
            <el-icon><Download /></el-icon>
            Windows 版本
          </el-button>
          <el-button type="primary" size="large" @click="downloadFile('service-mac')">
            <el-icon><Download /></el-icon>
            macOS 版本
          </el-button>
          <el-button type="primary" size="large" @click="downloadFile('service-linux')">
            <el-icon><Download /></el-icon>
            Linux 版本
          </el-button>
        </el-space>

        <el-alert 
          title="安装提示" 
          type="info" 
          :closable="false"
          style="margin-top: 15px;"
        >
          <div>1. 下载并解压安装包</div>
          <div>2. 按照 <strong>WEBDRIVER_SETUP.md</strong> 配置 WebDriver</div>
          <div>3. 运行 install.bat (Windows) 或 install.sh (Mac/Linux) 完成安装</div>
        </el-alert>
      </el-card>

      <!-- WebDriver -->
      <el-card shadow="hover" style="margin-bottom: 20px;">
        <template #header>
          <h3>🔧 WebDriver（必需）</h3>
        </template>
        
        <el-alert 
          title="手动配置" 
          type="warning" 
          :closable="false"
          style="margin-bottom: 15px;"
        >
          <strong>重要提示！</strong> 由于网络限制，需要手动下载并配置 WebDriver
        </el-alert>

        <p>请根据您的浏览器版本，从官方或镜像站下载对应的 WebDriver：</p>

        <h4>ChromeDriver 下载地址</h4>
        <el-space direction="vertical" style="margin-bottom: 15px; width: 100%;">
          <el-button type="primary" plain @click="openOfficial('chrome')">
            官方下载站
          </el-button>
          <el-button type="primary" plain @click="openOfficial('chrome-mirror')">
            淘宝镜像（推荐）
          </el-button>
          <el-button type="primary" plain @click="openOfficial('chrome-npmmirror')">
            NPM 镜像
          </el-button>
        </el-space>

        <h4>GeckoDriver (Firefox) 下载地址</h4>
        <el-space direction="vertical" style="margin-bottom: 15px; width: 100%;">
          <el-button type="primary" plain @click="openOfficial('firefox')">
            GitHub 官方
          </el-button>
          <el-button type="primary" plain @click="openOfficial('firefox-mirror')">
            NPM 镜像
          </el-button>
        </el-space>

        <el-alert 
          title="配置说明" 
          type="info" 
          :closable="false"
          style="margin-top: 15px;"
        >
          下载后请按照 <code>agent-service/WEBDRIVER_SETUP.md</code> 文档进行配置
        </el-alert>
      </el-card>


      <!-- 文档资源 -->
      <el-card shadow="hover" style="margin-bottom: 20px;">
        <template #header>
          <h3>📖 文档资源</h3>
        </template>
        
        <el-table :data="documents" border>
          <el-table-column prop="name" label="文档名称" width="200" />
          <el-table-column prop="description" label="说明" />
          <el-table-column label="操作" width="200">
            <template #default="scope">
              <el-button size="small" @click="viewDoc(scope.row.id)">
                在线查看
              </el-button>
              <el-button size="small" type="primary" @click="downloadDoc(scope.row.id)">
                下载PDF
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 示例测试用例 -->
      <el-card shadow="hover">
        <template #header>
          <h3>📝 示例测试用例</h3>
        </template>
        
        <el-table :data="sampleTests" border>
          <el-table-column prop="name" label="用例名称" width="200" />
          <el-table-column prop="description" label="说明" />
          <el-table-column label="操作" width="200">
            <template #default="scope">
              <el-button size="small" @click="previewSample(scope.row)">
                预览
              </el-button>
              <el-button size="small" type="primary" @click="importSample(scope.row)">
                导入
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </el-card>

    <!-- 示例预览对话框 -->
    <el-dialog
      v-model="previewDialogVisible"
      title="测试用例预览"
      width="700px"
    >
      <div v-if="currentSample">
        <h4>{{ currentSample.name }}</h4>
        <p>{{ currentSample.description }}</p>
        <h4>测试步骤</h4>
        <pre class="code-block">{{ currentSample.steps }}</pre>
      </div>
      <template #footer>
        <el-button @click="previewDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="importSample(currentSample)">
          导入到测试用例
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { Download } from '@element-plus/icons-vue';

export default {
  name: 'DownloadCenter',
  components: { Download },
  data() {
    return {
      serviceVersion: '1.0.0',
      previewDialogVisible: false,
      currentSample: null,
      
      // 文档
      documents: [
        {
          id: 'quickstart',
          name: '快速开始指南',
          description: '5分钟快速入门UI自动化测试'
        },
        {
          id: 'installation',
          name: '安装配置指南',
          description: '详细的安装和配置步骤'
        },
        {
          id: 'tutorial',
          name: '使用教程',
          description: '从创建到执行的完整教程'
        },
        {
          id: 'api-reference',
          name: 'API参考文档',
          description: '支持的操作类型和参数说明'
        },
        {
          id: 'troubleshooting',
          name: '故障排查指南',
          description: '常见问题和解决方案'
        }
      ],
      
      // 示例测试用例
      sampleTests: [
        {
          id: 'sample-login',
          name: '登录功能测试',
          description: '用户名密码登录的完整测试流程',
          steps: JSON.stringify([
            { action: 'open', input: 'http://localhost:3000/login' },
            { action: 'input', locator: 'id', value: 'username', input: 'admin' },
            { action: 'input', locator: 'id', value: 'password', input: 'admin123' },
            { action: 'click', locator: 'css', value: 'button[type="submit"]' },
            { action: 'assertUrl', input: '/dashboard' }
          ], null, 2)
        },
        {
          id: 'sample-form',
          name: '表单填写测试',
          description: '包含各种表单元素的填写测试',
          steps: JSON.stringify([
            { action: 'open', input: 'http://localhost:3000/form' },
            { action: 'input', locator: 'id', value: 'name', input: '张三' },
            { action: 'input', locator: 'id', value: 'email', input: 'test@example.com' },
            { action: 'select', locator: 'id', value: 'country', input: '中国' },
            { action: 'click', locator: 'id', value: 'submit' },
            { action: 'assertText', locator: 'css', value: '.success', input: '提交成功' }
          ], null, 2)
        },
        {
          id: 'sample-search',
          name: '搜索功能测试',
          description: '搜索框输入和结果验证',
          steps: JSON.stringify([
            { action: 'open', input: 'http://localhost:3000' },
            { action: 'input', locator: 'css', value: 'input[type="search"]', input: '测试' },
            { action: 'click', locator: 'css', value: '.search-button' },
            { action: 'wait', timeout: 2 },
            { action: 'assertText', locator: 'css', value: '.result-count', input: '找到' }
          ], null, 2)
        }
      ]
    };
  },
  methods: {
    downloadFile(fileId) {
      const fileMap = {
        'service-windows': '/api/downloads/atg-client-windows.zip',
        'service-mac': '/api/downloads/atg-client-macos.zip',
        'service-linux': '/api/downloads/atg-client-linux.tar.gz'
      };
      
      const url = fileMap[fileId];
      if (url) {
        window.open(url);
        this.$message.success('开始下载');
      } else {
        this.$message.warning('下载文件不存在');
      }
    },
    
    openOfficial(browser) {
      const urls = {
        chrome: 'https://chromedriver.chromium.org/downloads',
        'chrome-mirror': 'https://registry.npmmirror.com/binary.html?path=chromedriver/',
        'chrome-npmmirror': 'https://registry.npmmirror.com/-/binary/chromedriver/',
        firefox: 'https://github.com/mozilla/geckodriver/releases',
        'firefox-mirror': 'https://registry.npmmirror.com/binary.html?path=geckodriver/'
      };
      
      window.open(urls[browser], '_blank');
    },
    
    viewDoc(docId) {
      this.$router.push(`/help/ui-test?tab=${docId}`);
    },
    
    downloadDoc(docId) {
      this.$message.info('PDF文档生成功能开发中');
    },
    
    previewSample(sample) {
      this.currentSample = sample;
      this.previewDialogVisible = true;
    },
    
    importSample(sample) {
      this.$router.push({
        path: '/testcase/create',
        query: {
          from: 'sample',
          title: sample.name,
          steps: sample.steps
        }
      });
      
      this.$message.success('示例已加载，请继续编辑');
    }
  }
};
</script>

<style scoped>
.download-center {
  padding: 20px;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}

h3 {
  margin-bottom: 15px;
  color: #409eff;
}

h4 {
  margin-top: 20px;
  margin-bottom: 10px;
  color: #606266;
}

ul {
  padding-left: 25px;
}

li {
  margin: 8px 0;
  line-height: 1.6;
}

code {
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
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

:deep(.el-statistic__title) {
  font-size: 14px;
}

:deep(.el-statistic__content) {
  font-size: 20px;
}
</style>
