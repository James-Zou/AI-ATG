import request from './request'

/**
 * 创建测试用例
 */
export function createTestCase(data) {
  return request({
    url: '/testcase',
    method: 'post',
    data
  })
}

/**
 * 更新测试用例
 */
export function updateTestCase(id, data) {
  return request({
    url: `/testcase/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除测试用例
 */
export function deleteTestCase(id) {
  return request({
    url: `/testcase/${id}`,
    method: 'delete'
  })
}

/**
 * 获取测试用例详情
 */
export function getTestCaseDetail(id) {
  return request({
    url: `/testcase/${id}`,
    method: 'get'
  })
}

/**
 * 获取测试用例列表
 */
export function getTestCaseList(params) {
  return request({
    url: '/testcase/list',
    method: 'get',
    params
  })
}

/**
 * 更新测试用例状态
 */
export function updateTestCaseStatus(id, status) {
  return request({
    url: `/testcase/${id}/status`,
    method: 'put',
    params: { status }
  })
}

/**
 * 批量删除测试用例
 */
export function batchDeleteTestCase(ids) {
  return request({
    url: '/testcase/batch',
    method: 'delete',
    data: ids
  })
}

/**
 * 导入测试用例
 */
export function importTestCases(file, projectId) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('projectId', projectId)
  
  return request({
    url: '/testcase/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 导出测试用例
 */
export function exportTestCases(params) {
  return request({
    url: '/testcase/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}
