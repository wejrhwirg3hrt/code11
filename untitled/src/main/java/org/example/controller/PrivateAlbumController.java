package org.example.controller;

import org.example.entity.PrivateAlbum;
import org.example.entity.PrivatePhoto;
import org.example.entity.User;
import org.example.service.PrivateAlbumService;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/private-album")
public class PrivateAlbumController {

    @Autowired
    private PrivateAlbumService albumService;

    @Autowired
    private UserService userService;

    /**
     * 密码测试端点 - 仅用于调试
     */
    @GetMapping("/test-password/{albumId}")
    @ResponseBody
    public String testPassword(@PathVariable Long albumId, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "用户未登录";
        }

        User user = userService.findByUsername(auth.getName()).orElse(null);
        if (user == null) {
            return "用户不存在";
        }

        Optional<PrivateAlbum> albumOpt = albumService.getAlbum(albumId, user.getId());
        if (!albumOpt.isPresent()) {
            return "相册不存在";
        }

        PrivateAlbum album = albumOpt.get();
        StringBuilder result = new StringBuilder();
        result.append("相册信息:\n");
        result.append("ID: ").append(album.getId()).append("\n");
        result.append("名称: ").append(album.getAlbumName()).append("\n");
        result.append("用户ID: ").append(album.getUserId()).append("\n");
        result.append("密码长度: ").append(album.getPassword() != null ? album.getPassword().length() : "null").append("\n");
        result.append("密码前缀: ").append(album.getPassword() != null ? album.getPassword().substring(0, Math.min(20, album.getPassword().length())) : "null").append("\n");

        return result.toString();
    }

    /**
     * 重置密码端点 - 仅用于调试
     */
    @PostMapping("/reset-password/{albumId}")
    @ResponseBody
    public String resetPassword(@PathVariable Long albumId,
                               @RequestParam("newPassword") String newPassword,
                               Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "用户未登录";
        }

        User user = userService.findByUsername(auth.getName()).orElse(null);
        if (user == null) {
            return "用户不存在";
        }

        boolean success = albumService.resetAlbumPassword(albumId, user.getId(), newPassword);
        if (success) {
            return "密码重置成功！新密码: [" + newPassword + "]";
        } else {
            return "密码重置失败，相册不存在或无权限";
        }
    }

    /**
     * 私密相册主页
     */
    @GetMapping
    public String albumHome(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(auth.getName()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        List<PrivateAlbum> albums = albumService.getUserAlbums(user.getId());
        model.addAttribute("albums", albums);
        model.addAttribute("currentUser", user);

        return "private-album/index";
    }

    /**
     * 创建相册页面
     */
    @GetMapping("/create")
    public String createAlbumPage(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        return "private-album/create";
    }

    /**
     * 处理创建相册
     */
    @PostMapping("/create")
    public String createAlbum(@RequestParam("albumName") String albumName,
                            @RequestParam("password") String password,
                            @RequestParam(value = "description", required = false) String description,
                            Authentication auth,
                            RedirectAttributes redirectAttributes,
                            HttpSession session) {

        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        try {
            User user = userService.findByUsername(auth.getName()).orElse(null);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "用户不存在");
                return "redirect:/private-album/create";
            }

            PrivateAlbum album = albumService.createAlbum(user.getId(), albumName, password, description);

            // 创建成功后，自动将该相册标记为已验证
            @SuppressWarnings("unchecked")
            Set<Long> verifiedAlbums = (Set<Long>) session.getAttribute("verifiedAlbums");
            if (verifiedAlbums == null) {
                verifiedAlbums = new HashSet<>();
            }
            verifiedAlbums.add(album.getId());
            session.setAttribute("verifiedAlbums", verifiedAlbums);

            redirectAttributes.addFlashAttribute("success", "相册创建成功！");
            return "redirect:/private-album/" + album.getId();

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/private-album/create";
        }
    }

    /**
     * 相册详情页面（需要密码验证）
     */
    @GetMapping("/{albumId}")
    public String albumDetail(@PathVariable Long albumId, Model model, Authentication auth, HttpSession session) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(auth.getName()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        Optional<PrivateAlbum> albumOpt = albumService.getAlbum(albumId, user.getId());
        if (!albumOpt.isPresent()) {
            return "redirect:/private-album";
        }

        // 检查是否已经验证过密码
        @SuppressWarnings("unchecked")
        Set<Long> verifiedAlbums = (Set<Long>) session.getAttribute("verifiedAlbums");
        if (verifiedAlbums != null && verifiedAlbums.contains(albumId)) {
            // 已验证，直接显示相册内容
            try {
                List<PrivatePhoto> photos = albumService.getAlbumPhotos(albumId, user.getId());
                model.addAttribute("album", albumOpt.get());
                model.addAttribute("photos", photos);
                return "private-album/detail";
            } catch (Exception e) {
                System.out.println("获取相册详情时出错: " + e.getMessage());
                e.printStackTrace();
                // 如果获取照片失败，仍然显示相册页面，但照片列表为空
                model.addAttribute("album", albumOpt.get());
                model.addAttribute("photos", new ArrayList<>());
                model.addAttribute("error", "获取照片列表时出现错误，请稍后重试");
                return "private-album/detail";
            }
        }

        // 未验证，显示密码页面
        model.addAttribute("album", albumOpt.get());
        model.addAttribute("albumId", albumId);
        return "private-album/password";
    }

    /**
     * 验证相册密码
     */
    @PostMapping("/{albumId}/verify")
    public String verifyPassword(@PathVariable Long albumId,
                               @RequestParam("password") String password,
                               Authentication auth,
                               Model model,
                               RedirectAttributes redirectAttributes,
                               HttpSession session) {

        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(auth.getName()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        // 调试信息
        System.out.println("=== 控制器密码验证调试 ===");
        System.out.println("接收到的密码: [" + password + "]");
        System.out.println("密码长度: " + (password != null ? password.length() : "null"));
        System.out.println("相册ID: " + albumId);
        System.out.println("用户: " + (user != null ? user.getUsername() : "null"));
        System.out.println("用户ID: " + (user != null ? user.getId() : "null"));

        boolean isValid = albumService.verifyAlbumPassword(albumId, user.getId(), password);
        System.out.println("验证结果: " + isValid);
        System.out.println("==============================");

        if (isValid) {
            // 密码正确，记录到会话中
            @SuppressWarnings("unchecked")
            Set<Long> verifiedAlbums = (Set<Long>) session.getAttribute("verifiedAlbums");
            if (verifiedAlbums == null) {
                verifiedAlbums = new HashSet<>();
            }
            verifiedAlbums.add(albumId);
            session.setAttribute("verifiedAlbums", verifiedAlbums);

            // 重定向到相册详情页面
            return "redirect:/private-album/" + albumId;
        }

        redirectAttributes.addFlashAttribute("error", "密码错误");
        return "redirect:/private-album/" + albumId;
    }

    /**
     * 上传照片页面
     */
    @GetMapping("/{albumId}/upload")
    public String uploadPhotoPage(@PathVariable Long albumId, Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(auth.getName()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        Optional<PrivateAlbum> albumOpt = albumService.getAlbum(albumId, user.getId());
        if (!albumOpt.isPresent()) {
            return "redirect:/private-album";
        }

        model.addAttribute("album", albumOpt.get());
        return "private-album/upload";
    }

    /**
     * 处理照片上传
     */
    @PostMapping("/{albumId}/upload")
    public String uploadPhoto(@PathVariable Long albumId,
                            @RequestParam("photoFiles") MultipartFile[] photoFiles,
                            @RequestParam(value = "description", required = false) String description,
                            Authentication auth,
                            RedirectAttributes redirectAttributes,
                            HttpSession session) {

        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        try {
            User user = userService.findByUsername(auth.getName()).orElse(null);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "用户不存在");
                return "redirect:/private-album/" + albumId + "/upload";
            }

            int uploadCount = 0;
            for (MultipartFile photoFile : photoFiles) {
                if (!photoFile.isEmpty()) {
                    albumService.uploadPhoto(albumId, user.getId(), photoFile, description);
                    uploadCount++;
                }
            }

            if (uploadCount > 0) {
                // 确保相册在 session 中标记为已验证
                @SuppressWarnings("unchecked")
                Set<Long> verifiedAlbums = (Set<Long>) session.getAttribute("verifiedAlbums");
                if (verifiedAlbums == null) {
                    verifiedAlbums = new HashSet<>();
                }
                verifiedAlbums.add(albumId);
                session.setAttribute("verifiedAlbums", verifiedAlbums);

                redirectAttributes.addFlashAttribute("success", "成功上传 " + uploadCount + " 张照片！");
            } else {
                redirectAttributes.addFlashAttribute("error", "请选择要上传的照片");
            }

            return "redirect:/private-album/" + albumId;

        } catch (IOException | RuntimeException e) {
            System.out.println("上传照片时出错: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "上传失败：" + e.getMessage());
            return "redirect:/private-album/" + albumId + "/upload";
        }
    }

    /**
     * 删除照片
     */
    @PostMapping("/photo/delete/{photoId}")
    @ResponseBody
    public String deletePhoto(@PathVariable Long photoId, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "{\"success\": false, \"message\": \"请先登录\"}";
        }

        User user = userService.findByUsername(auth.getName()).orElse(null);
        if (user == null) {
            return "{\"success\": false, \"message\": \"用户不存在\"}";
        }

        boolean success = albumService.deletePhoto(photoId, user.getId());
        if (success) {
            return "{\"success\": true, \"message\": \"删除成功\"}";
        } else {
            return "{\"success\": false, \"message\": \"删除失败或无权限\"}";
        }
    }

    /**
     * 删除相册
     */
    @PostMapping("/delete/{albumId}")
    @ResponseBody
    public String deleteAlbum(@PathVariable Long albumId, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "{\"success\": false, \"message\": \"请先登录\"}";
        }

        User user = userService.findByUsername(auth.getName()).orElse(null);
        if (user == null) {
            return "{\"success\": false, \"message\": \"用户不存在\"}";
        }

        boolean success = albumService.deleteAlbum(albumId, user.getId());
        if (success) {
            return "{\"success\": true, \"message\": \"相册删除成功\"}";
        } else {
            return "{\"success\": false, \"message\": \"删除失败或无权限\"}";
        }
    }
}
