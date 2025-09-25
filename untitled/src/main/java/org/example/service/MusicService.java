package org.example.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.example.entity.Music;
import org.example.repository.MusicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = false)
public class MusicService {

    @Autowired
    private MusicRepository musicRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Value("${app.upload.path:E:/code11/uploads}")
    private String baseUploadPath;
    
    private final String MUSIC_UPLOAD_DIR = "music";
    private final String COVER_UPLOAD_DIR = "covers";

    /**
     * 上传音乐文件
     */
    @CacheEvict(value = {"userMusic", "publicMusic", "allPublicMusic", "musicSearch", "popularMusic", "musicStats"}, allEntries = true)
    public Music uploadMusic(MultipartFile file, String title, String artist, String album, 
                           Long userId, MultipartFile coverFile) throws IOException {
        
        // 保存音乐文件
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        
        // 使用绝对路径创建目录
        Path uploadPath = Paths.get(baseUploadPath, MUSIC_UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // 保存文件到绝对路径
        Path targetPath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), targetPath);
        
        // 生成Web访问路径
        String filePath = "/uploads/" + MUSIC_UPLOAD_DIR + "/" + fileName;
        
        // 保存封面图片（如果有）
        String coverPath = null;
        if (coverFile != null && !coverFile.isEmpty()) {
            String coverFileName = UUID.randomUUID().toString() + "_" + coverFile.getOriginalFilename();
            
            // 使用绝对路径创建封面目录
            Path coverUploadPath = Paths.get(baseUploadPath, COVER_UPLOAD_DIR);
            if (!Files.exists(coverUploadPath)) {
                Files.createDirectories(coverUploadPath);
            }
            
            Path coverTargetPath = coverUploadPath.resolve(coverFileName);
            Files.copy(coverFile.getInputStream(), coverTargetPath);
            
            // 生成Web访问路径
            coverPath = "/uploads/" + COVER_UPLOAD_DIR + "/" + coverFileName;
        }
        
        // 创建音乐记录
        Music music = new Music();
        music.setTitle(title);
        music.setArtist(artist);
        music.setAlbum(album);
        music.setFileName(fileName);
        // 保存Web访问路径，不要重复添加斜杠
        music.setFilePath(filePath);
        music.setCoverPath(coverPath);
        music.setUserId(userId);
        music.setUploadTime(LocalDateTime.now());
        music.setPlayCount(0L);
        music.setIsPublic(true);
        
        // 尝试提取音频时长（这里简化处理，实际可以使用FFmpeg等工具）
        // music.setDuration(extractDuration(targetPath.toString()));
        
        System.out.println("🎵 准备保存音乐到数据库:");
        System.out.println("  - 标题: " + title);
        System.out.println("  - 艺术家: " + artist);
        System.out.println("  - 专辑: " + album);
        System.out.println("  - 文件名: " + fileName);
        System.out.println("  - 文件路径: " + filePath);
        System.out.println("  - 用户ID: " + userId);
        System.out.println("  - 上传时间: " + LocalDateTime.now());
        
        try {
            Music savedMusic = musicRepository.save(music);
            System.out.println("✅ 音乐保存成功! ID: " + savedMusic.getId());
            return savedMusic;
        } catch (Exception e) {
            System.err.println("❌ 音乐保存失败: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 获取用户的音乐列表
     */
    @Cacheable(value = "userMusic", key = "#userId")
    public List<Music> getUserMusic(Long userId) {
        return musicRepository.findByUserIdOrderByUploadTimeDesc(userId);
    }

    /**
     * 获取公开的音乐列表（带缓存）
     */
    @Cacheable(value = "publicMusic", key = "#page + '_' + #size")
    public Page<Music> getPublicMusic(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return musicRepository.findByIsPublicTrueOrderByUploadTimeDesc(pageable);
    }

    /**
     * 获取所有公开的音乐列表（不分页，带缓存）
     */
    @Cacheable(value = "allPublicMusic")
    public List<Music> getAllPublicMusic() {
        return musicRepository.findByIsPublicTrueOrderByUploadTimeDesc();
    }

    /**
     * 根据ID获取音乐（带缓存）
     */
    @Cacheable(value = "musicById", key = "#id")
    public Optional<Music> getMusicById(Long id) {
        return musicRepository.findById(id);
    }

    /**
     * 增加播放次数（异步处理）
     */
    @Async
    public CompletableFuture<Void> incrementPlayCountAsync(Long musicId) {
        try {
            Optional<Music> musicOpt = musicRepository.findById(musicId);
            if (musicOpt.isPresent()) {
                Music music = musicOpt.get();
                music.setPlayCount(music.getPlayCount() + 1);
                musicRepository.save(music);
            }
        } catch (Exception e) {
            // 记录错误但不影响主流程
            System.err.println("增加播放次数失败: " + e.getMessage());
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * 增加播放次数（同步版本，保持兼容性）
     */
    public void incrementPlayCount(Long musicId) {
        Optional<Music> musicOpt = musicRepository.findById(musicId);
        if (musicOpt.isPresent()) {
            Music music = musicOpt.get();
            music.setPlayCount(music.getPlayCount() + 1);
            musicRepository.save(music);
        }
    }

    /**
     * 搜索音乐（带缓存）
     */
    @Cacheable(value = "musicSearch", key = "#keyword")
    public List<Music> searchMusic(String keyword) {
        List<Music> titleResults = musicRepository.searchByTitle(keyword);
        List<Music> artistResults = musicRepository.searchByArtist(keyword);
        
        // 合并结果并去重
        titleResults.addAll(artistResults);
        return titleResults.stream().distinct().toList();
    }

    /**
     * 获取热门音乐（带缓存）
     */
    @Cacheable(value = "popularMusic", key = "#limit")
    public List<Music> getPopularMusic(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return musicRepository.findPopularMusic(pageable);
    }

    /**
     * 删除音乐
     */
    @CacheEvict(value = {"userMusic", "publicMusic", "allPublicMusic", "musicSearch", "popularMusic", "musicStats"}, allEntries = true)
    public boolean deleteMusic(Long musicId, Long userId) {
        Optional<Music> musicOpt = musicRepository.findByIdAndUserId(musicId, userId);
        if (musicOpt.isPresent()) {
            Music music = musicOpt.get();
            
            // 删除文件 - 将Web路径转换为实际文件路径
            try {
                String filePath = music.getFilePath();
                if (filePath != null && filePath.startsWith("/uploads/")) {
                    // 将Web路径转换为实际文件路径
                    String relativePath = filePath.substring(8); // 去掉 "/uploads/"
                    Path actualFilePath = Paths.get(baseUploadPath, relativePath);
                    System.out.println("🗑️ 删除文件: " + actualFilePath);
                    Files.deleteIfExists(actualFilePath);
                }
                
                String coverPath = music.getCoverPath();
                if (coverPath != null && coverPath.startsWith("/uploads/")) {
                    // 将Web路径转换为实际文件路径
                    String relativePath = coverPath.substring(8); // 去掉 "/uploads/"
                    Path actualCoverPath = Paths.get(baseUploadPath, relativePath);
                    System.out.println("🗑️ 删除封面: " + actualCoverPath);
                    Files.deleteIfExists(actualCoverPath);
                }
            } catch (IOException e) {
                // 记录日志但不阻止删除数据库记录
                System.err.println("❌ 删除文件失败: " + e.getMessage());
                e.printStackTrace();
            }
            
            // 删除数据库记录
            musicRepository.delete(music);
            System.out.println("✅ 音乐删除成功: " + music.getTitle());
            return true;
        }
        return false;
    }

    /**
     * 更新歌词
     */
    public boolean updateLyrics(Long musicId, Long userId, String lyrics, String lyricsTimestamp) {
        Optional<Music> musicOpt = musicRepository.findByIdAndUserId(musicId, userId);
        if (musicOpt.isPresent()) {
            Music music = musicOpt.get();
            music.setLyrics(lyrics);
            music.setLyricsTimestamp(lyricsTimestamp);
            musicRepository.save(music);
            return true;
        }
        return false;
    }

    /**
     * 根据ID查找音乐
     */
    public Optional<Music> findById(Long id) {
        return musicRepository.findById(id);
    }

    /**
     * 保存音乐
     */
    public Music save(Music music) {
        return musicRepository.save(music);
    }

    /**
     * 获取音乐统计信息（带缓存）
     */
    @Cacheable(value = "musicStats")
    public Map<String, Object> getMusicStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            long totalMusic = musicRepository.count();
            long publicMusic = musicRepository.countByIsPublicTrue();
            
            stats.put("totalMusic", totalMusic);
            stats.put("publicMusic", publicMusic);
            
            // 获取最近上传的音乐数量
            LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
            long recentUploads = musicRepository.countByUploadTimeAfter(oneWeekAgo);
            stats.put("recentUploads", recentUploads);
            
        } catch (Exception e) {
            stats.put("error", e.getMessage());
        }
        
        return stats;
    }

    /**
     * 预加载热门音乐（用于首页优化）
     */
    @Async
    public CompletableFuture<List<Music>> preloadPopularMusic() {
        return CompletableFuture.completedFuture(getPopularMusic(6));
    }

    /**
     * 预加载最新音乐（用于首页优化）
     */
    @Async
    public CompletableFuture<Page<Music>> preloadLatestMusic() {
        return CompletableFuture.completedFuture(getPublicMusic(0, 12));
    }
}
