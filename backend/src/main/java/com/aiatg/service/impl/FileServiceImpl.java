package com.aiatg.service.impl;

import cn.hutool.core.util.IdUtil;
import com.aiatg.service.FileService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * 文件服务实现类
 */
@Service
public class FileServiceImpl implements FileService {
    
    @Value("${minio.endpoint:http://localhost:9000}")
    private String endpoint;
    
    @Value("${minio.accessKey:admin}")
    private String accessKey;
    
    @Value("${minio.secretKey:Aiatg123456}")
    private String secretKey;
    
    @Value("${minio.bucketName:ai-atg}")
    private String bucketName;
    
    @Override
    public String uploadFile(MultipartFile file, String folder) {
        try {
            // 创建MinIO客户端
            MinioClient minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : "";
            String fileName = folder + "/" + IdUtil.simpleUUID() + extension;
            
            // 上传文件
            InputStream inputStream = file.getInputStream();
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );
            inputStream.close();
            
            // 返回访问URL
            return endpoint + "/" + bucketName + "/" + fileName;
            
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }
    
    @Override
    public void deleteFile(String fileUrl) {
        try {
            // 从URL中提取文件路径
            String objectName = fileUrl.substring(fileUrl.indexOf(bucketName) + bucketName.length() + 1);
            
            // 创建MinIO客户端
            MinioClient minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
            
            // 删除文件
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build()
            );
            
        } catch (Exception e) {
            throw new RuntimeException("文件删除失败: " + e.getMessage());
        }
    }
}
