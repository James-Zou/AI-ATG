<template>
  <div class="report-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>测试报告</span>
          <el-button type="primary" @click="showStatistics">
            <el-icon><DataAnalysis /></el-icon>
            查看统计
          </el-button>
        </div>
      </template>
      
      <!-- 筛选条件 -->
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="报告类型">
          <el-select v-model="queryForm.reportType" placeholder="全部" clearable style="width: 150px">
            <el-option label="HTML" value="html" />
            <el-option label="PDF" value="pdf" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="创建时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            @change="handleDateChange"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <!-- 报告列表 -->
      <el-table :data="reportList" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="reportName" label="报告名称" min-width="200" />
        <el-table-column prop="projectName" label="项目" width="150" />
        <el-table-column prop="reportType" label="类型" width="100">
          <template #default="scope">
            <el-tag>{{ scope.row.reportType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalCases" label="总用例" width="90" />
        <el-table-column prop="passedCases" label="通过" width="80">
          <template #default="scope">
            <span style="color: #67C23A">{{ scope.row.passedCases || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="failedCases" label="失败" width="80">
          <template #default="scope">
            <span style="color: #F56C6C">{{ scope.row.failedCases || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="passRate" label="通过率" width="100">
          <template #default="scope">
            <el-progress
              :percentage="scope.row.passRate || 0"
              :color="getProgressColor(scope.row.passRate)"
              :stroke-width="8"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="viewDetail(scope.row)">
              查看详情
            </el-button>
            <el-button size="small" @click="exportHtml(scope.row)">
              导出HTML
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
      
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadData"
        class="pagination"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getReportList, deleteReport, exportHtmlReport } from '@/api/report'
import { ElMessage, ElMessageBox } from 'element-plus'
import { DataAnalysis } from '@element-plus/icons-vue'

const router = useRouter()

const loading = ref(false)
const reportList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const dateRange = ref([])

const queryForm = reactive({
  projectId: 1,
  reportType: '',
  startTime: null,
  endTime: null
})

const loadData = async () => {
  try {
    loading.value = true
    const res = await getReportList({
      ...queryForm,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    reportList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleDateChange = (dates) => {
  if (dates && dates.length === 2) {
    queryForm.startTime = dates[0]
    queryForm.endTime = dates[1]
  } else {
    queryForm.startTime = null
    queryForm.endTime = null
  }
}

const handleReset = () => {
  queryForm.reportType = ''
  queryForm.startTime = null
  queryForm.endTime = null
  dateRange.value = []
  loadData()
}

const viewDetail = (row) => {
  router.push(`/report/detail/${row.id}`)
}

const exportHtml = (row) => {
  const url = exportHtmlReport(row.id)
  window.open(url, '_blank')
  ElMessage.success('开始导出HTML报告')
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这个报告吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteReport(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const showStatistics = () => {
  router.push('/report/statistics')
}

const getProgressColor = (percentage) => {
  if (percentage >= 90) return '#67C23A'
  if (percentage >= 70) return '#E6A23C'
  return '#F56C6C'
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.report-list {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>
