import request from './request'

/**
 * 发送聊天消息并分析指令
 */
export function sendChatMessage(data) {
  return request({
    url: '/atgbot/chat',
    method: 'post',
    data
  })
}

/**
 * 执行测试套件
 */
export function executeTestSuite(suiteId) {
  return request({
    url: `/atgbot/execute/${suiteId}`,
    method: 'post'
  })
}

/**
 * 获取聊天历史
 */
export function getChatHistory(params) {
  return request({
    url: '/atgbot/history',
    method: 'get',
    params
  })
}
