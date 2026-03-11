<template>
  <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="handleRefresh">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>
      
      <el-table
        :data="userList"
        stripe
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="nickname" label="昵称" width="150" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="scope">
            <el-tag :type="getRoleType(scope.row.role)">
              {{ getRoleLabel(scope.row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button
              size="small"
              @click="handleView(scope.row)"
            >
              查看
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
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getUserList, deleteUser } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'

const userList = ref([])
const loading = ref(false)

const loadUserList = async () => {
  try {
    loading.value = true
    const res = await getUserList()
    userList.value = res.data
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleRefresh = () => {
  loadUserList()
}

const handleView = (row) => {
  ElMessageBox.alert(
    `<p><strong>用户名：</strong>${row.username}</p>
     <p><strong>昵称：</strong>${row.nickname || '-'}</p>
     <p><strong>邮箱：</strong>${row.email || '-'}</p>
     <p><strong>手机号：</strong>${row.phone || '-'}</p>
     <p><strong>角色：</strong>${getRoleLabel(row.role)}</p>
     <p><strong>状态：</strong>${row.status === 1 ? '正常' : '禁用'}</p>
     <p><strong>创建时间：</strong>${row.createdTime}</p>`,
    '用户详情',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '关闭'
    }
  )
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${row.username}" 吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    loadUserList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const getRoleType = (role) => {
  const types = {
    admin: 'danger',
    test_lead: 'warning',
    tester: 'success',
    developer: 'info'
  }
  return types[role] || 'info'
}

const getRoleLabel = (role) => {
  const labels = {
    admin: '管理员',
    test_lead: '测试负责人',
    tester: '测试人员',
    developer: '开发人员'
  }
  return labels[role] || role
}

onMounted(() => {
  loadUserList()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
