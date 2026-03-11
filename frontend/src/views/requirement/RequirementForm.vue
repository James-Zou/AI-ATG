<template>
  <div class="requirement-form">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑需求' : '创建需求' }}</span>
          <el-button @click="handleBack">返回</el-button>
        </div>
      </template>
      
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="需求标题" prop="title">
          <el-input
            v-model="formData.title"
            placeholder="请输入需求标题"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="需求内容" prop="content">
          <el-input
            v-model="formData.content"
            type="textarea"
            :rows="8"
            placeholder="请输入需求内容（支持Markdown格式）"
          />
        </el-form-item>
        
        <el-form-item label="需求类型" prop="type">
          <el-select v-model="formData.type" placeholder="请选择需求类型">
            <el-option label="用户故事" value="user_story" />
            <el-option label="功能需求" value="feature" />
            <el-option label="Bug修复" value="bug_fix" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="formData.priority" placeholder="请选择优先级">
            <el-option label="P0 - 紧急" value="P0" />
            <el-option label="P1 - 重要" value="P1" />
            <el-option label="P2 - 普通" value="P2" />
            <el-option label="P3 - 低" value="P3" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="需求来源" prop="source">
          <el-select v-model="formData.source" placeholder="请选择需求来源">
            <el-option label="手工录入" value="manual" />
            <el-option label="GitLab" value="gitlab" />
            <el-option label="JIRA" value="jira" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="附件上传">
          <el-upload
            :action="uploadUrl"
            :headers="uploadHeaders"
            :on-success="handleUploadSuccess"
            :on-remove="handleRemove"
            :file-list="fileList"
            :limit="5"
            :on-exceed="handleExceed"
          >
            <el-button type="primary">
              <el-icon><Upload /></el-icon>
              点击上传
            </el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持上传文档、图片等文件，单个文件不超过10MB，最多5个文件
              </div>
            </template>
          </el-upload>
        </el-form-item>
        
        <el-form-item label="需求状态" prop="status">
          <el-select v-model="formData.status" placeholder="请选择状态">
            <el-option label="草稿" value="draft" />
            <el-option label="评审中" value="reviewing" />
            <el-option label="已通过" value="approved" />
            <el-option label="测试中" value="testing" />
          </el-select>
        </el-form-item>
        
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
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { createRequirement, updateRequirement, getRequirementDetail } from '@/api/requirement'
import { ElMessage } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const formRef = ref()
const submitting = ref(false)
const fileList = ref([])

const isEdit = computed(() => !!route.params.id)

const uploadUrl = '/api/file/upload?folder=requirements'
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token')}`
}))

// 临时使用固定的项目ID，实际应该从路由或状态管理中获取
const formData = reactive({
  projectId: 1,
  title: '',
  content: '',
  type: '',
  priority: '',
  source: 'manual',
  attachmentUrls: [],
  status: 'draft'
})

const formRules = {
  title: [
    { required: true, message: '请输入需求标题', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择需求类型', trigger: 'change' }
  ],
  priority: [
    { required: true, message: '请选择优先级', trigger: 'change' }
  ]
}

const loadData = async () => {
  if (!isEdit.value) return
  
  try {
    const res = await getRequirementDetail(route.params.id)
    Object.assign(formData, res.data)
    
    // 处理文件列表
    if (res.data.attachmentUrls && res.data.attachmentUrls.length > 0) {
      fileList.value = res.data.attachmentUrls.map((url, index) => ({
        name: `附件${index + 1}`,
        url: url
      }))
    }
  } catch (error) {
    ElMessage.error('加载需求失败')
    handleBack()
  }
}

const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    formData.attachmentUrls.push(response.data)
    ElMessage.success('上传成功')
  } else {
    ElMessage.error('上传失败')
  }
}

const handleRemove = (file) => {
  if (file.url) {
    const index = formData.attachmentUrls.indexOf(file.url)
    if (index > -1) {
      formData.attachmentUrls.splice(index, 1)
    }
  } else if (file.response && file.response.data) {
    const index = formData.attachmentUrls.indexOf(file.response.data)
    if (index > -1) {
      formData.attachmentUrls.splice(index, 1)
    }
  }
}

const handleExceed = () => {
  ElMessage.warning('最多只能上传5个文件')
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    
    if (isEdit.value) {
      await updateRequirement(route.params.id, formData)
      ElMessage.success('更新成功')
    } else {
      await createRequirement(formData)
      ElMessage.success('创建成功')
    }
    
    handleBack()
  } catch (error) {
    if (error) {
      ElMessage.error(error.message || '提交失败')
    }
  } finally {
    submitting.value = false
  }
}

const handleBack = () => {
  router.push('/requirement/list')
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.requirement-form {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
