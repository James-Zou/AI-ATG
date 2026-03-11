/**
 * 本地测试服务工具类
 * 用于与本地轻量级测试服务通信
 */

const LOCAL_SERVICE_URL = 'http://localhost:9999';

class LocalTestService {
  /**
   * 检查服务是否运行
   */
  static async isRunning() {
    try {
      const response = await fetch(`${LOCAL_SERVICE_URL}/health`, {
        method: 'GET',
        timeout: 2000
      });
      
      if (response.ok) {
        const data = await response.json();
        return data.status === 'ok';
      }
      return false;
    } catch (e) {
      return false;
    }
  }
  
  /**
   * 获取服务状态
   */
  static async getStatus() {
    const response = await fetch(`${LOCAL_SERVICE_URL}/status`);
    return response.json();
  }
  
  /**
   * 获取服务配置
   */
  static async getConfig() {
    const response = await fetch(`${LOCAL_SERVICE_URL}/config`);
    return response.json();
  }
  
  /**
   * 更新服务配置
   */
  static async updateConfig(config) {
    const response = await fetch(`${LOCAL_SERVICE_URL}/config`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(config)
    });
    return response.json();
  }
  
  /**
   * 执行测试
   */
  static async executeTest(testCase) {
    const response = await fetch(`${LOCAL_SERVICE_URL}/execute`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ testCase })
    });
    return response.json();
  }
  
  /**
   * 停止当前测试
   */
  static async stopTest() {
    const response = await fetch(`${LOCAL_SERVICE_URL}/stop`, {
      method: 'POST'
    });
    return response.json();
  }
  
  /**
   * 获取下载链接
   */
  static getDownloadUrl() {
    return `${window.location.origin}/downloads/agent-service`;
  }
}

export default LocalTestService;
