package org.example.util;

import org.example.entity.User;

/**
 * 头像处理工具类
 * 统一处理用户头像的显示逻辑
 */
public class AvatarUtil {
    
    /**
     * 默认头像路径 - 统一使用主页的默认头像
     */
    public static final String DEFAULT_AVATAR_PATH = "/images/default-avatar.png";
    
    /**
     * 备用默认头像路径
     */
    public static final String FALLBACK_AVATAR_PATH = "/images/default-avatar.svg";
    
    /**
     * 获取用户头像URL
     * 如果用户没有设置头像或头像为空，返回默认头像
     * 
     * @param user 用户对象
     * @return 头像URL
     */
    public static String getUserAvatarUrl(User user) {
        if (user == null) {
            return DEFAULT_AVATAR_PATH;
        }
        
        String avatar = user.getAvatar();
        
        // 如果头像为空或者是无效路径，返回统一的默认头像
        if (avatar == null || avatar.trim().isEmpty() || 
            isInvalidAvatarPath(avatar)) {
            return DEFAULT_AVATAR_PATH;
        }
        
        return avatar;
    }
    
    /**
     * 检查是否为无效的头像路径
     * 
     * @param avatarPath 头像路径
     * @return 是否为无效路径
     */
    public static boolean isInvalidAvatarPath(String avatarPath) {
        if (avatarPath == null || avatarPath.trim().isEmpty()) {
            return true;
        }
        
        // 检查是否为旧的默认头像路径或无效路径
        return avatarPath.equals("/uploads/avatars/default.png") ||
               avatarPath.equals("/uploads/avatars/default.svg") ||
               avatarPath.equals("/images/default-avatar.svg") ||
               avatarPath.contains("c04a9a52-0da5-4e46-9526-8df8047d0dbb.jpeg") || // 特定的无效文件
               avatarPath.startsWith("avatars/") && !avatarPath.startsWith("/avatars/"); // 相对路径
    }
    
    /**
     * 检查是否为默认头像
     * 
     * @param avatarUrl 头像URL
     * @return 是否为默认头像
     */
    public static boolean isDefaultAvatar(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
            return true;
        }
        
        return avatarUrl.equals(DEFAULT_AVATAR_PATH) ||
               avatarUrl.equals(FALLBACK_AVATAR_PATH) ||
               avatarUrl.equals("/uploads/avatars/default.png") ||
               avatarUrl.equals("/uploads/avatars/default.svg") ||
               avatarUrl.equals("/images/default-avatar.svg");
    }
    
    /**
     * 设置用户默认头像
     * 
     * @param user 用户对象
     */
    public static void setDefaultAvatar(User user) {
        if (user != null) {
            user.setAvatar(DEFAULT_AVATAR_PATH);
        }
    }
    
    /**
     * 验证头像URL是否有效
     * 
     * @param avatarUrl 头像URL
     * @return 是否有效
     */
    public static boolean isValidAvatarUrl(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
            return false;
        }
        
        // 检查是否为有效的图片URL
        return avatarUrl.startsWith("http") || 
               avatarUrl.startsWith("/") ||
               avatarUrl.startsWith("data:image/");
    }
    
    /**
     * 获取安全的头像URL
     * 如果URL无效，返回默认头像
     * 
     * @param avatarUrl 原始头像URL
     * @return 安全的头像URL
     */
    public static String getSafeAvatarUrl(String avatarUrl) {
        if (isValidAvatarUrl(avatarUrl) && !isInvalidAvatarPath(avatarUrl)) {
            return avatarUrl;
        }
        return DEFAULT_AVATAR_PATH;
    }
}
