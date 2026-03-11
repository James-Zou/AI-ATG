import request from '@/utils/request'

/**
 * 获取技能列表
 */
export function getSkillList(params) {
  return request({
    url: '/skills/list',
    method: 'get',
    params
  })
}

/**
 * 创建技能（脚本类型）
 */
export function createSkill(data) {
  return request({
    url: '/skills/create',
    method: 'post',
    data
  })
}

/**
 * 从测试套件导入技能
 */
export function importFromTestSuite(data) {
  return request({
    url: '/skills/import-from-suite',
    method: 'post',
    data
  })
}

/**
 * 更新技能
 */
export function updateSkill(id, data) {
  return request({
    url: `/skills/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除技能
 */
export function deleteSkillById(id) {
  return request({
    url: `/skills/${id}`,
    method: 'delete'
  })
}

/**
 * 获取技能详情
 */
export function getSkillDetail(id) {
  return request({
    url: `/skills/${id}`,
    method: 'get'
  })
}

/**
 * 执行技能
 * @param {number} id - 技能ID
 * @param {object} parameters - 动态参数（可选）
 */
export function executeSkill(id, parameters = {}) {
  return request({
    url: `/skills/${id}/execute`,
    method: 'post',
    data: parameters
  })
}

/**
 * 获取脚本执行状态
 */
export function getScriptExecutionStatus(executionId) {
  return request({
    url: `/script-executions/${executionId}/status`,
    method: 'get'
  })
}

/**
 * 获取脚本执行输出
 */
export function getScriptExecutionOutput(executionId) {
  return request({
    url: `/script-executions/${executionId}/output`,
    method: 'get'
  })
}

/**
 * 获取脚本执行详情
 */
export function getScriptExecutionDetail(executionId) {
  return request({
    url: `/script-executions/${executionId}`,
    method: 'get'
  })
}
