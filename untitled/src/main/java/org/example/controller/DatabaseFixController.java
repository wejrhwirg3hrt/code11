package org.example.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fix")
public class DatabaseFixController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/user-moment-table")
    public String fixUserMomentTable() {
        try {
            // 检查当前表结构
            List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                "DESCRIBE user_moment"
            );
            
            StringBuilder result = new StringBuilder();
            result.append("当前表结构:\n");
            for (Map<String, Object> column : columns) {
                result.append(column.toString()).append("\n");
            }
            
            // 删除重复的驼峰命名字段
            String[] duplicateColumns = {
                "createTime", "commentCount", "isPublic", "likeCount", "userId", "viewCount"
            };
            
            for (String column : duplicateColumns) {
                try {
                    jdbcTemplate.execute("ALTER TABLE user_moment DROP COLUMN " + column);
                    result.append("成功删除重复字段: ").append(column).append("\n");
                } catch (Exception e) {
                    result.append("删除字段 ").append(column).append(" 失败: ").append(e.getMessage()).append("\n");
                }
            }
            
            // 确保create_time字段有默认值
            try {
                jdbcTemplate.execute(
                    "ALTER TABLE user_moment MODIFY COLUMN create_time DATETIME DEFAULT CURRENT_TIMESTAMP"
                );
                result.append("成功设置 create_time 默认值\n");
            } catch (Exception e) {
                result.append("设置 create_time 默认值失败: ").append(e.getMessage()).append("\n");
            }
            
            // 检查修复后的表结构
            List<Map<String, Object>> newColumns = jdbcTemplate.queryForList(
                "DESCRIBE user_moment"
            );
            
            result.append("\n修复后的表结构:\n");
            for (Map<String, Object> column : newColumns) {
                result.append(column.toString()).append("\n");
            }
            
            return result.toString();
            
        } catch (Exception e) {
            return "修复失败: " + e.getMessage();
        }
    }

    @GetMapping("/check-user-moment-table")
    public ResponseEntity<String> checkUserMomentTable() {
        try {
            String sql = "DESCRIBE user_moment";
            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
            
            StringBuilder response = new StringBuilder("user_moment表结构:\n");
            for (Map<String, Object> row : result) {
                response.append(String.format("字段: %s, 类型: %s, 是否为空: %s, 键: %s, 默认值: %s, 额外: %s\n",
                    row.get("Field"), row.get("Type"), row.get("Null"), 
                    row.get("Key"), row.get("Default"), row.get("Extra")));
            }
            
            return ResponseEntity.ok(response.toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("检查表结构失败: " + e.getMessage());
        }
    }

    @GetMapping("/clean-user-moment-table")
    public String cleanUserMomentTable() {
        try {
            StringBuilder result = new StringBuilder();
            
            // 首先删除外键约束
            try {
                jdbcTemplate.execute("ALTER TABLE user_moment DROP FOREIGN KEY FK6e1vkf59bb61morsl5vbj3mux");
                result.append("成功删除外键约束 FK6e1vkf61morsl5vbj3mux\n");
            } catch (Exception e) {
                result.append("删除外键约束失败: ").append(e.getMessage()).append("\n");
            }
            
            // 删除重复的驼峰命名字段
            String[] duplicateColumns = {
                "createTime", "commentCount", "isPublic", "likeCount", "userId", "viewCount"
            };
            
            for (String column : duplicateColumns) {
                try {
                    jdbcTemplate.execute("ALTER TABLE user_moment DROP COLUMN `" + column + "`");
                    result.append("成功删除重复字段: ").append(column).append("\n");
                } catch (Exception e) {
                    result.append("删除字段 ").append(column).append(" 失败: ").append(e.getMessage()).append("\n");
                }
            }
            
            // 确保create_time字段有默认值
            try {
                jdbcTemplate.execute(
                    "ALTER TABLE user_moment MODIFY COLUMN create_time DATETIME DEFAULT CURRENT_TIMESTAMP"
                );
                result.append("成功设置 create_time 默认值\n");
            } catch (Exception e) {
                result.append("设置 create_time 默认值失败: ").append(e.getMessage()).append("\n");
            }
            
            // 检查修复后的表结构
            List<Map<String, Object>> newColumns = jdbcTemplate.queryForList(
                "DESCRIBE user_moment"
            );
            
            result.append("\n修复后的表结构:\n");
            for (Map<String, Object> column : newColumns) {
                result.append(column.toString()).append("\n");
            }
            
            return result.toString();
            
        } catch (Exception e) {
            return "修复失败: " + e.getMessage();
        }
    }
} 