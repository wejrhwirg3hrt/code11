package org.example.controller.api;

import org.example.entity.CallRecord;
import org.example.service.CallService;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 通话API控制器
 */
@RestController
@RequestMapping("/api/call")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*", "http://[::1]:*"})
public class CallApiController {
    
    @Autowired
    private CallService callService;
    
    @Autowired
    private UserService userService;
    
    /**
     * 获取用户的通话记录
     */
    @GetMapping("/records")
    public ResponseEntity<Map<String, Object>> getCallRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }
        
        try {
            var user = userService.findByUsername(userDetails.getUsername()).orElse(null);
            if (user == null) {
                return ResponseEntity.status(401).body(Map.of("error", "用户不存在"));
            }
            
            Page<CallRecord> recordsPage = callService.getUserCallRecords(user.getId(), page, size);
            
            List<Map<String, Object>> records = recordsPage.getContent().stream()
                .map(this::convertCallRecordToMap)
                .collect(Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("records", records);
            result.put("totalPages", recordsPage.getTotalPages());
            result.put("totalElements", recordsPage.getTotalElements());
            result.put("currentPage", page);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "获取通话记录失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取两个用户之间的通话记录
     */
    @GetMapping("/records/between")
    public ResponseEntity<Map<String, Object>> getCallRecordsBetweenUsers(
            @RequestParam Long otherUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }
        
        try {
            var user = userService.findByUsername(userDetails.getUsername()).orElse(null);
            if (user == null) {
                return ResponseEntity.status(401).body(Map.of("error", "用户不存在"));
            }
            
            Page<CallRecord> recordsPage = callService.getCallRecordsBetweenUsers(
                user.getId(), otherUserId, page, size);
            
            List<Map<String, Object>> records = recordsPage.getContent().stream()
                .map(this::convertCallRecordToMap)
                .collect(Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("records", records);
            result.put("totalPages", recordsPage.getTotalPages());
            result.put("totalElements", recordsPage.getTotalElements());
            result.put("currentPage", page);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "获取通话记录失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取用户的未接听通话
     */
    @GetMapping("/missed")
    public ResponseEntity<Map<String, Object>> getMissedCalls(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }
        
        try {
            var user = userService.findByUsername(userDetails.getUsername()).orElse(null);
            if (user == null) {
                return ResponseEntity.status(401).body(Map.of("error", "用户不存在"));
            }
            
            List<CallRecord> missedCalls = callService.getUserMissedCalls(user.getId());
            
            List<Map<String, Object>> records = missedCalls.stream()
                .map(this::convertCallRecordToMap)
                .collect(Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("missedCalls", records);
            result.put("count", records.size());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "获取未接听通话失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取通话统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getCallStatistics(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }
        
        try {
            var user = userService.findByUsername(userDetails.getUsername()).orElse(null);
            if (user == null) {
                return ResponseEntity.status(401).body(Map.of("error", "用户不存在"));
            }
            
            CallService.CallStatistics stats = callService.getCallStatistics(user.getId());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("totalCalls", stats.getTotalCalls());
            result.put("totalDurationSeconds", stats.getTotalDurationSeconds());
            result.put("formattedDuration", stats.getFormattedDuration());
            result.put("missedCalls", stats.getMissedCalls());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "获取通话统计失败: " + e.getMessage()));
        }
    }
    
    /**
     * 根据房间ID获取通话记录
     */
    @GetMapping("/records/room/{roomId}")
    public ResponseEntity<Map<String, Object>> getCallRecordByRoomId(
            @PathVariable String roomId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }
        
        try {
            var callRecord = callService.getCallRecordByRoomId(roomId);
            
            if (callRecord.isPresent()) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("record", convertCallRecordToMap(callRecord.get()));
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(404).body(Map.of("error", "通话记录不存在"));
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "获取通话记录失败: " + e.getMessage()));
        }
    }
    
    /**
     * 将CallRecord实体转换为Map
     */
    private Map<String, Object> convertCallRecordToMap(CallRecord record) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", record.getId());
        map.put("callerId", record.getCaller().getId());
        map.put("callerName", record.getCaller().getUsername());
        map.put("callerNickname", record.getCaller().getNickname());
        map.put("callerAvatar", record.getCaller().getAvatar());
        map.put("calleeId", record.getCallee().getId());
        map.put("calleeName", record.getCallee().getUsername());
        map.put("calleeNickname", record.getCallee().getNickname());
        map.put("calleeAvatar", record.getCallee().getAvatar());
        map.put("callType", record.getCallType().name().toLowerCase());
        map.put("callStatus", record.getCallStatus().name().toLowerCase());
        map.put("roomId", record.getRoomId());
        map.put("startTime", record.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        if (record.getEndTime() != null) {
            map.put("endTime", record.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        
        map.put("duration", record.getDuration());
        map.put("endedBy", record.getEndedBy());
        map.put("createdAt", record.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        // 格式化通话时长
        if (record.getDuration() != null && record.getDuration() > 0) {
            map.put("formattedDuration", formatDuration(record.getDuration()));
        }
        
        return map;
    }
    
    /**
     * 格式化通话时长
     */
    private String formatDuration(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format("%d:%02d", minutes, secs);
        }
    }
}
