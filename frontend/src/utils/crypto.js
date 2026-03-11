import CryptoJS from 'crypto-js'

/**
 * AES 加密工具类
 * 注意：密钥需要与后端 application.yml 中的配置保持一致
 *
 * 配置方式：
 * 1. 在项目根目录创建 .env 文件
 * 2. 添加以下环境变量：
 *    VITE_ENCRYPTION_SECRET_KEY=你的32字节密钥
 *    VITE_ENCRYPTION_IV=你的16字节初始化向量
 * 3. 重启开发服务器
 */

/**
 * 从环境变量读取密钥，如果未配置则使用默认值
 * 生产环境请务必在 .env 文件中配置 VITE_ENCRYPTION_SECRET_KEY
 */
const SECRET_KEY = import.meta.env.VITE_ENCRYPTION_SECRET_KEY || 'AI-ATG'

/**
 * 从环境变量读取初始化向量，如果未配置则使用默认值
 * 生产环境请务必在 .env 文件中配置 VITE_ENCRYPTION_IV
 */
const IV = import.meta.env.VITE_ENCRYPTION_IV || 'AI'

/**
 * 加密 API Key
 * @param {string} plainText - 明文
 * @returns {string} 加密后的密文（Base64编码）
 */
export function encryptApiKey(plainText) {
  if (!plainText) {
    return ''
  }

  try {
    const key = CryptoJS.enc.Utf8.parse(SECRET_KEY)
    const iv = CryptoJS.enc.Utf8.parse(IV)

    const encrypted = CryptoJS.AES.encrypt(plainText, key, {
      iv: iv,
      mode: CryptoJS.mode.CBC,
      padding: CryptoJS.pad.Pkcs7
    })

    return encrypted.toString()
  } catch (error) {
    console.error('加密失败:', error)
    throw new Error('API Key 加密失败')
  }
}

/**
 * 解密 API Key（前端一般不需要，但提供以备用）
 * @param {string} cipherText - 密文（Base64编码）
 * @returns {string} 解密后的明文
 */
export function decryptApiKey(cipherText) {
  if (!cipherText) {
    return ''
  }

  try {
    const key = CryptoJS.enc.Utf8.parse(SECRET_KEY)
    const iv = CryptoJS.enc.Utf8.parse(IV)

    const decrypted = CryptoJS.AES.decrypt(cipherText, key, {
      iv: iv,
      mode: CryptoJS.mode.CBC,
      padding: CryptoJS.pad.Pkcs7
    })

    return decrypted.toString(CryptoJS.enc.Utf8)
  } catch (error) {
    console.error('解密失败:', error)
    throw new Error('API Key 解密失败')
  }
}

/**
 * 脱敏显示 API Key
 * @param {string} apiKey - API Key
 * @param {number} showLength - 显示的字符数（前后各显示的字符数）
 * @returns {string} 脱敏后的字符串
 */
export function maskApiKey(apiKey, showLength = 4) {
  if (!apiKey || apiKey.length <= showLength * 2) {
    return '******'
  }

  const start = apiKey.substring(0, showLength)
  const end = apiKey.substring(apiKey.length - showLength)
  return `${start}${'*'.repeat(Math.max(6, apiKey.length - showLength * 2))}${end}`
}
