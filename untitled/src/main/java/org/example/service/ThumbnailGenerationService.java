package org.example.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import javax.imageio.ImageIO;

/**
 * 缩略图生成服务
 */
@Service
@Transactional(readOnly = false)
public class ThumbnailGenerationService {

    @Value("${app.upload.path:./uploads}")
    private String uploadPath;

    /**
     * 为视频生成默认缩略图
     */
    public String generateDefaultThumbnail(String videoTitle) {
        try {
            // 创建缩略图目录
            Path thumbnailDir = Paths.get(uploadPath, "thumbnails");
            if (!Files.exists(thumbnailDir)) {
                Files.createDirectories(thumbnailDir);
            }

            // 生成缩略图
            BufferedImage thumbnail = createThumbnailImage(videoTitle);
            
            // 保存缩略图
            String fileName = UUID.randomUUID().toString() + ".png";
            Path filePath = thumbnailDir.resolve(fileName);
            ImageIO.write(thumbnail, "PNG", filePath.toFile());

            return "/uploads/thumbnails/" + fileName;
        } catch (Exception e) {
            System.err.println("生成缩略图失败: " + e.getMessage());
            try {
                return "https://via.placeholder.com/400x200?text=" +
                       java.net.URLEncoder.encode(videoTitle.length() > 20 ?
                       videoTitle.substring(0, 20) + "..." : videoTitle, "UTF-8");
            } catch (java.io.UnsupportedEncodingException ex) {
                return "https://via.placeholder.com/400x200?text=Video";
            }
        }
    }

    /**
     * 创建缩略图图像
     */
    private BufferedImage createThumbnailImage(String title) {
        int width = 400;
        int height = 200;
        
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // 设置渲染质量
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // 创建渐变背景
        GradientPaint gradient = new GradientPaint(
            0, 0, new Color(74, 144, 226),
            width, height, new Color(80, 170, 221)
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, width, height);
        
        // 添加装饰性元素
        g2d.setColor(new Color(255, 255, 255, 30));
        for (int i = 0; i < 5; i++) {
            int x = (int) (Math.random() * width);
            int y = (int) (Math.random() * height);
            int size = (int) (Math.random() * 50 + 20);
            g2d.fillOval(x, y, size, size);
        }
        
        // 添加播放按钮图标
        drawPlayButton(g2d, width / 2, height / 2);
        
        // 添加标题文字
        drawTitle(g2d, title, width, height);
        
        g2d.dispose();
        return image;
    }

    /**
     * 绘制播放按钮
     */
    private void drawPlayButton(Graphics2D g2d, int centerX, int centerY) {
        int size = 40;
        
        // 绘制圆形背景
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.fillOval(centerX - size/2, centerY - size/2, size, size);
        
        // 绘制三角形播放图标
        g2d.setColor(new Color(74, 144, 226));
        int[] xPoints = {centerX - 8, centerX - 8, centerX + 8};
        int[] yPoints = {centerY - 10, centerY + 10, centerY};
        g2d.fillPolygon(xPoints, yPoints, 3);
    }

    /**
     * 绘制标题文字
     */
    private void drawTitle(Graphics2D g2d, String title, int width, int height) {
        if (title == null || title.trim().isEmpty()) {
            title = "视频";
        }
        
        // 限制标题长度
        if (title.length() > 30) {
            title = title.substring(0, 30) + "...";
        }
        
        // 设置字体
        Font font = new Font("Microsoft YaHei", Font.BOLD, 16);
        g2d.setFont(font);
        
        // 计算文字位置
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(title);
        int textHeight = fm.getHeight();
        
        int x = (width - textWidth) / 2;
        int y = height - 20;
        
        // 绘制文字阴影
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.drawString(title, x + 1, y + 1);
        
        // 绘制文字
        g2d.setColor(Color.WHITE);
        g2d.drawString(title, x, y);
    }

    /**
     * 检查是否需要生成缩略图
     */
    public boolean needsDefaultThumbnail(String thumbnailUrl) {
        return thumbnailUrl == null || 
               thumbnailUrl.trim().isEmpty() || 
               thumbnailUrl.contains("placeholder");
    }

    /**
     * 为现有视频生成缩略图
     */
    public String generateThumbnailForExistingVideo(String videoTitle, String currentThumbnail) {
        if (needsDefaultThumbnail(currentThumbnail)) {
            return generateDefaultThumbnail(videoTitle);
        }
        return currentThumbnail;
    }
}
