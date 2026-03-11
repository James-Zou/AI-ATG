<template>
  <div class="environment-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>测试环境管理</span>
          <el-button type="primary" @click="showDialog()">
            <el-icon><Plus /></el-icon>
            新增环境
          </el-button>
        </div>
      </template>
      
      <el-table :data="environmentList" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="envName" label="环境名称" min-width="120" />
        <el-table-column prop="envCode" label="环境编码" width="120" />
        <el-table-column prop="baseUrl" label="基础URL" min-width="200" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" min-width="150" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="showDialog(scope.row)">
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
      
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadData"
        class="pagination"
      />
    </el-card>
    
    <!-- 环境对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑环境' : '新增环境'"
      width="600px"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="环境名称" prop="envName">
          <el-input v-model="formData.envName" placeholder="例如：测试环境" />
        </el-form-item>
        
        <el-form-item label="环境编码" prop="envCode">
          <el-input 
            v-model="formData.envCode" 
            placeholder="例如：test" 
            :disabled="isEdit"
          />
          <div class="form-tip">编码唯一，创建后不可修改</div>
        </el-form-item>
        
        <el-form-item label="基础URL">
          <el-input v-model="formData.baseUrl" placeholder="例如：http://test.example.com" />
        </el-form-item>
        
        <el-form-item label="描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入环境描述"
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
        <el-button
          type="primary"
          @click="handleSubmit"
          :loading="submitting"
        >
          {{ submitting ? '提交中...' : '确定' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { createEnvironment, updateEnvironment, deleteEnvironment, getEnvironmentList } from '@/api/environment'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const loading = ref(false)
const submitting = ref(false)
const environmentList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const dialogVisible = ref(false)
const isEdit = ref(false)

const formRef = ref()
const formData = reactive({
  projectId: 1,
  envName: '',
  envCode: '',
  baseUrl: '',
  description: '',
  status: 1
})

const formRules = {
  envName: [
    { required: true, message: '请输入环境名称', trigger: 'blur' }
  ],
  envCode: [
    { required: true, message: '请输入环境编码', trigger: 'blur' },
    { pattern: /^[a-z0-9_-]+$/, message: '编码只能包含小写字母、数字、下划线和短横线', trigger: 'blur' }
  ]
}

const loadData = async () => {
  try {
    loading.value = true
    const res = await getEnvironmentList({
      projectId: 1,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    environmentList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const showDialog = (row = null) => {
  isEdit.value = !!row
  
  if (row) {
    Object.assign(formData, row)
  } else {
    Object.assign(formData, {
      projectId: 1,
      envName: '',
      envCode: '',
      baseUrl: '',
      description: '',
      status: 1
    })
  }
  
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    
    if (isEdit.value) {
      await updateEnvironment(formData.id, formData)
      ElMessage.success('更新成功')
    } else {
      await createEnvironment(formData)
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    loadData()
    
  } catch (error) {
    if (error) {
      ElMessage.error(error.message || '操作失败')
    }
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该环境吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteEnvironment(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.environment-list {
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

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
