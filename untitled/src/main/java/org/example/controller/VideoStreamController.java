package org.example.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * 视频流媒体控制器
 * 专门处理视频文件的流媒体播放，支持范围请求
 */
@RestController
@RequestMapping("/video/stream")
public class VideoStreamController {

    private static final Logger logger = LoggerFactory.getLogger(VideoStreamController.class);

    @Value("${app.upload.path:./uploads}")
    private String uploadPath;

    /**
     * 视频流媒体播放端点
     */
    @GetMapping("/{folder}/{filename:.+}")
    public ResponseEntity<Resource> streamVideo(
            @PathVariable String folder,
            @PathVariable String filename,
            HttpServletRequest request,
            HttpServletResponse response) {

        try {
            // 查找视频文件
            File videoFile = findVideoFile(folder, filename);
            if (videoFile == null || !videoFile.exists()) {
                logger.warn("视频文件未找到: {}/{}", folder, filename);
                return ResponseEntity.notFound().build();
            }

            // 检查是否是视频文件
            if (!isVideoFile(filename)) {
                logger.warn("请求的文件不是视频文件: {}", filename);
                return ResponseEntity.badRequest().build();
            }

            Resource resource = new FileSystemResource(videoFile);
            String contentType = getVideoContentType(filename);

            // 处理范围请求
            String rangeHeader = request.getHeader("Range");
            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                return handleRangeRequest(videoFile, rangeHeader, contentType);
            }

            // 普通请求
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(videoFile.length()))
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                    .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                    .body(resource);

        } catch (Exception e) {
            if (isClientAbortException(e)) {
                logger.info("客户端中断视频流: {}/{} - {}", folder, filename, e.getMessage());
                return ResponseEntity.status(499).build(); // 499 Client Closed Request
            } else {
                logger.error("视频流播放失败: {}/{} - {}", folder, filename, e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
    }

    /**
     * 处理范围请求（支持视频拖拽播放）
     */
    private ResponseEntity<Resource> handleRangeRequest(File videoFile, String rangeHeader, String contentType) {
        try {
            long fileLength = videoFile.length();
            String range = rangeHeader.substring("bytes=".length());
            String[] ranges = range.split("-");
            
            long start = Long.parseLong(ranges[0]);
            long end = ranges.length > 1 && !ranges[1].isEmpty() ? 
                      Long.parseLong(ranges[1]) : fileLength - 1;
            
            // 验证范围
            if (start >= fileLength || end >= fileLength || start > end) {
                return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                        .header(HttpHeaders.CONTENT_RANGE, "bytes */" + fileLength)
                        .build();
            }

            long contentLength = end - start + 1;
            
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileLength)
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength))
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                    .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                    .body(new RangeFileSystemResource(videoFile, start, end));

        } catch (Exception e) {
            logger.error("处理范围请求失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 查找视频文件
     */
    private File findVideoFile(String folder, String filename) {
        String workingDir = System.getProperty("user.dir");
        String[] possibleBasePaths = {
            workingDir + "/../uploads",
            workingDir + "/uploads",
            "./uploads",
            "../uploads"
        };

        for (String basePath : possibleBasePaths) {
            File testFile = new File(basePath, folder + File.separator + filename);
            if (testFile.exists() && testFile.isFile()) {
                logger.debug("找到视频文件: {}", testFile.getAbsolutePath());
                return testFile;
            }
        }
        return null;
    }

    /**
     * 检查是否是视频文件
     */
    private boolean isVideoFile(String filename) {
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        return Arrays.asList("mp4", "avi", "mov", "wmv", "mkv", "flv", "webm").contains(extension);
    }

    /**
     * 获取视频文件的Content-Type
     */
    private String getVideoContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        switch (extension) {
            case "mp4":
                return "video/mp4";
            case "avi":
                return "video/x-msvideo";
            case "mov":
                return "video/quicktime";
            case "wmv":
                return "video/x-ms-wmv";
            case "mkv":
                return "video/x-matroska";
            case "flv":
                return "video/x-flv";
            case "webm":
                return "video/webm";
            default:
                return "video/mp4";
        }
    }

    /**
     * 检查是否是客户端中断连接异常
     */
    private boolean isClientAbortException(Exception e) {
        String message = e.getMessage();
        if (message != null) {
            return message.contains("ClientAbortException") ||
                   message.contains("你的主机中的软件中止了一个已建立的连接") ||
                   message.contains("Connection reset by peer") ||
                   message.contains("Broken pipe") ||
                   message.contains("getOutputStream() has already been called");
        }
        
        return e instanceof org.apache.catalina.connector.ClientAbortException ||
               e instanceof java.io.IOException && 
               (e.getCause() instanceof java.net.SocketException ||
                e.getCause() instanceof java.io.IOException);
    }

    /**
     * 支持范围请求的Resource实现
     */
    private static class RangeFileSystemResource extends FileSystemResource {
        private final long start;
        private final long end;

        public RangeFileSystemResource(File file, long start, long end) {
            super(file);
            this.start = start;
            this.end = end;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            InputStream inputStream = super.getInputStream();
            inputStream.skip(start);
            return new RangeInputStream(inputStream, end - start + 1);
        }

        @Override
        public long contentLength() {
            return end - start + 1;
        }
    }

    /**
     * 范围输入流
     */
    private static class RangeInputStream extends InputStream {
        private final InputStream delegate;
        private final long maxBytes;
        private long bytesRead = 0;

        public RangeInputStream(InputStream delegate, long maxBytes) {
            this.delegate = delegate;
            this.maxBytes = maxBytes;
        }

        @Override
        public int read() throws IOException {
            if (bytesRead >= maxBytes) {
                return -1;
            }
            int result = delegate.read();
            if (result != -1) {
                bytesRead++;
            }
            return result;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (bytesRead >= maxBytes) {
                return -1;
            }
            int remaining = (int) (maxBytes - bytesRead);
            int toRead = Math.min(len, remaining);
            int result = delegate.read(b, off, toRead);
            if (result != -1) {
                bytesRead += result;
            }
            return result;
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }
    }
} 