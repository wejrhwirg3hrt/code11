package org.example.controller.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.entity.Music;
import org.example.entity.User;
import org.example.service.MusicService;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/music")
@CrossOrigin(originPatterns = "*")
public class MusicApiController {

    @Value("${app.music.path:./music}")
    private String musicPath;

    @Autowired
    private MusicService musicService;

    @Autowired
    private UserService userService;

    @GetMapping("/library")
    public ResponseEntity<List<Map<String, Object>>> getMusicLibrary() {
        List<Map<String, Object>> musicList = new ArrayList<>();
        
        try {
            // 从数据库获取音乐列表，而不是从文件系统
            List<Music> dbMusicList = musicService.getAllPublicMusic();
            
            for (Music music : dbMusicList) {
                Map<String, Object> track = new HashMap<>();
                track.put("id", music.getId());
                track.put("title", music.getTitle());
                track.put("artist", music.getArtist());
                track.put("album", music.getAlbum());
                track.put("url", music.getFilePath());
                track.put("duration", music.getDuration() != null ? music.getDuration() : 0);
                track.put("playCount", music.getPlayCount());
                track.put("uploadTime", music.getUploadTime());
                musicList.add(track);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return ResponseEntity.ok(musicList);
    }

    @GetMapping("/list")
    public ResponseEntity<List<Map<String, Object>>> getMusicList() {
        List<Map<String, Object>> musicList = new ArrayList<>();
        
        try {
            System.out.println("🎵 开始获取音乐列表...");
            
            // 从数据库获取音乐列表
            List<Music> dbMusicList = musicService.getAllPublicMusic();
            System.out.println("🎵 从数据库获取到 " + dbMusicList.size() + " 首音乐");
            
            for (Music music : dbMusicList) {
                Map<String, Object> track = new HashMap<>();
                track.put("id", music.getId());
                track.put("title", music.getTitle());
                track.put("artist", music.getArtist());
                track.put("album", music.getAlbum());
                track.put("url", music.getFilePath());
                track.put("duration", music.getDuration() != null ? music.getDuration() : 0);
                track.put("playCount", music.getPlayCount());
                track.put("uploadTime", music.getUploadTime());
                track.put("userId", music.getUserId());
                track.put("isPublic", music.getIsPublic());
                musicList.add(track);
            }
            
            System.out.println("🎵 成功构建音乐列表，包含 " + musicList.size() + " 首音乐");
            
        } catch (Exception e) {
            System.err.println("❌ 获取音乐列表失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
        
        return ResponseEntity.ok(musicList);
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadMusic(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "artist", required = false) String artist,
            @RequestParam(value = "album", required = false) String album,
            Authentication auth) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "文件为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 检查用户登录状态
            if (auth == null || !auth.isAuthenticated()) {
                response.put("success", false);
                response.put("message", "请先登录");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 获取用户信息
            User user = userService.findByUsername(auth.getName()).orElse(null);
            if (user == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.badRequest().body(response);
            }
            
            String fileName = file.getOriginalFilename();
            String fileExtension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
            
            if (!Arrays.asList(".mp3", ".wav", ".ogg", ".flac", ".m4a").contains(fileExtension)) {
                response.put("success", false);
                response.put("message", "不支持的音频格式");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 使用MusicService上传音乐，这会同时保存文件和数据库记录
            String finalTitle = title != null ? title : fileName.replaceFirst("[.][^.]+$", "");
            String finalArtist = artist != null ? artist : "未知艺术家";
            String finalAlbum = album != null ? album : "未知专辑";
            
            Music music = musicService.uploadMusic(file, finalTitle, finalArtist, finalAlbum, user.getId(), null);
            
            response.put("success", true);
            response.put("message", "音乐上传成功");
            response.put("musicId", music.getId());
            response.put("title", music.getTitle());
            response.put("artist", music.getArtist());
            response.put("url", music.getFilePath());
            
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "上传失败: " + e.getMessage());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "处理失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteMusic(@PathVariable Long id, Authentication auth) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // 检查用户登录状态
            if (auth == null || !auth.isAuthenticated()) {
                response.put("success", "false");
                response.put("message", "请先登录");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 获取用户信息
            User user = userService.findByUsername(auth.getName()).orElse(null);
            if (user == null) {
                response.put("success", "false");
                response.put("message", "用户不存在");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 使用MusicService删除音乐
            boolean success = musicService.deleteMusic(id, user.getId());
            if (success) {
                response.put("success", "true");
                response.put("message", "音乐删除成功");
            } else {
                response.put("success", "false");
                response.put("message", "删除失败或无权限");
            }
        } catch (Exception e) {
            response.put("success", "false");
            response.put("message", "删除失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}