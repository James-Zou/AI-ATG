<template>
  <div class="ai-generate">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>AI 测试用例生成</span>
          <el-button type="primary" @click="showGenerateDialog">
            <el-icon><MagicStick /></el-icon>
            生成测试用例
          </el-button>
        </div>
      </template>
      
      <!-- 生成历史列表 -->
      <el-table :data="historyList" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="requirementId" label="需求ID" width="100" />
        <el-table-column prop="provider" label="AI提供商" width="120">
          <template #default="scope">
            <el-tag>{{ getProviderLabel(scope.row.provider) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="modelName" label="模型" width="150" />
        <el-table-column prop="generatedCount" label="生成数量" width="100" />
        <el-table-column prop="duration" label="耗时(ms)" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="viewHistory(scope.row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadHistory"
        class="pagination"
      />
    </el-card>
    
    <!-- AI生成对话框 -->
    <el-dialog
      v-model="generateDialogVisible"
      title="AI 生成测试用例"
      width="700px"
    >
      <el-form
        ref="formRef"
        :model="generateForm"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="需求" prop="requirementId">
          <el-input
            v-model.number="generateForm.requirementId"
            type="number"
            placeholder="请输入需求ID"
          />
        </el-form-item>
        
        <el-form-item label="AI提供商">
          <el-select
            v-model="generateForm.provider"
            placeholder="选择AI提供商（默认使用系统配置）"
            clearable
          >
            <el-option label="DeepSeek" value="deepseek" />
            <el-option label="阿里千问" value="qwen" />
            <el-option label="智谱AI" value="zhipu" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="生成数量">
          <el-input-number
            v-model="generateForm.count"
            :min="1"
            :max="20"
          />
        </el-form-item>
        
        <el-form-item label="温度参数">
          <el-slider
            v-model="generateForm.temperature"
            :min="0"
            :max="1"
            :step="0.1"
            show-input
          />
          <div class="help-text">温度越高，生成的内容越随机</div>
        </el-form-item>
        
        <el-form-item label="自定义提示词">
          <el-input
            v-model="generateForm.customPrompt"
            type="textarea"
            :rows="6"
            placeholder="留空则使用默认模板"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="generateDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="handleGenerate"
          :loading="generating"
        >
          {{ generating ? '生成中...' : '开始生成' }}
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 历史详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="生成历史详情"
      width="800px"
    >
      <el-descriptions :column="2" border v-if="currentHistory">
        <el-descriptions-item label="需求ID">
          {{ currentHistory.requirementId }}
        </el-descriptions-item>
        <el-descriptions-item label="AI提供商">
          {{ getProviderLabel(currentHistory.provider) }}
        </el-descriptions-item>
        <el-descriptions-item label="模型">
          {{ currentHistory.modelName }}
        </el-descriptions-item>
        <el-descriptions-item label="生成数量">
          {{ currentHistory.generatedCount }}
        </el-descriptions-item>
        <el-descriptions-item label="耗时">
          {{ currentHistory.duration }} ms
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentHistory.status)">
            {{ getStatusLabel(currentHistory.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="提示词" :span="2">
          <div class="prompt-box">{{ currentHistory.prompt }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="AI响应" :span="2" v-if="currentHistory.response">
          <div class="response-box">{{ currentHistory.response }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="错误信息" :span="2" v-if="currentHistory.errorMessage">
          <el-alert type="error" :closable="false">
            {{ currentHistory.errorMessage }}
          </el-alert>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { generateTestCases, getGenerateHistory, getHistoryDetail } from '@/api/ai'
import { ElMessage } from 'element-plus'
import { MagicStick } from '@element-plus/icons-vue'

const router = useRouter()

const loading = ref(false)
const generating = ref(false)
const historyList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const generateDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentHistory = ref(null)

const formRef = ref()
const generateForm = reactive({
  requirementId: null,
  provider: '',
  count: 5,
  temperature: 0.7,
  customPrompt: ''
})

const formRules = {
  requirementId: [
    { required: true, message: '请输入需求ID', trigger: 'blur' }
  ]
}

const loadHistory = async () => {
  try {
    loading.value = true
    const res = await getGenerateHistory({
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    historyList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    ElMessage.error('加载历史失败')
  } finally {
    loading.value = false
  }
}

const showGenerateDialog = () => {
  generateDialogVisible.value = true
}

const handleGenerate = async () => {
  try {
    await formRef.value.validate()
    generating.value = true
    
    const res = await generateTestCases(generateForm)
    
    ElMessage.success(`成功生成 ${res.data.generatedCount} 个测试用例！`)
    generateDialogVisible.value = false
    
    // 跳转到测试用例列表
    router.push('/testcase/list')
    
  } catch (error) {
    if (error) {
      ElMessage.error(error.message || '生成失败')
    }
  } finally {
    generating.value = false
  }
}

const viewHistory = async (row) => {
  try {
    const res = await getHistoryDetail(row.id)
    currentHistory.value = res.data
    detailDialogVisible.value = true
  } catch (error) {
    ElMessage.error('加载详情失败')
  }
}

const getProviderLabel = (provider) => {
  const labels = {
    deepseek: 'DeepSeek',
    qwen: '阿里千问',
    zhipu: '智谱AI'
  }
  return labels[provider] || provider
}

const getStatusLabel = (status) => {
  const labels = {
    0: '处理中',
    1: '成功',
    2: '失败'
  }
  return labels[status] || '未知'
}

const getStatusType = (status) => {
  const types = {
    0: 'warning',
    1: 'success',
    2: 'danger'
  }
  return types[status] || 'info'
}

onMounted(() => {
  loadHistory()
})
</script>

<style scoped>
.ai-generate {
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

.help-text {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.prompt-box,
.response-box {
  white-space: pre-wrap;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  max-height: 300px;
  overflow-y: auto;
  font-size: 13px;
}
</style>
