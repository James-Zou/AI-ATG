<template>
  <div class="testcase-detail">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>测试用例详情</span>
          <div>
            <el-button @click="handleEdit">编辑</el-button>
            <el-button @click="handleBack">返回</el-button>
          </div>
        </div>
      </template>
      
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用例编号">
          {{ detail.caseNo }}
        </el-descriptions-item>
        
        <el-descriptions-item label="项目名称">
          {{ detail.projectName || '-' }}
        </el-descriptions-item>
        
        <el-descriptions-item label="用例标题" :span="2">
          {{ detail.title }}
        </el-descriptions-item>
        
        <el-descriptions-item label="用例类型">
          <el-tag>{{ getTypeLabel(detail.type) }}</el-tag>
        </el-descriptions-item>
        
        <el-descriptions-item label="优先级">
          <el-tag :type="getPriorityType(detail.priority)">
            {{ detail.priority }}
          </el-tag>
        </el-descriptions-item>
        
        <el-descriptions-item label="用例状态">
          <el-tag :type="getStatusType(detail.status)">
            {{ getStatusLabel(detail.status) }}
          </el-tag>
        </el-descriptions-item>
        
        <el-descriptions-item label="所属套件">
          {{ detail.suiteName || '未分配' }}
        </el-descriptions-item>
        
        <el-descriptions-item label="关联需求">
          {{ detail.requirementTitle || '无' }}
        </el-descriptions-item>
        
        <el-descriptions-item label="创建人">
          {{ detail.createdByName || '-' }}
        </el-descriptions-item>
        
        <el-descriptions-item label="创建时间">
          {{ detail.createdTime }}
        </el-descriptions-item>
        
        <el-descriptions-item label="更新时间">
          {{ detail.updatedTime || '-' }}
        </el-descriptions-item>
        
        <el-descriptions-item label="用例描述" :span="2">
          <div class="content-box">
            {{ detail.description || '暂无描述' }}
          </div>
        </el-descriptions-item>
        
        <el-descriptions-item label="前置条件" :span="2">
          <div class="content-box">
            {{ detail.precondition || '无' }}
          </div>
        </el-descriptions-item>
      </el-descriptions>
      
      <!-- 测试步骤 -->
      <el-divider content-position="left">
        {{ getStepTitle(detail.type) }}
      </el-divider>
      
      <!-- UI自动化测试步骤 -->
      <div v-if="detail.type === 'ui' && parsedSteps && parsedSteps.length > 0">
        <div v-for="(step, index) in parsedSteps" :key="index" class="step-card">
          <el-card shadow="hover">
            <template #header>
              <div class="step-header">
                <el-tag>步骤 {{ index + 1 }}</el-tag>
                <el-tag type="info" style="margin-left: 10px;">{{ getActionLabel(step.action) }}</el-tag>
              </div>
            </template>
            <div class="step-content">
              <div v-if="step.action === 'open'">
                <strong>打开URL：</strong>{{ step.input }}
              </div>
              <div v-else-if="step.action === 'click'">
                <strong>点击元素：</strong>{{ step.locator }}="{{ step.value }}"
              </div>
              <div v-else-if="step.action === 'input'">
                <strong>输入文本：</strong>在 {{ step.locator }}="{{ step.value }}" 输入 "{{ step.input }}"
              </div>
              <div v-else-if="step.action === 'wait'">
                <strong>等待：</strong>{{ step.timeout || 1 }} 秒
              </div>
              <div v-else-if="step.action.startsWith('assert')">
                <strong>断言：</strong>{{ getAssertDescription(step) }}
              </div>
              <div v-else>
                <pre style="margin: 0;">{{ JSON.stringify(step, null, 2) }}</pre>
              </div>
            </div>
          </el-card>
        </div>
        
        <!-- JSON预览 -->
        <el-divider />
        <el-card>
          <template #header>
            <span>📄 JSON 预览</span>
          </template>
          <pre class="json-preview">{{ JSON.stringify(parsedSteps, null, 2) }}</pre>
        </el-card>
      </div>
      
      <!-- 传统测试步骤 -->
      <div v-else-if="detail.steps && detail.steps.length > 0">
        <el-table :data="detail.steps" stripe border>
          <el-table-column prop="stepOrder" label="步骤" width="80" />
          <el-table-column prop="stepDescription" label="步骤描述" min-width="200" />
          <el-table-column prop="expectedResult" label="预期结果" min-width="200" />
        </el-table>
      </div>
      
      <!-- 无步骤 -->
      <div v-else class="no-steps">
        <el-empty description="暂无测试步骤" />
      </div>
      
      <!-- 状态操作按钮 -->
      <div class="action-buttons">
        <el-button
          v-if="detail.status === 'draft'"
          type="primary"
          @click="handleUpdateStatus('pending')"
        >
          提交审核
        </el-button>
        <el-button
          v-if="detail.status === 'pending'"
          type="success"
          @click="handleUpdateStatus('approved')"
        >
          审核通过
        </el-button>
        <el-button
          v-if="detail.status === 'approved'"
          type="warning"
          @click="handleUpdateStatus('running')"
        >
          开始执行
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getTestCaseDetail, updateTestCaseStatus } from '@/api/testcase'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const detail = ref({})

// 解析JSON格式的测试步骤
const parsedSteps = computed(() => {
  if (!detail.value.stepsJson) return []
  
  try {
    return typeof detail.value.stepsJson === 'string' 
      ? JSON.parse(detail.value.stepsJson) 
      : detail.value.stepsJson
  } catch (e) {
    console.error('解析测试步骤失败', e)
    return []
  }
})

const loadData = async () => {
  try {
    loading.value = true
    const res = await getTestCaseDetail(route.params.id)
    detail.value = res.data
  } catch (error) {
    ElMessage.error('加载失败')
    handleBack()
  } finally {
    loading.value = false
  }
}

const handleEdit = () => {
  router.push(`/testcase/edit/${route.params.id}`)
}

const handleBack = () => {
  router.push('/testcase/list')
}

const handleUpdateStatus = async (status) => {
  try {
    await updateTestCaseStatus(route.params.id, status)
    ElMessage.success('状态更新成功')
    loadData()
  } catch (error) {
    ElMessage.error('状态更新失败')
  }
}

const getTypeLabel = (type) => {
  const labels = {
    functional: '功能测试',
    ui: '界面测试',
    api: '接口测试',
    performance: '性能测试'
  }
  return labels[type] || type
}

const getPriorityType = (priority) => {
  const types = {
    P0: 'danger',
    P1: 'warning',
    P2: 'info',
    P3: 'success'
  }
  return types[priority] || 'info'
}

const getStatusLabel = (status) => {
  const labels = {
    draft: '草稿',
    pending: '待审核',
    approved: '已通过',
    running: '执行中'
  }
  return labels[status] || status
}

const getStatusType = (status) => {
  const types = {
    draft: 'info',
    pending: 'warning',
    approved: 'success',
    running: 'primary'
  }
  return types[status] || 'info'
}

const getStepTitle = (type) => {
  const titles = {
    'ui': '🎬 UI自动化测试步骤',
    'api': '🔌 接口自动化配置',
    'performance': '⚡ 性能测试配置'
  }
  return titles[type] || '测试步骤'
}

const getActionLabel = (action) => {
  const labels = {
    'open': '打开URL',
    'click': '点击',
    'input': '输入',
    'select': '选择',
    'wait': '等待',
    'assertUrl': '验证URL',
    'assertTitle': '验证标题',
    'assertText': '验证文本',
    'assertVisible': '验证可见',
    'hover': '悬停'
  }
  return labels[action] || action
}

const getAssertDescription = (step) => {
  if (step.action === 'assertUrl') return `URL应为 "${step.input}"`
  if (step.action === 'assertTitle') return `标题应为 "${step.input}"`
  if (step.action === 'assertText') return `元素 ${step.locator}="${step.value}" 文本应为 "${step.input}"`
  if (step.action === 'assertVisible') return `元素 ${step.locator}="${step.value}" 应可见`
  return JSON.stringify(step)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.testcase-detail {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.content-box {
  white-space: pre-wrap;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  min-height: 60px;
}

.no-steps {
  padding: 20px 0;
}

.action-buttons {
  margin-top: 20px;
  text-align: center;
}

.step-card {
  margin-bottom: 15px;
}

.step-header {
  display: flex;
  align-items: center;
}

.step-content {
  font-size: 14px;
  line-height: 1.8;
}

.json-preview {
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 15px;
  margin: 0;
  overflow-x: auto;
  font-family: 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.6;
}
</style>
