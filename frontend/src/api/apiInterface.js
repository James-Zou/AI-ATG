import request from './request'

/**
 * 创建接口
 */
export function createInterface(data) {
  return request({
    url: '/interface',
    method: 'post',
    data
  })
}

/**
 * 更新接口
 */
export function updateInterface(id, data) {
  return request({
    url: `/interface/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除接口
 */
export function deleteInterface(id) {
  return request({
    url: `/interface/${id}`,
    method: 'delete'
  })
}

/**
 * 获取接口详情
 */
export function getInterfaceDetail(id) {
  return request({
    url: `/interface/${id}`,
    method: 'get'
  })
}

/**
 * 分页查询接口列表
 */
export function getInterfaceList(params) {
  return request({
    url: '/interface/list',
    method: 'get',
    params
  })
}

/**
 * 从cURL导入接口
 */
export function importFromCurl(curl, projectId) {
  return request({
    url: '/interface/import/curl',
    method: 'post',
    data: { curl, projectId }
  })
}

/**
 * 发布接口
 */
export function publishInterface(id) {
  return request({
    url: `/interface/${id}/publish`,
    method: 'put'
  })
}

/**
 * 获取已发布的接口列表
 */
export function getPublishedInterfaces(projectId) {
  return request({
    url: '/interface/published',
    method: 'get',
    params: { projectId }
  })
}
