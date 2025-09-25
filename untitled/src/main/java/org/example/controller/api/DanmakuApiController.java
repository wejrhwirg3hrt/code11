package org.example.controller.api;

import org.example.dto.RealtimeCommentMessage;
import org.example.entity.User;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 弹幕API控制器
 */
@RestController
@RequestMapping("/api/danmaku")
@CrossOrigin(originPatterns = "*")
public class DanmakuApiController {

    @Autowired
    private UserService userService;



    /**
     * 发送弹幕
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendDanmaku(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (authentication == null) {
                response.put("success", false);
                response.put("message", "未登录");
                return ResponseEntity.status(401).body(response);
            }

            String username = authentication.getName();
            Optional<User> userOpt = userService.findByUsername(username);
            if (!userOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.status(401).body(response);
            }

            User user = userOpt.get();
            Long videoId = Long.valueOf(request.get("videoId").toString());
            String text = request.get("text").toString();
            Double time = Double.valueOf(request.get("time").toString());

            // 创建弹幕消息
            RealtimeCommentMessage danmakuMessage = new RealtimeCommentMessage();
            danmakuMessage.setContent(text);
            danmakuMessage.setVideoId(videoId);
            danmakuMessage.setUsername(user.getUsername());
            danmakuMessage.setUserAvatar(user.getAvatar());
            danmakuMessage.setTime(time);
            danmakuMessage.setCreatedAt(LocalDateTime.now());
            danmakuMessage.setType("DANMAKU");

            // WebSocket功能已删除，弹幕广播功能暂时禁用

            response.put("success", true);
            response.put("message", "弹幕发送成功");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "发送失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取视频弹幕
     */
    @GetMapping("/{videoId}")
    public ResponseEntity<List<Map<String, Object>>> getDanmaku(@PathVariable Long videoId) {
        try {
            // 这里可以从数据库获取历史弹幕
            // 暂时返回空列表，后续可以实现弹幕持久化
            List<Map<String, Object>> danmakuList = new ArrayList<>();
            
            return ResponseEntity.ok(danmakuList);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }
}
