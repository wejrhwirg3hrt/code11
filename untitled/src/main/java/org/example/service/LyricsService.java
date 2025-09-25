package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional(readOnly = false)
public class LyricsService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 歌词行数据结构
     */
    public static class LyricLine {
        private double time;
        private String text;

        public LyricLine() {}

        public LyricLine(double time, String text) {
            this.time = time;
            this.text = text;
        }

        public double getTime() {
            return time;
        }

        public void setTime(double time) {
            this.time = time;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    /**
     * 解析LRC格式歌词
     * 格式: [mm:ss.xx]歌词内容
     */
    public List<LyricLine> parseLrcLyrics(String lrcContent) {
        List<LyricLine> lyrics = new ArrayList<>();
        
        if (lrcContent == null || lrcContent.trim().isEmpty()) {
            return lyrics;
        }

        // LRC时间戳正则表达式
        Pattern pattern = Pattern.compile("\\[(\\d{2}):(\\d{2})\\.(\\d{2})\\](.*)");
        String[] lines = lrcContent.split("\n");

        for (String line : lines) {
            Matcher matcher = pattern.matcher(line.trim());
            if (matcher.matches()) {
                int minutes = Integer.parseInt(matcher.group(1));
                int seconds = Integer.parseInt(matcher.group(2));
                int centiseconds = Integer.parseInt(matcher.group(3));
                String text = matcher.group(4).trim();

                // 转换为总秒数
                double time = minutes * 60 + seconds + centiseconds / 100.0;
                
                if (!text.isEmpty()) {
                    lyrics.add(new LyricLine(time, text));
                }
            }
        }

        return lyrics;
    }

    /**
     * 解析简单格式歌词（每行一句，无时间戳）
     */
    public List<LyricLine> parseSimpleLyrics(String content) {
        List<LyricLine> lyrics = new ArrayList<>();
        
        if (content == null || content.trim().isEmpty()) {
            return lyrics;
        }

        String[] lines = content.split("\n");
        double timeInterval = 5.0; // 每句歌词间隔5秒

        for (int i = 0; i < lines.length; i++) {
            String text = lines[i].trim();
            if (!text.isEmpty()) {
                lyrics.add(new LyricLine(i * timeInterval, text));
            }
        }

        return lyrics;
    }

    /**
     * 将歌词列表转换为JSON字符串
     */
    public String lyricsToJson(List<LyricLine> lyrics) {
        try {
            return objectMapper.writeValueAsString(lyrics);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    /**
     * 从JSON字符串解析歌词列表
     */
    public List<LyricLine> jsonToLyrics(String json) {
        try {
            return objectMapper.readValue(json, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, LyricLine.class));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * 生成空歌词（不再提供默认示例歌词）
     */
    public List<LyricLine> generateSampleLyrics(String title, String artist) {
        List<LyricLine> lyrics = new ArrayList<>();

        // 只显示基本信息，不提供模拟歌词
        lyrics.add(new LyricLine(0, "🎵 " + title + " 🎵"));
        lyrics.add(new LyricLine(3, "演唱：" + artist));
        lyrics.add(new LyricLine(6, ""));
        lyrics.add(new LyricLine(10, "暂无歌词"));
        lyrics.add(new LyricLine(14, "点击编辑按钮添加歌词"));
        lyrics.add(new LyricLine(18, "或使用AI语音识别功能"));

        return lyrics;
    }

    /**
     * 智能匹配歌词时间点
     */
    public int findCurrentLyricIndex(List<LyricLine> lyrics, double currentTime) {
        if (lyrics == null || lyrics.isEmpty()) {
            return -1;
        }

        int activeIndex = -1;
        for (int i = lyrics.size() - 1; i >= 0; i--) {
            if (currentTime >= lyrics.get(i).getTime()) {
                activeIndex = i;
                break;
            }
        }
        return activeIndex;
    }

    /**
     * 获取下一句歌词的索引
     */
    public int getNextLyricIndex(List<LyricLine> lyrics, int currentIndex) {
        if (lyrics == null || currentIndex < 0 || currentIndex >= lyrics.size() - 1) {
            return -1;
        }
        return currentIndex + 1;
    }

    /**
     * 检测歌词格式
     */
    public String detectLyricsFormat(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "empty";
        }

        // 检查是否包含LRC时间戳
        Pattern lrcPattern = Pattern.compile("\\[\\d{2}:\\d{2}\\.\\d{2}\\]");
        if (lrcPattern.matcher(content).find()) {
            return "lrc";
        }

        // 检查是否为JSON格式
        if (content.trim().startsWith("[") && content.trim().endsWith("]")) {
            try {
                jsonToLyrics(content);
                return "json";
            } catch (Exception e) {
                // 不是有效的JSON
            }
        }

        return "simple";
    }


}
