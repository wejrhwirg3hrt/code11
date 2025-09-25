package org.example.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "voice_clone")
public class VoiceClone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String voiceName;

    @Column(nullable = false)
    private String sourceVideoPath;

    @Column(nullable = false)
    private String extractedAudioPath;

    @Column(columnDefinition = "TEXT")
    private String transcription; // 转录文本

    @Column(nullable = false)
    private String voiceModelPath; // 语音模型文件路径

    @Column
    private String description;

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    @Column(name = "is_public")
    private Boolean isPublic = false;

    @Column
    private Integer duration; // 原始音频时长（秒）

    // 构造函数
    public VoiceClone() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getVoiceName() {
        return voiceName;
    }

    public void setVoiceName(String voiceName) {
        this.voiceName = voiceName;
    }

    public String getSourceVideoPath() {
        return sourceVideoPath;
    }

    public void setSourceVideoPath(String sourceVideoPath) {
        this.sourceVideoPath = sourceVideoPath;
    }

    public String getExtractedAudioPath() {
        return extractedAudioPath;
    }

    public void setExtractedAudioPath(String extractedAudioPath) {
        this.extractedAudioPath = extractedAudioPath;
    }

    public String getTranscription() {
        return transcription;
    }

    public void setTranscription(String transcription) {
        this.transcription = transcription;
    }

    public String getVoiceModelPath() {
        return voiceModelPath;
    }

    public void setVoiceModelPath(String voiceModelPath) {
        this.voiceModelPath = voiceModelPath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }
}
