package org.example.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Transactional(readOnly = false)
public class FileUploadService {

    private String uploadPath;

    @PostConstruct
    public void initUploadPath() {
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
                uploadPath = testDir.getAbsolutePath();
                System.out.println("✅ FileUploadService使用uploads目录: " + uploadPath);
                return;
            }
        }

        // 如果都不存在，创建根目录
        uploadPath = "E:\\code11\\uploads";
        File rootDir = new File(uploadPath);
        if (!rootDir.exists()) {
            rootDir.mkdirs();
            System.out.println("📁 创建根目录uploads: " + uploadPath);
        }
        System.out.println("✅ FileUploadService使用默认uploads目录: " + uploadPath);
    }

    @Value("${app.upload.max-file-size:524288000}") // 500MB
    private long maxFileSize;

    /**
     * 上传头像
     */
    public String uploadAvatar(MultipartFile file) throws IOException {
        validateImageFile(file);
        return uploadFileToSubDir(file, "avatars");
    }

    /**
     * 上传视频缩略图
     */
    public String uploadThumbnail(MultipartFile file) throws IOException {
        validateImageFile(file);
        return uploadFileToSubDir(file, "thumbnails");
    }

    /**
     * 上传视频文件
     */
    public String uploadVideo(MultipartFile file) throws IOException {
        validateVideoFile(file);
        return uploadFileToSubDir(file, "videos");
    }

    /**
     * 上传聊天图片
     */
    public String uploadImage(MultipartFile file) throws IOException {
        validateImageFile(file);
        return uploadFileToSubDir(file, "chat/images");
    }

    /**
     * 上传聊天文件
     */
    public String uploadChatFile(MultipartFile file) throws IOException {
        validateChatFile(file);
        return uploadFileToSubDir(file, "chat/files");
    }

    /**
     * 上传语音文件
     */
    public String uploadVoice(MultipartFile file) throws IOException {
        validateVoiceFile(file);
        return uploadFileToSubDir(file, "chat/voices");
    }

    /**
     * 生成缩略图
     */
    public String generateThumbnail(String imageUrl) {
        // 简单实现，返回原图片URL
        // 在实际项目中可以使用图片处理库生成真正的缩略图
        return imageUrl;
    }

    /**
     * 上传内容图片
     */
    public String uploadContentImage(MultipartFile file) throws IOException {
        validateImageFile(file);
        return uploadFileToSubDir(file, "content/images");
    }

    /**
     * 上传内容视频
     */
    public String uploadContentVideo(MultipartFile file) throws IOException {
        validateVideoFile(file);
        return uploadFileToSubDir(file, "content/videos");
    }

    /**
     * 上传内容音频
     */
    public String uploadContentAudio(MultipartFile file) throws IOException {
        validateAudioFile(file);
        return uploadFileToSubDir(file, "content/audios");
    }

    /**
     * 通用文件上传方法（保持向后兼容）
     */
    public String uploadFile(MultipartFile file) throws IOException {
        return uploadFileToSubDir(file, "general");
    }


    /**
     * 上传文件到指定子目录
     */
    private String uploadFileToSubDir(MultipartFile file, String subDir) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("文件为空");
        }

        // 按文件类型分类存储，不再按日期分类
        Path uploadDir = Paths.get(uploadPath, subDir);

        if (!Files.exists(uploadDir)) {
            System.out.println("创建上传目录: " + uploadDir.toAbsolutePath());
            Files.createDirectories(uploadDir);
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String fileName = UUID.randomUUID().toString() + extension;

        // 保存文件
        Path filePath = uploadDir.resolve(fileName);
        System.out.println("保存文件到: " + filePath.toAbsolutePath());
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // 返回相对路径，用于访问
        return "/uploads/" + subDir + "/" + fileName;
    }

    /**
     * 验证图片文件
     */
    private void validateImageFile(MultipartFile file) {
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("文件大小不能超过 " + (maxFileSize / 1024 / 1024) + "MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("只支持图片文件");
        }

        String[] allowedTypes = {"image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"};
        boolean isAllowed = false;
        for (String type : allowedTypes) {
            if (type.equals(contentType)) {
                isAllowed = true;
                break;
            }
        }

        if (!isAllowed) {
            throw new IllegalArgumentException("只支持 JPEG、PNG、GIF、WebP 格式的图片");
        }
    }

    /**
     * 验证视频文件
     */
    private void validateVideoFile(MultipartFile file) {
        if (file.getSize() > maxFileSize) { // 视频文件限制500MB
            throw new IllegalArgumentException("视频文件大小不能超过 500MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("video/")) {
            throw new IllegalArgumentException("只支持视频文件");
        }

        String[] allowedTypes = {"video/mp4", "video/avi", "video/mov", "video/wmv", "video/webm"};
        boolean isAllowed = false;
        for (String type : allowedTypes) {
            if (type.equals(contentType)) {
                isAllowed = true;
                break;
            }
        }

        if (!isAllowed) {
            throw new IllegalArgumentException("只支持 MP4、AVI、MOV、WMV、WebM 格式的视频");
        }
    }

    /**
     * 上传通用文件（文档、压缩包等）
     */
    public String uploadDocument(MultipartFile file) throws IOException {
        validateDocumentFile(file);
        return uploadFileToSubDir(file, "documents");
    }

    /**
     * 验证文档文件类型
     */
    private void validateDocumentFile(MultipartFile file) {
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();

        if (contentType == null || fileName == null) {
            throw new IllegalArgumentException("无效的文件");
        }

        // 支持的文档类型
        String[] allowedTypes = {
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "text/plain",
            "application/zip",
            "application/x-rar-compressed",
            "application/x-7z-compressed"
        };

        // 支持的文件扩展名
        String[] allowedExtensions = {
            ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx",
            ".txt", ".zip", ".rar", ".7z"
        };

        boolean isAllowed = false;

        // 检查MIME类型
        for (String type : allowedTypes) {
            if (type.equals(contentType)) {
                isAllowed = true;
                break;
            }
        }

        // 如果MIME类型检查失败，检查文件扩展名
        if (!isAllowed) {
            String lowerFileName = fileName.toLowerCase();
            for (String ext : allowedExtensions) {
                if (lowerFileName.endsWith(ext)) {
                    isAllowed = true;
                    break;
                }
            }
        }

        if (!isAllowed) {
            throw new IllegalArgumentException("只支持 PDF、Word、Excel、PowerPoint、文本文件和压缩包");
        }

        // 检查文件大小（最大50MB）
        if (file.getSize() > 50 * 1024 * 1024) {
            throw new IllegalArgumentException("文件大小不能超过50MB");
        }
    }

    /**
     * 验证聊天文件
     */
    private void validateChatFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("文件大小不能超过 " + (maxFileSize / 1024 / 1024) + "MB");
        }

        // 检查文件扩展名（基本安全检查）
        String fileName = file.getOriginalFilename();
        if (fileName != null) {
            String extension = getFileExtension(fileName).toLowerCase();
            String[] dangerousExtensions = {".exe", ".bat", ".cmd", ".scr", ".pif", ".jar", ".com", ".vbs", ".js"};
            for (String dangerous : dangerousExtensions) {
                if (extension.equals(dangerous)) {
                    throw new IllegalArgumentException("不允许上传可执行文件");
                }
            }
        }
    }

    /**
     * 验证语音文件
     */
    private void validateVoiceFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("语音文件不能为空");
        }

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("语音文件大小不能超过 " + (maxFileSize / 1024 / 1024) + "MB");
        }

        String contentType = file.getContentType();
        if (contentType != null) {
            String[] allowedTypes = {"audio/mpeg", "audio/mp3", "audio/wav", "audio/ogg", "audio/m4a", "audio/aac"};
            boolean isAllowed = false;
            for (String type : allowedTypes) {
                if (contentType.toLowerCase().contains(type) || contentType.toLowerCase().contains("audio")) {
                    isAllowed = true;
                    break;
                }
            }
            if (!isAllowed) {
                throw new IllegalArgumentException("不支持的语音格式，支持的格式：MP3, WAV, OGG, M4A, AAC");
            }
        }
    }

    /**
     * 验证音频文件
     */
    private void validateAudioFile(MultipartFile file) {
        System.out.println("验证音频文件: " + file.getOriginalFilename() + ", 大小: " + file.getSize() + ", 类型: " + file.getContentType());

        if (file.isEmpty()) {
            throw new IllegalArgumentException("音频文件不能为空");
        }

        if (file.getSize() > maxFileSize * 5) { // 音频文件限制50MB
            throw new IllegalArgumentException("音频文件大小不能超过 50MB");
        }

        String contentType = file.getContentType();
        if (contentType != null) {
            String[] allowedTypes = {"audio/mpeg", "audio/mp3", "audio/wav", "audio/ogg", "audio/m4a", "audio/aac", "audio/flac"};
            boolean isAllowed = false;
            for (String type : allowedTypes) {
                if (contentType.toLowerCase().contains(type) || contentType.toLowerCase().contains("audio")) {
                    isAllowed = true;
                    break;
                }
            }
            if (!isAllowed) {
                throw new IllegalArgumentException("不支持的音频格式，支持的格式：MP3, WAV, OGG, M4A, AAC, FLAC");
            }
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }

    /**
     * 删除文件
     */
    public boolean deleteFile(String filePath) {
        try {
            if (filePath != null && filePath.startsWith("/uploads/")) {
                Path path = Paths.get(uploadPath + filePath.substring("/uploads".length()));
                return Files.deleteIfExists(path);
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }
}