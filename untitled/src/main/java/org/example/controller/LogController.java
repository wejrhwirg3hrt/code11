package org.example.controller;

import org.example.entity.UserLog;
import org.example.service.UserLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/user-logs")
@PreAuthorize("hasRole('ADMIN')")
public class LogController {

    @Autowired
    private UserLogService userLogService;

    // 显示日志管理页面
    @GetMapping
    public String logsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {
        
        Page<UserLog> logs = userLogService.getAllLogs(page, size);
        List<UserLog> recentErrors = userLogService.getRecentErrors(10);
        
        model.addAttribute("logs", logs);
        model.addAttribute("recentErrors", recentErrors);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", logs.getTotalPages());
        model.addAttribute("totalElements", logs.getTotalElements());
        
        return "admin/logs";
    }

    // API: 获取最近错误
    @GetMapping("/api/recent-errors")
    @ResponseBody
    public ResponseEntity<List<UserLog>> getRecentErrors(@RequestParam(defaultValue = "20") int limit) {
        List<UserLog> errors = userLogService.getRecentErrors(limit);
        return ResponseEntity.ok(errors);
    }

    // API: 获取用户活动日志
    @GetMapping("/api/user-logs/{userId}")
    @ResponseBody
    public ResponseEntity<List<UserLog>> getUserActivity(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "20") int limit) {
        List<UserLog> activity = userLogService.getUserRecentActivity(userId, limit);
        return ResponseEntity.ok(activity);
    }

    // API: 系统状态概览
    @GetMapping("/api/system-status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSystemStatus() {
        Map<String, Object> status = new HashMap<>();
        
        // 获取最近错误
        List<UserLog> recentErrors = userLogService.getRecentErrors(5);
        status.put("recentErrors", recentErrors);
        status.put("errorCount", recentErrors.size());
        
        // 这里可以添加更多系统状态信息
        status.put("timestamp", System.currentTimeMillis());
        status.put("status", recentErrors.isEmpty() ? "HEALTHY" : "WARNING");
        
        return ResponseEntity.ok(status);
    }

    // API: 清除所有日志
    @DeleteMapping("/api/clear-all")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> clearAllLogs() {
        Map<String, Object> response = new HashMap<>();
        try {
            userLogService.clearAllLogs();
            response.put("success", true);
            response.put("message", "所有日志已清除");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "清除日志失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // API: 清除错误日志
    @DeleteMapping("/api/clear-errors")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> clearErrorLogs() {
        Map<String, Object> response = new HashMap<>();
        try {
            userLogService.clearErrorLogs();
            response.put("success", true);
            response.put("message", "错误日志已清除");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "清除错误日志失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // API: 清除指定日期之前的日志
    @DeleteMapping("/api/clear-before-date")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> clearLogsBeforeDate(@RequestParam String date) {
        Map<String, Object> response = new HashMap<>();
        try {
            LocalDateTime targetDate = LocalDateTime.parse(date);
            userLogService.clearLogsBeforeDate(targetDate);
            response.put("success", true);
            response.put("message", "指定日期之前的日志已清除");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "清除指定日期日志失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // API: 清除所有项目文件
    @DeleteMapping("/api/clear-all-files")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> clearAllProjectFiles() {
        Map<String, Object> response = new HashMap<>();
        try {
            userLogService.clearAllProjectFiles();
            response.put("success", true);
            response.put("message", "所有项目文件已清除");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "清除项目文件失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // API: 清除日志文件
    @DeleteMapping("/api/clear-log-files")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> clearLogFiles() {
        Map<String, Object> response = new HashMap<>();
        try {
            userLogService.clearLogFiles();
            response.put("success", true);
            response.put("message", "日志文件已清除");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "清除日志文件失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // API: 清除上传文件
    @DeleteMapping("/api/clear-upload-files")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> clearUploadFiles() {
        Map<String, Object> response = new HashMap<>();
        try {
            userLogService.clearUploadFiles();
            response.put("success", true);
            response.put("message", "上传文件已清除");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "清除上传文件失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // API: 清除临时文件
    @DeleteMapping("/api/clear-temp-files")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> clearTempFiles() {
        Map<String, Object> response = new HashMap<>();
        try {
            userLogService.clearTempFiles();
            response.put("success", true);
            response.put("message", "临时文件已清除");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "清除临时文件失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // API: 清除指定类型文件
    @DeleteMapping("/api/clear-files-by-type")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> clearFilesByType(@RequestParam String fileType) {
        Map<String, Object> response = new HashMap<>();
        try {
            userLogService.clearFilesByType(fileType);
            response.put("success", true);
            response.put("message", fileType + " 类型文件已清除");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "清除" + fileType + "类型文件失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // API: 获取文件统计信息
    @GetMapping("/api/file-statistics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getFileStatistics() {
        Map<String, Object> response = new HashMap<>();
        try {
            String statistics = userLogService.getFileStatistics();
            response.put("success", true);
            response.put("statistics", statistics);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取文件统计信息失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // API: 完整清理（数据库 + 所有文件）
    @DeleteMapping("/api/complete-cleanup")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> completeCleanup() {
        Map<String, Object> response = new HashMap<>();
        try {
            // 清除数据库日志
            userLogService.clearAllLogs();
            // 清除所有项目文件
            userLogService.clearAllProjectFiles();
            
            response.put("success", true);
            response.put("message", "完整清理完成：数据库日志和所有项目文件已清除");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "完整清理失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ==================== 用户数据清理API ====================

    // API: 清除所有用户数据
    @DeleteMapping("/api/clear-all-user-data")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> clearAllUserData() {
        Map<String, Object> response = new HashMap<>();
        try {
            String result = userLogService.clearAllUserData();
            response.put("success", true);
            response.put("message", "所有用户数据已清除");
            response.put("details", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "清除所有用户数据失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // API: 清除指定用户的数据
    @DeleteMapping("/api/clear-user-data/{username}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> clearUserDataByUsername(@PathVariable String username) {
        Map<String, Object> response = new HashMap<>();
        try {
            String result = userLogService.clearUserDataByUsername(username);
            response.put("success", true);
            response.put("message", "用户 '" + username + "' 的数据已清除");
            response.put("details", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "清除用户数据失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // API: 清除用户上传文件
    @DeleteMapping("/api/clear-user-upload-files")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> clearUserUploadFiles() {
        Map<String, Object> response = new HashMap<>();
        try {
            String result = userLogService.clearUserUploadFiles();
            response.put("success", true);
            response.put("message", "用户上传文件已清除");
            response.put("details", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "清除用户上传文件失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // API: 清除用户数据库记录
    @DeleteMapping("/api/clear-user-database-records")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> clearUserDatabaseRecords() {
        Map<String, Object> response = new HashMap<>();
        try {
            String result = userLogService.clearUserDatabaseRecords();
            response.put("success", true);
            response.put("message", "用户数据库记录已清除");
            response.put("details", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "清除用户数据库记录失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // API: 清除用户临时文件
    @DeleteMapping("/api/clear-user-temp-files")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> clearUserTempFiles() {
        Map<String, Object> response = new HashMap<>();
        try {
            String result = userLogService.clearUserTempFiles();
            response.put("success", true);
            response.put("message", "用户临时文件已清除");
            response.put("details", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "清除用户临时文件失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}

// 公开的日志API（用于前端调试）
@RestController
@RequestMapping("/api/logs")
class PublicLogController {

    @Autowired
    private UserLogService userLogService;

    // 记录前端错误
    @PostMapping("/frontend-error")
    public ResponseEntity<String> logFrontendError(@RequestBody Map<String, String> errorData) {
        try {
            String action = "FRONTEND_ERROR";
            String details = String.format("Error: %s, URL: %s, UserAgent: %s", 
                errorData.get("message"), 
                errorData.get("url"), 
                errorData.get("userAgent"));
            
            userLogService.logError(action, errorData.get("message"), null);
            return ResponseEntity.ok("Error logged successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to log error");
        }
    }

    // 记录页面访问
    @PostMapping("/page-visit")
    public ResponseEntity<String> logPageVisit(@RequestBody Map<String, String> visitData) {
        try {
            String action = "PAGE_VISIT";
            String details = String.format("Page: %s, LoadTime: %s ms", 
                visitData.get("page"), 
                visitData.get("loadTime"));
            
            userLogService.logUserAction(action, details);
            return ResponseEntity.ok("Visit logged successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to log visit");
        }
    }
}
