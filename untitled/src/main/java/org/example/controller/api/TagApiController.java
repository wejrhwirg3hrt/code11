package org.example.controller.api;

import org.example.entity.Tag;
import org.example.entity.User;
import org.example.service.TagService;
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
 * 标签API控制器
 */
@RestController
@RequestMapping("/api/tags")
@CrossOrigin(originPatterns = "*")
public class TagApiController {

    @Autowired
    private TagService tagService;

    @Autowired
    private UserService userService;

    /**
     * 获取所有启用的标签
     */
    @GetMapping
    public ResponseEntity<List<Tag>> getAllTags() {
        List<Tag> tags = tagService.getAllActiveTags();
        return ResponseEntity.ok(tags);
    }

    /**
     * 获取热门标签
     */
    @GetMapping("/popular")
    public ResponseEntity<List<Tag>> getPopularTags(@RequestParam(defaultValue = "10") int limit) {
        List<Tag> tags = tagService.getPopularTags(limit);
        return ResponseEntity.ok(tags);
    }

    /**
     * 搜索标签
     */
    @GetMapping("/search")
    public ResponseEntity<List<Tag>> searchTags(@RequestParam String keyword) {
        List<Tag> tags = tagService.searchTags(keyword);
        return ResponseEntity.ok(tags);
    }

    /**
     * 根据ID获取标签
     */
    @GetMapping("/{id}")
    public ResponseEntity<Tag> getTagById(@PathVariable Long id) {
        Optional<Tag> tag = tagService.findById(id);
        return tag.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 创建新标签
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createTag(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String color,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (authentication == null) {
                response.put("success", false);
                response.put("message", "请先登录");
                return ResponseEntity.ok(response);
            }

            Optional<User> user = userService.findByUsername(authentication.getName());
            if (!user.isPresent()) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.ok(response);
            }

            Tag tag = tagService.createTag(name, description, color, user.get());
            
            response.put("success", true);
            response.put("message", "标签创建成功");
            response.put("tag", tag);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "创建标签失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 更新标签
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateTag(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String color,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (authentication == null) {
                response.put("success", false);
                response.put("message", "请先登录");
                return ResponseEntity.ok(response);
            }

            Tag tag = tagService.updateTag(id, name, description, color);
            
            response.put("success", true);
            response.put("message", "标签更新成功");
            response.put("tag", tag);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新标签失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 删除标签
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteTag(
            @PathVariable Long id,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (authentication == null) {
                response.put("success", false);
                response.put("message", "请先登录");
                return ResponseEntity.ok(response);
            }

            tagService.deleteTag(id);
            
            response.put("success", true);
            response.put("message", "标签删除成功");
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除标签失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 启用/禁用标签
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Map<String, Object>> toggleTagStatus(
            @PathVariable Long id,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (authentication == null) {
                response.put("success", false);
                response.put("message", "请先登录");
                return ResponseEntity.ok(response);
            }

            Tag tag = tagService.toggleTagStatus(id);
            
            response.put("success", true);
            response.put("message", tag.getIsActive() ? "标签已启用" : "标签已禁用");
            response.put("tag", tag);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "操作失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取标签统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getTagStatistics() {
        Map<String, Object> stats = tagService.getTagStatistics();
        return ResponseEntity.ok(stats);
    }
}
