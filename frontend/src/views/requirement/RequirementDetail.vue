<template>
  <div class="requirement-detail">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>需求详情</span>
          <div>
            <el-button @click="handleEdit">编辑</el-button>
            <el-button @click="handleBack">返回</el-button>
          </div>
        </div>
      </template>
      
      <el-descriptions :column="2" border>
        <el-descriptions-item label="需求ID">
          {{ detail.id }}
        </el-descriptions-item>
        
        <el-descriptions-item label="项目名称">
          {{ detail.projectName || '-' }}
        </el-descriptions-item>
        
        <el-descriptions-item label="需求标题" :span="2">
          {{ detail.title }}
        </el-descriptions-item>
        
        <el-descriptions-item label="需求类型">
          <el-tag>{{ getTypeLabel(detail.type) }}</el-tag>
        </el-descriptions-item>
        
        <el-descriptions-item label="优先级">
          <el-tag :type="getPriorityType(detail.priority)">
            {{ detail.priority }}
          </el-tag>
        </el-descriptions-item>
        
        <el-descriptions-item label="需求状态">
          <el-tag :type="getStatusType(detail.status)">
            {{ getStatusLabel(detail.status) }}
          </el-tag>
        </el-descriptions-item>
        
        <el-descriptions-item label="需求来源">
          {{ getSourceLabel(detail.source) }}
        </el-descriptions-item>
        
        <el-descriptions-item label="创建人">
          {{ detail.createdByName || '-' }}
        </el-descriptions-item>
        
        <el-descriptions-item label="创建时间">
          {{ detail.createdTime }}
        </el-descriptions-item>
        
        <el-descriptions-item label="需求内容" :span="2">
          <div class="content-box">
            {{ detail.content || '暂无内容' }}
          </div>
        </el-descriptions-item>
        
        <el-descriptions-item label="附件" :span="2">
          <div v-if="detail.attachmentUrls && detail.attachmentUrls.length > 0">
            <el-link
              v-for="(url, index) in detail.attachmentUrls"
              :key="index"
              :href="url"
              target="_blank"
              type="primary"
              style="display: block; margin-bottom: 5px;"
            >
              <el-icon><Document /></el-icon>
              附件 {{ index + 1 }}
            </el-link>
          </div>
          <span v-else>暂无附件</span>
        </el-descriptions-item>
      </el-descriptions>
      
      <!-- 状态操作按钮 -->
      <div class="action-buttons">
        <el-button
          v-if="detail.status === 'draft'"
          type="primary"
          @click="handleUpdateStatus('reviewing')"
        >
          提交评审
        </el-button>
        <el-button
          v-if="detail.status === 'reviewing'"
          type="success"
          @click="handleUpdateStatus('approved')"
        >
          评审通过
        </el-button>
        <el-button
          v-if="detail.status === 'approved'"
          type="warning"
          @click="handleUpdateStatus('testing')"
        >
          开始测试
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getRequirementDetail, updateRequirementStatus } from '@/api/requirement'
import { ElMessage } from 'element-plus'
import { Document } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const detail = ref({})

const loadData = async () => {
  try {
    loading.value = true
    const res = await getRequirementDetail(route.params.id)
    detail.value = res.data
  } catch (error) {
    ElMessage.error('加载失败')
    handleBack()
  } finally {
    loading.value = false
  }
}

const handleEdit = () => {
  router.push(`/requirement/edit/${route.params.id}`)
}

const handleBack = () => {
  router.push('/requirement/list')
}

const handleUpdateStatus = async (status) => {
  try {
    await updateRequirementStatus(route.params.id, status)
    ElMessage.success('状态更新成功')
    loadData()
  } catch (error) {
    ElMessage.error('状态更新失败')
  }
}

const getTypeLabel = (type) => {
  const labels = {
    user_story: '用户故事',
    feature: '功能需求',
    bug_fix: 'Bug修复'
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
    reviewing: '评审中',
    approved: '已通过',
    testing: '测试中'
  }
  return labels[status] || status
}

const getStatusType = (status) => {
  const types = {
    draft: 'info',
    reviewing: 'warning',
    approved: 'success',
    testing: 'primary'
  }
  return types[status] || 'info'
}

const getSourceLabel = (source) => {
  const labels = {
    manual: '手工录入',
    gitlab: 'GitLab',
    jira: 'JIRA'
  }
  return labels[source] || source
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.requirement-detail {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.content-box {
  white-space: pre-wrap;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  min-height: 100px;
}

.action-buttons {
  margin-top: 20px;
  text-align: center;
}
</style>
