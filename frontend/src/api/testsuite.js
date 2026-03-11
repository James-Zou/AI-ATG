import request from './request'

/**
 * 创建测试套件
 */
export function createTestSuite(data) {
  return request({
    url: '/testsuite',
    method: 'post',
    data
  })
}

/**
 * 更新测试套件
 */
export function updateTestSuite(id, data) {
  return request({
    url: `/testsuite/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除测试套件
 */
export function deleteTestSuite(id) {
  return request({
    url: `/testsuite/${id}`,
    method: 'delete'
  })
}

/**
 * 获取测试套件详情
 */
export function getTestSuiteDetail(id) {
  return request({
    url: `/testsuite/${id}`,
    method: 'get'
  })
}

/**
 * 获取测试套件列表
 */
export function getTestSuiteList(params) {
  return request({
    url: '/testsuite/list',
    method: 'get',
    params
  })
}

/**
 * 获取套件用例列表（按执行顺序）
 */
export function getSuiteCases(suiteId) {
  return request({
    url: `/testsuite/${suiteId}/cases`,
    method: 'get'
  })
}

/**
 * 获取套件的合并测试步骤（用于技能编辑）
 */
export function getSuiteSteps(suiteId) {
  return request({
    url: `/testsuite/${suiteId}/steps`,
    method: 'get'
  })
}

/**
 * 更新套件用例执行顺序
 */
export function updateCaseOrder(data) {
  return request({
    url: '/testsuite/cases/order',
    method: 'put',
    data
  })
}
