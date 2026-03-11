<template>
  <div class="jmeter-performance-editor">
    <el-card shadow="hover">
      <template #header>
        <span>⚡ 性能自动化测试配置</span>
      </template>
      
      <el-alert
        title="性能自动化测试说明"
        type="warning"
        :closable="false"
        show-icon
        style="margin-bottom: 20px;"
      >
        <template #default>
          <p>通过JMeter在服务端执行性能测试，支持：</p>
          <ul style="margin: 5px 0; padding-left: 20px;">
            <li>并发用户数配置</li>
            <li>持续时间/循环次数</li>
            <li>Ramp-Up时间（逐步加压）</li>
            <li>TPS/响应时间监控</li>
          </ul>
        </template>
      </el-alert>

      <el-form :model="config" label-width="150px">
        <el-form-item label="测试场景名称">
          <el-input v-model="config.name" placeholder="请输入场景名称" />
        </el-form-item>

        <el-divider content-position="left">并发配置</el-divider>

        <el-form-item label="并发用户数">
          <el-input-number v-model="config.threads" :min="1" :max="10000" />
          <span style="margin-left: 10px; color: #909399;">个虚拟用户</span>
        </el-form-item>

        <el-form-item label="Ramp-Up时间">
          <el-input-number v-model="config.rampUp" :min="0" :max="3600" />
          <span style="margin-left: 10px; color: #909399;">秒（逐步加压时间）</span>
        </el-form-item>

        <el-form-item label="循环次数">
          <el-input-number v-model="config.loopCount" :min="1" :max="100000" />
          <span style="margin-left: 10px; color: #909399;">次（-1表示永久）</span>
        </el-form-item>

        <el-form-item label="持续时间">
          <el-input-number v-model="config.duration" :min="0" :max="86400" />
          <span style="margin-left: 10px; color: #909399;">秒（0表示不限制）</span>
        </el-form-item>

        <el-divider content-position="left">请求配置</el-divider>

        <el-form-item label="目标URL">
          <el-input v-model="config.url" placeholder="https://api.example.com/endpoint" />
        </el-form-item>

        <el-form-item label="请求方法">
          <el-select v-model="config.method">
            <el-option label="GET" value="GET" />
            <el-option label="POST" value="POST" />
            <el-option label="PUT" value="PUT" />
            <el-option label="DELETE" value="DELETE" />
          </el-select>
        </el-form-item>

        <el-form-item label="请求超时">
          <el-input-number v-model="config.timeout" :min="1000" :max="300000" :step="1000" />
          <span style="margin-left: 10px; color: #909399;">毫秒</span>
        </el-form-item>

        <el-divider content-position="left">性能指标</el-divider>

        <el-form-item label="目标TPS">
          <el-input-number v-model="config.targetTPS" :min="0" :max="100000" />
          <span style="margin-left: 10px; color: #909399;">次/秒（0表示不限制）</span>
        </el-form-item>

        <el-form-item label="响应时间阈值">
          <el-input-number v-model="config.responseTimeThreshold" :min="100" :max="60000" :step="100" />
          <span style="margin-left: 10px; color: #909399;">毫秒（超过则视为失败）</span>
        </el-form-item>
      </el-form>

      <!-- 配置预览 -->
      <el-divider />
      <div class="config-summary">
        <h4>配置概览</h4>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="并发用户">{{ config.threads }} 个</el-descriptions-item>
          <el-descriptions-item label="Ramp-Up">{{ config.rampUp }} 秒</el-descriptions-item>
          <el-descriptions-item label="循环次数">{{ config.loopCount }} 次</el-descriptions-item>
          <el-descriptions-item label="持续时间">{{ config.duration || '不限制' }} 秒</el-descriptions-item>
          <el-descriptions-item label="目标URL" :span="2">{{ config.url || '未配置' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue';

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['update:modelValue']);

// 用于防止递归更新的标志
let isUpdatingFromProp = false;

// 性能测试配置
const config = ref({
  name: '',
  threads: 10,
  rampUp: 10,
  loopCount: 1,
  duration: 0,
  url: '',
  method: 'GET',
  timeout: 30000,
  targetTPS: 0,
  responseTimeThreshold: 3000
});

// 初始化
if (props.modelValue && props.modelValue.length > 0) {
  config.value = { ...config.value, ...props.modelValue[0] };
}

// 监听内部 config 变化，向父组件发送更新
watch(config, (newVal) => {
  if (!isUpdatingFromProp) {
    // 将配置包装成数组格式
    emit('update:modelValue', [newVal]);
  }
}, { deep: true });

// 监听外部 props 变化，更新内部 config
watch(() => props.modelValue, (newVal) => {
  if (newVal && newVal.length > 0) {
    isUpdatingFromProp = true;
    config.value = { ...config.value, ...newVal[0] };
    // 使用 nextTick 确保更新完成后再重置标志
    setTimeout(() => {
      isUpdatingFromProp = false;
    }, 0);
  }
}, { deep: true });
</script>

<style scoped>
.jmeter-performance-editor {
  width: 100%;
}

.config-summary {
  margin-top: 20px;
}

.config-summary h4 {
  margin-bottom: 15px;
  color: #303133;
}
</style>
