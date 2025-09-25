package org.example.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    private String uploadDir;

    @PostConstruct
    public void initUploadDir() {
        // 优先使用根目录静态资源库
        String[] possibleUploadPaths = {
            "E:\\code11\\uploads",       // 绝对路径（根目录）
            System.getProperty("user.dir") + "/../uploads",  // 从untitled目录到上级uploads
            "../uploads",                // 上级目录uploads
            System.getProperty("user.dir") + "/uploads",     // 当前目录下的uploads
            "./uploads"                  // 相对路径uploads
        };

        for (String path : possibleUploadPaths) {
            File testDir = new File(path);
            if (testDir.exists() && testDir.isDirectory()) {
                uploadDir = testDir.getAbsolutePath();
                System.out.println("✅ FileStorageService使用uploads目录: " + uploadDir);
                return;
            }
        }

        // 如果都不存在，创建根目录
        uploadDir = "E:\\code11\\uploads";
        File rootDir = new File(uploadDir);
        if (!rootDir.exists()) {
            rootDir.mkdirs();
            System.out.println("📁 FileStorageService创建根目录uploads: " + uploadDir);
        }
        System.out.println("✅ FileStorageService使用默认uploads目录: " + uploadDir);
    }

    @Value("${app.upload.max-file-size:10485760}") // 10MB
    private long maxFileSize;

    // 支持的图片格式
    private static final List<String> IMAGE_EXTENSIONS = Arrays.asList(
        "jpg", "jpeg", "png", "gif", "bmp", "webp"
    );

    // 支持的音频格式
    private static final List<String> AUDIO_EXTENSIONS = Arrays.asList(
        "mp3", "wav", "ogg", "m4a", "aac", "flac"
    );

    // 支持的视频格式
    private static final List<String> VIDEO_EXTENSIONS = Arrays.asList(
        "mp4", "avi", "mov", "wmv", "flv", "webm", "mkv"
    );

    // 支持的文档格式
    private static final List<String> DOCUMENT_EXTENSIONS = Arrays.asList(
        "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "rtf"
    );

    /**
     * 上传文件
     */
    public FileUploadResult uploadFile(MultipartFile file, String category) throws IOException {
        // 验证文件
        validateFile(file);

        // 获取文件扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        
        // 确定文件类型
        String fileType = determineFileType(extension);
        
        // 生成唯一文件名
        String fileName = generateFileName(extension);
        
        // 创建目录结构
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Path categoryPath = Paths.get(uploadDir, category, fileType, datePath);
        Files.createDirectories(categoryPath);
        
        // 保存文件
        Path filePath = categoryPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // 生成访问URL
        String fileUrl = String.format("/uploads/%s/%s/%s/%s", category, fileType, datePath, fileName);
        
        return new FileUploadResult(
            fileName,
            originalFilename,
            fileUrl,
            filePath.toString(),
            file.getSize(),
            fileType,
            extension
        );
    }

    /**
     * 上传聊天文件（图片、语音、文件）
     */
    public FileUploadResult uploadChatFile(MultipartFile file) throws IOException {
        return uploadFile(file, "chat");
    }

    /**
     * 上传表情图片
     */
    public FileUploadResult uploadEmojiImage(MultipartFile file) throws IOException {
        // 表情只允许图片格式
        String extension = getFileExtension(file.getOriginalFilename());
        if (!IMAGE_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("表情只支持图片格式");
        }

        return uploadFile(file, "emoji");
    }

    /**
     * 上传头像
     */
    public FileUploadResult uploadAvatar(MultipartFile file) throws IOException {
        // 头像只允许图片格式
        String extension = getFileExtension(file.getOriginalFilename());
        if (!IMAGE_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("头像只支持图片格式");
        }
        
        return uploadFile(file, "avatar");
    }

    /**
     * 上传视频
     */
    public FileUploadResult uploadVideo(MultipartFile file) throws IOException {
        String extension = getFileExtension(file.getOriginalFilename());
        if (!VIDEO_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("不支持的视频格式");
        }
        
        return uploadFile(file, "video");
    }

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("文件大小不能超过 " + (maxFileSize / 1024 / 1024) + "MB");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * 确定文件类型
     */
    private String determineFileType(String extension) {
        String ext = extension.toLowerCase();
        
        if (IMAGE_EXTENSIONS.contains(ext)) {
            return "image";
        } else if (AUDIO_EXTENSIONS.contains(ext)) {
            return "audio";
        } else if (VIDEO_EXTENSIONS.contains(ext)) {
            return "video";
        } else if (DOCUMENT_EXTENSIONS.contains(ext)) {
            return "document";
        } else {
            return "other";
        }
    }

    /**
     * 生成唯一文件名
     */
    private String generateFileName(String extension) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String timestamp = String.valueOf(System.currentTimeMillis());
        return uuid + "_" + timestamp + "." + extension;
    }

    /**
     * 删除文件
     */
    public boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 文件上传结果
     */
    public static class FileUploadResult {
        private String fileName;
        private String originalFileName;
        private String fileUrl;
        private String filePath;
        private long fileSize;
        private String fileType;
        private String extension;

        public FileUploadResult(String fileName, String originalFileName, String fileUrl,
                              String filePath, long fileSize, String fileType, String extension) {
            this.fileName = fileName;
            this.originalFileName = originalFileName;
            this.fileUrl = fileUrl;
            this.filePath = filePath;
            this.fileSize = fileSize;
            this.fileType = fileType;
            this.extension = extension;
        }

        // Getters
        public String getFileName() { return fileName; }
        public String getOriginalFileName() { return originalFileName; }
        public String getUrl() { return fileUrl; }
        public String getFileUrl() { return fileUrl; }
        public String getFilePath() { return filePath; }
        public long getFileSize() { return fileSize; }
        public String getFileType() { return fileType; }
        public String getExtension() { return extension; }
        public String getThumbnailUrl() { return fileUrl; } // 简化实现，实际项目中可以生成缩略图
    }
}
