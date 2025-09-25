package org.example.controller;

import org.example.entity.User;
import org.example.entity.VoiceClone;
import org.example.service.VoiceCloneService;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/voice-clone")
public class VoiceCloneController {

    @Autowired
    private VoiceCloneService voiceCloneService;

    @Autowired
    private UserService userService;

    /**
     * 语音克隆主页
     */
    @GetMapping
    public String voiceCloneHome(Model model, Authentication auth) {
        // 获取公开的语音模型
        List<VoiceClone> publicVoiceClones = voiceCloneService.getPublicVoiceClones();
        model.addAttribute("publicVoiceClones", publicVoiceClones);

        // 如果用户已登录，获取用户的语音模型
        if (auth != null && auth.isAuthenticated()) {
            User user = userService.findByUsername(auth.getName()).orElse(null);
            if (user != null) {
                List<VoiceClone> userVoiceClones = voiceCloneService.getUserVoiceClones(user.getId());
                model.addAttribute("userVoiceClones", userVoiceClones);
                model.addAttribute("currentUser", user);
            }
        }

        return "voice-clone/index";
    }

    /**
     * 创建语音克隆页面
     */
    @GetMapping("/create")
    public String createPage(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        return "voice-clone/create";
    }

    /**
     * 处理创建语音克隆
     */
    @PostMapping("/create")
    public String createVoiceClone(@RequestParam("videoFile") MultipartFile videoFile,
                                 @RequestParam("voiceName") String voiceName,
                                 @RequestParam(value = "description", required = false) String description,
                                 @RequestParam(value = "isPublic", defaultValue = "false") Boolean isPublic,
                                 Authentication auth,
                                 RedirectAttributes redirectAttributes) {
        
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        try {
            User user = userService.findByUsername(auth.getName()).orElse(null);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "用户不存在");
                return "redirect:/voice-clone/create";
            }

            if (videoFile.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "请选择视频文件");
                return "redirect:/voice-clone/create";
            }

            VoiceClone voiceClone = voiceCloneService.createVoiceClone(user.getId(), videoFile, voiceName, description, isPublic);
            redirectAttributes.addFlashAttribute("success", "语音模型创建成功！正在处理中...");
            return "redirect:/voice-clone/" + voiceClone.getId();

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "创建失败：" + e.getMessage());
            return "redirect:/voice-clone/create";
        }
    }

    /**
     * 语音模型详情页面
     */
    @GetMapping("/{voiceCloneId}")
    public String voiceCloneDetail(@PathVariable Long voiceCloneId, Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(auth.getName()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        Optional<VoiceClone> voiceCloneOpt = voiceCloneService.getVoiceClone(voiceCloneId, user.getId());
        if (!voiceCloneOpt.isPresent()) {
            return "redirect:/voice-clone";
        }

        model.addAttribute("voiceClone", voiceCloneOpt.get());
        model.addAttribute("currentUser", user);

        return "voice-clone/detail";
    }

    /**
     * TTS生成页面
     */
    @GetMapping("/{voiceCloneId}/tts")
    public String ttsPage(@PathVariable Long voiceCloneId, Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(auth.getName()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        Optional<VoiceClone> voiceCloneOpt = voiceCloneService.getVoiceClone(voiceCloneId, user.getId());
        if (!voiceCloneOpt.isPresent()) {
            return "redirect:/voice-clone";
        }

        model.addAttribute("voiceClone", voiceCloneOpt.get());
        return "voice-clone/tts";
    }

    /**
     * 生成TTS音频
     */
    @PostMapping("/{voiceCloneId}/generate-tts")
    @ResponseBody
    public String generateTTS(@PathVariable Long voiceCloneId,
                            @RequestParam("text") String text,
                            @RequestParam(value = "emotion", defaultValue = "neutral") String emotion,
                            Authentication auth) {
        
        if (auth == null || !auth.isAuthenticated()) {
            return "{\"success\": false, \"message\": \"请先登录\"}";
        }

        User user = userService.findByUsername(auth.getName()).orElse(null);
        if (user == null) {
            return "{\"success\": false, \"message\": \"用户不存在\"}";
        }

        try {
            String audioPath = voiceCloneService.generateTTS(voiceCloneId, user.getId(), text, emotion);
            return "{\"success\": true, \"message\": \"生成成功\", \"audioPath\": \"" + audioPath + "\"}";
        } catch (IOException | RuntimeException e) {
            return "{\"success\": false, \"message\": \"" + e.getMessage() + "\"}";
        }
    }

    /**
     * 下载生成的音频文件
     */
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadAudio(@RequestParam("path") String audioPath, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Path filePath = Paths.get(audioPath);
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(filePath);
            String filename = filePath.getFileName().toString();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 我的语音模型页面
     */
    @GetMapping("/my")
    public String myVoiceClones(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(auth.getName()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        List<VoiceClone> userVoiceClones = voiceCloneService.getUserVoiceClones(user.getId());
        model.addAttribute("userVoiceClones", userVoiceClones);
        model.addAttribute("currentUser", user);

        return "voice-clone/my-voices";
    }

    /**
     * 搜索语音模型
     */
    @GetMapping("/search")
    public String searchVoiceClones(@RequestParam("q") String keyword, Model model, Authentication auth) {
        Long userId = null;
        if (auth != null && auth.isAuthenticated()) {
            User user = userService.findByUsername(auth.getName()).orElse(null);
            if (user != null) {
                userId = user.getId();
                model.addAttribute("currentUser", user);
            }
        }

        List<VoiceClone> searchResults = voiceCloneService.searchVoiceClones(keyword, userId);
        model.addAttribute("searchResults", searchResults);
        model.addAttribute("keyword", keyword);

        return "voice-clone/search";
    }

    /**
     * 删除语音模型
     */
    @PostMapping("/delete/{voiceCloneId}")
    @ResponseBody
    public String deleteVoiceClone(@PathVariable Long voiceCloneId, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "{\"success\": false, \"message\": \"请先登录\"}";
        }

        User user = userService.findByUsername(auth.getName()).orElse(null);
        if (user == null) {
            return "{\"success\": false, \"message\": \"用户不存在\"}";
        }

        boolean success = voiceCloneService.deleteVoiceClone(voiceCloneId, user.getId());
        if (success) {
            return "{\"success\": true, \"message\": \"删除成功\"}";
        } else {
            return "{\"success\": false, \"message\": \"删除失败或无权限\"}";
        }
    }
}
