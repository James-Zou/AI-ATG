<template>
  <div class="testcase-form">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑测试用例' : '创建测试用例' }}</span>
          <el-button @click="handleBack">返回</el-button>
        </div>
      </template>
      
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="所属项目" prop="projectId">
          <el-select 
            v-model="formData.projectId" 
            placeholder="请选择所属项目"
            filterable
            style="width: 100%;"
          >
            <el-option
              v-for="project in projectList"
              :key="project.id"
              :label="project.name"
              :value="project.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="用例标题" prop="title">
          <el-input
            v-model="formData.title"
            placeholder="请输入用例标题"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="用例描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="4"
            placeholder="请输入用例描述"
          />
        </el-form-item>
        
        <el-form-item label="前置条件">
          <el-input
            v-model="formData.precondition"
            type="textarea"
            :rows="3"
            placeholder="请输入前置条件"
          />
        </el-form-item>
        
        <el-form-item label="用例类型" prop="type">
          <el-select 
            v-model="formData.type" 
            placeholder="请选择用例类型"
            @change="handleTypeChange"
          >
            <el-option label="UI自动化测试" value="ui">
              <span style="display: flex; align-items: center; justify-content: space-between;">
                <span>🖥️ UI自动化测试</span>
                <el-tag size="small" type="info">需要ATG-Client</el-tag>
              </span>
            </el-option>
            <el-option label="接口自动化测试" value="api">
              <span style="display: flex; align-items: center; justify-content: space-between;">
                <span>🔌 接口自动化测试</span>
                <el-tag size="small" type="success">JMeter</el-tag>
              </span>
            </el-option>
            <el-option label="性能自动化测试" value="performance">
              <span style="display: flex; align-items: center; justify-content: space-between;">
                <span>⚡ 性能自动化测试</span>
                <el-tag size="small" type="warning">JMeter</el-tag>
              </span>
            </el-option>
          </el-select>
        </el-form-item>
        
        <!-- 类型说明 -->
        <el-alert
          v-if="formData.type"
          :title="getTypeDescription(formData.type)"
          :type="getTypeAlertType(formData.type)"
          :closable="false"
          show-icon
          style="margin-bottom: 20px;"
        />
        
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="formData.priority" placeholder="请选择优先级">
            <el-option label="P0 - 紧急" value="P0" />
            <el-option label="P1 - 重要" value="P1" />
            <el-option label="P2 - 普通" value="P2" />
            <el-option label="P3 - 低" value="P3" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="测试套件" prop="suiteId">
          <el-select
            v-model="formData.suiteId"
            placeholder="请选择测试套件"
            clearable
            filterable
          >
            <el-option
              v-for="suite in suiteList"
              :key="suite.id"
              :label="suite.name"
              :value="suite.id"
            />
          </el-select>
          <div v-if="formData.status === 'approved'" style="margin-top: 8px;">
            <el-text type="warning" size="small">
              <el-icon><WarningFilled /></el-icon>
              用例状态为"已通过"时必须选择测试套件
            </el-text>
          </div>
        </el-form-item>
        
        <el-form-item label="关联需求">
          <el-input
            v-model.number="formData.requirementId"
            type="number"
            placeholder="请输入需求ID"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="用例状态" prop="status">
          <el-select v-model="formData.status" placeholder="请选择状态">
            <el-option label="草稿" value="draft" />
            <el-option label="待审核" value="pending" />
            <el-option label="已通过" value="approved" />
          </el-select>
        </el-form-item>
        
        <!-- 测试步骤 -->
        <el-divider content-position="left">
          <span style="font-size: 16px; font-weight: bold;">
            {{ getStepEditorTitle(formData.type) }}
          </span>
        </el-divider>
        
        <!-- UI 自动化测试步骤编辑器 -->
        <el-form-item v-if="formData.type === 'ui'" prop="steps">
          <StepEditor v-model="formData.executableSteps" />
        </el-form-item>
        
        <!-- 接口自动化测试配置 -->
        <el-form-item v-else-if="formData.type === 'api'" prop="steps">
          <JMeterApiEditor v-model="formData.executableSteps" />
        </el-form-item>
        
        <!-- 性能自动化测试配置 -->
        <el-form-item v-else-if="formData.type === 'performance'" prop="steps">
          <JMeterPerformanceEditor v-model="formData.executableSteps" />
        </el-form-item>
        
        <!-- 未选择类型提示 -->
        <el-alert
          v-else
          title="请先选择用例类型"
          type="warning"
          :closable="false"
          show-icon
        >
          <template #default>
            <p>不同的测试类型需要配置不同的测试脚本：</p>
            <ul style="margin: 5px 0; padding-left: 20px;">
              <li>UI自动化：配置浏览器操作步骤（需要ATG-Client和WebDriver）</li>
              <li>接口自动化：配置HTTP请求（通过JMeter在服务端执行）</li>
              <li>性能自动化：配置性能测试场景（通过JMeter在服务端执行）</li>
            </ul>
          </template>
        </el-alert>
        
        <el-form-item>
          <el-button
            type="primary"
            @click="handleSubmit"
            :loading="submitting"
          >
            {{ submitting ? '提交中...' : '提交' }}
          </el-button>
          <el-button @click="handleBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { createTestCase, updateTestCase, getTestCaseDetail } from '@/api/testcase'
import { getTestSuiteList } from '@/api/testsuite'
import { getProjectList } from '@/api/project'
import { ElMessage } from 'element-plus'
import { WarningFilled } from '@element-plus/icons-vue'
import StepEditor from '@/components/StepEditor.vue'
import JMeterApiEditor from '@/components/JMeterApiEditor.vue'
import JMeterPerformanceEditor from '@/components/JMeterPerformanceEditor.vue'

const router = useRouter()
const route = useRoute()

const formRef = ref()
const submitting = ref(false)
const suiteList = ref([])
const projectList = ref([])

const isEdit = computed(() => !!route.params.id)

const formData = reactive({
  projectId: null,
  title: '',
  description: '',
  precondition: '',
  type: '',
  priority: '',
  suiteId: null,
  requirementId: null,
  status: 'draft',
  executableSteps: []
})

// 动态验证规则（状态为"已通过"时套件必选）
const formRules = computed(() => ({
  projectId: [
    { required: true, message: '请选择所属项目', trigger: 'change' }
  ],
  title: [
    { required: true, message: '请输入用例标题', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择用例类型', trigger: 'change' }
  ],
  priority: [
    { required: true, message: '请选择优先级', trigger: 'change' }
  ],
  suiteId: formData.status === 'approved' ? [
    { required: true, message: '用例状态为"已通过"时必须选择测试套件', trigger: 'change' }
  ] : []
}))

const loadProjects = async () => {
  try {
    const res = await getProjectList({ pageNum: 1, pageSize: 100 })
    projectList.value = res.data.records
  } catch (error) {
    console.error('加载项目列表失败', error)
  }
}

const loadSuites = async () => {
  try {
    // 如果没有选择项目，则不加载套件
    if (!formData.projectId) {
      suiteList.value = []
      return
    }
    
    const res = await getTestSuiteList({ 
      projectId: formData.projectId, 
      pageNum: 1, 
      pageSize: 100 
    })
    suiteList.value = res.data.records
  } catch (error) {
    console.error('加载套件列表失败', error)
  }
}

const loadData = async () => {
  if (!isEdit.value) return
  
  try {
    const res = await getTestCaseDetail(route.params.id)
    console.log('加载的测试用例数据:', res.data)
    
    // 复制基本字段
    formData.projectId = res.data.projectId
    formData.title = res.data.title
    formData.description = res.data.description
    formData.precondition = res.data.preconditions
    formData.type = res.data.type
    formData.priority = res.data.priority
    formData.suiteId = res.data.suiteId
    formData.requirementId = res.data.requirementId
    formData.status = res.data.status
    
    // 处理步骤数据 - 解析JSON字符串为数组
    if (res.data.stepsJson) {
      try {
        const steps = typeof res.data.stepsJson === 'string' 
          ? JSON.parse(res.data.stepsJson) 
          : res.data.stepsJson
        
        console.log('解析后的步骤:', steps)
        
        // 根据测试类型过滤有效步骤
        if (res.data.type === 'ui') {
          // UI测试：需要有action字段
          formData.executableSteps = steps.filter(step => step && step.action)
        } else if (res.data.type === 'api') {
          // 接口测试：需要有url字段
          formData.executableSteps = steps.filter(step => step && step.url)
        } else if (res.data.type === 'performance') {
          // 性能测试：需要有url或threads字段
          formData.executableSteps = steps.filter(step => step && (step.url || step.threads))
        } else {
          // 其他类型：保留所有非空对象
          formData.executableSteps = steps.filter(step => step && typeof step === 'object')
        }
      } catch (e) {
        console.error('解析步骤失败', e)
        formData.executableSteps = []
      }
    } else {
      formData.executableSteps = []
    }
    
    console.log('最终的executableSteps:', formData.executableSteps)
  } catch (error) {
    console.error('加载测试用例失败', error)
    ElMessage.error('加载测试用例失败')
    handleBack()
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    // 验证步骤
    if (!formData.executableSteps || formData.executableSteps.length === 0) {
      ElMessage.warning('请添加至少一个测试步骤')
      return
    }
    
    console.log('提交前的步骤数据:', formData.executableSteps)
    
    submitting.value = true
    
    // 准备提交数据
    const submitData = {
      projectId: formData.projectId,
      title: formData.title,
      description: formData.description,
      precondition: formData.precondition,
      type: formData.type,
      priority: formData.priority,
      suiteId: formData.suiteId,
      requirementId: formData.requirementId,
      status: formData.status,
      steps: formData.executableSteps  // 直接传数组
    }
    
    console.log('提交的完整数据:', submitData)
    
    if (isEdit.value) {
      await updateTestCase(route.params.id, submitData)
      ElMessage.success('更新成功')
    } else {
      await createTestCase(submitData)
      ElMessage.success('创建成功')
    }
    
    handleBack()
  } catch (error) {
    console.error('提交失败', error)
    if (error) {
      ElMessage.error(error.message || '提交失败')
    }
  } finally {
    submitting.value = false
  }
}

const handleBack = () => {
  router.push('/testcase/list')
}

// 处理类型变更
const handleTypeChange = (newType) => {
  // 切换类型时清空步骤，避免格式冲突
  if (formData.executableSteps && formData.executableSteps.length > 0) {
    ElMessage.warning('切换用例类型将清空已配置的测试步骤')
    formData.executableSteps = []
  }
}

// 获取类型描述
const getTypeDescription = (type) => {
  const descriptions = {
    'ui': '🖥️ UI自动化测试：通过ATG-Client在本地浏览器中执行UI操作，需要安装WebDriver插件',
    'api': '🔌 接口自动化测试：通过JMeter在服务端执行HTTP请求测试，无需本地环境',
    'performance': '⚡ 性能自动化测试：通过JMeter在服务端执行性能压测，支持并发和持续时间配置'
  }
  return descriptions[type] || ''
}

// 获取Alert类型
const getTypeAlertType = (type) => {
  const types = {
    'ui': 'info',
    'api': 'success',
    'performance': 'warning'
  }
  return types[type] || 'info'
}

// 获取步骤编辑器标题
const getStepEditorTitle = (type) => {
  const titles = {
    'ui': '🎬 UI自动化测试步骤',
    'api': '🔌 接口自动化测试配置',
    'performance': '⚡ 性能自动化测试配置'
  }
  return titles[type] || '测试配置'
}

// 监听项目变更，重新加载套件列表
watch(() => formData.projectId, (newProjectId) => {
  if (newProjectId) {
    loadSuites()
    // 切换项目时清空套件选择
    if (formData.suiteId) {
      formData.suiteId = null
    }
  } else {
    suiteList.value = []
  }
})

onMounted(async () => {
  await loadProjects()
  await loadData()
  // 加载完数据后，如果有项目ID，则加载套件
  if (formData.projectId) {
    loadSuites()
  }
})
</script>

<style scoped>
.testcase-form {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.step-item {
  margin-bottom: 15px;
}

.step-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
