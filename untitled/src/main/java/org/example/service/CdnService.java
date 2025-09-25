package org.example.service;

import org.example.entity.CdnConfig;
import org.example.repository.CdnConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional(readOnly = false)
public class CdnService {

    @Autowired
    private CdnConfigRepository cdnConfigRepository;

    public List<CdnConfig> getAllCdnConfigs() {
        return cdnConfigRepository.findAll();
    }

    public CdnConfig createCdnConfig(CdnConfig config) {
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        return cdnConfigRepository.save(config);
    }

    public void toggleCdnStatus(Long id) {
        Optional<CdnConfig> configOpt = cdnConfigRepository.findById(id);
        if (configOpt.isPresent()) {
            CdnConfig config = configOpt.get();
            if (config.getStatus() == CdnConfig.CdnStatus.ACTIVE) {
                config.setStatus(CdnConfig.CdnStatus.INACTIVE);
            } else {
                config.setStatus(CdnConfig.CdnStatus.ACTIVE);
            }
            config.setUpdatedAt(LocalDateTime.now());
            cdnConfigRepository.save(config);
        }
    }

    public void clearCache(Long configId, String path) {
        // CDN缓存清理接口预留
        // 这里可以集成阿里云、腾讯云、七牛云等CDN服务商的API
        Optional<CdnConfig> configOpt = cdnConfigRepository.findById(configId);
        if (configOpt.isPresent()) {
            CdnConfig config = configOpt.get();

            String provider = config.getProvider() != null ? config.getProvider() : "default";
            switch (provider.toLowerCase()) {
                case "阿里云":
                case "aliyun":
                    clearAliyunCache(config, path);
                    break;
                case "腾讯云":
                case "tencent":
                    clearTencentCache(config, path);
                    break;
                case "七牛云":
                case "qiniu":
                    clearQiniuCache(config, path);
                    break;
                default:
                    System.out.println("清理CDN缓存: " + config.getName() + ", 路径: " + path);
            }
        }
    }

    public Map<String, Object> getCdnStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 统计CDN配置数量
        long totalConfigs = cdnConfigRepository.count();
        long activeConfigs = cdnConfigRepository.countByStatus(CdnConfig.CdnStatus.ACTIVE);

        stats.put("totalNodes", totalConfigs);
        stats.put("activeNodes", activeConfigs);
        stats.put("inactiveNodes", totalConfigs - activeConfigs);

        // 这里可以添加更多统计信息，如流量使用情况、缓存命中率等
        stats.put("totalBandwidth", "1.2 TB"); // 示例数据
        stats.put("hitRate", "95.6%"); // 示例数据

        return stats;
    }

    // CDN服务商接口预留方法
    private void clearAliyunCache(CdnConfig config, String path) {
        // 阿里云CDN缓存清理接口
        // 使用阿里云SDK调用RefreshObjectCaches接口
        String domain = config.getDomain() != null ? config.getDomain() : config.getDomainName();
        System.out.println("清理阿里云CDN缓存: " + domain + path);
    }

    private void clearTencentCache(CdnConfig config, String path) {
        // 腾讯云CDN缓存清理接口
        // 使用腾讯云SDK调用PurgeUrlsCache接口
        String domain = config.getDomain() != null ? config.getDomain() : config.getDomainName();
        System.out.println("清理腾讯云CDN缓存: " + domain + path);
    }

    private void clearQiniuCache(CdnConfig config, String path) {
        // 七牛云CDN缓存清理接口
        // 使用七牛云SDK调用缓存刷新接口
        String domain = config.getDomain() != null ? config.getDomain() : config.getDomainName();
        System.out.println("清理七牛云CDN缓存: " + domain + path);
    }

    // 获取CDN加速URL
    public String getCdnUrl(String originalUrl) {
        List<CdnConfig> activeConfigs = cdnConfigRepository.findByStatus(CdnConfig.CdnStatus.ACTIVE);
        if (!activeConfigs.isEmpty()) {
            CdnConfig config = activeConfigs.get(0); // 使用第一个激活的CDN配置
            String domain = config.getDomain() != null ? config.getDomain() : config.getDomainName();
            return "https://" + domain + originalUrl;
        }
        return originalUrl; // 如果没有CDN配置，返回原始URL
    }

    // 预热CDN缓存
    public void preloadCache(Long configId, List<String> urls) {
        Optional<CdnConfig> configOpt = cdnConfigRepository.findById(configId);
        if (configOpt.isPresent()) {
            CdnConfig config = configOpt.get();
            // 这里可以调用CDN服务商的预热接口
            String domain = config.getDomain() != null ? config.getDomain() : config.getDomainName();
            System.out.println("预热CDN缓存: " + domain + ", URLs: " + urls);
        }
    }
}