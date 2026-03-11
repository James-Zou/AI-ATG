<template>
  <div class="dashboard">
    <div class="welcome-banner">
      <div class="welcome-text">
        <h1>👋 欢迎回来，{{ userStore.username }}！</h1>
        <p>基于 AI 的智能测试平台</p>
      </div>
      <div class="welcome-time">
        <el-icon><Clock /></el-icon>
        {{ currentTime }}
      </div>
    </div>

    <el-row :gutter="20" class="stats-cards">
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card card-blue">
          <div class="stat-icon">
            <el-icon><Folder /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.projectCount }}</div>
            <div class="stat-label">项目总数</div>
            <div class="stat-trend">
              <el-icon><CaretTop /></el-icon>
              <span>12%</span>
            </div>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card card-purple">
          <div class="stat-icon">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.testCaseCount }}</div>
            <div class="stat-label">测试用例</div>
            <div class="stat-trend">
              <el-icon><CaretTop /></el-icon>
              <span>8%</span>
            </div>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card card-green">
          <div class="stat-icon">
            <el-icon><VideoPlay /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.executionCount }}</div>
            <div class="stat-label">执行次数</div>
            <div class="stat-trend">
              <el-icon><CaretTop /></el-icon>
              <span>15%</span>
            </div>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card card-orange">
          <div class="stat-icon">
            <el-icon><CircleCheck /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.passRate }}%</div>
            <div class="stat-label">通过率</div>
            <div class="stat-trend success">
              <el-icon><CaretTop /></el-icon>
              <span>5%</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-section">
      <el-col :xs="24" :lg="16">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>测试执行趋势</span>
              <el-radio-group v-model="trendPeriod" size="small">
                <el-radio-button label="week">本周</el-radio-button>
                <el-radio-button label="month">本月</el-radio-button>
                <el-radio-button label="year">本年</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="chart-container" ref="trendChart"></div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :lg="8">
        <el-card class="chart-card">
          <template #header>
            <span>测试状态分布</span>
          </template>
          <div class="chart-container" ref="statusChart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="bottom-section">
      <el-col :xs="24" :lg="12">
        <el-card class="activity-card">
          <template #header>
            <div class="card-header">
              <span>最近活动</span>
              <el-button text type="primary">查看全部</el-button>
            </div>
          </template>
          <el-timeline>
            <el-timeline-item
              v-for="activity in recentActivities"
              :key="activity.id"
              :timestamp="activity.time"
              :type="activity.type"
            >
              {{ activity.content }}
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :lg="12">
        <el-card class="activity-card">
          <template #header>
            <div class="card-header">
              <span>待办事项</span>
              <el-badge :value="todoList.length" type="danger">
                <el-icon><Bell /></el-icon>
              </el-badge>
            </div>
          </template>
          <div class="todo-list">
            <div v-for="todo in todoList" :key="todo.id" class="todo-item">
              <el-checkbox v-model="todo.completed">
                {{ todo.content }}
              </el-checkbox>
              <el-tag :type="getPriorityType(todo.priority)" size="small">
                {{ todo.priority }}
              </el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import { getDashboardData } from '@/api/dashboard'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { 
  Clock, Folder, Document, VideoPlay, CircleCheck, 
  CaretTop, Bell 
} from '@element-plus/icons-vue'

const userStore = useUserStore()

const currentTime = ref('')
const trendPeriod = ref('week')
const trendChart = ref(null)
const statusChart = ref(null)
const loading = ref(false)

let trendChartInstance = null
let statusChartInstance = null
let timeInterval = null

const stats = ref({
  projectCount: 0,
  testCaseCount: 0,
  executionCount: 0,
  passRate: 0
})

const recentActivities = ref([])

const todoList = ref([
  {
    id: 1,
    content: '审核待批准的测试用例',
    priority: 'P0',
    completed: false
  },
  {
    id: 2,
    content: '完成本周测试报告',
    priority: 'P1',
    completed: false
  },
  {
    id: 3,
    content: '更新项目文档',
    priority: 'P2',
    completed: false
  },
  {
    id: 4,
    content: '配置 GitLab 集成',
    priority: 'P1',
    completed: true
  }
])

// 趋势图数据
const trendData = ref({
  dates: [],
  executionCounts: [],
  passCounts: [],
  failCounts: []
})

// 状态分布数据
const statusDistribution = ref({
  passCount: 0,
  failCount: 0,
  blockCount: 0,
  skipCount: 0
})

/**
 * 加载仪表盘数据
 */
const loadDashboardData = async () => {
  loading.value = true
  try {
    const res = await getDashboardData(trendPeriod.value)
    if (res.code === 200 && res.data) {
      stats.value = res.data.stats || stats.value
      trendData.value = res.data.trendData || trendData.value
      statusDistribution.value = res.data.statusDistribution || statusDistribution.value
      recentActivities.value = res.data.recentActivities || []
      
      // 更新图表
      updateTrendChart()
      updateStatusChart()
    }
  } catch (error) {
    console.error('加载仪表盘数据失败:', error)
    ElMessage.error('加载仪表盘数据失败')
  } finally {
    loading.value = false
  }
}

// 监听时间周期变化
watch(trendPeriod, () => {
  loadDashboardData()
})

const updateTime = () => {
  const now = new Date()
  const options = { 
    year: 'numeric', 
    month: '2-digit', 
    day: '2-digit',
    hour: '2-digit', 
    minute: '2-digit',
    second: '2-digit'
  }
  currentTime.value = now.toLocaleString('zh-CN', options)
}

const initTrendChart = () => {
  if (!trendChart.value) return
  trendChartInstance = echarts.init(trendChart.value)
  updateTrendChart()
}

const updateTrendChart = () => {
  if (!trendChartInstance) return
  
  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(0, 0, 0, 0.8)',
      borderColor: '#409eff',
      textStyle: {
        color: '#fff'
      }
    },
    legend: {
      data: ['执行次数', '通过次数', '失败次数']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: trendData.value.dates
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '执行次数',
        type: 'line',
        smooth: true,
        data: trendData.value.executionCounts,
        itemStyle: {
          color: '#409eff'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.5)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
          ])
        }
      },
      {
        name: '通过次数',
        type: 'line',
        smooth: true,
        data: trendData.value.passCounts,
        itemStyle: {
          color: '#67c23a'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(103, 194, 58, 0.5)' },
            { offset: 1, color: 'rgba(103, 194, 58, 0.1)' }
          ])
        }
      },
      {
        name: '失败次数',
        type: 'line',
        smooth: true,
        data: trendData.value.failCounts,
        itemStyle: {
          color: '#f56c6c'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(245, 108, 108, 0.5)' },
            { offset: 1, color: 'rgba(245, 108, 108, 0.1)' }
          ])
        }
      }
    ]
  }
  
  trendChartInstance.setOption(option)
}

const initStatusChart = () => {
  if (!statusChart.value) return
  statusChartInstance = echarts.init(statusChart.value)
  updateStatusChart()
}

const updateStatusChart = () => {
  if (!statusChartInstance) return
  
  const option = {
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(0, 0, 0, 0.8)',
      borderColor: '#409eff',
      textStyle: {
        color: '#fff'
      }
    },
    legend: {
      orient: 'vertical',
      right: '10%',
      top: 'center'
    },
    series: [
      {
        name: '测试状态',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['40%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 20,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { 
            value: statusDistribution.value.passCount, 
            name: '通过',
            itemStyle: { color: '#67c23a' }
          },
          { 
            value: statusDistribution.value.failCount, 
            name: '失败',
            itemStyle: { color: '#f56c6c' }
          },
          { 
            value: statusDistribution.value.blockCount, 
            name: '阻塞',
            itemStyle: { color: '#e6a23c' }
          },
          { 
            value: statusDistribution.value.skipCount, 
            name: '跳过',
            itemStyle: { color: '#909399' }
          }
        ]
      }
    ]
  }
  
  statusChartInstance.setOption(option)
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

onMounted(async () => {
  updateTime()
  timeInterval = setInterval(updateTime, 1000)
  
  // 加载仪表盘数据
  await loadDashboardData()
  
  // 初始化图表
  setTimeout(() => {
    initTrendChart()
    initStatusChart()
  }, 100)
  
  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    trendChartInstance?.resize()
    statusChartInstance?.resize()
  })
})

onUnmounted(() => {
  if (timeInterval) {
    clearInterval(timeInterval)
  }
  trendChartInstance?.dispose()
  statusChartInstance?.dispose()
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.welcome-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 30px;
  border-radius: 12px;
  margin-bottom: 20px;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.3);
}

.welcome-text h1 {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
}

.welcome-text p {
  margin: 0;
  opacity: 0.9;
  font-size: 14px;
}

.welcome-time {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  opacity: 0.95;
}

.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  cursor: pointer;
  margin-bottom: 20px;
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, var(--card-color-start), var(--card-color-end));
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.card-blue {
  --card-color-start: #409eff;
  --card-color-end: #66b1ff;
}

.card-purple {
  --card-color-start: #a855f7;
  --card-color-end: #c084fc;
}

.card-green {
  --card-color-start: #67c23a;
  --card-color-end: #85ce61;
}

.card-orange {
  --card-color-start: #e6a23c;
  --card-color-end: #f39c12;
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  color: white;
}

.card-blue .stat-icon {
  background: linear-gradient(135deg, #409eff, #66b1ff);
}

.card-purple .stat-icon {
  background: linear-gradient(135deg, #a855f7, #c084fc);
}

.card-green .stat-icon {
  background: linear-gradient(135deg, #67c23a, #85ce61);
}

.card-orange .stat-icon {
  background: linear-gradient(135deg, #e6a23c, #f39c12);
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
  line-height: 1;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #67c23a;
  font-weight: 500;
}

.stat-trend.success {
  color: #67c23a;
}

.chart-section {
  margin-bottom: 20px;
}

.chart-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border-radius: 12px;
}

.chart-card :deep(.el-card__header) {
  border-bottom: 1px solid #f0f0f0;
  padding: 16px 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #303133;
}

.chart-container {
  height: 320px;
  width: 100%;
}

.bottom-section {
  margin-bottom: 20px;
}

.activity-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border-radius: 12px;
  margin-bottom: 20px;
}

.activity-card :deep(.el-card__header) {
  border-bottom: 1px solid #f0f0f0;
  padding: 16px 20px;
}

.activity-card :deep(.el-card__body) {
  max-height: 400px;
  overflow-y: auto;
}

.todo-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.todo-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.todo-item:hover {
  background: #ecf5ff;
}

.todo-item :deep(.el-checkbox__label) {
  color: #606266;
}

@media (max-width: 768px) {
  .welcome-banner {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .welcome-text h1 {
    font-size: 22px;
  }
  
  .stat-value {
    font-size: 24px;
  }
  
  .chart-container {
    height: 250px;
  }
}
</style>
