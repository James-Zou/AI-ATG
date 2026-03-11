<template>
  <div class="layout">
    <el-container>
      <el-header>
        <div class="header-left">
          <h1>AI-ATG 自动化平台</h1>
        </div>
        <div class="header-right">
          <el-button 
            text 
            class="guide-button"
            @click="goToGuide"
          >
            <el-icon><Reading /></el-icon>
            <span>产品说明</span>
          </el-button>
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><User /></el-icon>
              {{ userStore.username }}
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <el-container>
        <el-aside width="200px">
          <el-menu
            :default-active="activeMenu"
            @select="handleMenuSelect"
          >
            <el-menu-item index="/dashboard">
              <el-icon><Odometer /></el-icon>
              <span>仪表盘</span>
            </el-menu-item>
            <el-menu-item index="/atgbot">
              <el-icon><ChatDotRound /></el-icon>
              <span>ATG Bot</span>
            </el-menu-item>
            <el-menu-item index="/skills/list">
              <el-icon><MagicStick /></el-icon>
              <span>技能列表</span>
            </el-menu-item>
            <el-menu-item index="/requirement/list">
              <el-icon><Document /></el-icon>
              <span>需求管理</span>
            </el-menu-item>
            <el-sub-menu index="testcase">
              <template #title>
                <el-icon><Files /></el-icon>
                <span>测试用例</span>
              </template>
              <el-menu-item index="/testcase/list">用例列表</el-menu-item>
              <el-menu-item index="/testsuite/manage">套件管理</el-menu-item>
              <el-menu-item index="/interface/list">接口全集</el-menu-item>
            </el-sub-menu>
            <el-sub-menu index="ai">
              <template #title>
                <el-icon><MagicStick /></el-icon>
                <span>AI 生成</span>
              </template>
              <el-menu-item index="/ai/generate">用例生成</el-menu-item>
              <el-menu-item index="/ai/config">AI 配置</el-menu-item>
            </el-sub-menu>
            <el-menu-item index="/execution/list">
              <el-icon><VideoPlay /></el-icon>
              <span>测试执行</span>
            </el-menu-item>
            <el-menu-item index="/environment/list">
              <el-icon><Setting /></el-icon>
              <span>环境管理</span>
            </el-menu-item>
            <el-menu-item index="/report/list">
              <el-icon><Document /></el-icon>
              <span>测试报告</span>
            </el-menu-item>
            <el-menu-item index="/gitlab/config">
              <el-icon><Link /></el-icon>
              <span>GitLab集成</span>
            </el-menu-item>
            <el-menu-item index="/project/list">
              <el-icon><Grid /></el-icon>
              <span>项目管理</span>
            </el-menu-item>
            <el-menu-item index="/user-manage">
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </el-menu-item>
            <el-menu-item index="/api-key/manage">
              <el-icon><Key /></el-icon>
              <span>API Key 管理</span>
            </el-menu-item>
            <el-sub-menu index="help">
              <template #title>
                <el-icon><QuestionFilled /></el-icon>
                <span>帮助中心</span>
              </template>
              <el-menu-item index="/help/ui-test">UI测试指南</el-menu-item>
              <el-menu-item index="/help/downloads">下载中心</el-menu-item>
            </el-sub-menu>
          </el-menu>
        </el-aside>
        
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Odometer, Document, Files, MagicStick, VideoPlay, Link, Grid, Reading, Setting, QuestionFilled, Key, ChatDotRound } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const handleMenuSelect = (index) => {
  router.push(index)
}

const goToGuide = () => {
  router.push('/product-guide')
}

const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/login')
    }).catch(() => {})
  } else if (command === 'profile') {
    ElMessage.info('个人信息功能开发中...')
  }
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.el-header {
  background-color: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left h1 {
  font-size: 20px;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.guide-button {
  color: white;
  font-size: 15px;
  padding: 8px 16px;
  border-radius: 4px;
  transition: all 0.3s;
}

.guide-button:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.guide-button span {
  margin-left: 6px;
}

.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.el-aside {
  background-color: white;
  min-height: calc(100vh - 60px);
}

.el-main {
  padding: 20px;
}
</style>
