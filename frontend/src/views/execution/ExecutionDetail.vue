<template>
  <div class="execution-detail">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>执行详情</span>
          <div>
            <el-button
              v-if="detail.status === 1"
              type="danger"
              @click="handleStop"
            >
              停止执行
            </el-button>
            <el-button @click="handleBack">返回</el-button>
          </div>
        </div>
      </template>
      
      <el-descriptions :column="3" border>
        <el-descriptions-item label="执行ID">
          {{ detail.id }}
        </el-descriptions-item>
        
        <el-descriptions-item label="执行名称">
          {{ detail.executionName }}
        </el-descriptions-item>
        
        <el-descriptions-item label="执行类型">
          <el-tag>{{ getTypeLabel(detail.executionType) }}</el-tag>
        </el-descriptions-item>
        
        <el-descriptions-item label="测试环境">
          {{ detail.environment }}
        </el-descriptions-item>
        
        <el-descriptions-item label="触发方式">
          {{ getTriggerLabel(detail.triggerType) }}
        </el-descriptions-item>
        
        <el-descriptions-item label="执行状态">
          <el-tag :type="getStatusType(detail.status)">
            {{ getStatusLabel(detail.status) }}
          </el-tag>
        </el-descriptions-item>
        
        <el-descriptions-item label="总用例数">
          {{ detail.totalCases || 0 }}
        </el-descriptions-item>
        
        <el-descriptions-item label="通过数">
          <span style="color: #67C23A; font-weight: bold">
            {{ detail.passedCases || 0 }}
          </span>
        </el-descriptions-item>
        
        <el-descriptions-item label="失败数">
          <span style="color: #F56C6C; font-weight: bold">
            {{ detail.failedCases || 0 }}
          </span>
        </el-descriptions-item>
        
        <el-descriptions-item label="开始时间">
          {{ detail.startTime }}
        </el-descriptions-item>
        
        <el-descriptions-item label="结束时间">
          {{ detail.endTime || '-' }}
        </el-descriptions-item>
        
        <el-descriptions-item label="执行耗时">
          {{ formatDuration(detail.duration) }}
        </el-descriptions-item>
      </el-descriptions>
      
      <!-- 执行明细 -->
      <el-divider content-position="left">执行明细</el-divider>
      
      <el-table :data="detail.details" stripe border v-if="detail.details">
        <el-table-column prop="testCaseTitle" label="用例标题" min-width="200" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getDetailStatusType(scope.row.status)">
              {{ getDetailStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="耗时(ms)" width="100" />
        <el-table-column prop="errorMessage" label="错误信息" min-width="200">
          <template #default="scope">
            <span v-if="scope.row.errorMessage" style="color: #F56C6C">
              {{ scope.row.errorMessage }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button size="small" @click="viewDetailLogs(scope.row)">
              查看日志
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-empty v-else description="暂无执行明细" />
    </el-card>
    
    <!-- 日志对话框 -->
    <el-dialog
      v-model="logDialogVisible"
      title="执行日志"
      width="800px"
    >
      <div v-if="currentDetail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用例标题">
            {{ currentDetail.testCaseTitle }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getDetailStatusType(currentDetail.status)">
              {{ getDetailStatusLabel(currentDetail.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="执行耗时">
            {{ formatDuration(currentDetail.duration) }}
          </el-descriptions-item>
        </el-descriptions>
        
        <el-divider content-position="left">执行日志</el-divider>
        <div class="log-box">
          {{ currentDetail.logs || '暂无日志' }}
        </div>
        
        <el-divider v-if="currentDetail.errorMessage" content-position="left">错误信息</el-divider>
        <el-alert
          v-if="currentDetail.errorMessage"
          type="error"
          :closable="false"
        >
          {{ currentDetail.errorMessage }}
        </el-alert>
        
        <el-divider v-if="currentDetail.stackTrace" content-position="left">堆栈跟踪</el-divider>
        <div v-if="currentDetail.stackTrace" class="log-box">
          {{ currentDetail.stackTrace }}
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getExecutionDetail, stopExecution } from '@/api/execution'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const detail = ref({})
const logDialogVisible = ref(false)
const currentDetail = ref(null)
let pollTimer = null // 轮询定时器

const loadData = async (silent = false) => {
  try {
    if (!silent) {
      loading.value = true
    }
    const res = await getExecutionDetail(route.params.id)
    detail.value = res.data
    
    // 如果执行完成（已完成、已停止、失败），停止轮询
    if (res.data.status !== 1) {
      stopPolling()
    }
  } catch (error) {
    if (!silent) {
      ElMessage.error('加载失败')
      handleBack()
    }
  } finally {
    loading.value = false
  }
}

// 开始轮询
const startPolling = () => {
  // 先清除之前的定时器（如果存在）
  stopPolling()
  
  // 每3秒轮询一次
  pollTimer = setInterval(() => {
    loadData(true) // silent=true，不显示loading状态，避免页面闪烁
  }, 3000)
}

// 停止轮询
const stopPolling = () => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

const handleStop = async () => {
  try {
    await ElMessageBox.confirm('确定要停止执行吗？', '停止确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await stopExecution(route.params.id)
    ElMessage.success('执行已停止')
    loadData(true) // 立即刷新一次
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('停止失败')
    }
  }
}

const handleBack = () => {
  stopPolling() // 返回时停止轮询
  router.push('/execution/list')
}

const viewDetailLogs = (row) => {
  currentDetail.value = row
  logDialogVisible.value = true
}

const formatDuration = (duration) => {
  if (!duration) return '-'
  if (duration < 1000) return `${duration}ms`
  return `${(duration / 1000).toFixed(2)}s`
}

const getTypeLabel = (type) => {
  const labels = {
    api: 'API测试',
    ui: 'UI测试',
    performance: '性能测试'
  }
  return labels[type] || type
}

const getTriggerLabel = (trigger) => {
  const labels = {
    manual: '手动触发',
    schedule: '定时触发',
    ci: 'CI触发'
  }
  return labels[trigger] || trigger
}

const getStatusLabel = (status) => {
  const labels = {
    1: '执行中',
    2: '已完成',
    3: '已停止',
    4: '失败'
  }
  return labels[status] || '未知'
}

const getStatusType = (status) => {
  const types = {
    1: 'warning',
    2: 'success',
    3: 'info',
    4: 'danger'
  }
  return types[status] || 'info'
}

const getDetailStatusLabel = (status) => {
  const labels = {
    1: '通过',
    2: '失败',
    3: '跳过'
  }
  return labels[status] || '未知'
}

const getDetailStatusType = (status) => {
  const types = {
    1: 'success',
    2: 'danger',
    3: 'info'
  }
  return types[status] || 'info'
}

onMounted(async () => {
  await loadData()
  
  // 如果执行状态为"执行中"，开始轮询
  if (detail.value.status === 1) {
    startPolling()
  }
})

// 组件卸载时清理定时器
onBeforeUnmount(() => {
  stopPolling()
})
</script>

<style scoped>
.execution-detail {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.log-box {
  white-space: pre-wrap;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  max-height: 400px;
  overflow-y: auto;
  font-size: 13px;
  font-family: 'Courier New', monospace;
  margin: 10px 0;
}
</style>
