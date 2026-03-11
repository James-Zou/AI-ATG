<template>
  <el-card>
      <template #header>
        <div class="card-header">
          <span>需求管理</span>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            创建需求
          </el-button>
        </div>
      </template>
      
      <!-- 搜索筛选区域 -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="queryForm.keyword"
            placeholder="搜索标题或内容"
            clearable
            @clear="handleSearch"
          />
        </el-form-item>
        
        <el-form-item label="类型">
          <el-select v-model="queryForm.type" placeholder="全部" clearable>
            <el-option label="用户故事" value="user_story" />
            <el-option label="功能需求" value="feature" />
            <el-option label="Bug修复" value="bug_fix" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="优先级">
          <el-select v-model="queryForm.priority" placeholder="全部" clearable>
            <el-option label="P0" value="P0" />
            <el-option label="P1" value="P1" />
            <el-option label="P2" value="P2" />
            <el-option label="P3" value="P3" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable>
            <el-option label="草稿" value="draft" />
            <el-option label="评审中" value="reviewing" />
            <el-option label="已通过" value="approved" />
            <el-option label="测试中" value="testing" />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <!-- 表格 -->
      <el-table
        :data="requirementList"
        stripe
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="需求标题" min-width="200" />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="scope">
            <el-tag>{{ getTypeLabel(scope.row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="scope">
            <el-tag :type="getPriorityType(scope.row.priority)">
              {{ scope.row.priority }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdByName" label="创建人" width="120" />
        <el-table-column prop="createdTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleView(scope.row)">
              查看
            </el-button>
            <el-button size="small" @click="handleEdit(scope.row)">
              编辑
            </el-button>
            <el-button
              size="small"
              type="success"
              :loading="aiGeneratingRowId === scope.row.id"
              @click="handleAiGenerateCases(scope.row)"
            >
              <el-icon v-if="aiGeneratingRowId !== scope.row.id"><MagicStick /></el-icon>
              AI生成用例
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="handleDelete(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- AI生成用例配置弹窗 -->
      <el-dialog
        v-model="aiConfigDialogVisible"
        title="AI生成用例配置"
        width="500px"
      >
        <el-form :model="aiGenerateForm" label-width="100px">
          <el-form-item label="生成数量">
            <el-input-number v-model="aiGenerateForm.count" :min="1" :max="20" />
          </el-form-item>
          <el-form-item label="AI温度">
            <el-slider v-model="aiGenerateForm.temperature" :min="0" :max="1" :step="0.1" show-input />
            <el-text size="small" type="info">温度越高，生成内容越随机</el-text>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="aiConfigDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="startAiGenerate">开始生成</el-button>
        </template>
      </el-dialog>
      
      <!-- AI生成进度弹窗 -->
      <el-dialog
        v-model="aiDialogVisible"
        :title="aiDialogTitle"
        width="600px"
        :close-on-click-modal="false"
        :close-on-press-escape="false"
        :show-close="!aiGenerating"
      >
        <div class="ai-generate-demo">
          <el-icon class="demo-icon" :class="{ 'is-loading': aiGenerating }">
            <MagicStick />
          </el-icon>
          <p class="demo-status">{{ aiGenerateStatus }}</p>
          <el-progress
            v-if="aiGenerating"
            :percentage="aiProgress"
            :stroke-width="8"
          />
          
          <!-- 生成完成后显示结果 -->
          <div v-if="!aiGenerating && generatedCases.length > 0" class="result-info">
            <el-divider />
            <el-descriptions :column="2" border>
              <el-descriptions-item label="生成数量">
                {{ generatedCases.length }}
              </el-descriptions-item>
              <el-descriptions-item label="耗时">
                {{ aiDuration }}ms
              </el-descriptions-item>
              <el-descriptions-item label="AI模型">
                {{ aiModelName }}
              </el-descriptions-item>
              <el-descriptions-item label="提供商">
                {{ aiProvider }}
              </el-descriptions-item>
            </el-descriptions>
            
            <div class="case-list" style="margin-top: 16px;">
              <h4>生成的用例：</h4>
              <el-tag
                v-for="(testCase, index) in generatedCases"
                :key="index"
                type="success"
                style="margin: 4px;"
              >
                {{ testCase.title }}
              </el-tag>
            </div>
          </div>
        </div>
        
        <template #footer v-if="!aiGenerating">
          <el-button @click="aiDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="viewGeneratedCases">查看用例</el-button>
        </template>
      </el-dialog>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="queryForm.pageNum"
        v-model:page-size="queryForm.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSearch"
        @current-change="handleSearch"
        class="pagination"
      />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getRequirementList, deleteRequirement } from '@/api/requirement'
import { generateTestCases } from '@/api/ai'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, MagicStick } from '@element-plus/icons-vue'

const router = useRouter()

const loading = ref(false)
const requirementList = ref([])
const total = ref(0)

// AI生成用例配置
const aiConfigDialogVisible = ref(false)
const aiGenerateForm = ref({
  count: 5,
  temperature: 0.7
})
const currentRequirement = ref(null)

// AI生成进度
const aiGenerating = ref(false)
const aiGeneratingRowId = ref(null)
const aiDialogVisible = ref(false)
const aiDialogTitle = ref('AI生成用例')
const aiGenerateStatus = ref('')
const aiProgress = ref(0)

// AI生成结果
const generatedCases = ref([])
const aiDuration = ref(0)
const aiProvider = ref('')
const aiModelName = ref('')

const queryForm = ref({
  keyword: '',
  type: '',
  priority: '',
  status: '',
  pageNum: 1,
  pageSize: 10
})

const loadData = async () => {
  try {
    loading.value = true
    const res = await getRequirementList(queryForm.value)
    requirementList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryForm.value.pageNum = 1
  loadData()
}

const handleReset = () => {
  queryForm.value = {
    keyword: '',
    type: '',
    priority: '',
    status: '',
    pageNum: 1,
    pageSize: 10
  }
  loadData()
}

const handleCreate = () => {
  router.push('/requirement/create')
}

// AI生成用例：打开配置弹窗
const handleAiGenerateCases = (row) => {
  currentRequirement.value = row
  aiConfigDialogVisible.value = true
}

// 开始AI生成
const startAiGenerate = async () => {
  aiConfigDialogVisible.value = false
  aiGeneratingRowId.value = currentRequirement.value.id
  aiDialogTitle.value = `AI生成用例 - ${currentRequirement.value.title}`
  aiGenerating.value = true
  aiDialogVisible.value = true
  aiProgress.value = 0
  aiGenerateStatus.value = '准备中...'
  generatedCases.value = []
  
  // 模拟进度更新
  const progressSteps = [
    { status: '正在连接AI服务...', progress: 10 },
    { status: '正在分析需求内容...', progress: 30 },
    { status: '正在生成测试用例...', progress: 60 },
    { status: '正在保存用例...', progress: 90 }
  ]
  
  let progressIndex = 0
  const progressTimer = setInterval(() => {
    if (progressIndex < progressSteps.length) {
      aiGenerateStatus.value = progressSteps[progressIndex].status
      aiProgress.value = progressSteps[progressIndex].progress
      progressIndex++
    }
  }, 500)
  
  try {
    const res = await generateTestCases({
      requirementId: currentRequirement.value.id,
      count: aiGenerateForm.value.count,
      temperature: aiGenerateForm.value.temperature
    })
    
    clearInterval(progressTimer)
    aiProgress.value = 100
    aiGenerateStatus.value = '生成完成！'
    
    // 保存生成结果
    generatedCases.value = res.data.testCases || []
    aiDuration.value = res.data.duration || 0
    aiProvider.value = res.data.provider || ''
    aiModelName.value = res.data.modelName || ''
    
    aiGenerating.value = false
    aiGeneratingRowId.value = null
    
    ElMessage.success(`成功生成 ${res.data.generatedCount} 个测试用例`)
  } catch (error) {
    clearInterval(progressTimer)
    aiGenerating.value = false
    aiGeneratingRowId.value = null
    aiDialogVisible.value = false
    ElMessage.error(error.message || 'AI生成失败')
  }
}

// 查看生成的用例
const viewGeneratedCases = () => {
  aiDialogVisible.value = false
  router.push(`/testcase?requirementId=${currentRequirement.value.id}`)
}

const handleView = (row) => {
  router.push(`/requirement/detail/${row.id}`)
}

const handleEdit = (row) => {
  router.push(`/requirement/edit/${row.id}`)
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除需求"${row.title}"吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteRequirement(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const getTypeLabel = (type) => {
  const labels = {
    user_story: '用户故事',
    feature: '功能需求',
    bug_fix: 'Bug修复'
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
    reviewing: '评审中',
    approved: '已通过',
    testing: '测试中'
  }
  return labels[status] || status
}

const getStatusType = (status) => {
  const types = {
    draft: 'info',
    reviewing: 'warning',
    approved: 'success',
    testing: 'primary'
  }
  return types[status] || 'info'
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.ai-generate-demo {
  padding: 20px 0;
  text-align: center;
}

.ai-generate-demo .demo-icon {
  font-size: 48px;
  color: var(--el-color-success);
  margin-bottom: 16px;
}

.ai-generate-demo .demo-icon.is-loading {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.ai-generate-demo .demo-status {
  margin: 0 0 20px;
  font-size: 15px;
  color: var(--el-text-color-regular);
}

.ai-generate-demo .result-info {
  text-align: left;
  margin-top: 20px;
}

.ai-generate-demo .case-list h4 {
  margin: 0 0 12px;
  color: var(--el-text-color-primary);
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>
