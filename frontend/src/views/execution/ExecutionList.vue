<template>
  <div class="execution-list">
    <!-- 本地服务状态检查 -->
    <LocalServiceChecker @service-status="onServiceStatus" />
    
    <!-- 快速使用指南 -->
    <QuickGuide :test-type="selectedTestType" />
    
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span>测试执行</span>
            <el-radio-group v-model="selectedTestType" size="small" style="margin-left: 20px;">
              <el-radio-button label="ui">🖥️ UI自动化</el-radio-button>
              <el-radio-button label="api">🔌 接口自动化</el-radio-button>
              <el-radio-button label="performance">⚡ 性能自动化</el-radio-button>
            </el-radio-group>
          </div>
          <div class="header-actions">
            <el-button @click="showHelp">
              <el-icon><QuestionFilled /></el-icon>
              帮助文档
            </el-button>
            <el-button @click="goToDownloads">
              <el-icon><Download /></el-icon>
              下载中心
            </el-button>
            <el-button type="primary" @click="showExecutionDialog">
              <el-icon><VideoPlay /></el-icon>
              开始执行
            </el-button>
          </div>
        </div>
      </template>
      
      <el-table :data="executionList" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="executionName" label="执行名称" min-width="200" />
        <el-table-column prop="executionType" label="类型" width="100">
          <template #default="scope">
            <el-tag>{{ getTypeLabel(scope.row.executionType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="environment" label="环境" width="100" />
        <el-table-column prop="totalCases" label="用例数" width="90" />
        <el-table-column prop="passedCases" label="通过" width="80">
          <template #default="scope">
            <span style="color: #67C23A">{{ scope.row.passedCases || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="failedCases" label="失败" width="80">
          <template #default="scope">
            <span style="color: #F56C6C">{{ scope.row.failedCases || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="耗时(ms)" width="100" />
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="viewDetail(scope.row)">
              查看详情
            </el-button>
            <el-button
              v-if="scope.row.status === 1"
              size="small"
              type="danger"
              @click="stopExecution(scope.row)"
            >
              停止
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="loadData"
        @size-change="handleSizeChange"
        class="pagination"
      />
    </el-card>
    
    <!-- 执行对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="创建测试执行"
      width="600px"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="执行类型" prop="executionType">
          <el-select v-model="formData.executionType" placeholder="请选择">
            <el-option label="API测试" value="api" />
            <el-option label="UI测试" value="ui" />
            <el-option label="性能测试" value="performance" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="测试套件" prop="suiteId">
          <el-select
            v-model="formData.suiteId"
            placeholder="请选择测试套件"
            filterable
          >
            <el-option
              v-for="suite in suiteList"
              :key="suite.id"
              :label="suite.name"
              :value="suite.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item v-if="executionScope" label="执行范围">
          <el-alert :closable="false" type="info" show-icon>
            {{ executionScope }}
          </el-alert>
        </el-form-item>
        
        <el-form-item>
          <el-alert :closable="false" type="warning" show-icon>
            ⚠️ 将只执行测试套件中状态为"已通过"的用例
          </el-alert>
        </el-form-item>
        
        <el-form-item label="测试环境" prop="environment">
          <el-select 
            v-model="formData.environment" 
            placeholder="请选择测试环境"
          >
            <el-option
              v-for="env in environmentList"
              :key="env.id"
              :label="env.envName"
              :value="env.envCode"
            >
              <div style="display: flex; justify-content: space-between; align-items: center;">
                <span>{{ env.envName }}</span>
                <span style="font-size: 12px; color: #909399;">{{ env.envCode }}</span>
              </div>
            </el-option>
          </el-select>
          <div style="margin-top: 8px;">
            <el-link type="primary" href="/#/environment/list" target="_blank" :underline="false">
              <el-icon><Setting /></el-icon>
              管理测试环境
            </el-link>
          </div>
        </el-form-item>
        
        <el-form-item label="触发方式">
          <el-radio-group v-model="formData.triggerType">
            <el-radio label="manual">手动触发</el-radio>
            <el-radio label="schedule">定时触发</el-radio>
            <el-radio label="ci">CI触发</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="handleExecute"
          :loading="executing"
        >
          {{ executing ? '执行中...' : '开始执行' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { createExecution, getExecutionList, stopExecution as stopExecutionApi } from '@/api/execution'
import { getTestSuiteList } from '@/api/testsuite'
import { getAllEnvironments } from '@/api/environment'
import { ElMessage, ElMessageBox } from 'element-plus'
import { VideoPlay, Setting, QuestionFilled, Download } from '@element-plus/icons-vue'
import LocalServiceChecker from '@/components/LocalServiceChecker.vue'
import QuickGuide from '@/components/QuickGuide.vue'

const router = useRouter()

const loading = ref(false)
const executing = ref(false)
const executionList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const serviceRunning = ref(false)
const selectedTestType = ref('ui') // 默认显示UI自动化指南

const dialogVisible = ref(false)
const suiteList = ref([])
const environmentList = ref([])

const formRef = ref()
const formData = reactive({
  projectId: 1,
  suiteId: null,
  executionType: '',
  environment: 'test',
  triggerType: 'manual'
})

const formRules = {
  executionType: [
    { required: true, message: '请选择执行类型', trigger: 'change' }
  ],
  suiteId: [
    { required: true, message: '请选择测试套件', trigger: 'change' }
  ],
  environment: [
    { required: true, message: '请选择测试环境', trigger: 'change' }
  ]
}

// 计算执行范围描述
const executionScope = computed(() => {
  if (formData.suiteId) {
    const suite = suiteList.value.find(s => s.id === formData.suiteId)
    return suite ? `将执行套件「${suite.name}」中状态为"已通过"的用例` : ''
  }
  return ''
})

const loadData = async () => {
  try {
    loading.value = true
    console.log('请求执行列表 - 参数:', {
      projectId: 1,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    
    const res = await getExecutionList({
      projectId: 1,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    
    console.log('执行列表响应:', res.data)
    console.log('记录数:', res.data.records?.length, '总数:', res.data.total)
    
    executionList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('加载执行列表失败:', error)
    ElMessage.error(error.message || '加载失败，请稍后重试')
    executionList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleSizeChange = () => {
  console.log('每页大小改变:', pageSize.value)
  pageNum.value = 1 // 改变每页大小时重置到第一页
  loadData()
}

const loadSuites = async () => {
  try {
    const res = await getTestSuiteList({ projectId: 1, pageNum: 1, pageSize: 100 })
    suiteList.value = res.data.records
  } catch (error) {
    console.error('加载套件列表失败', error)
  }
}

const loadEnvironments = async () => {
  try {
    const res = await getAllEnvironments(1)
    environmentList.value = res.data
  } catch (error) {
    console.error('加载环境列表失败', error)
  }
}

const showExecutionDialog = () => {
  dialogVisible.value = true
}

const handleExecute = async () => {
  try {
    await formRef.value.validate()
    executing.value = true
    
    const res = await createExecution(formData)
    
    ElMessage.success('执行已开始！')
    dialogVisible.value = false
    
    // 刷新列表
    loadData()
    
    // 跳转到详情页查看实时进度
    router.push(`/execution/detail/${res.data.id}`)
    
  } catch (error) {
    if (error) {
      ElMessage.error(error.message || '执行失败')
    }
  } finally {
    executing.value = false
  }
}

const viewDetail = (row) => {
  router.push(`/execution/detail/${row.id}`)
}

const stopExecution = async (row) => {
  try {
    await ElMessageBox.confirm('确定要停止执行吗？', '停止确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await stopExecutionApi(row.id)
    ElMessage.success('执行已停止')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('停止失败')
    }
  }
}

const onServiceStatus = (running) => {
  serviceRunning.value = running
  if (!running) {
    ElMessage.warning('ATG-Client未运行，请先安装并启动服务')
  }
}

const showHelp = () => {
  router.push('/help/ui-test')
}

const goToDownloads = () => {
  router.push('/help/downloads')
}

const getTypeLabel = (type) => {
  const labels = {
    api: 'API测试',
    ui: 'UI测试',
    performance: '性能测试'
  }
  return labels[type] || type
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

onMounted(() => {
  loadData()
  loadSuites()
  loadEnvironments()
})
</script>

<style scoped>
.execution-list {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>
