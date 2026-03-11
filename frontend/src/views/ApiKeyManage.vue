<template>
  <div class="api-key-manage">
    <el-card class="header-card">
      <template #header>
        <div class="card-header">
          <span class="title">API Key 管理</span>
          <el-button type="primary" @click="showGenerateDialog">生成新的 API Key</el-button>
        </div>
      </template>
      
      <el-alert
        title="API Key 使用说明"
        type="info"
        :closable="false"
        show-icon
        class="info-alert"
      >
        <p>• API Key 用于第三方应用调用本系统的 API 接口</p>
        <p>• 请妥善保管您的 Secret Key，一旦丢失无法找回</p>
        <p>• 每个 API Key 仅在生成时显示一次完整的 Secret Key</p>
        <p>• 建议为不同的应用创建独立的 API Key，便于管理和追踪</p>
      </el-alert>
    </el-card>

    <el-card class="table-card">
      <el-table :data="apiKeyList" style="width: 100%" v-loading="loading">
        <el-table-column prop="appName" label="应用名称" width="180" />
        <el-table-column prop="apiKey" label="API Key" width="280">
          <template #default="{ row }">
            <el-text class="mono-text">{{ row.apiKey }}</el-text>
            <el-button
              link
              type="primary"
              size="small"
              @click="copyToClipboard(row.apiKey)"
              style="margin-left: 8px"
            >
              复制
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="secretKey" label="Secret Key" width="200">
          <template #default="{ row }">
            <el-text class="mono-text">{{ row.secretKey }}</el-text>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.statusText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="过期状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.expired" type="danger">已过期</el-tag>
            <el-tag v-else-if="row.expireTime" type="warning">
              {{ formatExpireTime(row.expireTime) }}
            </el-tag>
            <el-tag v-else type="success">永不过期</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastUsedTime" label="最后使用时间" width="160">
          <template #default="{ row }">
            {{ row.lastUsedTime ? formatDateTime(row.lastUsedTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 1"
              link
              type="warning"
              size="small"
              @click="handleRevoke(row)"
            >
              禁用
            </el-button>
            <el-button
              link
              type="danger"
              size="small"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
            <el-button
              link
              type="primary"
              size="small"
              @click="showDetailDialog(row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 生成 API Key 对话框 -->
    <el-dialog
      v-model="generateDialogVisible"
      title="生成新的 API Key"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="generateFormRef"
        :model="generateForm"
        :rules="generateRules"
        label-width="100px"
      >
        <el-form-item label="应用名称" prop="appName">
          <el-input
            v-model="generateForm.appName"
            placeholder="请输入应用名称"
            clearable
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="generateForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入 API Key 用途描述（可选）"
          />
        </el-form-item>
        <el-form-item label="过期时间" prop="expireDays">
          <el-input-number
            v-model="generateForm.expireDays"
            :min="1"
            :max="3650"
            placeholder="天数"
            style="width: 200px"
          />
          <span style="margin-left: 10px; color: #909399">天后过期（不填则永不过期）</span>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="generateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleGenerate" :loading="generating">生成</el-button>
      </template>
    </el-dialog>

    <!-- API Key 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="API Key 详情"
      width="700px"
    >
      <el-descriptions :column="1" border v-if="currentApiKey">
        <el-descriptions-item label="应用名称">{{ currentApiKey.appName }}</el-descriptions-item>
        <el-descriptions-item label="API Key">
          <el-text class="mono-text">{{ currentApiKey.apiKey }}</el-text>
          <el-button
            link
            type="primary"
            size="small"
            @click="copyToClipboard(currentApiKey.apiKey)"
            style="margin-left: 8px"
          >
            复制
          </el-button>
        </el-descriptions-item>
        <el-descriptions-item label="Secret Key">
          <el-text class="mono-text">{{ currentApiKey.secretKey }}</el-text>
          <el-button
            v-if="!currentApiKey.secretKey.includes('****')"
            link
            type="primary"
            size="small"
            @click="copyToClipboard(currentApiKey.secretKey)"
            style="margin-left: 8px"
          >
            复制
          </el-button>
        </el-descriptions-item>
        <el-descriptions-item label="描述">{{ currentApiKey.description || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentApiKey.status === 1 ? 'success' : 'danger'">
            {{ currentApiKey.statusText }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="过期时间">
          {{ currentApiKey.expireTime ? formatDateTime(currentApiKey.expireTime) : '永不过期' }}
        </el-descriptions-item>
        <el-descriptions-item label="最后使用">
          {{ currentApiKey.lastUsedTime ? formatDateTime(currentApiKey.lastUsedTime) : '未使用' }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatDateTime(currentApiKey.createTime) }}
        </el-descriptions-item>
      </el-descriptions>
      
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 新生成的 API Key 展示对话框 -->
    <el-dialog
      v-model="newApiKeyDialogVisible"
      title="API Key 生成成功"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-alert
        title="请妥善保管以下信息"
        type="warning"
        :closable="false"
        show-icon
        class="warning-alert"
      >
        <p><strong>Secret Key 仅显示一次，请立即复制保存！</strong></p>
        <p>如果丢失，您将无法找回，只能重新生成新的 API Key。</p>
      </el-alert>
      
      <el-descriptions :column="1" border v-if="newApiKey" style="margin-top: 20px">
        <el-descriptions-item label="应用名称">{{ newApiKey.appName }}</el-descriptions-item>
        <el-descriptions-item label="API Key">
          <el-text class="mono-text">{{ newApiKey.apiKey }}</el-text>
          <el-button
            link
            type="primary"
            size="small"
            @click="copyToClipboard(newApiKey.apiKey)"
            style="margin-left: 8px"
          >
            复制
          </el-button>
        </el-descriptions-item>
        <el-descriptions-item label="Secret Key">
          <el-text class="mono-text" type="danger">{{ newApiKey.secretKey }}</el-text>
          <el-button
            link
            type="primary"
            size="small"
            @click="copyToClipboard(newApiKey.secretKey)"
            style="margin-left: 8px"
          >
            复制
          </el-button>
        </el-descriptions-item>
      </el-descriptions>
      
      <template #footer>
        <el-button type="primary" @click="handleCloseNewKeyDialog">我已保存，关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { generateApiKey, getApiKeyList, revokeApiKey, deleteApiKey } from '@/api/apiKey'

const loading = ref(false)
const generating = ref(false)
const apiKeyList = ref([])

const generateDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const newApiKeyDialogVisible = ref(false)

const generateFormRef = ref()
const generateForm = reactive({
  appName: '',
  description: '',
  expireDays: null
})

const generateRules = {
  appName: [
    { required: true, message: '请输入应用名称', trigger: 'blur' },
    { min: 2, max: 100, message: '应用名称长度在 2 到 100 个字符', trigger: 'blur' }
  ]
}

const currentApiKey = ref(null)
const newApiKey = ref(null)

// 加载 API Key 列表
const loadApiKeyList = async () => {
  try {
    loading.value = true
    const res = await getApiKeyList()
    apiKeyList.value = res.data || []
  } catch (error) {
    ElMessage.error(error.message || '获取 API Key 列表失败')
  } finally {
    loading.value = false
  }
}

// 显示生成对话框
const showGenerateDialog = () => {
  generateForm.appName = ''
  generateForm.description = ''
  generateForm.expireDays = null
  generateDialogVisible.value = true
}

// 生成 API Key
const handleGenerate = async () => {
  try {
    await generateFormRef.value.validate()
    generating.value = true
    
    const res = await generateApiKey(generateForm)
    newApiKey.value = res.data
    
    generateDialogVisible.value = false
    newApiKeyDialogVisible.value = true
    
    ElMessage.success('API Key 生成成功')
    
    // 刷新列表
    await loadApiKeyList()
  } catch (error) {
    if (error.message) {
      ElMessage.error(error.message)
    }
  } finally {
    generating.value = false
  }
}

// 关闭新生成的 Key 对话框
const handleCloseNewKeyDialog = () => {
  newApiKeyDialogVisible.value = false
  newApiKey.value = null
}

// 禁用 API Key
const handleRevoke = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要禁用 "${row.appName}" 的 API Key 吗？禁用后将无法使用该 Key 调用 API。`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await revokeApiKey(row.apiKey)
    ElMessage.success('API Key 已禁用')
    await loadApiKeyList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '禁用失败')
    }
  }
}

// 删除 API Key
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除 "${row.appName}" 的 API Key 吗？删除后无法恢复。`,
      '警告',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    
    await deleteApiKey(row.id)
    ElMessage.success('API Key 已删除')
    await loadApiKeyList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 显示详情对话框
const showDetailDialog = (row) => {
  currentApiKey.value = row
  detailDialogVisible.value = true
}

// 复制到剪贴板
const copyToClipboard = async (text) => {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败，请手动复制')
  }
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 格式化过期时间提示
const formatExpireTime = (expireTime) => {
  const now = new Date()
  const expire = new Date(expireTime)
  const days = Math.ceil((expire - now) / (1000 * 60 * 60 * 24))
  
  if (days <= 0) return '已过期'
  if (days <= 7) return `${days}天后过期`
  if (days <= 30) return `${Math.ceil(days / 7)}周后过期`
  return `${Math.ceil(days / 30)}月后过期`
}

onMounted(() => {
  loadApiKeyList()
})
</script>

<style scoped>
.api-key-manage {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 18px;
  font-weight: 600;
}

.info-alert {
  margin-bottom: 0;
}

.info-alert p {
  margin: 4px 0;
  line-height: 1.6;
}

.table-card {
  margin-bottom: 20px;
}

.mono-text {
  font-family: 'Courier New', Consolas, monospace;
  font-size: 13px;
}

.warning-alert {
  margin-bottom: 0;
}

.warning-alert p {
  margin: 4px 0;
  line-height: 1.6;
}
</style>
