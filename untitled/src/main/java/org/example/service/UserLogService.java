package org.example.service;

import org.example.entity.UserLog;
import org.example.repository.UserLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = false)
public class UserLogService {

    @Autowired
    private UserLogRepository userLogRepository;

    @Value("${app.upload.path:uploads}")
    private String uploadPath;

    @Value("${app.log.path:logs}")
    private String logPath;

    @Value("${app.temp.path:temp}")
    private String tempPath;

    // 记录用户操作日志
    public void logUserAction(String action, String details) {
        try {
            UserLog log = new UserLog();
            log.setAction(action);
            log.setDetails(details);
            log.setStatus("SUCCESS");

            // 获取当前用户信息
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                log.setUsername(auth.getName());
                // 这里可以根据需要设置userId
            }

            // 获取请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                log.setIpAddress(getClientIpAddress(request));
                log.setUserAgent(request.getHeader("User-Agent"));
                log.setRequestUrl(request.getRequestURL().toString());
            }

            userLogRepository.save(log);
        } catch (Exception e) {
            // 记录日志失败不应该影响主要业务流程
            System.err.println("Failed to log user action: " + e.getMessage());
        }
    }

    // 记录错误日志
    public void logError(String action, String errorMessage, Exception exception) {
        try {
            UserLog log = new UserLog();
            log.setAction(action);
            log.setDetails("Error occurred: " + errorMessage);
            log.setStatus("ERROR");
            log.setErrorMessage(exception != null ? exception.getMessage() : errorMessage);

            // 获取当前用户信息
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                log.setUsername(auth.getName());
            }

            // 获取请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                log.setIpAddress(getClientIpAddress(request));
                log.setUserAgent(request.getHeader("User-Agent"));
                log.setRequestUrl(request.getRequestURL().toString());
            }

            userLogRepository.save(log);
        } catch (Exception e) {
            System.err.println("Failed to log error: " + e.getMessage());
        }
    }

    // 记录登录尝试
    public void logLoginAttempt(String username, boolean success, String details) {
        try {
            UserLog log = new UserLog();
            log.setUsername(username);
            log.setAction(success ? "LOGIN_SUCCESS" : "LOGIN_FAILED");
            log.setDetails(details);
            log.setStatus(success ? "SUCCESS" : "ERROR");

            // 获取请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                log.setIpAddress(getClientIpAddress(request));
                log.setUserAgent(request.getHeader("User-Agent"));
                log.setRequestUrl(request.getRequestURL().toString());
            }

            userLogRepository.save(log);
        } catch (Exception e) {
            System.err.println("Failed to log login attempt: " + e.getMessage());
        }
    }

    // 获取用户日志
    public Page<UserLog> getUserLogs(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    // 获取所有日志
    public Page<UserLog> getAllLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userLogRepository.findAll(pageable);
    }

    // 获取错误日志
    public List<UserLog> getRecentErrors(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return userLogRepository.findRecentErrors(pageable);
    }

    // 获取用户最近活动
    public List<UserLog> getUserRecentActivity(Long userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return userLogRepository.findRecentUserActivity(userId, pageable);
    }

    // 清除所有日志（数据库 + 文件）
    public void clearAllLogs() {
        try {
            // 清除数据库日志
            userLogRepository.deleteAll();
            
            // 清除日志文件
            clearLogFiles();
            
            // 清除临时文件
            clearTempFiles();
            
            System.out.println("所有日志和文件已清除");
        } catch (Exception e) {
            System.err.println("清除日志失败: " + e.getMessage());
            throw new RuntimeException("清除日志失败", e);
        }
    }

    // 清除错误日志（数据库 + 文件）
    public void clearErrorLogs() {
        try {
            // 清除数据库错误日志
            userLogRepository.deleteByStatus("ERROR");
            
            // 清除错误日志文件
            clearErrorLogFiles();
            
            System.out.println("错误日志和文件已清除");
        } catch (Exception e) {
            System.err.println("清除错误日志失败: " + e.getMessage());
            throw new RuntimeException("清除错误日志失败", e);
        }
    }

    // 清除指定日期之前的日志（数据库 + 文件）
    public void clearLogsBeforeDate(LocalDateTime date) {
        try {
            // 清除数据库日志
            userLogRepository.deleteByCreatedAtBefore(date);
            
            // 清除指定日期前的日志文件
            clearLogFilesBeforeDate(date);
            
            System.out.println("指定日期之前的日志和文件已清除");
        } catch (Exception e) {
            System.err.println("清除指定日期日志失败: " + e.getMessage());
            throw new RuntimeException("清除指定日期日志失败", e);
        }
    }

    // 清除日志文件
    public void clearLogFiles() {
        try {
            Path logsDir = Paths.get(logPath);
            if (Files.exists(logsDir)) {
                deleteDirectoryContents(logsDir);
                System.out.println("日志文件已清除");
            }
        } catch (Exception e) {
            System.err.println("清除日志文件失败: " + e.getMessage());
        }
    }

    // 清除错误日志文件
    public void clearErrorLogFiles() {
        try {
            Path logsDir = Paths.get(logPath);
            if (Files.exists(logsDir)) {
                try (Stream<Path> paths = Files.walk(logsDir)) {
                    paths.filter(path -> {
                        String fileName = path.getFileName().toString().toLowerCase();
                        return fileName.contains("error") || fileName.contains("exception");
                    }).forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            System.err.println("删除错误日志文件失败: " + path + " - " + e.getMessage());
                        }
                    });
                }
                System.out.println("错误日志文件已清除");
            }
        } catch (Exception e) {
            System.err.println("清除错误日志文件失败: " + e.getMessage());
        }
    }

    // 清除指定日期前的日志文件
    public void clearLogFilesBeforeDate(LocalDateTime date) {
        try {
            Path logsDir = Paths.get(logPath);
            if (Files.exists(logsDir)) {
                try (Stream<Path> paths = Files.walk(logsDir)) {
                    paths.filter(Files::isRegularFile)
                         .filter(path -> isFileOlderThan(path, date))
                         .forEach(path -> {
                             try {
                                 Files.deleteIfExists(path);
                             } catch (IOException e) {
                                 System.err.println("删除旧日志文件失败: " + path + " - " + e.getMessage());
                             }
                         });
                }
                System.out.println("指定日期前的日志文件已清除");
            }
        } catch (Exception e) {
            System.err.println("清除指定日期前日志文件失败: " + e.getMessage());
        }
    }

    // 清除临时文件
    public void clearTempFiles() {
        try {
            Path tempDir = Paths.get(tempPath);
            if (Files.exists(tempDir)) {
                deleteDirectoryContents(tempDir);
                System.out.println("临时文件已清除");
            }
        } catch (Exception e) {
            System.err.println("清除临时文件失败: " + e.getMessage());
        }
    }

    // 清除上传文件
    public void clearUploadFiles() {
        try {
            Path uploadDir = Paths.get(uploadPath);
            if (Files.exists(uploadDir)) {
                deleteDirectoryContents(uploadDir);
                System.out.println("上传文件已清除");
            }
        } catch (Exception e) {
            System.err.println("清除上传文件失败: " + e.getMessage());
        }
    }

    // 清除所有项目文件（包括日志、上传、临时文件）
    public void clearAllProjectFiles() {
        try {
            clearLogFiles();
            clearUploadFiles();
            clearTempFiles();
            System.out.println("所有项目文件已清除");
        } catch (Exception e) {
            System.err.println("清除项目文件失败: " + e.getMessage());
            throw new RuntimeException("清除项目文件失败", e);
        }
    }

    // 清除指定类型的文件
    public void clearFilesByType(String fileType) {
        try {
            Path baseDir = Paths.get(".");
            if (Files.exists(baseDir)) {
                try (Stream<Path> paths = Files.walk(baseDir, 3)) {
                    paths.filter(Files::isRegularFile)
                         .filter(path -> {
                             String fileName = path.getFileName().toString().toLowerCase();
                             return fileName.endsWith(fileType.toLowerCase());
                         })
                         .forEach(path -> {
                             try {
                                 Files.deleteIfExists(path);
                             } catch (IOException e) {
                                 System.err.println("删除文件失败: " + path + " - " + e.getMessage());
                             }
                         });
                }
                System.out.println(fileType + " 类型文件已清除");
            }
        } catch (Exception e) {
            System.err.println("清除" + fileType + "类型文件失败: " + e.getMessage());
        }
    }

    // 删除目录内容
    private void deleteDirectoryContents(Path directory) throws IOException {
        if (Files.exists(directory)) {
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (!dir.equals(directory)) {
                        Files.delete(dir);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    // 检查文件是否早于指定日期
    private boolean isFileOlderThan(Path file, LocalDateTime date) {
        try {
            BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
            LocalDateTime fileTime = LocalDateTime.ofInstant(attrs.creationTime().toInstant(), 
                                                           java.time.ZoneId.systemDefault());
            return fileTime.isBefore(date);
        } catch (IOException e) {
            return false;
        }
    }

    // 获取文件统计信息
    public String getFileStatistics() {
        StringBuilder stats = new StringBuilder();
        
        try {
            // 统计日志文件
            Path logsDir = Paths.get(logPath);
            if (Files.exists(logsDir)) {
                long logFileCount = Files.walk(logsDir).filter(Files::isRegularFile).count();
                long logFileSize = Files.walk(logsDir).filter(Files::isRegularFile)
                                      .mapToLong(path -> {
                                          try {
                                              return Files.size(path);
                                          } catch (IOException e) {
                                              return 0;
                                          }
                                      }).sum();
                stats.append("日志文件: ").append(logFileCount).append(" 个, ")
                     .append(formatFileSize(logFileSize)).append("\n");
            }

            // 统计上传文件
            Path uploadDir = Paths.get(uploadPath);
            if (Files.exists(uploadDir)) {
                long uploadFileCount = Files.walk(uploadDir).filter(Files::isRegularFile).count();
                long uploadFileSize = Files.walk(uploadDir).filter(Files::isRegularFile)
                                         .mapToLong(path -> {
                                             try {
                                                 return Files.size(path);
                                             } catch (IOException e) {
                                                 return 0;
                                             }
                                         }).sum();
                stats.append("上传文件: ").append(uploadFileCount).append(" 个, ")
                     .append(formatFileSize(uploadFileSize)).append("\n");
            }

            // 统计临时文件
            Path tempDir = Paths.get(tempPath);
            if (Files.exists(tempDir)) {
                long tempFileCount = Files.walk(tempDir).filter(Files::isRegularFile).count();
                long tempFileSize = Files.walk(tempDir).filter(Files::isRegularFile)
                                       .mapToLong(path -> {
                                           try {
                                               return Files.size(path);
                                           } catch (IOException e) {
                                               return 0;
                                           }
                                       }).sum();
                stats.append("临时文件: ").append(tempFileCount).append(" 个, ")
                     .append(formatFileSize(tempFileSize));
            }
        } catch (Exception e) {
            stats.append("获取文件统计信息失败: ").append(e.getMessage());
        }
        
        return stats.toString();
    }

    // 格式化文件大小
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    // 获取客户端IP地址
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    // ==================== 用户数据清理功能 ====================

    /**
     * 确保默认头像文件存在
     */
    public void ensureDefaultAvatarExists() {
        try {
            Path avatarsDir = Paths.get(uploadPath, "avatars");
            if (!Files.exists(avatarsDir)) {
                Files.createDirectories(avatarsDir);
            }
            
            // 检查默认头像文件是否存在
            Path defaultAvatarPath = avatarsDir.resolve("default.svg");
            if (!Files.exists(defaultAvatarPath)) {
                // 从静态资源复制默认头像
                Path sourcePath = Paths.get("src/main/resources/static/images/default-avatar.svg");
                if (Files.exists(sourcePath)) {
                    Files.copy(sourcePath, defaultAvatarPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("✅ 已创建默认头像文件: " + defaultAvatarPath);
                } else {
                    // 如果源文件不存在，创建一个简单的默认头像
                    String defaultSvg = """
                        <svg xmlns="http://www.w3.org/2000/svg" width="100" height="100" viewBox="0 0 100 100">
                            <circle cx="50" cy="35" r="20" fill="#6c757d"/>
                            <circle cx="50" cy="100" r="45" fill="#6c757d"/>
                        </svg>
                        """;
                    Files.write(defaultAvatarPath, defaultSvg.getBytes());
                    System.out.println("✅ 已创建默认头像文件: " + defaultAvatarPath);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ 创建默认头像文件失败: " + e.getMessage());
        }
    }

    /**
     * 检查是否为默认头像文件
     */
    public boolean isDefaultAvatarFile(Path file) {
        if (file == null) return false;
        
        String fileName = file.getFileName().toString().toLowerCase();
        
        // 保护默认头像文件
        return fileName.contains("default") || 
               fileName.contains("avatar") ||
               fileName.equals("default.png") ||
               fileName.equals("default.jpg") ||
               fileName.equals("default.jpeg") ||
               fileName.equals("default.svg") ||
               fileName.equals("default-avatar.png") ||
               fileName.equals("default-avatar.svg");
    }

    /**
     * 清除所有用户数据（数据库记录 + 用户文件）
     */
    public String clearAllUserData() {
        StringBuilder result = new StringBuilder();
        result.append("=== 开始清除所有用户数据 ===\n");
        
        try {
            // 1. 清除用户上传的文件
            result.append(clearUserUploadFiles());
            
            // 2. 清除用户相关的数据库记录
            result.append(clearUserDatabaseRecords());
            
            // 3. 清除用户相关的临时文件
            result.append(clearUserTempFiles());
            
            result.append("=== 所有用户数据清除完成 ===\n");
            
        } catch (Exception e) {
            result.append("清除用户数据时出错: ").append(e.getMessage()).append("\n");
        }
        
        return result.toString();
    }

    /**
     * 清除用户上传的文件
     */
    public String clearUserUploadFiles() {
        StringBuilder result = new StringBuilder();
        result.append("清除用户上传文件...\n");
        
        try {
            // 确保默认头像文件存在
            ensureDefaultAvatarExists();
            
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                result.append("上传目录不存在，跳过\n");
                return result.toString();
            }

            // 清除用户头像（保护默认头像）
            Path avatarsDir = uploadDir.resolve("avatars");
            if (Files.exists(avatarsDir)) {
                long avatarCount = Files.walk(avatarsDir)
                    .filter(Files::isRegularFile)
                    .filter(file -> !isDefaultAvatarFile(file)) // 使用新的保护方法
                    .peek(file -> {
                        try {
                            Files.delete(file);
                        } catch (IOException e) {
                            result.append("删除头像文件失败: ").append(file).append(" - ").append(e.getMessage()).append("\n");
                        }
                    })
                    .count();
                result.append(String.format("已删除 %d 个用户头像文件（已保护默认头像）\n", avatarCount));
            }

            // 清除用户动态图片
            Path momentsDir = uploadDir.resolve("moments");
            if (Files.exists(momentsDir)) {
                long momentCount = Files.walk(momentsDir)
                    .filter(Files::isRegularFile)
                    .peek(file -> {
                        try {
                            Files.delete(file);
                        } catch (IOException e) {
                            result.append("删除动态图片失败: ").append(file).append(" - ").append(e.getMessage()).append("\n");
                        }
                    })
                    .count();
                result.append(String.format("已删除 %d 个动态图片文件\n", momentCount));
            }

            // 清除用户聊天文件
            Path chatDir = uploadDir.resolve("chat");
            if (Files.exists(chatDir)) {
                long chatCount = Files.walk(chatDir)
                    .filter(Files::isRegularFile)
                    .peek(file -> {
                        try {
                            Files.delete(file);
                        } catch (IOException e) {
                            result.append("删除聊天文件失败: ").append(file).append(" - ").append(e.getMessage()).append("\n");
                        }
                    })
                    .count();
                result.append(String.format("已删除 %d 个聊天文件\n", chatCount));
            }

            // 清除用户私密文件
            Path privateDir = uploadDir.resolve("private");
            if (Files.exists(privateDir)) {
                long privateCount = Files.walk(privateDir)
                    .filter(Files::isRegularFile)
                    .peek(file -> {
                        try {
                            Files.delete(file);
                        } catch (IOException e) {
                            result.append("删除私密文件失败: ").append(file).append(" - ").append(e.getMessage()).append("\n");
                        }
                    })
                    .count();
                result.append(String.format("已删除 %d 个私密文件\n", privateCount));
            }

            // 清除用户音乐文件
            Path musicDir = uploadDir.resolve("music");
            if (Files.exists(musicDir)) {
                long musicCount = Files.walk(musicDir)
                    .filter(Files::isRegularFile)
                    .peek(file -> {
                        try {
                            Files.delete(file);
                        } catch (IOException e) {
                            result.append("删除音乐文件失败: ").append(file).append(" - ").append(e.getMessage()).append("\n");
                        }
                    })
                    .count();
                result.append(String.format("已删除 %d 个音乐文件\n", musicCount));
            }

            result.append("用户上传文件清除完成\n");
            
        } catch (Exception e) {
            result.append("清除用户上传文件时出错: ").append(e.getMessage()).append("\n");
        }
        
        return result.toString();
    }

    /**
     * 清除用户相关的数据库记录
     */
    public String clearUserDatabaseRecords() {
        StringBuilder result = new StringBuilder();
        result.append("清除用户数据库记录...\n");
        
        try {
            // 这里需要注入相关的Repository来清除用户数据
            // 由于没有具体的Repository，这里提供框架代码
            result.append("注意: 需要注入具体的Repository来清除用户数据\n");
            result.append("建议清除以下表的数据:\n");
            result.append("- users (用户表)\n");
            result.append("- user_profiles (用户资料表)\n");
            result.append("- user_achievements (用户成就表)\n");
            result.append("- user_follows (用户关注表)\n");
            result.append("- user_moments (用户动态表)\n");
            result.append("- user_messages (用户消息表)\n");
            result.append("- user_notifications (用户通知表)\n");
            result.append("- user_logs (用户日志表)\n");
            result.append("- user_uploads (用户上传表)\n");
            result.append("- user_settings (用户设置表)\n");
            
            // 清除用户日志
            long logCount = userLogRepository.count();
            userLogRepository.deleteAll();
            result.append(String.format("已删除 %d 条用户日志记录\n", logCount));
            
            result.append("用户数据库记录清除完成\n");
            
        } catch (Exception e) {
            result.append("清除用户数据库记录时出错: ").append(e.getMessage()).append("\n");
        }
        
        return result.toString();
    }

    /**
     * 清除用户相关的临时文件
     */
    public String clearUserTempFiles() {
        StringBuilder result = new StringBuilder();
        result.append("清除用户临时文件...\n");
        
        try {
            Path tempDir = Paths.get(tempPath);
            if (!Files.exists(tempDir)) {
                result.append("临时目录不存在，跳过\n");
                return result.toString();
            }

            // 清除用户相关的临时文件
            long tempCount = Files.walk(tempDir)
                .filter(Files::isRegularFile)
                .filter(file -> {
                    String fileName = file.getFileName().toString().toLowerCase();
                    // 过滤用户相关的临时文件
                    return fileName.contains("user") || 
                           fileName.contains("avatar") || 
                           fileName.contains("upload") ||
                           fileName.contains("temp");
                })
                .peek(file -> {
                    try {
                        Files.delete(file);
                    } catch (IOException e) {
                        result.append("删除临时文件失败: ").append(file).append(" - ").append(e.getMessage()).append("\n");
                    }
                })
                .count();
            
            result.append(String.format("已删除 %d 个用户相关临时文件\n", tempCount));
            result.append("用户临时文件清除完成\n");
            
        } catch (Exception e) {
            result.append("清除用户临时文件时出错: ").append(e.getMessage()).append("\n");
        }
        
        return result.toString();
    }

    /**
     * 清除指定用户的数据
     */
    public String clearUserDataByUsername(String username) {
        StringBuilder result = new StringBuilder();
        result.append(String.format("清除用户 '%s' 的数据...\n", username));
        
        try {
            // 1. 清除该用户的上传文件
            result.append(clearUserUploadFilesByUsername(username));
            
            // 2. 清除该用户的数据库记录
            result.append(clearUserDatabaseRecordsByUsername(username));
            
            // 3. 清除该用户的临时文件
            result.append(clearUserTempFilesByUsername(username));
            
            result.append(String.format("用户 '%s' 的数据清除完成\n", username));
            
        } catch (Exception e) {
            result.append("清除用户数据时出错: ").append(e.getMessage()).append("\n");
        }
        
        return result.toString();
    }

    /**
     * 清除指定用户的上传文件
     */
    public String clearUserUploadFilesByUsername(String username) {
        StringBuilder result = new StringBuilder();
        result.append(String.format("清除用户 '%s' 的上传文件...\n", username));
        
        try {
            // 确保默认头像文件存在
            ensureDefaultAvatarExists();
            
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                result.append("上传目录不存在，跳过\n");
                return result.toString();
            }

            // 清除该用户的头像（保护默认头像）
            Path avatarsDir = uploadDir.resolve("avatars");
            if (Files.exists(avatarsDir)) {
                long avatarCount = Files.walk(avatarsDir)
                    .filter(Files::isRegularFile)
                    .filter(file -> {
                        // 保护默认头像文件
                        if (isDefaultAvatarFile(file)) {
                            return false;
                        }
                        // 只删除包含用户名的文件
                        String fileName = file.getFileName().toString();
                        return fileName.contains(username);
                    })
                    .peek(file -> {
                        try {
                            Files.delete(file);
                        } catch (IOException e) {
                            result.append("删除用户头像失败: ").append(file).append(" - ").append(e.getMessage()).append("\n");
                        }
                    })
                    .count();
                result.append(String.format("已删除用户 %d 个头像文件（已保护默认头像）\n", avatarCount));
            }

            // 清除该用户的其他文件
            String[] subDirs = {"moments", "chat", "private", "music"};
            for (String subDir : subDirs) {
                Path dir = uploadDir.resolve(subDir);
                if (Files.exists(dir)) {
                    long fileCount = Files.walk(dir)
                        .filter(Files::isRegularFile)
                        .filter(file -> file.getFileName().toString().contains(username))
                        .peek(file -> {
                            try {
                                Files.delete(file);
                            } catch (IOException e) {
                                result.append(String.format("删除用户%s文件失败: ", subDir)).append(file).append(" - ").append(e.getMessage()).append("\n");
                            }
                        })
                        .count();
                    result.append(String.format("已删除用户 %d 个%s文件\n", fileCount, subDir));
                }
            }

            result.append(String.format("用户 '%s' 的上传文件清除完成\n", username));
            
        } catch (Exception e) {
            result.append("清除用户上传文件时出错: ").append(e.getMessage()).append("\n");
        }
        
        return result.toString();
    }

    /**
     * 清除指定用户的数据库记录
     */
    public String clearUserDatabaseRecordsByUsername(String username) {
        StringBuilder result = new StringBuilder();
        result.append(String.format("清除用户 '%s' 的数据库记录...\n", username));
        
        try {
            // 清除该用户的日志记录
            long logCount = userLogRepository.countByUsername(username);
            userLogRepository.deleteByUsername(username);
            result.append(String.format("已删除用户 %d 条日志记录\n", logCount));
            
            result.append(String.format("用户 '%s' 的数据库记录清除完成\n", username));
            
        } catch (Exception e) {
            result.append("清除用户数据库记录时出错: ").append(e.getMessage()).append("\n");
        }
        
        return result.toString();
    }

    /**
     * 清除指定用户的临时文件
     */
    public String clearUserTempFilesByUsername(String username) {
        StringBuilder result = new StringBuilder();
        result.append(String.format("清除用户 '%s' 的临时文件...\n", username));
        
        try {
            Path tempDir = Paths.get(tempPath);
            if (!Files.exists(tempDir)) {
                result.append("临时目录不存在，跳过\n");
                return result.toString();
            }

            // 清除该用户的临时文件
            long tempCount = Files.walk(tempDir)
                .filter(Files::isRegularFile)
                .filter(file -> file.getFileName().toString().contains(username))
                .peek(file -> {
                    try {
                        Files.delete(file);
                    } catch (IOException e) {
                        result.append("删除用户临时文件失败: ").append(file).append(" - ").append(e.getMessage()).append("\n");
                    }
                })
                .count();
            
            result.append(String.format("已删除用户 %d 个临时文件\n", tempCount));
            result.append(String.format("用户 '%s' 的临时文件清除完成\n", username));
            
        } catch (Exception e) {
            result.append("清除用户临时文件时出错: ").append(e.getMessage()).append("\n");
        }
        
        return result.toString();
    }
}
