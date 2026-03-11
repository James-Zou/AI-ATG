<template>
  <div class="report-detail">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>报告详情</span>
          <div>
            <el-button @click="exportHtml">导出HTML</el-button>
            <el-button @click="handleBack">返回</el-button>
          </div>
        </div>
      </template>
      
      <!-- 报告概览 -->
      <el-descriptions :column="3" border>
        <el-descriptions-item label="报告ID">
          {{ detail.id }}
        </el-descriptions-item>
        
        <el-descriptions-item label="报告名称">
          {{ detail.reportName }}
        </el-descriptions-item>
        
        <el-descriptions-item label="项目">
          {{ detail.projectName }}
        </el-descriptions-item>
        
        <el-descriptions-item label="总用例数">
          {{ detail.totalCases || 0 }}
        </el-descriptions-item>
        
        <el-descriptions-item label="通过数">
          <span style="color: #67C23A; font-weight: bold">
            {{ detail.passedCases || 0 }}
          </span>
        </el-descriptions-item>
        
        <el-descriptions-item label="失败数">
          <span style="color: #F56C6C; font-weight: bold">
            {{ detail.failedCases || 0 }}
          </span>
        </el-descriptions-item>
        
        <el-descriptions-item label="通过率">
          <el-tag :type="getPassRateType(detail.passRate)">
            {{ detail.passRate }}%
          </el-tag>
        </el-descriptions-item>
        
        <el-descriptions-item label="执行时长">
          {{ formatDuration(detail.duration) }}
        </el-descriptions-item>
        
        <el-descriptions-item label="创建时间">
          {{ detail.createdTime }}
        </el-descriptions-item>
      </el-descriptions>
      
      <!-- 摘要 -->
      <el-divider content-position="left">测试摘要</el-divider>
      <el-alert :title="detail.summary" type="info" :closable="false" />
      
      <!-- 图表展示 -->
      <el-divider content-position="left">测试结果分布</el-divider>
      <div class="charts-container">
        <div id="pieChart" style="width: 400px; height: 300px;"></div>
        <div id="barChart" style="width: 600px; height: 300px;"></div>
      </div>
      
      <!-- 执行明细 -->
      <el-divider content-position="left">执行明细</el-divider>
      <el-table :data="detail.executionDetails" stripe border>
        <el-table-column prop="testCaseTitle" label="用例标题" min-width="200" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="耗时(ms)" width="100" />
        <el-table-column prop="errorMessage" label="错误信息" min-width="200">
          <template #default="scope">
            <span v-if="scope.row.errorMessage" style="color: #F56C6C">
              {{ scope.row.errorMessage }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getReportDetail, exportHtmlReport } from '@/api/report'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const detail = ref({})

const loadData = async () => {
  try {
    loading.value = true
    const res = await getReportDetail(route.params.id)
    detail.value = res.data
    
    // 加载完数据后渲染图表
    await nextTick()
    renderCharts()
  } catch (error) {
    ElMessage.error('加载失败')
    handleBack()
  } finally {
    loading.value = false
  }
}

const renderCharts = () => {
  // 饼图
  const pieChart = echarts.init(document.getElementById('pieChart'))
  pieChart.setOption({
    title: {
      text: '测试结果分布',
      left: 'center'
    },
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
        name: '测试结果',
        type: 'pie',
        radius: '50%',
        data: [
          { value: detail.value.passedCases || 0, name: '通过', itemStyle: { color: '#67C23A' } },
          { value: detail.value.failedCases || 0, name: '失败', itemStyle: { color: '#F56C6C' } },
          { value: detail.value.skippedCases || 0, name: '跳过', itemStyle: { color: '#909399' } }
        ],
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
  
  // 柱状图
  const barChart = echarts.init(document.getElementById('barChart'))
  barChart.setOption({
    title: {
      text: '测试用例执行时长',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    xAxis: {
      type: 'category',
      data: detail.value.executionDetails?.slice(0, 10).map((item, index) => `用例${index + 1}`) || []
    },
    yAxis: {
      type: 'value',
      name: '耗时(ms)'
    },
    series: [
      {
        name: '执行时长',
        type: 'bar',
        data: detail.value.executionDetails?.slice(0, 10).map(item => item.duration) || [],
        itemStyle: {
          color: '#409EFF'
        }
      }
    ]
  })
}

const exportHtml = () => {
  const url = exportHtmlReport(route.params.id)
  window.open(url, '_blank')
  ElMessage.success('开始导出HTML报告')
}

const handleBack = () => {
  router.push('/report/list')
}

const formatDuration = (duration) => {
  if (!duration) return '-'
  if (duration < 1000) return `${duration}ms`
  return `${(duration / 1000).toFixed(2)}s`
}

const getPassRateType = (rate) => {
  if (rate >= 90) return 'success'
  if (rate >= 70) return 'warning'
  return 'danger'
}

const getStatusLabel = (status) => {
  const labels = {
    1: '通过',
    2: '失败',
    3: '跳过'
  }
  return labels[status] || '未知'
}

const getStatusType = (status) => {
  const types = {
    1: 'success',
    2: 'danger',
    3: 'info'
  }
  return types[status] || 'info'
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.report-detail {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.charts-container {
  display: flex;
  justify-content: space-around;
  align-items: center;
  margin: 20px 0;
}
</style>
