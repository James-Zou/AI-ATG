<template>
  <div v-if="!serviceRunning" class="service-alert">
    <el-alert
      title="本地测试服务未运行"
      type="warning"
      :closable="false"
      show-icon
    >
      <template #default>
        <p>执行UI自动化测试需要安装并启动ATG-Client客户端。</p>
        <div class="alert-actions">
          <el-button size="small" type="primary" @click="checkService">
            重新检测
          </el-button>
          <el-button size="small" @click="showInstallDialog">
            下载安装
          </el-button>
          <el-button size="small" @click="showHelpDialog">
            帮助文档
          </el-button>
        </div>
      </template>
    </el-alert>
    
    <!-- 安装对话框 -->
    <el-dialog
      v-model="installDialogVisible"
      title="安装ATG-Client"
      width="600px"
    >
      <div class="install-content">
        <h3>📦 安装步骤</h3>
        <ol>
          <li>下载对应系统的安装包</li>
          <li>解压到任意目录</li>
          <li>运行安装脚本（需要管理员权限）</li>
          <li>服务会自动启动并开机自启</li>
        </ol>
        
        <h3>💾 下载链接</h3>
        <div class="download-links">
          <el-button type="primary" @click="downloadWindows">
            <i class="el-icon-download"></i> Windows 版本
          </el-button>
          <el-button type="primary" @click="downloadMac">
            <i class="el-icon-download"></i> macOS 版本
          </el-button>
          <el-button type="primary" @click="downloadLinux">
            <i class="el-icon-download"></i> Linux 版本
          </el-button>
        </div>
        
        <h3>📝 系统要求</h3>
        <ul>
          <li>Node.js 18+ 或使用打包的可执行文件</li>
          <li>Chrome 或 Firefox 浏览器</li>
          <li>500MB 可用磁盘空间</li>
        </ul>
      </div>
    </el-dialog>
    
    <!-- 帮助对话框 -->
    <el-dialog
      v-model="helpDialogVisible"
      title="本地测试服务帮助"
      width="600px"
    >
      <div class="help-content">
        <h3>❓ 什么是ATG-Client？</h3>
        <p>ATG-Client是一个专业的客户端程序，运行在您的电脑上，负责执行UI自动化测试。</p>
        
        <h3>✨ 主要特点</h3>
        <ul>
          <li>✅ 一次安装，开机自启动</li>
          <li>✅ 后台运行，用户无感知</li>
          <li>✅ 在Web页面直接点击执行</li>
          <li>✅ 支持所有Selenium操作</li>
          <li>✅ 系统托盘显示状态</li>
        </ul>
        
        <h3>🔧 故障排查</h3>
        <p><strong>服务未运行？</strong></p>
        <ol>
          <li>检查系统托盘是否有ATG-Client图标</li>
          <li>访问 http://localhost:9999/health 检查</li>
          <li>查看日志文件</li>
          <li>手动重启服务</li>
        </ol>
        
        <p><strong>端口冲突？</strong></p>
        <p>修改配置文件中的端口号。</p>
        
        <p><strong>浏览器启动失败？</strong></p>
        <p>确认已安装Chrome或Firefox浏览器。</p>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import LocalTestService from '@/utils/localTestService';

export default {
  name: 'LocalServiceChecker',
  data() {
    return {
      serviceRunning: false,
      installDialogVisible: false,
      helpDialogVisible: false,
      checkTimer: null
    };
  },
  mounted() {
    this.checkService();
    // 每30秒检查一次服务状态
    this.checkTimer = setInterval(() => {
      this.checkService();
    }, 30000);
  },
  beforeUnmount() {
    if (this.checkTimer) {
      clearInterval(this.checkTimer);
    }
  },
  methods: {
    async checkService() {
      this.serviceRunning = await LocalTestService.isRunning();
      this.$emit('service-status', this.serviceRunning);
    },
    showInstallDialog() {
      this.installDialogVisible = true;
    },
    showHelpDialog() {
      this.helpDialogVisible = true;
    },
    downloadWindows() {
      window.open('/downloads/atg-client-windows.zip');
    },
    downloadMac() {
      window.open('/downloads/atg-client-macos.zip');
    },
    downloadLinux() {
      window.open('/downloads/atg-client-linux.tar.gz');
    }
  }
};
</script>

<style scoped>
.service-alert {
  margin-bottom: 20px;
}

.alert-actions {
  margin-top: 10px;
}

.install-content h3,
.help-content h3 {
  margin-top: 20px;
  margin-bottom: 10px;
  color: #409eff;
}

.install-content ul,
.install-content ol,
.help-content ul,
.help-content ol {
  padding-left: 20px;
}

.install-content li,
.help-content li {
  margin: 5px 0;
}

.download-links {
  display: flex;
  gap: 10px;
  margin: 15px 0;
}
</style>
