package org.example.controller.api;

import org.example.entity.SearchHistory;
import org.example.entity.User;
import org.example.service.SearchService;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 搜索建议API控制器
 */
@RestController
@RequestMapping("/api/search")
public class SearchSuggestionController {

    @Autowired
    private SearchService searchService;

    @Autowired
    private UserService userService;

    /**
     * 获取搜索建议
     */
    @GetMapping("/suggestions-enhanced")
    public ResponseEntity<Map<String, Object>> getSearchSuggestions(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") int limit,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            User currentUser = null;
            if (authentication != null) {
                Optional<User> userOpt = userService.findByUsername(authentication.getName());
                currentUser = userOpt.orElse(null);
            }
            
            List<String> suggestions = searchService.getSearchSuggestions(q, currentUser, limit);
            
            response.put("success", true);
            response.put("suggestions", suggestions);
            response.put("query", q);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取搜索建议失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取热门搜索关键词
     */
    @GetMapping("/hot-keywords")
    public ResponseEntity<Map<String, Object>> getHotKeywords(
            @RequestParam(defaultValue = "10") int limit) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<String> hotKeywords = searchService.getHotSearchKeywords(limit);
            
            response.put("success", true);
            response.put("hotKeywords", hotKeywords);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取热门搜索失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取用户搜索历史
     */
    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getUserSearchHistory(
            @RequestParam(defaultValue = "20") int limit,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        if (authentication == null) {
            response.put("success", false);
            response.put("message", "请先登录");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            if (!userOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.status(404).body(response);
            }
            
            User user = userOpt.get();
            List<SearchHistory> searchHistory = searchService.getUserSearchHistory(user, limit);
            
            response.put("success", true);
            response.put("searchHistory", searchHistory);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取搜索历史失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 清除用户搜索历史
     */
    @DeleteMapping("/history")
    public ResponseEntity<Map<String, Object>> clearSearchHistory(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        if (authentication == null) {
            response.put("success", false);
            response.put("message", "请先登录");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            if (!userOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.status(404).body(response);
            }
            
            User user = userOpt.get();
            searchService.clearUserSearchHistory(user);
            
            response.put("success", true);
            response.put("message", "搜索历史已清除");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "清除搜索历史失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}
