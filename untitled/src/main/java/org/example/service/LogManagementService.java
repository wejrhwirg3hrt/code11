package org.example.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * 日志管理服务
 * 提供系统日志文件的清理和管理功能
 */
@Service
public class LogManagementService {
    
    private static final Logger logger = LoggerFactory.getLogger(LogManagementService.class);
    
    @Value("${logging.file.path:logs}")
    private String logDirectory;
    
    @Value("${logging.file.name:video-website.log}")
    private String mainLogFileName;
    
    /**
     * 一键清除所有日志文件
     */
    public LogCleanupResult clearAllLogs() {
        LogCleanupResult result = new LogCleanupResult();
        result.setOperation("一键清除所有日志");
        result.setTimestamp(LocalDateTime.now());
        
        try {
            Path logDir = Paths.get(logDirectory);
            if (!Files.exists(logDir)) {
                result.setSuccess(false);
                result.setMessage("日志目录不存在: " + logDirectory);
                return result;
            }
            
            List<String> deletedFiles = new ArrayList<>();
            final long[] totalSize = {0};
            
            // 删除所有日志文件
            try (Stream<Path> paths = Files.walk(logDir)) {
                paths.filter(Files::isRegularFile)
                     .filter(path -> isLogFile(path))
                     .forEach(path -> {
                         try {
                             long size = Files.size(path);
                             Files.delete(path);
                             deletedFiles.add(path.getFileName().toString());
                             totalSize[0] += size;
                             logger.info("删除日志文件: {}", path);
                         } catch (IOException e) {
                             logger.error("删除日志文件失败: {}", path, e);
                         }
                     });
            }
            
            result.setSuccess(true);
            result.setDeletedFiles(deletedFiles);
            result.setDeletedCount(deletedFiles.size());
            result.setFreedSpace(totalSize[0]);
            result.setMessage(String.format("成功删除 %d 个日志文件，释放空间 %.2f MB", 
                deletedFiles.size(), totalSize[0] / (1024.0 * 1024.0)));
            
        } catch (Exception e) {
            logger.error("清除所有日志失败", e);
            result.setSuccess(false);
            result.setMessage("清除日志失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 保留指定天数的日志文件
     */
    public LogCleanupResult retainLogsForDays(int days) {
        LogCleanupResult result = new LogCleanupResult();
        result.setOperation("保留" + days + "天日志");
        result.setTimestamp(LocalDateTime.now());
        
        try {
            Path logDir = Paths.get(logDirectory);
            if (!Files.exists(logDir)) {
                result.setSuccess(false);
                result.setMessage("日志目录不存在: " + logDirectory);
                return result;
            }
            
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
            List<String> deletedFiles = new ArrayList<>();
            final long[] totalSize = {0};
            
            // 删除超过指定天数的日志文件
            try (Stream<Path> paths = Files.walk(logDir)) {
                paths.filter(Files::isRegularFile)
                     .filter(path -> isLogFile(path))
                     .forEach(path -> {
                         try {
                             BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
                             LocalDateTime fileDate = LocalDateTime.ofInstant(
                                 attrs.creationTime().toInstant(), ZoneId.systemDefault());
                             
                             if (fileDate.isBefore(cutoffDate)) {
                                 long size = Files.size(path);
                                 Files.delete(path);
                                 deletedFiles.add(path.getFileName().toString());
                                 totalSize[0] += size;
                                 logger.info("删除过期日志文件: {} (创建时间: {})", 
                                     path, fileDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                             }
                         } catch (IOException e) {
                             logger.error("处理日志文件失败: {}", path, e);
                         }
                     });
            }
            
            result.setSuccess(true);
            result.setDeletedFiles(deletedFiles);
            result.setDeletedCount(deletedFiles.size());
            result.setFreedSpace(totalSize[0]);
            result.setMessage(String.format("成功删除 %d 个过期日志文件，释放空间 %.2f MB", 
                deletedFiles.size(), totalSize[0] / (1024.0 * 1024.0)));
            
        } catch (Exception e) {
            logger.error("保留{}天日志失败", days, e);
            result.setSuccess(false);
            result.setMessage("保留日志失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 获取日志文件统计信息
     */
    public LogStatistics getLogStatistics() {
        LogStatistics stats = new LogStatistics();
        stats.setTimestamp(LocalDateTime.now());
        
        try {
            Path logDir = Paths.get(logDirectory);
            if (!Files.exists(logDir)) {
                stats.setLogDirectoryExists(false);
                return stats;
            }
            
            stats.setLogDirectoryExists(true);
            stats.setLogDirectory(logDirectory);
            
            List<LogFileInfo> logFiles = new ArrayList<>();
            final long[] totalSize = {0};
            final int[] totalFiles = {0};
            
            // 统计所有日志文件
            try (Stream<Path> paths = Files.walk(logDir)) {
                paths.filter(Files::isRegularFile)
                     .filter(path -> isLogFile(path))
                     .forEach(path -> {
                         try {
                             BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
                             long size = Files.size(path);
                             LocalDateTime fileDate = LocalDateTime.ofInstant(
                                 attrs.creationTime().toInstant(), ZoneId.systemDefault());
                             
                             LogFileInfo fileInfo = new LogFileInfo();
                             fileInfo.setFileName(path.getFileName().toString());
                             fileInfo.setSize(size);
                             fileInfo.setCreatedDate(fileDate);
                             fileInfo.setPath(path.toString());
                             
                             logFiles.add(fileInfo);
                             totalSize[0] += size;
                             totalFiles[0]++;
                             
                         } catch (IOException e) {
                             logger.error("获取文件信息失败: {}", path, e);
                         }
                     });
            }
            
            stats.setLogFiles(logFiles);
            stats.setTotalFiles(totalFiles[0]);
            stats.setTotalSize(totalSize[0]);
            stats.setTotalSizeMB(totalSize[0] / (1024.0 * 1024.0));
            
        } catch (Exception e) {
            logger.error("获取日志统计信息失败", e);
        }
        
        return stats;
    }
    
    /**
     * 判断是否为日志文件
     */
    private boolean isLogFile(Path path) {
        String fileName = path.getFileName().toString().toLowerCase();
        return fileName.endsWith(".log") || 
               fileName.contains("video-website") ||
               fileName.contains("error") ||
               fileName.contains("access");
    }
    
    /**
     * 日志清理结果
     */
    public static class LogCleanupResult {
        private String operation;
        private LocalDateTime timestamp;
        private boolean success;
        private String message;
        private List<String> deletedFiles;
        private int deletedCount;
        private long freedSpace;
        
        // Getters and Setters
        public String getOperation() { return operation; }
        public void setOperation(String operation) { this.operation = operation; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public List<String> getDeletedFiles() { return deletedFiles; }
        public void setDeletedFiles(List<String> deletedFiles) { this.deletedFiles = deletedFiles; }
        
        public int getDeletedCount() { return deletedCount; }
        public void setDeletedCount(int deletedCount) { this.deletedCount = deletedCount; }
        
        public long getFreedSpace() { return freedSpace; }
        public void setFreedSpace(long freedSpace) { this.freedSpace = freedSpace; }
    }
    
    /**
     * 日志统计信息
     */
    public static class LogStatistics {
        private LocalDateTime timestamp;
        private boolean logDirectoryExists;
        private String logDirectory;
        private List<LogFileInfo> logFiles;
        private int totalFiles;
        private long totalSize;
        private double totalSizeMB;
        
        // Getters and Setters
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        
        public boolean isLogDirectoryExists() { return logDirectoryExists; }
        public void setLogDirectoryExists(boolean logDirectoryExists) { this.logDirectoryExists = logDirectoryExists; }
        
        public String getLogDirectory() { return logDirectory; }
        public void setLogDirectory(String logDirectory) { this.logDirectory = logDirectory; }
        
        public List<LogFileInfo> getLogFiles() { return logFiles; }
        public void setLogFiles(List<LogFileInfo> logFiles) { this.logFiles = logFiles; }
        
        public int getTotalFiles() { return totalFiles; }
        public void setTotalFiles(int totalFiles) { this.totalFiles = totalFiles; }
        
        public long getTotalSize() { return totalSize; }
        public void setTotalSize(long totalSize) { this.totalSize = totalSize; }
        
        public double getTotalSizeMB() { return totalSizeMB; }
        public void setTotalSizeMB(double totalSizeMB) { this.totalSizeMB = totalSizeMB; }
    }
    
    /**
     * 日志文件信息
     */
    public static class LogFileInfo {
        private String fileName;
        private long size;
        private LocalDateTime createdDate;
        private String path;
        
        // Getters and Setters
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        
        public long getSize() { return size; }
        public void setSize(long size) { this.size = size; }
        
        public LocalDateTime getCreatedDate() { return createdDate; }
        public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
        
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
        
        public double getSizeMB() {
            return size / (1024.0 * 1024.0);
        }
    }
} 