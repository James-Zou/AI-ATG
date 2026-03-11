import request from './request'

/**
 * 保存系统配置
 */
export function saveConfig(data) {
  return request({
    url: '/system/config',
    method: 'post',
    data
  })
}

/**
 * 获取配置
 */
export function getConfig(key) {
  return request({
    url: `/system/config/${key}`,
    method: 'get'
  })
}

/**
 * 获取所有配置
 */
export function getAllConfigs() {
  return request({
    url: '/system/config/list',
    method: 'get'
  })
}

/**
 * 删除配置
 */
export function deleteConfig(key) {
  return request({
    url: `/system/config/${key}`,
    method: 'delete'
  })
}

/**
 * 获取操作日志
 */
export function getOperationLogs(params) {
  return request({
    url: '/system/logs',
    method: 'get',
    params
  })
}
