import request from './request'

/**
 * 用户注册
 */
export function register(data) {
  return request({
    url: '/user/register',
    method: 'post',
    data
  })
}

/**
 * 用户登录
 */
export function login(data) {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}

/**
 * 获取用户信息
 */
export function getUserInfo(id) {
  return request({
    url: `/user/${id}`,
    method: 'get'
  })
}

/**
 * 获取用户列表
 */
export function getUserList() {
  return request({
    url: '/user/list',
    method: 'get'
  })
}

/**
 * 更新用户信息
 */
export function updateUser(id, data) {
  return request({
    url: `/user/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除用户
 */
export function deleteUser(id) {
  return request({
    url: `/user/${id}`,
    method: 'delete'
  })
}
