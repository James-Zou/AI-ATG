import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建 axios 实例
const request = axios.create({
  baseURL: '/api',
  timeout: 30000, // 增加到30秒，避免大数据量查询超时
  withCredentials: true // 启用 Cookie，支持 Session/Cookie 认证
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // Session/Cookie 认证不需要手动设置 Authorization header
    // Cookie 会自动随请求发送
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    
    // 如果返回的状态码不是 200，则显示错误信息
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      
      // 401: 未授权，跳转到登录页
      if (res.code === 401) {
        // Session 过期或未登录，清除本地用户信息
        localStorage.removeItem('userInfo')
        window.location.href = '/login'
      }
      
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    
    return res
  },
  error => {
    console.error('响应错误:', error)
    
    // 处理不同类型的错误
    let errorMessage = '网络错误'
    
    if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
      errorMessage = '请求超时，请检查网络连接或稍后重试'
    } else if (error.response) {
      // 服务器返回了错误状态码
      const status = error.response.status
      
      // 401: Session 过期或未登录
      if (status === 401) {
        localStorage.removeItem('userInfo')
        window.location.href = '/login'
        errorMessage = '登录已过期，请重新登录'
      } else {
        errorMessage = error.response.data?.message || `服务器错误: ${status}`
      }
    } else if (error.request) {
      // 请求已发出但没有收到响应
      errorMessage = '服务器无响应，请检查网络连接'
    } else {
      errorMessage = error.message || '请求失败'
    }
    
    ElMessage.error(errorMessage)
    return Promise.reject(error)
  }
)

export default request
