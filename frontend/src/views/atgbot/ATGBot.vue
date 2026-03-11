<template>
  <div class="atgbot-container">
    <!-- 科技感背景 -->
    <div class="tech-background">
      <div class="grid-overlay"></div>
      <div class="gradient-orb orb-1"></div>
      <div class="gradient-orb orb-2"></div>
      <div class="gradient-orb orb-3"></div>
    </div>

    <!-- 智能 Skills 面板 -->
    <div class="quick-actions-section" :class="{ 'collapsed': collapsed }">
      <div class="section-header">
        <div class="header-left">
          <el-icon class="header-icon"><MagicStick /></el-icon>
          <span class="header-title">ATG Bot - 企业级智能助手</span>
          <el-tag :type="botStatus === 'thinking' ? 'warning' : 'success'" effect="dark">
            {{ botStatus === 'thinking' ? '执行中' : '就绪' }}
          </el-tag>
        </div>
        <el-button 
          circle 
          size="small" 
          @click="collapsed = !collapsed"
          class="collapse-btn"
        >
          <el-icon v-if="collapsed"><ArrowDown /></el-icon>
          <el-icon v-else><ArrowUp /></el-icon>
        </el-button>
      </div>

      <transition name="slide-fade">
        <div class="quick-actions-grid" v-show="!collapsed">
          <div
            v-for="skill in skills"
            :key="skill.id"
            class="suite-card"
            @click="sendQuickAction(skill)"
          >
          <div class="card-glow"></div>
          <div class="card-content">
            <el-icon class="card-icon"><Files /></el-icon>
            <div class="card-title">{{ skill.name }}</div>
            <div class="card-desc">{{ skill.description || 'Skill: 自动化执行流程' }}</div>
            <div class="card-stats">
              <span class="stat-item">
                <el-icon><Document /></el-icon>
                {{ skill.stepCount || 0 }} 个操作步骤
              </span>
            </div>
          </div>
        </div>
        
        <div v-if="skills.length === 0" class="empty-suites">
          <el-icon class="empty-icon"><FolderOpened /></el-icon>
          <p>暂无可用 Skills</p>
        </div>
      </div>
      </transition>
    </div>

    <!-- 聊天区域 -->
    <div class="chat-section">
      <div class="chat-messages" ref="messagesContainer">
        <div 
          v-for="(message, index) in messages" 
          :key="index"
          :class="['message-item', message.role === 'user' ? 'user-message' : 'bot-message']"
        >
          <div class="message-avatar">
            <el-icon v-if="message.role === 'user'">
              <User />
            </el-icon>
            <el-icon v-else>
              <Avatar />
            </el-icon>
          </div>
          <div class="message-content">
            <div class="message-text">{{ message.content }}</div>
            <div v-if="message.suiteInfo" class="suite-info">
              <el-card shadow="hover">
                <div class="suite-header">
                  <el-icon><Files /></el-icon>
                  <span>🎯 已识别 Skill: {{ message.suiteInfo.name }}</span>
                </div>
                <div class="suite-actions">
                  <el-button 
                    type="primary" 
                    size="small"
                    @click="executeSuite(message.suiteInfo.id)"
                    :loading="executing"
                  >
                    触发 Skill
                  </el-button>
                  <el-button 
                    size="small"
                    @click="viewSuite(message.suiteInfo.id)"
                  >
                    查看配置
                  </el-button>
                </div>
              </el-card>
            </div>
            <!-- 候选技能卡片 -->
            <div v-if="message.candidateSkills && message.candidateSkills.length > 0" class="candidate-skills">
              <div class="candidate-skills-header">
                <el-icon><MagicStick /></el-icon>
                <span>🎯 请选择要执行的技能：</span>
              </div>
              <div class="candidate-skills-grid">
                <el-card 
                  v-for="skill in message.candidateSkills" 
                  :key="skill.id"
                  shadow="hover"
                  class="candidate-skill-card"
                  @click="handleSkillSelect(skill)"
                >
                  <div class="skill-card-content">
                    <div class="skill-card-title">{{ skill.name }}</div>
                    <div class="skill-card-desc">{{ skill.description }}</div>
                    <div class="skill-card-type">
                      <el-tag size="small" :type="skill.type === 'TESTSUITE' ? 'primary' : 'success'">
                        {{ skill.type === 'TESTSUITE' ? '测试套件' : '自定义脚本' }}
                      </el-tag>
                    </div>
                  </div>
                </el-card>
              </div>
            </div>
            <div v-if="message.executionResult" class="execution-result">
              <el-alert
                :type="message.executionResult.status === 'success' ? 'success' : 'error'"
                :closable="false"
              >
                <template #title>
                  Skill 执行结果: {{ message.executionResult.message }}
                </template>
              </el-alert>
            </div>
            <div class="message-time">{{ formatTime(message.timestamp) }}</div>
          </div>
        </div>
        <div v-if="loading" class="typing-indicator">
          <span></span>
          <span></span>
          <span></span>
        </div>
      </div>

      <div class="chat-input-section">
        <el-input
          v-model="inputMessage"
          type="textarea"
          :rows="3"
          placeholder="输入自然语言指令，例如: 执行数据库备份任务、查询服务器状态..."
          @keydown.enter.ctrl="sendMessage"
          :disabled="loading"
        />
        <div class="input-actions">
          <el-button 
            type="primary" 
            @click="sendMessage"
            :loading="loading"
            :disabled="!inputMessage.trim()"
          >
            发送 (Ctrl+Enter)
          </el-button>
          <el-button @click="clearMessages">清空对话</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Avatar, Files, MagicStick, Document, FolderOpened, ArrowDown, ArrowUp } from '@element-plus/icons-vue'
import { sendChatMessage, executeTestSuite } from '@/api/atgbot'
import { getSkillList, executeSkill, getScriptExecutionDetail } from '@/api/skills'
import { getExecutionDetail } from '@/api/execution'
import { useRouter } from 'vue-router'

const router = useRouter()
const messagesContainer = ref(null)
const messages = ref([])
const inputMessage = ref('')
const loading = ref(false)
const executing = ref(false)
const botStatus = ref('idle')
const skills = ref([])
const collapsed = ref(false) // 快速操作区域折叠状态

onMounted(async () => {
  loadChatHistory()
  await loadSkills()
  // 添加欢迎消息
  if (messages.value.length === 0) {
    addWelcomeMessage()
  }
})

const loadSkills = async () => {
  try {
    const response = await getSkillList({ pageNum: 1, pageSize: 6 })
    skills.value = response.data.records || []
  } catch (error) {
    console.error('加载技能列表失败:', error)
  }
}

const addWelcomeMessage = () => {
  const welcomeMessage = {
    role: 'assistant',
    content: '👋 您好！我是 ATG Bot，您的企业级智能自动化助手。\n\n🎯 核心能力：\n• 自然语言指令解析\n• 自动化 Skill 执行\n• 第三方接口集成\n• 实时结果反馈\n\n💡 使用方式：\n✨ 点击上方 Skill 卡片快速触发\n💬 用自然语言描述自动化需求\n📝 例如："执行数据库备份"、"查询服务器状态"',
    timestamp: new Date()
  }
  messages.value.push(welcomeMessage)
  saveChatHistory()
}

const sendQuickAction = async (skill) => {
  if (executing.value || loading.value) {
    return
  }

  // 添加用户消息
  const userMessage = {
    role: 'user',
    content: `触发 Skill: ${skill.name}`,
    timestamp: new Date()
  }
  messages.value.push(userMessage)
  saveChatHistory()
  await scrollToBottom()

  executing.value = true
  botStatus.value = 'thinking'

  try {
    // 执行Skill（传递参数）
    const response = await executeSkill(skill.id, skill.parameters || {})
    const executionId = response.data

    // 添加执行开始的消息
    const startMessage = {
      role: 'assistant',
      content: `🚀 正在执行 Skill: ${skill.name}`,
      timestamp: new Date(),
      executionId: executionId
    }
    messages.value.push(startMessage)
    saveChatHistory()
    await scrollToBottom()

    // 轮询执行结果
    await pollExecutionResult(executionId, skill.name, skill.type)

  } catch (error) {
    ElMessage.error(error.message || 'Skill 执行失败')
    const errorMessage = {
      role: 'assistant',
      content: `❌ Skill 执行失败: ${error.message || '未知错误'}`,
      timestamp: new Date()
    }
    messages.value.push(errorMessage)
    saveChatHistory()
    await scrollToBottom()
  } finally {
    executing.value = false
    botStatus.value = 'idle'
  }
}

// 轮询执行结果
const pollExecutionResult = async (executionId, skillName, skillType) => {
  const maxAttempts = 60 // 最多轮询60次（30秒）
  const pollInterval = 500 // 每500ms轮询一次
  
  for (let attempt = 0; attempt < maxAttempts; attempt++) {
    try {
      // 根据技能类型选择不同的 API
      let response
      if (skillType === 'TESTSUITE') {
        response = await getExecutionDetail(executionId)
      } else if (skillType === 'SCRIPT') {
        response = await getScriptExecutionDetail(executionId)
      } else {
        throw new Error(`未知的技能类型: ${skillType}`)
      }
      
      const execution = response.data

      // 判断执行状态（不同技能类型状态表示不同）
      let isSuccess = false
      let isFailed = false
      let isRunning = false

      if (skillType === 'TESTSUITE') {
        // TestExecution: status=1(执行中), status=2(已完成), status=4(失败)
        isSuccess = execution.status === 2
        isFailed = execution.status === 4
        isRunning = execution.status === 1
      } else if (skillType === 'SCRIPT') {
        // ScriptExecution: status='SUCCESS', 'FAILED', 'PENDING', 'RUNNING'
        isSuccess = execution.status === 'SUCCESS'
        isFailed = execution.status === 'FAILED'
        isRunning = execution.status === 'PENDING' || execution.status === 'RUNNING'
      }

      if (isSuccess) {
        // 执行成功
        const resultMessage = {
          role: 'assistant',
          content: `✅ Skill "${skillName}" 执行成功\n\n📊 执行输出：\n${execution.output || execution.executionName || '无输出'}`,
          timestamp: new Date(),
          executionResult: {
            status: 'success',
            executionId: executionId,
            durationMs: execution.durationMs || execution.duration
          }
        }
        messages.value.push(resultMessage)
        saveChatHistory()
        await scrollToBottom()
        ElMessage.success('Skill 执行成功')
        return
      } else if (isFailed) {
        // 执行失败
        const errorMessage = {
          role: 'assistant',
          content: `❌ Skill "${skillName}" 执行失败\n\n📄 错误信息：\n${execution.errorMessage || '未知错误'}`,
          timestamp: new Date(),
          executionResult: {
            status: 'error',
            executionId: executionId
          }
        }
        messages.value.push(errorMessage)
        saveChatHistory()
        await scrollToBottom()
        ElMessage.error('Skill 执行失败')
        return
      } else if (isRunning) {
        // 等待下一次轮询
        await new Promise(resolve => setTimeout(resolve, pollInterval))
      } else {
        // 未知状态
        throw new Error(`未知的执行状态: ${execution.status}`)
      }
    } catch (error) {
      console.error('轮询执行结果失败:', error)
      // 继续轮询
      await new Promise(resolve => setTimeout(resolve, pollInterval))
    }
  }

  // 超时
  const timeoutMessage = {
    role: 'assistant',
    content: `⏱️ Skill "${skillName}" 执行超时（超过30秒）\n\n您可以稍后在技能列表中查看执行结果。`,
    timestamp: new Date()
  }
  messages.value.push(timeoutMessage)
  saveChatHistory()
  await scrollToBottom()
  ElMessage.warning('Skill 执行超时')
}

const loadChatHistory = () => {
  const history = localStorage.getItem('atgbot_chat_history')
  if (history) {
    messages.value = JSON.parse(history)
    scrollToBottom()
  }
}

const saveChatHistory = () => {
  localStorage.setItem('atgbot_chat_history', JSON.stringify(messages.value))
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || loading.value) {
    return
  }

  const userMessage = {
    role: 'user',
    content: inputMessage.value.trim(),
    timestamp: new Date()
  }

  messages.value.push(userMessage)
  const userInput = inputMessage.value.trim()
  inputMessage.value = ''
  loading.value = true
  botStatus.value = 'thinking'

  await scrollToBottom()

  try {
    const response = await sendChatMessage({
      message: userInput
    })

    const botMessage = {
      role: 'assistant',
      content: response.data.reply,
      timestamp: new Date(),
      suiteInfo: response.data.suiteInfo,
      skillInfo: response.data.skillInfo, // 添加技能信息
      candidateSkills: response.data.candidateSkills, // 添加候选技能列表
      executionResult: null
    }

    messages.value.push(botMessage)
    saveChatHistory()
    await scrollToBottom()

    // 如果AI识别到要执行技能，自动触发执行
    if (response.data.skillInfo && response.data.skillInfo.id) {
      const skillInfo = response.data.skillInfo
      await sendQuickAction({
        id: skillInfo.id,
        name: skillInfo.name,
        description: skillInfo.description,
        parameters: skillInfo.parameters // 传递AI识别的参数
      })
    }
  } catch (error) {
    ElMessage.error(error.message || '发送消息失败')
    const errorMessage = {
      role: 'assistant',
      content: '抱歉，我遇到了一些问题，请稍后再试。',
      timestamp: new Date()
    }
    messages.value.push(errorMessage)
  } finally {
    loading.value = false
    botStatus.value = 'idle'
  }
}

const handleSkillSelect = async (skill) => {
  if (executing.value || loading.value) {
    return
  }

  // 添加用户选择的技能消息
  const userMessage = {
    role: 'user',
    content: `选择执行 Skill: ${skill.name}`,
    timestamp: new Date()
  }
  messages.value.push(userMessage)
  saveChatHistory()
  await scrollToBottom()

  // 执行选中的技能
  await sendQuickAction(skill)
}

const executeSuite = async (suiteId) => {
  if (executing.value) {
    return
  }

  executing.value = true
  try {
    const response = await executeTestSuite(suiteId)
    
    const resultMessage = {
      role: 'assistant',
      content: '✅ Skill 已成功触发',
      timestamp: new Date(),
      executionResult: {
        status: 'success',
        message: `任务ID: ${response.data.executionId}，正在执行自动化流程...`
      }
    }

    messages.value.push(resultMessage)
    saveChatHistory()
    await scrollToBottom()

    ElMessage.success('Skill 执行成功')
  } catch (error) {
    ElMessage.error(error.message || '执行失败')
    const errorMessage = {
      role: 'assistant',
      content: '❌ Skill 执行失败',
      timestamp: new Date(),
      executionResult: {
        status: 'error',
        message: error.message || '执行出错'
      }
    }
    messages.value.push(errorMessage)
  } finally {
    executing.value = false
  }
}

const viewSuite = (suiteId) => {
  router.push(`/testsuite/detail/${suiteId}`)
}

const clearMessages = () => {
  ElMessageBox.confirm('确定要清空所有对话记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    messages.value = []
    localStorage.removeItem('atgbot_chat_history')
    ElMessage.success('对话已清空')
  }).catch(() => {})
}

const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}
</script>

<style scoped>
.atgbot-container {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 20px;
  height: calc(100vh - 140px);
  padding: 20px;
  overflow: hidden;
}

/* 科技感背景 */
.tech-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #0f0c29 0%, #302b63 50%, #24243e 100%);
  z-index: 0;
}

.grid-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    linear-gradient(rgba(64, 158, 255, 0.15) 1px, transparent 1px),
    linear-gradient(90deg, rgba(64, 158, 255, 0.15) 1px, transparent 1px);
  background-size: 50px 50px;
  animation: gridMove 20s linear infinite;
  opacity: 0.6;
}

.grid-overlay::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 50% 50%, rgba(64, 158, 255, 0.1) 0%, transparent 70%);
}

@keyframes gridMove {
  0% {
    background-position: 0 0;
  }
  100% {
    background-position: 50px 50px;
  }
}

.gradient-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.4;
  animation: float 8s ease-in-out infinite;
}

.orb-1 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  top: -100px;
  left: -100px;
}

.orb-2 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  bottom: -150px;
  right: -150px;
  animation-delay: 2s;
}

.orb-3 {
  width: 250px;
  height: 250px;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  top: 50%;
  left: 50%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  33% {
    transform: translate(30px, -30px) scale(1.1);
  }
  66% {
    transform: translate(-20px, 20px) scale(0.9);
  }
}

/* Skills 面板 */
.quick-actions-section {
  position: relative;
  z-index: 1;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  transition: all 0.3s ease-in-out;
  overflow: hidden;
}

.quick-actions-section.collapsed {
  padding: 20px 20px 10px 20px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 20px;
  color: #fff;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  font-size: 24px;
  color: #409eff;
}

.header-title {
  font-size: 18px;
  font-weight: 700;
  flex: 1;
  background: linear-gradient(135deg, #fff 0%, #409eff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 0 0 20px rgba(64, 158, 255, 0.3);
}

.collapse-btn {
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.05);
  color: #fff;
  transition: all 0.3s;
}

.collapse-btn:hover {
  color: #409eff;
  border-color: #409eff;
  background: rgba(64, 158, 255, 0.1);
}

.quick-actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.suite-card {
  position: relative;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  overflow: hidden;
}

.suite-card:hover {
  transform: translateY(-4px);
  border-color: #409eff;
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.3);
}

.suite-card:hover .card-glow {
  opacity: 1;
}

.card-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.1) 0%, rgba(103, 58, 183, 0.1) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.card-content {
  position: relative;
  z-index: 1;
}

.card-icon {
  font-size: 32px;
  color: #409eff;
  margin-bottom: 12px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-desc {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
  margin-bottom: 12px;
  min-height: 36px;
}

.card-stats {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.empty-suites {
  grid-column: 1 / -1;
  text-align: center;
  padding: 40px;
  color: rgba(255, 255, 255, 0.5);
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

/* 聊天区域 */
.chat-section {
  position: relative;
  z-index: 1;
  flex: 1;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-messages {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: rgba(64, 158, 255, 0.4);
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: rgba(64, 158, 255, 0.6);
}

.message-item {
  display: flex;
  gap: 12px;
  animation: messageSlideIn 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes messageSlideIn {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.user-message {
  justify-content: flex-end;
}

.user-message .message-avatar {
  order: 2;
}

.user-message .message-content {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.bot-message .message-content {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.15);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
  color: white;
  border: 2px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.message-content {
  max-width: 60%;
  padding: 14px 18px;
  border-radius: 12px;
  word-wrap: break-word;
}

.message-text {
  line-height: 1.6;
  margin-bottom: 8px;
}

.suite-info {
  margin-top: 12px;
}

.suite-info :deep(.el-card) {
  background: rgba(64, 158, 255, 0.1);
  border: 1px solid rgba(64, 158, 255, 0.3);
  backdrop-filter: blur(10px);
}

.suite-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-weight: 500;
  color: #fff;
}

.suite-actions {
  display: flex;
  gap: 8px;
}

.execution-result {
  margin-top: 12px;
}

/* 候选技能样式 */
.candidate-skills {
  margin-top: 12px;
}

.candidate-skills-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-weight: 500;
  color: #fff;
  font-size: 14px;
}

.candidate-skills-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 12px;
}

.candidate-skill-card {
  cursor: pointer;
  transition: all 0.3s ease;
  background: rgba(64, 158, 255, 0.1) !important;
  border: 1px solid rgba(64, 158, 255, 0.3) !important;
  backdrop-filter: blur(10px);
}

.candidate-skill-card:hover {
  transform: translateY(-2px);
  border-color: #409eff !important;
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.4) !important;
}

.skill-card-content {
  padding: 4px;
}

.skill-card-title {
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.skill-card-desc {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 8px;
  min-height: 32px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.skill-card-type {
  display: flex;
  justify-content: flex-end;
}

.message-time {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  margin-top: 4px;
}

.typing-indicator {
  display: flex;
  gap: 6px;
  padding: 14px 20px;
  background: rgba(64, 158, 255, 0.15);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(64, 158, 255, 0.3);
  border-radius: 12px;
  width: fit-content;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.typing-indicator span {
  width: 10px;
  height: 10px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  animation: typing 1.4s infinite;
  box-shadow: 0 0 10px rgba(102, 126, 234, 0.5);
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
  }
  30% {
    transform: translateY(-10px);
  }
}

/* Quick Actions 折叠动画 */
.slide-fade-enter-active {
  transition: all 0.3s ease-out;
}

.slide-fade-leave-active {
  transition: all 0.2s ease-in;
}

.slide-fade-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.slide-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.chat-input-section {
  position: relative;
  z-index: 1;
  padding: 20px;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 12px;
}

:deep(.el-textarea__inner) {
  resize: none;
  background: rgba(255, 255, 255, 0.1) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  color: #fff !important;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

:deep(.el-textarea__inner):focus {
  border-color: #409eff !important;
  box-shadow: 0 0 12px rgba(64, 158, 255, 0.4) !important;
}

:deep(.el-textarea__inner)::placeholder {
  color: rgba(255, 255, 255, 0.5);
}
</style>
