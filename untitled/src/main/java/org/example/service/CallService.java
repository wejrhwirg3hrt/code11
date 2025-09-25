package org.example.service;

import org.example.entity.CallRecord;
import org.example.entity.User;
import org.example.repository.CallRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * 通话服务类
 */
@Service
@Transactional(readOnly = false)
public class CallService {
    
    @Autowired
    private CallRecordRepository callRecordRepository;
    
    @Autowired
    private UserService userService;
    
    /**
     * 创建通话记录
     */
    public CallRecord createCallRecord(Long callerId, Long calleeId, CallRecord.CallType callType, String roomId) {
        User caller = userService.findById(callerId)
            .orElseThrow(() -> new RuntimeException("主叫用户不存在"));
        User callee = userService.findById(calleeId)
            .orElseThrow(() -> new RuntimeException("被叫用户不存在"));
        
        CallRecord callRecord = new CallRecord(caller, callee, callType, roomId);
        return callRecordRepository.save(callRecord);
    }
    
    /**
     * 接受通话
     */
    public CallRecord acceptCall(String roomId) {
        CallRecord callRecord = callRecordRepository.findByRoomId(roomId)
            .orElseThrow(() -> new RuntimeException("通话记录不存在"));
        
        callRecord.setCallStatus(CallRecord.CallStatus.ACCEPTED);
        return callRecordRepository.save(callRecord);
    }
    
    /**
     * 拒绝通话
     */
    public CallRecord rejectCall(String roomId) {
        CallRecord callRecord = callRecordRepository.findByRoomId(roomId)
            .orElseThrow(() -> new RuntimeException("通话记录不存在"));
        
        callRecord.setCallStatus(CallRecord.CallStatus.REJECTED);
        callRecord.setEndTime(LocalDateTime.now());
        return callRecordRepository.save(callRecord);
    }
    
    /**
     * 结束通话
     */
    public CallRecord endCall(String roomId, Long endedByUserId) {
        CallRecord callRecord = callRecordRepository.findByRoomId(roomId)
            .orElseThrow(() -> new RuntimeException("通话记录不存在"));
        
        LocalDateTime endTime = LocalDateTime.now();
        callRecord.setCallStatus(CallRecord.CallStatus.ENDED);
        callRecord.setEndTime(endTime);
        callRecord.setEndedBy(endedByUserId);
        
        // 计算通话时长（只有在接受通话后才计算）
        if (callRecord.getCallStatus() == CallRecord.CallStatus.ACCEPTED) {
            long duration = ChronoUnit.SECONDS.between(callRecord.getStartTime(), endTime);
            callRecord.setDuration((int) duration);
        }
        
        return callRecordRepository.save(callRecord);
    }
    
    /**
     * 标记为未接听
     */
    public CallRecord markAsMissed(String roomId) {
        CallRecord callRecord = callRecordRepository.findByRoomId(roomId)
            .orElseThrow(() -> new RuntimeException("通话记录不存在"));
        
        callRecord.setCallStatus(CallRecord.CallStatus.MISSED);
        callRecord.setEndTime(LocalDateTime.now());
        return callRecordRepository.save(callRecord);
    }
    
    /**
     * 获取用户的通话记录
     */
    public Page<CallRecord> getUserCallRecords(Long userId, int page, int size) {
        User user = userService.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        Pageable pageable = PageRequest.of(page, size);
        return callRecordRepository.findByUser(user, pageable);
    }
    
    /**
     * 获取两个用户之间的通话记录
     */
    public Page<CallRecord> getCallRecordsBetweenUsers(Long userId1, Long userId2, int page, int size) {
        User user1 = userService.findById(userId1)
            .orElseThrow(() -> new RuntimeException("用户1不存在"));
        User user2 = userService.findById(userId2)
            .orElseThrow(() -> new RuntimeException("用户2不存在"));
        
        Pageable pageable = PageRequest.of(page, size);
        return callRecordRepository.findByTwoUsers(user1, user2, pageable);
    }
    
    /**
     * 获取用户的未接听通话
     */
    public List<CallRecord> getUserMissedCalls(Long userId) {
        User user = userService.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        return callRecordRepository.findMissedCallsByUser(user);
    }
    
    /**
     * 获取通话统计信息
     */
    public CallStatistics getCallStatistics(Long userId) {
        User user = userService.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        long totalCalls = callRecordRepository.countCompletedCallsByUser(user);
        long totalDuration = callRecordRepository.sumCallDurationByUser(user);
        List<CallRecord> missedCalls = callRecordRepository.findMissedCallsByUser(user);
        
        return new CallStatistics(totalCalls, totalDuration, missedCalls.size());
    }
    
    /**
     * 根据房间ID获取通话记录
     */
    public Optional<CallRecord> getCallRecordByRoomId(String roomId) {
        return callRecordRepository.findByRoomId(roomId);
    }
    
    /**
     * 清理过期的通话记录
     */
    @Transactional
    public void cleanupOldCallRecords(int daysToKeep) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(daysToKeep);
        callRecordRepository.deleteOldRecords(cutoffTime);
    }
    
    /**
     * 通话统计信息类
     */
    public static class CallStatistics {
        private long totalCalls;
        private long totalDurationSeconds;
        private int missedCalls;
        
        public CallStatistics(long totalCalls, long totalDurationSeconds, int missedCalls) {
            this.totalCalls = totalCalls;
            this.totalDurationSeconds = totalDurationSeconds;
            this.missedCalls = missedCalls;
        }
        
        // Getters
        public long getTotalCalls() { return totalCalls; }
        public long getTotalDurationSeconds() { return totalDurationSeconds; }
        public int getMissedCalls() { return missedCalls; }
        
        // 格式化通话时长
        public String getFormattedDuration() {
            long hours = totalDurationSeconds / 3600;
            long minutes = (totalDurationSeconds % 3600) / 60;
            long seconds = totalDurationSeconds % 60;
            
            if (hours > 0) {
                return String.format("%d小时%d分钟%d秒", hours, minutes, seconds);
            } else if (minutes > 0) {
                return String.format("%d分钟%d秒", minutes, seconds);
            } else {
                return String.format("%d秒", seconds);
            }
        }
    }
}
