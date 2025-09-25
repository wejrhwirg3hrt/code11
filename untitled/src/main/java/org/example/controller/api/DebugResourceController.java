package org.example.controller.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 调试资源访问控制器
 */
@RestController
@RequestMapping("/api/debug")
public class DebugResourceController {

    @Value("${app.upload.path:./uploads}")
    private String uploadPath;

    /**
     * 检查资源路径配置
     */
    @GetMapping("/resource-config")
    public ResponseEntity<Map<String, Object>> getResourceConfig() {
        Map<String, Object> config = new HashMap<>();
        
        String absoluteUploadPath = Paths.get(uploadPath).toAbsolutePath().toString();
        config.put("uploadPath", uploadPath);
        config.put("absoluteUploadPath", absoluteUploadPath);
        
        // 检查目录是否存在
        File uploadDir = new File(absoluteUploadPath);
        config.put("uploadDirExists", uploadDir.exists());
        config.put("uploadDirCanRead", uploadDir.canRead());
        
        // 检查子目录
        File thumbnailsDir = new File(absoluteUploadPath, "thumbnails");
        config.put("thumbnailsDirExists", thumbnailsDir.exists());
        config.put("thumbnailsDirCanRead", thumbnailsDir.canRead());
        
        File videosDir = new File(absoluteUploadPath, "videos");
        config.put("videosDirExists", videosDir.exists());
        config.put("videosDirCanRead", videosDir.canRead());
        
        // 列出一些文件
        if (thumbnailsDir.exists()) {
            File[] thumbnailFiles = thumbnailsDir.listFiles();
            if (thumbnailFiles != null && thumbnailFiles.length > 0) {
                config.put("sampleThumbnail", thumbnailFiles[0].getName());
            }
        }
        
        return ResponseEntity.ok(config);
    }

    /**
     * 直接访问文件测试
     */
    @GetMapping("/test-file")
    public ResponseEntity<Resource> testFile(@RequestParam String filename) {
        try {
            String absoluteUploadPath = Paths.get(uploadPath).toAbsolutePath().toString();
            Path filePath = Paths.get(absoluteUploadPath, "thumbnails", filename);
            
            File file = filePath.toFile();
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
