package org.example.controller.api;

import org.example.entity.User;
import org.example.service.UserService;
import org.example.util.AvatarUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 头像上传控制器
 */
@RestController
@RequestMapping("/api/avatar")
public class AvatarController {

    @Autowired
    private UserService userService;

    @Value("${app.upload.path:./uploads}")
    private String uploadPath;

    /**
     * 上传头像文件
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (authentication == null) {
                response.put("success", false);
                response.put("message", "请先登录");
                return ResponseEntity.status(401).body(response);
            }

            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            if (!userOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.status(401).body(response);
            }

            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "请选择要上传的文件");
                return ResponseEntity.ok(response);
            }

            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                response.put("success", false);
                response.put("message", "只能上传图片文件");
                return ResponseEntity.ok(response);
            }

            // 检查文件大小 (5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                response.put("success", false);
                response.put("message", "文件大小不能超过5MB");
                return ResponseEntity.ok(response);
            }

            // 创建头像目录
            Path avatarDir = Paths.get(uploadPath, "avatars");
            Files.createDirectories(avatarDir);

            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;
            
            // 保存文件
            Path filePath = avatarDir.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            // 更新用户头像
            User user = userOpt.get();
            String avatarUrl = "/uploads/avatars/" + filename;
            user.setAvatar(avatarUrl);
            userService.save(user);

            response.put("success", true);
            response.put("message", "头像上传成功");
            response.put("avatarUrl", avatarUrl);

            System.out.println("用户 " + user.getUsername() + " 上传头像: " + avatarUrl);

        } catch (IOException e) {
            System.err.println("头像上传失败: " + e.getMessage());
            response.put("success", false);
            response.put("message", "文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("头像上传异常: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "上传失败: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 通过URL设置头像
     */
    @PostMapping("/set-url")
    public ResponseEntity<Map<String, Object>> setAvatarUrl(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (authentication == null) {
                response.put("success", false);
                response.put("message", "请先登录");
                return ResponseEntity.status(401).body(response);
            }

            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            if (!userOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.status(401).body(response);
            }

            String avatarUrl = request.get("avatarUrl");
            if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "头像URL不能为空");
                return ResponseEntity.ok(response);
            }

            // 简单的URL验证
            if (!avatarUrl.startsWith("http://") && !avatarUrl.startsWith("https://") && !avatarUrl.startsWith("/")) {
                response.put("success", false);
                response.put("message", "请输入有效的URL");
                return ResponseEntity.ok(response);
            }

            // 更新用户头像
            User user = userOpt.get();
            user.setAvatar(avatarUrl.trim());
            userService.save(user);

            response.put("success", true);
            response.put("message", "头像设置成功");
            response.put("avatarUrl", avatarUrl.trim());

            System.out.println("用户 " + user.getUsername() + " 设置头像URL: " + avatarUrl);

        } catch (Exception e) {
            System.err.println("设置头像URL失败: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "设置失败: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 重置为默认头像
     */
    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetAvatar(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (authentication == null) {
                response.put("success", false);
                response.put("message", "请先登录");
                return ResponseEntity.status(401).body(response);
            }

            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            if (!userOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.status(401).body(response);
            }

            // 重置为默认头像
            User user = userOpt.get();
            AvatarUtil.setDefaultAvatar(user);
            userService.save(user);

            response.put("success", true);
            response.put("message", "已重置为默认头像");
            response.put("avatarUrl", AvatarUtil.DEFAULT_AVATAR_PATH);

            System.out.println("用户 " + user.getUsername() + " 重置头像");

        } catch (Exception e) {
            System.err.println("重置头像失败: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "重置失败: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }
}
