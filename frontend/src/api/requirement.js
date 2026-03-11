import request from './request'

/**
 * 创建需求
 */
export function createRequirement(data) {
  return request({
    url: '/requirement',
    method: 'post',
    data
  })
}

/**
 * 更新需求
 */
export function updateRequirement(id, data) {
  return request({
    url: `/requirement/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除需求
 */
export function deleteRequirement(id) {
  return request({
    url: `/requirement/${id}`,
    method: 'delete'
  })
}

/**
 * 获取需求详情
 */
export function getRequirementDetail(id) {
  return request({
    url: `/requirement/${id}`,
    method: 'get'
  })
}

/**
 * 获取需求列表
 */
export function getRequirementList(params) {
  return request({
    url: '/requirement/list',
    method: 'get',
    params
  })
}

/**
 * 更新需求状态
 */
export function updateRequirementStatus(id, status) {
  return request({
    url: `/requirement/${id}/status`,
    method: 'put',
    params: { status }
  })
}

/**
 * 上传文件
 */
export function uploadFile(file, folder = 'requirements') {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('folder', folder)
  
  return request({
    url: '/file/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
