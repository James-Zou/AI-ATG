<template>
  <div class="jmeter-api-editor">
    <el-card shadow="hover" style="margin-bottom: 20px;">
      <template #header>
        <div class="card-header">
          <span>🔌 接口自动化测试配置</span>
          <div>
            <el-button size="small" @click="showImportDialog">
              <el-icon><FolderOpened /></el-icon>
              从接口全集导入
            </el-button>
            <el-button type="primary" size="small" @click="handleAddRequest">
              <el-icon><Plus /></el-icon>
              添加接口
            </el-button>
          </div>
        </div>
      </template>
      
      <el-alert
        title="接口自动化测试说明"
        type="success"
        :closable="false"
        show-icon
        style="margin-bottom: 15px;"
      >
        <template #default>
          <p>通过JMeter在服务端执行接口测试，支持：</p>
          <ul style="margin: 5px 0; padding-left: 20px;">
            <li>HTTP/HTTPS 请求</li>
            <li>请求头、参数、Body配置</li>
            <li>断言验证（状态码、响应内容）</li>
            <li>前置/后置处理器</li>
          </ul>
        </template>
      </el-alert>

      <!-- 接口列表 -->
      <el-empty v-if="requests.length === 0" description="暂无接口配置，请添加接口" />
      
      <div v-else>
        <div v-for="(request, index) in requests" :key="index" class="request-item">
          <el-card shadow="hover">
            <template #header>
              <div class="request-header">
                <div>
                  <el-tag :type="getMethodType(request.method)">{{ request.method }}</el-tag>
                  <span style="margin-left: 10px; font-weight: bold;">{{ request.name || '未命名接口' }}</span>
                </div>
                <div>
                  <el-button size="small" @click="handleEditRequest(index)">编辑</el-button>
                  <el-button size="small" type="danger" @click="handleRemoveRequest(index)">删除</el-button>
                </div>
              </div>
            </template>
            
            <div class="request-preview">
              <p><strong>URL：</strong>{{ request.url || '未配置' }}</p>
              <p v-if="request.timeout"><strong>超时时间：</strong>{{ request.timeout }}ms</p>
              <p v-if="request.assertions && request.assertions.length > 0">
                <strong>断言：</strong>{{ request.assertions.length }} 个
              </p>
            </div>
          </el-card>
        </div>
      </div>
    </el-card>

    <!-- JSON 预览 -->
    <el-card v-if="requests.length > 0" shadow="hover">
      <template #header>
        <div class="json-header">
          <span>📄 JMeter配置预览</span>
          <el-button size="small" @click="handleCopyJSON">
            <el-icon><DocumentCopy /></el-icon>
            复制配置
          </el-button>
        </div>
      </template>
      
      <pre class="json-preview">{{ JSON.stringify(requests, null, 2) }}</pre>
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      :title="editingIndex === -1 ? '添加接口' : '编辑接口'"
      width="800px"
    >
      <el-form :model="currentRequest" label-width="100px">
        <el-form-item label="接口名称">
          <el-input v-model="currentRequest.name" placeholder="请输入接口名称" />
        </el-form-item>

        <el-form-item label="请求方法">
          <el-select v-model="currentRequest.method" placeholder="请选择">
            <el-option label="GET" value="GET" />
            <el-option label="POST" value="POST" />
            <el-option label="PUT" value="PUT" />
            <el-option label="DELETE" value="DELETE" />
            <el-option label="PATCH" value="PATCH" />
          </el-select>
        </el-form-item>

        <el-form-item label="请求URL">
          <el-input v-model="currentRequest.url" placeholder="https://api.example.com/users" />
        </el-form-item>

        <el-form-item label="超时时间">
          <el-input-number v-model="currentRequest.timeout" :min="1000" :max="300000" :step="1000" />
          <span style="margin-left: 10px; color: #909399;">毫秒</span>
        </el-form-item>

        <el-form-item label="请求头">
          <el-input
            v-model="currentRequest.headers"
            type="textarea"
            :rows="3"
            placeholder='{"Content-Type": "application/json"}'
          />
        </el-form-item>

        <el-form-item label="请求Body">
          <el-input
            v-model="currentRequest.body"
            type="textarea"
            :rows="4"
            placeholder='{"key": "value"}'
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveRequest">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 从接口全集导入对话框 -->
    <el-dialog
      v-model="importDialogVisible"
      title="从接口全集导入"
      width="700px"
    >
      <el-form label-width="100px">
        <el-form-item label="选择项目">
          <el-select 
            v-model="importProjectId" 
            placeholder="请选择项目"
            @change="loadPublishedInterfaces"
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
        
        <el-form-item label=" " label-width="0" v-if="publishedInterfaces.length === 0 && importProjectId">
          <el-empty description="该项目暂无已发布的接口" />
        </el-form-item>
        
        <el-form-item label=" " label-width="0" v-else-if="publishedInterfaces.length > 0">
          <el-table
            :data="publishedInterfaces"
            stripe
            max-height="400"
            @selection-change="handleSelectionChange"
          >
            <el-table-column type="selection" width="55" />
            <el-table-column prop="interfaceName" label="接口名称" min-width="200" />
            <el-table-column prop="method" label="方法" width="80">
              <template #default="scope">
                <el-tag :type="getMethodType(scope.row.method)" size="small">
                  {{ scope.row.method }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="url" label="URL" min-width="250" show-overflow-tooltip />
          </el-table>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleImportSelected" :disabled="selectedInterfaces.length === 0">
          导入选中的接口（{{ selectedInterfaces.length }}）
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, DocumentCopy, FolderOpened } from '@element-plus/icons-vue'
import { getPublishedInterfaces } from '@/api/apiInterface'
import { getProjectList } from '@/api/project'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['update:modelValue']);

const requests = ref([...props.modelValue])
const editDialogVisible = ref(false)
const importDialogVisible = ref(false)
const editingIndex = ref(-1)
const projectList = ref([])
const publishedInterfaces = ref([])
const selectedInterfaces = ref([])
const importProjectId = ref(null)

const currentRequest = ref({
  name: '',
  method: 'GET',
  url: '',
  headers: '',
  body: '',
  timeout: 30000,
  assertions: []
})

// 监听变化
watch(() => props.modelValue, (newVal) => {
  if (JSON.stringify(newVal) !== JSON.stringify(requests.value)) {
    requests.value = [...newVal];
  }
});

watch(requests, (newVal, oldVal) => {
  if (JSON.stringify(newVal) !== JSON.stringify(oldVal)) {
    emit('update:modelValue', [...newVal]);
  }
}, { deep: true });

// 添加接口
const handleAddRequest = () => {
  editingIndex.value = -1;
  currentRequest.value = {
    name: '',
    method: 'GET',
    url: '',
    headers: '',
    body: '',
    timeout: 30000,
    assertions: []
  };
  editDialogVisible.value = true;
};

// 编辑接口
const handleEditRequest = (index) => {
  editingIndex.value = index;
  currentRequest.value = { ...requests.value[index] };
  editDialogVisible.value = true;
};

// 保存接口
const handleSaveRequest = () => {
  if (!currentRequest.value.url) {
    ElMessage.warning('请输入请求URL');
    return;
  }

  const request = { ...currentRequest.value };

  if (editingIndex.value === -1) {
    requests.value.push(request);
    ElMessage.success('接口添加成功');
  } else {
    requests.value[editingIndex.value] = request;
    ElMessage.success('接口更新成功');
  }

  // 强制触发父组件更新
  emit('update:modelValue', [...requests.value]);
  
  editDialogVisible.value = false;
};

// 删除接口
const handleRemoveRequest = (index) => {
  requests.value.splice(index, 1);
  ElMessage.success('接口已删除');
  
  // 强制触发父组件更新
  emit('update:modelValue', [...requests.value]);
};

// 复制JSON
const handleCopyJSON = () => {
  const json = JSON.stringify(requests.value, null, 2);
  navigator.clipboard.writeText(json).then(() => {
    ElMessage.success('配置已复制到剪贴板');
  });
};

// 加载项目列表
const loadProjects = async () => {
  try {
    const res = await getProjectList({ pageNum: 1, pageSize: 100 })
    projectList.value = res.data.records
  } catch (error) {
    console.error('加载项目列表失败', error)
  }
}

// 加载已发布的接口
const loadPublishedInterfaces = async () => {
  if (!importProjectId.value) {
    publishedInterfaces.value = []
    return
  }
  
  try {
    const res = await getPublishedInterfaces(importProjectId.value)
    publishedInterfaces.value = res.data
  } catch (error) {
    console.error('加载接口失败', error)
    ElMessage.error('加载接口失败')
  }
}

// 显示导入对话框
const showImportDialog = () => {
  importProjectId.value = null
  publishedInterfaces.value = []
  selectedInterfaces.value = []
  importDialogVisible.value = true
}

// 处理接口选择变化
const handleSelectionChange = (selection) => {
  selectedInterfaces.value = selection
}

// 导入选中的接口
const handleImportSelected = () => {
  if (selectedInterfaces.value.length === 0) {
    ElMessage.warning('请至少选择一个接口')
    return
  }
  
  // 将选中的接口转换为 JMeter 配置格式
  const importedRequests = selectedInterfaces.value.map(iface => ({
    name: iface.interfaceName,
    method: iface.method,
    url: iface.url,
    headers: formatHeaders(iface.headers),
    body: iface.body || '',
    timeout: iface.timeout || 30000,
    assertions: []
  }))
  
  // 追加到现有请求列表
  requests.value.push(...importedRequests)
  
  // 强制触发父组件更新
  emit('update:modelValue', [...requests.value])
  
  ElMessage.success(`成功导入 ${importedRequests.length} 个接口`)
  importDialogVisible.value = false
}

// 格式化 headers 为字符串
const formatHeaders = (headers) => {
  if (!headers) return ''
  
  return Object.entries(headers)
    .map(([key, value]) => `${key}: ${value}`)
    .join('\n')
}

// 获取方法类型
const getMethodType = (method) => {
  const types = {
    'GET': 'success',
    'POST': 'primary',
    'PUT': 'warning',
    'DELETE': 'danger',
    'PATCH': 'info'
  }
  return types[method] || 'info'
}

onMounted(() => {
  loadProjects()
})
</script>

<style scoped>
.jmeter-api-editor {
  width: 100%;
}

.card-header,
.json-header,
.request-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.request-item {
  margin-bottom: 15px;
}

.request-preview {
  font-size: 14px;
  line-height: 1.8;
}

.json-preview {
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 15px;
  margin: 0;
  overflow-x: auto;
  font-family: 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.6;
}
</style>
