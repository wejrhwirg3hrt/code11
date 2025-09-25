package org.example.exception;

import org.example.service.UserLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Autowired
    private UserLogService userLogService;

    /**
     * 处理客户端中断连接异常
     */
    @ExceptionHandler(org.apache.catalina.connector.ClientAbortException.class)
    public ResponseEntity<Object> handleClientAbortException(
            org.apache.catalina.connector.ClientAbortException ex,
            WebRequest request) {
        
        String requestUrl = getRequestUrl(request);
        logger.info("客户端中断连接: {} - {}", requestUrl, ex.getMessage());
        
        // 返回499状态码，表示客户端关闭请求
        return ResponseEntity.status(499).build();
    }

    /**
     * 处理IOException（可能包含客户端中断）
     */
    @ExceptionHandler(java.io.IOException.class)
    public ResponseEntity<Object> handleIOException(
            java.io.IOException ex,
            WebRequest request) {
        
        String requestUrl = getRequestUrl(request);
        String message = ex.getMessage();
        
        // 检查是否是客户端中断相关的异常
        if (isClientAbortRelated(message)) {
            logger.info("客户端中断连接(IOException): {} - {}", requestUrl, message);
            return ResponseEntity.status(499).build();
        }
        
        // 其他IO异常
        logger.error("IO异常: {} - {}", requestUrl, message, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("文件操作失败", message));
    }

    /**
     * 处理IllegalStateException（getOutputStream/getWriter冲突）
     */
    @ExceptionHandler(java.lang.IllegalStateException.class)
    public ResponseEntity<Object> handleIllegalStateException(
            java.lang.IllegalStateException ex,
            WebRequest request) {
        
        String requestUrl = getRequestUrl(request);
        String message = ex.getMessage();
        
        // 检查是否是流媒体相关的状态异常
        if (isStreamingRelated(message)) {
            logger.info("流媒体状态异常: {} - {}", requestUrl, message);
            return ResponseEntity.status(499).build();
        }
        
        // 其他状态异常
        logger.error("状态异常: {} - {}", requestUrl, message, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("请求处理失败", message));
    }

    /**
     * 处理SocketException（网络连接异常）
     */
    @ExceptionHandler(java.net.SocketException.class)
    public ResponseEntity<Object> handleSocketException(
            java.net.SocketException ex,
            WebRequest request) {
        
        String requestUrl = getRequestUrl(request);
        String message = ex.getMessage();
        
        logger.info("网络连接异常: {} - {}", requestUrl, message);
        return ResponseEntity.status(499).build();
    }

    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleDataAccessException(DataAccessException e, HttpServletRequest request) {
        String username = getCurrentUsername();
        userLogService.logError("DATABASE_ERROR",
            String.format("Database error for user %s on %s: %s",
                username, request.getRequestURI(), e.getMessage()), e);

        System.err.println("数据库访问异常: " + e.getMessage());
        e.printStackTrace();

        ModelAndView mav = new ModelAndView("error/500");
        mav.addObject("error", "数据库连接异常，请稍后重试");
        mav.addObject("details", "如果问题持续存在，请联系管理员");
        return mav;
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ModelAndView handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        String username = getCurrentUsername();
        userLogService.logError("ACCESS_DENIED",
            String.format("Access denied for user %s on %s", username, request.getRequestURI()), e);

        ModelAndView mav = new ModelAndView("error/403");
        mav.addObject("error", "访问被拒绝");
        return mav;
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        String username = getCurrentUsername();
        userLogService.logError("RUNTIME_ERROR",
            String.format("Runtime error for user %s on %s: %s",
                username, request.getRequestURI(), e.getMessage()), e);

        System.err.println("运行时异常: " + e.getMessage());
        e.printStackTrace();

        ModelAndView mav = new ModelAndView("error/500");
        mav.addObject("error", e.getMessage() != null ? e.getMessage() : "系统运行时错误");
        mav.addObject("details", "请检查输入数据或联系管理员");
        return mav;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleException(Exception e, HttpServletRequest request) {
        String username = getCurrentUsername();
        userLogService.logError("GENERAL_ERROR",
            String.format("General error for user %s on %s: %s",
                username, request.getRequestURI(), e.getMessage()), e);

        System.err.println("系统异常: " + e.getMessage());
        e.printStackTrace();

        ModelAndView mav = new ModelAndView("error/500");
        mav.addObject("error", "系统内部错误");
        mav.addObject("details", "我们正在处理这个问题，请稍后重试");
        return mav;
    }

    /**
     * 检查是否是客户端中断相关的异常消息
     */
    private boolean isClientAbortRelated(String message) {
        if (message == null) {
            return false;
        }
        
        return message.contains("ClientAbortException") ||
               message.contains("你的主机中的软件中止了一个已建立的连接") ||
               message.contains("Connection reset by peer") ||
               message.contains("Broken pipe") ||
               message.contains("Connection refused") ||
               message.contains("Connection timed out") ||
               message.contains("Software caused connection abort");
    }

    /**
     * 检查是否是流媒体相关的状态异常
     */
    private boolean isStreamingRelated(String message) {
        if (message == null) {
            return false;
        }
        
        return message.contains("getOutputStream() has already been called") ||
               message.contains("getWriter() has already been called") ||
               message.contains("response has already been committed") ||
               message.contains("stream closed");
    }

    /**
     * 获取请求URL
     */
    private String getRequestUrl(WebRequest request) {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            return httpRequest.getRequestURL().toString();
        }
        return "unknown";
    }

    /**
     * 创建错误响应
     */
    private Map<String, Object> createErrorResponse(String error, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", error);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() ? auth.getName() : "anonymous";
    }
}