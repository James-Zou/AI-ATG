import request from './request'

/**
 * 创建项目
 */
export function createProject(data) {
  return request({
    url: '/project',
    method: 'post',
    data
  })
}

/**
 * 更新项目
 */
export function updateProject(id, data) {
  return request({
    url: `/project/${id}`,
    method: 'put',
    data
  })
}

/**
 * 获取项目详情
 */
export function getProject(id) {
  return request({
    url: `/project/${id}`,
    method: 'get'
  })
}

/**
 * 获取项目列表
 */
export function getProjectList(params) {
  return request({
    url: '/project/list',
    method: 'get',
    params
  })
}

/**
 * 删除项目
 */
export function deleteProject(id) {
  return request({
    url: `/project/${id}`,
    method: 'delete'
  })
}

/**
 * 添加项目成员
 */
export function addMember(projectId, data) {
  return request({
    url: `/project/${projectId}/members`,
    method: 'post',
    data
  })
}

/**
 * 移除项目成员
 */
export function removeMember(projectId, userId) {
  return request({
    url: `/project/${projectId}/members/${userId}`,
    method: 'delete'
  })
}

/**
 * 获取项目成员列表
 */
export function getProjectMembers(projectId) {
  return request({
    url: `/project/${projectId}/members`,
    method: 'get'
  })
}
