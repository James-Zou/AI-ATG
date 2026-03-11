<template>
  <div class="gitlab-config">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>GitLab 集成配置</span>
          <el-button 
            v-if="!config"
            type="primary" 
            @click="showConfigDialog"
          >
            <el-icon><Plus /></el-icon>
            配置GitLab
          </el-button>
        </div>
      </template>
      
      <!-- 配置信息 -->
      <div v-if="config" v-loading="loading">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="GitLab地址">
            {{ config.gitlabUrl }}
          </el-descriptions-item>
          
          <el-descriptions-item label="仓库地址">
            {{ config.repositoryUrl }}
          </el-descriptions-item>
          
          <el-descriptions-item label="默认分支">
            {{ config.defaultBranch }}
          </el-descriptions-item>
          
          <el-descriptions-item label="自动触发">
            <el-tag :type="config.autoTrigger === 1 ? 'success' : 'info'">
              {{ config.autoTrigger === 1 ? '已启用' : '已禁用' }}
            </el-tag>
          </el-descriptions-item>
          
          <el-descriptions-item label="状态">
            <el-tag :type="config.status === 1 ? 'success' : 'danger'">
              {{ config.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          
          <el-descriptions-item label="创建时间">
            {{ config.createdTime }}
          </el-descriptions-item>
        </el-descriptions>
        
        <el-divider content-position="left">Webhook 配置</el-divider>
        
        <el-alert
          title="请在 GitLab 仓库设置中配置 Webhook"
          type="info"
          :closable="false"
        >
          <template #default>
            <p><strong>Webhook URL:</strong></p>
            <el-input
              v-model="config.webhookUrl"
              readonly
              style="margin: 10px 0;"
            >
              <template #append>
                <el-button @click="copyWebhookUrl">复制</el-button>
              </template>
            </el-input>
            
            <p><strong>Secret Token:</strong></p>
            <el-input
              v-model="config.webhookSecret"
              readonly
              type="password"
              show-password
              style="margin: 10px 0;"
            >
              <template #append>
                <el-button @click="copySecret">复制</el-button>
              </template>
            </el-input>
            
            <p style="margin-top: 10px;">
              <strong>触发事件:</strong> Push events
            </p>
          </template>
        </el-alert>
        
        <div style="margin-top: 20px;">
          <el-button type="primary" @click="editConfig">编辑配置</el-button>
          <el-button type="danger" @click="handleDelete">删除配置</el-button>
          <el-button @click="viewWebhookRecords">查看Webhook记录</el-button>
        </div>
      </div>
      
      <!-- 未配置提示 -->
      <el-empty v-else description="未配置 GitLab 集成" />
    </el-card>
    
    <!-- 配置对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑GitLab配置' : '新建GitLab配置'"
      width="600px"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="GitLab地址" prop="gitlabUrl">
          <el-input
            v-model="formData.gitlabUrl"
            placeholder="https://gitlab.com"
          />
        </el-form-item>
        
        <el-form-item label="Access Token" prop="gitlabToken">
          <el-input
            v-model="formData.gitlabToken"
            type="password"
            show-password
            placeholder="输入GitLab Access Token"
          />
          <span class="form-tip">需要有读取仓库权限</span>
        </el-form-item>
        
        <el-form-item label="仓库地址" prop="repositoryUrl">
          <el-input
            v-model="formData.repositoryUrl"
            placeholder="https://gitlab.com/user/repo.git"
          />
        </el-form-item>
        
        <el-form-item label="默认分支">
          <el-input
            v-model="formData.defaultBranch"
            placeholder="main"
          />
        </el-form-item>
        
        <el-form-item label="Webhook密钥">
          <el-input
            v-model="formData.webhookSecret"
            placeholder="用于验证Webhook请求（可选）"
          />
        </el-form-item>
        
        <el-form-item label="自动触发">
          <el-switch
            v-model="formData.autoTrigger"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          />
          <div class="form-tip">代码变更时自动触发测试用例生成</div>
        </el-form-item>
        
        <el-form-item label="状态">
          <el-switch
            v-model="formData.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="handleSubmit"
          :loading="submitting"
        >
          {{ submitting ? '保存中...' : '保存' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  createGitlabConfig,
  updateGitlabConfig,
  getGitlabConfigByProject,
  deleteGitlabConfig
} from '@/api/gitlab'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useClipboard } from '@vueuse/core'

const router = useRouter()

const loading = ref(false)
const config = ref(null)
const dialogVisible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)

const formRef = ref()
const formData = reactive({
  projectId: 1,
  gitlabUrl: '',
  gitlabToken: '',
  repositoryUrl: '',
  defaultBranch: 'main',
  webhookSecret: '',
  autoTrigger: 1,
  status: 1
})

const formRules = {
  gitlabUrl: [
    { required: true, message: '请输入GitLab地址', trigger: 'blur' }
  ],
  gitlabToken: [
    { required: true, message: '请输入Access Token', trigger: 'blur' }
  ],
  repositoryUrl: [
    { required: true, message: '请输入仓库地址', trigger: 'blur' }
  ]
}

const loadConfig = async () => {
  try {
    loading.value = true
    const res = await getGitlabConfigByProject(1)
    config.value = res.data
  } catch (error) {
    // 未配置时会报错，这里忽略
    config.value = null
  } finally {
    loading.value = false
  }
}

const showConfigDialog = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const editConfig = () => {
  isEdit.value = true
  Object.assign(formData, config.value)
  dialogVisible.value = true
}

const resetForm = () => {
  Object.assign(formData, {
    projectId: 1,
    gitlabUrl: '',
    gitlabToken: '',
    repositoryUrl: '',
    defaultBranch: 'main',
    webhookSecret: '',
    autoTrigger: 1,
    status: 1
  })
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    
    if (isEdit.value) {
      await updateGitlabConfig(config.value.id, formData)
      ElMessage.success('配置更新成功')
    } else {
      await createGitlabConfig(formData)
      ElMessage.success('配置创建成功')
    }
    
    dialogVisible.value = false
    loadConfig()
  } catch (error) {
    if (error) {
      ElMessage.error(error.message || '操作失败')
    }
  } finally {
    submitting.value = false
  }
}

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除GitLab配置吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteGitlabConfig(config.value.id)
    ElMessage.success('配置删除成功')
    config.value = null
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const copyWebhookUrl = () => {
  const { copy } = useClipboard()
  copy(config.value.webhookUrl)
  ElMessage.success('Webhook URL已复制到剪贴板')
}

const copySecret = () => {
  const { copy } = useClipboard()
  copy(config.value.webhookSecret || '')
  ElMessage.success('密钥已复制到剪贴板')
}

const viewWebhookRecords = () => {
  router.push('/gitlab/webhook-records')
}

onMounted(() => {
  loadConfig()
})
</script>

<style scoped>
.gitlab-config {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style>
