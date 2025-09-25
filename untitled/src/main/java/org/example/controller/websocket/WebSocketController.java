package org.example.controller.websocket;

import org.example.service.UserOnlineStatusService;
import org.example.service.UserService;
import org.example.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

@Controller
public class WebSocketController {

    @Autowired
    private UserOnlineStatusService userOnlineStatusService;
    
    @Autowired
    private UserService userService;
    
    private ObjectMapper objectMapper = new ObjectMapper();

    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public String greeting(String message) throws Exception {
        Thread.sleep(1000); // 模拟处理时间
        return "Hello, " + message + "!";
    }

    /**
     * 处理用户身份标识消息
     */
    @MessageMapping("/user-identification")
    public void handleUserIdentification(String message, SimpMessageHeaderAccessor headerAccessor) {
        try {
            System.out.println("收到用户身份标识消息: " + message);
            
            // 解析消息
            Map<String, Object> messageData = objectMapper.readValue(message, Map.class);
            String type = (String) messageData.get("type");
            
            if ("user_identification".equals(type)) {
                String username = (String) messageData.get("username");
                Object userIdObj = messageData.get("userId");
                
                if (username != null && userIdObj != null) {
                    Long userId = null;
                    if (userIdObj instanceof Integer) {
                        userId = ((Integer) userIdObj).longValue();
                    } else if (userIdObj instanceof Long) {
                        userId = (Long) userIdObj;
                    }
                    
                    if (userId != null) {
                        // 获取会话ID
                        String sessionId = headerAccessor.getSessionId();
                        
                        // 设置用户上线状态
                        userOnlineStatusService.userOnline(userId, sessionId);
                        userOnlineStatusService.broadcastOnlineCount();
                        
                        System.out.println("通过WebSocket设置用户上线: " + username + " (ID: " + userId + ", 会话: " + sessionId + ")");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("处理用户身份标识失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
