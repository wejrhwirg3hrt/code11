package org.example.controller;

import org.example.entity.CallRecord;
import org.example.entity.User;
import org.example.service.CallService;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebRTC信令服务器控制器
 * 处理WebRTC连接建立过程中的信令交换
 */
@Controller
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*", "http://[::1]:*"})
public class WebRTCSignalingController {

    private static final Logger logger = LoggerFactory.getLogger(WebRTCSignalingController.class);
    
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    private CallService callService;

    @Autowired
    private UserService userService;

    // 存储在线用户的会话信息 (userId -> sessionId)
    private final Map<String, String> userSessions = new ConcurrentHashMap<>();
    // 存储会话到用户的映射 (sessionId -> userId)
    private final Map<String, String> sessionUsers = new ConcurrentHashMap<>();
    // 存储通话房间信息
    private final Map<String, CallRoom> callRooms = new ConcurrentHashMap<>();

    public WebRTCSignalingController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * 在线状态测试页面
     */
    @GetMapping("/test-online-status")
    public String testOnlineStatusPage() {
        return "test-online-status";
    }

    /**
     * WebRTC调试页面
     */
    @GetMapping("/webrtc-debug")
    public String webrtcDebugPage() {
        return "webrtc-debug";
    }

    /**
     * WebRTC连接诊断页面
     */
    @GetMapping("/webrtc-diagnosis")
    public String webrtcDiagnosisPage() {
        return "webrtc-diagnosis";
    }

    /**
     * 视频通话测试页面
     */
    @GetMapping("/test-video-call")
    public String testVideoCallPage() {
        return "test-video-call";
    }

    /**
     * WebRTC简单测试页面
     */
    @GetMapping("/webrtc-simple-test")
    public String webrtcSimpleTestPage() {
        return "webrtc-simple-test";
    }

    /**
     * 获取当前用户信息（简化版，用于WebRTC测试）
     */
    @GetMapping("/api/current-user")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCurrentUserSimple(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        if (authentication == null || !authentication.isAuthenticated()) {
            response.put("error", "用户未登录");
            return ResponseEntity.status(401).body(response);
        }

        try {
            User user = userService.findByUsername(authentication.getName()).orElse(null);
            if (user == null) {
                response.put("error", "用户不存在");
                return ResponseEntity.status(404).body(response);
            }

            // 直接返回用户信息，不包装在额外的结构中
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("avatar", user.getAvatar());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "获取用户信息失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }





    /**
     * 用户离线处理
     */
    public void handleUserDisconnect(String sessionId) {
        String userId = sessionUsers.get(sessionId);
        if (userId != null) {
            userSessions.remove(userId);
            sessionUsers.remove(sessionId);

            logger.info("=== 用户离线 ===");
            logger.info("用户ID: {}", userId);
            logger.info("会话ID: {}", sessionId);
            logger.info("当前在线用户数: {}", userSessions.size());

            // 广播在线用户数量更新
            broadcastOnlineCount();
        }
    }

    /**
     * 广播在线用户数量
     */
    private void broadcastOnlineCount() {
        int onlineCount = userSessions.size();
        Map<String, Object> message = Map.of(
            "type", "online-count-update",
            "onlineCount", onlineCount,
            "timestamp", System.currentTimeMillis()
        );

        messagingTemplate.convertAndSend("/topic/online-status", message);
        logger.debug("广播在线用户数量: {}", onlineCount);
    }
    
    /**
     * 用户加入信令服务器
     */
    @MessageMapping("/webrtc/join")
    public void joinSignaling(Map<String, Object> message, SimpMessageHeaderAccessor headerAccessor) {
        try {
            logger.info("🔥🔥🔥 收到加入请求，原始消息: {}", message);
            System.out.println("🔥🔥🔥 收到加入请求，原始消息: " + message);

            String userId = (String) message.get("userId");
            String sessionId = headerAccessor.getSessionId();

            if (userId == null || userId.trim().isEmpty()) {
                logger.error("❌ 用户ID为空，无法处理加入请求");
                System.out.println("❌ 用户ID为空，无法处理加入请求");
                return;
            }

            if (sessionId == null || sessionId.trim().isEmpty()) {
                logger.error("❌ 会话ID为空，无法处理加入请求");
                System.out.println("❌ 会话ID为空，无法处理加入请求");
                return;
            }

            logger.info("🔥🔥🔥 解析结果 - 用户ID: {}, 会话ID: {}", userId, sessionId);
            System.out.println("🔥🔥🔥 解析结果 - 用户ID: " + userId + ", 会话ID: " + sessionId);

            // 双向映射
            userSessions.put(userId, sessionId);
            sessionUsers.put(sessionId, userId);

            logger.info("=== 用户加入WebRTC信令服务器 ===");
            logger.info("用户ID: {}", userId);
            logger.info("会话ID: {}", sessionId);
            logger.info("当前在线用户数: {}", userSessions.size());
            logger.info("在线用户列表: {}", userSessions.keySet());

            System.out.println("=== 用户加入WebRTC信令服务器 ===");
            System.out.println("用户ID: " + userId);
            System.out.println("会话ID: " + sessionId);
            System.out.println("当前在线用户数: " + userSessions.size());
            System.out.println("在线用户列表: " + userSessions.keySet());

            // 广播在线用户数量更新
            broadcastOnlineCount();

            // 立即发送加入成功通知
            try {
                Map<String, Object> joinResponse = new HashMap<>();
                joinResponse.put("status", "success");
                joinResponse.put("message", "已连接到信令服务器");
                joinResponse.put("userId", userId);
                joinResponse.put("onlineCount", userSessions.size());
                joinResponse.put("timestamp", System.currentTimeMillis());

                messagingTemplate.convertAndSendToUser(userId, "/queue/webrtc/joined", joinResponse);
                logger.info("✅ 已向用户 {} 发送加入成功通知", userId);
                System.out.println("✅ 已向用户 " + userId + " 发送加入成功通知");
            } catch (Exception e) {
                logger.error("❌ 发送加入成功通知失败: {}", e.getMessage(), e);
                System.out.println("❌ 发送加入成功通知失败: " + e.getMessage());
                e.printStackTrace();
            }

        } catch (Exception e) {
            logger.error("❌ 处理加入请求失败: {}", e.getMessage(), e);
            System.out.println("❌ 处理加入请求失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 处理保活消息
     */
    @MessageMapping("/webrtc/keepalive")
    public void keepAlive(Map<String, Object> message, SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) message.get("userId");
        String sessionId = headerAccessor.getSessionId();

        if (userId != null) {
            // 更新用户会话信息
            userSessions.put(userId, sessionId);
            sessionUsers.put(sessionId, userId);
            logger.debug("收到用户 {} 的保活消息，会话ID: {}", userId, sessionId);
        }
    }
    
    /**
     * 发起通话邀请
     */
    @MessageMapping("/webrtc/call")
    public void initiateCall(Map<String, Object> message) {
        logger.info("🔥🔥🔥 收到通话邀请请求，原始消息: {}", message);
        System.out.println("🔥🔥🔥 收到通话邀请请求，原始消息: " + message);

        String callerId = (String) message.get("callerId");
        String calleeId = (String) message.get("calleeId");
        String callType = (String) message.get("callType"); // "audio" 或 "video"
        String roomId = generateRoomId(callerId, calleeId);

        logger.info("=== 收到通话邀请请求 ===");
        logger.info("发起者ID: {}", callerId);
        logger.info("接收者ID: {}", calleeId);
        logger.info("通话类型: {}", callType);
        logger.info("房间ID: {}", roomId);
        logger.info("原始消息: {}", message);

        System.out.println("=== 收到通话邀请请求 ===");
        System.out.println("发起者ID: " + callerId);
        System.out.println("接收者ID: " + calleeId);
        System.out.println("通话类型: " + callType);
        System.out.println("房间ID: " + roomId);
        System.out.println("原始消息: " + message);

        try {
            // 获取调用者用户信息
            User caller = userService.getUserById(Long.parseLong(callerId));

            // 创建通话记录
            CallRecord.CallType type = "video".equals(callType) ? CallRecord.CallType.VIDEO : CallRecord.CallType.AUDIO;
            callService.createCallRecord(Long.parseLong(callerId), Long.parseLong(calleeId), type, roomId);

            // 创建通话房间
            CallRoom room = new CallRoom(roomId, callerId, calleeId, callType);
            callRooms.put(roomId, room);

            // 向被叫用户发送通话邀请（包含调用者信息）
            Map<String, Object> callInvitation = Map.of(
                "type", "call-invitation",
                "callerId", callerId,
                "calleeId", calleeId,
                "callType", callType,
                "roomId", roomId,
                "callerName", caller.getUsername(),
                "callerAvatar", caller.getAvatar() != null ? caller.getAvatar() : "/images/default-avatar.png"
            );

            // 发送通话邀请
            logger.info("=== 开始发送通话邀请 ===");
            logger.info("目标用户ID: {}", calleeId);
            logger.info("调用者姓名: {}", caller.getUsername());
            logger.info("消息内容: {}", callInvitation);
            logger.info("目标用户会话ID: {}", userSessions.get(calleeId));

            // 检查目标用户是否在线
            boolean isOnline = userSessions.containsKey(calleeId);
            logger.info("🔍 目标用户在线状态检查:");
            logger.info("  - 用户ID: {}", calleeId);
            logger.info("  - 是否在线: {}", isOnline);
            logger.info("  - 会话ID: {}", userSessions.get(calleeId));
            logger.info("  - 当前在线用户: {}", userSessions.keySet());

            System.out.println("🔍 目标用户在线状态检查:");
            System.out.println("  - 用户ID: " + calleeId);
            System.out.println("  - 是否在线: " + isOnline);
            System.out.println("  - 会话ID: " + userSessions.get(calleeId));
            System.out.println("  - 当前在线用户: " + userSessions.keySet());

            if (!isOnline) {
                logger.warn("⚠️ 目标用户 {} 不在线，但仍尝试发送消息", calleeId);
                System.out.println("⚠️ 目标用户 " + calleeId + " 不在线，但仍尝试发送消息");
            }

            // 发送到用户队列（主要方式）
            try {
                logger.info("📤 发送到用户队列: /user/{}/queue/webrtc/call-invitation", calleeId);
                System.out.println("📤 发送到用户队列: /user/" + calleeId + "/queue/webrtc/call-invitation");
                messagingTemplate.convertAndSendToUser(calleeId, "/queue/webrtc/call-invitation", callInvitation);
                logger.info("✅ 用户队列消息发送完成");
                System.out.println("✅ 用户队列消息发送完成");
            } catch (Exception e) {
                logger.error("❌ 用户队列消息发送失败: {}", e.getMessage(), e);
                System.out.println("❌ 用户队列消息发送失败: " + e.getMessage());
            }

            // 同时发送到广播频道作为备用
            try {
                String topicPath = "/topic/webrtc/call-invitation/" + calleeId;
                logger.info("📤 发送到广播频道: {}", topicPath);
                System.out.println("📤 发送到广播频道: " + topicPath);
                messagingTemplate.convertAndSend(topicPath, callInvitation);
                logger.info("✅ 广播频道消息发送完成");
                System.out.println("✅ 广播频道消息发送完成");
            } catch (Exception e) {
                logger.error("❌ 广播频道消息发送失败: {}", e.getMessage(), e);
                System.out.println("❌ 广播频道消息发送失败: " + e.getMessage());
            }

            // 额外发送到所有连接的会话（强制广播）
            try {
                logger.info("📤 强制广播到所有会话: /topic/webrtc/call-invitation-broadcast");
                System.out.println("📤 强制广播到所有会话: /topic/webrtc/call-invitation-broadcast");
                messagingTemplate.convertAndSend("/topic/webrtc/call-invitation-broadcast", callInvitation);
                logger.info("✅ 强制广播完成");
                System.out.println("✅ 强制广播完成");
            } catch (Exception e) {
                logger.error("❌ 强制广播失败: {}", e.getMessage(), e);
                System.out.println("❌ 强制广播失败: " + e.getMessage());
            }

            logger.info("=== 通话邀请发送完成 ===");
        } catch (Exception e) {
            logger.error("处理通话邀请失败: {}", e.getMessage());
            // 即使出现错误，也尝试发送基本的通话邀请
            try {
                CallRoom room = new CallRoom(roomId, callerId, calleeId, callType);
                callRooms.put(roomId, room);

                Map<String, Object> callInvitation = Map.of(
                    "type", "call-invitation",
                    "callerId", callerId,
                    "calleeId", calleeId,
                    "callType", callType,
                    "roomId", roomId,
                    "callerName", "用户" + callerId,
                    "callerAvatar", "/images/default-avatar.png"
                );

                messagingTemplate.convertAndSendToUser(calleeId, "/queue/webrtc/call-invitation", callInvitation);
                logger.info("发送了基本通话邀请给用户 {}", calleeId);
            } catch (Exception ex) {
                logger.error("发送基本通话邀请也失败: {}", ex.getMessage());
            }
        }
    }
    
    /**
     * 发起通话邀请 (别名方法)
     */
    @MessageMapping("/webrtc/invite")
    public void inviteCall(Map<String, Object> message) {
        logger.info("🔥🔥🔥 收到invite请求，原始消息: {}", message);
        System.out.println("🔥🔥🔥 收到invite请求，原始消息: " + message);

        // 直接调用initiateCall方法
        initiateCall(message);
    }

    /**
     * 接受通话
     */
    @MessageMapping("/webrtc/accept")
    public void acceptCall(Map<String, Object> message) {
        String roomId = (String) message.get("roomId");
        String calleeId = (String) message.get("calleeId");

        CallRoom room = callRooms.get(roomId);
        if (room != null) {
            room.setStatus("accepted");
            logger.info("用户 {} 接受通话，房间ID: {}", calleeId, roomId);

            try {
                // 更新通话记录状态
                callService.acceptCall(roomId);
            } catch (Exception e) {
                logger.error("更新通话记录失败: {}", e.getMessage());
            }

            // 通知主叫用户通话被接受
            Map<String, Object> response = Map.of(
                "type", "call-accepted",
                "roomId", roomId,
                "calleeId", calleeId
            );

            logger.info("🔔 发送通话接受消息给用户 {}: {}", room.getCallerId(), response);
            messagingTemplate.convertAndSendToUser(room.getCallerId(), "/queue/webrtc/call-accepted", response);

            // 额外发送到广播频道作为备用
            messagingTemplate.convertAndSend("/topic/webrtc/call-accepted/" + room.getCallerId(), response);
            logger.info("📡 备用广播消息已发送到: /topic/webrtc/call-accepted/{}", room.getCallerId());
        }
    }
    
    /**
     * 拒绝通话
     */
    @MessageMapping("/webrtc/reject")
    public void rejectCall(Map<String, Object> message) {
        String roomId = (String) message.get("roomId");
        String calleeId = (String) message.get("calleeId");

        CallRoom room = callRooms.get(roomId);
        if (room != null) {
            logger.info("用户 {} 拒绝通话，房间ID: {}", calleeId, roomId);

            try {
                // 更新通话记录状态
                callService.rejectCall(roomId);
            } catch (Exception e) {
                logger.error("更新通话记录失败: {}", e.getMessage());
            }

            // 通知主叫用户通话被拒绝
            Map<String, Object> response = Map.of(
                "type", "call-rejected",
                "roomId", roomId,
                "calleeId", calleeId
            );

            messagingTemplate.convertAndSendToUser(room.getCallerId(), "/queue/webrtc/call-rejected", response);

            // 清理房间
            callRooms.remove(roomId);
        }
    }
    
    /**
     * 挂断通话
     */
    @MessageMapping("/webrtc/hangup")
    public void hangupCall(Map<String, Object> message) {
        String roomId = (String) message.get("roomId");
        String userId = (String) message.get("userId");

        CallRoom room = callRooms.get(roomId);
        if (room != null) {
            logger.info("用户 {} 挂断通话，房间ID: {}", userId, roomId);

            try {
                // 更新通话记录状态
                callService.endCall(roomId, Long.parseLong(userId));
            } catch (Exception e) {
                logger.error("更新通话记录失败: {}", e.getMessage());
            }

            // 通知房间内的其他用户
            String otherUserId = userId.equals(room.getCallerId()) ? room.getCalleeId() : room.getCallerId();

            Map<String, Object> response = Map.of(
                "type", "call-ended",
                "roomId", roomId,
                "endedBy", userId
            );

            messagingTemplate.convertAndSendToUser(otherUserId, "/queue/webrtc/call-ended", response);

            // 清理房间
            callRooms.remove(roomId);
        }
    }
    
    /**
     * 转发WebRTC Offer
     */
    @MessageMapping("/webrtc/offer")
    public void forwardOffer(Map<String, Object> message) {
        String roomId = (String) message.get("roomId");
        String fromUserId = (String) message.get("fromUserId");
        String toUserId = (String) message.get("toUserId");
        Object offer = message.get("offer");
        
        logger.info("转发Offer: {} -> {}, 房间ID: {}", fromUserId, toUserId, roomId);
        
        Map<String, Object> forwardMessage = Map.of(
            "type", "offer",
            "roomId", roomId,
            "fromUserId", fromUserId,
            "offer", offer
        );
        
        messagingTemplate.convertAndSendToUser(toUserId, "/queue/webrtc/offer", forwardMessage);
    }
    
    /**
     * 转发WebRTC Answer
     */
    @MessageMapping("/webrtc/answer")
    public void forwardAnswer(Map<String, Object> message) {
        String roomId = (String) message.get("roomId");
        String fromUserId = (String) message.get("fromUserId");
        String toUserId = (String) message.get("toUserId");
        Object answer = message.get("answer");
        
        logger.info("转发Answer: {} -> {}, 房间ID: {}", fromUserId, toUserId, roomId);
        
        Map<String, Object> forwardMessage = Map.of(
            "type", "answer",
            "roomId", roomId,
            "fromUserId", fromUserId,
            "answer", answer
        );
        
        messagingTemplate.convertAndSendToUser(toUserId, "/queue/webrtc/answer", forwardMessage);
    }
    
    /**
     * 转发ICE候选
     */
    @MessageMapping("/webrtc/ice-candidate")
    public void forwardIceCandidate(Map<String, Object> message) {
        String roomId = (String) message.get("roomId");
        String fromUserId = (String) message.get("fromUserId");
        String toUserId = (String) message.get("toUserId");
        Object candidate = message.get("candidate");
        
        logger.debug("转发ICE候选: {} -> {}, 房间ID: {}", fromUserId, toUserId, roomId);
        
        Map<String, Object> forwardMessage = Map.of(
            "type", "ice-candidate",
            "roomId", roomId,
            "fromUserId", fromUserId,
            "candidate", candidate
        );
        
        messagingTemplate.convertAndSendToUser(toUserId, "/queue/webrtc/ice-candidate", forwardMessage);
    }
    
    /**
     * 生成房间ID
     */
    private String generateRoomId(String userId1, String userId2) {
        // 确保房间ID的唯一性和一致性
        String[] users = {userId1, userId2};
        java.util.Arrays.sort(users);
        return "room_" + users[0] + "_" + users[1] + "_" + System.currentTimeMillis();
    }
    
    /**
     * 通话房间类
     */
    public static class CallRoom {
        private String roomId;
        private String callerId;
        private String calleeId;
        private String callType;
        private String status; // "calling", "accepted", "ended"
        private long createdAt;
        
        public CallRoom(String roomId, String callerId, String calleeId, String callType) {
            this.roomId = roomId;
            this.callerId = callerId;
            this.calleeId = calleeId;
            this.callType = callType;
            this.status = "calling";
            this.createdAt = System.currentTimeMillis();
        }
        
        // Getters and Setters
        public String getRoomId() { return roomId; }
        public String getCallerId() { return callerId; }
        public String getCalleeId() { return calleeId; }
        public String getCallType() { return callType; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public long getCreatedAt() { return createdAt; }
    }
}
