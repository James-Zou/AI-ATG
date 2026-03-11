<template>
  <div class="ai-config">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>AI 配置管理</span>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            新增配置
          </el-button>
        </div>
      </template>
      
      <el-table :data="configList" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="provider" label="提供商" width="120">
          <template #default="scope">
            <el-tag>{{ getProviderLabel(scope.row.provider) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="modelName" label="模型名称" width="150" />
        <el-table-column prop="apiUrl" label="API地址" min-width="200" />
        <el-table-column prop="maxTokens" label="最大Token" width="100" />
        <el-table-column prop="temperature" label="温度" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isDefault" label="默认" width="80">
          <template #default="scope">
            <el-icon v-if="scope.row.isDefault === 1" color="#67C23A">
              <Check />
            </el-icon>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
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
    </el-card>
    
    <!-- 配置对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑配置' : '新增配置'"
      width="600px"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="提供商" prop="provider">
          <el-select v-model="formData.provider" placeholder="请选择">
            <el-option label="DeepSeek" value="deepseek" />
            <el-option label="阿里千问" value="qwen" />
            <el-option label="智谱AI" value="zhipu" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="模型名称" prop="modelName">
          <el-input v-model="formData.modelName" placeholder="如: deepseek-chat" />
        </el-form-item>
        
        <el-form-item label="API Key" prop="apiKey">
          <el-input
            v-model="formData.apiKey"
            :type="editId ? 'text' : 'password'"
            :placeholder="editId ? '不修改请留空，修改请输入新的API Key' : '请输入API Key'"
            :show-password="!editId"
            @input="apiKeyModified = true"
          >
            <template v-if="editId && !apiKeyModified" #suffix>
              <el-tag type="info" size="small">已加密存储</el-tag>
            </template>
          </el-input>
          <div v-if="editId" style="margin-top: 4px; font-size: 12px; color: #909399;">
            提示：API Key 已加密存储，编辑时若不修改可留空
          </div>
        </el-form-item>
        
        <el-form-item label="API地址">
          <el-input v-model="formData.apiUrl" placeholder="留空使用默认地址" />
        </el-form-item>
        
        <el-form-item label="最大Token">
          <el-input-number v-model="formData.maxTokens" :min="100" :max="10000" />
        </el-form-item>
        
        <el-form-item label="温度参数">
          <el-input-number
            v-model="formData.temperature"
            :min="0"
            :max="1"
            :step="0.1"
          />
        </el-form-item>
        
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="设为默认">
          <el-switch v-model="formData.isDefault" :active-value="1" :inactive-value="0" />
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
  getAiConfigList,
  createAiConfig,
  updateAiConfig,
  deleteAiConfig
} from '@/api/ai'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Check } from '@element-plus/icons-vue'
import { encryptApiKey, maskApiKey } from '@/utils/crypto'

const loading = ref(false)
const submitting = ref(false)
const configList = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)

const formRef = ref()
const formData = reactive({
  provider: '',
  modelName: '',
  apiKey: '',
  apiUrl: '',
  maxTokens: 4000,
  temperature: 0.7,
  status: 1,
  isDefault: 0
})

// 用于跟踪 API Key 是否被修改
const apiKeyModified = ref(false)
// 存储原始的脱敏后的 API Key（用于显示）
const originalMaskedApiKey = ref('')

const formRules = {
  provider: [{ required: true, message: '请选择提供商', trigger: 'change' }],
  modelName: [{ required: true, message: '请输入模型名称', trigger: 'blur' }],
  apiKey: [
    { 
      required: (rule, value, callback) => {
        // 新建时必填
        if (!isEdit.value) {
          if (!value) {
            callback(new Error('请输入API Key'))
          } else {
            callback()
          }
        } else {
          // 编辑时可选
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ]
}

const loadData = async () => {
  try {
    loading.value = true
    const res = await getAiConfigList()
    configList.value = res.data
  } catch (error) {
    ElMessage.error('加载配置失败')
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  isEdit.value = false
  editId.value = null
  apiKeyModified.value = false
  originalMaskedApiKey.value = ''
  Object.assign(formData, {
    provider: '',
    modelName: '',
    apiKey: '',
    apiUrl: '',
    maxTokens: 4000,
    temperature: 0.7,
    status: 1,
    isDefault: 0
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  editId.value = row.id
  Object.assign(formData, row)
  
  // 脱敏显示 API Key
  if (row.apiKey) {
    originalMaskedApiKey.value = maskApiKey(row.apiKey)
    formData.apiKey = originalMaskedApiKey.value
  } else {
    originalMaskedApiKey.value = ''
    formData.apiKey = ''
  }
  
  apiKeyModified.value = false
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    
    // 准备提交的数据
    const submitData = { ...formData }
    
    // 加密 API Key
    if (isEdit.value) {
      // 编辑模式：只有当 API Key 被修改时才加密并提交
      if (apiKeyModified.value && formData.apiKey !== originalMaskedApiKey.value) {
        submitData.apiKey = encryptApiKey(formData.apiKey)
      } else {
        // 未修改则不提交 API Key 字段，后端保持原值
        delete submitData.apiKey
      }
    } else {
      // 新建模式：加密 API Key
      if (formData.apiKey) {
        submitData.apiKey = encryptApiKey(formData.apiKey)
      }
    }
    
    if (isEdit.value) {
      await updateAiConfig(editId.value, submitData)
      ElMessage.success('更新成功')
    } else {
      await createAiConfig(submitData)
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
    await ElMessageBox.confirm(`确定要删除该配置吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteAiConfig(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
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

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.ai-config {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
