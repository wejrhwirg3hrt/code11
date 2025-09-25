package org.example.service;

import org.example.entity.User;
import org.example.entity.Violation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = false)
public class ViolationService {

    @Autowired
    private UserService userService;

    public List<Map<String, Object>> getRecentViolations() {
        // 模拟违规记录数据
        List<Map<String, Object>> violations = new ArrayList<>();

        Map<String, Object> violation1 = new HashMap<>();
        violation1.put("id", 1L);
        violation1.put("type", "SPAM");
        violation1.put("description", "垃圾评论");
        violation1.put("userId", 123L);
        violation1.put("status", "PENDING");
        violation1.put("createdAt", LocalDateTime.now());
        violations.add(violation1);

        Map<String, Object> violation2 = new HashMap<>();
        violation2.put("id", 2L);
        violation2.put("type", "INAPPROPRIATE_CONTENT");
        violation2.put("description", "不当内容");
        violation2.put("userId", 124L);
        violation2.put("status", "RESOLVED");
        violation2.put("createdAt", LocalDateTime.now().minusDays(1));
        violations.add(violation2);

        return violations;
    }

    public List<Violation> getAllViolations() {
        // 返回空列表，实际项目中应该从数据库查询
        return new ArrayList<>();
    }

    // 警告用户 - 重构为不直接操作用户数据，避免循环依赖
    public void warnUser(Long userId, String reason, User admin) {
        // 记录警告违规
        recordViolation(userId, "USER_WARNING", reason, admin != null ? admin.getId() : null);
        System.out.println("用户警告记录: ID=" + userId + ", 原因=" + reason);
    }

    // 记录违规
    public void recordViolation(Long userId, String type, String reason, Long adminId) {
        // 这里应该保存到违规记录表
        System.out.println("记录违规: 用户ID=" + userId + ", 类型=" + type + ", 原因=" + reason);
    }

    // 记录用户注销违规
    public void recordAccountDeletion(Long userId, String reason, String adminUsername) {
        recordViolation(userId, "ACCOUNT_DELETION", reason, null);
    }
}