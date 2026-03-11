package com.aiatg.controller;

import com.aiatg.common.Result;
import com.aiatg.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件控制器
 */
@RestController
@RequestMapping("/file")
@CrossOrigin
public class FileController {
    
    @Autowired
    private FileService fileService;
    
    /**
     * 上传文件
     */
    @PostMapping("/upload")
    public Result<String> uploadFile(
        @RequestParam("file") MultipartFile file,
        @RequestParam(value = "folder", defaultValue = "common") String folder
    ) {
        try {
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }
            
            String fileUrl = fileService.uploadFile(file, folder);
            return Result.success("上传成功", fileUrl);
        } catch (Exception e) {
            return Result.error("上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除文件
     */
    @DeleteMapping
    public Result<Void> deleteFile(@RequestParam String fileUrl) {
        try {
            fileService.deleteFile(fileUrl);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }
}
