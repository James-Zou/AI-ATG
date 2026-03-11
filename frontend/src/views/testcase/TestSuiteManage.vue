<template>
  <div class="testsuite-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>测试套件管理</span>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            创建套件
          </el-button>
        </div>
      </template>
      
      <!-- 套件列表 -->
      <el-table
        :data="suiteList"
        stripe
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="套件名称" min-width="200" />
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column prop="caseCount" label="用例数量" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdByName" label="创建人" width="120" />
        <el-table-column prop="createdTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleManageCases(scope.row)">
              管理用例
            </el-button>
            <el-button size="small" @click="handleEdit(scope.row)">
              编辑
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
      
      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
        class="pagination"
      />
    </el-card>
    
    <!-- 用例顺序管理对话框 -->
    <el-dialog
      v-model="caseOrderDialogVisible"
      width="800px"
      title="管理用例执行顺序"
    >
      <div v-loading="caseLoading">
        <el-alert
          title="提示"
          type="info"
          :closable="false"
          style="margin-bottom: 15px;"
        >
          可以通过拖拽或上下移动按钮调整用例执行顺序
        </el-alert>
        
        <el-table
          :data="caseList"
          row-key="caseId"
          style="width: 100%"
          border
        >
          <el-table-column prop="executeOrder" label="执行顺序" width="100" align="center">
            <template #default="scope">
              <el-tag type="primary">{{ scope.row.executeOrder }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="caseTitle" label="用例标题" min-width="200" />
          <el-table-column prop="caseType" label="类型" width="100">
            <template #default="scope">
              <el-tag v-if="scope.row.caseType === 'ui'">UI测试</el-tag>
              <el-tag v-else-if="scope.row.caseType === 'api'" type="success">接口测试</el-tag>
              <el-tag v-else-if="scope.row.caseType === 'performance'" type="warning">性能测试</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="casePriority" label="优先级" width="80" />
          <el-table-column label="操作" width="150" align="center">
            <template #default="scope">
              <el-button-group>
                <el-button
                  size="small"
                  :disabled="scope.$index === 0"
                  @click="moveUp(scope.$index)"
                >
                  <el-icon><ArrowUp /></el-icon>
                </el-button>
                <el-button
                  size="small"
                  :disabled="scope.$index === caseList.length - 1"
                  @click="moveDown(scope.$index)"
                >
                  <el-icon><ArrowDown /></el-icon>
                </el-button>
              </el-button-group>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <template #footer>
        <el-button @click="caseOrderDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveOrder" :loading="saving">
          保存顺序
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      width="600px"
    >
      <template #header>
        <div class="dialog-header">
          <span>{{ isEdit ? '编辑套件' : '创建套件' }}</span>
          <el-tooltip
            content="点击查看测试套件使用说明"
            placement="top"
          >
            <el-link
              type="primary"
              :underline="false"
              @click="showHelp"
              class="help-link"
            >
              <el-icon><QuestionFilled /></el-icon>
              什么是测试套件？
            </el-link>
          </el-tooltip>
        </div>
      </template>
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="套件名称" prop="name">
          <el-input
            v-model="formData.name"
            placeholder="请输入套件名称"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="4"
            placeholder="请输入描述"
          />
        </el-form-item>
        
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import {
  getTestSuiteList,
  createTestSuite,
  updateTestSuite,
  deleteTestSuite,
  getSuiteCases,
  updateCaseOrder
} from '@/api/testsuite'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, QuestionFilled, ArrowUp, ArrowDown } from '@element-plus/icons-vue'

const loading = ref(false)
const suiteList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const editId = ref(null)

const formData = reactive({
  projectId: 1,
  name: '',
  description: '',
  status: 1
})

const formRules = {
  name: [
    { required: true, message: '请输入套件名称', trigger: 'blur' }
  ]
}

// 用例顺序管理
const caseOrderDialogVisible = ref(false)
const caseList = ref([])
const caseLoading = ref(false)
const saving = ref(false)
const currentSuiteId = ref(null)

const loadData = async () => {
  try {
    loading.value = true
    const res = await getTestSuiteList({
      projectId: 1,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    suiteList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  isEdit.value = false
  editId.value = null
  Object.assign(formData, {
    projectId: 1,
    name: '',
    description: '',
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  editId.value = row.id
  Object.assign(formData, {
    projectId: row.projectId,
    name: row.name,
    description: row.description,
    status: row.status
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    
    if (isEdit.value) {
      await updateTestSuite(editId.value, formData)
      ElMessage.success('更新成功')
    } else {
      await createTestSuite(formData)
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    loadData()
  } catch (error) {
    if (error) {
      ElMessage.error(error.message || '提交失败')
    }
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除套件"${row.name}"吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteTestSuite(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const showHelp = () => {
  ElMessageBox.alert(
    `
      <div style="line-height: 1.8;">
        <h3 style="margin-top: 0;">📦 什么是测试套件？</h3>
        <p><strong>测试套件（Test Suite）</strong> 是用来管理和组织测试用例的容器，它可以：</p>
        <ul style="padding-left: 20px;">
          <li><strong>批量管理</strong> - 将相关的测试用例归类到一个套件中</li>
          <li><strong>组织资产</strong> - 按功能模块、测试类型等维度组织</li>
          <li><strong>便于执行</strong> - 一次性执行整个套件中的所有测试用例</li>
          <li><strong>提高复用</strong> - 同一套件可以在不同场景下重复使用</li>
        </ul>
        <h3>🚀 如何使用？</h3>
        <ol style="padding-left: 20px;">
          <li><strong>创建套件</strong> - 填写套件名称和描述，设置启用状态</li>
          <li><strong>添加用例</strong> - 在测试用例管理页面，将用例关联到套件</li>
          <li><strong>执行测试</strong> - 选择套件后可批量执行其中的所有用例</li>
          <li><strong>查看报告</strong> - 执行完成后查看整个套件的测试报告</li>
        </ol>
        <p style="margin-bottom: 0; color: #909399;">更多详情请查看 <strong>用户手册</strong></p>
      </div>
    `,
    '测试套件使用说明',
    {
      confirmButtonText: '我知道了',
      dangerouslyUseHTMLString: true,
      customClass: 'help-message-box'
    }
  )
}

// 管理用例执行顺序
const handleManageCases = async (row) => {
  currentSuiteId.value = row.id
  caseOrderDialogVisible.value = true
  await loadCaseList()
}

const loadCaseList = async () => {
  try {
    caseLoading.value = true
    const res = await getSuiteCases(currentSuiteId.value)
    caseList.value = res.data || []
  } catch (error) {
    ElMessage.error(error.message || '加载用例列表失败')
  } finally {
    caseLoading.value = false
  }
}

const moveUp = (index) => {
  if (index > 0) {
    const temp = caseList.value[index]
    caseList.value[index] = caseList.value[index - 1]
    caseList.value[index - 1] = temp
    updateOrderNumbers()
  }
}

const moveDown = (index) => {
  if (index < caseList.value.length - 1) {
    const temp = caseList.value[index]
    caseList.value[index] = caseList.value[index + 1]
    caseList.value[index + 1] = temp
    updateOrderNumbers()
  }
}

const updateOrderNumbers = () => {
  caseList.value.forEach((item, index) => {
    item.executeOrder = index + 1
  })
}

const handleSaveOrder = async () => {
  try {
    saving.value = true
    const caseIds = caseList.value.map(item => item.caseId)
    await updateCaseOrder({
      suiteId: currentSuiteId.value,
      caseIds
    })
    ElMessage.success('保存顺序成功')
    caseOrderDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '保存顺序失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.testsuite-manage {
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

.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 18px;
  font-weight: 500;
}

.help-link {
  font-size: 14px;
  font-weight: normal;
  display: flex;
  align-items: center;
  gap: 4px;
}

:deep(.help-message-box) {
  width: 600px;
}

:deep(.help-message-box h3) {
  color: #409eff;
  margin: 10px 0;
}

:deep(.help-message-box ul),
:deep(.help-message-box ol) {
  margin: 10px 0;
}

:deep(.help-message-box li) {
  margin: 5px 0;
}
</style>
