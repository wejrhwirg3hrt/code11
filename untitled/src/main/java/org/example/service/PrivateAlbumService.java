package org.example.service;

import org.example.entity.PrivateAlbum;
import org.example.entity.PrivatePhoto;
import org.example.repository.PrivateAlbumRepository;
import org.example.repository.PrivatePhotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = false)
public class PrivateAlbumService {

    @Autowired
    private PrivateAlbumRepository albumRepository;

    @Autowired
    private PrivatePhotoRepository photoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String PRIVATE_PHOTO_DIR = "uploads/private/";
    private final String PRIVATE_THUMBNAIL_DIR = "uploads/private/thumbnails/";

    /**
     * 创建私密相册
     */
    public PrivateAlbum createAlbum(Long userId, String albumName, String password, String description) {
        // 检查相册名是否已存在
        Optional<PrivateAlbum> existing = albumRepository.findByUserIdAndAlbumName(userId, albumName);
        if (existing.isPresent()) {
            throw new RuntimeException("相册名已存在");
        }

        String cleanPassword = password != null ? password.trim() : "";
        String encodedPassword = passwordEncoder.encode(cleanPassword);

        // 调试信息
        System.out.println("=== 创建相册调试信息 ===");
        System.out.println("用户ID: " + userId);
        System.out.println("相册名: " + albumName);
        System.out.println("原始密码长度: " + (password != null ? password.length() : "null"));
        System.out.println("清理后密码长度: " + cleanPassword.length());
        System.out.println("原始密码: [" + password + "]");
        System.out.println("清理后密码: [" + cleanPassword + "]");
        System.out.println("加密后密码长度: " + encodedPassword.length());
        System.out.println("加密后密码前缀: " + encodedPassword.substring(0, Math.min(20, encodedPassword.length())));
        System.out.println("========================");

        PrivateAlbum album = new PrivateAlbum();
        album.setUserId(userId);
        album.setAlbumName(albumName);
        album.setPassword(encodedPassword);
        album.setDescription(description);
        album.setCreatedTime(LocalDateTime.now());
        album.setUpdatedTime(LocalDateTime.now());

        return albumRepository.save(album);
    }

    /**
     * 验证相册密码
     */
    public boolean verifyAlbumPassword(Long albumId, Long userId, String password) {
        Optional<PrivateAlbum> albumOpt = albumRepository.findByIdAndUserId(albumId, userId);
        if (albumOpt.isPresent()) {
            PrivateAlbum album = albumOpt.get();
            String inputPassword = password != null ? password.trim() : "";
            String storedPassword = album.getPassword();

            // 调试信息
            System.out.println("=== 密码验证调试信息 ===");
            System.out.println("相册ID: " + albumId);
            System.out.println("用户ID: " + userId);
            System.out.println("输入密码长度: " + inputPassword.length());
            System.out.println("存储密码长度: " + (storedPassword != null ? storedPassword.length() : "null"));
            System.out.println("输入密码: [" + inputPassword + "]");
            System.out.println("存储密码前缀: " + (storedPassword != null ? storedPassword.substring(0, Math.min(20, storedPassword.length())) : "null"));

            boolean matches = passwordEncoder.matches(inputPassword, storedPassword);
            System.out.println("密码匹配结果: " + matches);

            // 如果 BCrypt 验证失败，尝试直接比较（临时调试用）
            if (!matches) {
                System.out.println("BCrypt 验证失败，尝试直接比较...");
                boolean directMatch = inputPassword.equals(storedPassword);
                System.out.println("直接比较结果: " + directMatch);

                // 如果存储的密码看起来不像 BCrypt 格式，直接比较
                if (storedPassword != null && !storedPassword.startsWith("$2a$") && !storedPassword.startsWith("$2b$")) {
                    System.out.println("检测到非 BCrypt 格式密码，使用直接比较");
                    matches = directMatch;
                }
            }

            System.out.println("最终验证结果: " + matches);
            System.out.println("========================");

            return matches;
        }
        System.out.println("相册不存在: albumId=" + albumId + ", userId=" + userId);
        return false;
    }

    /**
     * 重置相册密码（临时调试用）
     */
    public boolean resetAlbumPassword(Long albumId, Long userId, String newPassword) {
        Optional<PrivateAlbum> albumOpt = albumRepository.findByIdAndUserId(albumId, userId);
        if (albumOpt.isPresent()) {
            PrivateAlbum album = albumOpt.get();
            String cleanPassword = newPassword != null ? newPassword.trim() : "";
            String encodedPassword = passwordEncoder.encode(cleanPassword);

            System.out.println("=== 重置密码调试信息 ===");
            System.out.println("相册ID: " + albumId);
            System.out.println("新密码: [" + cleanPassword + "]");
            System.out.println("加密后: " + encodedPassword);
            System.out.println("========================");

            album.setPassword(encodedPassword);
            albumRepository.save(album);
            return true;
        }
        return false;
    }

    /**
     * 获取用户的相册列表
     */
    public List<PrivateAlbum> getUserAlbums(Long userId) {
        return albumRepository.findByUserIdOrderByCreatedTimeDesc(userId);
    }

    /**
     * 上传照片到私密相册
     */
    public PrivatePhoto uploadPhoto(Long albumId, Long userId, MultipartFile file, String description) throws IOException {
        // 验证相册所有权
        Optional<PrivateAlbum> albumOpt = albumRepository.findByIdAndUserId(albumId, userId);
        if (!albumOpt.isPresent()) {
            throw new RuntimeException("相册不存在或无权限");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("只能上传图片文件");
        }

        // 创建目录
        Path photoDir = Paths.get(PRIVATE_PHOTO_DIR);
        Path thumbnailDir = Paths.get(PRIVATE_THUMBNAIL_DIR);
        if (!Files.exists(photoDir)) {
            Files.createDirectories(photoDir);
        }
        if (!Files.exists(thumbnailDir)) {
            Files.createDirectories(thumbnailDir);
        }

        // 生成文件名
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        String filePath = PRIVATE_PHOTO_DIR + fileName;
        String thumbnailPath = PRIVATE_THUMBNAIL_DIR + "thumb_" + fileName;

        // 保存原图
        Path targetPath = photoDir.resolve(fileName);
        Files.copy(file.getInputStream(), targetPath);

        // 生成缩略图
        generateThumbnail(targetPath.toString(), thumbnailDir.resolve("thumb_" + fileName).toString());

        // 获取图片尺寸
        BufferedImage image = ImageIO.read(targetPath.toFile());
        int width = image.getWidth();
        int height = image.getHeight();

        // 创建照片记录
        PrivatePhoto photo = new PrivatePhoto();
        photo.setAlbumId(albumId);
        photo.setFileName(fileName);
        photo.setFilePath(filePath);
        photo.setThumbnailPath(thumbnailPath);
        photo.setDescription(description);
        photo.setUploadTime(LocalDateTime.now());
        photo.setFileSize(file.getSize());
        photo.setWidth(width);
        photo.setHeight(height);

        // 更新相册时间
        PrivateAlbum album = albumOpt.get();
        album.setUpdatedTime(LocalDateTime.now());
        albumRepository.save(album);

        return photoRepository.save(photo);
    }

    /**
     * 获取相册中的照片
     */
    public List<PrivatePhoto> getAlbumPhotos(Long albumId, Long userId) {
        // 验证相册所有权
        Optional<PrivateAlbum> albumOpt = albumRepository.findByIdAndUserId(albumId, userId);
        if (!albumOpt.isPresent()) {
            throw new RuntimeException("相册不存在或无权限");
        }

        try {
            List<PrivatePhoto> photos = photoRepository.findByAlbumIdOrderByUploadTimeDesc(albumId);
            return photos != null ? photos : new ArrayList<>();
        } catch (Exception e) {
            System.out.println("获取相册照片时出错: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * 删除照片
     */
    public boolean deletePhoto(Long photoId, Long userId) {
        Optional<PrivatePhoto> photoOpt = photoRepository.findById(photoId);
        if (photoOpt.isPresent()) {
            PrivatePhoto photo = photoOpt.get();
            
            // 验证权限
            Optional<PrivateAlbum> albumOpt = albumRepository.findByIdAndUserId(photo.getAlbumId(), userId);
            if (!albumOpt.isPresent()) {
                return false;
            }

            // 删除文件
            try {
                Files.deleteIfExists(Paths.get(photo.getFilePath()));
                if (photo.getThumbnailPath() != null) {
                    Files.deleteIfExists(Paths.get(photo.getThumbnailPath()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            photoRepository.delete(photo);
            return true;
        }
        return false;
    }

    /**
     * 删除相册
     */
    public boolean deleteAlbum(Long albumId, Long userId) {
        Optional<PrivateAlbum> albumOpt = albumRepository.findByIdAndUserId(albumId, userId);
        if (albumOpt.isPresent()) {
            // 删除所有照片
            List<PrivatePhoto> photos = photoRepository.findByAlbumIdOrderByUploadTimeDesc(albumId);
            for (PrivatePhoto photo : photos) {
                try {
                    Files.deleteIfExists(Paths.get(photo.getFilePath()));
                    if (photo.getThumbnailPath() != null) {
                        Files.deleteIfExists(Paths.get(photo.getThumbnailPath()));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // 删除数据库记录
            photoRepository.deleteByAlbumId(albumId);
            albumRepository.delete(albumOpt.get());
            return true;
        }
        return false;
    }

    /**
     * 生成缩略图
     */
    private void generateThumbnail(String originalPath, String thumbnailPath) {
        try {
            BufferedImage originalImage = ImageIO.read(Paths.get(originalPath).toFile());
            
            // 计算缩略图尺寸（保持比例，最大300px）
            int maxSize = 300;
            int width = originalImage.getWidth();
            int height = originalImage.getHeight();
            
            double ratio = Math.min((double) maxSize / width, (double) maxSize / height);
            int newWidth = (int) (width * ratio);
            int newHeight = (int) (height * ratio);
            
            BufferedImage thumbnailImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            thumbnailImage.createGraphics().drawImage(originalImage.getScaledInstance(newWidth, newHeight, java.awt.Image.SCALE_SMOOTH), 0, 0, null);
            
            // 保存缩略图
            String format = "jpg";
            ImageIO.write(thumbnailImage, format, Paths.get(thumbnailPath).toFile());
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取相册信息
     */
    public Optional<PrivateAlbum> getAlbum(Long albumId, Long userId) {
        return albumRepository.findByIdAndUserId(albumId, userId);
    }
}
