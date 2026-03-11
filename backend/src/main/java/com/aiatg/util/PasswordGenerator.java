package com.aiatg.util;

import cn.hutool.crypto.digest.BCrypt;

/**
 * 密码生成工具类
 * 用于生成和验证 BCrypt 密码哈希
 */
public class PasswordGenerator {
    
    /**
     * 生成密码哈希
     */
    public static String generateHash(String password) {
        return BCrypt.hashpw(password);
    }
    
    /**
     * 验证密码
     */
    public static boolean verify(String password, String hash) {
        return BCrypt.checkpw(password, hash);
    }
    
    /**
     * 主方法 - 用于生成密码
     * 使用方式: mvn exec:java -Dexec.mainClass="com.aiatg.util.PasswordGenerator" -Dexec.args="Admin@123"
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("用法: java PasswordGenerator <password>");
            System.out.println("\n=== 生成默认密码 ===");
            
            // 生成常用密码
            String[] passwords = {"admin123", "Admin@123", "123456"};
            for (String pwd : passwords) {
                String hash = generateHash(pwd);
                System.out.println("\n密码: " + pwd);
                System.out.println("哈希: " + hash);
                System.out.println("SQL: UPDATE user SET password = '" + hash + "' WHERE username = 'admin';");
            }
            
            // 验证数据库中的密码
            System.out.println("\n=== 验证数据库密码 ===");
            String dbHash = "$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHODYRz6zXPL6EauAy";
            System.out.println("数据库中的密码哈希: " + dbHash);
            
            for (String pwd : passwords) {
                boolean result = verify(pwd, dbHash);
                System.out.println("密码 '" + pwd + "' 验证结果: " + (result ? "✅ 匹配" : "❌ 不匹配"));
            }
            
            return;
        }
        
        String password = args[0];
        String hash = generateHash(password);
        
        System.out.println("\n=== 密码生成结果 ===");
        System.out.println("原始密码: " + password);
        System.out.println("BCrypt哈希: " + hash);
        System.out.println("\nSQL更新语句:");
        System.out.println("UPDATE user SET password = '" + hash + "' WHERE username = 'admin';");
    }
}
