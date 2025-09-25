package org.example.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.document.VideoDocument;
import org.example.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 搜索API控制器
 * 提供高性能的视频搜索功能
 */
@RestController
@RequestMapping("/api/search")
@Tag(name = "搜索API", description = "视频搜索相关接口")
public class SearchApiController {

    @Autowired
    private SearchService searchService;

    @Operation(summary = "智能搜索", description = "支持多字段智能搜索视频")
    @GetMapping("/smart")
    public ResponseEntity<Page<VideoDocument>> smartSearch(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<VideoDocument> results = searchService.smartSearch(keyword, pageable);
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "高级搜索", description = "支持多条件组合的高级搜索")
    @GetMapping("/advanced")
    public ResponseEntity<Page<VideoDocument>> advancedSearch(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "视频状态") @RequestParam(defaultValue = "APPROVED") String status,
            @Parameter(description = "最小观看次数") @RequestParam(defaultValue = "0") Long minViews,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<VideoDocument> results = searchService.advancedSearch(keyword, status, minViews, pageable);
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "按标签搜索", description = "根据标签搜索相关视频")
    @GetMapping("/tag/{tag}")
    public ResponseEntity<Page<VideoDocument>> searchByTag(
            @Parameter(description = "标签名称") @PathVariable String tag,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<VideoDocument> results = searchService.searchByTag(tag, pageable);
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "按用户搜索", description = "搜索特定用户上传的视频")
    @GetMapping("/user/{username}")
    public ResponseEntity<Page<VideoDocument>> searchByUser(
            @Parameter(description = "用户名") @PathVariable String username,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<VideoDocument> results = searchService.searchByUser(username, pageable);
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "热门视频", description = "获取按观看次数排序的热门视频")
    @GetMapping("/popular")
    public ResponseEntity<Page<VideoDocument>> getPopularVideos(
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<VideoDocument> results = searchService.getPopularVideos(pageable);
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "最新视频", description = "获取按时间排序的最新视频")
    @GetMapping("/latest")
    public ResponseEntity<Page<VideoDocument>> getLatestVideos(
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<VideoDocument> results = searchService.getLatestVideos(pageable);
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "搜索建议", description = "获取搜索关键词的自动补全建议")
    @GetMapping("/suggestions")
    public ResponseEntity<List<String>> getSearchSuggestions(
            @Parameter(description = "搜索前缀") @RequestParam String prefix) {
        
        List<String> suggestions = searchService.getSearchSuggestions(prefix);
        return ResponseEntity.ok(suggestions);
    }

    @Operation(summary = "数据库搜索优化", description = "优化MySQL数据库搜索性能")
    @PostMapping("/optimize")
    public ResponseEntity<String> optimizeSearch() {
        try {
            // MySQL数据库搜索不需要重建索引，返回成功信息
            return ResponseEntity.ok("MySQL数据库搜索已优化");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("搜索优化失败: " + e.getMessage());
        }
    }

    @Operation(summary = "搜索服务状态", description = "检查搜索服务的状态和配置")
    @GetMapping("/status")
    public ResponseEntity<SearchStatus> getSearchStatus() {
        SearchStatus status = new SearchStatus();
        status.setElasticsearchEnabled(false);
        status.setSearchMode("MySQL Database");
        status.setMessage("使用MySQL数据库搜索");
        return ResponseEntity.ok(status);
    }

    /**
     * 搜索状态信息类
     */
    public static class SearchStatus {
        private boolean elasticsearchEnabled;
        private String searchMode;
        private String message;

        // Getters and Setters
        public boolean isElasticsearchEnabled() { return elasticsearchEnabled; }
        public void setElasticsearchEnabled(boolean elasticsearchEnabled) { this.elasticsearchEnabled = elasticsearchEnabled; }

        public String getSearchMode() { return searchMode; }
        public void setSearchMode(String searchMode) { this.searchMode = searchMode; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
