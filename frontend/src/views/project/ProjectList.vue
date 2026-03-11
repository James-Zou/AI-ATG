<template>
  <div class="project-container">
    <div class="header">
      <h2>项目管理</h2>
      <el-button type="primary" @click="handleCreate">创建项目</el-button>
    </div>

    <el-card>
      <el-table :data="projectList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="项目名称" min-width="150" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="memberCount" label="成员数" width="100" align="center">
          <template #default="{ row }">
            <el-tag>{{ row.memberCount }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="requirementCount" label="需求数" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="success">{{ row.requirementCount }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="testCaseCount" label="用例数" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="warning">{{ row.testCaseCount }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleMembers(row)">成员管理</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchProjects"
        @current-change="fetchProjects"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="resetForm"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入项目名称" />
        </el-form-item>
        <el-form-item label="项目描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入项目描述"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="memberDialogVisible"
      title="成员管理"
      width="800px"
    >
      <div style="margin-bottom: 15px;">
        <el-button type="primary" size="small" @click="handleAddMember">添加成员</el-button>
      </div>

      <el-table :data="memberList" v-loading="memberLoading">
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="nickname" label="昵称" width="150" />
        <el-table-column prop="email" label="邮箱" min-width="200" />
        <el-table-column prop="role" label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="row.role === 'admin' ? 'danger' : 'primary'">
              {{ row.role === 'admin' ? '管理员' : '成员' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button link type="danger" @click="handleRemoveMember(row)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog
      v-model="addMemberDialogVisible"
      title="添加成员"
      width="500px"
    >
      <el-form :model="memberForm" label-width="100px">
        <el-form-item label="用户ID">
          <el-input v-model.number="memberForm.userId" placeholder="请输入用户ID" type="number" />
        </el-form-item>
        <el-form-item label="角色">
          <el-radio-group v-model="memberForm.role">
            <el-radio label="member">成员</el-radio>
            <el-radio label="admin">管理员</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="addMemberDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAddMember">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getProjectList,
  createProject,
  updateProject,
  deleteProject,
  getProjectMembers,
  addMember,
  removeMember
} from '../../api/project'

const loading = ref(false)
const projectList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const dialogVisible = ref(false)
const dialogTitle = ref('创建项目')
const form = ref({
  name: '',
  description: '',
  status: 1
})
const formRef = ref(null)

const rules = {
  name: [
    { required: true, message: '请输入项目名称', trigger: 'blur' }
  ]
}

const memberDialogVisible = ref(false)
const memberList = ref([])
const memberLoading = ref(false)
const currentProjectId = ref(null)

const addMemberDialogVisible = ref(false)
const memberForm = ref({
  userId: null,
  role: 'member'
})

onMounted(() => {
  fetchProjects()
})

const fetchProjects = async () => {
  loading.value = true
  try {
    const res = await getProjectList({
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    if (res.code === 200) {
      projectList.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    ElMessage.error('获取项目列表失败')
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  dialogTitle.value = '创建项目'
  form.value = {
    name: '',
    description: '',
    status: 1
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑项目'
  form.value = {
    id: row.id,
    name: row.name,
    description: row.description,
    status: row.status
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  
  try {
    if (form.value.id) {
      await updateProject(form.value.id, form.value)
      ElMessage.success('项目更新成功')
    } else {
      await createProject(form.value)
      ElMessage.success('项目创建成功')
    }
    dialogVisible.value = false
    fetchProjects()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除项目"${row.name}"吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteProject(row.id)
      ElMessage.success('删除成功')
      fetchProjects()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const handleMembers = async (row) => {
  currentProjectId.value = row.id
  memberDialogVisible.value = true
  fetchMembers()
}

const fetchMembers = async () => {
  memberLoading.value = true
  try {
    const res = await getProjectMembers(currentProjectId.value)
    if (res.code === 200) {
      memberList.value = res.data
    }
  } catch (error) {
    ElMessage.error('获取成员列表失败')
  } finally {
    memberLoading.value = false
  }
}

const handleAddMember = () => {
  memberForm.value = {
    userId: null,
    role: 'member'
  }
  addMemberDialogVisible.value = true
}

const submitAddMember = async () => {
  if (!memberForm.value.userId) {
    ElMessage.warning('请输入用户ID')
    return
  }

  try {
    await addMember(currentProjectId.value, memberForm.value)
    ElMessage.success('添加成功')
    addMemberDialogVisible.value = false
    fetchMembers()
  } catch (error) {
    ElMessage.error('添加失败')
  }
}

const handleRemoveMember = (row) => {
  ElMessageBox.confirm(
    `确定要移除成员"${row.username}"吗？`,
    '移除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await removeMember(currentProjectId.value, row.userId)
      ElMessage.success('移除成功')
      fetchMembers()
    } catch (error) {
      ElMessage.error('移除失败')
    }
  }).catch(() => {})
}

const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
}
</script>

<style scoped>
.project-container {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h2 {
  margin: 0;
  font-size: 24px;
}
</style>
