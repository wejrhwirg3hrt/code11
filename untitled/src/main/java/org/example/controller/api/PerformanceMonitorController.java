package org.example.controller.api;

import org.example.config.PerformanceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 性能监控控制器
 * 提供实时性能数据和系统状态
 */
@RestController
@RequestMapping("/api/performance")
public class PerformanceMonitorController {

    @Autowired
    private PerformanceConfig.PerformanceMonitor performanceMonitor;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private Executor taskExecutor;

    /**
     * 获取系统性能状态
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getPerformanceStatus() {
        Map<String, Object> status = new HashMap<>();
        
        // 运行时间
        status.put("uptime", performanceMonitor.getUptime());
        status.put("uptimeFormatted", formatUptime(performanceMonitor.getUptime()));
        
        // 内存使用情况
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long totalMemory = memoryBean.getHeapMemoryUsage().getMax();
        long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
        long freeMemory = totalMemory - usedMemory;
        
        status.put("memory", Map.of(
            "total", totalMemory,
            "used", usedMemory,
            "free", freeMemory,
            "usagePercent", (double) usedMemory / totalMemory * 100
        ));
        
        // 线程信息
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        status.put("threads", Map.of(
            "total", threadBean.getThreadCount(),
            "peak", threadBean.getPeakThreadCount(),
            "daemon", threadBean.getDaemonThreadCount()
        ));
        
        // 系统信息
        Runtime runtime = Runtime.getRuntime();
        status.put("system", Map.of(
            "processors", runtime.availableProcessors(),
            "maxMemory", runtime.maxMemory(),
            "totalMemory", runtime.totalMemory(),
            "freeMemory", runtime.freeMemory()
        ));
        
        return ResponseEntity.ok(status);
    }

    /**
     * 获取缓存统计信息
     */
    @GetMapping("/cache/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        
        if (cacheManager instanceof org.springframework.cache.concurrent.ConcurrentMapCacheManager) {
            org.springframework.cache.concurrent.ConcurrentMapCacheManager concurrentCacheManager = 
                (org.springframework.cache.concurrent.ConcurrentMapCacheManager) cacheManager;
            
            // 获取缓存名称列表
            String[] cacheNames = concurrentCacheManager.getCacheNames().toArray(new String[0]);
            stats.put("cacheCount", cacheNames.length);
            stats.put("cacheNames", cacheNames);
            
            // 统计每个缓存的大小
            Map<String, Integer> cacheSizes = new HashMap<>();
            for (String cacheName : cacheNames) {
                org.springframework.cache.Cache cache = concurrentCacheManager.getCache(cacheName);
                if (cache != null && cache.getNativeCache() instanceof java.util.concurrent.ConcurrentHashMap) {
                    java.util.concurrent.ConcurrentHashMap<?, ?> map = 
                        (java.util.concurrent.ConcurrentHashMap<?, ?>) cache.getNativeCache();
                    cacheSizes.put(cacheName, map.size());
                }
            }
            stats.put("cacheSizes", cacheSizes);
        }
        
        return ResponseEntity.ok(stats);
    }

    /**
     * 清理所有缓存
     */
    @PostMapping("/cache/clear")
    public ResponseEntity<Map<String, Object>> clearAllCaches() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            cacheManager.getCacheNames().forEach(cacheName -> {
                org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                }
            });
            
            result.put("success", true);
            result.put("message", "所有缓存已清理");
            result.put("clearedCaches", cacheManager.getCacheNames());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 获取线程池状态
     */
    @GetMapping("/threadpool/stats")
    public ResponseEntity<Map<String, Object>> getThreadPoolStats() {
        Map<String, Object> stats = new HashMap<>();
        
        if (taskExecutor instanceof org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor) {
            org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor executor = 
                (org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor) taskExecutor;
            
            stats.put("threadPool", Map.of(
                "corePoolSize", executor.getCorePoolSize(),
                "maxPoolSize", executor.getMaxPoolSize(),
                "activeThreads", executor.getActiveCount(),
                "poolSize", executor.getPoolSize(),
                "queueSize", executor.getThreadPoolExecutor().getQueue().size(),
                "completedTasks", executor.getThreadPoolExecutor().getCompletedTaskCount(),
                "totalTasks", executor.getThreadPoolExecutor().getTaskCount()
            ));
        }
        
        return ResponseEntity.ok(stats);
    }

    /**
     * 性能测试接口
     */
    @PostMapping("/test")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> performanceTest() {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Object> result = new HashMap<>();
            
            long startTime = System.currentTimeMillis();
            
            // 模拟一些操作
            try {
                Thread.sleep(100); // 模拟100ms的处理时间
                
                long endTime = System.currentTimeMillis();
                long responseTime = endTime - startTime;
                
                result.put("success", true);
                result.put("responseTime", responseTime);
                result.put("message", "性能测试完成");
                result.put("timestamp", System.currentTimeMillis());
                
            } catch (InterruptedException e) {
                result.put("success", false);
                result.put("error", "性能测试被中断");
            }
            
            return ResponseEntity.ok(result);
        }, taskExecutor);
    }

    /**
     * 获取启动性能数据
     */
    @GetMapping("/startup")
    public ResponseEntity<Map<String, Object>> getStartupPerformance() {
        Map<String, Object> data = new HashMap<>();
        
        // 获取JVM启动时间
        long startTime = ManagementFactory.getRuntimeMXBean().getStartTime();
        long currentTime = System.currentTimeMillis();
        long startupTime = currentTime - startTime;
        
        data.put("startTime", startTime);
        data.put("currentTime", currentTime);
        data.put("startupTime", startupTime);
        data.put("startupTimeFormatted", formatUptime(startupTime));
        
        // 获取类加载统计
        java.lang.management.ClassLoadingMXBean classLoadingBean = ManagementFactory.getClassLoadingMXBean();
        data.put("classLoading", Map.of(
            "loadedClasses", classLoadingBean.getLoadedClassCount(),
            "totalLoadedClasses", classLoadingBean.getTotalLoadedClassCount(),
            "unloadedClasses", classLoadingBean.getUnloadedClassCount()
        ));
        
        return ResponseEntity.ok(data);
    }

    /**
     * 格式化运行时间
     */
    private String formatUptime(long uptime) {
        long seconds = uptime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return String.format("%d天 %d小时 %d分钟 %d秒", days, hours % 24, minutes % 60, seconds % 60);
        } else if (hours > 0) {
            return String.format("%d小时 %d分钟 %d秒", hours, minutes % 60, seconds % 60);
        } else if (minutes > 0) {
            return String.format("%d分钟 %d秒", minutes, seconds % 60);
        } else {
            return String.format("%d秒", seconds);
        }
    }
} 