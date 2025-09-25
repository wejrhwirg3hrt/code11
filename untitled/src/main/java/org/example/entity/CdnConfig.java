package org.example.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cdn_configs")
public class CdnConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_name", nullable = false)
    private String providerName;

    @Column(name = "domain_name", nullable = false)
    private String domainName;

    @Column(name = "access_key")
    private String accessKey;

    @Column(name = "secret_key")
    private String secretKey;

    @Column(name = "bucket_name")
    private String bucketName;

    @Column(name = "region")
    private String region;

    @Column(name = "name")
    private String name;

    @Column(name = "domain")
    private String domain;

    @Column(name = "bandwidth")
    private String bandwidth;

    @Column(name = "provider")
    private String provider;

    @Column(name = "origin_server")
    private String originServer;

    @Column(name = "cache_rules", columnDefinition = "TEXT")
    private String cacheRules;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "config", columnDefinition = "TEXT")
    private String config;

    @Enumerated(EnumType.STRING)
    private CdnStatus status = CdnStatus.INACTIVE;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum CdnStatus {
        ACTIVE("激活"),
        INACTIVE("未激活"),
        MAINTENANCE("维护中");

        private final String description;

        CdnStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 构造函数
    public CdnConfig() {}

    // Getter和Setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }

    public String getDomainName() { return domainName; }
    public void setDomainName(String domainName) { this.domainName = domainName; }

    public String getAccessKey() { return accessKey; }
    public void setAccessKey(String accessKey) { this.accessKey = accessKey; }

    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }

    public String getBucketName() { return bucketName; }
    public void setBucketName(String bucketName) { this.bucketName = bucketName; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public CdnStatus getStatus() { return status; }
    public void setStatus(CdnStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public String getBandwidth() { return bandwidth; }
    public void setBandwidth(String bandwidth) { this.bandwidth = bandwidth; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getOriginServer() { return originServer; }
    public void setOriginServer(String originServer) { this.originServer = originServer; }

    public String getCacheRules() { return cacheRules; }
    public void setCacheRules(String cacheRules) { this.cacheRules = cacheRules; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getConfig() { return config; }
    public void setConfig(String config) { this.config = config; }
}