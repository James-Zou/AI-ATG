import request from './request'

/**
 * 创建GitLab配置
 */
export function createGitlabConfig(data) {
  return request({
    url: '/gitlab/config',
    method: 'post',
    data
  })
}

/**
 * 更新GitLab配置
 */
export function updateGitlabConfig(id, data) {
  return request({
    url: `/gitlab/config/${id}`,
    method: 'put',
    data
  })
}

/**
 * 获取配置详情
 */
export function getGitlabConfig(id) {
  return request({
    url: `/gitlab/config/${id}`,
    method: 'get'
  })
}

/**
 * 获取项目的GitLab配置
 */
export function getGitlabConfigByProject(projectId) {
  return request({
    url: `/gitlab/config/project/${projectId}`,
    method: 'get'
  })
}

/**
 * 删除配置
 */
export function deleteGitlabConfig(id) {
  return request({
    url: `/gitlab/config/${id}`,
    method: 'delete'
  })
}

/**
 * 获取Webhook记录列表
 */
export function getWebhookRecords(params) {
  return request({
    url: '/gitlab/webhook/records',
    method: 'get',
    params
  })
}

/**
 * 获取Webhook记录详情
 */
export function getWebhookRecord(id) {
  return request({
    url: `/gitlab/webhook/records/${id}`,
    method: 'get'
  })
}
