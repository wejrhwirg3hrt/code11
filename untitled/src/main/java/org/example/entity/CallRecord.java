package org.example.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 通话记录实体类
 */
@Entity
@Table(name = "call_records")
public class CallRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caller_id", nullable = false)
    private User caller; // 主叫用户
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "callee_id", nullable = false)
    private User callee; // 被叫用户
    
    @Enumerated(EnumType.STRING)
    @Column(name = "call_type", nullable = false)
    private CallType callType; // 通话类型：语音或视频
    
    @Enumerated(EnumType.STRING)
    @Column(name = "call_status", nullable = false)
    private CallStatus callStatus; // 通话状态
    
    @Column(name = "room_id", length = 100)
    private String roomId; // 房间ID
    
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime; // 通话开始时间
    
    @Column(name = "end_time")
    private LocalDateTime endTime; // 通话结束时间
    
    @Column(name = "duration")
    private Integer duration; // 通话时长（秒）
    
    @Column(name = "ended_by")
    private Long endedBy; // 挂断通话的用户ID
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    // 构造函数
    public CallRecord() {}
    
    public CallRecord(User caller, User callee, CallType callType, String roomId) {
        this.caller = caller;
        this.callee = callee;
        this.callType = callType;
        this.callStatus = CallStatus.CALLING;
        this.roomId = roomId;
        this.startTime = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getCaller() {
        return caller;
    }
    
    public void setCaller(User caller) {
        this.caller = caller;
    }
    
    public User getCallee() {
        return callee;
    }
    
    public void setCallee(User callee) {
        this.callee = callee;
    }
    
    public CallType getCallType() {
        return callType;
    }
    
    public void setCallType(CallType callType) {
        this.callType = callType;
    }
    
    public CallStatus getCallStatus() {
        return callStatus;
    }
    
    public void setCallStatus(CallStatus callStatus) {
        this.callStatus = callStatus;
    }
    
    public String getRoomId() {
        return roomId;
    }
    
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public Integer getDuration() {
        return duration;
    }
    
    public void setDuration(Integer duration) {
        this.duration = duration;
    }
    
    public Long getEndedBy() {
        return endedBy;
    }
    
    public void setEndedBy(Long endedBy) {
        this.endedBy = endedBy;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 通话类型枚举
     */
    public enum CallType {
        AUDIO, // 语音通话
        VIDEO  // 视频通话
    }
    
    /**
     * 通话状态枚举
     */
    public enum CallStatus {
        CALLING,    // 呼叫中
        ACCEPTED,   // 已接听
        REJECTED,   // 已拒绝
        MISSED,     // 未接听
        ENDED,      // 已结束
        FAILED      // 通话失败
    }
}
