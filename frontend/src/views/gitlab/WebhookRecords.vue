<template>
  <div class="webhook-records">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>Webhook 记录</span>
          <el-button @click="handleBack">返回配置</el-button>
        </div>
      </template>
      
      <!-- 记录列表 -->
      <el-table :data="recordList" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="eventType" label="事件类型" width="120" />
        <el-table-column prop="ref" label="分支" width="150" />
        <el-table-column prop="commitId" label="提交ID" width="120">
          <template #default="scope">
            <span v-if="scope.row.commitId">
              {{ scope.row.commitId.substring(0, 8) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="commitMessage" label="提交信息" min-width="200" show-overflow-tooltip />
        <el-table-column prop="commitAuthor" label="提交人" width="120" />
        <el-table-column prop="statusLabel" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ scope.row.statusLabel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="generatedCases" label="生成用例数" width="120" />
        <el-table-column prop="processingTime" label="处理时长" width="120">
          <template #default="scope">
            <span v-if="scope.row.processingTime">
              {{ formatDuration(scope.row.processingTime) }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="receivedTime" label="接收时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="viewDetail(scope.row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadData"
        class="pagination"
      />
    </el-card>
    
    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="Webhook 详情"
      width="800px"
    >
      <div v-if="currentRecord">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="ID">
            {{ currentRecord.id }}
          </el-descriptions-item>
          
          <el-descriptions-item label="事件类型">
            {{ currentRecord.eventType }}
          </el-descriptions-item>
          
          <el-descriptions-item label="对象类型">
            {{ currentRecord.objectKind }}
          </el-descriptions-item>
          
          <el-descriptions-item label="分支">
            {{ currentRecord.ref }}
          </el-descriptions-item>
          
          <el-descriptions-item label="提交ID">
            {{ currentRecord.commitId }}
          </el-descriptions-item>
          
          <el-descriptions-item label="提交人">
            {{ currentRecord.commitAuthor }}
          </el-descriptions-item>
          
          <el-descriptions-item label="状态" :span="2">
            <el-tag :type="getStatusType(currentRecord.status)">
              {{ currentRecord.statusLabel }}
            </el-tag>
          </el-descriptions-item>
          
          <el-descriptions-item label="接收时间">
            {{ currentRecord.receivedTime }}
          </el-descriptions-item>
          
          <el-descriptions-item label="处理时间">
            {{ currentRecord.processedTime || '-' }}
          </el-descriptions-item>
          
          <el-descriptions-item label="处理时长">
            {{ formatDuration(currentRecord.processingTime) }}
          </el-descriptions-item>
          
          <el-descriptions-item label="生成用例数">
            {{ currentRecord.generatedCases || 0 }}
          </el-descriptions-item>
        </el-descriptions>
        
        <el-divider content-position="left">提交信息</el-divider>
        <div class="commit-message">
          {{ currentRecord.commitMessage || '无' }}
        </div>
        
        <el-divider v-if="currentRecord.errorMessage" content-position="left">
          错误信息
        </el-divider>
        <el-alert
          v-if="currentRecord.errorMessage"
          type="error"
          :closable="false"
        >
          {{ currentRecord.errorMessage }}
        </el-alert>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getWebhookRecords, getWebhookRecord } from '@/api/gitlab'
import { ElMessage } from 'element-plus'

const router = useRouter()

const loading = ref(false)
const recordList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const detailVisible = ref(false)
const currentRecord = ref(null)

const loadData = async () => {
  try {
    loading.value = true
    const res = await getWebhookRecords({
      projectId: 1,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    recordList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const viewDetail = async (row) => {
  try {
    const res = await getWebhookRecord(row.id)
    currentRecord.value = res.data
    detailVisible.value = true
  } catch (error) {
    ElMessage.error('加载详情失败')
  }
}

const handleBack = () => {
  router.push('/gitlab/config')
}

const formatDuration = (duration) => {
  if (!duration) return '-'
  if (duration < 1000) return `${duration}ms`
  return `${(duration / 1000).toFixed(2)}s`
}

const getStatusType = (status) => {
  const types = {
    0: 'warning',
    1: 'success',
    2: 'danger',
    3: 'info'
  }
  return types[status] || 'info'
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.webhook-records {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}

.commit-message {
  white-space: pre-wrap;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  font-size: 13px;
  font-family: 'Courier New', monospace;
}
</style>
