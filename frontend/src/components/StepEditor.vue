<template>
  <div class="step-editor">
    <!-- AI 生成区域 -->
    <el-card shadow="hover" style="margin-bottom: 20px;">
      <template #header>
        <div class="ai-header">
          <span>🤖 AI 智能生成测试步骤</span>
          <el-button 
            size="small" 
            @click="handleRefreshAIList"
            :loading="loadingAIList"
          >
            <el-icon><Refresh /></el-icon>
            刷新模型列表
          </el-button>
        </div>
      </template>
      
      <!-- AI模型选择 -->
      <div style="margin-bottom: 15px;">
        <el-form-item label="选择AI模型" style="margin-bottom: 0;">
          <el-select 
            v-model="selectedAI" 
            placeholder="请选择AI模型" 
            style="width: 100%;"
            :loading="loadingAIList"
          >
            <el-option
              v-for="ai in availableAIList"
              :key="ai.id"
              :label="`${getProviderLabel(ai.provider)} - ${ai.modelName}`"
              :value="ai.provider"
            >
              <div style="display: flex; justify-content: space-between; align-items: center;">
                <span>{{ getProviderLabel(ai.provider) }} - {{ ai.modelName }}</span>
                <el-tag v-if="ai.isDefault === 1" type="success" size="small">默认</el-tag>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
      </div>
      
      <!-- 描述输入 -->
      <el-input
        v-model="aiDescription"
        type="textarea"
        :rows="4"
        placeholder="请用自然语言描述测试场景，AI将自动生成可执行的测试步骤&#10;&#10;示例：打开百度首页，在搜索框输入'AI测试'，点击搜索按钮，等待2秒，验证页面标题包含'AI测试'"
      />
      
      <!-- 操作按钮 -->
      <div style="margin-top: 15px;">
        <el-button 
          type="primary" 
          @click="handleAIGenerate"
          :loading="aiGenerating"
          :disabled="!selectedAI || availableAIList.length === 0"
        >
          <el-icon><MagicStick /></el-icon>
          {{ aiGenerating ? 'AI生成中...' : 'AI 生成步骤' }}
        </el-button>
        <el-button 
          v-if="steps.length > 0"
          @click="handleClearSteps"
        >
          清空步骤
        </el-button>
        <el-button 
          link 
          type="primary"
          @click="goToAIConfig"
        >
          <el-icon><Setting /></el-icon>
          配置AI模型
        </el-button>
      </div>
      
      <!-- AI提示 -->
      <el-alert
        v-if="availableAIList.length === 0"
        title="尚未配置AI模型"
        type="warning"
        show-icon
        :closable="false"
        style="margin-top: 15px;"
      >
        <template #default>
          <p>请先前往 <el-button link type="primary" @click="goToAIConfig">AI配置管理</el-button> 添加并启用AI模型</p>
        </template>
      </el-alert>
    </el-card>

    <!-- 导入脚本 -->
    <el-card shadow="hover" style="margin-bottom: 20px;">
      <template #header>
        <div class="import-header">
          <span>📥 导入测试脚本</span>
        </div>
      </template>
      
      <el-alert
        title="支持导入JSON格式的测试步骤脚本"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 15px;"
      >
        <template #default>
          <p>可以导入：</p>
          <ul style="margin: 5px 0; padding-left: 20px;">
            <li>手动编写的JSON脚本</li>
            <li>浏览器插件录制的脚本</li>
            <li>从其他测试用例导出的脚本</li>
          </ul>
        </template>
      </el-alert>
      
      <el-input
        v-model="importScript"
        type="textarea"
        :rows="6"
        placeholder='请粘贴JSON格式的测试步骤，例如：&#10;[&#10;  {"action": "open", "input": "https://www.example.com"},&#10;  {"action": "click", "locator": "id", "value": "btn-login"}&#10;]'
      />
      
      <div style="margin-top: 15px; display: flex; justify-content: space-between; align-items: center;">
        <div>
          <el-button 
            type="success" 
            @click="handleImportScript"
            :loading="importing"
          >
            <el-icon><Upload /></el-icon>
            {{ importing ? '导入中...' : '导入脚本' }}
          </el-button>
          <el-button 
            v-if="importScript"
            @click="handleClearImport"
          >
            清空
          </el-button>
        </div>
        <div>
          <el-button 
            type="primary"
            @click="copyPromptTemplate"
          >
            <el-icon><DocumentCopy /></el-icon>
            复制Prompt模板
          </el-button>
          <el-button 
            link
            type="primary"
            @click="showImportExample"
          >
            <el-icon><QuestionFilled /></el-icon>
            查看示例
          </el-button>
        </div>
      </div>
      
      <!-- Prompt提示 -->
      <el-alert
        title="💡 提示：使用 AI 生成脚本"
        type="info"
        :closable="false"
        style="margin-top: 15px;"
      >
        <template #default>
          <p>点击"复制Prompt模板"，然后：</p>
          <ol style="margin: 5px 0; padding-left: 20px;">
            <li>在 Cursor / ChatGPT / Claude 等 AI 平台粘贴模板</li>
            <li>添加您的测试场景描述</li>
            <li>AI 将生成符合规范的 JSON 脚本</li>
            <li>复制生成的脚本粘贴到上方导入</li>
          </ol>
        </template>
      </el-alert>
    </el-card>

    <!-- 手动添加步骤 -->
    <el-card shadow="hover" style="margin-bottom: 20px;">
      <template #header>
        <div class="manual-header">
          <span>✏️ 手动添加步骤</span>
          <el-button type="primary" size="small" @click="handleAddStep">
            <el-icon><Plus /></el-icon>
            添加步骤
          </el-button>
        </div>
      </template>

      <!-- 步骤列表 -->
      <el-empty v-if="steps.length === 0" description="暂无测试步骤，请使用AI生成或手动添加" />
      
      <draggable 
        v-else
        v-model="steps" 
        item-key="id"
        handle=".drag-handle"
        @end="handleDragEnd"
      >
        <template #item="{ element, index }">
          <div class="step-item">
            <el-card shadow="hover">
              <template #header>
                <div class="step-item-header">
                  <div class="step-left">
                    <el-icon class="drag-handle" style="cursor: move; margin-right: 8px;">
                      <DCaret />
                    </el-icon>
                    <el-tag>步骤 {{ index + 1 }}</el-tag>
                    <el-tag type="info" style="margin-left: 8px;">{{ getActionLabel(element.action) }}</el-tag>
                  </div>
                  <div class="step-right">
                    <el-button
                      size="small"
                      @click="handleEditStep(index)"
                    >
                      编辑
                    </el-button>
                    <el-button
                      type="danger"
                      size="small"
                      @click="handleRemoveStep(index)"
                    >
                      删除
                    </el-button>
                  </div>
                </div>
              </template>
              
              <!-- 步骤预览 -->
              <div class="step-preview">
                <div v-if="element.action === 'open'">
                  <strong>打开URL：</strong>{{ element.input }}
                </div>
                <div v-else-if="element.action === 'click'">
                  <strong>点击元素：</strong>{{ element.locator }}="{{ element.value }}"
                </div>
                <div v-else-if="element.action === 'input'">
                  <strong>输入文本：</strong>在 {{ element.locator }}="{{ element.value }}" 输入 "{{ element.input }}"
                </div>
                <div v-else-if="element.action === 'select'">
                  <strong>选择选项：</strong>在 {{ element.locator }}="{{ element.value }}" 选择 "{{ element.input }}"
                </div>
                <div v-else-if="element.action === 'wait'">
                  <strong>等待：</strong>{{ element.timeout || 1 }} 秒
                </div>
                <div v-else-if="element.action.startsWith('assert')">
                  <strong>断言：</strong>{{ getAssertDescription(element) }}
                </div>
                <div v-else>
                  <pre style="margin: 0;">{{ JSON.stringify(element, null, 2) }}</pre>
                </div>
              </div>
            </el-card>
          </div>
        </template>
      </draggable>
    </el-card>

    <!-- JSON 预览 -->
    <el-card v-if="steps.length > 0" shadow="hover">
      <template #header>
        <div class="json-header">
          <span>📄 JSON 预览</span>
          <el-button size="small" @click="handleCopyJSON">
            <el-icon><DocumentCopy /></el-icon>
            复制JSON
          </el-button>
        </div>
      </template>
      
      <pre class="json-preview">{{ JSON.stringify(steps, null, 2) }}</pre>
    </el-card>

    <!-- 步骤编辑对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      :title="editingIndex === -1 ? '添加步骤' : '编辑步骤'"
      width="700px"
    >
      <el-form :model="currentStep" label-width="100px">
        <el-form-item label="操作类型">
          <el-select v-model="currentStep.action" placeholder="请选择操作类型" @change="handleActionChange">
            <el-option-group label="页面操作">
              <el-option label="打开URL" value="open" />
              <el-option label="刷新页面" value="refresh" />
              <el-option label="后退" value="back" />
              <el-option label="前进" value="forward" />
            </el-option-group>
            <el-option-group label="元素操作">
              <el-option label="点击" value="click" />
              <el-option label="双击" value="doubleClick" />
              <el-option label="右键点击" value="contextClick" />
              <el-option label="悬停" value="hover" />
              <el-option label="输入文本" value="input" />
              <el-option label="清空输入" value="clear" />
              <el-option label="选择下拉" value="select" />
            </el-option-group>
            <el-option-group label="等待与延时">
              <el-option label="等待" value="wait" />
              <el-option label="等待元素" value="waitForElement" />
            </el-option-group>
            <el-option-group label="断言验证">
              <el-option label="验证URL" value="assertUrl" />
              <el-option label="验证标题" value="assertTitle" />
              <el-option label="验证文本" value="assertText" />
              <el-option label="验证元素可见" value="assertVisible" />
              <el-option label="验证元素存在" value="assertExists" />
            </el-option-group>
            <el-option-group label="其他">
              <el-option label="截图" value="screenshot" />
              <el-option label="执行脚本" value="executeScript" />
              <el-option label="切换窗口" value="switchWindow" />
              <el-option label="切换Frame" value="switchFrame" />
            </el-option-group>
          </el-select>
        </el-form-item>

        <!-- 定位器类型 -->
        <el-form-item 
          v-if="needsLocator(currentStep.action)"
          label="定位方式"
        >
          <el-select v-model="currentStep.locator" placeholder="请选择定位方式">
            <el-option label="ID" value="id" />
            <el-option label="CSS选择器" value="css" />
            <el-option label="XPath" value="xpath" />
            <el-option label="Name属性" value="name" />
            <el-option label="Class名称" value="className" />
            <el-option label="标签名" value="tagName" />
            <el-option label="链接文本" value="linkText" />
          </el-select>
        </el-form-item>

        <!-- 定位器值 -->
        <el-form-item 
          v-if="needsLocator(currentStep.action)"
          label="定位器值"
        >
          <el-input 
            v-model="currentStep.value" 
            placeholder="例如: #username 或 .btn-submit"
          />
        </el-form-item>

        <!-- 输入内容 -->
        <el-form-item 
          v-if="needsInput(currentStep.action)"
          :label="getInputLabel(currentStep.action)"
        >
          <el-input 
            v-model="currentStep.input" 
            :placeholder="getInputPlaceholder(currentStep.action)"
          />
        </el-form-item>

        <!-- 超时时间 -->
        <el-form-item 
          v-if="needsTimeout(currentStep.action)"
          label="等待时间(秒)"
        >
          <el-input-number 
            v-model="currentStep.timeout" 
            :min="0.1" 
            :max="60" 
            :step="0.5"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleCancelStep">取消</el-button>
          <el-button type="primary" @click="handleSaveStep">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, MagicStick, DocumentCopy, DCaret, Refresh, Setting, Upload, QuestionFilled } from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';
import draggable from 'vuedraggable';
import axios from 'axios';
import { getAiConfigList } from '@/api/ai';

const router = useRouter();

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['update:modelValue']);

const steps = ref([...props.modelValue]);
const aiDescription = ref('');
const aiGenerating = ref(false);
const editDialogVisible = ref(false);
const editingIndex = ref(-1);
const currentStep = ref({
  action: '',
  locator: '',
  value: '',
  input: '',
  timeout: 1
});

// AI模型相关
const availableAIList = ref([]);
const selectedAI = ref('');
const loadingAIList = ref(false);

// 导入脚本相关
const importScript = ref('');
const importing = ref(false);

// 监听外部变化（避免递归）
watch(() => props.modelValue, (newVal) => {
  // 只有当外部值真正改变时才更新
  if (JSON.stringify(newVal) !== JSON.stringify(steps.value)) {
    steps.value = [...newVal];
  }
});

// 监听内部变化（避免递归）
watch(steps, (newVal, oldVal) => {
  // 只有当内部值真正改变时才emit
  if (JSON.stringify(newVal) !== JSON.stringify(oldVal)) {
    emit('update:modelValue', [...newVal]);
  }
}, { deep: true });

// 组件挂载时加载AI列表
onMounted(() => {
  loadAIList();
});

// 加载AI模型列表
const loadAIList = async () => {
  try {
    loadingAIList.value = true;
    const response = await getAiConfigList();
    
    if (response.data) {
      // 只显示启用的配置
      availableAIList.value = response.data.filter(item => item.status === 1);
      
      // 自动选择默认的AI
      const defaultAI = availableAIList.value.find(item => item.isDefault === 1);
      if (defaultAI) {
        selectedAI.value = defaultAI.provider;
      } else if (availableAIList.value.length > 0) {
        selectedAI.value = availableAIList.value[0].provider;
      }
    }
  } catch (error) {
    console.error('加载AI列表失败:', error);
    ElMessage.error('加载AI模型列表失败');
  } finally {
    loadingAIList.value = false;
  }
};

// 刷新AI列表
const handleRefreshAIList = () => {
  loadAIList();
};

// 跳转到AI配置页面
const goToAIConfig = () => {
  router.push('/ai/config');
};

// 获取提供商显示名称
const getProviderLabel = (provider) => {
  const labels = {
    'deepseek': 'DeepSeek',
    'qwen': '阿里千问',
    'zhipu': '智谱AI'
  };
  return labels[provider] || provider;
};

// AI生成步骤
const handleAIGenerate = async () => {
  if (!aiDescription.value.trim()) {
    ElMessage.warning('请输入测试场景描述');
    return;
  }
  
  if (!selectedAI.value) {
    ElMessage.warning('请选择AI模型');
    return;
  }

  aiGenerating.value = true;
  try {
    const response = await axios.post('/api/ai/generate-steps', {
      description: aiDescription.value,
      provider: selectedAI.value
    });

    if (response.data.code === 200) {
      steps.value = response.data.data;
      
      // 强制触发父组件更新
      emit('update:modelValue', [...steps.value]);
      
      ElMessage.success('AI生成成功！');
      aiDescription.value = '';
    } else {
      ElMessage.error(response.data.message || 'AI生成失败');
    }
  } catch (error) {
    console.error('AI生成失败:', error);
    ElMessage.error(error.response?.data?.message || 'AI生成失败，请稍后重试');
  } finally {
    aiGenerating.value = false;
  }
};

// 导入脚本
const handleImportScript = async () => {
  if (!importScript.value.trim()) {
    ElMessage.warning('请粘贴JSON格式的测试脚本');
    return;
  }

  importing.value = true;
  
  try {
    // 解析JSON
    let importedSteps;
    try {
      importedSteps = JSON.parse(importScript.value.trim());
    } catch (parseError) {
      ElMessage.error('JSON 格式错误：' + parseError.message);
      return;
    }
    
    // 验证格式
    if (!Array.isArray(importedSteps)) {
      ElMessage.error('脚本格式错误：必须是JSON数组格式');
      return;
    }
    
    if (importedSteps.length === 0) {
      ElMessage.warning('脚本为空，请添加测试步骤');
      return;
    }
    
    // 验证每个步骤
    const invalidSteps = [];
    importedSteps.forEach((step, index) => {
      if (!step.action) {
        invalidSteps.push(index + 1);
      }
    });
    
    if (invalidSteps.length > 0) {
      ElMessage.error(`步骤 ${invalidSteps.join(', ')} 缺少必填字段 'action'`);
      return;
    }
    
    // 如果当前已有步骤，询问是追加还是替换
    if (steps.value.length > 0) {
      try {
        await ElMessageBox.confirm(
          `当前已有 ${steps.value.length} 个步骤，是否要追加新步骤？点击"取消"将替换现有步骤。`,
          '导入确认',
          {
            confirmButtonText: '追加',
            cancelButtonText: '替换',
            distinguishCancelAndClose: true,
            type: 'warning'
          }
        );
        // 追加
        steps.value = [...steps.value, ...importedSteps];
        
        // 强制触发父组件更新
        emit('update:modelValue', [...steps.value]);
        
        ElMessage.success(`成功追加 ${importedSteps.length} 个测试步骤！`);
        importScript.value = '';
      } catch (action) {
        if (action === 'cancel') {
          // 替换
          steps.value = importedSteps;
          
          // 强制触发父组件更新
          emit('update:modelValue', [...steps.value]);
          
          ElMessage.success(`成功导入 ${importedSteps.length} 个测试步骤！`);
          importScript.value = '';
        }
        // 点击关闭按钮或 ESC 时不做任何操作
      }
    } else {
      // 直接导入
      steps.value = importedSteps;
      
      // 强制触发父组件更新
      emit('update:modelValue', [...steps.value]);
      
      ElMessage.success(`成功导入 ${importedSteps.length} 个测试步骤！`);
      importScript.value = '';
    }
    
  } catch (error) {
    console.error('导入失败:', error);
    ElMessage.error('导入失败：' + error.message);
  } finally {
    importing.value = false;
  }
};

// 清空导入区域
const handleClearImport = () => {
  importScript.value = '';
};

// 复制 Prompt 模板
const copyPromptTemplate = () => {
  const promptTemplate = `你是一个 Web UI 自动化测试专家。请根据我提供的测试场景描述，生成符合以下规范的 JSON 格式测试步骤。

## 输出要求
1. 必须是 JSON 数组格式
2. 只返回 JSON，不要包含任何解释文字或 markdown 代码块标记
3. 每个步骤必须包含 action 字段

## 支持的操作类型及格式

### 页面操作
- 打开URL: {"action": "open", "input": "https://example.com"}
- 刷新页面: {"action": "refresh"}
- 后退: {"action": "back"}
- 前进: {"action": "forward"}

### 元素操作
- 点击: {"action": "click", "locator": "id|css|xpath", "value": "元素选择器"}
- 双击: {"action": "doubleClick", "locator": "id|css|xpath", "value": "元素选择器"}
- 右键: {"action": "contextClick", "locator": "id|css|xpath", "value": "元素选择器"}
- 悬停: {"action": "hover", "locator": "id|css|xpath", "value": "元素选择器"}
- 输入: {"action": "input", "locator": "id|css|xpath", "value": "元素选择器", "input": "输入内容"}
- 清空: {"action": "clear", "locator": "id|css|xpath", "value": "元素选择器"}
- 选择: {"action": "select", "locator": "id|css|xpath", "value": "元素选择器", "input": "选项值"}

### 等待操作
- 固定等待: {"action": "wait", "timeout": 秒数}
- 等待元素: {"action": "waitForElement", "locator": "id|css|xpath", "value": "元素选择器", "timeout": 秒数}

### 断言验证
- 验证URL: {"action": "assertUrl", "input": "预期URL"}
- 验证标题: {"action": "assertTitle", "input": "预期标题"}
- 验证文本: {"action": "assertText", "locator": "id|css|xpath", "value": "元素选择器", "input": "预期文本"}
- 验证可见: {"action": "assertVisible", "locator": "id|css|xpath", "value": "元素选择器"}
- 验证存在: {"action": "assertExists", "locator": "id|css|xpath", "value": "元素选择器"}

### 其他操作
- 截图: {"action": "screenshot"}
- 执行脚本: {"action": "executeScript", "input": "JavaScript代码"}
- 切换窗口: {"action": "switchWindow"}
- 切换Frame: {"action": "switchFrame"}

## 定位器类型 (locator)
- id: 元素的 ID 属性（优先使用）
- css: CSS 选择器（如 .btn-login, #username）
- xpath: XPath 表达式
- name: 元素的 name 属性
- className: 元素的 class 属性
- tagName: HTML 标签名
- linkText: 链接文本

## 最佳实践
1. 优先使用 id 定位器
2. 在关键操作后添加适当的等待
3. 在重要步骤后添加断言验证
4. 使用清晰的选择器命名

## 示例
输入：用户登录流程
输出：
[
  {"action": "open", "input": "https://www.example.com/login"},
  {"action": "input", "locator": "id", "value": "username", "input": "admin"},
  {"action": "input", "locator": "id", "value": "password", "input": "admin123"},
  {"action": "click", "locator": "css", "value": ".btn-login"},
  {"action": "wait", "timeout": 2},
  {"action": "assertUrl", "input": "https://www.example.com/dashboard"}
]

---

现在，请根据以下测试场景生成 JSON 格式的测试步骤：

【请在这里描述您的测试场景】`;

  navigator.clipboard.writeText(promptTemplate).then(() => {
    ElMessage.success({
      message: 'Prompt 模板已复制到剪贴板！现在可以粘贴到 Cursor/ChatGPT/Claude 等 AI 平台使用',
      duration: 3000
    });
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制');
  });
};

// 显示导入示例
const showImportExample = () => {
  const example = [
    {
      "action": "open",
      "input": "https://www.example.com"
    },
    {
      "action": "input",
      "locator": "id",
      "value": "username",
      "input": "testuser"
    },
    {
      "action": "input",
      "locator": "id",
      "value": "password",
      "input": "password123"
    },
    {
      "action": "click",
      "locator": "css",
      "value": ".btn-login"
    },
    {
      "action": "wait",
      "timeout": 2
    },
    {
      "action": "assertUrl",
      "input": "https://www.example.com/dashboard"
    }
  ];
  
  ElMessageBox.alert(
    `<pre style="background: #f5f7fa; padding: 15px; border-radius: 4px; overflow-x: auto; font-size: 12px;">${JSON.stringify(example, null, 2)}</pre>`,
    '脚本示例',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '复制示例',
      callback: () => {
        navigator.clipboard.writeText(JSON.stringify(example, null, 2));
        ElMessage.success('示例已复制到剪贴板');
      }
    }
  );
};

// 手动添加步骤
const handleAddStep = () => {
  editingIndex.value = -1;
  currentStep.value = {
    action: '',
    locator: '',
    value: '',
    input: '',
    timeout: 1
  };
  editDialogVisible.value = true;
};

// 编辑步骤
const handleEditStep = (index) => {
  editingIndex.value = index;
  currentStep.value = { ...steps.value[index] };
  editDialogVisible.value = true;
};

// 取消编辑
const handleCancelStep = () => {
  editDialogVisible.value = false;
  // 重置表单
  currentStep.value = {
    action: '',
    locator: '',
    value: '',
    input: '',
    timeout: 1
  };
};

// 保存步骤
const handleSaveStep = () => {
  console.log('保存步骤被调用', currentStep.value);
  
  if (!currentStep.value.action) {
    ElMessage.warning('请选择操作类型');
    return;
  }

  // 验证必填字段
  if (needsLocator(currentStep.value.action)) {
    if (!currentStep.value.locator || !currentStep.value.value) {
      ElMessage.warning('请填写定位方式和定位器值');
      return;
    }
  }
  
  if (needsInput(currentStep.value.action) && currentStep.value.action === 'open') {
    if (!currentStep.value.input) {
      ElMessage.warning('请填写URL地址');
      return;
    }
  }

  // 清理不需要的字段
  const cleanStep = { action: currentStep.value.action };
  
  if (needsLocator(currentStep.value.action)) {
    cleanStep.locator = currentStep.value.locator;
    cleanStep.value = currentStep.value.value;
  }
  
  if (needsInput(currentStep.value.action)) {
    cleanStep.input = currentStep.value.input;
  }
  
  if (needsTimeout(currentStep.value.action)) {
    cleanStep.timeout = currentStep.value.timeout;
  }

  if (editingIndex.value === -1) {
    steps.value.push(cleanStep);
    ElMessage.success('步骤添加成功');
  } else {
    steps.value[editingIndex.value] = cleanStep;
    ElMessage.success('步骤更新成功');
  }

  // 强制触发父组件更新
  emit('update:modelValue', [...steps.value]);
  
  editDialogVisible.value = false;
  
  // 重置表单
  currentStep.value = {
    action: '',
    locator: '',
    value: '',
    input: '',
    timeout: 1
  };
};

// 删除步骤
const handleRemoveStep = (index) => {
  steps.value.splice(index, 1);
  
  // 强制触发父组件更新
  emit('update:modelValue', [...steps.value]);
};

// 清空步骤
const handleClearSteps = () => {
  steps.value = [];
  
  // 强制触发父组件更新
  emit('update:modelValue', []);
  
  ElMessage.success('已清空所有步骤');
};

// 拖拽结束
const handleDragEnd = () => {
  // 强制触发父组件更新（拖拽改变了顺序）
  emit('update:modelValue', [...steps.value]);
  
  ElMessage.success('步骤顺序已更新');
};

// 复制JSON
const handleCopyJSON = () => {
  const json = JSON.stringify(steps.value, null, 2);
  navigator.clipboard.writeText(json).then(() => {
    ElMessage.success('JSON已复制到剪贴板');
  });
};

// 操作类型改变
const handleActionChange = () => {
  // 重置其他字段
  if (!needsLocator(currentStep.value.action)) {
    currentStep.value.locator = '';
    currentStep.value.value = '';
  }
  if (!needsInput(currentStep.value.action)) {
    currentStep.value.input = '';
  }
  if (!needsTimeout(currentStep.value.action)) {
    currentStep.value.timeout = 1;
  }
};

// 判断是否需要定位器
const needsLocator = (action) => {
  const locatorActions = ['click', 'doubleClick', 'contextClick', 'hover', 'input', 'clear', 
    'select', 'waitForElement', 'assertText', 'assertVisible', 'assertExists'];
  return locatorActions.includes(action);
};

// 判断是否需要输入内容
const needsInput = (action) => {
  const inputActions = ['open', 'input', 'select', 'assertUrl', 'assertTitle', 'assertText', 'executeScript'];
  return inputActions.includes(action);
};

// 判断是否需要超时时间
const needsTimeout = (action) => {
  return ['wait', 'waitForElement'].includes(action);
};

// 获取输入框标签
const getInputLabel = (action) => {
  const labels = {
    'open': 'URL地址',
    'input': '输入内容',
    'select': '选择值',
    'assertUrl': '预期URL',
    'assertTitle': '预期标题',
    'assertText': '预期文本',
    'executeScript': '脚本内容'
  };
  return labels[action] || '输入内容';
};

// 获取输入框占位符
const getInputPlaceholder = (action) => {
  const placeholders = {
    'open': 'https://www.example.com',
    'input': '要输入的文本',
    'select': '要选择的选项值',
    'assertUrl': '预期的URL',
    'assertTitle': '预期的页面标题',
    'assertText': '预期的文本内容',
    'executeScript': 'JavaScript代码'
  };
  return placeholders[action] || '请输入内容';
};

// 获取操作标签
const getActionLabel = (action) => {
  const labels = {
    'open': '打开URL',
    'click': '点击',
    'input': '输入',
    'select': '选择',
    'wait': '等待',
    'assertUrl': '验证URL',
    'assertTitle': '验证标题',
    'assertText': '验证文本',
    'assertVisible': '验证可见',
    'hover': '悬停',
    'doubleClick': '双击',
    'refresh': '刷新',
    'back': '后退',
    'forward': '前进'
  };
  return labels[action] || action;
};

// 获取断言描述
const getAssertDescription = (step) => {
  if (step.action === 'assertUrl') return `URL应为 "${step.input}"`;
  if (step.action === 'assertTitle') return `标题应为 "${step.input}"`;
  if (step.action === 'assertText') return `元素 ${step.locator}="${step.value}" 文本应为 "${step.input}"`;
  if (step.action === 'assertVisible') return `元素 ${step.locator}="${step.value}" 应可见`;
  if (step.action === 'assertExists') return `元素 ${step.locator}="${step.value}" 应存在`;
  return JSON.stringify(step);
};
</script>

<style scoped>
.step-editor {
  width: 100%;
}

.ai-header,
.import-header,
.manual-header,
.json-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.step-item {
  margin-bottom: 15px;
}

.step-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.step-left {
  display: flex;
  align-items: center;
}

.step-right {
  display: flex;
  gap: 8px;
}

.step-preview {
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

.drag-handle {
  cursor: move;
}

.drag-handle:hover {
  color: #409eff;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
