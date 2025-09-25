/*
 Navicat Premium Data Transfer

 Source Server         : 11111
 Source Server Type    : MySQL
 Source Server Version : 80033 (8.0.33)
 Source Host           : localhost:3306
 Source Schema         : video_website

 Target Server Type    : MySQL
 Target Server Version : 80033 (8.0.33)
 File Encoding         : 65001

 Date: 29/08/2025 21:56:26
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for achievements
-- ----------------------------
DROP TABLE IF EXISTS `achievements`;
CREATE TABLE `achievements`  (
  `is_active` bit(1) NOT NULL,
  `points` int NOT NULL,
  `condition_value` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  `condition_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `category` enum('BASIC','MILESTONE','SOCIAL','SPECIAL','UPLOAD','WATCH') CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `rarity` enum('COMMON','EPIC','LEGENDARY','RARE','UNCOMMON') CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK_ktpif54u9a3ssn6rpxxqx6jvp`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 52 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of achievements
-- ----------------------------
INSERT INTO `achievements` VALUES (b'1', 10, 1, '2025-08-19 00:42:33.295192', 1, '2025-08-19 00:42:33.295192', 'VIDEO_COUNT', '首次上传', '上传第一个视频', 'UPLOAD', 'fa-upload', 'COMMON');
INSERT INTO `achievements` VALUES (b'1', 25, 5, '2025-08-19 00:42:33.306215', 2, '2025-08-19 00:42:33.306215', 'VIDEO_COUNT', '初级创作者', '上传5个视频', 'UPLOAD', 'fa-video', 'COMMON');
INSERT INTO `achievements` VALUES (b'1', 50, 10, '2025-08-19 00:42:33.307186', 3, '2025-08-19 00:42:33.307186', 'VIDEO_COUNT', '活跃创作者', '上传10个视频', 'UPLOAD', 'fa-film', 'UNCOMMON');
INSERT INTO `achievements` VALUES (b'1', 100, 25, '2025-08-19 00:42:33.307186', 4, '2025-08-19 00:42:33.307186', 'VIDEO_COUNT', '资深创作者', '上传25个视频', 'UPLOAD', 'fa-camera', 'RARE');
INSERT INTO `achievements` VALUES (b'1', 200, 50, '2025-08-19 00:42:33.311700', 5, '2025-08-19 00:42:33.311700', 'VIDEO_COUNT', '专业创作者', '上传50个视频', 'UPLOAD', 'fa-broadcast-tower', 'EPIC');
INSERT INTO `achievements` VALUES (b'1', 500, 100, '2025-08-19 00:42:33.311700', 6, '2025-08-19 00:42:33.311700', 'VIDEO_COUNT', '传奇创作者', '上传100个视频', 'UPLOAD', 'fa-crown', 'LEGENDARY');
INSERT INTO `achievements` VALUES (b'1', 1000, 200, '2025-08-19 00:42:33.311700', 7, '2025-08-19 00:42:33.311700', 'VIDEO_COUNT', '高产作家', '上传200个视频', 'UPLOAD', 'fa-trophy', 'LEGENDARY');
INSERT INTO `achievements` VALUES (b'1', 5, 1, '2025-08-19 00:42:33.311700', 8, '2025-08-19 00:42:33.311700', 'LIKE_COUNT', '初次点赞', '获得第一个点赞', 'SOCIAL', 'fa-thumbs-up', 'COMMON');
INSERT INTO `achievements` VALUES (b'1', 20, 10, '2025-08-19 00:42:33.318710', 9, '2025-08-19 00:42:33.318710', 'LIKE_COUNT', '受欢迎', '获得10个点赞', 'SOCIAL', 'fa-heart', 'COMMON');
INSERT INTO `achievements` VALUES (b'1', 40, 50, '2025-08-19 00:42:33.318710', 10, '2025-08-19 00:42:33.318710', 'LIKE_COUNT', '小有名气', '获得50个点赞', 'SOCIAL', 'fa-star', 'UNCOMMON');
INSERT INTO `achievements` VALUES (b'1', 75, 100, '2025-08-19 00:42:33.322096', 11, '2025-08-19 00:42:33.322096', 'LIKE_COUNT', '点赞达人', '获得100个点赞', 'SOCIAL', 'fa-fire', 'UNCOMMON');
INSERT INTO `achievements` VALUES (b'1', 150, 500, '2025-08-19 00:42:33.323060', 12, '2025-08-19 00:42:33.323060', 'LIKE_COUNT', '人气之星', '获得500个点赞', 'SOCIAL', 'fa-gem', 'RARE');
INSERT INTO `achievements` VALUES (b'1', 300, 1000, '2025-08-19 00:42:33.323060', 13, '2025-08-19 00:42:33.323060', 'LIKE_COUNT', '网红达人', '获得1000个点赞', 'SOCIAL', 'fa-medal', 'EPIC');
INSERT INTO `achievements` VALUES (b'1', 750, 5000, '2025-08-19 00:42:33.323060', 14, '2025-08-19 00:42:33.323060', 'LIKE_COUNT', '超级明星', '获得5000个点赞', 'SOCIAL', 'fa-crown', 'LEGENDARY');
INSERT INTO `achievements` VALUES (b'1', 5, 1, '2025-08-19 00:42:33.328130', 15, '2025-08-19 00:42:33.328130', 'COMMENT_COUNT', '初次评论', '发表第一条评论', 'SOCIAL', 'fa-comment', 'COMMON');
INSERT INTO `achievements` VALUES (b'1', 15, 10, '2025-08-19 00:42:33.328130', 16, '2025-08-19 00:42:33.328130', 'COMMENT_COUNT', '话痨', '发表10条评论', 'SOCIAL', 'fa-comments', 'COMMON');
INSERT INTO `achievements` VALUES (b'1', 35, 50, '2025-08-19 00:42:33.328130', 17, '2025-08-19 00:42:33.328130', 'COMMENT_COUNT', '评论专家', '发表50条评论', 'SOCIAL', 'fa-comment-dots', 'UNCOMMON');
INSERT INTO `achievements` VALUES (b'1', 60, 100, '2025-08-19 00:42:33.328130', 18, '2025-08-19 00:42:33.328130', 'COMMENT_COUNT', '互动达人', '发表100条评论', 'SOCIAL', 'fa-handshake', 'RARE');
INSERT INTO `achievements` VALUES (b'1', 150, 500, '2025-08-19 00:42:33.335936', 19, '2025-08-19 00:42:33.335936', 'COMMENT_COUNT', '社区活跃分子', '发表500条评论', 'SOCIAL', 'fa-users', 'EPIC');
INSERT INTO `achievements` VALUES (b'1', 5, 1, '2025-08-19 00:42:33.337245', 20, '2025-08-19 00:42:33.337245', 'WATCH_TIME', '初次观看', '观看第一个视频', 'WATCH', 'fa-play', 'COMMON');
INSERT INTO `achievements` VALUES (b'1', 10, 3600, '2025-08-19 00:42:33.337245', 21, '2025-08-19 00:42:33.337245', 'WATCH_TIME', '电影爱好者', '观看视频超过1小时', 'WATCH', 'fa-clock', 'COMMON');
INSERT INTO `achievements` VALUES (b'1', 25, 36000, '2025-08-19 00:42:33.340269', 22, '2025-08-19 00:42:33.340269', 'WATCH_TIME', '追剧达人', '观看视频超过10小时', 'WATCH', 'fa-tv', 'UNCOMMON');
INSERT INTO `achievements` VALUES (b'1', 75, 180000, '2025-08-19 00:42:33.341286', 23, '2025-08-19 00:42:33.341286', 'WATCH_TIME', '观影狂人', '观看视频超过50小时', 'WATCH', 'fa-eye', 'RARE');
INSERT INTO `achievements` VALUES (b'1', 150, 360000, '2025-08-19 00:42:33.342277', 24, '2025-08-19 00:42:33.342277', 'WATCH_TIME', '视频收藏家', '观看视频超过100小时', 'WATCH', 'fa-archive', 'EPIC');
INSERT INTO `achievements` VALUES (b'1', 500, 1800000, '2025-08-19 00:42:33.343951', 25, '2025-08-19 00:42:33.343951', 'WATCH_TIME', '终极观众', '观看视频超过500小时', 'WATCH', 'fa-infinity', 'LEGENDARY');
INSERT INTO `achievements` VALUES (b'1', 10, 1, '2025-08-19 00:42:33.345380', 26, '2025-08-19 00:42:33.345380', 'PROFILE_COMPLETE', '新手上路', '完成个人资料设置', 'BASIC', 'fa-user-edit', 'COMMON');
INSERT INTO `achievements` VALUES (b'1', 20, 7, '2025-08-19 00:42:33.345380', 27, '2025-08-19 00:42:33.345380', 'CONSECUTIVE_DAYS', '坚持不懈', '连续登录7天', 'BASIC', 'fa-calendar-check', 'COMMON');
INSERT INTO `achievements` VALUES (b'1', 50, 30, '2025-08-19 00:42:33.345380', 28, '2025-08-19 00:42:33.345380', 'CONSECUTIVE_DAYS', '忠实用户', '连续登录30天', 'BASIC', 'fa-calendar-alt', 'UNCOMMON');
INSERT INTO `achievements` VALUES (b'1', 150, 100, '2025-08-19 00:42:33.345380', 29, '2025-08-19 00:42:33.345380', 'CONSECUTIVE_DAYS', '铁杆粉丝', '连续登录100天', 'BASIC', 'fa-calendar-plus', 'RARE');
INSERT INTO `achievements` VALUES (b'1', 500, 365, '2025-08-19 00:42:33.345380', 30, '2025-08-19 00:42:33.345380', 'CONSECUTIVE_DAYS', '超级用户', '连续登录365天', 'BASIC', 'fa-calendar-star', 'LEGENDARY');
INSERT INTO `achievements` VALUES (b'1', 30, 10, '2025-08-19 00:42:33.353835', 31, '2025-08-19 00:42:33.353835', 'SHARE_COUNT', '分享达人', '分享10个视频', 'SOCIAL', 'fa-share', 'UNCOMMON');
INSERT INTO `achievements` VALUES (b'1', 40, 50, '2025-08-19 00:42:33.354849', 32, '2025-08-19 00:42:33.354849', 'FAVORITE_COUNT', '收藏家', '收藏50个视频', 'SOCIAL', 'fa-bookmark', 'RARE');
INSERT INTO `achievements` VALUES (b'1', 15, 2, '2025-08-19 00:42:33.355852', 33, '2025-08-19 00:42:33.355852', 'LEVEL', '新手', '达到2级', 'MILESTONE', 'fa-seedling', 'COMMON');
INSERT INTO `achievements` VALUES (b'1', 25, 5, '2025-08-19 00:42:33.356794', 34, '2025-08-19 00:42:33.356794', 'LEVEL', '进阶用户', '达到5级', 'MILESTONE', 'fa-leaf', 'COMMON');
INSERT INTO `achievements` VALUES (b'1', 50, 10, '2025-08-19 00:42:33.356794', 35, '2025-08-19 00:42:33.356794', 'LEVEL', '等级达人', '达到10级', 'MILESTONE', 'fa-star', 'UNCOMMON');
INSERT INTO `achievements` VALUES (b'1', 100, 20, '2025-08-19 00:42:33.360009', 36, '2025-08-19 00:42:33.360009', 'LEVEL', '资深用户', '达到20级', 'MILESTONE', 'fa-award', 'RARE');
INSERT INTO `achievements` VALUES (b'1', 250, 50, '2025-08-19 00:42:33.361597', 37, '2025-08-19 00:42:33.361597', 'LEVEL', '专家级别', '达到50级', 'MILESTONE', 'fa-trophy', 'EPIC');
INSERT INTO `achievements` VALUES (b'1', 500, 100, '2025-08-19 00:42:33.361597', 38, '2025-08-19 00:42:33.361597', 'LEVEL', '传奇等级', '达到100级', 'MILESTONE', 'fa-crown', 'LEGENDARY');
INSERT INTO `achievements` VALUES (b'1', 30, 1, '2025-08-19 00:42:33.361597', 39, '2025-08-19 00:42:33.361597', 'EARLY_UPLOAD', '早起鸟儿', '在早上6点前上传视频', 'SPECIAL', 'fa-sun', 'UNCOMMON');
INSERT INTO `achievements` VALUES (b'1', 30, 1, '2025-08-19 00:42:33.367267', 40, '2025-08-19 00:42:33.367267', 'LATE_UPLOAD', '夜猫子', '在深夜12点后上传视频', 'SPECIAL', 'fa-moon', 'UNCOMMON');
INSERT INTO `achievements` VALUES (b'1', 20, 1, '2025-08-19 00:42:33.368279', 41, '2025-08-19 00:42:33.368279', 'WEEKEND_UPLOAD', '周末战士', '在周末上传视频', 'SPECIAL', 'fa-calendar-weekend', 'COMMON');
INSERT INTO `achievements` VALUES (b'1', 50, 1, '2025-08-19 00:42:33.370283', 42, '2025-08-19 00:42:33.370283', 'HOLIDAY_UPLOAD', '节日庆祝', '在节假日上传视频', 'SPECIAL', 'fa-gift', 'RARE');
INSERT INTO `achievements` VALUES (b'1', 100, 1, '2025-08-19 00:42:33.370283', 43, '2025-08-19 00:42:33.370283', 'BIRTHDAY_LOGIN', '生日快乐', '在生日当天登录', 'SPECIAL', 'fa-birthday-cake', 'EPIC');
INSERT INTO `achievements` VALUES (b'1', 200, 1, '2025-08-19 00:42:33.370283', 44, '2025-08-19 00:42:33.370283', 'PERFECT_RATING', '完美主义者', '上传的视频获得100%好评率', 'SPECIAL', 'fa-check-circle', 'LEGENDARY');
INSERT INTO `achievements` VALUES (b'1', 300, 10000, '2025-08-19 00:42:33.375391', 45, '2025-08-19 00:42:33.375391', 'VIRAL_VIDEO', '病毒传播', '单个视频获得10000次观看', 'SPECIAL', 'fa-virus', 'LEGENDARY');
INSERT INTO `achievements` VALUES (b'1', 75, 100, '2025-08-19 00:42:33.377383', 46, '2025-08-19 00:42:33.377383', 'FOLLOW_COUNT', '社交蝴蝶', '关注100个用户', 'SPECIAL', 'fa-user-friends', 'RARE');
INSERT INTO `achievements` VALUES (b'1', 500, 1000, '2025-08-19 00:42:33.378864', 47, '2025-08-19 00:42:33.378864', 'FOLLOWER_COUNT', '人气磁铁', '被1000个用户关注', 'SPECIAL', 'fa-magnet', 'LEGENDARY');
INSERT INTO `achievements` VALUES (b'1', 150, 5, '2025-08-19 00:42:33.378864', 48, '2025-08-19 00:42:33.378864', 'CATEGORY_DIVERSITY', '多才多艺', '在5个不同分类上传视频', 'SPECIAL', 'fa-palette', 'EPIC');
INSERT INTO `achievements` VALUES (b'1', 40, 1, '2025-08-19 00:42:33.378864', 49, '2025-08-19 00:42:33.378864', 'HD_UPLOAD', '技术专家', '上传高清视频', 'SPECIAL', 'fa-hd-video', 'UNCOMMON');
INSERT INTO `achievements` VALUES (b'1', 60, 10, '2025-08-19 00:42:33.378864', 50, '2025-08-19 00:42:33.378864', 'QUICK_REPLY', '速度之王', '快速回复评论', 'SPECIAL', 'fa-bolt', 'RARE');
INSERT INTO `achievements` VALUES (b'1', 100, 1, '2025-08-19 00:42:33.378864', 51, '2025-08-19 00:42:33.378864', 'EXPLORE_ALL', '探索者', '观看所有分类的视频', 'SPECIAL', 'fa-compass', 'EPIC');

-- ----------------------------
-- Table structure for admin_permission_types
-- ----------------------------
DROP TABLE IF EXISTS `admin_permission_types`;
CREATE TABLE `admin_permission_types`  (
  `permission_id` bigint NOT NULL,
  `permission_type` enum('ADMIN_BAN','ADMIN_CREATE','ADMIN_EDIT','ADMIN_PERMISSION','ANNOUNCEMENT_CREATE','ANNOUNCEMENT_DELETE','ANNOUNCEMENT_EDIT','CDN_CACHE','CDN_CONFIG','CDN_STATISTICS','COMMENT_BAN','COMMENT_DELETE','COMMENT_EDIT','COMMENT_VIEW','CONTENT_CATEGORY','CONTENT_REPORT','CONTENT_STATISTICS','CONTENT_TAG','SYSTEM_BACKUP','SYSTEM_CONFIG','SYSTEM_LOG','SYSTEM_MONITOR','USER_BAN','USER_DELETE','USER_EDIT','USER_VIEW','VIDEO_APPROVE','VIDEO_BAN','VIDEO_DELETE','VIDEO_EDIT','VIDEO_VIEW') CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  INDEX `FKrsayxv3kqc4cqpkkeqav71ab4`(`permission_id` ASC) USING BTREE,
  CONSTRAINT `FKrsayxv3kqc4cqpkkeqav71ab4` FOREIGN KEY (`permission_id`) REFERENCES `admin_permissions` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of admin_permission_types
-- ----------------------------

-- ----------------------------
-- Table structure for admin_permissions
-- ----------------------------
DROP TABLE IF EXISTS `admin_permissions`;
CREATE TABLE `admin_permissions`  (
  `active` bit(1) NULL DEFAULT NULL,
  `admin_id` bigint NULL DEFAULT NULL,
  `expires_at` datetime(6) NULL DEFAULT NULL,
  `granted_at` datetime(6) NULL DEFAULT NULL,
  `granted_by` bigint NULL DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of admin_permissions
-- ----------------------------

-- ----------------------------
-- Table structure for admin_users
-- ----------------------------
DROP TABLE IF EXISTS `admin_users`;
CREATE TABLE `admin_users`  (
  `banned` bit(1) NULL DEFAULT NULL,
  `enabled` bit(1) NULL DEFAULT NULL,
  `created_at` datetime(6) NULL DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `last_login` datetime(6) NULL DEFAULT NULL,
  `updated_at` datetime(6) NULL DEFAULT NULL,
  `ban_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `login_ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `role` enum('ADMIN','SUPER_ADMIN') CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK_cp8822350s9vtyww7xdbgeuvu`(`email` ASC) USING BTREE,
  UNIQUE INDEX `UK_3fgxk4ewgaxgtgvqwb1jjudj6`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of admin_users
-- ----------------------------
INSERT INTO `admin_users` VALUES (b'0', b'1', '2025-08-19 00:42:30.639706', 1, '2025-08-29 21:54:21.881077', '2025-08-19 00:42:30.639706', NULL, 'superadmin@example.com', '0:0:0:0:0:0:0:1', '$2a$10$JdRVHTzZE2mjfCmHOQKaueJEtkCQoWE2wgm7lDUW18eV22CmDBtbi', 'SUPER_ADMIN', 'superadmin');

-- ----------------------------
-- Table structure for announcements
-- ----------------------------
DROP TABLE IF EXISTS `announcements`;
CREATE TABLE `announcements`  (
  `active` bit(1) NOT NULL,
  `created_at` datetime(6) NULL DEFAULT NULL,
  `created_by` bigint NULL DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) NULL DEFAULT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKht7cvemps7a8tjylacwtyyckj`(`created_by` ASC) USING BTREE,
  CONSTRAINT `fk_announcements_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `FKht7cvemps7a8tjylacwtyyckj` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of announcements
-- ----------------------------

-- ----------------------------
-- Table structure for call_records
-- ----------------------------
DROP TABLE IF EXISTS `call_records`;
CREATE TABLE `call_records`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `call_status` enum('ACCEPTED','CALLING','ENDED','FAILED','MISSED','REJECTED') CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `call_type` enum('AUDIO','VIDEO') CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `duration` int NULL DEFAULT NULL,
  `end_time` datetime(6) NULL DEFAULT NULL,
  `ended_by` bigint NULL DEFAULT NULL,
  `room_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `start_time` datetime(6) NOT NULL,
  `updated_at` datetime(6) NULL DEFAULT NULL,
  `callee_id` bigint NOT NULL,
  `caller_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK861rhc88xclbg7xe3j2siipcb`(`callee_id` ASC) USING BTREE,
  INDEX `FKn2yt4qb1r1ejrjg6jvpu3ygy4`(`caller_id` ASC) USING BTREE,
  CONSTRAINT `fk_call_records_callee_id` FOREIGN KEY (`callee_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_call_records_caller_id` FOREIGN KEY (`caller_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `FKn2yt4qb1r1ejrjg6jvpu3ygy4` FOREIGN KEY (`caller_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 111 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of call_records
-- ----------------------------
INSERT INTO `call_records` VALUES (60, 'CALLING', 'VIDEO', '2025-08-20 09:50:54.169384', NULL, NULL, NULL, 'room_2_2_1755654654156', '2025-08-20 09:50:54.169384', '2025-08-20 09:50:54.169384', 2, 2);

-- ----------------------------
-- Table structure for categories
-- ----------------------------
DROP TABLE IF EXISTS `categories`;
CREATE TABLE `categories`  (
  `is_active` bit(1) NULL DEFAULT NULL,
  `sort_order` int NULL DEFAULT NULL,
  `created_at` datetime(6) NULL DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `color` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK_t8o6pivur7nn124jehx7cygw5`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of categories
-- ----------------------------
INSERT INTO `categories` VALUES (b'1', 0, '2025-08-19 00:42:33.123498', 1, NULL, '娱乐搞笑类视频', NULL, '娱乐');
INSERT INTO `categories` VALUES (b'1', 0, '2025-08-19 00:42:33.132405', 2, NULL, '教育学习类视频', NULL, '教育');
INSERT INTO `categories` VALUES (b'1', 0, '2025-08-19 00:42:33.140452', 3, NULL, '科技数码类视频', NULL, '科技');
INSERT INTO `categories` VALUES (b'1', 0, '2025-08-19 00:42:33.144797', 4, NULL, '音乐MV类视频', NULL, '音乐');
INSERT INTO `categories` VALUES (b'1', 0, '2025-08-19 00:42:33.154253', 5, NULL, '游戏相关视频', NULL, '游戏');
INSERT INTO `categories` VALUES (b'1', 0, '2025-08-19 00:42:33.161853', 6, NULL, '生活日常类视频', NULL, '生活');
INSERT INTO `categories` VALUES (b'1', 0, '2025-08-19 00:42:33.161853', 7, NULL, '美食制作类视频', NULL, '美食');
INSERT INTO `categories` VALUES (b'1', 0, '2025-08-19 00:42:33.178146', 8, NULL, '旅行风景类视频', NULL, '旅行');
INSERT INTO `categories` VALUES (b'1', 0, '2025-08-19 00:42:33.178146', 9, NULL, '体育运动类视频', NULL, '体育');
INSERT INTO `categories` VALUES (b'1', 0, '2025-08-19 00:42:33.190249', 10, NULL, '新闻资讯类视频', NULL, '新闻');
INSERT INTO `categories` VALUES (b'1', 0, '2025-08-26 19:19:57.624978', 11, NULL, '电影分类', NULL, '电影');
INSERT INTO `categories` VALUES (b'1', 0, '2025-08-26 19:19:57.654984', 12, NULL, '动画分类', NULL, '动画');
INSERT INTO `categories` VALUES (b'1', 0, '2025-08-26 19:19:57.661986', 13, NULL, '其他分类', NULL, '其他');

-- ----------------------------
-- Table structure for cdn_configs
-- ----------------------------
DROP TABLE IF EXISTS `cdn_configs`;
CREATE TABLE `cdn_configs`  (
  `created_at` datetime(6) NULL DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) NULL DEFAULT NULL,
  `access_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `bandwidth` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `bucket_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `cache_rules` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
  `config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
  `domain` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `domain_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `origin_server` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `provider` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `provider_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `region` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `secret_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `status` enum('ACTIVE','INACTIVE','MAINTENANCE') CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of cdn_configs
-- ----------------------------

-- ----------------------------
-- Table structure for comment_replies
-- ----------------------------
DROP TABLE IF EXISTS `comment_replies`;
CREATE TABLE `comment_replies`  (
  `is_deleted` bit(1) NOT NULL,
  `like_count` int NOT NULL,
  `comment_id` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `reply_to_user_id` bigint NULL DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `user_id` bigint NOT NULL,
  `ip_address` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKrrv53wwt4b7plhna65ixeiiio`(`comment_id` ASC) USING BTREE,
  INDEX `FK5o3v89ulis6mtt1drp7c6sy8e`(`reply_to_user_id` ASC) USING BTREE,
  INDEX `FKkbiso9jse4b6sssi7wjy9l7y7`(`user_id` ASC) USING BTREE,
  CONSTRAINT `FK5o3v89ulis6mtt1drp7c6sy8e` FOREIGN KEY (`reply_to_user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKkbiso9jse4b6sssi7wjy9l7y7` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKrrv53wwt4b7plhna65ixeiiio` FOREIGN KEY (`comment_id`) REFERENCES `comments` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of comment_replies
-- ----------------------------

-- ----------------------------
-- Table structure for comments
-- ----------------------------
DROP TABLE IF EXISTS `comments`;
CREATE TABLE `comments`  (
  `like_count` int NULL DEFAULT NULL,
  `created_at` datetime(6) NULL DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parent_id` bigint NULL DEFAULT NULL,
  `updated_at` datetime(6) NULL DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `video_id` bigint NOT NULL,
  `banned_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `status` enum('APPROVED','BANNED','PENDING','REJECTED') CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK8omq0tc18jd43bu5tjh6jvraq`(`user_id` ASC) USING BTREE,
  INDEX `FKesqgvcfwlscgco0dqkdnvw8l3`(`video_id` ASC) USING BTREE,
  CONSTRAINT `FK8omq0tc18jd43bu5tjh6jvraq` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKesqgvcfwlscgco0dqkdnvw8l3` FOREIGN KEY (`video_id`) REFERENCES `videos` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of comments
-- ----------------------------

-- ----------------------------
-- Table structure for conversation_participants
-- ----------------------------
DROP TABLE IF EXISTS `conversation_participants`;
CREATE TABLE `conversation_participants`  (
  `is_active` bit(1) NULL DEFAULT NULL,
  `conversation_id` bigint NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `joined_at` datetime(6) NOT NULL,
  `last_read_at` datetime(6) NULL DEFAULT NULL,
  `last_read_message_id` bigint NULL DEFAULT NULL,
  `left_at` datetime(6) NULL DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `role` enum('ADMIN','MEMBER','OWNER') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK84npv3fo2vwl7ut63im0p417q`(`conversation_id` ASC) USING BTREE,
  INDEX `FKjukjgq6uinvvk4307y8u9lixu`(`user_id` ASC) USING BTREE,
  CONSTRAINT `FK84npv3fo2vwl7ut63im0p417q` FOREIGN KEY (`conversation_id`) REFERENCES `conversations` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKjukjgq6uinvvk4307y8u9lixu` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of conversation_participants
-- ----------------------------
INSERT INTO `conversation_participants` VALUES (b'1', 3, 5, '2025-08-21 04:03:34.912981', NULL, NULL, NULL, 2, 'MEMBER');
INSERT INTO `conversation_participants` VALUES (b'1', 3, 6, '2025-08-21 04:03:34.912981', NULL, NULL, NULL, 1, 'MEMBER');
INSERT INTO `conversation_participants` VALUES (b'1', 5, 9, '2025-08-28 18:58:49.594615', NULL, NULL, NULL, 2, 'MEMBER');
INSERT INTO `conversation_participants` VALUES (b'1', 6, 11, '2025-08-29 09:49:31.772722', NULL, NULL, NULL, 28, 'MEMBER');
INSERT INTO `conversation_participants` VALUES (b'1', 6, 12, '2025-08-29 09:49:31.772722', NULL, NULL, NULL, 2, 'MEMBER');
INSERT INTO `conversation_participants` VALUES (b'1', 7, 13, '2025-08-29 20:06:54.054783', NULL, NULL, NULL, 28, 'MEMBER');
INSERT INTO `conversation_participants` VALUES (b'1', 7, 14, '2025-08-29 20:06:54.054783', NULL, NULL, NULL, 1, 'MEMBER');

-- ----------------------------
-- Table structure for conversations
-- ----------------------------
DROP TABLE IF EXISTS `conversations`;
CREATE TABLE `conversations`  (
  `is_active` bit(1) NULL DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `created_by` bigint NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `last_message_at` datetime(6) NULL DEFAULT NULL,
  `updated_at` datetime(6) NULL DEFAULT NULL,
  `avatar_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `type` enum('GROUP','PRIVATE') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK5uxcbsjes7nd38wm1qtsfaw28`(`created_by` ASC) USING BTREE,
  CONSTRAINT `FK5uxcbsjes7nd38wm1qtsfaw28` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of conversations
-- ----------------------------
INSERT INTO `conversations` VALUES (b'1', '2025-08-21 04:03:34.908995', 2, 3, '2025-08-21 19:31:02.666572', '2025-08-21 19:31:02.667768', NULL, NULL, NULL, 'PRIVATE');
INSERT INTO `conversations` VALUES (b'1', '2025-08-28 18:58:49.582631', 2, 5, '2025-08-28 20:04:07.626977', '2025-08-28 20:04:07.631983', NULL, NULL, NULL, 'PRIVATE');
INSERT INTO `conversations` VALUES (b'1', '2025-08-29 09:49:31.772230', 28, 6, '2025-08-29 20:26:17.627194', '2025-08-29 20:26:17.627194', NULL, NULL, NULL, 'PRIVATE');
INSERT INTO `conversations` VALUES (b'1', '2025-08-29 20:06:54.042286', 28, 7, '2025-08-29 20:06:54.042286', '2025-08-29 20:06:54.042286', NULL, NULL, NULL, 'PRIVATE');

-- ----------------------------
-- Table structure for daily_checkins
-- ----------------------------
DROP TABLE IF EXISTS `daily_checkins`;
CREATE TABLE `daily_checkins`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `checkin_date` date NOT NULL,
  `exp_reward` int NOT NULL DEFAULT 10,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `checkinDate` date NOT NULL,
  `consecutiveDays` int NOT NULL,
  `createdAt` datetime(6) NOT NULL,
  `expGained` int NOT NULL,
  `userId` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_date`(`user_id` ASC, `checkin_date` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of daily_checkins
-- ----------------------------

-- ----------------------------
-- Table structure for daily_tasks
-- ----------------------------
DROP TABLE IF EXISTS `daily_tasks`;
CREATE TABLE `daily_tasks`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `target` int NOT NULL,
  `exp_reward` int NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  `expReward` int NOT NULL,
  `createdAt` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updatedAt` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of daily_tasks
-- ----------------------------
INSERT INTO `daily_tasks` VALUES (1, '上传视频', '今日上传1个视频', 'video_upload', 1, 50, 1, '2025-08-29 11:22:05.000000', '2025-08-29 11:22:05.000000', 0, '2025-08-29 11:56:51.298900', '2025-08-29 11:56:51.330285');
INSERT INTO `daily_tasks` VALUES (2, '观看视频', '观看3个视频', 'video_watch', 3, 20, 1, '2025-08-29 11:22:05.000000', '2025-08-29 11:22:05.000000', 0, '2025-08-29 11:56:51.298900', '2025-08-29 11:56:51.330285');
INSERT INTO `daily_tasks` VALUES (3, '发表评论', '发表5条评论', 'comment', 5, 30, 1, '2025-08-29 11:22:05.000000', '2025-08-29 11:22:05.000000', 0, '2025-08-29 11:56:51.298900', '2025-08-29 11:56:51.330285');
INSERT INTO `daily_tasks` VALUES (4, '点赞互动', '点赞10个视频', 'like', 10, 25, 1, '2025-08-29 11:22:05.000000', '2025-08-29 11:22:05.000000', 0, '2025-08-29 11:56:51.298900', '2025-08-29 11:56:51.330285');
INSERT INTO `daily_tasks` VALUES (5, '分享内容', '分享2个视频', 'share', 2, 35, 1, '2025-08-29 11:22:05.000000', '2025-08-29 11:22:05.000000', 0, '2025-08-29 11:56:51.298900', '2025-08-29 11:56:51.330285');

-- ----------------------------
-- Table structure for daily_tasks_backup
-- ----------------------------
DROP TABLE IF EXISTS `daily_tasks_backup`;
CREATE TABLE `daily_tasks_backup`  (
  `active` bit(1) NOT NULL,
  `exp_reward` int NOT NULL,
  `target` int NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL DEFAULT 0,
  `updated_at` datetime(6) NOT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `expReward` int NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of daily_tasks_backup
-- ----------------------------
INSERT INTO `daily_tasks_backup` VALUES (b'1', 50, 1, '2025-08-19 00:42:33.395321', 1, '2025-08-19 00:42:33.395321', '今日上传1个视频', '上传视频', 'video_upload', 0);
INSERT INTO `daily_tasks_backup` VALUES (b'1', 20, 3, '2025-08-19 00:42:33.395321', 2, '2025-08-19 00:42:33.395321', '观看3个视频', '观看视频', 'video_watch', 0);
INSERT INTO `daily_tasks_backup` VALUES (b'1', 30, 5, '2025-08-19 00:42:33.395321', 3, '2025-08-19 00:42:33.395321', '发表5条评论', '发表评论', 'comment', 0);
INSERT INTO `daily_tasks_backup` VALUES (b'1', 25, 10, '2025-08-19 00:42:33.395321', 4, '2025-08-19 00:42:33.395321', '点赞10个视频', '点赞互动', 'like', 0);
INSERT INTO `daily_tasks_backup` VALUES (b'1', 35, 2, '2025-08-19 00:42:33.395321', 5, '2025-08-19 00:42:33.395321', '分享2个视频', '分享内容', 'share', 0);

-- ----------------------------
-- Table structure for file_uploads
-- ----------------------------
DROP TABLE IF EXISTS `file_uploads`;
CREATE TABLE `file_uploads`  (
  `duration` int NULL DEFAULT NULL,
  `height` int NULL DEFAULT NULL,
  `is_deleted` bit(1) NULL DEFAULT NULL,
  `width` int NULL DEFAULT NULL,
  `access_count` bigint NULL DEFAULT NULL,
  `file_size` bigint NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `last_accessed` datetime(6) NULL DEFAULT NULL,
  `upload_time` datetime(6) NOT NULL,
  `uploaded_by` bigint NOT NULL,
  `file_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `file_type` enum('AUDIO','DOCUMENT','IMAGE','OTHER','VIDEO') CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `file_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `mime_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `original_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `stored_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `thumbnail_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK_lvle2a7jndr65ihb0md8affo7`(`stored_name` ASC) USING BTREE,
  INDEX `FK4pr7aw65sp0u9aji5kjshi7j0`(`uploaded_by` ASC) USING BTREE,
  CONSTRAINT `FK4pr7aw65sp0u9aji5kjshi7j0` FOREIGN KEY (`uploaded_by`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of file_uploads
-- ----------------------------

-- ----------------------------
-- Table structure for message_attachments
-- ----------------------------
DROP TABLE IF EXISTS `message_attachments`;
CREATE TABLE `message_attachments`  (
  `duration` int NULL DEFAULT NULL,
  `height` int NULL DEFAULT NULL,
  `width` int NULL DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `file_size` bigint NULL DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `message_id` bigint NOT NULL,
  `attachment_type` enum('FILE','IMAGE','THUMBNAIL','VIDEO','VOICE') CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `extra_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `file_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `mime_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `thumbnail_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKj7twd218e2gqw9cmlhwvo1rth`(`message_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of message_attachments
-- ----------------------------
INSERT INTO `message_attachments` VALUES (NULL, 1000, 1000, '2025-08-17 18:31:29.552937', 195694, 1, 1, 'IMAGE', NULL, '20220610091426_7afde.jpg', '/uploads/chat/image/2025/08/17/790dab55-49e0-4a8c-aa44-9d4365c6806c.jpg', 'image/jpeg', '/uploads/chat/image/2025/08/17/790dab55-49e0-4a8c-aa44-9d4365c6806c_thumb.jpg');

-- ----------------------------
-- Table structure for messages
-- ----------------------------
DROP TABLE IF EXISTS `messages`;
CREATE TABLE `messages`  (
  `duration` int NULL DEFAULT NULL,
  `is_deleted` bit(1) NOT NULL,
  `is_recalled` bit(1) NOT NULL,
  `conversation_id` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) NULL DEFAULT NULL,
  `file_size` bigint NULL DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `recalled_at` datetime(6) NULL DEFAULT NULL,
  `reply_to_message_id` bigint NULL DEFAULT NULL,
  `sender_id` bigint NOT NULL,
  `updated_at` datetime(6) NULL DEFAULT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `extra_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `file_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `message_type` enum('EMOJI','FILE','IMAGE','LOCATION','SYSTEM','TEXT','VIDEO','VOICE') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `mime_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `status` enum('DELIVERED','FAILED','READ','SENDING','SENT') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `thumbnail_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `mentioned_users` json NULL,
  `type` enum('EMOJI','FILE','IMAGE','SYSTEM','TEXT','VIDEO','VOICE') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `reply_to_id` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKa0efscl1qaot4lml4w4gpydo2`(`reply_to_message_id` ASC) USING BTREE,
  INDEX `FKg23x99if9xk265onv7btb0cg9`(`reply_to_id` ASC) USING BTREE,
  INDEX `FKt492th6wsovh1nush5yl5jj8e`(`conversation_id` ASC) USING BTREE,
  INDEX `FK4ui4nnwntodh6wjvck53dbk9m`(`sender_id` ASC) USING BTREE,
  CONSTRAINT `FK4ui4nnwntodh6wjvck53dbk9m` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKa0efscl1qaot4lml4w4gpydo2` FOREIGN KEY (`reply_to_message_id`) REFERENCES `messages` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKg23x99if9xk265onv7btb0cg9` FOREIGN KEY (`reply_to_id`) REFERENCES `messages` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKt492th6wsovh1nush5yl5jj8e` FOREIGN KEY (`conversation_id`) REFERENCES `conversations` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 64 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of messages
-- ----------------------------
INSERT INTO `messages` VALUES (NULL, b'1', b'0', 5, '2025-08-28 20:04:07.610267', '2025-08-28 23:02:46.913507', 1275992, 60, NULL, NULL, 2, '2025-08-28 23:02:46.914505', '', NULL, '20220603234750_00ddb.png', '/uploads/chat/images/a4f96113-7715-4a48-8d04-8ba2fed4962e.png', 'EMOJI', NULL, 'DELIVERED', NULL, NULL, 'IMAGE', NULL);
INSERT INTO `messages` VALUES (NULL, b'0', b'0', 6, '2025-08-29 20:07:02.766300', NULL, NULL, 61, NULL, NULL, 28, '2025-08-29 20:07:02.766300', '😎', NULL, NULL, NULL, 'EMOJI', NULL, 'DELIVERED', NULL, NULL, 'TEXT', NULL);
INSERT INTO `messages` VALUES (NULL, b'1', b'0', 6, '2025-08-29 20:07:09.665505', '2025-08-29 20:26:11.863398', 195694, 62, NULL, NULL, 28, '2025-08-29 20:26:11.866031', '', NULL, '20220610091426_7afde.jpg', '/uploads/chat/images/7ea26ad5-d095-4d84-9a8c-d4a75ea67cbd.jpg', 'EMOJI', NULL, 'DELIVERED', NULL, NULL, 'IMAGE', NULL);
INSERT INTO `messages` VALUES (NULL, b'0', b'0', 6, '2025-08-29 20:26:17.624683', NULL, 126235, 63, NULL, NULL, 28, '2025-08-29 20:26:17.627194', '', NULL, '20220523133345_ba5e0.jpeg', '/uploads/chat/images/09951675-3b5c-4fdc-8169-3520d1c043e9.jpeg', 'EMOJI', NULL, 'DELIVERED', NULL, NULL, 'IMAGE', NULL);

-- ----------------------------
-- Table structure for moment_comment
-- ----------------------------
DROP TABLE IF EXISTS `moment_comment`;
CREATE TABLE `moment_comment`  (
  `create_time` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `like_count` bigint NULL DEFAULT NULL,
  `moment_id` bigint NOT NULL,
  `reply_to_comment_id` bigint NULL DEFAULT NULL,
  `reply_to_user_id` bigint NULL DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `likeCount` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of moment_comment
-- ----------------------------
INSERT INTO `moment_comment` VALUES ('2025-08-29 19:24:07.380201', 1, NULL, 4, NULL, NULL, 28, 'aa', 0);

-- ----------------------------
-- Table structure for moment_likes
-- ----------------------------
DROP TABLE IF EXISTS `moment_likes`;
CREATE TABLE `moment_likes`  (
  `create_time` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `moment_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UKbc22nr9bgfuvlymb12cry8rxs`(`moment_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `FKcou8c1g1ck9s54y41r1484n5l`(`user_id` ASC) USING BTREE,
  CONSTRAINT `FKcou8c1g1ck9s54y41r1484n5l` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of moment_likes
-- ----------------------------
INSERT INTO `moment_likes` VALUES ('2025-08-21 03:35:32.733133', 1, 1, 2);
INSERT INTO `moment_likes` VALUES ('2025-08-21 19:29:41.506965', 2, 2, 2);
INSERT INTO `moment_likes` VALUES ('2025-08-28 10:44:24.518195', 3, 3, 2);
INSERT INTO `moment_likes` VALUES ('2025-08-29 19:21:12.134327', 6, 4, 28);

-- ----------------------------
-- Table structure for music
-- ----------------------------
DROP TABLE IF EXISTS `music`;
CREATE TABLE `music`  (
  `duration` int NULL DEFAULT NULL,
  `is_public` bit(1) NULL DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `play_count` bigint NULL DEFAULT NULL,
  `upload_time` datetime(6) NOT NULL,
  `user_id` bigint NOT NULL,
  `album` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `artist` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `cover_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `file_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `lyrics` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
  `lyrics_timestamp` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of music
-- ----------------------------
INSERT INTO `music` VALUES (NULL, b'1', 12, 4, '2025-08-29 21:38:24.120346', 28, '', '呦猫UNEKO - 梦回还 (TV size)', '/uploads/covers/847a5434-e958-4735-a175-a6c2bc46d80f_84cf0d85e33e4878b670a8a647c0c05e.jpeg', 'de863be4-31a1-455d-bb33-b603add5c1db_呦猫UNEKO - 梦回还 (TV size).mp3', '/uploads/music/de863be4-31a1-455d-bb33-b603add5c1db_呦猫UNEKO - 梦回还 (TV size).mp3', NULL, NULL, '呦猫UNEKO - 梦回还 (TV size)');

-- ----------------------------
-- Table structure for notifications
-- ----------------------------
DROP TABLE IF EXISTS `notifications`;
CREATE TABLE `notifications`  (
  `is_read` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `from_user_id` bigint NULL DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `read_at` datetime(6) NULL DEFAULT NULL,
  `related_id` bigint NULL DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `type` enum('ACHIEVEMENT','ANNOUNCEMENT','COMMENT','FAVORITE','FOLLOW','FRIEND_ACCEPTED','FRIEND_REQUEST','LIKE','MENTION','MESSAGE','REPLY','SYSTEM','VIDEO_APPROVED','VIDEO_REJECTED') CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
  `related_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKpalb3w8yony75cf2odwxks4ns`(`from_user_id` ASC) USING BTREE,
  INDEX `FK9y21adhxn0ayjhfocscqox7bh`(`user_id` ASC) USING BTREE,
  CONSTRAINT `FK9y21adhxn0ayjhfocscqox7bh` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `FKpalb3w8yony75cf2odwxks4ns` FOREIGN KEY (`from_user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 41 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of notifications
-- ----------------------------
INSERT INTO `notifications` VALUES (b'0', '2025-08-28 18:57:15.223493', 2, 3, NULL, 2, 1, 'FOLLOW', 'root 关注了您', 'USER', '新关注者');
INSERT INTO `notifications` VALUES (b'0', '2025-08-28 18:57:15.223493', 2, 4, NULL, 2, 1, 'FOLLOW', 'root 关注了您', 'USER', '新关注者');
INSERT INTO `notifications` VALUES (b'0', '2025-08-29 09:49:37.409305', 28, 38, NULL, 28, 2, 'FOLLOW', 'testuser 关注了您', 'USER', '新关注者');
INSERT INTO `notifications` VALUES (b'1', '2025-08-29 10:03:59.596585', 2, 39, '2025-08-29 14:52:33.274154', 2, 28, 'FOLLOW', 'root 关注了您', 'USER', '新关注者');
INSERT INTO `notifications` VALUES (b'1', '2025-08-29 10:04:03.020032', 2, 40, '2025-08-29 14:52:33.274154', 2, 28, 'FOLLOW', 'root 关注了您', 'USER', '新关注者');

-- ----------------------------
-- Table structure for private_album
-- ----------------------------
DROP TABLE IF EXISTS `private_album`;
CREATE TABLE `private_album`  (
  `created_time` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `updated_time` datetime(6) NULL DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `album_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of private_album
-- ----------------------------
INSERT INTO `private_album` VALUES ('2025-08-29 19:36:33.051173', 1, '2025-08-29 19:36:40.203788', 28, '$2a$10$9Kv4R5RW.HtYhkVeEyi70OmZ8FxkDB5HIqIkWwphjrFHcjZgISDAm', '学习资料', '');

-- ----------------------------
-- Table structure for private_photo
-- ----------------------------
DROP TABLE IF EXISTS `private_photo`;
CREATE TABLE `private_photo`  (
  `height` int NULL DEFAULT NULL,
  `width` int NULL DEFAULT NULL,
  `album_id` bigint NOT NULL,
  `file_size` bigint NULL DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `upload_time` datetime(6) NOT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `file_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `thumbnail_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of private_photo
-- ----------------------------
INSERT INTO `private_photo` VALUES (400, 400, 1, 49961, 1, '2025-08-29 19:36:40.203788', '', '765824c0-63f6-4ec3-9965-878771f94742_20200624130802_TiRQ8.jpeg', 'uploads/private/765824c0-63f6-4ec3-9965-878771f94742_20200624130802_TiRQ8.jpeg', 'uploads/private/thumbnails/thumb_765824c0-63f6-4ec3-9965-878771f94742_20200624130802_TiRQ8.jpeg');

-- ----------------------------
-- Table structure for search_history
-- ----------------------------
DROP TABLE IF EXISTS `search_history`;
CREATE TABLE `search_history`  (
  `result_count` int NOT NULL,
  `search_count` int NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) NOT NULL,
  `user_id` bigint NULL DEFAULT NULL,
  `ip_address` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `search_keyword` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK8ll2cxj6i83mnrcyxrxl4b7dm`(`user_id` ASC) USING BTREE,
  CONSTRAINT `FK8ll2cxj6i83mnrcyxrxl4b7dm` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of search_history
-- ----------------------------

-- ----------------------------
-- Table structure for tags
-- ----------------------------
DROP TABLE IF EXISTS `tags`;
CREATE TABLE `tags`  (
  `is_active` bit(1) NULL DEFAULT NULL,
  `color` varchar(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `created_by` bigint NULL DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) NULL DEFAULT NULL,
  `video_count` bigint NULL DEFAULT NULL,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK_t48xdq560gs3gap9g7jg36kgc`(`name` ASC) USING BTREE,
  INDEX `FK3sh3rn8hrvjb08s6vu09bldt7`(`created_by` ASC) USING BTREE,
  CONSTRAINT `FK3sh3rn8hrvjb08s6vu09bldt7` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tags
-- ----------------------------
INSERT INTO `tags` VALUES (b'1', '#007bff', '2025-08-19 00:42:33.201682', NULL, 1, NULL, 0, '热门', '热门内容标签');
INSERT INTO `tags` VALUES (b'1', '#007bff', '2025-08-19 00:42:33.228949', NULL, 2, NULL, 0, '推荐', '推荐内容标签');
INSERT INTO `tags` VALUES (b'1', '#007bff', '2025-08-19 00:42:33.228949', NULL, 3, NULL, 0, '原创', '原创内容标签');
INSERT INTO `tags` VALUES (b'1', '#007bff', '2025-08-19 00:42:33.241649', NULL, 4, NULL, 0, '搞笑', '搞笑内容标签');
INSERT INTO `tags` VALUES (b'1', '#007bff', '2025-08-19 00:42:33.245634', NULL, 5, NULL, 0, '教程', '教程类内容标签');
INSERT INTO `tags` VALUES (b'1', '#007bff', '2025-08-19 00:42:33.255935', NULL, 6, NULL, 0, '评测', '产品评测标签');
INSERT INTO `tags` VALUES (b'1', '#007bff', '2025-08-19 00:42:33.261951', NULL, 7, NULL, 0, 'Vlog', '生活记录标签');
INSERT INTO `tags` VALUES (b'1', '#007bff', '2025-08-19 00:42:33.270309', NULL, 8, NULL, 0, '音乐', '音乐相关标签');
INSERT INTO `tags` VALUES (b'1', '#007bff', '2025-08-19 00:42:33.278909', NULL, 9, NULL, 0, '舞蹈', '舞蹈相关标签');
INSERT INTO `tags` VALUES (b'1', '#007bff', '2025-08-19 00:42:33.278909', NULL, 10, NULL, 0, '美食', '美食相关标签');

-- ----------------------------
-- Table structure for user_achievements
-- ----------------------------
DROP TABLE IF EXISTS `user_achievements`;
CREATE TABLE `user_achievements`  (
  `is_displayed` bit(1) NOT NULL,
  `notification_sent` bit(1) NOT NULL,
  `progress` double NOT NULL,
  `achievement_id` bigint NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `unlocked_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `user_id` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UKehxedycprv0kd4wts369ikmw1`(`user_id` ASC, `achievement_id` ASC) USING BTREE,
  INDEX `FK8ipvec6cs8t3g8515thtlsxuf`(`achievement_id` ASC) USING BTREE,
  CONSTRAINT `FK6vt5fpu0uta41vny1x6vpk45k` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FK8ipvec6cs8t3g8515thtlsxuf` FOREIGN KEY (`achievement_id`) REFERENCES `achievements` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_achievements
-- ----------------------------
INSERT INTO `user_achievements` VALUES (b'1', b'0', 1, 1, 1, '2025-08-20 12:08:42.514179', 2, '2025-08-29 11:22:05.504284');
INSERT INTO `user_achievements` VALUES (b'1', b'0', 1, 26, 2, '2025-08-20 12:08:42.528047', 2, '2025-08-29 11:22:05.504284');

-- ----------------------------
-- Table structure for user_activities
-- ----------------------------
DROP TABLE IF EXISTS `user_activities`;
CREATE TABLE `user_activities`  (
  `is_public` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `target_user_id` bigint NULL DEFAULT NULL,
  `target_video_id` bigint NULL DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `metadata` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `activity_type` enum('ACHIEVEMENT','COMMENT_VIDEO','FAVORITE_VIDEO','FOLLOW_USER','LEVEL_UP','LIKE_VIDEO','SHARE_VIDEO','UPLOAD_VIDEO') CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKd2dibhhmj5cvl9dmfl52fj5sp`(`target_user_id` ASC) USING BTREE,
  INDEX `FK4fki5bdee7oamgjbdniswo3pm`(`target_video_id` ASC) USING BTREE,
  INDEX `FKbe7yq8t74yxeoarmxlxevoped`(`user_id` ASC) USING BTREE,
  CONSTRAINT `FK4fki5bdee7oamgjbdniswo3pm` FOREIGN KEY (`target_video_id`) REFERENCES `videos` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKbe7yq8t74yxeoarmxlxevoped` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKd2dibhhmj5cvl9dmfl52fj5sp` FOREIGN KEY (`target_user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_activities
-- ----------------------------

-- ----------------------------
-- Table structure for user_follows
-- ----------------------------
DROP TABLE IF EXISTS `user_follows`;
CREATE TABLE `user_follows`  (
  `created_at` datetime(6) NULL DEFAULT NULL,
  `follower_id` bigint NULL DEFAULT NULL,
  `following_id` bigint NULL DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKqx9mu1fniaua5jfe1cdyspxdt`(`follower_id` ASC) USING BTREE,
  INDEX `FKp1rxuw1ulwo6mu84qaajuttrk`(`following_id` ASC) USING BTREE,
  CONSTRAINT `FKp1rxuw1ulwo6mu84qaajuttrk` FOREIGN KEY (`following_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKqx9mu1fniaua5jfe1cdyspxdt` FOREIGN KEY (`follower_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 41 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_follows
-- ----------------------------
INSERT INTO `user_follows` VALUES ('2025-08-28 18:57:15.203396', 2, 1, 3);
INSERT INTO `user_follows` VALUES ('2025-08-28 18:57:15.219485', 2, 1, 4);
INSERT INTO `user_follows` VALUES ('2025-08-29 09:49:37.409305', 28, 2, 38);

-- ----------------------------
-- Table structure for user_levels
-- ----------------------------
DROP TABLE IF EXISTS `user_levels`;
CREATE TABLE `user_levels`  (
  `consecutive_days` int NOT NULL,
  `level` int NOT NULL,
  `total_comments_made` int NOT NULL,
  `total_likes_received` int NOT NULL,
  `total_videos_uploaded` int NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `experience_points` bigint NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `last_exp_gain` datetime(6) NOT NULL,
  `next_level_exp` bigint NOT NULL,
  `total_watch_time` bigint NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK_cj45mt7np3roqgpb806vms21r`(`user_id` ASC) USING BTREE,
  CONSTRAINT `FKr5aqf5bnqm78uhohv89f203v2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_levels
-- ----------------------------
INSERT INTO `user_levels` VALUES (4, 2, 0, 0, 0, '2025-08-19 00:42:55.972969', 116, 1, '2025-08-29 10:28:43.018566', 282, 0, '2025-08-29 10:28:43.036404', 2);
INSERT INTO `user_levels` VALUES (0, 1, 0, 0, 0, '2025-08-19 01:03:21.024357', 0, 2, '2025-08-19 01:03:21.024357', 100, 0, '2025-08-29 21:54:22.184904', 1);
INSERT INTO `user_levels` VALUES (0, 1, 0, 1, 2, '2025-08-29 09:49:13.464257', 0, 6, '2025-08-29 09:49:13.464257', 100, 0, '2025-08-29 21:40:37.155474', 28);

-- ----------------------------
-- Table structure for user_login_logs
-- ----------------------------
DROP TABLE IF EXISTS `user_login_logs`;
CREATE TABLE `user_login_logs`  (
  `latitude` double NULL DEFAULT NULL,
  `login_success` tinyint(1) NULL DEFAULT 1,
  `longitude` double NULL DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `login_time` datetime(6) NULL DEFAULT NULL,
  `user_id` bigint NULL DEFAULT NULL,
  `login_ip` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `login_location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `user_agent` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 32 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_login_logs
-- ----------------------------
INSERT INTO `user_login_logs` VALUES (0, 1, 0, 1, '2025-08-19 01:03:20.992254', 1, '0:0:0:0:0:0:0:1', 'superadmin', '未知', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0');
INSERT INTO `user_login_logs` VALUES (0, 1, 0, 2, '2025-08-19 01:04:50.306457', 1, '0:0:0:0:0:0:0:1', 'superadmin', '未知', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0');
INSERT INTO `user_login_logs` VALUES (0, 1, 0, 3, '2025-08-22 18:19:36.566360', 1, '0:0:0:0:0:0:0:1', 'superadmin', '未知', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0');
INSERT INTO `user_login_logs` VALUES (0, 1, 0, 4, '2025-08-26 14:26:05.755069', 1, '0:0:0:0:0:0:0:1', 'superadmin', '未知', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0');
INSERT INTO `user_login_logs` VALUES (0, 1, 0, 5, '2025-08-26 18:29:29.887219', 1, '0:0:0:0:0:0:0:1', 'superadmin', '未知', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0');
INSERT INTO `user_login_logs` VALUES (0, 1, 0, 6, '2025-08-26 19:26:57.643391', 1, '0:0:0:0:0:0:0:1', 'superadmin', '未知', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0');
INSERT INTO `user_login_logs` VALUES (0, 1, 0, 7, '2025-08-26 19:38:01.517219', 1, '0:0:0:0:0:0:0:1', 'superadmin', '未知', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0');
INSERT INTO `user_login_logs` VALUES (0, 1, 0, 8, '2025-08-26 20:41:19.833511', 1, '0:0:0:0:0:0:0:1', 'superadmin', '未知', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0');
INSERT INTO `user_login_logs` VALUES (0, 1, 0, 9, '2025-08-27 18:27:30.522908', 1, '0:0:0:0:0:0:0:1', 'superadmin', '未知', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0');
INSERT INTO `user_login_logs` VALUES (0, 1, 0, 10, '2025-08-27 22:33:31.702410', 1, '0:0:0:0:0:0:0:1', 'superadmin', '未知', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0');
INSERT INTO `user_login_logs` VALUES (0, 1, 0, 11, '2025-08-27 22:53:50.993288', 1, '0:0:0:0:0:0:0:1', 'superadmin', '未知', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0');
INSERT INTO `user_login_logs` VALUES (0, 1, 0, 12, '2025-08-27 23:11:52.232397', 1, '0:0:0:0:0:0:0:1', 'superadmin', '未知', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0');
INSERT INTO `user_login_logs` VALUES (0, 1, 0, 13, '2025-08-27 23:38:58.383861', 1, '0:0:0:0:0:0:0:1', 'superadmin', '未知', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0');
INSERT INTO `user_login_logs` VALUES (0, 1, 0, 14, '2025-08-28 09:57:38.986250', 1, '0:0:0:0:0:0:0:1', 'superadmin', '未知', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0');
INSERT INTO `user_login_logs` VALUES (0, 1, 0, 15, '2025-08-28 10:09:55.740641', 1, '0:0:0:0:0:0:0:1', 'superadmin', '未知', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0');
INSERT INTO `user_login_logs` VALUES (0, 1, 0, 16, '2025-08-28 11:11:39.894193', 1, '0:0:0:0:0:0:0:1', 'superadmin', '未知', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0');
INSERT INTO `user_login_logs` VALUES (0, 1, 0, 17, '2025-08-28 15:27:36.659028', 1, '0:0:0:0:0:0:0:1', 'superadmin', '未知', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0');
INSERT INTO `user_login_logs` VALUES (0, 1, 0, 18, '2025-08-28 15:37:22.803756', 1, '0:0:0:0:0:0:0:1', 'superadmin', '未知', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0');
INSERT INTO `user_login_logs` VALUES (0, 1, 0, 19, '2025-08-28 18:00:38.548831', 1, '0:0:0:0:0:0:0:1', 'superadmin', '未知', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0');
INSERT INTO `user_login_logs` VALUES (0, 1, 0, 20, '2025-08-28 18:08:33.829334', 1, '0:0:0:0:0:0:0:1', 'superadmin', '未知', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.22621.5624');
INSERT INTO `user_login_logs` VALUES (0, 1, 0, 21, '2025-08-28 18:25:15.837635', 1, '0:0:0:0:0:0:0:1', 'superadmin', '未知', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0');
INSERT INTO `user_login_logs` VALUES (0, 1, 0, 22, '2025-08-28 23:00:02.657150', 1, '0:0:0:0:0:0:0:1', 'superadmin', '未知', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0');
INSERT INTO `user_login_logs` VALUES (0, 1, 0, 23, '2025-08-28 23:24:02.154697', 1, '0:0:0:0:0:0:0:1', 'superadmin', '未知', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0');
INSERT INTO `user_login_logs` VALUES (0, 1, 0, 24, '2025-08-28 23:25:05.509469', 1, '0:0:0:0:0:0:0:1', 'superadmin', '未知', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0');
INSERT INTO `user_login_logs` VALUES (0, 1, 0, 25, '2025-08-29 09:05:16.903497', 1, '0:0:0:0:0:0:0:1', 'superadmin', '未知', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0');
INSERT INTO `user_login_logs` VALUES (0, 1, 0, 26, '2025-08-29 09:45:32.637103', 1, '0:0:0:0:0:0:0:1', 'superadmin', '未知', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0');
INSERT INTO `user_login_logs` VALUES (0, 1, 0, 27, '2025-08-29 09:46:23.766123', 1, '0:0:0:0:0:0:0:1', 'superadmin', '未知', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0');
INSERT INTO `user_login_logs` VALUES (0, 1, 0, 28, '2025-08-29 13:21:52.530189', 1, '0:0:0:0:0:0:0:1', 'superadmin', '未知', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0');
INSERT INTO `user_login_logs` VALUES (0, 1, 0, 29, '2025-08-29 13:31:42.117796', 1, '0:0:0:0:0:0:0:1', 'superadmin', '未知', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0');
INSERT INTO `user_login_logs` VALUES (0, 1, 0, 30, '2025-08-29 21:40:18.728436', 1, '0:0:0:0:0:0:0:1', 'superadmin', '未知', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0');
INSERT INTO `user_login_logs` VALUES (0, 1, 0, 31, '2025-08-29 21:54:21.886394', 1, '0:0:0:0:0:0:0:1', 'superadmin', '未知', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0');

-- ----------------------------
-- Table structure for user_logs
-- ----------------------------
DROP TABLE IF EXISTS `user_logs`;
CREATE TABLE `user_logs`  (
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NULL DEFAULT NULL,
  `action` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `details` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
  `ip_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `request_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `user_agent` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3131 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_logs
-- ----------------------------
INSERT INTO `user_logs` VALUES ('2025-08-29 09:46:50.333869', 2994, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 09:48:42.512593', 2995, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/auth/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 09:48:45.928487', 2996, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 09:48:45.936500', 2997, NULL, 'LOGIN_ERROR', 'Error occurred: Login failed - invalid credentials', 'Login failed - invalid credentials', '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'ERROR', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 09:49:13.617911', 2998, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 10:03:55.080512', 2999, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 10:28:07.284345', 3000, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 10:38:50.826968', 3001, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 12:46:08.215807', 3002, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 12:46:30.082520', 3003, NULL, 'DATABASE_ERROR', 'Error occurred: Database error for user root on /chat/private: Unable to commit against JDBC Connection', 'Unable to commit against JDBC Connection', '0:0:0:0:0:0:0:1', 'http://localhost:8081/chat/private', 'ERROR', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', 'root');
INSERT INTO `user_logs` VALUES ('2025-08-29 13:06:14.768763', 3004, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 13:06:14.794509', 3005, NULL, 'DATABASE_ERROR', 'Error occurred: Database error for user anonymousUser on /login: Unable to commit against JDBC Connection', 'Unable to commit against JDBC Connection', '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'ERROR', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 13:06:18.448185', 3006, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 13:06:18.455323', 3007, NULL, 'DATABASE_ERROR', 'Error occurred: Database error for user anonymousUser on /login: Unable to commit against JDBC Connection', 'Unable to commit against JDBC Connection', '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'ERROR', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 13:09:07.901540', 3008, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 13:09:07.928152', 3009, NULL, 'DATABASE_ERROR', 'Error occurred: Database error for user anonymousUser on /login: Unable to commit against JDBC Connection', 'Unable to commit against JDBC Connection', '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'ERROR', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 13:09:12.765440', 3010, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 13:09:12.771138', 3011, NULL, 'DATABASE_ERROR', 'Error occurred: Database error for user anonymousUser on /login: Unable to commit against JDBC Connection', 'Unable to commit against JDBC Connection', '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'ERROR', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 13:09:15.549601', 3012, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 13:09:15.556117', 3013, NULL, 'DATABASE_ERROR', 'Error occurred: Database error for user anonymousUser on /login: Unable to commit against JDBC Connection', 'Unable to commit against JDBC Connection', '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'ERROR', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 13:17:58.645383', 3014, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 13:19:09.782412', 3015, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 13:21:25.032544', 3016, NULL, 'VIDEO_UPLOAD_SUCCESS', 'Video uploaded successfully: ID=13, Title=郝宇星 - 其实都没有', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/upload', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', 'testuser');
INSERT INTO `user_logs` VALUES ('2025-08-29 13:33:18.553501', 3017, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 13:33:20.030156', 3018, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 13:33:20.033151', 3019, NULL, 'LOGIN_ERROR', 'Error occurred: Login failed - invalid credentials', 'Login failed - invalid credentials', '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'ERROR', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 13:35:36.009953', 3020, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 13:41:56.599725', 3021, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 13:50:08.968568', 3022, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 13:52:30.174984', 3023, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 13:55:05.417442', 3024, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 14:01:21.439719', 3025, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 14:22:23.207056', 3026, NULL, 'RUNTIME_ERROR', 'Error occurred: Runtime error for user anonymousUser on /video/13: No converter for [class java.util.LinkedHashMap] with preset Content-Type \'text/html;charset=UTF-8\'', 'No converter for [class java.util.LinkedHashMap] with preset Content-Type \'text/html;charset=UTF-8\'', '0:0:0:0:0:0:0:1', 'http://localhost:8081/video/13', 'ERROR', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.22621.5624', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 14:23:43.260402', 3027, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 14:24:18.244346', 3028, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 14:28:50.271195', 3029, NULL, 'RUNTIME_ERROR', 'Error occurred: Runtime error for user anonymousUser on /video/13/edit: Cannot invoke \"org.springframework.security.core.Authentication.getName()\" because \"authentication\" is null', 'Cannot invoke \"org.springframework.security.core.Authentication.getName()\" because \"authentication\" is null', '0:0:0:0:0:0:0:1', 'http://localhost:8081/video/13/edit', 'ERROR', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.22621.5624', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 14:32:57.068909', 3030, NULL, 'RUNTIME_ERROR', 'Error occurred: Runtime error for user anonymousUser on /video/13/edit: Cannot invoke \"org.springframework.security.core.Authentication.getName()\" because \"authentication\" is null', 'Cannot invoke \"org.springframework.security.core.Authentication.getName()\" because \"authentication\" is null', '0:0:0:0:0:0:0:1', 'http://localhost:8081/video/13/edit', 'ERROR', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 14:35:49.403606', 3031, NULL, 'RUNTIME_ERROR', 'Error occurred: Runtime error for user anonymousUser on /video/13/edit: Cannot invoke \"org.springframework.security.core.Authentication.getName()\" because \"authentication\" is null', 'Cannot invoke \"org.springframework.security.core.Authentication.getName()\" because \"authentication\" is null', '0:0:0:0:0:0:0:1', 'http://localhost:8081/video/13/edit', 'ERROR', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.22621.5624', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 14:37:45.245852', 3032, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 14:50:17.222323', 3033, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 14:55:16.165680', 3034, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 15:00:02.354177', 3035, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 15:09:38.286152', 3036, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 15:20:04.071230', 3037, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 15:43:09.203229', 3038, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 15:53:28.556369', 3039, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 15:58:46.575782', 3040, NULL, 'DATABASE_ERROR', 'Error occurred: Database error for user testuser on /posts/publish: could not execute statement [Field \'create_time\' doesn\'t have a default value] [insert into user_moment (commentCount,content,createTime,images,isPublic,likeCount,location,mood,userId,viewCount) values (?,?,?,?,?,?,?,?,?,?)]', 'could not execute statement [Field \'create_time\' doesn\'t have a default value] [insert into user_moment (commentCount,content,createTime,images,isPublic,likeCount,location,mood,userId,viewCount) values (?,?,?,?,?,?,?,?,?,?)]', '0:0:0:0:0:0:0:1', 'http://localhost:8081/posts/publish', 'ERROR', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', 'testuser');
INSERT INTO `user_logs` VALUES ('2025-08-29 16:00:20.920795', 3041, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 16:02:44.796524', 3042, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 16:03:24.960736', 3043, NULL, 'DATABASE_ERROR', 'Error occurred: Database error for user testuser on /posts/publish: could not execute statement [Field \'user_id\' doesn\'t have a default value] [insert into user_moment (comment_count,content,create_time,images,is_public,like_count,location,mood,userId,view_count) values (?,?,?,?,?,?,?,?,?,?)]', 'could not execute statement [Field \'user_id\' doesn\'t have a default value] [insert into user_moment (comment_count,content,create_time,images,is_public,like_count,location,mood,userId,view_count) values (?,?,?,?,?,?,?,?,?,?)]', '0:0:0:0:0:0:0:1', 'http://localhost:8081/posts/publish', 'ERROR', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', 'testuser');
INSERT INTO `user_logs` VALUES ('2025-08-29 16:06:50.901357', 3044, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 16:07:06.082552', 3045, NULL, 'DATABASE_ERROR', 'Error occurred: Database error for user testuser on /posts/publish: could not execute statement [Field \'user_id\' doesn\'t have a default value] [insert into user_moment (comment_count,content,create_time,images,is_public,like_count,location,mood,userId,view_count) values (?,?,?,?,?,?,?,?,?,?)]', 'could not execute statement [Field \'user_id\' doesn\'t have a default value] [insert into user_moment (comment_count,content,create_time,images,is_public,like_count,location,mood,userId,view_count) values (?,?,?,?,?,?,?,?,?,?)]', '0:0:0:0:0:0:0:1', 'http://localhost:8081/posts/publish', 'ERROR', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', 'testuser');
INSERT INTO `user_logs` VALUES ('2025-08-29 16:17:02.802035', 3046, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 16:17:49.911662', 3047, NULL, 'DATABASE_ERROR', 'Error occurred: Database error for user testuser on /music/upload: could not execute statement [Field \'upload_time\' doesn\'t have a default value] [insert into music (album,artist,coverPath,duration,fileName,filePath,isPublic,lyrics,lyricsTimestamp,playCount,title,uploadTime,user_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?)]', 'could not execute statement [Field \'upload_time\' doesn\'t have a default value] [insert into music (album,artist,coverPath,duration,fileName,filePath,isPublic,lyrics,lyricsTimestamp,playCount,title,uploadTime,user_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?)]', '0:0:0:0:0:0:0:1', 'http://localhost:8081/music/upload', 'ERROR', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', 'testuser');
INSERT INTO `user_logs` VALUES ('2025-08-29 16:19:05.219441', 3048, NULL, 'DATABASE_ERROR', 'Error occurred: Database error for user testuser on /posts/publish: could not execute statement [Field \'createTime\' doesn\'t have a default value] [insert into user_moment (comment_count,content,create_time,images,is_public,like_count,location,mood,user_id,view_count) values (?,?,?,?,?,?,?,?,?,?)]', 'could not execute statement [Field \'createTime\' doesn\'t have a default value] [insert into user_moment (comment_count,content,create_time,images,is_public,like_count,location,mood,user_id,view_count) values (?,?,?,?,?,?,?,?,?,?)]', '0:0:0:0:0:0:0:1', 'http://localhost:8081/posts/publish', 'ERROR', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', 'testuser');
INSERT INTO `user_logs` VALUES ('2025-08-29 16:25:53.599320', 3049, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 16:26:16.481237', 3050, NULL, 'DATABASE_ERROR', 'Error occurred: Database error for user testuser on /music/upload: could not execute statement [Field \'file_name\' doesn\'t have a default value] [insert into music (album,artist,coverPath,duration,fileName,filePath,isPublic,lyrics,lyricsTimestamp,playCount,title,upload_time,user_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?)]', 'could not execute statement [Field \'file_name\' doesn\'t have a default value] [insert into music (album,artist,coverPath,duration,fileName,filePath,isPublic,lyrics,lyricsTimestamp,playCount,title,upload_time,user_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?)]', '0:0:0:0:0:0:0:1', 'http://localhost:8081/music/upload', 'ERROR', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', 'testuser');
INSERT INTO `user_logs` VALUES ('2025-08-29 16:33:45.336000', 3051, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 16:34:17.378414', 3052, NULL, 'DATABASE_ERROR', 'Error occurred: Database error for user testuser on /music/upload: could not execute statement [Field \'fileName\' doesn\'t have a default value] [insert into music (album,artist,cover_path,duration,file_name,file_path,is_public,lyrics,lyrics_timestamp,play_count,title,upload_time,user_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?)]', 'could not execute statement [Field \'fileName\' doesn\'t have a default value] [insert into music (album,artist,cover_path,duration,file_name,file_path,is_public,lyrics,lyrics_timestamp,play_count,title,upload_time,user_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?)]', '0:0:0:0:0:0:0:1', 'http://localhost:8081/music/upload', 'ERROR', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', 'testuser');
INSERT INTO `user_logs` VALUES ('2025-08-29 16:41:46.086250', 3053, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 16:42:07.551747', 3054, NULL, 'DATABASE_ERROR', 'Error occurred: Database error for user testuser on /music/upload: could not execute statement [Field \'file_name\' doesn\'t have a default value] [insert into music (album,artist,coverPath,duration,fileName,filePath,isPublic,lyrics,lyrics_timestamp,playCount,title,upload_time,user_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?)]', 'could not execute statement [Field \'file_name\' doesn\'t have a default value] [insert into music (album,artist,coverPath,duration,fileName,filePath,isPublic,lyrics,lyrics_timestamp,playCount,title,upload_time,user_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?)]', '0:0:0:0:0:0:0:1', 'http://localhost:8081/music/upload', 'ERROR', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', 'testuser');
INSERT INTO `user_logs` VALUES ('2025-08-29 16:48:34.701799', 3055, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 16:49:00.211970', 3056, NULL, 'DATABASE_ERROR', 'Error occurred: Database error for user testuser on /music/upload: could not execute statement [Field \'fileName\' doesn\'t have a default value] [insert into music (album,artist,cover_path,duration,file_name,file_path,is_public,lyrics,lyrics_timestamp,play_count,title,upload_time,user_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?)]', 'could not execute statement [Field \'fileName\' doesn\'t have a default value] [insert into music (album,artist,cover_path,duration,file_name,file_path,is_public,lyrics,lyrics_timestamp,play_count,title,upload_time,user_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?)]', '0:0:0:0:0:0:0:1', 'http://localhost:8081/music/upload', 'ERROR', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', 'testuser');
INSERT INTO `user_logs` VALUES ('2025-08-29 16:49:04.831735', 3057, NULL, 'DATABASE_ERROR', 'Error occurred: Database error for user testuser on /music/upload: could not execute statement [Field \'fileName\' doesn\'t have a default value] [insert into music (album,artist,cover_path,duration,file_name,file_path,is_public,lyrics,lyrics_timestamp,play_count,title,upload_time,user_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?)]', 'could not execute statement [Field \'fileName\' doesn\'t have a default value] [insert into music (album,artist,cover_path,duration,file_name,file_path,is_public,lyrics,lyrics_timestamp,play_count,title,upload_time,user_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?)]', '0:0:0:0:0:0:0:1', 'http://localhost:8081/music/upload', 'ERROR', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', 'testuser');
INSERT INTO `user_logs` VALUES ('2025-08-29 17:02:58.203278', 3058, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 17:03:42.791776', 3059, NULL, 'DATABASE_ERROR', 'Error occurred: Database error for user testuser on /music/upload: could not execute statement [Field \'upload_time\' doesn\'t have a default value] [insert into music (album,artist,coverPath,duration,fileName,filePath,isPublic,lyrics,lyricsTimestamp,playCount,title,uploadTime,userId) values (?,?,?,?,?,?,?,?,?,?,?,?,?)]', 'could not execute statement [Field \'upload_time\' doesn\'t have a default value] [insert into music (album,artist,coverPath,duration,fileName,filePath,isPublic,lyrics,lyricsTimestamp,playCount,title,uploadTime,userId) values (?,?,?,?,?,?,?,?,?,?,?,?,?)]', '0:0:0:0:0:0:0:1', 'http://localhost:8081/music/upload', 'ERROR', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', 'testuser');
INSERT INTO `user_logs` VALUES ('2025-08-29 17:04:03.479447', 3060, NULL, 'DATABASE_ERROR', 'Error occurred: Database error for user testuser on /music/upload: could not execute statement [Field \'upload_time\' doesn\'t have a default value] [insert into music (album,artist,coverPath,duration,fileName,filePath,isPublic,lyrics,lyricsTimestamp,playCount,title,uploadTime,userId) values (?,?,?,?,?,?,?,?,?,?,?,?,?)]', 'could not execute statement [Field \'upload_time\' doesn\'t have a default value] [insert into music (album,artist,coverPath,duration,fileName,filePath,isPublic,lyrics,lyricsTimestamp,playCount,title,uploadTime,userId) values (?,?,?,?,?,?,?,?,?,?,?,?,?)]', '0:0:0:0:0:0:0:1', 'http://localhost:8081/music/upload', 'ERROR', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', 'testuser');
INSERT INTO `user_logs` VALUES ('2025-08-29 17:09:35.127591', 3061, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 17:09:48.609373', 3062, NULL, 'DATABASE_ERROR', 'Error occurred: Database error for user testuser on /music/upload: could not execute statement [Field \'fileName\' doesn\'t have a default value] [insert into music (album,artist,cover_path,duration,file_name,file_path,is_public,lyrics,lyrics_timestamp,play_count,title,upload_time,user_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?)]', 'could not execute statement [Field \'fileName\' doesn\'t have a default value] [insert into music (album,artist,cover_path,duration,file_name,file_path,is_public,lyrics,lyrics_timestamp,play_count,title,upload_time,user_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?)]', '0:0:0:0:0:0:0:1', 'http://localhost:8081/music/upload', 'ERROR', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', 'testuser');
INSERT INTO `user_logs` VALUES ('2025-08-29 17:21:14.327665', 3063, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 17:21:29.601779', 3064, NULL, 'DATABASE_ERROR', 'Error occurred: Database error for user testuser on /music/upload: could not execute statement [Field \'fileName\' doesn\'t have a default value] [insert into music (album,artist,cover_path,duration,file_name,file_path,is_public,lyrics,lyrics_timestamp,play_count,title,upload_time,user_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?)]', 'could not execute statement [Field \'fileName\' doesn\'t have a default value] [insert into music (album,artist,cover_path,duration,file_name,file_path,is_public,lyrics,lyrics_timestamp,play_count,title,upload_time,user_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?)]', '0:0:0:0:0:0:0:1', 'http://localhost:8081/music/upload', 'ERROR', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', 'testuser');
INSERT INTO `user_logs` VALUES ('2025-08-29 17:24:14.257767', 3065, NULL, 'GENERAL_ERROR', 'Error occurred: General error for user anonymousUser on /api/music/list: Request method \'GET\' is not supported', 'Request method \'GET\' is not supported', '0:0:0:0:0:0:0:1', 'http://localhost:8081/api/music/list', 'ERROR', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.22621.5624', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 17:24:14.287248', 3066, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 17:24:29.463867', 3067, NULL, 'GENERAL_ERROR', 'Error occurred: General error for user anonymousUser on /api/music/list: Request method \'GET\' is not supported', 'Request method \'GET\' is not supported', '0:0:0:0:0:0:0:1', 'http://localhost:8081/api/music/list', 'ERROR', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.22621.5624', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 17:24:51.246374', 3068, NULL, 'GENERAL_ERROR', 'Error occurred: General error for user anonymousUser on /api/music/list: Request method \'GET\' is not supported', 'Request method \'GET\' is not supported', '0:0:0:0:0:0:0:1', 'http://localhost:8081/api/music/list', 'ERROR', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.22621.5624', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 17:26:58.914745', 3069, NULL, 'GENERAL_ERROR', 'Error occurred: General error for user anonymousUser on /api/music/list: Request method \'GET\' is not supported', 'Request method \'GET\' is not supported', '0:0:0:0:0:0:0:1', 'http://localhost:8081/api/music/list', 'ERROR', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.22621.5624', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 17:28:14.393625', 3070, NULL, 'GENERAL_ERROR', 'Error occurred: General error for user anonymousUser on /api/music/list: Request method \'GET\' is not supported', 'Request method \'GET\' is not supported', '0:0:0:0:0:0:0:1', 'http://localhost:8081/api/music/list', 'ERROR', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.22621.5624', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 17:36:28.527354', 3071, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 17:38:06.955220', 3072, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 17:46:11.702870', 3073, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 17:53:19.579859', 3074, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 17:59:59.040434', 3075, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 18:01:51.705941', 3076, NULL, 'DATABASE_ERROR', 'Error occurred: Database error for user testuser on /posts/publish: could not execute statement [Field \'createTime\' doesn\'t have a default value] [insert into user_moment (comment_count,content,create_time,images,is_public,like_count,location,mood,user_id,view_count) values (?,?,?,?,?,?,?,?,?,?)]', 'could not execute statement [Field \'createTime\' doesn\'t have a default value] [insert into user_moment (comment_count,content,create_time,images,is_public,like_count,location,mood,user_id,view_count) values (?,?,?,?,?,?,?,?,?,?)]', '0:0:0:0:0:0:0:1', 'http://localhost:8081/posts/publish', 'ERROR', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', 'testuser');
INSERT INTO `user_logs` VALUES ('2025-08-29 18:05:51.073653', 3077, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 18:06:09.200376', 3078, NULL, 'DATABASE_ERROR', 'Error occurred: Database error for user testuser on /posts/publish: could not execute statement [Field \'createTime\' doesn\'t have a default value] [insert into user_moment (comment_count,content,create_time,images,is_public,like_count,location,mood,user_id,view_count) values (?,?,?,?,?,?,?,?,?,?)]', 'could not execute statement [Field \'createTime\' doesn\'t have a default value] [insert into user_moment (comment_count,content,create_time,images,is_public,like_count,location,mood,user_id,view_count) values (?,?,?,?,?,?,?,?,?,?)]', '0:0:0:0:0:0:0:1', 'http://localhost:8081/posts/publish', 'ERROR', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', 'testuser');
INSERT INTO `user_logs` VALUES ('2025-08-29 18:10:57.809419', 3079, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 18:11:25.340738', 3080, NULL, 'DATABASE_ERROR', 'Error occurred: Database error for user testuser on /posts/publish: could not execute statement [Field \'createTime\' doesn\'t have a default value] [insert into user_moment (comment_count,content,create_time,images,is_public,like_count,location,mood,user_id,view_count) values (?,?,?,?,?,?,?,?,?,?)]', 'could not execute statement [Field \'createTime\' doesn\'t have a default value] [insert into user_moment (comment_count,content,create_time,images,is_public,like_count,location,mood,user_id,view_count) values (?,?,?,?,?,?,?,?,?,?)]', '0:0:0:0:0:0:0:1', 'http://localhost:8081/posts/publish', 'ERROR', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', 'testuser');
INSERT INTO `user_logs` VALUES ('2025-08-29 18:15:54.253663', 3081, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 18:16:06.119772', 3082, NULL, 'DATABASE_ERROR', 'Error occurred: Database error for user testuser on /posts/publish: could not execute statement [Field \'createTime\' doesn\'t have a default value] [insert into user_moment (comment_count,content,create_time,images,is_public,like_count,location,mood,user_id,view_count) values (?,?,?,?,?,?,?,?,?,?)]', 'could not execute statement [Field \'createTime\' doesn\'t have a default value] [insert into user_moment (comment_count,content,create_time,images,is_public,like_count,location,mood,user_id,view_count) values (?,?,?,?,?,?,?,?,?,?)]', '0:0:0:0:0:0:0:1', 'http://localhost:8081/posts/publish', 'ERROR', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', 'testuser');
INSERT INTO `user_logs` VALUES ('2025-08-29 18:16:30.203378', 3083, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.22621.5624', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 18:16:34.439474', 3084, NULL, 'GENERAL_ERROR', 'Error occurred: General error for user anonymousUser on /register: Required request parameter \'confirmPassword\' for method parameter type String is not present', 'Required request parameter \'confirmPassword\' for method parameter type String is not present', '0:0:0:0:0:0:0:1', 'http://localhost:8081/register', 'ERROR', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.22621.5624', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 18:26:24.005222', 3085, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.22621.5624', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 18:31:09.295260', 3086, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.22621.5624', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 18:31:12.410760', 3087, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 18:31:29.668273', 3088, NULL, 'DATABASE_ERROR', 'Error occurred: Database error for user testuser on /posts/publish: could not execute statement [Field \'userId\' doesn\'t have a default value] [insert into user_moment (comment_count,content,create_time,images,is_public,like_count,location,mood,user_id,view_count) values (?,?,?,?,?,?,?,?,?,?)]', 'could not execute statement [Field \'userId\' doesn\'t have a default value] [insert into user_moment (comment_count,content,create_time,images,is_public,like_count,location,mood,user_id,view_count) values (?,?,?,?,?,?,?,?,?,?)]', '0:0:0:0:0:0:0:1', 'http://localhost:8081/posts/publish', 'ERROR', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', 'testuser');
INSERT INTO `user_logs` VALUES ('2025-08-29 18:36:51.065734', 3089, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.22621.5624', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 18:36:58.127756', 3090, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 18:37:14.853790', 3091, NULL, 'DATABASE_ERROR', 'Error occurred: Database error for user testuser on /posts/publish: could not execute statement [Field \'userId\' doesn\'t have a default value] [insert into user_moment (comment_count,content,create_time,images,is_public,like_count,location,mood,user_id,view_count) values (?,?,?,?,?,?,?,?,?,?)]', 'could not execute statement [Field \'userId\' doesn\'t have a default value] [insert into user_moment (comment_count,content,create_time,images,is_public,like_count,location,mood,user_id,view_count) values (?,?,?,?,?,?,?,?,?,?)]', '0:0:0:0:0:0:0:1', 'http://localhost:8081/posts/publish', 'ERROR', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', 'testuser');
INSERT INTO `user_logs` VALUES ('2025-08-29 18:41:24.105944', 3092, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 18:41:38.265538', 3093, NULL, 'DATABASE_ERROR', 'Error occurred: Database error for user testuser on /posts/publish: could not execute statement [Field \'userId\' doesn\'t have a default value] [insert into user_moment (comment_count,content,create_time,images,is_public,like_count,location,mood,user_id,view_count) values (?,?,?,?,?,?,?,?,?,?)]', 'could not execute statement [Field \'userId\' doesn\'t have a default value] [insert into user_moment (comment_count,content,create_time,images,is_public,like_count,location,mood,user_id,view_count) values (?,?,?,?,?,?,?,?,?,?)]', '0:0:0:0:0:0:0:1', 'http://localhost:8081/posts/publish', 'ERROR', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', 'testuser');
INSERT INTO `user_logs` VALUES ('2025-08-29 18:45:51.376959', 3094, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.22621.5624', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 18:45:51.382978', 3095, NULL, 'LOGIN_ERROR', 'Error occurred: Login failed - invalid credentials', 'Login failed - invalid credentials', '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'ERROR', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.22621.5624', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 18:45:51.420234', 3096, NULL, 'GENERAL_ERROR', 'Error occurred: General error for user anonymousUser on /posts/publish: Required request parameter \'content\' for method parameter type String is not present', 'Required request parameter \'content\' for method parameter type String is not present', '0:0:0:0:0:0:0:1', 'http://localhost:8081/posts/publish', 'ERROR', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.22621.5624', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 18:47:49.137214', 3097, NULL, 'DATABASE_ERROR', 'Error occurred: Database error for user testuser on /posts/publish: could not execute statement [Field \'userId\' doesn\'t have a default value] [insert into user_moment (comment_count,content,create_time,images,is_public,like_count,location,mood,user_id,view_count) values (?,?,?,?,?,?,?,?,?,?)]', 'could not execute statement [Field \'userId\' doesn\'t have a default value] [insert into user_moment (comment_count,content,create_time,images,is_public,like_count,location,mood,user_id,view_count) values (?,?,?,?,?,?,?,?,?,?)]', '0:0:0:0:0:0:0:1', 'http://localhost:8081/posts/publish', 'ERROR', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', 'testuser');
INSERT INTO `user_logs` VALUES ('2025-08-29 18:48:09.734486', 3098, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 18:48:25.621556', 3099, NULL, 'DATABASE_ERROR', 'Error occurred: Database error for user testuser on /posts/publish: could not execute statement [Field \'userId\' doesn\'t have a default value] [insert into user_moment (comment_count,content,create_time,images,is_public,like_count,location,mood,user_id,view_count) values (?,?,?,?,?,?,?,?,?,?)]', 'could not execute statement [Field \'userId\' doesn\'t have a default value] [insert into user_moment (comment_count,content,create_time,images,is_public,like_count,location,mood,user_id,view_count) values (?,?,?,?,?,?,?,?,?,?)]', '0:0:0:0:0:0:0:1', 'http://localhost:8081/posts/publish', 'ERROR', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', 'testuser');
INSERT INTO `user_logs` VALUES ('2025-08-29 18:50:41.640680', 3100, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.22621.5624', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 18:50:41.645680', 3101, NULL, 'LOGIN_ERROR', 'Error occurred: Login failed - invalid credentials', 'Login failed - invalid credentials', '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'ERROR', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.22621.5624', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 18:50:41.658709', 3102, NULL, 'GENERAL_ERROR', 'Error occurred: General error for user anonymousUser on /posts/publish: Required request parameter \'content\' for method parameter type String is not present', 'Required request parameter \'content\' for method parameter type String is not present', '0:0:0:0:0:0:0:1', 'http://localhost:8081/posts/publish', 'ERROR', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.22621.5624', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 18:56:48.650884', 3103, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.22621.5624', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 18:56:48.657219', 3104, NULL, 'LOGIN_ERROR', 'Error occurred: Login failed - invalid credentials', 'Login failed - invalid credentials', '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'ERROR', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.22621.5624', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 18:56:48.672585', 3105, NULL, 'GENERAL_ERROR', 'Error occurred: General error for user anonymousUser on /posts/publish: Required request parameter \'content\' for method parameter type String is not present', 'Required request parameter \'content\' for method parameter type String is not present', '0:0:0:0:0:0:0:1', 'http://localhost:8081/posts/publish', 'ERROR', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.22621.5624', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 18:57:53.967034', 3106, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.22621.5624', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 18:57:53.972681', 3107, NULL, 'LOGIN_ERROR', 'Error occurred: Login failed - invalid credentials', 'Login failed - invalid credentials', '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'ERROR', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.22621.5624', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 18:57:53.990519', 3108, NULL, 'GENERAL_ERROR', 'Error occurred: General error for user anonymousUser on /posts/publish: Required request parameter \'content\' for method parameter type String is not present', 'Required request parameter \'content\' for method parameter type String is not present', '0:0:0:0:0:0:0:1', 'http://localhost:8081/posts/publish', 'ERROR', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.22621.5624', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 19:05:00.415704', 3109, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 19:05:12.860985', 3110, NULL, 'DATABASE_ERROR', 'Error occurred: Database error for user testuser on /posts/publish: could not execute statement [Field \'userId\' doesn\'t have a default value] [insert into user_moment (comment_count,content,create_time,images,is_public,like_count,location,mood,user_id,view_count) values (?,?,?,?,?,?,?,?,?,?)]', 'could not execute statement [Field \'userId\' doesn\'t have a default value] [insert into user_moment (comment_count,content,create_time,images,is_public,like_count,location,mood,user_id,view_count) values (?,?,?,?,?,?,?,?,?,?)]', '0:0:0:0:0:0:0:1', 'http://localhost:8081/posts/publish', 'ERROR', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', 'testuser');
INSERT INTO `user_logs` VALUES ('2025-08-29 19:07:19.896882', 3111, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 19:07:32.081544', 3112, NULL, 'DATABASE_ERROR', 'Error occurred: Database error for user testuser on /posts/publish: could not execute statement [Field \'userId\' doesn\'t have a default value] [insert into user_moment (comment_count,content,create_time,images,is_public,like_count,location,mood,user_id,view_count) values (?,?,?,?,?,?,?,?,?,?)]', 'could not execute statement [Field \'userId\' doesn\'t have a default value] [insert into user_moment (comment_count,content,create_time,images,is_public,like_count,location,mood,user_id,view_count) values (?,?,?,?,?,?,?,?,?,?)]', '0:0:0:0:0:0:0:1', 'http://localhost:8081/posts/publish', 'ERROR', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', 'testuser');
INSERT INTO `user_logs` VALUES ('2025-08-29 19:12:04.283861', 3113, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 19:15:36.610119', 3114, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 19:18:18.511070', 3115, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 19:23:57.287873', 3116, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 19:36:16.591573', 3117, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 19:55:04.037099', 3118, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 19:55:09.381903', 3119, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 19:55:09.389180', 3120, NULL, 'LOGIN_ERROR', 'Error occurred: Login failed - invalid credentials', 'Login failed - invalid credentials', '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'ERROR', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 20:06:43.372593', 3121, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 20:19:14.772666', 3122, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 20:25:43.793471', 3123, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 20:47:51.142137', 3124, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 21:23:27.539879', 3125, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/auth/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 21:38:01.628834', 3126, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 21:39:56.059805', 3127, NULL, 'VIDEO_UPLOAD_SUCCESS', 'Video uploaded successfully: ID=14, Title=郝宇星 - 其实都没有', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/upload', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', 'testuser');
INSERT INTO `user_logs` VALUES ('2025-08-29 21:41:29.987174', 3128, NULL, 'DATABASE_ERROR', 'Error occurred: Database error for user testuser on /video/14/delete: could not execute batch [Cannot delete or update a parent row: a foreign key constraint fails (`video_website`.`video_contents`, CONSTRAINT `FKryksphiqn0506se0mr9v27ew9` FOREIGN KEY (`video_id`) REFERENCES `videos` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT)] [delete from videos where id=?]; SQL [delete from videos where id=?]; constraint [null]', 'could not execute batch [Cannot delete or update a parent row: a foreign key constraint fails (`video_website`.`video_contents`, CONSTRAINT `FKryksphiqn0506se0mr9v27ew9` FOREIGN KEY (`video_id`) REFERENCES `videos` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT)] [delete from videos where id=?]; SQL [delete from videos where id=?]; constraint [null]', '0:0:0:0:0:0:0:1', 'http://localhost:8081/video/14/delete', 'ERROR', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', 'testuser');
INSERT INTO `user_logs` VALUES ('2025-08-29 21:52:36.618345', 3129, NULL, 'LOGIN_PAGE_VISIT', 'User visited login page', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/login', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', NULL);
INSERT INTO `user_logs` VALUES ('2025-08-29 21:54:07.674410', 3130, NULL, 'VIDEO_UPLOAD_SUCCESS', 'Video uploaded successfully: ID=15, Title=一束光', NULL, '0:0:0:0:0:0:0:1', 'http://localhost:8081/upload', 'SUCCESS', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', 'testuser');

-- ----------------------------
-- Table structure for user_moment
-- ----------------------------
DROP TABLE IF EXISTS `user_moment`;
CREATE TABLE `user_moment`  (
  `is_public` bit(1) NULL DEFAULT NULL,
  `comment_count` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `like_count` bigint NULL DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `view_count` bigint NULL DEFAULT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `mood` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKtkf0pgq20y7tpk7e5bf6yjlqo`(`user_id` ASC) USING BTREE,
  CONSTRAINT `FKtkf0pgq20y7tpk7e5bf6yjlqo` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_moment
-- ----------------------------
INSERT INTO `user_moment` VALUES (b'1', 1, '2025-08-29 19:12:17', 4, 1, 28, 0, 'aa', '[\"uploads/moments/f61dcb64-e361-4faa-8d0d-e960e87aed5f_20220603234750_00ddb.png\"]', '', '');

-- ----------------------------
-- Table structure for user_task_progress
-- ----------------------------
DROP TABLE IF EXISTS `user_task_progress`;
CREATE TABLE `user_task_progress`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `task_id` bigint NOT NULL,
  `progress` int NOT NULL DEFAULT 0,
  `completed` tinyint(1) NOT NULL DEFAULT 0,
  `date` date NOT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  `completedAt` datetime(6) NULL DEFAULT NULL,
  `createdAt` datetime(6) NOT NULL,
  `taskId` bigint NOT NULL,
  `updatedAt` datetime(6) NOT NULL,
  `userId` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_task_date`(`user_id` ASC, `task_id` ASC, `date` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_task_progress
-- ----------------------------

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `banned` bit(1) NULL DEFAULT NULL,
  `consecutive_checkin_days` int NULL DEFAULT NULL,
  `deleted` bit(1) NULL DEFAULT NULL,
  `enabled` bit(1) NULL DEFAULT NULL,
  `last_checkin_date` date NULL DEFAULT NULL,
  `points` int NULL DEFAULT NULL,
  `total_checkin_days` int NULL DEFAULT NULL,
  `warning_count` int NULL DEFAULT NULL,
  `created_at` datetime(6) NULL DEFAULT NULL,
  `deleted_at` datetime(6) NULL DEFAULT NULL,
  `experience` bigint NULL DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) NULL DEFAULT NULL,
  `version` bigint NULL DEFAULT NULL,
  `nickname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `bio` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `ban_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `delete_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK_6dotkott2kjsp8vw4d0m25fb7`(`email` ASC) USING BTREE,
  UNIQUE INDEX `UK_r43af9ap4edm43mmtq01oddj6`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 29 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (b'0', 0, b'0', b'1', NULL, 0, 0, 0, '2025-08-19 00:42:30.484873', NULL, 0, 1, '2025-08-19 00:42:30.484873', 0, NULL, NULL, NULL, NULL, NULL, 'admin@example.com', '$2a$10$VCUETOFsi2RjHMq3CFv9kOo1PHixHqjbFdkOimG2F2ahe4NCUP2s2', 'ADMIN', 'admin');
INSERT INTO `users` VALUES (b'0', 4, b'0', b'1', '2025-08-29', 0, 8, 0, '2025-08-19 00:42:32.662907', NULL, 0, 2, '2025-08-29 10:28:42.987398', 9, NULL, NULL, '/uploads/avatars/c04a9a52-0da5-4e46-9526-8df8047d0dbb.jpeg', NULL, NULL, 'root@example.com', '$2a$10$vUlsGcBe8D4USdFB664Si.yjKEx4VnF.CrKypJ2nHIZxfvH8cCPHi', 'ADMIN', 'root');
INSERT INTO `users` VALUES (b'0', 0, b'0', b'1', NULL, 0, 0, 0, '2025-08-29 09:49:13.327198', NULL, 0, 28, '2025-08-29 09:49:13.327198', 0, NULL, NULL, '/images/default-avatar.png', NULL, NULL, '3364606601@qq.com', '$2a$10$lNrt33dTzPhu2LQ0ocPFCeg/AXMsfwcCKM0dkIeA5n4AG/.nvI0Jq', 'USER', 'testuser');

-- ----------------------------
-- Table structure for video_contents
-- ----------------------------
DROP TABLE IF EXISTS `video_contents`;
CREATE TABLE `video_contents`  (
  `sort_order` int NULL DEFAULT NULL,
  `created_at` datetime(6) NULL DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `video_id` bigint NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
  `data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
  `type` enum('AUDIO','IMAGE','TEXT','VIDEO') CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `url` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKryksphiqn0506se0mr9v27ew9`(`video_id` ASC) USING BTREE,
  CONSTRAINT `FKryksphiqn0506se0mr9v27ew9` FOREIGN KEY (`video_id`) REFERENCES `videos` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of video_contents
-- ----------------------------
INSERT INTO `video_contents` VALUES (1, '2025-08-29 13:21:25.022556', 3, 13, '', '', 'IMAGE', '/uploads/content/images/923a5ee3-34b0-4c0d-b664-0941ad44ceef.jpeg');
INSERT INTO `video_contents` VALUES (1, '2025-08-29 21:54:07.670388', 5, 15, '', '', 'AUDIO', '/uploads/content/audios/572fe75f-0fb4-43db-b8a4-a73cc750460a.mp3');

-- ----------------------------
-- Table structure for video_favorites
-- ----------------------------
DROP TABLE IF EXISTS `video_favorites`;
CREATE TABLE `video_favorites`  (
  `created_at` datetime(6) NULL DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `video_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UKltjpn6gbujms96o9i5rknjvvk`(`user_id` ASC, `video_id` ASC) USING BTREE,
  INDEX `FKmn2mgrwgl5ttndr3815tngi11`(`video_id` ASC) USING BTREE,
  CONSTRAINT `FK32aayhl06xmulit83fhj4rbq3` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKmn2mgrwgl5ttndr3815tngi11` FOREIGN KEY (`video_id`) REFERENCES `videos` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of video_favorites
-- ----------------------------

-- ----------------------------
-- Table structure for video_likes
-- ----------------------------
DROP TABLE IF EXISTS `video_likes`;
CREATE TABLE `video_likes`  (
  `created_at` datetime(6) NULL DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `video_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK5tlc8gm2hqk6pd2ef02jhmx9b`(`user_id` ASC, `video_id` ASC) USING BTREE,
  INDEX `FKh5xdmkusmdp62bin35eulq2ny`(`video_id` ASC) USING BTREE,
  CONSTRAINT `FK8gal6orxcks0llsblhsc8yxu7` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKh5xdmkusmdp62bin35eulq2ny` FOREIGN KEY (`video_id`) REFERENCES `videos` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of video_likes
-- ----------------------------

-- ----------------------------
-- Table structure for video_tags
-- ----------------------------
DROP TABLE IF EXISTS `video_tags`;
CREATE TABLE `video_tags`  (
  `tag_id` bigint NOT NULL,
  `video_id` bigint NOT NULL,
  PRIMARY KEY (`tag_id`, `video_id`) USING BTREE,
  INDEX `FKpr6ks7ia3ilx9ec2mmwb82lb6`(`video_id` ASC) USING BTREE,
  CONSTRAINT `FKpr6ks7ia3ilx9ec2mmwb82lb6` FOREIGN KEY (`video_id`) REFERENCES `videos` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKqfnpe46owtsy6c6t5my7exjmq` FOREIGN KEY (`tag_id`) REFERENCES `tags` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of video_tags
-- ----------------------------

-- ----------------------------
-- Table structure for videos
-- ----------------------------
DROP TABLE IF EXISTS `videos`;
CREATE TABLE `videos`  (
  `favorite_count` int NULL DEFAULT NULL,
  `like_count` int NULL DEFAULT NULL,
  `category_id` bigint NULL DEFAULT NULL,
  `created_at` datetime(6) NULL DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) NULL DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `view_count` bigint NULL DEFAULT NULL,
  `views` bigint NULL DEFAULT NULL,
  `tags` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `ban_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
  `duration` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `file_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
  `reject_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `status` enum('APPROVED','BANNED','PENDING','REJECTED') CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `thumbnail` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `thumbnail_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK757y9y2j67t6nl4h5746si1rx`(`category_id` ASC) USING BTREE,
  INDEX `FK75696octon297ywni28sk19ek`(`user_id` ASC) USING BTREE,
  CONSTRAINT `FK75696octon297ywni28sk19ek` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FK757y9y2j67t6nl4h5746si1rx` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of videos
-- ----------------------------
INSERT INTO `videos` VALUES (0, 0, 1, '2025-08-29 13:21:24.985694', 13, '2025-08-29 16:00:29.648742', 28, 19, 19, NULL, NULL, '[{\"type\":\"IMAGE\",\"data\":\"\",\"url\":\"/uploads/content/images/923a5ee3-34b0-4c0d-b664-0941ad44ceef.jpeg\",\"order\":1}]', '啊啊', NULL, '/uploads/videos/578925a6-348b-4e34-82c7-03f591a63c3d.mp4', NULL, NULL, 'APPROVED', '/uploads/thumbnails/7140831f-a7cf-4af3-bfd6-ef273e231968.jpeg', NULL, '郝宇星 - 其实都没有', '/uploads/videos/578925a6-348b-4e34-82c7-03f591a63c3d.mp4');
INSERT INTO `videos` VALUES (0, 0, 1, '2025-08-29 21:54:07.595910', 15, '2025-08-29 21:54:24.646406', 28, 0, 0, NULL, NULL, '[{\"type\":\"AUDIO\",\"data\":\"\",\"url\":\"/uploads/content/audios/572fe75f-0fb4-43db-b8a4-a73cc750460a.mp3\",\"order\":1}]', '', NULL, '/uploads/videos/0e8aa867-86a5-4533-a38d-17998008a3e2.mp4', NULL, NULL, 'APPROVED', '/uploads/thumbnails/c03b8afd-9834-445c-9860-168c643229dc.jpeg', NULL, '一束光', '/uploads/videos/0e8aa867-86a5-4533-a38d-17998008a3e2.mp4');

-- ----------------------------
-- Table structure for view_history
-- ----------------------------
DROP TABLE IF EXISTS `view_history`;
CREATE TABLE `view_history`  (
  `completion_rate` double NULL DEFAULT NULL,
  `watch_count` int NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `last_position` bigint NULL DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `user_id` bigint NOT NULL,
  `video_id` bigint NOT NULL,
  `watch_duration` bigint NOT NULL,
  `ip_address` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `device_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKd8qhxyfxcdueyy3f5xdju7oj5`(`user_id` ASC) USING BTREE,
  INDEX `FKhxr1ialwsi5n8qcagtd6uti4w`(`video_id` ASC) USING BTREE,
  CONSTRAINT `FKd8qhxyfxcdueyy3f5xdju7oj5` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKhxr1ialwsi5n8qcagtd6uti4w` FOREIGN KEY (`video_id`) REFERENCES `videos` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of view_history
-- ----------------------------

-- ----------------------------
-- Table structure for violations
-- ----------------------------
DROP TABLE IF EXISTS `violations`;
CREATE TABLE `violations`  (
  `created_at` datetime(6) NULL DEFAULT NULL,
  `created_by` bigint NULL DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NULL DEFAULT NULL,
  `video_id` bigint NULL DEFAULT NULL,
  `action` enum('USER_BAN','VIDEO_BAN','WARNING') CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `type` enum('COPYRIGHT_VIOLATION','HARASSMENT','INAPPROPRIATE_CONTENT','OTHER','SPAM') CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK7ih0pl7n4kkom4d7y6jtkhlv2`(`created_by` ASC) USING BTREE,
  INDEX `FK2lstlejyppuqwmlnrv237jubi`(`user_id` ASC) USING BTREE,
  INDEX `FKiq39485q8xahj9hry2qd45fx2`(`video_id` ASC) USING BTREE,
  CONSTRAINT `FK2lstlejyppuqwmlnrv237jubi` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FK7ih0pl7n4kkom4d7y6jtkhlv2` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKiq39485q8xahj9hry2qd45fx2` FOREIGN KEY (`video_id`) REFERENCES `videos` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of violations
-- ----------------------------

-- ----------------------------
-- Table structure for voice_clone
-- ----------------------------
DROP TABLE IF EXISTS `voice_clone`;
CREATE TABLE `voice_clone`  (
  `duration` int NULL DEFAULT NULL,
  `is_public` bit(1) NULL DEFAULT NULL,
  `created_time` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `extracted_audio_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `source_video_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `transcription` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
  `voice_model_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `voice_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `createdTime` datetime(6) NOT NULL,
  `extractedAudioPath` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `isPublic` bit(1) NULL DEFAULT NULL,
  `sourceVideoPath` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `userId` bigint NOT NULL,
  `voiceModelPath` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `voiceName` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of voice_clone
-- ----------------------------

-- ----------------------------
-- Procedure structure for safe_delete_table_data
-- ----------------------------
DROP PROCEDURE IF EXISTS `safe_delete_table_data`;
delimiter ;;
CREATE PROCEDURE `safe_delete_table_data`(IN table_name VARCHAR(64))
BEGIN
    DECLARE table_exists INT DEFAULT 0;
    
    -- 检查表是否存在
    SELECT COUNT(*) INTO table_exists 
    FROM information_schema.TABLES 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = table_name;
    
    -- 如果表存在，则删除数据
    IF table_exists > 0 THEN
        SET @sql = CONCAT('DELETE FROM ', table_name);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
        SELECT CONCAT('✓ 已清理表: ', table_name) as result;
    ELSE
        SELECT CONCAT('⚠ 表不存在，跳过: ', table_name) as result;
    END IF;
END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
