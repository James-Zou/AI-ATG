import request from './request'

/**
 * 获取仪表盘数据
 * @param {string} period - 时间周期：week, month, year
 */
export function getDashboardData(period = 'week') {
  return request({
    url: '/dashboard/data',
    method: 'get',
    params: { period }
  })
}
