import request from './request'

/**
 * AI生成测试用例
 */
export function generateTestCases(data) {
  return request({
    url: '/ai/generate/requirement',
    method: 'post',
    data
  })
}

/**
 * 获取生成历史
 */
export function getGenerateHistory(params) {
  return request({
    url: '/ai/generate/history',
    method: 'get',
    params
  })
}

/**
 * 获取历史详情
 */
export function getHistoryDetail(id) {
  return request({
    url: `/ai/generate/history/${id}`,
    method: 'get'
  })
}

/**
 * 获取AI配置列表
 */
export function getAiConfigList() {
  return request({
    url: '/ai/config/list',
    method: 'get'
  })
}

/**
 * 创建AI配置
 */
export function createAiConfig(data) {
  return request({
    url: '/ai/config',
    method: 'post',
    data
  })
}

/**
 * 更新AI配置
 */
export function updateAiConfig(id, data) {
  return request({
    url: `/ai/config/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除AI配置
 */
export function deleteAiConfig(id) {
  return request({
    url: `/ai/config/${id}`,
    method: 'delete'
  })
}

/**
 * 获取提示词模板列表
 */
export function getTemplateList() {
  return request({
    url: '/ai/template/list',
    method: 'get'
  })
}

/**
 * 创建提示词模板
 */
export function createTemplate(data) {
  return request({
    url: '/ai/template',
    method: 'post',
    data
  })
}

/**
 * 更新提示词模板
 */
export function updateTemplate(id, data) {
  return request({
    url: `/ai/template/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除提示词模板
 */
export function deleteTemplate(id) {
  return request({
    url: `/ai/template/${id}`,
    method: 'delete'
  })
}
