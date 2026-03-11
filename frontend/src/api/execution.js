import request from './request'

/**
 * 创建并执行测试
 */
export function createExecution(data) {
  return request({
    url: '/execution',
    method: 'post',
    data
  })
}

/**
 * 获取执行详情
 */
export function getExecutionDetail(id) {
  return request({
    url: `/execution/${id}`,
    method: 'get'
  })
}

/**
 * 获取执行列表
 */
export function getExecutionList(params) {
  return request({
    url: '/execution/list',
    method: 'get',
    params
  })
}

/**
 * 停止执行
 */
export function stopExecution(id) {
  return request({
    url: `/execution/${id}/stop`,
    method: 'put'
  })
}
