<template>
  <div class="interface-form">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑接口' : '新建接口' }}</span>
          <el-button @click="handleBack">返回</el-button>
        </div>
      </template>
      
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <!-- 基本信息 -->
        <el-form-item label="所属项目" prop="projectId">
          <el-select 
            v-model="formData.projectId" 
            placeholder="请选择所属项目"
            filterable
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
        
        <el-form-item label="接口名称" prop="interfaceName">
          <el-input
            v-model="formData.interfaceName"
            placeholder="请输入接口名称"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        
        <!-- 请求配置（类似Postman） -->
        <el-divider content-position="left">🌐 请求配置</el-divider>
        
        <el-form-item label="请求方法" prop="method">
          <el-select v-model="formData.method" placeholder="请选择请求方法" style="width: 150px;">
            <el-option label="GET" value="GET">
              <span style="color: #67C23A;">●</span> GET
            </el-option>
            <el-option label="POST" value="POST">
              <span style="color: #409EFF;">●</span> POST
            </el-option>
            <el-option label="PUT" value="PUT">
              <span style="color: #E6A23C;">●</span> PUT
            </el-option>
            <el-option label="DELETE" value="DELETE">
              <span style="color: #F56C6C;">●</span> DELETE
            </el-option>
            <el-option label="PATCH" value="PATCH">
              <span style="color: #909399;">●</span> PATCH
            </el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="请求URL" prop="url">
          <el-input
            v-model="formData.url"
            placeholder="https://api.example.com/v1/users"
          />
        </el-form-item>
        
        <el-form-item label="接口描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入接口描述"
          />
        </el-form-item>
        
        <!-- Tabs 配置区域 -->
        <el-form-item label=" " label-width="0">
          <el-tabs v-model="activeTab" class="config-tabs">
            <!-- Headers -->
            <el-tab-pane label="Headers" name="headers">
              <div class="kv-list">
                <div class="kv-header">
                  <div class="kv-col-checkbox"></div>
                  <div class="kv-col-key">KEY</div>
                  <div class="kv-col-value">VALUE</div>
                  <div class="kv-col-description">DESCRIPTION</div>
                  <div class="kv-col-actions"></div>
                </div>
                
                <div 
                  v-for="(header, index) in formData.headersList" 
                  :key="index" 
                  class="kv-row"
                >
                  <div class="kv-col-checkbox">
                    <el-checkbox v-model="header.enabled" />
                  </div>
                  <div class="kv-col-key">
                    <el-input 
                      v-model="header.key" 
                      placeholder="Key"
                      size="small"
                    />
                  </div>
                  <div class="kv-col-value">
                    <el-input 
                      v-model="header.value" 
                      placeholder="Value"
                      size="small"
                    />
                  </div>
                  <div class="kv-col-description">
                    <el-input 
                      v-model="header.description" 
                      placeholder="Description"
                      size="small"
                    />
                  </div>
                  <div class="kv-col-actions">
                    <el-button 
                      size="small" 
                      text 
                      type="danger"
                      @click="removeHeader(index)"
                    >
                      删除
                    </el-button>
                  </div>
                </div>
                
                <el-button size="small" @click="addHeader" style="margin-top: 10px;">
                  + 添加Header
                </el-button>
              </div>
            </el-tab-pane>
            
            <!-- Params -->
            <el-tab-pane label="Params" name="params">
              <div class="kv-list">
                <div class="kv-header">
                  <div class="kv-col-checkbox"></div>
                  <div class="kv-col-key">KEY</div>
                  <div class="kv-col-value">VALUE</div>
                  <div class="kv-col-description">DESCRIPTION</div>
                  <div class="kv-col-actions"></div>
                </div>
                
                <div 
                  v-for="(param, index) in formData.paramsList" 
                  :key="index" 
                  class="kv-row"
                >
                  <div class="kv-col-checkbox">
                    <el-checkbox v-model="param.enabled" />
                  </div>
                  <div class="kv-col-key">
                    <el-input 
                      v-model="param.key" 
                      placeholder="Key"
                      size="small"
                    />
                  </div>
                  <div class="kv-col-value">
                    <el-input 
                      v-model="param.value" 
                      placeholder="Value"
                      size="small"
                    />
                  </div>
                  <div class="kv-col-description">
                    <el-input 
                      v-model="param.description" 
                      placeholder="Description"
                      size="small"
                    />
                  </div>
                  <div class="kv-col-actions">
                    <el-button 
                      size="small" 
                      text 
                      type="danger"
                      @click="removeParam(index)"
                    >
                      删除
                    </el-button>
                  </div>
                </div>
                
                <el-button size="small" @click="addParam" style="margin-top: 10px;">
                  + 添加Param
                </el-button>
              </div>
            </el-tab-pane>
            
            <!-- Body -->
            <el-tab-pane label="Body" name="body">
              <el-radio-group v-model="formData.bodyType" style="margin-bottom: 15px;">
                <el-radio-button label="raw">raw</el-radio-button>
                <el-radio-button label="form-data">form-data</el-radio-button>
                <el-radio-button label="x-www-form-urlencoded">x-www-form-urlencoded</el-radio-button>
              </el-radio-group>
              
              <el-input
                v-model="formData.body"
                type="textarea"
                :rows="12"
                placeholder='请输入请求体，例如：&#10;{&#10;  "name": "test",&#10;  "age": 18&#10;}'
              />
            </el-tab-pane>
            
            <!-- Settings -->
            <el-tab-pane label="Settings" name="settings">
              <el-form-item label="超时时间(ms)">
                <el-input-number 
                  v-model="formData.timeout" 
                  :min="1000" 
                  :max="300000" 
                  :step="1000"
                  style="width: 200px;"
                />
              </el-form-item>
              
              <el-form-item label="接口分类">
                <el-input 
                  v-model="formData.category" 
                  placeholder="例如：用户管理、订单管理"
                  style="width: 300px;"
                />
              </el-form-item>
              
              <el-form-item label="标签">
                <el-input 
                  v-model="formData.tags" 
                  placeholder="多个标签用逗号分隔"
                  style="width: 300px;"
                />
              </el-form-item>
            </el-tab-pane>
          </el-tabs>
        </el-form-item>
        
        <!-- 操作按钮 -->
        <el-form-item label=" " label-width="0">
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ submitting ? '保存中...' : '保存接口' }}
          </el-button>
          <el-button v-if="!isEdit" type="success" @click="handleSaveAndPublish" :loading="submitting">
            保存并发布
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
import { 
  createInterface, 
  updateInterface, 
  getInterfaceDetail,
  publishInterface 
} from '@/api/apiInterface'
import { getProjectList } from '@/api/project'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

const formRef = ref()
const submitting = ref(false)
const projectList = ref([])
const activeTab = ref('headers')

const isEdit = computed(() => !!route.params.id)

const formData = reactive({
  projectId: null,
  interfaceName: '',
  method: 'GET',
  url: '',
  description: '',
  headersList: [],
  paramsList: [],
  body: '',
  bodyType: 'raw',
  timeout: 30000,
  category: '',
  tags: '',
  status: 'draft'
})

const formRules = {
  projectId: [
    { required: true, message: '请选择所属项目', trigger: 'change' }
  ],
  interfaceName: [
    { required: true, message: '请输入接口名称', trigger: 'blur' }
  ],
  method: [
    { required: true, message: '请选择请求方法', trigger: 'change' }
  ],
  url: [
    { required: true, message: '请输入请求URL', trigger: 'blur' }
  ]
}

const loadProjects = async () => {
  try {
    const res = await getProjectList({ pageNum: 1, pageSize: 100 })
    projectList.value = res.data.records
  } catch (error) {
    console.error('加载项目列表失败', error)
  }
}

const loadData = async () => {
  if (!isEdit.value) return
  
  try {
    const res = await getInterfaceDetail(route.params.id)
    
    console.log('获取到的接口数据:', res.data)
    
    Object.assign(formData, {
      projectId: res.data.projectId,
      interfaceName: res.data.interfaceName,
      method: res.data.method,
      url: res.data.url,
      description: res.data.description,
      body: res.data.body,
      bodyType: res.data.bodyType || 'raw',
      timeout: res.data.timeout || 30000,
      category: res.data.category,
      tags: res.data.tags,
      status: res.data.status
    })
    
    // 转换 headers 为列表（确保 headers 是对象类型）
    if (res.data.headers && typeof res.data.headers === 'object' && !Array.isArray(res.data.headers)) {
      formData.headersList = Object.entries(res.data.headers).map(([key, value]) => ({
        key,
        value,
        enabled: true,
        description: ''
      }))
      console.log('解析到的 headers:', formData.headersList)
    } else {
      formData.headersList = []
      console.log('headers 为空或格式不正确:', res.data.headers)
    }
    
    // 转换 params 为列表（确保 params 是对象类型）
    if (res.data.params && typeof res.data.params === 'object' && !Array.isArray(res.data.params)) {
      formData.paramsList = Object.entries(res.data.params).map(([key, value]) => ({
        key,
        value,
        enabled: true,
        description: ''
      }))
      console.log('解析到的 params:', formData.paramsList)
    } else {
      formData.paramsList = []
      console.log('params 为空或格式不正确:', res.data.params)
    }
  } catch (error) {
    console.error('加载接口失败', error)
    ElMessage.error('加载接口失败: ' + (error.message || '未知错误'))
    handleBack()
  }
}

const addHeader = () => {
  formData.headersList.push({
    key: '',
    value: '',
    enabled: true,
    description: ''
  })
}

const removeHeader = (index) => {
  formData.headersList.splice(index, 1)
}

const addParam = () => {
  formData.paramsList.push({
    key: '',
    value: '',
    enabled: true,
    description: ''
  })
}

const removeParam = (index) => {
  formData.paramsList.splice(index, 1)
}

const buildRequestData = () => {
  // 转换 headersList 为 Map
  const headers = {}
  formData.headersList
    .filter(h => h.enabled && h.key)
    .forEach(h => {
      headers[h.key] = h.value
    })
  
  // 转换 paramsList 为 Map
  const params = {}
  formData.paramsList
    .filter(p => p.enabled && p.key)
    .forEach(p => {
      params[p.key] = p.value
    })
  
  return {
    projectId: formData.projectId,
    interfaceName: formData.interfaceName,
    method: formData.method,
    url: formData.url,
    description: formData.description,
    headers,
    params,
    body: formData.body,
    bodyType: formData.bodyType,
    timeout: formData.timeout,
    category: formData.category,
    tags: formData.tags,
    status: formData.status
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    
    const requestData = buildRequestData()
    
    if (isEdit.value) {
      await updateInterface(route.params.id, requestData)
      ElMessage.success('更新成功')
    } else {
      await createInterface(requestData)
      ElMessage.success('创建成功')
    }
    
    handleBack()
  } catch (error) {
    if (error) {
      ElMessage.error(error.message || '操作失败')
    }
  } finally {
    submitting.value = false
  }
}

const handleSaveAndPublish = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    
    const requestData = buildRequestData()
    requestData.status = 'published'
    
    const res = await createInterface(requestData)
    ElMessage.success('保存并发布成功！')
    handleBack()
  } catch (error) {
    if (error) {
      ElMessage.error(error.message || '操作失败')
    }
  } finally {
    submitting.value = false
  }
}

const handleBack = () => {
  router.push('/interface/list')
}

onMounted(() => {
  loadProjects()
  loadData()
})
</script>

<style scoped>
.interface-form {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.config-tabs {
  width: 100%;
}

.kv-list {
  width: 100%;
}

.kv-header,
.kv-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.kv-header {
  font-weight: 600;
  color: #606266;
  font-size: 13px;
  padding-bottom: 10px;
  border-bottom: 1px solid #EBEEF5;
}

.kv-col-checkbox {
  width: 40px;
  flex-shrink: 0;
}

.kv-col-key {
  flex: 1;
  min-width: 150px;
}

.kv-col-value {
  flex: 1;
  min-width: 200px;
}

.kv-col-description {
  flex: 1;
  min-width: 150px;
}

.kv-col-actions {
  width: 80px;
  flex-shrink: 0;
}
</style>
