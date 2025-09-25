package org.example.controller.api;

import org.example.entity.User;
import org.example.repository.UserRepository;
import org.example.service.UserService;
import org.example.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(originPatterns = "*")
public class UserApiController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FollowService followService;

    @PostMapping("/users/register")
    public ResponseEntity<Map<String, Object>> register(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password) {

        Map<String, Object> response = new HashMap<>();

        try {
            if (userService.existsByUsername(username)) {
                response.put("success", false);
                response.put("message", "用户名已存在");
                return ResponseEntity.badRequest().body(response);
            }

            if (userService.existsByEmail(email)) {
                response.put("success", false);
                response.put("message", "邮箱已被注册");
                return ResponseEntity.badRequest().body(response);
            }

            User user = userService.registerUser(username, email, password);
            response.put("success", true);
            response.put("message", "注册成功");
            response.put("user", user);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "注册失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/user/current")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        if (authentication == null) {
            response.put("success", false);
            response.put("message", "请先登录");
            return ResponseEntity.status(401).body(response);
        }

        try {
            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            if (!userOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.status(404).body(response);
            }

            User user = userOpt.get();

            // 构建用户信息
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("email", user.getEmail());
            userInfo.put("avatar", user.getAvatar());
            userInfo.put("createdAt", user.getCreatedAt());
            userInfo.put("enabled", user.isEnabled());
            userInfo.put("banned", user.isBanned());

            response.put("success", true);
            response.put("user", userInfo);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取用户信息失败: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getProfile(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        Optional<User> user = userService.findByUsername(authentication.getName());
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        Optional<User> user = userService.findById(id);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/users/search")
    public ResponseEntity<Map<String, Object>> searchUsers(@RequestParam String q, Authentication authentication) {
        System.out.println("=== 用户搜索API被调用 ===");
        System.out.println("搜索关键词: " + q);

        Map<String, Object> response = new HashMap<>();

        try {
            // 使用真实的用户搜索
            List<User> users;
            if (q == null || q.trim().isEmpty()) {
                // 如果搜索词为空，返回前10个用户
                users = userService.getAllUsers().stream().limit(10).collect(Collectors.toList());
            } else {
                // 根据用户名或昵称搜索
                users = userService.searchUsers(q);
            }
            System.out.println("搜索到用户数量: " + users.size());

            // 获取当前用户信息（如果已登录）
            User currentUser = null;
            if (authentication != null) {
                Optional<User> currentUserOpt = userService.findByUsername(authentication.getName());
                if (currentUserOpt.isPresent()) {
                    currentUser = currentUserOpt.get();
                }
            }

            List<Map<String, Object>> userList = new ArrayList<>();

            for (User user : users) {
                System.out.println("找到用户: " + user.getUsername() + " (" + user.getNickname() + ")");
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("username", user.getUsername());
                userInfo.put("nickname", user.getNickname() != null ? user.getNickname() : user.getUsername());
                userInfo.put("avatar", user.getAvatar());
                userInfo.put("email", user.getEmail());

                // 添加关注状态信息
                if (currentUser != null && !currentUser.getId().equals(user.getId())) {
                    boolean isFollowing = followService.isFollowing(currentUser.getId(), user.getId());
                    boolean isMutual = followService.isMutualFollow(currentUser.getId(), user.getId());
                    userInfo.put("isFollowing", isFollowing);
                    userInfo.put("isMutual", isMutual);
                } else {
                    userInfo.put("isFollowing", false);
                    userInfo.put("isMutual", false);
                }

                userList.add(userInfo);
            }

            response.put("success", true);
            response.put("users", userList);
            response.put("data", userList); // 为了兼容前端，同时提供data字段
            System.out.println("搜索成功，返回结果");
        } catch (Exception e) {
            System.out.println("搜索失败: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "搜索失败: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 简单的测试端点
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testEndpoint() {
        System.out.println("=== 测试端点被调用 ===");
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "测试端点工作正常");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * 调试用户数据
     */
    @GetMapping("/debug/users")
    public ResponseEntity<Map<String, Object>> debugUsers() {
        System.out.println("=== 调试用户数据 ===");
        Map<String, Object> response = new HashMap<>();

        try {
            // 获取所有用户
            List<User> allUsers = userService.getAllUsers();
            System.out.println("数据库中总用户数: " + allUsers.size());

            List<Map<String, Object>> userList = new ArrayList<>();
            for (User user : allUsers) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("username", user.getUsername());
                userInfo.put("nickname", user.getNickname());
                userInfo.put("email", user.getEmail());
                userInfo.put("deleted", user.isDeleted());
                userInfo.put("createdAt", user.getCreatedAt());
                userList.add(userInfo);
                System.out.println("用户: " + user.getUsername() + " (ID: " + user.getId() + ", 删除: " + user.isDeleted() + ")");
            }

            response.put("success", true);
            response.put("totalUsers", allUsers.size());
            response.put("users", userList);

        } catch (Exception e) {
            System.out.println("调试失败: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "调试失败: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }
}