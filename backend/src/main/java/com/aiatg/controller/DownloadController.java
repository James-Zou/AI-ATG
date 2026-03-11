package com.aiatg.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件下载控制器
 * 注意：此控制器不使用全局 context-path，直接响应 /downloads 路径
 */
@Slf4j
@RestController
@CrossOrigin
public class DownloadController {
    
    /**
     * 下载文件
     */
    @GetMapping("/downloads/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        try {
            String workDir = System.getProperty("user.dir");
            log.info("当前工作目录: {}", workDir);
            
            // 尝试多个可能的路径
            Path filePath = null;
            File file = null;
            
            // 路径1: 相对于当前工作目录的 downloads
            Path path1 = Paths.get(workDir, "downloads", filename);
            if (path1.toFile().exists()) {
                filePath = path1;
                file = path1.toFile();
                log.info("在路径1找到文件: {}", path1);
            }
            
            // 路径2: 相对于当前工作目录的 backend/downloads
            if (file == null || !file.exists()) {
                Path path2 = Paths.get(workDir, "backend", "downloads", filename);
                if (path2.toFile().exists()) {
                    filePath = path2;
                    file = path2.toFile();
                    log.info("在路径2找到文件: {}", path2);
                }
            }
            
            // 路径3: 绝对路径（项目固定位置）
            if (file == null || !file.exists()) {
                Path path3 = Paths.get("/Users/roderickzou/Desktop/AI-ATG/backend/downloads", filename);
                if (path3.toFile().exists()) {
                    filePath = path3;
                    file = path3.toFile();
                    log.info("在路径3找到文件: {}", path3);
                }
            }
            
            if (!file.exists()) {
                log.warn("文件不存在: {}", filename);
                return ResponseEntity.notFound().build();
            }
            
            if (!file.canRead()) {
                log.error("文件不可读: {}", filename);
                return ResponseEntity.status(403).build();
            }
            
            // 创建资源
            Resource resource = new FileSystemResource(file);
            
            // 确定内容类型
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                if (filename.endsWith(".zip")) {
                    contentType = "application/zip";
                } else if (filename.endsWith(".tar.gz") || filename.endsWith(".tgz")) {
                    contentType = "application/gzip";
                } else {
                    contentType = "application/octet-stream";
                }
            }
            
            log.info("下载文件: {} ({})", filename, contentType);
            
            // 返回文件
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + filename + "\"")
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("下载文件失败: {}", filename, e);
            return ResponseEntity.status(500).build();
        }
    }
}
