import request from './request'

/**
 * 生成 API Key
 * @param {Object} data - { appName, description, expireDays }
 */
export function generateApiKey(data) {
  return request({
    url: '/api-key/generate',
    method: 'post',
    data
  })
}

/**
 * 获取当前用户的 API Key 列表
 */
export function getApiKeyList() {
  return request({
    url: '/api-key/list',
    method: 'get'
  })
}

/**
 * 撤销 API Key
 * @param {String} apiKey
 */
export function revokeApiKey(apiKey) {
  return request({
    url: '/api-key/revoke',
    method: 'post',
    params: { apiKey }
  })
}

/**
 * 删除 API Key
 * @param {String} id
 */
export function deleteApiKey(id) {
  return request({
    url: `/api-key/${id}`,
    method: 'delete'
  })
}
