package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;
import jakarta.servlet.http.HttpSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket配置类
 * 配置WebSocket消息代理和端点
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // 连接计数器
    private final AtomicInteger connectionCount = new AtomicInteger(0);
    private final Map<String, Long> connectionTimes = new ConcurrentHashMap<>();
    private final Map<String, String> connectionUsers = new ConcurrentHashMap<>();
    
    // 更严格的连接限制
    private static final int MAX_CONNECTIONS = 500; // 降低最大连接数
    private static final long CONNECTION_TIMEOUT = 180000; // 3分钟超时
    private static final int MAX_CONNECTIONS_PER_USER = 3; // 每个用户最多3个连接
    
    // 定时清理任务
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();

    public WebSocketConfig() {
        // 启动定时清理任务
        cleanupExecutor.scheduleAtFixedRate(this::cleanupTimeoutConnections, 30, 30, TimeUnit.SECONDS);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用简单消息代理，并设置消息代理路径前缀
        config.enableSimpleBroker("/topic", "/queue", "/user");

        // 设置应用程序消息前缀
        config.setApplicationDestinationPrefixes("/app");

        // 设置用户目标前缀
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册WebSocket端点，并允许跨域
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*", "http://[::1]:*")
                .addInterceptors(new HandshakeInterceptor() {
                    @Override
                    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
                        try {
                            // 检查连接数量限制
                            int currentConnections = connectionCount.get();
                            if (currentConnections >= MAX_CONNECTIONS) {
                                System.err.println("WebSocket连接数达到上限: " + currentConnections + "/" + MAX_CONNECTIONS);
                                return false;
                            }

                            // 清理超时连接
                            cleanupTimeoutConnections();

                            String username = null;
                            String sessionId = null;

                            // 方法1: 从HTTP会话中获取用户信息
                            if (request instanceof org.springframework.http.server.ServletServerHttpRequest) {
                                org.springframework.http.server.ServletServerHttpRequest servletRequest =
                                    (org.springframework.http.server.ServletServerHttpRequest) request;
                                HttpSession session = servletRequest.getServletRequest().getSession(false);

                                if (session != null) {
                                    sessionId = session.getId();
                                    System.out.println("WebSocket握手：找到HTTP会话，ID: " + sessionId);

                                    // 从会话中获取Spring Security的认证信息
                                    Object securityContext = session.getAttribute("SPRING_SECURITY_CONTEXT");
                                    if (securityContext instanceof org.springframework.security.core.context.SecurityContext) {
                                        Authentication auth = ((org.springframework.security.core.context.SecurityContext) securityContext).getAuthentication();
                                        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                                            username = auth.getName();
                                            System.out.println("WebSocket握手：从会话获取用户 " + username);
                                        }
                                    }

                                    // 尝试从会话属性中直接获取用户信息
                                    if (username == null) {
                                        Object userObj = session.getAttribute("user");
                                        if (userObj != null) {
                                            username = userObj.toString();
                                            System.out.println("WebSocket握手：从会话属性获取用户 " + username);
                                        }
                                    }
                                }
                            }

                            // 方法2: 从SecurityContextHolder获取
                            if (username == null) {
                                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                                if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                                    username = auth.getName();
                                    System.out.println("WebSocket握手：从SecurityContext获取用户 " + username);
                                }
                            }

                            // 检查用户连接数限制
                            if (username != null) {
                                int userConnections = countUserConnections(username);
                                if (userConnections >= MAX_CONNECTIONS_PER_USER) {
                                    System.err.println("用户 " + username + " 连接数达到上限: " + userConnections + "/" + MAX_CONNECTIONS_PER_USER);
                                    return false;
                                }
                            }

                            if (username != null) {
                                attributes.put("username", username);
                                attributes.put("sessionId", sessionId);
                                attributes.put("connectionTime", System.currentTimeMillis());
                                
                                // 增加连接计数
                                connectionCount.incrementAndGet();
                                String connectionKey = sessionId != null ? sessionId : username + "_" + System.currentTimeMillis();
                                connectionTimes.put(connectionKey, System.currentTimeMillis());
                                connectionUsers.put(connectionKey, username);
                                
                                System.out.println("WebSocket握手：用户 " + username + " 连接成功，当前连接数: " + connectionCount.get() + "/" + MAX_CONNECTIONS);
                            } else {
                                System.out.println("WebSocket握手：匿名用户连接");
                            }

                        } catch (Exception e) {
                            System.err.println("WebSocket握手处理出错: " + e.getMessage());
                            e.printStackTrace();
                            return false;
                        }
                        return true;
                    }

                    @Override
                    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                               WebSocketHandler wsHandler, Exception exception) {
                        // 握手完成后的处理
                        if (exception != null) {
                            System.err.println("WebSocket握手失败: " + exception.getMessage());
                            // 减少连接计数
                            connectionCount.decrementAndGet();
                        }
                    }
                })
                .withSockJS()
                .setHeartbeatTime(30000) // 30秒心跳
                .setDisconnectDelay(3000) // 3秒断开延迟
                .setHttpMessageCacheSize(500) // 减少消息缓存大小
                .setStreamBytesLimit(256 * 1024) // 减少流字节限制
                .setSessionCookieNeeded(false); // 不需要会话cookie
    }

    /**
     * 配置WebSocket容器
     */
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(4096); // 减少缓冲区大小
        container.setMaxBinaryMessageBufferSize(4096);
        container.setMaxSessionIdleTimeout(180000L); // 3分钟空闲超时
        container.setAsyncSendTimeout(10000L); // 10秒异步发送超时
        return container;
    }

    /**
     * 清理超时连接
     */
    private void cleanupTimeoutConnections() {
        long currentTime = System.currentTimeMillis();
        connectionTimes.entrySet().removeIf(entry -> {
            if (currentTime - entry.getValue() > CONNECTION_TIMEOUT) {
                connectionCount.decrementAndGet();
                String username = connectionUsers.remove(entry.getKey());
                System.out.println("清理超时连接: " + entry.getKey() + " (用户: " + username + ")");
                return true;
            }
            return false;
        });
    }

    /**
     * 统计用户连接数
     */
    private int countUserConnections(String username) {
        return (int) connectionUsers.values().stream()
                .filter(user -> user.equals(username))
                .count();
    }

    /**
     * 获取当前连接数
     */
    public int getCurrentConnectionCount() {
        return connectionCount.get();
    }

    /**
     * 获取最大连接数
     */
    public int getMaxConnectionCount() {
        return MAX_CONNECTIONS;
    }

    /**
     * 移除连接
     */
    public void removeConnection(String sessionId) {
        if (connectionTimes.remove(sessionId) != null) {
            connectionCount.decrementAndGet();
            String username = connectionUsers.remove(sessionId);
            System.out.println("移除WebSocket连接: " + sessionId + " (用户: " + username + ")，当前连接数: " + connectionCount.get());
        }
    }

    /**
     * 获取连接统计信息
     */
    public Map<String, Object> getConnectionStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("currentConnections", connectionCount.get());
        stats.put("maxConnections", MAX_CONNECTIONS);
        stats.put("connectionTimeout", CONNECTION_TIMEOUT);
        stats.put("maxConnectionsPerUser", MAX_CONNECTIONS_PER_USER);
        stats.put("activeUsers", connectionUsers.values().stream().distinct().count());
        return stats;
    }

    /**
     * 重置连接计数（紧急情况使用）
     */
    public void resetConnectionCount() {
        int oldCount = connectionCount.get();
        connectionCount.set(0);
        connectionTimes.clear();
        connectionUsers.clear();
        System.out.println("⚠️  紧急重置WebSocket连接计数: " + oldCount + " -> 0");
    }

    /**
     * 强制清理所有连接
     */
    public void forceCleanupAllConnections() {
        int oldCount = connectionCount.get();
        connectionCount.set(0);
        connectionTimes.clear();
        connectionUsers.clear();
        System.out.println("🧹 强制清理所有WebSocket连接: " + oldCount + " -> 0");
    }
}
