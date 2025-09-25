package org.example.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.service.WebSocketAutoManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@Component
public class WebSocketHandler extends TextWebSocketHandler {
    
    private static final Logger logger = Logger.getLogger(WebSocketHandler.class.getName());
    
    @Autowired
    private WebSocketAutoManager autoManager;
    
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        
        // 注册到自动管理器
        autoManager.registerConnection(sessionId, session);
        
        logger.info("WebSocket连接建立: " + sessionId);
        
        // 发送连接确认消息
        Map<String, Object> response = Map.of(
            "type", "connection_established",
            "sessionId", sessionId,
            "message", "连接成功建立"
        );
        
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String sessionId = session.getId();
        String payload = message.getPayload();
        
        logger.info("收到消息 from " + sessionId + ": " + payload);
        
        try {
            // 解析消息
            Map<String, Object> messageData = objectMapper.readValue(payload, Map.class);
            String type = (String) messageData.get("type");
            
            // 处理不同类型的消息
            switch (type) {
                case "ping":
                    handlePing(session, messageData);
                    break;
                case "chat":
                    handleChat(session, messageData);
                    break;
                case "status":
                    handleStatus(session, messageData);
                    break;
                default:
                    handleUnknownMessage(session, messageData);
            }
            
        } catch (Exception e) {
            logger.warning("处理消息时发生错误: " + e.getMessage());
            
            // 发送错误响应
            Map<String, Object> errorResponse = Map.of(
                "type", "error",
                "message", "消息处理失败: " + e.getMessage()
            );
            
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorResponse)));
        }
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        
        // 从自动管理器注销
        autoManager.unregisterConnection(sessionId);
        
        logger.info("WebSocket连接关闭: " + sessionId + " - 状态: " + status);
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String sessionId = session.getId();
        logger.severe("WebSocket传输错误: " + sessionId + " - " + exception.getMessage());
        
        // 清理连接
        sessions.remove(sessionId);
        autoManager.unregisterConnection(sessionId);
    }
    
    private void handlePing(WebSocketSession session, Map<String, Object> messageData) throws IOException {
        Map<String, Object> response = Map.of(
            "type", "pong",
            "timestamp", System.currentTimeMillis(),
            "message", "pong"
        );
        
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }
    
    private void handleChat(WebSocketSession session, Map<String, Object> messageData) throws IOException {
        String content = (String) messageData.get("content");
        String sender = (String) messageData.get("sender");
        
        // 广播消息给所有连接的客户端
        Map<String, Object> broadcastMessage = Map.of(
            "type", "chat",
            "content", content,
            "sender", sender,
            "timestamp", System.currentTimeMillis()
        );
        
        String messageJson = objectMapper.writeValueAsString(broadcastMessage);
        
        for (WebSocketSession clientSession : sessions.values()) {
            if (clientSession.isOpen()) {
                try {
                    clientSession.sendMessage(new TextMessage(messageJson));
                } catch (IOException e) {
                    logger.warning("发送消息失败: " + e.getMessage());
                }
            }
        }
    }
    
    private void handleStatus(WebSocketSession session, Map<String, Object> messageData) throws IOException {
        // 获取连接状态信息
        Map<String, Object> stats = autoManager.getConnectionStats();
        
        Map<String, Object> response = Map.of(
            "type", "status_response",
            "stats", stats,
            "sessionId", session.getId()
        );
        
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }
    
    private void handleUnknownMessage(WebSocketSession session, Map<String, Object> messageData) throws IOException {
        Map<String, Object> response = Map.of(
            "type", "error",
            "message", "未知的消息类型: " + messageData.get("type")
        );
        
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }
    
    /**
     * 获取当前连接数
     */
    public int getCurrentConnectionCount() {
        return sessions.size();
    }
    
    /**
     * 获取活跃连接数
     */
    public int getActiveConnectionCount() {
        return (int) sessions.values().stream()
                .filter(WebSocketSession::isOpen)
                .count();
    }
    
    /**
     * 广播消息给所有客户端
     */
    public void broadcastMessage(String message) {
        for (WebSocketSession session : sessions.values()) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    logger.warning("广播消息失败: " + e.getMessage());
                }
            }
        }
    }
} 