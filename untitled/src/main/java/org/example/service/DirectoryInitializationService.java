package org.example.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class DirectoryInitializationService {

    @Value("${app.upload.path:E:/code11/uploads}")
    private String baseUploadPath;

    /**
     * 应用启动时初始化必要的目录
     */
    @PostConstruct
    public void initializeDirectories() {
        System.out.println("🚀 开始初始化上传目录...");
        
        try {
            // 创建基础上传目录
            createDirectoryIfNotExists(baseUploadPath);
            
            // 创建音乐相关目录
            createDirectoryIfNotExists(Paths.get(baseUploadPath, "music").toString());
            createDirectoryIfNotExists(Paths.get(baseUploadPath, "covers").toString());
            
            // 创建其他必要目录
            createDirectoryIfNotExists(Paths.get(baseUploadPath, "videos").toString());
            createDirectoryIfNotExists(Paths.get(baseUploadPath, "thumbnails").toString());
            createDirectoryIfNotExists(Paths.get(baseUploadPath, "avatars").toString());
            createDirectoryIfNotExists(Paths.get(baseUploadPath, "temp").toString());
            
            // 创建聊天相关目录
            createDirectoryIfNotExists(Paths.get(baseUploadPath, "chat", "images").toString());
            createDirectoryIfNotExists(Paths.get(baseUploadPath, "chat", "videos").toString());
            createDirectoryIfNotExists(Paths.get(baseUploadPath, "chat", "voices").toString());
            createDirectoryIfNotExists(Paths.get(baseUploadPath, "chat", "files").toString());
            
            System.out.println("✅ 目录初始化完成！基础路径: " + baseUploadPath);
            
        } catch (Exception e) {
            System.err.println("❌ 目录初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 创建目录（如果不存在）
     */
    private void createDirectoryIfNotExists(String path) {
        try {
            Path dirPath = Paths.get(path);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
                System.out.println("📁 创建目录: " + path);
            }
        } catch (Exception e) {
            System.err.println("❌ 创建目录失败 " + path + ": " + e.getMessage());
        }
    }
} 