package com.aiatg.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * AES 加密解密工具类
 * 用于 API Key 的加密存储和解密使用
 * 
 * @author AI-ATG
 * @date 2024
 */
@Component
public class AesUtil {

    /**
     * 密钥（32字节 = 256位）- 从配置文件读取
     */
    @Value("${encryption.secret-key}")
    private String configSecretKey;
    
    /**
     * 初始化向量（16字节 = 128位）- 从配置文件读取
     */
    @Value("${encryption.iv}")
    private String configIv;
    
    /**
     * 静态密钥实例（初始化后赋值）
     */
    private static String SECRET_KEY;
    
    /**
     * 静态 IV 实例（初始化后赋值）
     */
    private static String IV;
    
    /**
     * 加密算法
     */
    private static final String ALGORITHM = "AES";
    
    /**
     * 加密模式和填充方式
     */
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";

    /**
     * Spring Bean 初始化后，将配置值赋给静态变量
     */
    @PostConstruct
    public void init() {
        SECRET_KEY = this.configSecretKey;
        IV = this.configIv;
    }

    /**
     * 加密 API Key
     * 
     * @param plainText 明文
     * @return Base64 编码的密文
     * @throws Exception 加密失败时抛出异常
     */
    public static String encrypt(String plainText) throws Exception {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }

        try {
            // 创建密钥
            SecretKeySpec keySpec = new SecretKeySpec(
                SECRET_KEY.getBytes(StandardCharsets.UTF_8), 
                ALGORITHM
            );
            
            // 创建初始化向量
            IvParameterSpec ivSpec = new IvParameterSpec(
                IV.getBytes(StandardCharsets.UTF_8)
            );
            
            // 创建加密器
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            
            // 执行加密
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            
            // Base64 编码返回
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new Exception("API Key 加密失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解密 API Key
     * 
     * @param cipherText Base64 编码的密文
     * @return 明文
     * @throws Exception 解密失败时抛出异常
     */
    public static String decrypt(String cipherText) throws Exception {
        if (cipherText == null || cipherText.isEmpty()) {
            return cipherText;
        }

        try {
            // 创建密钥
            SecretKeySpec keySpec = new SecretKeySpec(
                SECRET_KEY.getBytes(StandardCharsets.UTF_8), 
                ALGORITHM
            );
            
            // 创建初始化向量
            IvParameterSpec ivSpec = new IvParameterSpec(
                IV.getBytes(StandardCharsets.UTF_8)
            );
            
            // 创建解密器
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            
            // Base64 解码
            byte[] cipherBytes = Base64.getDecoder().decode(cipherText);
            
            // 执行解密
            byte[] decrypted = cipher.doFinal(cipherBytes);
            
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new Exception("API Key 解密失败: " + e.getMessage(), e);
        }
    }

    /**
     * 脱敏显示 API Key
     * 
     * @param apiKey API Key
     * @param showLength 前后各显示的字符数
     * @return 脱敏后的字符串
     */
    public static String maskApiKey(String apiKey, int showLength) {
        if (apiKey == null || apiKey.length() <= showLength * 2) {
            return "******";
        }
        
        String start = apiKey.substring(0, showLength);
        String end = apiKey.substring(apiKey.length() - showLength);
        int maskLength = Math.max(6, apiKey.length() - showLength * 2);
        
        // Java 8 兼容的字符串重复实现
        StringBuilder mask = new StringBuilder();
        for (int i = 0; i < maskLength; i++) {
            mask.append("*");
        }
        
        return start + mask.toString() + end;
    }

    /**
     * 脱敏显示 API Key（默认显示前后各4个字符）
     * 
     * @param apiKey API Key
     * @return 脱敏后的字符串
     */
    public static String maskApiKey(String apiKey) {
        return maskApiKey(apiKey, 4);
    }
}
