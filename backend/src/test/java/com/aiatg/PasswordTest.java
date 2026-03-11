package com.aiatg;

import cn.hutool.crypto.digest.BCrypt;
import org.junit.jupiter.api.Test;

/**
 * 密码加密测试
 */
public class PasswordTest {
    
    @Test
    public void testGeneratePassword() {
        String password = "Admin@123";
        
        // 使用 Hutool BCrypt 生成密码
        String hutoolHash = BCrypt.hashpw(password);
        System.out.println("Hutool BCrypt 生成的密码: " + hutoolHash);
        
        // 验证密码
        boolean hutoolCheck = BCrypt.checkpw(password, hutoolHash);
        System.out.println("Hutool BCrypt 验证结果: " + hutoolCheck);
        
        // 测试数据库中的密码
        String dbPassword = "$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHODYRz6zXPL6EauAy";
        boolean dbCheck = BCrypt.checkpw(password, dbPassword);
        System.out.println("数据库密码验证结果: " + dbCheck);
        
        // 测试其他可能的密码
        String[] testPasswords = {"admin123", "admin", "123456", "Admin123"};
        for (String testPwd : testPasswords) {
            boolean check = BCrypt.checkpw(testPwd, dbPassword);
            System.out.println("测试密码 '" + testPwd + "' 验证结果: " + check);
        }
    }
}
