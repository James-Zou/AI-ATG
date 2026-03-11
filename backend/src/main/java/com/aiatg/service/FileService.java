package com.aiatg.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务接口
 */
public interface FileService {
    
    /**
     * 上传文件到MinIO
     * @param file 文件
     * @param folder 文件夹（如：requirements, testcases）
     * @return 文件访问URL
     */
    String uploadFile(MultipartFile file, String folder);
    
    /**
     * 删除文件
     * @param fileUrl 文件URL
     */
    void deleteFile(String fileUrl);
}
