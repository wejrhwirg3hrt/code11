package org.example.service;

import org.example.entity.Video;
import org.example.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = false)
public class DataCleanupService {

    @Autowired
    private VideoRepository videoRepository;

    /**
     * 清理包含example.com的测试数据
     */
    @Transactional
    public void cleanupExampleData() {
        try {
            System.out.println("开始清理包含example.com的测试数据...");
            
            // 查找并删除包含example.com的视频URL
            List<Video> videosWithExampleUrl = videoRepository.findByUrlContaining("example.com");
            if (!videosWithExampleUrl.isEmpty()) {
                videoRepository.deleteAll(videosWithExampleUrl);
                System.out.println("删除了 " + videosWithExampleUrl.size() + " 个包含example.com URL的视频");
            }
            
            // 查找并删除包含example.com的缩略图
            List<Video> videosWithExampleThumbnail = videoRepository.findByThumbnailContaining("example.com");
            if (!videosWithExampleThumbnail.isEmpty()) {
                videoRepository.deleteAll(videosWithExampleThumbnail);
                System.out.println("删除了 " + videosWithExampleThumbnail.size() + " 个包含example.com缩略图的视频");
            }
            
            long remainingVideos = videoRepository.count();
            System.out.println("清理完成，剩余视频数量: " + remainingVideos);
            
        } catch (Exception e) {
            System.err.println("清理测试数据时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 清理视频标签中的乱码
     */
    @Transactional
    public void cleanupGarbledTags() {
        try {
            System.out.println("开始清理视频标签中的乱码...");

            List<Video> allVideos = videoRepository.findAll();
            int cleanedCount = 0;

            for (Video video : allVideos) {
                String originalTags = video.getTagsString();
                if (originalTags != null && !originalTags.isEmpty()) {
                    // 检查是否包含乱码字符
                    if (containsGarbledText(originalTags)) {
                        // 清理乱码，设置为空或默认标签
                        video.setTagsString("");
                        videoRepository.save(video);
                        cleanedCount++;
                        System.out.println("清理视频ID " + video.getId() + " 的乱码标签: " + originalTags);
                    }
                }
            }

            System.out.println("标签乱码清理完成，共清理了 " + cleanedCount + " 个视频的标签");

        } catch (Exception e) {
            System.err.println("清理标签乱码时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 检查文本是否包含乱码
     */
    private boolean containsGarbledText(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        // 检查是否包含常见的乱码模式
        // 1. 包含大量连续的特殊字符
        if (text.matches(".*[\\x00-\\x1F\\x7F-\\x9F]{3,}.*")) {
            return true;
        }

        // 2. 包含明显的编码错误字符
        if (text.contains("�") || text.contains("?")) {
            return true;
        }

        // 3. 检查是否包含大量非中文、非英文、非数字的字符
        long invalidCharCount = text.chars()
            .filter(c -> !Character.isLetterOrDigit(c) &&
                        !isChineseCharacter(c) &&
                        c != ',' && c != ' ' && c != '-' && c != '_')
            .count();

        return invalidCharCount > text.length() * 0.3; // 如果超过30%是无效字符，认为是乱码
    }

    /**
     * 检查是否是中文字符
     */
    private boolean isChineseCharacter(int codePoint) {
        return (codePoint >= 0x4E00 && codePoint <= 0x9FFF) || // CJK统一汉字
               (codePoint >= 0x3400 && codePoint <= 0x4DBF) || // CJK扩展A
               (codePoint >= 0x20000 && codePoint <= 0x2A6DF); // CJK扩展B
    }

    /**
     * 为视频添加示例标签
     */
    @Transactional
    public void addSampleTags() {
        try {
            System.out.println("开始为视频添加示例标签...");

            List<Video> allVideos = videoRepository.findAll();
            String[] sampleTags = {"娱乐", "搞笑", "生活", "音乐", "舞蹈", "游戏", "科技", "美食", "旅行", "教育"};

            int updatedCount = 0;
            for (Video video : allVideos) {
                if (video.getTagsString() == null || video.getTagsString().isEmpty()) {
                    // 随机选择2-3个标签
                    int tagCount = 2 + (int)(Math.random() * 2); // 2-3个标签
                    StringBuilder tags = new StringBuilder();

                    for (int i = 0; i < tagCount; i++) {
                        if (i > 0) tags.append(",");
                        int randomIndex = (int)(Math.random() * sampleTags.length);
                        tags.append(sampleTags[randomIndex]);
                    }

                    video.setTagsString(tags.toString());
                    videoRepository.save(video);
                    updatedCount++;
                    System.out.println("为视频ID " + video.getId() + " 添加标签: " + tags.toString());
                }
            }

            System.out.println("示例标签添加完成，共为 " + updatedCount + " 个视频添加了标签");

        } catch (Exception e) {
            System.err.println("添加示例标签时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 修复无效的缩略图URL
     */
    @Transactional
    public void fixInvalidThumbnails() {
        try {
            System.out.println("开始修复无效的缩略图URL...");
            
            // 查找所有包含example.com或无效URL的视频
            List<Video> videosWithInvalidThumbnails = videoRepository.findAll().stream()
                .filter(video -> {
                    String thumbnail = video.getThumbnail();
                    return thumbnail != null && (
                        thumbnail.contains("example.com") ||
                        thumbnail.contains("thumb.jpg") ||
                        thumbnail.trim().isEmpty()
                    );
                })
                .toList();
            
            for (Video video : videosWithInvalidThumbnails) {
                // 使用placeholder替换无效的缩略图
                String newThumbnail = "https://via.placeholder.com/400x200?text=" + 
                    java.net.URLEncoder.encode(video.getTitle().length() > 20 ? 
                        video.getTitle().substring(0, 20) + "..." : video.getTitle(), "UTF-8");
                video.setThumbnail(newThumbnail);
                videoRepository.save(video);
            }
            
            if (!videosWithInvalidThumbnails.isEmpty()) {
                System.out.println("修复了 " + videosWithInvalidThumbnails.size() + " 个无效缩略图");
            }
            
        } catch (Exception e) {
            System.err.println("修复缩略图时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
