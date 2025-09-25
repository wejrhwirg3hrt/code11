package org.example.controller.api;

import org.example.entity.User;
import org.example.entity.Message;
import org.example.entity.Conversation;
import org.example.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.example.service.UserService;
import org.example.service.FileUploadService;
import org.example.service.FollowService;
import org.example.util.AvatarUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.DirectoryStream;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;

/**
 * 聊天API控制器
 */
@RestController
@RequestMapping("/api/chat")
public class ChatApiController {

    @Autowired
    private UserService userService;

    @Autowired
    private org.example.repository.MessageRepository messageRepository;

    @Autowired
    private FollowService followService;

    /**
     * 获取用户信息
     */
    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }

        User user = userService.findByUsername(userDetails.getUsername()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "用户不存在"));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("avatar", user.getAvatar());
        result.put("email", user.getEmail());

        return ResponseEntity.ok(result);
    }

    /**
     * 获取会话列表
     */
    @GetMapping("/conversations")
    public ResponseEntity<Map<String, Object>> getConversations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }

        try {
            System.out.println("DEBUG: 开始获取会话列表，用户: " + userDetails.getUsername());
            
            User currentUser = userService.findByUsername(userDetails.getUsername()).orElse(null);
            if (currentUser == null) {
                System.out.println("DEBUG: 用户不存在: " + userDetails.getUsername());
                return ResponseEntity.status(401).body(Map.of("error", "用户不存在"));
            }

            System.out.println("DEBUG: 用户ID: " + currentUser.getId() + ", 用户名: " + currentUser.getUsername());

            Page<Conversation> conversationsPage = chatService.getUserConversations(currentUser.getId(), page, size);
            System.out.println("DEBUG: 查询到会话数量: " + conversationsPage.getTotalElements());

            List<Map<String, Object>> conversations = new ArrayList<>();
            for (Conversation conversation : conversationsPage.getContent()) {
                System.out.println("DEBUG: 处理会话 ID: " + conversation.getId() + ", 类型: " + conversation.getType());
                
                try {
                Map<String, Object> convMap = convertConversationToMap(conversation);

                // 添加对方用户信息（对于私聊）
                if (conversation.getType() == Conversation.ConversationType.PRIVATE) {
                        System.out.println("DEBUG: 获取私聊对方用户信息，会话ID: " + conversation.getId());
                    // 获取对方用户信息
                    User otherUser = chatService.getOtherUserInPrivateConversation(conversation.getId(), currentUser.getId());
                    if (otherUser != null) {
                            System.out.println("DEBUG: 找到对方用户: " + otherUser.getUsername());
                        convMap.put("otherUser", Map.of(
                            "id", otherUser.getId(),
                            "username", otherUser.getUsername(),
                            "nickname", otherUser.getNickname() != null ? otherUser.getNickname() : otherUser.getUsername(),
                            "avatar", AvatarUtil.getUserAvatarUrl(otherUser)
                        ));
                        convMap.put("title", otherUser.getNickname() != null ? otherUser.getNickname() : otherUser.getUsername());
                        } else {
                            System.out.println("DEBUG: 未找到对方用户");
                    }
                }

                conversations.add(convMap);
                } catch (Exception e) {
                    System.err.println("DEBUG: 处理会话时出错，会话ID: " + conversation.getId() + ", 错误: " + e.getMessage());
                    e.printStackTrace();
                    // 继续处理其他会话，不中断整个请求
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("conversations", conversations);
            result.put("totalPages", conversationsPage.getTotalPages());
            result.put("totalElements", conversationsPage.getTotalElements());
            result.put("currentPage", page);

            System.out.println("DEBUG: 会话列表获取成功，返回会话数量: " + conversations.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("DEBUG: 获取会话列表时发生异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @Autowired
    private ChatService chatService;

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private org.example.service.UserOnlineStatusService userOnlineStatusService;

    /**
     * 发送文本消息
     */
    @PostMapping("/messages/text")
    public ResponseEntity<Map<String, Object>> sendTextMessage(
            @RequestParam Long conversationId,
            @RequestParam String content,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }

        try {
            System.out.println("DEBUG: 开始发送消息，会话ID: " + conversationId + ", 内容: " + content);

            User currentUser = userService.findByUsername(userDetails.getUsername()).orElse(null);
            if (currentUser == null) {
                System.out.println("DEBUG: 发送消息时用户不存在: " + userDetails.getUsername());
                return ResponseEntity.status(401).body(Map.of("error", "用户不存在"));
            }

            System.out.println("DEBUG: 发送消息的用户ID: " + currentUser.getId() + ", 用户名: " + currentUser.getUsername());

            Message message = chatService.sendMessage(conversationId, currentUser.getId(),
                Message.MessageType.TEXT, content);

            // 通过WebSocket发送实时消息通知
            Map<String, Object> messageMap = convertMessageToMap(message);
            messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, messageMap);
            System.out.println("DEBUG: 已通过WebSocket发送消息通知到会话: " + conversationId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", messageMap);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 发送回复消息
     */
    @PostMapping("/messages/reply")
    public ResponseEntity<Map<String, Object>> sendReplyMessage(
            @RequestParam Long conversationId,
            @RequestParam String content,
            @RequestParam(required = false) Long replyToMessageId,
            @RequestParam(required = false) String mentionedUsers,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }

        try {
            System.out.println("DEBUG: 开始发送回复消息，会话ID: " + conversationId + ", 内容: " + content + ", 回复消息ID: " + replyToMessageId);

            User currentUser = userService.findByUsername(userDetails.getUsername()).orElse(null);
            if (currentUser == null) {
                System.out.println("DEBUG: 发送回复消息时用户不存在: " + userDetails.getUsername());
                return ResponseEntity.status(401).body(Map.of("error", "用户不存在"));
            }

            System.out.println("DEBUG: 发送回复消息的用户ID: " + currentUser.getId() + ", 用户名: " + currentUser.getUsername());

            Message message = chatService.sendReplyMessage(conversationId, currentUser.getId(),
                Message.MessageType.TEXT, content, replyToMessageId, mentionedUsers);

            // 通过WebSocket发送实时消息通知
            Map<String, Object> messageMap = convertMessageToMap(message);
            messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, messageMap);
            System.out.println("DEBUG: 已通过WebSocket发送回复消息通知到会话: " + conversationId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", messageMap);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 发送文件消息
     */
    @PostMapping("/messages/file")
    public ResponseEntity<Map<String, Object>> sendFileMessage(
            @RequestParam Long conversationId,
            @RequestParam("files") MultipartFile[] files,
            @AuthenticationPrincipal UserDetails userDetails) {

        System.out.println("DEBUG: 文件上传API被调用");
        System.out.println("DEBUG: 会话ID: " + conversationId);
        System.out.println("DEBUG: 用户: " + (userDetails != null ? userDetails.getUsername() : "null"));
        System.out.println("DEBUG: 文件数量: " + (files != null ? files.length : 0));

        if (userDetails == null) {
            System.out.println("DEBUG: 用户未登录");
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }

        if (files == null || files.length == 0) {
            System.out.println("DEBUG: 没有选择文件");
            return ResponseEntity.status(400).body(Map.of("error", "请选择要发送的文件"));
        }

        // 检查文件数量限制（最多9个文件）
        if (files.length > 9) {
            System.out.println("DEBUG: 文件数量超过限制: " + files.length);
            return ResponseEntity.status(400).body(Map.of("error", "最多只能同时发送9个文件"));
        }

        try {
            User currentUser = userService.findByUsername(userDetails.getUsername()).orElse(null);
            if (currentUser == null) {
                System.out.println("DEBUG: 用户不存在: " + userDetails.getUsername());
                return ResponseEntity.status(401).body(Map.of("error", "用户不存在"));
            }

            System.out.println("DEBUG: 当前用户ID: " + currentUser.getId());
            List<Map<String, Object>> uploadedFiles = new ArrayList<>();

            for (int i = 0; i < files.length; i++) {
                MultipartFile file = files[i];
                System.out.println("DEBUG: 处理文件 " + (i + 1) + "/" + files.length + ": " + file.getOriginalFilename());
                System.out.println("DEBUG: 文件大小: " + file.getSize() + " bytes");
                System.out.println("DEBUG: 文件类型: " + file.getContentType());

                if (file.isEmpty()) {
                    System.out.println("DEBUG: 跳过空文件");
                    continue;
                }

                // 检查文件大小（限制为50MB）
                if (file.getSize() > 50 * 1024 * 1024) {
                    System.out.println("DEBUG: 文件超过大小限制: " + file.getOriginalFilename());
                    return ResponseEntity.status(400).body(Map.of("error", "文件 " + file.getOriginalFilename() + " 超过50MB限制"));
                }

                // 实际上传文件
                String fileUrl;
                Message.MessageType messageType;

                try {
                    System.out.println("DEBUG: 开始上传文件: " + file.getOriginalFilename());

                    if (isImageFile(file.getContentType())) {
                        System.out.println("DEBUG: 识别为图片文件");
                        fileUrl = fileUploadService.uploadImage(file);
                        messageType = Message.MessageType.IMAGE;
                    } else if (isVideoFile(file.getContentType())) {
                        System.out.println("DEBUG: 识别为视频文件");
                        fileUrl = fileUploadService.uploadChatFile(file);
                        messageType = Message.MessageType.VIDEO;
                    } else if (isAudioFile(file.getContentType())) {
                        System.out.println("DEBUG: 识别为音频文件");
                        fileUrl = fileUploadService.uploadVoice(file);
                        messageType = Message.MessageType.VOICE;
                    } else {
                        System.out.println("DEBUG: 识别为普通文件");
                        fileUrl = fileUploadService.uploadChatFile(file);
                        messageType = Message.MessageType.FILE;
                    }

                    System.out.println("DEBUG: 文件上传成功，URL: " + fileUrl);

                    // 发送文件消息
                    System.out.println("DEBUG: 保存消息到数据库");
                    Message message = chatService.sendFileMessage(
                        conversationId,
                        currentUser.getId(),
                        messageType,
                        "", // 空内容，文件消息主要是文件
                        fileUrl,
                        file.getOriginalFilename(),
                        file.getSize()
                    );

                    System.out.println("DEBUG: 消息保存成功，ID: " + message.getId());

                    Map<String, Object> fileInfo = convertMessageToMap(message);
                    uploadedFiles.add(fileInfo);

                    // 通过WebSocket发送实时消息通知
                    try {
                        messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, fileInfo);
                        System.out.println("DEBUG: 已通过WebSocket发送文件消息通知");
                    } catch (Exception wsException) {
                        System.err.println("DEBUG: WebSocket通知失败: " + wsException.getMessage());
                        // WebSocket失败不影响文件上传成功
                    }

                } catch (IOException e) {
                    System.err.println("DEBUG: 文件上传IO异常: " + e.getMessage());
                    e.printStackTrace();
                    return ResponseEntity.status(500).body(Map.of("error", "文件上传失败: " + e.getMessage()));
                } catch (Exception e) {
                    System.err.println("DEBUG: 文件处理异常: " + e.getMessage());
                    e.printStackTrace();
                    return ResponseEntity.status(500).body(Map.of("error", "文件处理失败: " + e.getMessage()));
                }
            }

            System.out.println("DEBUG: 所有文件处理完成，成功上传: " + uploadedFiles.size() + " 个文件");

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "文件发送成功");
            result.put("files", uploadedFiles);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("DEBUG: 文件上传总体异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "文件上传失败: " + e.getMessage()));
        }
    }

    /**
     * 简化的文件上传测试API
     */
    @PostMapping("/messages/file/test")
    public ResponseEntity<Map<String, Object>> testFileUpload(
            @RequestParam("files") MultipartFile[] files,
            @AuthenticationPrincipal UserDetails userDetails) {

        System.out.println("DEBUG: 测试文件上传API被调用");

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }

        if (files == null || files.length == 0) {
            return ResponseEntity.status(400).body(Map.of("error", "请选择要上传的文件"));
        }

        try {
            List<Map<String, Object>> uploadedFiles = new ArrayList<>();

            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;

                System.out.println("DEBUG: 测试上传文件: " + file.getOriginalFilename());

                // 只测试文件上传，不保存到数据库
                String fileUrl = fileUploadService.uploadChatFile(file);

                Map<String, Object> fileInfo = new HashMap<>();
                fileInfo.put("fileName", file.getOriginalFilename());
                fileInfo.put("fileSize", file.getSize());
                fileInfo.put("fileUrl", fileUrl);
                fileInfo.put("contentType", file.getContentType());

                uploadedFiles.add(fileInfo);
                System.out.println("DEBUG: 测试上传成功: " + fileUrl);
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "测试上传成功",
                "files", uploadedFiles
            ));

        } catch (Exception e) {
            System.err.println("DEBUG: 测试上传失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "测试上传失败: " + e.getMessage()));
        }
    }

    /**
     * 撤回消息
     */
    @PostMapping("/messages/{messageId}/recall")
    public ResponseEntity<Map<String, Object>> recallMessage(
            @PathVariable Long messageId,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }

        try {
            User currentUser = userService.findByUsername(userDetails.getUsername()).orElse(null);
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("error", "用户不存在"));
            }

            boolean success = chatService.recallMessage(messageId, currentUser.getId());
            if (success) {
                // 通过WebSocket通知撤回
                Map<String, Object> notification = new HashMap<>();
                notification.put("type", "message_recalled");
                notification.put("messageId", messageId);
                notification.put("userId", currentUser.getId());

                // 这里需要获取会话ID来发送通知，暂时先返回成功
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "消息撤回成功");
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "撤回失败，可能超过时间限制或无权限"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 删除消息
     */
    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Map<String, Object>> deleteMessage(
            @PathVariable Long messageId,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }

        try {
            User currentUser = userService.findByUsername(userDetails.getUsername()).orElse(null);
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("error", "用户不存在"));
            }

            boolean success = chatService.deleteMessage(messageId, currentUser.getId());
            if (success) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "消息删除成功");
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "删除失败或无权限"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 清空会话消息
     */
    @DeleteMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<Map<String, Object>> clearConversationMessages(
            @PathVariable Long conversationId,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }

        try {
            User currentUser = userService.findByUsername(userDetails.getUsername()).orElse(null);
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("error", "用户不存在"));
            }

            boolean success = chatService.clearConversationMessages(conversationId, currentUser.getId());
            if (success) {
                // 通过WebSocket通知清空消息
                Map<String, Object> notification = new HashMap<>();
                notification.put("type", "messages_cleared");
                notification.put("conversationId", conversationId);
                notification.put("userId", currentUser.getId());
                messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, notification);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "聊天记录清空成功");
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "清空失败或无权限"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/conversations/{conversationId}")
    public ResponseEntity<Map<String, Object>> deleteConversation(
            @PathVariable Long conversationId,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }

        try {
            User currentUser = userService.findByUsername(userDetails.getUsername()).orElse(null);
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("error", "用户不存在"));
            }

            boolean success = chatService.deleteConversation(conversationId, currentUser.getId());
            if (success) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "会话删除成功");
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "删除失败或无权限"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 调试API - 查看消息数据
     */
    @GetMapping("/debug/messages/{conversationId}")
    public ResponseEntity<Map<String, Object>> debugMessages(
            @PathVariable Long conversationId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // 简化权限检查，允许调试
            System.out.println("调试API被调用，会话ID: " + conversationId);

            // 对于调试API，使用一个默认用户ID（1）来绕过权限检查
            Page<Message> messagesPage = chatService.getConversationMessages(conversationId, 1L, 0, 10);
            List<Map<String, Object>> messages = messagesPage.getContent().stream()
                .map(this::convertMessageToMap)
                .collect(java.util.stream.Collectors.toList());

            System.out.println("找到 " + messages.size() + " 条消息");

            return ResponseEntity.ok(Map.of(
                "success", true,
                "messages", messages,
                "totalMessages", messagesPage.getTotalElements(),
                "conversationId", conversationId
            ));
        } catch (Exception e) {
            System.err.println("调试API错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage(),
                "conversationId", conversationId
            ));
        }
    }

    /**
     * 图片代理API - 根据文件名查找实际图片
     */
    @GetMapping("/image/{fileName}")
    public ResponseEntity<org.springframework.core.io.Resource> getImage(@PathVariable String fileName) {
        try {
            System.out.println("图片代理API被调用，文件名: " + fileName);

            // 优先在聊天相关目录中查找
            String[] searchDirs = {"chat/files", "chat/images", "chat/videos", "chat/voices", "private", "moments", "avatars", "thumbnails", "videos"};
            String[] extensions = {".jpeg", ".jpg", ".png", ".gif", ".webp", ".mp4", ".mp3", ".wav", ".ogg"};

            String baseUploadPath = "E:/code11/uploads";

            for (String dir : searchDirs) {
                java.io.File dirFile = new java.io.File(baseUploadPath, dir);
                System.out.println("搜索目录: " + dirFile.getAbsolutePath());

                if (dirFile.exists() && dirFile.isDirectory()) {
                    // 首先尝试精确匹配（完整文件名）
                    java.io.File exactFile = new java.io.File(dirFile, fileName);
                    if (exactFile.exists()) {
                        System.out.println("找到精确匹配文件: " + exactFile.getAbsolutePath());
                        org.springframework.core.io.Resource resource = new org.springframework.core.io.FileSystemResource(exactFile);
                        String contentType = getContentType(fileName);
                        return ResponseEntity.ok()
                            .header("Content-Type", contentType)
                            .header("Cache-Control", "no-cache")
                            .body(resource);
                    }

                    // 然后尝试添加扩展名的精确匹配
                    for (String ext : extensions) {
                        java.io.File extFile = new java.io.File(dirFile, fileName + ext);
                        if (extFile.exists()) {
                            System.out.println("找到扩展名匹配文件: " + extFile.getAbsolutePath());
                            org.springframework.core.io.Resource resource = new org.springframework.core.io.FileSystemResource(extFile);
                            String contentType = getContentType(extFile.getName());
                            return ResponseEntity.ok()
                                .header("Content-Type", contentType)
                                .header("Cache-Control", "no-cache")
                                .body(resource);
                        }
                    }

                    // 最后尝试模糊匹配（包含文件名的文件）
                    java.io.File[] files = dirFile.listFiles();
                    if (files != null) {
                        for (java.io.File file : files) {
                            String name = file.getName().toLowerCase();
                            if (name.contains(fileName.toLowerCase()) || fileName.toLowerCase().contains(name.replace(".jpg", "").replace(".jpeg", "").replace(".png", ""))) {
                                System.out.println("找到模糊匹配文件: " + file.getAbsolutePath());
                                org.springframework.core.io.Resource resource = new org.springframework.core.io.FileSystemResource(file);
                                String contentType = getContentType(file.getName());
                                return ResponseEntity.ok()
                                    .header("Content-Type", contentType)
                                    .header("Cache-Control", "no-cache")
                                    .body(resource);
                            }
                        }
                    }
                }
            }

            System.out.println("未找到文件: " + fileName);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("图片代理API错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 聊天文件访问API - 直接通过文件名访问聊天文件
     */
    @GetMapping("/file/{fileName}")
    public ResponseEntity<org.springframework.core.io.Resource> getChatFile(@PathVariable String fileName) {
        try {
            System.out.println("聊天文件API被调用，文件名: " + fileName);

            String baseUploadPath = "E:/code11/uploads";
            String[] chatDirs = {"chat/files", "chat/images", "chat/videos", "chat/voices"};
            String[] extensions = {".jpeg", ".jpg", ".png", ".gif", ".webp", ".mp4", ".mp3", ".wav", ".ogg"};

            // 首先尝试精确匹配
            for (String dir : chatDirs) {
                Path filePath = Paths.get(baseUploadPath, dir, fileName);
                File file = filePath.toFile();

                if (file.exists() && file.isFile()) {
                    System.out.println("找到聊天文件(精确匹配): " + filePath);

                    org.springframework.core.io.Resource resource = new FileSystemResource(file);
                    String contentType = getContentType(fileName);

                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_TYPE, contentType)
                            .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                            .body(resource);
                }
            }

            // 然后尝试添加扩展名
            for (String dir : chatDirs) {
                for (String ext : extensions) {
                    Path filePath = Paths.get(baseUploadPath, dir, fileName + ext);
                    File file = filePath.toFile();

                    if (file.exists() && file.isFile()) {
                        System.out.println("找到聊天文件(扩展名匹配): " + filePath);

                        org.springframework.core.io.Resource resource = new FileSystemResource(file);
                        String contentType = getContentType(file.getName());

                        return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_TYPE, contentType)
                                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                                .body(resource);
                    }
                }
            }

            // 最后尝试模糊匹配
            for (String dir : chatDirs) {
                File dirFile = new File(baseUploadPath, dir);
                if (dirFile.exists() && dirFile.isDirectory()) {
                    File[] files = dirFile.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            String name = file.getName().toLowerCase();
                            String searchName = fileName.toLowerCase();

                            // 移除扩展名进行比较
                            String nameWithoutExt = name.replaceAll("\\.[^.]+$", "");
                            String searchWithoutExt = searchName.replaceAll("\\.[^.]+$", "");

                            if (nameWithoutExt.equals(searchWithoutExt) || name.contains(searchName) || searchName.contains(nameWithoutExt)) {
                                System.out.println("找到聊天文件(模糊匹配): " + file.getAbsolutePath());

                                org.springframework.core.io.Resource resource = new FileSystemResource(file);
                                String contentType = getContentType(file.getName());

                                return ResponseEntity.ok()
                                        .header(HttpHeaders.CONTENT_TYPE, contentType)
                                        .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                                        .body(resource);
                            }
                        }
                    }
                }
            }

            System.out.println("聊天文件未找到: " + fileName);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            System.err.println("获取聊天文件失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    private String getContentType(String fileName) {
        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerName.endsWith(".png")) {
            return "image/png";
        } else if (lowerName.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerName.endsWith(".webp")) {
            return "image/webp";
        } else if (lowerName.endsWith(".mp4")) {
            return "video/mp4";
        }
        return "application/octet-stream";
    }

    /**
     * 检查聊天权限
     */
    @GetMapping("/permission/{targetUserId}")
    public ResponseEntity<Map<String, Object>> checkChatPermission(
            @PathVariable Long targetUserId,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }

        try {
            User currentUser = userService.findByUsername(userDetails.getUsername()).orElse(null);
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("error", "用户不存在"));
            }

            User targetUser = userService.findById(targetUserId).orElse(null);
            if (targetUser == null) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "allowed", false,
                    "reason", "目标用户不存在"
                ));
            }

            // 不能和自己聊天
            if (currentUser.getId().equals(targetUserId)) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "allowed", false,
                    "reason", "不能和自己聊天"
                ));
            }

            // 检查是否可以发送消息（暂时允许所有用户聊天，用于测试）
            boolean canSend = true; // chatService.canSendMessage(currentUser.getId(), targetUserId);
            String followStatus = chatService.getFollowStatusForChat(currentUser.getId(), targetUserId);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "allowed", canSend,
                "followStatus", followStatus,
                "reason", canSend ? "允许聊天" : "需要先关注对方才能发起聊天"
            ));

        } catch (Exception e) {
            System.err.println("检查聊天权限时出错: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "检查权限失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取或创建私聊会话
     */
    @PostMapping("/conversations/private")
    public ResponseEntity<Map<String, Object>> getOrCreatePrivateConversation(
            @RequestParam Long targetUserId,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }

        try {
            System.out.println("DEBUG: 开始创建私聊会话，用户名: " + userDetails.getUsername() + ", 目标用户ID: " + targetUserId);

            User currentUser = userService.findByUsername(userDetails.getUsername()).orElse(null);
            if (currentUser == null) {
                System.out.println("DEBUG: 当前用户不存在: " + userDetails.getUsername());
                return ResponseEntity.status(401).body(Map.of("error", "用户不存在"));
            }

            System.out.println("DEBUG: 当前用户ID: " + currentUser.getId() + ", 用户名: " + currentUser.getUsername());

            Conversation conversation = chatService.getOrCreatePrivateConversation(currentUser.getId(), targetUserId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("conversation", convertConversationToMap(conversation));

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取会话消息
     */
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<Map<String, Object>> getConversationMessages(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }

        try {
            User currentUser = userService.findByUsername(userDetails.getUsername()).orElse(null);
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("error", "用户不存在"));
            }
            
            Page<Message> messagesPage = chatService.getConversationMessages(conversationId, currentUser.getId(), page, size);

            List<Map<String, Object>> messages = new ArrayList<>();
            for (Message message : messagesPage.getContent()) {
                messages.add(convertMessageToMap(message));
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("messages", messages);
            result.put("totalPages", messagesPage.getTotalPages());
            result.put("totalElements", messagesPage.getTotalElements());
            result.put("currentPage", page);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取会话参与者
     */
    @GetMapping("/conversations/{conversationId}/participants")
    public ResponseEntity<Map<String, Object>> getConversationParticipants(
            @PathVariable Long conversationId,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }

        try {
            User currentUser = userService.findByUsername(userDetails.getUsername()).orElse(null);
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("error", "用户不存在"));
            }

            List<User> participants = chatService.getConversationParticipants(conversationId);

            List<Map<String, Object>> participantMaps = new ArrayList<>();
            for (User participant : participants) {
                participantMaps.add(Map.of(
                    "id", participant.getId(),
                    "username", participant.getUsername(),
                    "nickname", participant.getNickname() != null ? participant.getNickname() : participant.getUsername(),
                    "avatar", AvatarUtil.getUserAvatarUrl(participant)
                ));
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("participants", participantMaps);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取未读消息数量
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }

        // 暂时返回0，等实现完整功能后再完善
        Map<String, Object> result = new HashMap<>();
        result.put("count", 0);

        return ResponseEntity.ok(result);
    }

    /**
     * 获取在线用户数量
     */
    @GetMapping("/online-count")
    public ResponseEntity<Map<String, Object>> getOnlineCount() {
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("count", userOnlineStatusService.getOnlineUserCount());
            result.put("timestamp", System.currentTimeMillis());

            System.out.println("获取在线用户数量API被调用，当前在线用户数: " + userOnlineStatusService.getOnlineUserCount());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("获取在线用户数量失败: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "获取在线用户数量失败", "success", false));
        }
    }

    /**
     * 检查用户是否在线
     */
    @GetMapping("/user/{userId}/online-status")
    public ResponseEntity<Map<String, Object>> getUserOnlineStatus(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("userId", userId);
        result.put("isOnline", userOnlineStatusService.isUserOnline(userId));
        result.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(result);
    }

    /**
     * 更新用户活跃状态
     */
    @PostMapping("/user/heartbeat")
    public ResponseEntity<Map<String, Object>> updateUserHeartbeat(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }

        try {
            User currentUser = userService.findByUsername(userDetails.getUsername()).orElse(null);
            if (currentUser != null) {
                userOnlineStatusService.updateUserActivity(currentUser.getId());
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "更新活跃状态失败"));
        }
    }

    /**
     * 手动通知用户上线
     */
    @PostMapping("/user/online")
    public ResponseEntity<Map<String, Object>> notifyUserOnline(
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }

        try {
            User currentUser = userService.findByUsername(userDetails.getUsername()).orElse(null);
            if (currentUser != null) {
                // 生成一个模拟的会话ID
                String sessionId = "manual-" + System.currentTimeMillis();
                userOnlineStatusService.userOnline(currentUser.getId(), sessionId);
                userOnlineStatusService.broadcastOnlineCount();

                System.out.println("手动设置用户上线: " + currentUser.getUsername() + " (ID: " + currentUser.getId() + ")");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("count", userOnlineStatusService.getOnlineUserCount());
            result.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "设置用户上线状态失败"));
        }
    }

    /**
     * 标记会话为已读
     */
    @PostMapping("/conversations/{conversationId}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(
            @PathVariable Long conversationId,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }

        // 暂时返回成功响应，等实现完整功能后再完善
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);

        return ResponseEntity.ok(result);
    }

    /**
     * 搜索用户
     */
    @GetMapping("/users/search")
    public ResponseEntity<Map<String, Object>> searchUsers(
            @RequestParam String keyword,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }

        // 如果关键词为空，返回所有用户（排除当前用户）
        if (keyword == null || keyword.trim().isEmpty()) {
            try {
                User currentUser = userService.findByUsername(userDetails.getUsername()).orElse(null);
                if (currentUser == null) {
                    return ResponseEntity.status(401).body(Map.of("error", "用户不存在"));
                }

                // 获取所有用户（排除当前用户）
                List<User> allUsers = userService.getAllActiveUsersExcluding(currentUser.getId());

                List<Map<String, Object>> userList = allUsers.stream().map(user -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", user.getId());
                    userMap.put("username", user.getUsername());
                    userMap.put("nickname", user.getNickname() != null ? user.getNickname() : user.getUsername());
                    userMap.put("avatar", user.getAvatar() != null ? user.getAvatar() : "/images/default-avatar.png");
                    userMap.put("email", user.getEmail());
                    return userMap;
                }).toList();

                return ResponseEntity.ok(Map.of("users", userList, "total", userList.size()));
            } catch (Exception e) {
                return ResponseEntity.status(500).body(Map.of("error", "获取用户列表失败: " + e.getMessage()));
            }
        }

        try {
            // 搜索用户（排除当前用户）
            User currentUser = userService.findByUsername(userDetails.getUsername()).orElse(null);
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("error", "用户不存在"));
            }

            // 根据用户名或昵称搜索用户
            List<User> users = userService.searchUsers(keyword.trim(), currentUser.getId());

            // 转换为前端需要的格式
            List<Map<String, Object>> userList = users.stream().map(user -> {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("username", user.getUsername());
                userMap.put("nickname", user.getNickname() != null ? user.getNickname() : user.getUsername());
                userMap.put("avatar", user.getAvatar() != null ? user.getAvatar() : "/images/default-avatar.png");
                userMap.put("email", user.getEmail());
                return userMap;
            }).toList();

            Map<String, Object> result = new HashMap<>();
            result.put("users", userList);
            result.put("total", userList.size());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "搜索失败: " + e.getMessage()));
        }
    }

    /**
     * 检查是否是图片文件
     */
    private boolean isImageFile(String contentType) {
        if (contentType == null) return false;
        return contentType.startsWith("image/");
    }

    /**
     * 检查是否是视频文件
     */
    private boolean isVideoFile(String contentType) {
        if (contentType == null) return false;
        return contentType.startsWith("video/");
    }

    /**
     * 检查是否是音频文件
     */
    private boolean isAudioFile(String contentType) {
        if (contentType == null) return false;
        return contentType.startsWith("audio/");
    }

    /**
     * 将Message实体转换为Map
     */
    private Map<String, Object> convertMessageToMap(Message message) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", message.getId());
        map.put("conversationId", message.getConversation().getId());
        map.put("senderId", message.getSender().getId());
        map.put("senderName", message.getSender().getUsername());
        map.put("senderAvatar", AvatarUtil.getUserAvatarUrl(message.getSender()));
        map.put("messageType", message.getType().name().toLowerCase());
        map.put("content", message.getContent());
        map.put("fileUrl", message.getFileUrl());
        map.put("fileName", message.getFileName());
        map.put("fileSize", message.getFileSize());
        map.put("thumbnailUrl", message.getThumbnailUrl());
        map.put("duration", message.getDuration());
        map.put("timestamp", message.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        map.put("isRecalled", message.getIsRecalled());

        // 添加回复信息
        if (message.getReplyTo() != null) {
            Map<String, Object> replyToMap = new HashMap<>();
            replyToMap.put("id", message.getReplyTo().getId());
            replyToMap.put("senderId", message.getReplyTo().getSender().getId());
            replyToMap.put("senderName", message.getReplyTo().getSender().getUsername());
            replyToMap.put("content", message.getReplyTo().getContent());
            replyToMap.put("messageType", message.getReplyTo().getType().name().toLowerCase());
            replyToMap.put("timestamp", message.getReplyTo().getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            map.put("replyTo", replyToMap);
        }

        // 添加@用户信息
        if (message.getMentionedUsers() != null && !message.getMentionedUsers().trim().isEmpty()) {
            map.put("mentionedUsers", message.getMentionedUsers());
        }

        return map;
    }

    /**
     * 将Conversation实体转换为Map
     */
    private Map<String, Object> convertConversationToMap(Conversation conversation) {
        Map<String, Object> map = new HashMap<>();
        
        try {
        map.put("id", conversation.getId());
        map.put("type", conversation.getType().name().toLowerCase());
        map.put("title", conversation.getTitle());
        map.put("description", conversation.getDescription());
        map.put("avatarUrl", conversation.getAvatarUrl());
            
            // 安全获取创建者信息
            if (conversation.getCreatedBy() != null) {
        map.put("createdBy", conversation.getCreatedBy().getId());
            } else {
                map.put("createdBy", null);
            }
            
            // 安全格式化时间
            if (conversation.getCreatedAt() != null) {
        map.put("createdAt", conversation.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            } else {
                map.put("createdAt", null);
            }
            
            if (conversation.getLastMessageAt() != null) {
        map.put("lastMessageAt", conversation.getLastMessageAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            } else {
                map.put("lastMessageAt", null);
            }
            
        map.put("isActive", conversation.getIsActive());

        // 获取最后一条消息
        try {
            Message lastMessage = chatService.getLastMessageInConversation(conversation.getId());
            if (lastMessage != null) {
                String messagePreview = lastMessage.getContent();
                if (messagePreview != null && messagePreview.length() > 50) {
                    messagePreview = messagePreview.substring(0, 50) + "...";
                }
                map.put("lastMessage", messagePreview);
                    
                    if (lastMessage.getCreatedAt() != null) {
                map.put("lastMessageTime", lastMessage.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    } else {
                        map.put("lastMessageTime", null);
                    }
                    
                    if (lastMessage.getSender() != null) {
                map.put("lastMessageSender", lastMessage.getSender().getNickname() != null ?
                    lastMessage.getSender().getNickname() : lastMessage.getSender().getUsername());
                    } else {
                        map.put("lastMessageSender", null);
                    }
            } else {
                map.put("lastMessage", null);
                map.put("lastMessageTime", null);
                map.put("lastMessageSender", null);
            }
        } catch (Exception e) {
                System.err.println("DEBUG: 获取最后一条消息失败，会话ID: " + conversation.getId() + ", 错误: " + e.getMessage());
            map.put("lastMessage", null);
            map.put("lastMessageTime", null);
            map.put("lastMessageSender", null);
            }
        } catch (Exception e) {
            System.err.println("DEBUG: 转换会话数据失败，会话ID: " + conversation.getId() + ", 错误: " + e.getMessage());
            e.printStackTrace();
        }

        return map;
    }

    /**
     * 数据库状态测试端点
     */
    @GetMapping("/test/database")
    public ResponseEntity<Map<String, Object>> testDatabase() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("timestamp", java.time.LocalDateTime.now());

        try {
            // 简单的数据库连接测试
            User testUser = userService.findByUsername("testuser").orElse(null);
            response.put("database", Map.of(
                "status", "connected",
                "test_user_exists", testUser != null
            ));
        } catch (Exception e) {
            response.put("database_error", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 搜索用户（用于@功能）- 只搜索当前会话的参与者
     */
    @GetMapping("/users/mention")
    public ResponseEntity<Map<String, Object>> searchUsersForMention(
            @RequestParam String query,
            @RequestParam Long conversationId,
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }

        try {
            User currentUser = userService.findByUsername(userDetails.getUsername()).orElse(null);
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("error", "用户不存在"));
            }

            // 获取当前会话的参与者
            List<User> conversationUsers = chatService.getConversationParticipants(conversationId);

            // 过滤掉当前用户，并根据查询字符串筛选
            List<User> filteredUsers = conversationUsers.stream()
                .filter(user -> !user.getId().equals(currentUser.getId())) // 排除当前用户
                .filter(user -> query == null || query.trim().isEmpty() ||
                    user.getUsername().toLowerCase().contains(query.toLowerCase()))
                .limit(limit)
                .collect(java.util.stream.Collectors.toList());

            List<Map<String, Object>> userList = filteredUsers.stream()
                .map(user -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", user.getId());
                    userMap.put("username", user.getUsername());
                    userMap.put("avatar", AvatarUtil.getUserAvatarUrl(user));
                    return userMap;
                })
                .collect(java.util.stream.Collectors.toList());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "users", userList
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 取消关注用户
     */
    @PostMapping("/users/{userId}/unfollow")
    public ResponseEntity<Map<String, Object>> unfollowUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }

        try {
            User currentUser = userService.findByUsername(userDetails.getUsername()).orElse(null);
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("error", "用户不存在"));
            }

            // 获取目标用户
            Optional<User> targetUserOpt = userService.findById(userId);
            if (!targetUserOpt.isPresent()) {
                return ResponseEntity.status(404).body(Map.of("error", "目标用户不存在"));
            }

            User targetUser = targetUserOpt.get();

            // 调用关注服务取消关注
            boolean success = followService.unfollowUser(currentUser, targetUser);
            
            if (success) {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "取消关注成功"
            ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "取消关注失败"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 清理指向不存在文件的消息记录
     */
    @PostMapping("/cleanup-missing-files")
    public ResponseEntity<Map<String, Object>> cleanupMissingFiles() {
        try {
            System.out.println("🧹 开始清理指向不存在文件的消息记录...");

            // 查询所有图片类型的消息
            List<Message> imageMessages = messageRepository.findByType(Message.MessageType.IMAGE);
            int totalCount = imageMessages.size();
            int deletedCount = 0;
            List<String> deletedFiles = new ArrayList<>();

            String baseUploadPath = "E:/code11/uploads";
            String[] chatDirs = {"chat/files", "chat/images", "chat/videos", "chat/voices"};

            for (Message message : imageMessages) {
                String fileName = message.getFileName();

                boolean fileExists = false;

                // 检查文件是否存在
                if (fileName != null && !fileName.trim().isEmpty()) {
                    for (String dir : chatDirs) {
                        Path filePath = Paths.get(baseUploadPath, dir, fileName);
                        if (Files.exists(filePath)) {
                            fileExists = true;
                            break;
                        }
                    }
                }

                // 如果文件不存在，删除消息记录
                if (!fileExists) {
                    System.out.println("🗑️ 删除指向不存在文件的消息: " + fileName);
                    messageRepository.deleteById(message.getId());
                    deletedCount++;
                    deletedFiles.add(fileName != null ? fileName : "未知文件");
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("totalChecked", totalCount);
            result.put("deletedCount", deletedCount);
            result.put("deletedFiles", deletedFiles);
            result.put("message", "清理完成，删除了 " + deletedCount + " 条指向不存在文件的消息记录");

            System.out.println("✅ 清理完成: 检查了 " + totalCount + " 条消息，删除了 " + deletedCount + " 条无效记录");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("❌ 清理失败: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "清理失败: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 视频文件迁移到根目录静态资源库
     */
    @PostMapping("/migrate-videos-to-root")
    public ResponseEntity<Map<String, Object>> migrateVideosToRoot() {
        try {
            System.out.println("🚀 开始视频文件迁移到根目录静态资源库...");

            String rootUploadPath = "E:/code11/uploads";
            String projectUploadPath = System.getProperty("user.dir") + "/uploads";

            // 确保根目录存在
            Files.createDirectories(Paths.get(rootUploadPath, "chat", "videos"));
            Files.createDirectories(Paths.get(rootUploadPath, "videos"));

            Map<String, Object> result = new HashMap<>();
            List<String> migratedFiles = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            int totalMigrated = 0;

            // 1. 迁移聊天视频文件
            Path projectChatVideos = Paths.get(projectUploadPath, "chat", "videos");
            Path rootChatVideos = Paths.get(rootUploadPath, "chat", "videos");

            if (Files.exists(projectChatVideos)) {
                totalMigrated += migrateDirectory(projectChatVideos, rootChatVideos, migratedFiles, errors);
            }

            // 2. 迁移主要视频文件
            Path projectVideos = Paths.get(projectUploadPath, "videos");
            Path rootVideos = Paths.get(rootUploadPath, "videos");

            if (Files.exists(projectVideos)) {
                totalMigrated += migrateDirectory(projectVideos, rootVideos, migratedFiles, errors);
            }

            // 3. 更新数据库中的视频文件路径
            int updatedRecords = updateVideoPathsInDatabase(rootUploadPath);

            result.put("success", true);
            result.put("totalMigrated", totalMigrated);
            result.put("migratedFiles", migratedFiles);
            result.put("updatedDatabaseRecords", updatedRecords);
            result.put("errors", errors);
            result.put("message", "视频文件迁移完成，共迁移 " + totalMigrated + " 个文件，更新 " + updatedRecords + " 条数据库记录");

            System.out.println("✅ 视频文件迁移完成: 迁移了 " + totalMigrated + " 个文件");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("❌ 视频文件迁移失败: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "迁移失败: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 迁移目录中的所有文件
     */
    private int migrateDirectory(Path sourceDir, Path targetDir, List<String> migratedFiles, List<String> errors) {
        int count = 0;
        try {
            if (!Files.exists(sourceDir)) {
                return 0;
            }

            Files.createDirectories(targetDir);

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceDir)) {
                for (Path sourceFile : stream) {
                    if (Files.isRegularFile(sourceFile)) {
                        String fileName = sourceFile.getFileName().toString();
                        Path targetFile = targetDir.resolve(fileName);

                        try {
                            // 检查目标文件是否已存在
                            if (Files.exists(targetFile)) {
                                // 比较文件大小，如果相同则跳过
                                if (Files.size(sourceFile) == Files.size(targetFile)) {
                                    System.out.println("⏭️ 跳过已存在的文件: " + fileName);
                                    continue;
                                }
                            }

                            // 复制文件
                            Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);

                            // 验证复制是否成功
                            if (Files.exists(targetFile) && Files.size(sourceFile) == Files.size(targetFile)) {
                                migratedFiles.add(fileName);
                                count++;
                                System.out.println("✅ 迁移成功: " + fileName);
                            } else {
                                errors.add("文件复制验证失败: " + fileName);
                                System.err.println("❌ 文件复制验证失败: " + fileName);
                            }

                        } catch (Exception e) {
                            String error = "迁移文件失败 " + fileName + ": " + e.getMessage();
                            errors.add(error);
                            System.err.println("❌ " + error);
                        }
                    }
                }
            }
        } catch (Exception e) {
            String error = "迁移目录失败 " + sourceDir + ": " + e.getMessage();
            errors.add(error);
            System.err.println("❌ " + error);
        }
        return count;
    }

    /**
     * 更新数据库中的视频文件路径
     */
    private int updateVideoPathsInDatabase(String rootUploadPath) {
        try {
            // 查询所有视频类型的消息
            List<Message> videoMessages = messageRepository.findByType(Message.MessageType.VIDEO);
            int updatedCount = 0;

            for (Message message : videoMessages) {
                String fileName = message.getFileName();

                if (fileName != null && !fileName.trim().isEmpty()) {
                    // 构造新的文件路径
                    String newFileUrl = "/uploads/chat/videos/" + fileName;

                    // 验证文件是否存在于新位置
                    Path newFilePath = Paths.get(rootUploadPath, "chat", "videos", fileName);
                    if (Files.exists(newFilePath)) {
                        // 更新数据库记录
                        message.setFileUrl(newFileUrl);
                        messageRepository.save(message);
                        updatedCount++;

                        System.out.println("📝 更新数据库记录: " + fileName + " -> " + newFileUrl);
                    } else {
                        System.out.println("⚠️ 文件不存在，跳过更新: " + fileName);
                    }
                }
            }

            return updatedCount;

        } catch (Exception e) {
            System.err.println("❌ 更新数据库失败: " + e.getMessage());
            return 0;
        }
    }

    /**
     * 全面文件迁移 - 将所有文件迁移到根目录静态资源库
     */
    @PostMapping("/migrate-all-files-to-root")
    public ResponseEntity<Map<String, Object>> migrateAllFilesToRoot() {
        try {
            System.out.println("🚀 开始全面文件迁移到根目录静态资源库...");

            String rootUploadPath = "E:/code11/uploads";
            String projectUploadPath = System.getProperty("user.dir") + "/uploads";

            Map<String, Object> result = new HashMap<>();
            List<String> migratedFiles = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            int totalMigrated = 0;

            // 需要迁移的目录映射 (项目路径 -> 根目录路径)
            Map<String, String> migrationPaths = new HashMap<>();
            migrationPaths.put("chat/images", "chat/images");
            migrationPaths.put("chat/videos", "chat/videos");
            migrationPaths.put("chat/files", "chat/files");
            migrationPaths.put("chat/voices", "chat/voices");
            migrationPaths.put("chat/audios", "chat/audios");
            migrationPaths.put("videos", "videos");
            migrationPaths.put("thumbnails", "thumbnails");
            migrationPaths.put("avatars", "avatars");
            migrationPaths.put("music", "music");
            migrationPaths.put("covers", "covers");
            migrationPaths.put("moments", "moments");
            migrationPaths.put("private", "private");

            // 执行迁移
            for (Map.Entry<String, String> entry : migrationPaths.entrySet()) {
                String sourcePath = entry.getKey();
                String targetPath = entry.getValue();

                Path sourceDir = Paths.get(projectUploadPath, sourcePath);
                Path targetDir = Paths.get(rootUploadPath, targetPath);

                if (Files.exists(sourceDir)) {
                    System.out.println("📁 迁移目录: " + sourcePath);
                    int migrated = migrateDirectory(sourceDir, targetDir, migratedFiles, errors);
                    totalMigrated += migrated;
                    System.out.println("✅ 目录 " + sourcePath + " 迁移完成，共 " + migrated + " 个文件");
                } else {
                    System.out.println("⏭️ 跳过不存在的目录: " + sourcePath);
                }
            }

            // 更新数据库中的文件路径
            int updatedRecords = updateAllFilePathsInDatabase(rootUploadPath);

            result.put("success", true);
            result.put("totalMigrated", totalMigrated);
            result.put("migratedFiles", migratedFiles);
            result.put("updatedDatabaseRecords", updatedRecords);
            result.put("errors", errors);
            result.put("message", "全面文件迁移完成，共迁移 " + totalMigrated + " 个文件，更新 " + updatedRecords + " 条数据库记录");

            System.out.println("✅ 全面文件迁移完成: 迁移了 " + totalMigrated + " 个文件");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("❌ 全面文件迁移失败: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "迁移失败: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 更新数据库中的所有文件路径
     */
    private int updateAllFilePathsInDatabase(String rootUploadPath) {
        try {
            int totalUpdated = 0;

            // 更新聊天消息中的文件路径
            List<Message> fileMessages = messageRepository.findByType(Message.MessageType.IMAGE);
            fileMessages.addAll(messageRepository.findByType(Message.MessageType.VIDEO));
            fileMessages.addAll(messageRepository.findByType(Message.MessageType.FILE));

            for (Message message : fileMessages) {
                String fileName = message.getFileName();
                if (fileName != null && !fileName.trim().isEmpty()) {
                    String newFileUrl = determineNewFileUrl(message.getType(), fileName, rootUploadPath);
                    if (newFileUrl != null) {
                        message.setFileUrl(newFileUrl);
                        messageRepository.save(message);
                        totalUpdated++;
                        System.out.println("📝 更新消息文件路径: " + fileName + " -> " + newFileUrl);
                    }
                }
            }

            return totalUpdated;

        } catch (Exception e) {
            System.err.println("❌ 更新数据库失败: " + e.getMessage());
            return 0;
        }
    }

    /**
     * 根据文件类型确定新的文件URL
     */
    private String determineNewFileUrl(Message.MessageType type, String fileName, String rootUploadPath) {
        String subPath = null;

        switch (type) {
            case IMAGE:
                subPath = "chat/images";
                break;
            case VIDEO:
                subPath = "chat/videos";
                break;
            case FILE:
                subPath = "chat/files";
                break;
            default:
                return null;
        }

        // 验证文件是否存在于新位置
        Path filePath = Paths.get(rootUploadPath, subPath, fileName);
        if (Files.exists(filePath)) {
            return "/uploads/" + subPath + "/" + fileName;
        }

        return null;
    }

    /**
     * 验证迁移结果
     */
    @GetMapping("/verify-migration")
    public ResponseEntity<Map<String, Object>> verifyMigration() {
        try {
            System.out.println("🔍 开始验证文件迁移结果...");

            String rootUploadPath = "E:/code11/uploads";
            Map<String, Object> result = new HashMap<>();
            List<String> missingFiles = new ArrayList<>();
            List<String> validFiles = new ArrayList<>();

            // 检查聊天消息中的文件
            List<Message> fileMessages = messageRepository.findByType(Message.MessageType.IMAGE);
            fileMessages.addAll(messageRepository.findByType(Message.MessageType.VIDEO));
            fileMessages.addAll(messageRepository.findByType(Message.MessageType.FILE));

            for (Message message : fileMessages) {
                String fileName = message.getFileName();
                String fileUrl = message.getFileUrl();

                if (fileName != null && !fileName.trim().isEmpty()) {
                    // 根据文件URL确定文件路径
                    String filePath = null;
                    if (fileUrl != null && fileUrl.startsWith("/uploads/")) {
                        filePath = rootUploadPath + fileUrl.substring("/uploads".length());
                    } else {
                        // 尝试根据类型推断路径
                        String subPath = determineSubPath(message.getType());
                        if (subPath != null) {
                            filePath = rootUploadPath + "/" + subPath + "/" + fileName;
                        }
                    }

                    if (filePath != null && Files.exists(Paths.get(filePath))) {
                        validFiles.add(fileName);
                    } else {
                        missingFiles.add(fileName + " (URL: " + fileUrl + ")");
                    }
                }
            }

            result.put("success", true);
            result.put("totalChecked", fileMessages.size());
            result.put("validFiles", validFiles.size());
            result.put("missingFiles", missingFiles.size());
            result.put("missingFilesList", missingFiles);
            result.put("message", "验证完成，共检查 " + fileMessages.size() + " 个文件，" +
                     validFiles.size() + " 个有效，" + missingFiles.size() + " 个缺失");

            System.out.println("✅ 验证完成: " + validFiles.size() + " 个有效文件，" + missingFiles.size() + " 个缺失文件");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("❌ 验证失败: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "验证失败: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 根据消息类型确定子路径
     */
    private String determineSubPath(Message.MessageType type) {
        switch (type) {
            case IMAGE:
                return "chat/images";
            case VIDEO:
                return "chat/videos";
            case FILE:
                return "chat/files";
            default:
                return null;
        }
    }

    /**
     * 检查当前上传路径配置
     */
    @GetMapping("/check-upload-config")
    public ResponseEntity<Map<String, Object>> checkUploadConfig() {
        try {
            Map<String, Object> result = new HashMap<>();

            // 检查WebConfig中的实际路径
            String workingDir = System.getProperty("user.dir");
            String[] possibleUploadPaths = {
                "E:\\code11\\uploads",       // 绝对路径（根目录）
                workingDir + "/../uploads",  // 从untitled目录到上级uploads
                "../uploads",                // 上级目录uploads
                workingDir + "/uploads",     // 当前目录下的uploads
                "./uploads"                  // 相对路径uploads
            };

            String actualUploadPath = null;
            for (String path : possibleUploadPaths) {
                File testDir = new File(path);
                if (testDir.exists() && testDir.isDirectory()) {
                    actualUploadPath = testDir.getAbsolutePath();
                    break;
                }
            }

            result.put("workingDirectory", workingDir);
            result.put("actualUploadPath", actualUploadPath);
            result.put("possiblePaths", Arrays.asList(possibleUploadPaths));

            // 检查关键目录是否存在
            Map<String, Boolean> dirExists = new HashMap<>();
            if (actualUploadPath != null) {
                dirExists.put("chat/images", Files.exists(Paths.get(actualUploadPath, "chat", "images")));
                dirExists.put("chat/videos", Files.exists(Paths.get(actualUploadPath, "chat", "videos")));
                dirExists.put("chat/files", Files.exists(Paths.get(actualUploadPath, "chat", "files")));
                dirExists.put("videos", Files.exists(Paths.get(actualUploadPath, "videos")));
                dirExists.put("thumbnails", Files.exists(Paths.get(actualUploadPath, "thumbnails")));
            }
            result.put("directoryExists", dirExists);

            result.put("success", true);
            result.put("message", "上传路径配置检查完成");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "检查失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
}
