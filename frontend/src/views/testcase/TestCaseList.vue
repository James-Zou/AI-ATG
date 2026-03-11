<template>
  <div class="testcase-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>测试用例管理</span>
          <div>
            <el-button type="success" @click="handleImport">
              <el-icon><Upload /></el-icon>
              导入用例
            </el-button>
            <el-button type="warning" @click="handleExport">
              <el-icon><Download /></el-icon>
              导出用例
            </el-button>
            <el-button type="primary" @click="handleCreate">
              <el-icon><Plus /></el-icon>
              创建用例
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 搜索筛选区域 -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="queryForm.keyword"
            placeholder="搜索标题、描述或用例编号"
            clearable
            style="width: 220px;"
            @clear="handleSearch"
          />
        </el-form-item>
        
        <el-form-item label="所属项目">
          <el-select 
            v-model="queryForm.projectId" 
            placeholder="全部项目" 
            clearable
            filterable
            style="width: 180px;"
          >
            <el-option
              v-for="project in projectList"
              :key="project.id"
              :label="project.name"
              :value="project.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="类型">
          <el-select v-model="queryForm.type" placeholder="全部" clearable style="width: 140px;">
            <el-option label="功能测试" value="functional" />
            <el-option label="界面测试" value="ui" />
            <el-option label="接口测试" value="api" />
            <el-option label="性能测试" value="performance" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="优先级">
          <el-select v-model="queryForm.priority" placeholder="全部" clearable style="width: 120px;">
            <el-option label="P0" value="P0" />
            <el-option label="P1" value="P1" />
            <el-option label="P2" value="P2" />
            <el-option label="P3" value="P3" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 120px;">
            <el-option label="草稿" value="draft" />
            <el-option label="待审核" value="pending" />
            <el-option label="已通过" value="approved" />
            <el-option label="执行中" value="running" />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <!-- 表格 -->
      <el-table
        :data="testCaseList"
        stripe
        v-loading="loading"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="caseNo" label="用例编号" width="150" />
        <el-table-column prop="title" label="用例标题" min-width="200" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="scope">
            <el-tag>{{ getTypeLabel(scope.row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="90">
          <template #default="scope">
            <el-tag :type="getPriorityType(scope.row.priority)">
              {{ scope.row.priority }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="projectName" label="所属项目" width="150" />
        <el-table-column prop="suiteName" label="所属套件" width="150" />
        <el-table-column prop="createdByName" label="创建人" width="100" />
        <el-table-column prop="createdTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleView(scope.row)">
              查看
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
      
      <!-- 批量操作 -->
      <div v-if="selectedIds.length > 0" class="batch-actions">
        <el-button type="danger" @click="handleBatchDelete">
          批量删除（已选{{ selectedIds.length }}项）
        </el-button>
      </div>
      
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
    
    <!-- 导入对话框 -->
    <el-dialog v-model="importDialogVisible" title="导入测试用例" width="500px">
      <el-upload
        drag
        :auto-upload="false"
        :on-change="handleFileChange"
        :limit="1"
        accept=".xlsx,.xls"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            只能上传 Excel 文件，且不超过 10MB
          </div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleImportConfirm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  getTestCaseList,
  deleteTestCase,
  batchDeleteTestCase,
  importTestCases,
  exportTestCases
} from '@/api/testcase'
import { getProjectList } from '@/api/project'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Upload, Download, UploadFilled } from '@element-plus/icons-vue'

const router = useRouter()

const loading = ref(false)
const testCaseList = ref([])
const total = ref(0)
const selectedIds = ref([])
const importDialogVisible = ref(false)
const importFile = ref(null)
const projectList = ref([])

const queryForm = ref({
  keyword: '',
  projectId: null,
  type: '',
  priority: '',
  status: '',
  pageNum: 1,
  pageSize: 10
})

const loadProjects = async () => {
  try {
    const res = await getProjectList({ pageNum: 1, pageSize: 100 })
    projectList.value = res.data.records
  } catch (error) {
    console.error('加载项目列表失败', error)
  }
}

const loadData = async () => {
  try {
    loading.value = true
    const res = await getTestCaseList(queryForm.value)
    testCaseList.value = res.data.records
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
    projectId: null,
    type: '',
    priority: '',
    status: '',
    pageNum: 1,
    pageSize: 10
  }
  loadData()
}

const handleCreate = () => {
  router.push('/testcase/create')
}

const handleView = (row) => {
  router.push(`/testcase/detail/${row.id}`)
}

const handleEdit = (row) => {
  router.push(`/testcase/edit/${row.id}`)
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除测试用例"${row.title}"吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteTestCase(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedIds.value.length} 个测试用例吗？`,
      '批量删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await batchDeleteTestCase(selectedIds.value)
    ElMessage.success('批量删除成功')
    selectedIds.value = []
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

const handleImport = () => {
  importDialogVisible.value = true
}

const handleFileChange = (file) => {
  importFile.value = file.raw
}

const handleImportConfirm = async () => {
  if (!importFile.value) {
    ElMessage.warning('请选择文件')
    return
  }
  
  try {
    await importTestCases(importFile.value, 1)
    ElMessage.success('导入成功')
    importDialogVisible.value = false
    importFile.value = null
    loadData()
  } catch (error) {
    ElMessage.error('导入失败')
  }
}

const handleExport = async () => {
  try {
    const blob = await exportTestCases(queryForm.value)
    const url = window.URL.createObjectURL(new Blob([blob]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `测试用例_${Date.now()}.xlsx`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
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

onMounted(() => {
  loadProjects()
  loadData()
})
</script>

<style scoped>
.testcase-list {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.batch-actions {
  margin-top: 20px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>
