<template>
  <div class="skills-container">
    <div class="header-section">
      <div class="header-title">
        <el-icon class="title-icon"><MagicStick /></el-icon>
        <h2>技能列表</h2>
      </div>
      <el-button type="primary" @click="showAddDialog">
        <el-icon><Plus /></el-icon>
        添加技能
      </el-button>
    </div>

    <el-card class="filter-card">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="技能名称">
          <el-input 
            v-model="queryParams.name" 
            placeholder="输入技能名称搜索"
            clearable
            @clear="fetchSkills"
          />
        </el-form-item>
        <el-form-item label="技能类型">
          <el-select v-model="queryParams.type" placeholder="选择类型" clearable @clear="fetchSkills">
            <el-option label="测试套件" value="TESTSUITE" />
            <el-option label="自定义脚本" value="SCRIPT" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchSkills">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table 
        :data="skillsList" 
        v-loading="loading"
        stripe
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="技能名称" min-width="150">
          <template #default="{ row }">
            <el-tag :type="row.type === 'TESTSUITE' ? 'success' : 'primary'" effect="plain">
              {{ row.name }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.type === 'TESTSUITE' ? 'success' : 'warning'">
              {{ row.type === 'TESTSUITE' ? '测试套件' : '自定义脚本' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="enabled" label="状态" width="100">
          <template #default="{ row }">
            <el-switch 
              v-model="row.enabled" 
              @change="toggleEnabled(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="viewDetail(row)">
              查看
            </el-button>
            <el-button link type="primary" size="small" @click="editSkill(row)">
              编辑
            </el-button>
            <el-button link type="primary" size="small" @click="testSkill(row)">
              测试执行
            </el-button>
            <el-button link type="danger" size="small" @click="deleteSkill(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="fetchSkills"
          @current-change="fetchSkills"
        />
      </div>
    </el-card>

    <!-- 添加技能弹窗 -->
    <el-dialog
      v-model="addDialogVisible"
      title="添加技能"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-steps :active="currentStep" finish-status="success" align-center>
        <el-step title="选择类型" />
        <el-step title="配置技能" />
        <el-step title="完成" />
      </el-steps>

      <div class="step-content">
        <!-- Step 1: 选择技能类型 -->
        <div v-if="currentStep === 0" class="skill-type-selection">
          <el-card 
            class="type-card"
            :class="{ active: skillForm.type === 'TESTSUITE' }"
            @click="skillForm.type = 'TESTSUITE'"
            shadow="hover"
          >
            <div class="type-icon">
              <el-icon><Files /></el-icon>
            </div>
            <h3>导入测试套件</h3>
            <p>将现有测试套件转换为可执行的 Skill</p>
          </el-card>
          <el-card 
            class="type-card"
            :class="{ active: skillForm.type === 'SCRIPT' }"
            @click="skillForm.type = 'SCRIPT'"
            shadow="hover"
          >
            <div class="type-icon">
              <el-icon><EditPen /></el-icon>
            </div>
            <h3>自定义脚本</h3>
            <p>编写 Python/JavaScript/Shell 脚本创建 Skill</p>
          </el-card>
        </div>

        <!-- Step 2: 配置技能 - 测试套件类型 -->
        <div v-if="currentStep === 1 && skillForm.type === 'TESTSUITE'" class="skill-config">
          <el-form :model="skillForm" label-width="100px">
            <el-form-item label="选择套件" required>
              <el-select 
                v-model="skillForm.testSuiteId" 
                placeholder="选择要导入的测试套件（同一套件可多次导入）"
                filterable
                style="width: 100%"
              >
                <el-option
                  v-for="suite in availableSuites"
                  :key="suite.id"
                  :label="suite.name"
                  :value="suite.id"
                >
                  <span>{{ suite.name }}</span>
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="技能名称" required>
              <el-input v-model="skillForm.name" placeholder="输入技能名称" />
            </el-form-item>
            <el-form-item label="技能描述" required>
              <el-input 
                v-model="skillForm.description" 
                type="textarea" 
                :rows="3"
                placeholder="描述这个技能的功能"
              />
            </el-form-item>
            
            <!-- 测试步骤配置（可编辑） -->
            <el-form-item label="测试步骤" v-if="skillForm.testSuiteId">
              <div class="steps-editor-container">
                <div class="editor-header">
                  <el-tag type="info" size="small">
                    <el-icon><InfoFilled /></el-icon>
                    可以使用 {参数名称} 设置变量占位符，例如：{工单标题}、{用户名}
                  </el-tag>
                </div>
                <el-input
                  v-model="skillForm.configData"
                  type="textarea"
                  :rows="15"
                  placeholder="测试步骤 JSON 配置..."
                  :loading="loadingSteps"
                  style="font-family: 'Courier New', monospace; font-size: 13px;"
                />
                <div class="editor-footer">
                  <el-button 
                    size="small" 
                    @click="formatStepsJson"
                    :disabled="!skillForm.configData"
                  >
                    格式化 JSON
                  </el-button>
                  <el-button 
                    size="small" 
                    type="warning"
                    @click="resetSteps"
                    :disabled="!skillForm.testSuiteId"
                  >
                    重置步骤
                  </el-button>
                </div>
              </div>
            </el-form-item>
          </el-form>
        </div>

        <!-- Step 2: 配置技能 - 脚本类型 -->
        <div v-if="currentStep === 1 && skillForm.type === 'SCRIPT'" class="skill-config">
          <el-form :model="skillForm" label-width="100px">
            <el-form-item label="技能名称" required>
              <el-input v-model="skillForm.name" placeholder="输入技能名称" />
            </el-form-item>
            <el-form-item label="技能描述" required>
              <el-input 
                v-model="skillForm.description" 
                type="textarea" 
                :rows="2"
                placeholder="描述这个技能的功能"
              />
            </el-form-item>
            <el-form-item label="脚本语言" required>
              <el-select v-model="skillForm.scriptLanguage" placeholder="选择脚本语言">
                <el-option label="Python" value="python" />
                <el-option label="JavaScript" value="javascript" />
                <el-option label="Shell" value="shell" />
              </el-select>
            </el-form-item>
            <el-form-item label="脚本内容" required>
              <el-input
                v-model="skillForm.scriptContent"
                type="textarea"
                :rows="12"
                placeholder="输入脚本代码..."
                style="font-family: 'Courier New', monospace;"
              />
            </el-form-item>
          </el-form>
        </div>

        <!-- Step 3: 完成确认 -->
        <div v-if="currentStep === 2" class="skill-confirm">
          <el-result icon="success" title="技能配置完成" sub-title="请确认以下信息">
            <template #extra>
              <el-descriptions :column="1" border>
                <el-descriptions-item label="技能类型">
                  {{ skillForm.type === 'TESTSUITE' ? '测试套件' : '自定义脚本' }}
                </el-descriptions-item>
                <el-descriptions-item label="技能名称">{{ skillForm.name }}</el-descriptions-item>
                <el-descriptions-item label="技能描述">{{ skillForm.description }}</el-descriptions-item>
                <el-descriptions-item v-if="skillForm.type === 'SCRIPT'" label="脚本语言">
                  {{ skillForm.scriptLanguage }}
                </el-descriptions-item>
              </el-descriptions>
            </template>
          </el-result>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="addDialogVisible = false">取消</el-button>
          <el-button v-if="currentStep > 0" @click="prevStep">上一步</el-button>
          <el-button v-if="currentStep < 2" type="primary" @click="nextStep">下一步</el-button>
          <el-button v-if="currentStep === 2" type="primary" @click="submitSkill" :loading="submitting">
            确认创建
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 技能详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="技能详情"
      width="700px"
    >
      <el-descriptions :column="1" border v-if="currentSkill">
        <el-descriptions-item label="技能ID">{{ currentSkill.id }}</el-descriptions-item>
        <el-descriptions-item label="技能名称">{{ currentSkill.name }}</el-descriptions-item>
        <el-descriptions-item label="技能类型">
          {{ currentSkill.type === 'TESTSUITE' ? '测试套件' : '自定义脚本' }}
        </el-descriptions-item>
        <el-descriptions-item label="技能描述">{{ currentSkill.description }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentSkill.enabled ? 'success' : 'info'">
            {{ currentSkill.enabled ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="currentSkill.type === 'TESTSUITE'" label="关联套件ID">
          {{ currentSkill.testSuiteId }}
        </el-descriptions-item>
        <el-descriptions-item v-if="currentSkill.type === 'SCRIPT'" label="脚本语言">
          {{ currentSkill.scriptLanguage }}
        </el-descriptions-item>
        <el-descriptions-item v-if="currentSkill.type === 'SCRIPT'" label="脚本内容">
          <pre class="script-preview">{{ currentSkill.scriptContent }}</pre>
        </el-descriptions-item>
        <el-descriptions-item v-if="currentSkill.type === 'TESTSUITE'" label="配置数据">
          <pre class="script-preview">{{ currentSkill.configData }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentSkill.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 编辑技能弹窗 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑技能"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form :model="editForm" label-width="100px" v-if="editForm">
        <el-form-item label="技能名称">
          <el-input v-model="editForm.name" placeholder="输入技能名称" />
        </el-form-item>
        <el-form-item label="技能描述">
          <el-input 
            v-model="editForm.description" 
            type="textarea" 
            :rows="3"
            placeholder="描述这个技能的功能"
          />
        </el-form-item>
        <el-form-item label="技能类型">
          <el-tag :type="editForm.type === 'TESTSUITE' ? 'success' : 'warning'">
            {{ editForm.type === 'TESTSUITE' ? '测试套件' : '自定义脚本' }}
          </el-tag>
          <span style="margin-left: 10px; color: #999; font-size: 12px;">
            （技能类型不可修改）
          </span>
        </el-form-item>
        <el-form-item v-if="editForm.type === 'SCRIPT'" label="脚本语言">
          <el-select v-model="editForm.scriptLanguage" placeholder="选择脚本语言">
            <el-option label="Python" value="python" />
            <el-option label="JavaScript" value="javascript" />
            <el-option label="Shell" value="shell" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="editForm.type === 'SCRIPT'" label="脚本内容">
          <el-input
            v-model="editForm.scriptContent"
            type="textarea"
            :rows="12"
            placeholder="输入脚本代码..."
            style="font-family: 'Courier New', monospace;"
          />
        </el-form-item>
        <el-form-item v-if="editForm.type === 'TESTSUITE'" label="配置数据">
          <el-input
            v-model="editForm.configData"
            type="textarea"
            :rows="12"
            placeholder="JSON格式的测试步骤配置..."
            style="font-family: 'Courier New', monospace;"
          />
        </el-form-item>
        <el-form-item label="启用状态">
          <el-switch v-model="editForm.enabled" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEdit" :loading="submitting">
            保存修改
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, MagicStick, Files, EditPen, InfoFilled } from '@element-plus/icons-vue'
import { 
  getSkillList, 
  createSkill, 
  updateSkill, 
  deleteSkillById,
  importFromTestSuite 
} from '@/api/skills'
import { getTestSuiteList, getSuiteSteps } from '@/api/testsuite'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const skillsList = ref([])
const total = ref(0)
const queryParams = ref({
  name: '',
  type: '',
  pageNum: 1,
  pageSize: 10
})

const addDialogVisible = ref(false)
const editDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentStep = ref(0)
const currentSkill = ref(null)
const availableSuites = ref([])

const skillForm = ref({
  type: 'TESTSUITE',
  name: '',
  description: '',
  testSuiteId: null,
  configData: '', // 存储测试步骤的 JSON 配置
  scriptLanguage: 'python',
  scriptContent: '',
  enabled: true
})

const loadingSteps = ref(false) // 加载步骤状态

const editForm = ref(null)

onMounted(() => {
  fetchSkills()
})

// 监听测试套件选择变化，自动加载步骤
watch(() => skillForm.value.testSuiteId, (newSuiteId) => {
  if (newSuiteId && skillForm.value.type === 'TESTSUITE') {
    loadSuiteSteps(newSuiteId)
  }
})

const fetchSkills = async () => {
  loading.value = true
  try {
    const response = await getSkillList(queryParams.value)
    skillsList.value = response.data.records || []
    total.value = response.data.total || 0
  } catch (error) {
    ElMessage.error(error.message || '获取技能列表失败')
  } finally {
    loading.value = false
  }
}

const showAddDialog = async () => {
  resetForm()
  if (skillForm.value.type === 'TESTSUITE') {
    await loadAvailableSuites()
  }
  addDialogVisible.value = true
}

const loadAvailableSuites = async () => {
  try {
    const response = await getTestSuiteList({ pageNum: 1, pageSize: 100 })
    // 直接使用所有套件，移除重复导入限制
    // 同一个套件可以导入多次成为不同的技能（独立副本）
    availableSuites.value = response.data.records || []
  } catch (error) {
    ElMessage.error('加载测试套件失败')
  }
}

// 加载套件的测试步骤
const loadSuiteSteps = async (suiteId) => {
  if (!suiteId) {
    skillForm.value.configData = ''
    return
  }
  
  loadingSteps.value = true
  try {
    const response = await getSuiteSteps(suiteId)
    // 后端返回的是格式化的 JSON 字符串
    skillForm.value.configData = response.data || '[]'
  } catch (error) {
    ElMessage.error('加载套件步骤失败')
    skillForm.value.configData = '[]'
  } finally {
    loadingSteps.value = false
  }
}

const nextStep = async () => {
  if (currentStep.value === 0) {
    if (!skillForm.value.type) {
      ElMessage.warning('请选择技能类型')
      return
    }
    if (skillForm.value.type === 'TESTSUITE') {
      await loadAvailableSuites()
    }
  }
  
  if (currentStep.value === 1) {
    if (!skillForm.value.name || !skillForm.value.name.trim()) {
      ElMessage.warning('请输入技能名称')
      return
    }
    if (!skillForm.value.description || !skillForm.value.description.trim()) {
      ElMessage.warning('请输入技能描述')
      return
    }
    if (skillForm.value.type === 'TESTSUITE' && !skillForm.value.testSuiteId) {
      ElMessage.warning('请选择测试套件')
      return
    }
    if (skillForm.value.type === 'SCRIPT' && !skillForm.value.scriptContent) {
      ElMessage.warning('请输入脚本内容')
      return
    }
  }
  
  currentStep.value++
}

const prevStep = () => {
  currentStep.value--
}

const submitSkill = async () => {
  submitting.value = true
  try {
    if (skillForm.value.type === 'TESTSUITE') {
      await importFromTestSuite({
        testSuiteId: skillForm.value.testSuiteId,
        name: skillForm.value.name.trim(),
        description: skillForm.value.description.trim(),
        configData: skillForm.value.configData // 提交编辑后的步骤
      })
    } else {
      await createSkill(skillForm.value)
    }
    
    ElMessage.success('技能创建成功')
    addDialogVisible.value = false
    fetchSkills()
  } catch (error) {
    ElMessage.error(error.message || '创建失败')
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  currentStep.value = 0
  skillForm.value = {
    type: 'TESTSUITE',
    name: '',
    description: '',
    testSuiteId: null,
    configData: '',
    scriptLanguage: 'python',
    scriptContent: '',
    enabled: true
  }
}

// 格式化 JSON
const formatStepsJson = () => {
  try {
    const parsed = JSON.parse(skillForm.value.configData)
    skillForm.value.configData = JSON.stringify(parsed, null, 2)
    ElMessage.success('JSON 格式化成功')
  } catch (error) {
    ElMessage.error('JSON 格式不正确，无法格式化')
  }
}

// 重置步骤为原始值
const resetSteps = async () => {
  if (skillForm.value.testSuiteId) {
    await loadSuiteSteps(skillForm.value.testSuiteId)
    ElMessage.success('步骤已重置')
  }
}

const resetQuery = () => {
  queryParams.value = {
    name: '',
    type: '',
    pageNum: 1,
    pageSize: 10
  }
  fetchSkills()
}

const toggleEnabled = async (row) => {
  try {
    await updateSkill(row.id, { enabled: row.enabled })
    ElMessage.success(row.enabled ? '技能已启用' : '技能已禁用')
  } catch (error) {
    row.enabled = !row.enabled
    ElMessage.error('状态更新失败')
  }
}

const viewDetail = (row) => {
  currentSkill.value = row
  detailDialogVisible.value = true
}

const editSkill = (row) => {
  editForm.value = {
    id: row.id,
    name: row.name,
    description: row.description,
    type: row.type,
    testSuiteId: row.testSuiteId,
    scriptLanguage: row.scriptLanguage,
    scriptContent: row.scriptContent,
    configData: row.configData,
    enabled: row.enabled
  }
  editDialogVisible.value = true
}

const submitEdit = async () => {
  if (!editForm.value.name || !editForm.value.name.trim()) {
    ElMessage.warning('请输入技能名称')
    return
  }
  
  if (!editForm.value.description || !editForm.value.description.trim()) {
    ElMessage.warning('请输入技能描述')
    return
  }
  
  if (editForm.value.type === 'SCRIPT' && !editForm.value.scriptContent) {
    ElMessage.warning('请输入脚本内容')
    return
  }
  
  submitting.value = true
  try {
    await updateSkill(editForm.value.id, {
      name: editForm.value.name.trim(),
      description: editForm.value.description.trim(),
      scriptLanguage: editForm.value.scriptLanguage,
      scriptContent: editForm.value.scriptContent,
      configData: editForm.value.configData,
      enabled: editForm.value.enabled,
      type: editForm.value.type
    })
    
    ElMessage.success('技能更新成功')
    editDialogVisible.value = false
    fetchSkills()
  } catch (error) {
    ElMessage.error(error.message || '更新失败')
  } finally {
    submitting.value = false
  }
}

const testSkill = async (row) => {
  router.push({
    path: '/atgbot',
    query: { skillId: row.id, skillName: row.name }
  })
}

const deleteSkill = (row) => {
  ElMessageBox.confirm(`确定要删除技能 "${row.name}" 吗？`, '确认删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteSkillById(row.id)
      ElMessage.success('删除成功')
      fetchSkills()
    } catch (error) {
      ElMessage.error(error.message || '删除失败')
    }
  }).catch(() => {})
}
</script>

<style scoped>
.skills-container {
  padding: 20px;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  font-size: 28px;
  color: #409eff;
}

.header-title h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.filter-card {
  margin-bottom: 20px;
}

.table-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.step-content {
  margin-top: 30px;
  min-height: 400px;
}

.skill-type-selection {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  padding: 20px 0;
}

.type-card {
  cursor: pointer;
  text-align: center;
  padding: 30px 20px;
  transition: all 0.3s;
  border: 2px solid transparent;
}

.type-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.2);
}

.type-card.active {
  border-color: #409eff;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.05) 0%, rgba(64, 158, 255, 0.1) 100%);
}

.type-icon {
  font-size: 48px;
  color: #409eff;
  margin-bottom: 16px;
}

.type-card h3 {
  margin: 12px 0 8px;
  font-size: 18px;
  font-weight: 600;
}

.type-card p {
  color: #666;
  font-size: 14px;
  margin: 0;
}

.skill-config {
  padding: 20px 0;
}

.skill-confirm {
  padding: 20px 0;
}

.script-preview {
  max-height: 300px;
  overflow-y: auto;
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 13px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.steps-editor-container {
  width: 100%;
}

.editor-header {
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.editor-footer {
  margin-top: 8px;
  display: flex;
  gap: 8px;
}
</style>
