import request from './request'

/**
 * 创建测试环境
 */
export function createEnvironment(data) {
  return request({
    url: '/environment',
    method: 'post',
    data
  })
}

/**
 * 更新测试环境
 */
export function updateEnvironment(id, data) {
  return request({
    url: `/environment/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除测试环境
 */
export function deleteEnvironment(id) {
  return request({
    url: `/environment/${id}`,
    method: 'delete'
  })
}

/**
 * 获取环境详情
 */
export function getEnvironmentById(id) {
  return request({
    url: `/environment/${id}`,
    method: 'get'
  })
}

/**
 * 获取环境列表（分页）
 */
export function getEnvironmentList(params) {
  return request({
    url: '/environment/list',
    method: 'get',
    params
  })
}

/**
 * 获取项目的所有环境（不分页）
 */
export function getAllEnvironments(projectId) {
  return request({
    url: '/environment/all',
    method: 'get',
    params: { projectId }
  })
}
