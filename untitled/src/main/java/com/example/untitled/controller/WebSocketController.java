package com.example.untitled.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // 模拟存储会话和消息
    private static final Map<String, List<Map<String, Object>>> conversations = new HashMap<>();
    private static final List<Map<String, Object>> conversationList = new ArrayList<>();
    
    static {
        // 初始化一个默认会话
        String defaultConversationId = "conv-" + System.currentTimeMillis();
        Map<String, Object> defaultConversation = new HashMap<>();
        defaultConversation.put("id", defaultConversationId);
        defaultConversation.put("title", "新会话");
        defaultConversation.put("lastMessage", "开始聊天吧！");
        defaultConversation.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        conversationList.add(defaultConversation);
        conversations.put(defaultConversationId, new ArrayList<>());
    }

    @MessageMapping("/chat/send")
    public void sendMessage(Map<String, Object> messageData) {
        try {
            String conversationId = (String) messageData.get("conversationId");
            String content = (String) messageData.get("content");
            String messageType = (String) messageData.get("messageType");

            // 创建消息对象
            Map<String, Object> message = new HashMap<>();
            message.put("id", "msg-" + System.currentTimeMillis());
            message.put("conversationId", conversationId);
            message.put("senderId", "testuser");
            message.put("senderName", "testuser");
            message.put("messageType", messageType);
            message.put("content", content);
            message.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            // 保存消息
            conversations.computeIfAbsent(conversationId, k -> new ArrayList<>()).add(message);

            // 更新会话的最后消息
            for (Map<String, Object> conv : conversationList) {
                if (conversationId.equals(conv.get("id"))) {
                    conv.put("lastMessage", content);
                    conv.put("timestamp", message.get("timestamp"));
                    break;
                }
            }

            // 发送消息到指定会话
            messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, message);
            
            // 模拟自动回复
            simulateAutoReply(conversationId, content);
            
        } catch (Exception e) {
            System.err.println("处理消息时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void simulateAutoReply(String conversationId, String userMessage) {
        // 延迟1秒后发送自动回复
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                
                String replyContent = generateAutoReply(userMessage);
                
                Map<String, Object> replyMessage = new HashMap<>();
                replyMessage.put("id", "msg-" + System.currentTimeMillis());
                replyMessage.put("conversationId", conversationId);
                replyMessage.put("senderId", "assistant");
                replyMessage.put("senderName", "AI助手");
                replyMessage.put("messageType", "text");
                replyMessage.put("content", replyContent);
                replyMessage.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

                // 保存回复消息
                conversations.computeIfAbsent(conversationId, k -> new ArrayList<>()).add(replyMessage);

                // 更新会话的最后消息
                for (Map<String, Object> conv : conversationList) {
                    if (conversationId.equals(conv.get("id"))) {
                        conv.put("lastMessage", replyContent);
                        conv.put("timestamp", replyMessage.get("timestamp"));
                        break;
                    }
                }

                // 发送回复消息
                messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, replyMessage);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private String generateAutoReply(String userMessage) {
        String[] replies = {
            "我收到了您的消息：" + userMessage,
            "感谢您的消息！我正在处理中...",
            "这是一个很有趣的话题！",
            "我明白您的意思，让我想想...",
            "您说得对，这确实值得讨论。",
            "这个问题很好，我来为您解答。"
        };
        
        return replies[new Random().nextInt(replies.length)];
    }
}
