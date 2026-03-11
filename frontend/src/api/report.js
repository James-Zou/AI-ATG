import request from './request'

/**
 * 生成测试报告
 */
export function generateReport(params) {
  return request({
    url: '/report/generate',
    method: 'post',
    params
  })
}

/**
 * 获取报告详情
 */
export function getReportDetail(id) {
  return request({
    url: `/report/${id}`,
    method: 'get'
  })
}

/**
 * 获取报告列表
 */
export function getReportList(params) {
  return request({
    url: '/report/list',
    method: 'get',
    params
  })
}

/**
 * 导出HTML报告
 */
export function exportHtmlReport(id) {
  return `/api/report/${id}/export/html`
}

/**
 * 导出PDF报告
 */
export function exportPdfReport(id) {
  return request({
    url: `/report/${id}/export/pdf`,
    method: 'get'
  })
}

/**
 * 获取统计数据
 */
export function getReportStatistics(params) {
  return request({
    url: '/report/statistics',
    method: 'get',
    params
  })
}

/**
 * 删除报告
 */
export function deleteReport(id) {
  return request({
    url: `/report/${id}`,
    method: 'delete'
  })
}
