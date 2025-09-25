package org.example.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/diagnostic")
public class DiagnosticController {

    @Value("${app.upload.path:./uploads}")
    private String uploadPath;

    private final ResourceLoader resourceLoader;

    public DiagnosticController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @GetMapping("/static-resources")
    public ResponseEntity<Map<String, Object>> checkStaticResources() {
        Map<String, Object> result = new HashMap<>();
        
        // 检查上传路径
        String absoluteUploadPath = Paths.get(uploadPath).toAbsolutePath().toString();
        result.put("uploadPath", absoluteUploadPath);
        result.put("uploadPathExists", new File(absoluteUploadPath).exists());
        
        // 检查静态资源
        Map<String, Object> staticResources = new HashMap<>();
        
        // 检查classpath中的静态资源
        try {
            Resource defaultAvatar = resourceLoader.getResource("classpath:/static/images/default-avatar.png");
            staticResources.put("default-avatar.png", Map.of(
                "exists", defaultAvatar.exists(),
                "readable", defaultAvatar.isReadable(),
                "uri", defaultAvatar.getURI().toString()
            ));
        } catch (Exception e) {
            staticResources.put("default-avatar.png", Map.of("error", e.getMessage()));
        }
        
        // 检查上传目录中的文件
        Map<String, Object> uploadedFiles = new HashMap<>();
        
        File avatarFile = new File(absoluteUploadPath, "avatars/default.svg");
        uploadedFiles.put("avatars/default.svg", Map.of(
            "exists", avatarFile.exists(),
            "path", avatarFile.getAbsolutePath(),
            "size", avatarFile.exists() ? avatarFile.length() : 0
        ));
        
        File videoFile = new File(absoluteUploadPath, "videos/902f4722-8dcf-410e-9d8a-c0baf9491ab4.mp4");
        uploadedFiles.put("videos/xxx.mp4", Map.of(
            "exists", videoFile.exists(),
            "path", videoFile.getAbsolutePath(),
            "size", videoFile.exists() ? videoFile.length() : 0
        ));
        
        File thumbnailFile = new File(absoluteUploadPath, "thumbnails/8d6720f1-08fd-4481-9c50-0fc65311a7ad.jpeg");
        uploadedFiles.put("thumbnails/xxx.jpeg", Map.of(
            "exists", thumbnailFile.exists(),
            "path", thumbnailFile.getAbsolutePath(),
            "size", thumbnailFile.exists() ? thumbnailFile.length() : 0
        ));
        
        result.put("staticResources", staticResources);
        result.put("uploadedFiles", uploadedFiles);
        
        // 检查工作目录
        result.put("workingDirectory", System.getProperty("user.dir"));
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/paths")
    public ResponseEntity<Map<String, Object>> checkPaths() {
        Map<String, Object> result = new HashMap<>();
        
        // 当前工作目录
        result.put("workingDirectory", System.getProperty("user.dir"));
        
        // 上传路径配置
        result.put("configuredUploadPath", uploadPath);
        result.put("absoluteUploadPath", Paths.get(uploadPath).toAbsolutePath().toString());
        
        // 检查关键目录
        String[] directories = {
            "uploads",
            "uploads/avatars", 
            "uploads/videos",
            "uploads/thumbnails",
            "untitled/src/main/resources/static",
            "untitled/src/main/resources/static/images"
        };
        
        Map<String, Object> directoryStatus = new HashMap<>();
        for (String dir : directories) {
            File directory = new File(dir);
            directoryStatus.put(dir, Map.of(
                "exists", directory.exists(),
                "isDirectory", directory.isDirectory(),
                "absolutePath", directory.getAbsolutePath()
            ));
        }
        
        result.put("directories", directoryStatus);
        
        return ResponseEntity.ok(result);
    }
}
