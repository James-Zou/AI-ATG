<template>
  <div class="report-statistics">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>测试统计分析</span>
          <el-button @click="handleBack">返回</el-button>
        </div>
      </template>
      
      <!-- 筛选条件 -->
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="统计时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>
      
      <!-- 统计卡片 -->
      <el-row :gutter="20" class="stats-cards">
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-card">
              <div class="stat-icon" style="background: #409EFF;">
                <el-icon :size="30"><Document /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ statistics.totalExecutions || 0 }}</div>
                <div class="stat-label">总执行次数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-card">
              <div class="stat-icon" style="background: #67C23A;">
                <el-icon :size="30"><Check /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ statistics.totalPassed || 0 }}</div>
                <div class="stat-label">总通过用例</div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-card">
              <div class="stat-icon" style="background: #F56C6C;">
                <el-icon :size="30"><Close /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ statistics.totalFailed || 0 }}</div>
                <div class="stat-label">总失败用例</div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-card">
              <div class="stat-icon" style="background: #E6A23C;">
                <el-icon :size="30"><DataAnalysis /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ formatRate(statistics.avgPassRate) }}</div>
                <div class="stat-label">平均通过率</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <!-- 趋势图表 -->
      <el-row :gutter="20" class="charts-row">
        <el-col :span="24">
          <el-card>
            <template #header>
              <span>测试通过率趋势</span>
            </template>
            <div id="trendChart" style="width: 100%; height: 400px;"></div>
          </el-card>
        </el-col>
      </el-row>
      
      <!-- 用例分布和失败Top10 -->
      <el-row :gutter="20" class="charts-row">
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>用例类型分布</span>
            </template>
            <div id="distributionChart" style="width: 100%; height: 400px;"></div>
          </el-card>
        </el-col>
        
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>失败用例Top10</span>
            </template>
            <el-table :data="statistics.topFailedCases" stripe>
              <el-table-column type="index" label="#" width="50" />
              <el-table-column prop="testCaseTitle" label="用例标题" min-width="200" />
              <el-table-column prop="failedCount" label="失败次数" width="100">
                <template #default="scope">
                  <el-tag type="danger">{{ scope.row.failedCount }}</el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { getReportStatistics } from '@/api/report'
import { ElMessage } from 'element-plus'
import { Document, Check, Close, DataAnalysis } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const router = useRouter()

const dateRange = ref([])
const statistics = ref({
  totalExecutions: 0,
  totalCases: 0,
  totalPassed: 0,
  totalFailed: 0,
  avgPassRate: 0,
  trendData: [],
  caseDistribution: {},
  topFailedCases: []
})

const queryForm = reactive({
  projectId: 1,
  startDate: null,
  endDate: null
})

const loadData = async () => {
  try {
    const res = await getReportStatistics(queryForm)
    statistics.value = res.data
    
    // 加载完数据后渲染图表
    await nextTick()
    renderCharts()
  } catch (error) {
    ElMessage.error('加载统计数据失败')
  }
}

const handleDateChange = (dates) => {
  if (dates && dates.length === 2) {
    queryForm.startDate = dates[0]
    queryForm.endDate = dates[1]
  } else {
    queryForm.startDate = null
    queryForm.endDate = null
  }
}

const renderCharts = () => {
  // 趋势图
  if (statistics.value.trendData && statistics.value.trendData.length > 0) {
    const trendChart = echarts.init(document.getElementById('trendChart'))
    trendChart.setOption({
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: ['总用例数', '通过数', '失败数', '通过率']
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
        data: statistics.value.trendData.map(item => item.date)
      },
      yAxis: [
        {
          type: 'value',
          name: '用例数'
        },
        {
          type: 'value',
          name: '通过率(%)',
          min: 0,
          max: 100
        }
      ],
      series: [
        {
          name: '总用例数',
          type: 'line',
          data: statistics.value.trendData.map(item => item.totalCases),
          itemStyle: { color: '#409EFF' }
        },
        {
          name: '通过数',
          type: 'line',
          data: statistics.value.trendData.map(item => item.passedCases),
          itemStyle: { color: '#67C23A' }
        },
        {
          name: '失败数',
          type: 'line',
          data: statistics.value.trendData.map(item => item.failedCases),
          itemStyle: { color: '#F56C6C' }
        },
        {
          name: '通过率',
          type: 'line',
          yAxisIndex: 1,
          data: statistics.value.trendData.map(item => item.passRate),
          itemStyle: { color: '#E6A23C' }
        }
      ]
    })
  }
  
  // 用例分布图
  if (statistics.value.caseDistribution) {
    const distributionChart = echarts.init(document.getElementById('distributionChart'))
    const data = Object.keys(statistics.value.caseDistribution).map(key => ({
      name: getTypeLabel(key),
      value: statistics.value.caseDistribution[key]
    }))
    
    distributionChart.setOption({
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [
        {
          name: '用例类型',
          type: 'pie',
          radius: '50%',
          data: data,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    })
  }
}

const handleBack = () => {
  router.push('/report/list')
}

const formatRate = (rate) => {
  if (rate === null || rate === undefined) return '0%'
  return `${rate.toFixed(2)}%`
}

const getTypeLabel = (type) => {
  const labels = {
    api: 'API测试',
    ui: 'UI测试',
    performance: '性能测试'
  }
  return labels[type] || type
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.report-statistics {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stats-cards {
  margin: 20px 0;
}

.stat-card {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-right: 15px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.charts-row {
  margin-top: 20px;
}
</style>
