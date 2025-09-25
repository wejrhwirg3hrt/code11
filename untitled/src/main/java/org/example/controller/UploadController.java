package org.example.controller;

import org.example.dto.VideoContentBlock;
import org.example.entity.Tag;
import org.example.entity.User;
import org.example.entity.Video;
import org.example.entity.VideoContent;
import org.example.entity.Category;
import org.example.service.FileUploadService;
import org.example.service.TagService;
import org.example.service.UserService;
import org.example.service.VideoService;
import org.example.service.VideoContentService;
import org.example.service.UserLogService;
import org.example.service.ThumbnailGenerationService;
import org.example.service.CategoryService;
import org.example.service.AchievementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Arrays;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Controller
public class UploadController {

    @Autowired
    private VideoService videoService;

    @Autowired
    private UserService userService;

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private VideoContentService videoContentService;

    @Autowired
    private UserLogService userLogService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ThumbnailGenerationService thumbnailGenerationService;

    @Autowired
    private TagService tagService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AchievementService achievementService;

    @GetMapping("/upload")
    public String uploadPage(Model model) {
        return "upload-new";
    }

    @GetMapping("/upload-old")
    public String uploadOldPage(Model model) {
        return "upload";
    }

    @PostMapping("/upload")
    public String uploadVideo(
            HttpServletRequest request,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        System.out.println("========== 视频上传开始 ==========");

        try {
            // 检查用户认证
            if (authentication == null) {
                System.out.println("❌ 错误: 用户未登录");
                redirectAttributes.addFlashAttribute("error", "请先登录");
                return "redirect:/auth/login";
            }

            String username = authentication.getName();
            System.out.println("✅ 用户已登录: " + username);

            // 获取用户信息
            Optional<User> userOpt = userService.findByUsername(username);
            if (!userOpt.isPresent()) {
                System.out.println("❌ 错误: 找不到用户信息");
                redirectAttributes.addFlashAttribute("error", "用户信息错误");
                return "redirect:/auth/login";
            }
            User user = userOpt.get();
            System.out.println("✅ 用户信息获取成功: ID=" + user.getId());

            // 手动提取参数
            String title = request.getParameter("title");
            String description = request.getParameter("description");
            String selectedTags = request.getParameter("selectedTags");
            String categoryName = request.getParameter("category");
            String url = request.getParameter("url");
            String thumbnailUrl = request.getParameter("thumbnail");
            String content = request.getParameter("content");
            String[] contentTypes = request.getParameterValues("contentTypes");
            String[] contentUrls = request.getParameterValues("contentUrl");

            System.out.println("📝 表单参数:");
            System.out.println("  - 标题: " + (title != null ? "'" + title + "'" : "null"));
            System.out.println("  - 描述: " + (description != null ? "'" + description + "'" : "null"));
            System.out.println("  - 分类: " + (categoryName != null ? "'" + categoryName + "'" : "null"));
            System.out.println("  - 标签: " + (selectedTags != null ? "'" + selectedTags + "'" : "null"));
            System.out.println("  - 富文本内容: " + (content != null ? "有内容(" + content.length() + "字符)" : "null"));

            // 验证必填字段
            if (title == null || title.trim().isEmpty()) {
                System.out.println("❌ 错误: 标题为空");
                redirectAttributes.addFlashAttribute("error", "请输入视频标题");
                return "redirect:/upload";
            }

            if (categoryName == null || categoryName.trim().isEmpty()) {
                System.out.println("❌ 错误: 分类为空");
                redirectAttributes.addFlashAttribute("error", "请选择视频分类");
                return "redirect:/upload";
            }

            // 处理文件上传
            MultipartFile videoFile = null;
            MultipartFile thumbnailFile = null;
            MultipartFile[] contentFiles = null;

            if (request instanceof MultipartHttpServletRequest) {
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;

                // 打印所有文件参数名
                System.out.println("📁 所有文件参数:");
                multipartRequest.getFileMap().forEach((key, file) -> {
                    System.out.println("  - 参数名: " + key + ", 文件: " +
                        (file != null && !file.isEmpty() ? file.getOriginalFilename() + " (" + file.getSize() + " bytes)" : "空"));
                });

                videoFile = multipartRequest.getFile("videoFile");
                thumbnailFile = multipartRequest.getFile("thumbnailFile");

                System.out.println("📁 文件信息:");
                System.out.println("  - 视频文件: " + (videoFile != null && !videoFile.isEmpty() ?
                    videoFile.getOriginalFilename() + " (" + videoFile.getSize() + " bytes)" : "无"));
                System.out.println("  - 缩略图: " + (thumbnailFile != null && !thumbnailFile.isEmpty() ?
                    thumbnailFile.getOriginalFilename() + " (" + thumbnailFile.getSize() + " bytes)" : "无"));

                // 处理内容文件数组
                List<MultipartFile> contentFileList = multipartRequest.getFiles("contentFile");
                if (contentFileList != null && !contentFileList.isEmpty()) {
                    contentFiles = contentFileList.toArray(new MultipartFile[0]);
                    System.out.println("  - 内容文件数量: " + contentFiles.length);
                }
            } else {
                System.out.println("❌ 请求不是MultipartHttpServletRequest类型: " + request.getClass().getSimpleName());
            }

            // 验证视频文件
            if ((videoFile == null || videoFile.isEmpty()) && (url == null || url.trim().isEmpty())) {
                System.out.println("❌ 错误: 没有视频文件或URL");
                redirectAttributes.addFlashAttribute("error", "请选择视频文件或输入视频URL");
                return "redirect:/upload";
            }

            // 打印所有请求参数用于调试
            System.out.println("=== 所有请求参数 ===");
            request.getParameterMap().forEach((key, values) -> {
                System.out.println(key + ": " + Arrays.toString(values));
            });

            // 检查用户认证
            if (authentication == null) {
                System.out.println("用户未认证");
                redirectAttributes.addFlashAttribute("error", "请先登录");
                return "redirect:/login";
            }

            // 检查必填字段
            if (title == null || title.trim().isEmpty()) {
                System.out.println("标题为空");
                redirectAttributes.addFlashAttribute("error", "请输入视频标题");
                return "redirect:/upload";
            }

            Video video = new Video();
            video.setTitle(title.trim());
            video.setDescription(description != null ? description.trim() : "");

            // 处理主视频文件上传或URL
            if (videoFile != null && !videoFile.isEmpty()) {
                System.out.println("处理视频文件上传: " + videoFile.getOriginalFilename());
                String videoPath = fileUploadService.uploadVideo(videoFile); // 使用专门的视频上传方法
                video.setUrl(videoPath);
                video.setFilePath(videoPath); // 设置file_path字段
                System.out.println("视频文件上传成功: " + videoPath);
            } else if (url != null && !url.trim().isEmpty()) {
                video.setUrl(url.trim());
                video.setFilePath(url.trim()); // 设置file_path字段
                System.out.println("使用视频URL: " + url);
            } else {
                System.out.println("没有提供视频文件或URL");
                redirectAttributes.addFlashAttribute("error", "请选择视频文件或输入视频URL");
                return "redirect:/upload";
            }

            // 处理缩略图
            if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
                System.out.println("处理缩略图上传: " + thumbnailFile.getOriginalFilename());
                String thumbnailPath = fileUploadService.uploadThumbnail(thumbnailFile); // 使用专门的缩略图上传方法
                video.setThumbnail(thumbnailPath);
            } else if (thumbnailUrl != null && !thumbnailUrl.trim().isEmpty()) {
                video.setThumbnail(thumbnailUrl.trim());
            } else {
                // 自动生成缩略图
                System.out.println("自动生成缩略图: " + title);
                String generatedThumbnail = thumbnailGenerationService.generateDefaultThumbnail(title);
                video.setThumbnail(generatedThumbnail);
            }

            // 设置用户ID
            video.setUserId(user.getId());
            System.out.println("✅ 设置用户ID: " + user.getId());

            // 处理标签
            if (selectedTags != null && !selectedTags.trim().isEmpty()) {
                try {
                    List<String> tagNames = Arrays.asList(selectedTags.split(","));
                    Set<Tag> tags = tagService.getOrCreateTags(tagNames, user);
                    video.setTags(tags);
                    System.out.println("✅ 设置视频标签: " + tagNames.size() + " 个标签");
                } catch (Exception e) {
                    System.out.println("❌ 处理标签失败: " + e.getMessage());
                }
            }

            // 处理分类
            if (categoryName != null && !categoryName.trim().isEmpty()) {
                try {
                    Optional<Category> category = categoryService.getCategoryByName(categoryName.trim());
                    if (category.isPresent()) {
                        video.setCategory(category.get());
                        System.out.println("设置视频分类: " + categoryName);
                    } else {
                        System.out.println("分类不存在: " + categoryName);
                    }
                } catch (Exception e) {
                    System.err.println("处理分类失败: " + e.getMessage());
                }
            }

            // 处理富文本内容
            List<VideoContentBlock> contentBlocks = processVideoContent(
                contentTypes, request, contentFiles, contentUrls);

            if (!contentBlocks.isEmpty()) {
                try {
                    String contentJson = objectMapper.writeValueAsString(contentBlocks);
                    video.setContent(contentJson);
                    System.out.println("设置视频内容: " + contentBlocks.size() + " 个内容块");
                } catch (Exception e) {
                    System.err.println("序列化内容失败: " + e.getMessage());
                }
            }

            // 设置默认值
            video.setViews(0L);
            video.setLikeCount(0);
            video.setFavoriteCount(0);
            video.setStatus(Video.VideoStatus.PENDING);
            System.out.println("✅ 设置默认值完成");

            // 打印视频对象信息
            System.out.println("📋 视频对象信息:");
            System.out.println("  - 标题: " + video.getTitle());
            System.out.println("  - 描述: " + video.getDescription());
            System.out.println("  - 用户ID: " + video.getUserId());
            System.out.println("  - 视频URL: " + video.getUrl());
            System.out.println("  - 缩略图: " + video.getThumbnail());
            System.out.println("  - 状态: " + video.getStatus());

            // 保存视频
            System.out.println("💾 开始保存视频到数据库...");
            try {
                Video savedVideo = videoService.saveVideo(video);
                System.out.println("🎉 视频保存成功！");
                System.out.println("  - 视频ID: " + savedVideo.getId());
                System.out.println("  - 标题: " + savedVideo.getTitle());
                System.out.println("  - 状态: " + savedVideo.getStatus());

                // 同时保存内容到video_contents表
                if (!contentBlocks.isEmpty()) {
                    try {
                        System.out.println("💾 开始保存内容到video_contents表...");
                        for (int i = 0; i < contentBlocks.size(); i++) {
                            VideoContentBlock block = contentBlocks.get(i);
                            VideoContent videoContent = new VideoContent();
                            videoContent.setVideo(savedVideo);
                            videoContent.setType(VideoContent.ContentType.valueOf(block.getType()));
                            videoContent.setData(block.getData());
                            videoContent.setUrl(block.getUrl());
                            videoContent.setContent(block.getData() != null ? block.getData() : block.getUrl());
                            videoContent.setSortOrder(block.getOrder());

                            VideoContent savedContent = videoContentService.save(videoContent);
                            System.out.println("  - 内容块 " + i + " 保存成功: ID=" + savedContent.getId() +
                                ", 类型=" + savedContent.getType() +
                                ", 内容=" + (savedContent.getContent() != null ?
                                    savedContent.getContent().substring(0, Math.min(50, savedContent.getContent().length())) + "..." : "null"));
                        }
                        System.out.println("✅ 所有内容块保存完成！");
                    } catch (Exception e) {
                        System.err.println("❌ 保存内容到video_contents表失败: " + e.getMessage());
                        e.printStackTrace();
                    }
                }

                userLogService.logUserAction("VIDEO_UPLOAD_SUCCESS",
                    String.format("Video uploaded successfully: ID=%d, Title=%s",
                        savedVideo.getId(), savedVideo.getTitle()));

                // 触发成就检查
                try {
                    System.out.println("🎯 开始成就检查...");

                    // 触发视频上传成就检查
                    achievementService.triggerAchievementCheck(user, "UPLOAD_VIDEO");

                    System.out.println("✅ 成就检查完成");
                } catch (Exception e) {
                    System.err.println("❌ 成就检查失败: " + e.getMessage());
                    e.printStackTrace();
                }

                redirectAttributes.addFlashAttribute("success", "🎉 视频上传成功！视频正在审核中，请稍后查看。");
                System.out.println("========== 视频上传完成 ==========");
                return "redirect:/profile";

            } catch (Exception e) {
                System.out.println("❌ 保存视频失败: " + e.getMessage());
                e.printStackTrace();
                throw e; // 重新抛出异常，让外层catch处理
            }

        } catch (Exception e) {
            System.out.println("========== 视频上传失败 ==========");
            System.out.println("❌ 异常类型: " + e.getClass().getSimpleName());
            System.out.println("❌ 异常信息: " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("❌ 根本原因: " + e.getCause().getMessage());
            }
            System.out.println("❌ 堆栈跟踪:");
            e.printStackTrace();

            userLogService.logError("VIDEO_UPLOAD_ERROR",
                String.format("Video upload failed for user %s: %s",
                    authentication != null ? authentication.getName() : "anonymous",
                    e.getMessage()), e);

            redirectAttributes.addFlashAttribute("error", "❌ 上传失败：" + e.getMessage());
            System.out.println("========== 错误处理完成 ==========");
            return "redirect:/upload";
        }
    }

    /**
     * 处理视频内容块
     */
    private List<VideoContentBlock> processVideoContent(
            String[] contentTypes, HttpServletRequest request,
            MultipartFile[] contentFiles, String[] contentUrls) {

        List<VideoContentBlock> contentBlocks = new ArrayList<>();

        if (contentTypes == null || contentTypes.length == 0) {
            return contentBlocks;
        }

        for (int i = 0; i < contentTypes.length; i++) {
            try {
                String type = contentTypes[i];
                System.out.println("=== 处理内容块 " + i + " ===");
                System.out.println("内容类型: " + type);

                // 从request中获取不同类型的内容数据
                String[] contentDataArray = request.getParameterValues("contentData");
                String[] contentTextArray = request.getParameterValues("contentText");
                String data = "";

                System.out.println("contentTextArray长度: " + (contentTextArray != null ? contentTextArray.length : "null"));
                System.out.println("contentDataArray长度: " + (contentDataArray != null ? contentDataArray.length : "null"));

                // 根据类型获取对应的数据
                if ("TEXT".equals(type) && contentTextArray != null && i < contentTextArray.length) {
                    data = contentTextArray[i];
                    System.out.println("从contentText获取数据: " + data);
                } else if (contentDataArray != null && i < contentDataArray.length) {
                    data = contentDataArray[i];
                    System.out.println("从contentData获取数据: " + data);
                }

                String url = (contentUrls != null && i < contentUrls.length) ? contentUrls[i] : "";
                MultipartFile file = (contentFiles != null && i < contentFiles.length) ? contentFiles[i] : null;

                VideoContentBlock block = new VideoContentBlock();
                block.setType(type);
                block.setOrder(i + 1);

                switch (type) {
                    case "TEXT":
                        block.setData(data);
                        System.out.println("处理文字内容: " + data);
                        break;

                    case "RICH_TEXT":
                    case "HTML":
                        block.setData(data);
                        break;

                    case "IMAGE":
                        if (file != null && !file.isEmpty()) {
                            String imageUrl = fileUploadService.uploadContentImage(file);
                            block.setUrl(imageUrl);
                            block.setData(""); // 图片类型不需要data
                            System.out.println("处理图片内容: " + imageUrl);
                        }
                        break;

                    case "AUDIO":
                        if (file != null && !file.isEmpty()) {
                            String audioUrl = fileUploadService.uploadContentAudio(file);
                            block.setUrl(audioUrl);
                            block.setData(""); // 音频类型不需要data
                            System.out.println("处理音频内容: " + audioUrl);
                        }
                        break;

                    case "VIDEO":
                        if (file != null && !file.isEmpty()) {
                            String videoUrl = fileUploadService.uploadContentVideo(file);
                            block.setUrl(videoUrl);
                        } else if (url != null && !url.trim().isEmpty()) {
                            block.setUrl(url.trim());
                        }
                        block.setData(""); // 视频类型不需要data
                        break;
                }

                // 只添加有效的内容块
                if ((block.getData() != null && !block.getData().trim().isEmpty()) ||
                    (block.getUrl() != null && !block.getUrl().trim().isEmpty())) {
                    contentBlocks.add(block);
                }

            } catch (Exception e) {
                System.err.println("处理内容块 " + i + " 时出错: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return contentBlocks;
    }
}