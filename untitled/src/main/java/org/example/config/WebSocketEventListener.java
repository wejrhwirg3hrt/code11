package org.example.config;

import org.example.controller.WebRTCSignalingController;
import org.example.service.UserOnlineStatusService;
import org.example.service.UserService;
import org.example.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket事件监听器
 * 处理WebSocket连接和断开事件
 */
@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @Autowired
    private WebRTCSignalingController webRTCSignalingController;

    @Autowired
    private UserOnlineStatusService userOnlineStatusService;

    @Autowired
    private UserService userService;

    // 存储会话ID到用户ID的映射
    private final Map<String, Long> sessionUserMap = new ConcurrentHashMap<>();

    /**
     * 处理WebSocket连接事件
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        logger.info("=== WebSocket连接建立 ===");
        logger.info("会话ID: {}", sessionId);

        // 尝试从会话中获取用户信息
        try {
            // 从会话属性中获取用户信息
            String username = null;
            if (headerAccessor.getSessionAttributes() != null) {
                username = (String) headerAccessor.getSessionAttributes().get("username");
            }

            if (username == null) {
                // 尝试从Principal获取
                Authentication auth = (Authentication) headerAccessor.getUser();
                if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                    username = auth.getName();
                }
            }

            if (username != null) {
                User user = userService.findByUsername(username).orElse(null);
                if (user != null) {
                    Long userId = user.getId();
                    sessionUserMap.put(sessionId, userId);
                    userOnlineStatusService.userOnline(userId, sessionId);
                    userOnlineStatusService.broadcastOnlineCount();

                    logger.info("用户上线: {} (ID: {})", user.getUsername(), userId);
                } else {
                    logger.warn("找不到用户: {}", username);
                }
            } else {
                logger.warn("WebSocket连接未包含用户信息");
            }
        } catch (Exception e) {
            logger.warn("处理用户上线时出错: {}", e.getMessage());
        }
    }

    /**
     * 处理WebSocket断开事件
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        logger.info("=== WebSocket连接断开 ===");
        logger.info("会话ID: {}", sessionId);

        // 处理用户离线
        webRTCSignalingController.handleUserDisconnect(sessionId);

        // 处理用户在线状态
        try {
            Long userId = sessionUserMap.remove(sessionId);
            if (userId != null) {
                userOnlineStatusService.userOffline(userId);
                userOnlineStatusService.broadcastOnlineCount();

                logger.info("用户下线: ID {}", userId);
            }
        } catch (Exception e) {
            logger.warn("处理用户下线时出错: {}", e.getMessage());
        }
    }
}
