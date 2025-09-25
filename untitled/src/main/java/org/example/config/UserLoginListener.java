package org.example.config;

import org.example.entity.User;
import org.example.service.UserOnlineStatusService;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 用户登录成功监听器
 * 当用户登录成功时自动设置为在线状态
 */
@Component
public class UserLoginListener implements ApplicationListener<AuthenticationSuccessEvent> {

    @Autowired
    private UserOnlineStatusService userOnlineStatusService;
    
    @Autowired
    private UserService userService;

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        try {
            Object principal = event.getAuthentication().getPrincipal();
            
            if (principal instanceof User) {
                User user = (User) principal;
                
                // 设置用户为在线状态
                userOnlineStatusService.userOnline(user.getId(), "web-login-" + System.currentTimeMillis());
                userOnlineStatusService.broadcastOnlineCount();
                
                System.out.println("用户登录成功，自动设置在线状态: " + user.getUsername() + " (ID: " + user.getId() + ")");
            } else if (principal instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) principal;
                String username = userDetails.getUsername();
                
                // 通过用户名查找用户实体
                Optional<User> userOpt = userService.findByUsername(username);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    
                    // 设置用户为在线状态
                    userOnlineStatusService.userOnline(user.getId(), "web-login-" + System.currentTimeMillis());
                    userOnlineStatusService.broadcastOnlineCount();
                    
                    System.out.println("用户登录成功，自动设置在线状态: " + user.getUsername() + " (ID: " + user.getId() + ")");
                } else {
                    System.out.println("用户登录成功: " + username + "，但无法找到用户实体");
                }
            } else {
                System.out.println("用户登录成功，但principal类型不支持: " + principal.getClass().getSimpleName());
            }
        } catch (Exception e) {
            System.err.println("处理用户登录事件失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
