package org.example.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.file.Paths;

@RestController
public class StaticResourceController {

    @Value("${app.upload.path:./uploads}")
    private String uploadPath;

    @GetMapping("/uploads/{folder}/{filename:.+}")
    public ResponseEntity<Resource> getUploadedFile(
            @PathVariable String folder,
            @PathVariable String filename) {

        try {
            // 确定正确的uploads路径
            String workingDir = System.getProperty("user.dir");
            String[] possibleBasePaths = {
                workingDir + "/../uploads",  // 从untitled目录到上级uploads
                workingDir + "/uploads",     // 当前目录下的uploads
                "./uploads",                 // 相对路径uploads
                "../uploads"                 // 上级目录uploads
            };

            File foundFile = null;
            for (String basePath : possibleBasePaths) {
                File testFile = new File(basePath, folder + File.separator + filename);
                System.out.println("尝试路径: " + testFile.getAbsolutePath());
                if (testFile.exists() && testFile.isFile()) {
                    foundFile = testFile;
                    System.out.println("✅ 找到文件: " + foundFile.getAbsolutePath());
                    break;
                }
            }

            if (foundFile == null) {
                System.err.println("❌ 文件未找到: " + folder + "/" + filename);
                System.err.println("工作目录: " + workingDir);
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(foundFile);

            // 根据文件扩展名设置Content-Type
            String contentType = getContentType(filename);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                    .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                    .body(resource);

        } catch (Exception e) {
            System.err.println("获取文件失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> getStaticImage(@PathVariable String filename) {
        try {
            // 首先尝试从classpath获取
            org.springframework.core.io.ClassPathResource classPathResource = 
                new org.springframework.core.io.ClassPathResource("static/images/" + filename);
            
            if (classPathResource.exists()) {
                String contentType = getContentType(filename);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, contentType)
                        .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                        .body(classPathResource);
            }
            
            // 如果classpath中没有，尝试从uploads目录获取
            return getUploadedFile("avatars", filename);
            
        } catch (Exception e) {
            System.err.println("获取图片失败: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    private String getContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        switch (extension) {
            case "jpg":
            case "jpeg":
                return MediaType.IMAGE_JPEG_VALUE;
            case "png":
                return MediaType.IMAGE_PNG_VALUE;
            case "gif":
                return MediaType.IMAGE_GIF_VALUE;
            case "svg":
                return "image/svg+xml";
            case "mp4":
                return "video/mp4";
            case "avi":
                return "video/x-msvideo";
            case "mov":
                return "video/quicktime";
            case "wmv":
                return "video/x-ms-wmv";
            default:
                return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
    }
}
