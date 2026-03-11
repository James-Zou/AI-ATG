<template>
  <div class="interface-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>🔌 接口全集</span>
          <div>
            <el-button @click="showCurlImport">
              <el-icon><Upload /></el-icon>
              导入cURL
            </el-button>
            <el-button type="primary" @click="handleCreate">
              <el-icon><Plus /></el-icon>
              新建接口
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 搜索筛选区域 -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="queryForm.keyword"
            placeholder="搜索接口名称、URL"
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
        
        <el-form-item label="请求方法">
          <el-select v-model="queryForm.method" placeholder="全部" clearable style="width: 120px;">
            <el-option label="GET" value="GET" />
            <el-option label="POST" value="POST" />
            <el-option label="PUT" value="PUT" />
            <el-option label="DELETE" value="DELETE" />
            <el-option label="PATCH" value="PATCH" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 120px;">
            <el-option label="草稿" value="draft" />
            <el-option label="已发布" value="published" />
            <el-option label="已归档" value="archived" />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <!-- 表格 -->
      <el-table
        :data="interfaceList"
        stripe
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="interfaceName" label="接口名称" min-width="200" />
        <el-table-column prop="method" label="方法" width="100">
          <template #default="scope">
            <el-tag :type="getMethodType(scope.row.method)">
              {{ scope.row.method }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="url" label="URL" min-width="300" show-overflow-tooltip />
        <el-table-column prop="projectName" label="所属项目" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdByName" label="创建人" width="100" />
        <el-table-column prop="createdTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleView(scope.row)">
              查看
            </el-button>
            <el-button size="small" type="primary" @click="handleEdit(scope.row)">
              编辑
            </el-button>
            <el-button 
              v-if="scope.row.status === 'draft'" 
              size="small" 
              type="success" 
              @click="handlePublish(scope.row)"
            >
              发布
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
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
    
    <!-- cURL导入对话框 -->
    <el-dialog v-model="curlDialogVisible" title="导入cURL" width="700px">
      <el-form :model="curlForm" label-width="100px">
        <el-form-item label="所属项目" required>
          <el-select 
            v-model="curlForm.projectId" 
            placeholder="请选择项目"
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
        
        <el-form-item label="cURL命令" required>
          <el-input
            v-model="curlForm.curl"
            type="textarea"
            :rows="10"
            placeholder="粘贴cURL命令，例如：&#10;curl 'https://api.example.com/users' \&#10;  -H 'Content-Type: application/json' \&#10;  --data-raw '{&quot;name&quot;:&quot;test&quot;}'"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="curlDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCurlImport" :loading="importing">
          导入
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  getInterfaceList,
  deleteInterface,
  publishInterface,
  importFromCurl
} from '@/api/apiInterface'
import { getProjectList } from '@/api/project'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Upload } from '@element-plus/icons-vue'

const router = useRouter()

const loading = ref(false)
const importing = ref(false)
const interfaceList = ref([])
const total = ref(0)
const projectList = ref([])
const curlDialogVisible = ref(false)

const queryForm = reactive({
  keyword: '',
  projectId: null,
  method: '',
  status: '',
  pageNum: 1,
  pageSize: 10
})

const curlForm = reactive({
  projectId: null,
  curl: ''
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
    const res = await getInterfaceList(queryForm)
    interfaceList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryForm.pageNum = 1
  loadData()
}

const handleReset = () => {
  Object.assign(queryForm, {
    keyword: '',
    projectId: null,
    method: '',
    status: '',
    pageNum: 1,
    pageSize: 10
  })
  loadData()
}

const handleCreate = () => {
  router.push('/interface/create')
}

const handleView = (row) => {
  router.push(`/interface/view/${row.id}`)
}

const handleEdit = (row) => {
  router.push(`/interface/edit/${row.id}`)
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该接口吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteInterface(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handlePublish = async (row) => {
  try {
    await ElMessageBox.confirm(
      '发布后的接口可以在测试用例中直接导入使用，确定要发布吗？', 
      '发布确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    await publishInterface(row.id)
    ElMessage.success('发布成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('发布失败')
    }
  }
}

const showCurlImport = () => {
  curlForm.projectId = null
  curlForm.curl = ''
  curlDialogVisible.value = true
}

const handleCurlImport = async () => {
  if (!curlForm.projectId) {
    ElMessage.warning('请选择所属项目')
    return
  }
  
  if (!curlForm.curl || curlForm.curl.trim() === '') {
    ElMessage.warning('请输入cURL命令')
    return
  }
  
  try {
    importing.value = true
    const res = await importFromCurl(curlForm.curl, curlForm.projectId)
    ElMessage.success('导入成功')
    curlDialogVisible.value = false
    
    // 跳转到编辑页面
    router.push(`/interface/edit/${res.data.id}`)
  } catch (error) {
    ElMessage.error(error.message || '导入失败')
  } finally {
    importing.value = false
  }
}

const getMethodType = (method) => {
  const types = {
    GET: 'success',
    POST: 'primary',
    PUT: 'warning',
    DELETE: 'danger',
    PATCH: 'info'
  }
  return types[method] || 'info'
}

const getStatusLabel = (status) => {
  const labels = {
    draft: '草稿',
    published: '已发布',
    archived: '已归档'
  }
  return labels[status] || status
}

const getStatusType = (status) => {
  const types = {
    draft: 'info',
    published: 'success',
    archived: 'warning'
  }
  return types[status] || 'info'
}

onMounted(() => {
  loadProjects()
  loadData()
})
</script>

<style scoped>
.interface-list {
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

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>
