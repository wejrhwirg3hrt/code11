package org.example.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.example.entity.Music;
import org.example.entity.User;
import org.example.service.LyricsService;
import org.example.service.MusicService;
import org.example.service.UserService;
import org.example.service.VoskAudioRecognitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/music")
public class MusicController {

    private static final Logger logger = LoggerFactory.getLogger(MusicController.class);

    @Autowired
    private MusicService musicService;



    @Autowired
    private UserService userService;

    @Autowired
    private LyricsService lyricsService;

    @Autowired
    @Qualifier("voskAudioRecognitionService")
    private VoskAudioRecognitionService voskAudioRecognitionService;

    /**
     * 新的音乐主页
     */
    @GetMapping("/new")
    public String musicNewHome(Model model, Authentication authentication) {
        try {
            // 如果用户已登录，可以添加用户相关的数据
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                Optional<User> userOpt = userService.findByUsername(username);
                if (userOpt.isPresent()) {
                    model.addAttribute("user", userOpt.get());
                }
            }

            return "music/music-new";
        } catch (Exception e) {
            logger.error("加载新音乐页面失败", e);
            return "error/500";
        }
    }



    /**
     * 音乐主页
     */
    @GetMapping
    public String musicHome(Model model, Authentication auth,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "12") int size) {
        
        // 获取公开音乐列表
        Page<Music> musicPage = musicService.getPublicMusic(page, size);
        model.addAttribute("musicPage", musicPage);
        
        // 获取热门音乐
        List<Music> popularMusic = musicService.getPopularMusic(6);
        model.addAttribute("popularMusic", popularMusic);
        
        // 如果用户已登录，获取用户音乐
        if (auth != null && auth.isAuthenticated()) {
            User user = userService.findByUsername(auth.getName()).orElse(null);
            if (user != null) {
                List<Music> userMusic = musicService.getUserMusic(user.getId());
                model.addAttribute("userMusic", userMusic);
            }
        }
        
        return "music/index";
    }

    /**
     * 音乐上传页面
     */
    @GetMapping("/upload")
    public String uploadPage(Authentication auth, Model model) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        return "music/upload";
    }

    /**
     * 处理音乐上传
     */
    @PostMapping("/upload")
    public String uploadMusic(@RequestParam("musicFile") MultipartFile musicFile,
                            @RequestParam("title") String title,
                            @RequestParam("artist") String artist,
                            @RequestParam(value = "album", required = false) String album,
                            @RequestParam(value = "coverFile", required = false) MultipartFile coverFile,
                            Authentication auth,
                            RedirectAttributes redirectAttributes) {
        
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        try {
            User user = userService.findByUsername(auth.getName()).orElse(null);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "用户不存在");
                return "redirect:/music/upload";
            }

            if (musicFile.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "请选择音乐文件");
                return "redirect:/music/upload";
            }

            Music music = musicService.uploadMusic(musicFile, title, artist, album, user.getId(), coverFile);

            // 自动启动Vosk歌词识别
            autoGenerateLyrics(music);

            redirectAttributes.addFlashAttribute("success", "音乐上传成功！正在自动生成歌词...");
            return "redirect:/music/play/" + music.getId();

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "上传失败：" + e.getMessage());
            return "redirect:/music/upload";
        }
    }

    /**
     * 音乐播放页面
     */
    @GetMapping("/play/{id}")
    public String playMusic(@PathVariable Long id, Model model, Authentication auth) {
        Optional<Music> musicOpt = musicService.getMusicById(id);
        if (!musicOpt.isPresent()) {
            return "redirect:/music";
        }

        Music music = musicOpt.get();
        model.addAttribute("music", music);

        // 异步增加播放次数，不阻塞页面加载
        musicService.incrementPlayCountAsync(id);

        // 获取用户信息
        if (auth != null && auth.isAuthenticated()) {
            User user = userService.findByUsername(auth.getName()).orElse(null);
            model.addAttribute("currentUser", user);
        }

        // 获取相关音乐推荐（使用缓存）
        List<Music> relatedMusic = musicService.getPopularMusic(6);
        model.addAttribute("relatedMusic", relatedMusic);

        return "music/player";
    }

    /**
     * 我的音乐页面
     */
    @GetMapping("/my")
    public String myMusic(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(auth.getName()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        List<Music> userMusic = musicService.getUserMusic(user.getId());
        model.addAttribute("userMusic", userMusic);
        model.addAttribute("currentUser", user);

        return "music/my-music";
    }

    /**
     * 音乐搜索
     */
    @GetMapping("/search")
    public String searchMusic(@RequestParam("q") String keyword, Model model) {
        List<Music> searchResults = musicService.searchMusic(keyword);
        model.addAttribute("searchResults", searchResults);
        model.addAttribute("keyword", keyword);
        return "music/search";
    }

    /**
     * 删除音乐
     */
    @PostMapping("/delete/{id}")
    @ResponseBody
    public Map<String, Object> deleteMusic(@PathVariable Long id, Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        
        if (auth == null || !auth.isAuthenticated()) {
            response.put("success", false);
            response.put("message", "请先登录");
            return response;
        }

        User user = userService.findByUsername(auth.getName()).orElse(null);
        if (user == null) {
            response.put("success", false);
            response.put("message", "用户不存在");
            return response;
        }

        boolean success = musicService.deleteMusic(id, user.getId());
        if (success) {
            response.put("success", true);
            response.put("message", "删除成功");
        } else {
            response.put("success", false);
            response.put("message", "删除失败或无权限");
        }
        
        return response;
    }

    /**
     * 更新歌词
     */
    @PostMapping("/lyrics/{id}")
    @ResponseBody
    public String updateLyrics(@PathVariable Long id,
                             @RequestParam("lyrics") String lyrics,
                             @RequestParam(value = "lyricsTimestamp", required = false) String lyricsTimestamp,
                             Authentication auth) {
        
        if (auth == null || !auth.isAuthenticated()) {
            return "{\"success\": false, \"message\": \"请先登录\"}";
        }

        User user = userService.findByUsername(auth.getName()).orElse(null);
        if (user == null) {
            return "{\"success\": false, \"message\": \"用户不存在\"}";
        }

        boolean success = musicService.updateLyrics(id, user.getId(), lyrics, lyricsTimestamp);
        if (success) {
            return "{\"success\": true, \"message\": \"歌词更新成功\"}";
        } else {
            return "{\"success\": false, \"message\": \"更新失败或无权限\"}";
        }
    }

    /**
     * 获取音乐歌词数据（API）
     */
    @GetMapping("/api/lyrics/{id}")
    @ResponseBody
    public List<LyricsService.LyricLine> getMusicLyrics(@PathVariable Long id) {
        Optional<Music> musicOpt = musicService.findById(id);
        if (musicOpt.isPresent()) {
            Music music = musicOpt.get();

            // 如果有歌词数据，解析并返回
            if (music.getLyrics() != null && !music.getLyrics().trim().isEmpty()) {
                String format = lyricsService.detectLyricsFormat(music.getLyrics());

                switch (format) {
                    case "lrc":
                        return lyricsService.parseLrcLyrics(music.getLyrics());
                    case "json":
                        return lyricsService.jsonToLyrics(music.getLyrics());
                    case "simple":
                        return lyricsService.parseSimpleLyrics(music.getLyrics());
                    default:
                        return new ArrayList<>(); // 返回空列表而不是示例歌词
                }
            } else {
                // 没有歌词时返回空列表
                return new ArrayList<>();
            }
        }

        return new ArrayList<>();
    }

    /**
     * 歌词编辑器页面
     */
    @GetMapping("/lyrics/edit/{id}")
    public String lyricsEditor(@PathVariable Long id, Model model, Authentication auth) {
        if (auth == null) {
            return "redirect:/login";
        }

        Optional<Music> musicOpt = musicService.findById(id);
        if (musicOpt.isPresent()) {
            Music music = musicOpt.get();

            // 检查权限：只有上传者可以编辑歌词
            Optional<User> userOpt = userService.findByUsername(auth.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (music.getUserId().equals(user.getId()) || user.getRole().equals("ADMIN")) {
                    model.addAttribute("music", music);
                    return "music/lyrics-editor";
                }
            }
        }

        return "redirect:/music";
    }

    /**
     * 保存歌词（API）
     */
    @PostMapping("/api/lyrics/{id}")
    @ResponseBody
    public String saveMusicLyrics(@PathVariable Long id, @RequestBody String requestBody, Authentication auth) {
        if (auth == null) {
            return "{\"success\": false, \"message\": \"请先登录\"}";
        }

        try {
            // 解析请求体
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(requestBody);
            String lyrics = jsonNode.get("lyrics").asText();
            String format = jsonNode.get("format").asText();

            Optional<User> userOpt = userService.findByUsername(auth.getName());
            if (!userOpt.isPresent()) {
                return "{\"success\": false, \"message\": \"用户不存在\"}";
            }

            User user = userOpt.get();

            Optional<Music> musicOpt = musicService.findById(id);
            if (musicOpt.isPresent()) {
                Music music = musicOpt.get();

                // 检查权限
                if (!music.getUserId().equals(user.getId()) && !user.getRole().equals("ADMIN")) {
                    return "{\"success\": false, \"message\": \"无权限编辑此音乐的歌词\"}";
                }

                // 保存歌词
                music.setLyrics(lyrics);
                musicService.save(music);

                return "{\"success\": true, \"message\": \"歌词保存成功\"}";
            } else {
                return "{\"success\": false, \"message\": \"音乐不存在\"}";
            }
        } catch (Exception e) {
            return "{\"success\": false, \"message\": \"保存失败：" + e.getMessage() + "\"}";
        }
    }

    /**
     * 使用Vosk自动识别歌词
     */
    @PostMapping("/api/vosk-generate-lyrics/{id}")
    @ResponseBody
    public String voskGenerateLyrics(@PathVariable Long id, Authentication auth) {
        if (auth == null) {
            return "{\"success\": false, \"message\": \"请先登录\"}";
        }

        try {
            Optional<Music> musicOpt = musicService.findById(id);
            if (!musicOpt.isPresent()) {
                return "{\"success\": false, \"message\": \"音乐不存在\"}";
            }

            Music music = musicOpt.get();
            Optional<User> userOpt = userService.findByUsername(auth.getName());
            if (!userOpt.isPresent()) {
                return "{\"success\": false, \"message\": \"用户不存在\"}";
            }

            User user = userOpt.get();
            if (!music.getUserId().equals(user.getId()) && !user.getRole().equals("ADMIN")) {
                return "{\"success\": false, \"message\": \"无权限操作此音乐\"}";
            }

            logger.info("开始Vosk识别: " + music.getTitle());

            // 异步处理音频识别
            voskAudioRecognitionService.processAudioToLyricsAsync(music.getFilePath())
                .thenAccept(lyrics -> {
                    try {
                        if (!lyrics.isEmpty()) {
                            // 转换为LRC格式
                            String lrcLyrics = convertToLrcFormat(lyrics);
                            music.setLyrics(lrcLyrics);
                            musicService.save(music);

                            System.out.println("Vosk自动生成歌词成功: " + music.getTitle());
                        } else {
                            System.out.println("Vosk识别结果为空: " + music.getTitle());
                        }
                    } catch (Exception e) {
                        System.err.println("保存Vosk识别歌词失败: " + e.getMessage());
                    }
                })
                .exceptionally(throwable -> {
                    System.err.println("Vosk自动生成歌词失败: " + throwable.getMessage());
                    return null;
                });

            return "{\"success\": true, \"message\": \"Vosk歌词识别任务已启动，请稍后刷新查看结果\"}";

        } catch (Exception e) {
            return "{\"success\": false, \"message\": \"处理失败：" + e.getMessage() + "\"}";
        }
    }

    /**
     * 测试页面
     */
    @GetMapping("/test")
    public String test() {
        return "music/test";
    }

    /**
     * 检查Vosk服务状态
     */
    @GetMapping("/api/vosk-status")
    @ResponseBody
    public String getVoskStatus() {
        boolean available = voskAudioRecognitionService.isVoskAvailable();
        String statusInfo = voskAudioRecognitionService.getVoskStatusInfo();

        return "{\"available\": " + available +
               ", \"message\": \"" + (available ? "Vosk服务可用" : "Vosk服务不可用") + "\"" +
               ", \"details\": \"" + statusInfo.replace("\n", "\\n").replace("\"", "\\\"") + "\"}";
    }

    /**
     * 清理模拟歌词数据
     */
    @PostMapping("/api/clear-mock-lyrics")
    @ResponseBody
    public String clearMockLyrics(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "{\"success\": false, \"message\": \"请先登录\"}";
        }

        try {
            User user = userService.findByUsername(auth.getName()).orElse(null);
            if (user == null) {
                return "{\"success\": false, \"message\": \"用户不存在\"}";
            }

            List<Music> userMusic = musicService.getUserMusic(user.getId());
            int clearedCount = 0;

            for (Music music : userMusic) {
                if (music.getLyrics() != null &&
                    (music.getLyrics().contains("模拟") ||
                     music.getLyrics().contains("示例") ||
                     music.getLyrics().contains("AI识别") ||
                     music.getLyrics().contains("请安装") ||
                     music.getLyrics().contains("音频识别失败"))) {

                    music.setLyrics(null);
                    musicService.save(music);
                    clearedCount++;
                }
            }

            return "{\"success\": true, \"message\": \"已清理 " + clearedCount + " 首歌曲的模拟歌词\", \"count\": " + clearedCount + "}";

        } catch (Exception e) {
            logger.error("清理模拟歌词失败: " + e.getMessage());
            return "{\"success\": false, \"message\": \"清理失败: " + e.getMessage() + "\"}";
        }
    }

    /**
     * 自动生成歌词（上传后调用）
     */
    private void autoGenerateLyrics(Music music) {
        try {
            logger.info("开始自动生成歌词: " + music.getTitle());

            // 转换Web路径为文件系统路径
            String webPath = music.getFilePath(); // 例如: /uploads/music/xxx.mp3
            String filePath = webPath.startsWith("/") ? webPath.substring(1) : webPath; // 去掉开头的"/"
            File audioFile = new File(filePath);

            logger.info("音频文件路径: {}", audioFile.getAbsolutePath());
            logger.info("文件是否存在: {}", audioFile.exists());

            // 异步处理音频识别 - 使用绝对路径
            voskAudioRecognitionService.processAudioToLyricsAsync(audioFile.getAbsolutePath())
                .thenAccept(lyrics -> {
                    try {
                        if (!lyrics.isEmpty()) {
                            // 直接使用识别结果，不管准确性如何
                            String lrcLyrics = convertToLrcFormat(lyrics);
                            music.setLyrics(lrcLyrics);
                            musicService.save(music);
                            logger.info("自动生成歌词完成: " + music.getTitle());
                        } else {
                            logger.warn("Vosk识别结果为空: " + music.getTitle());
                        }
                    } catch (Exception e) {
                        logger.error("保存自动生成歌词失败: " + e.getMessage());
                    }
                })
                .exceptionally(throwable -> {
                    logger.error("自动生成歌词失败: " + throwable.getMessage());
                    return null;
                });

        } catch (Exception e) {
            logger.error("启动自动歌词生成失败: " + e.getMessage());
        }
    }

    /**
     * 转换为LRC格式
     */
    private String convertToLrcFormat(List<LyricsService.LyricLine> lyrics) {
        StringBuilder lrc = new StringBuilder();
        for (LyricsService.LyricLine lyric : lyrics) {
            int minutes = (int) (lyric.getTime() / 60);
            int seconds = (int) (lyric.getTime() % 60);
            int centiseconds = (int) ((lyric.getTime() % 1) * 100);

            lrc.append(String.format("[%02d:%02d.%02d]%s\n",
                minutes, seconds, centiseconds, lyric.getText()));
        }
        return lrc.toString();
    }


}
