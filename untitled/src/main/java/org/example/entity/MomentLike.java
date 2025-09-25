package org.example.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "moment_likes", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"moment_id", "user_id"}))
public class MomentLike {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "moment_id", nullable = false)
    private Long momentId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;
    
    public MomentLike() {
        this.createTime = LocalDateTime.now();
    }
    
    public MomentLike(Long momentId, Long userId) {
        this.momentId = momentId;
        this.userId = userId;
        this.createTime = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getMomentId() {
        return momentId;
    }
    
    public void setMomentId(Long momentId) {
        this.momentId = momentId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
