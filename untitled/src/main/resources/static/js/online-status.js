/**
 * 在线状态管理器
 * 负责管理用户在线状态的显示和更新
 */
class OnlineStatusManager {
    constructor() {
        this.onlineUsers = new Set();
        this.userStatuses = new Map();
        this.updateInterval = null;
        this.isInitialized = false;
        
        console.log('🟢 在线状态管理器初始化');
        this.init();
    }

    /**
     * 初始化在线状态管理器
     */
    async init() {
        try {
            // 启动定期更新
            this.startPeriodicUpdate();

            // 监听页面可见性变化
            this.setupVisibilityListener();

            // 立即获取一次在线用户统计
            await this.getOnlineUsers();

            // 订阅在线状态更新
            this.subscribeToOnlineUpdates();

            // 如果用户已登录，自动设置在线状态
            this.autoSetUserOnline();

            this.isInitialized = true;
            console.log('✅ 在线状态管理器初始化完成');
        } catch (error) {
            console.error('❌ 在线状态管理器初始化失败:', error);
        }
    }

    /**
     * 自动设置用户在线状态
     */
    autoSetUserOnline() {
        const userElement = document.querySelector('[data-current-user-id]');
        if (userElement) {
            const userId = userElement.getAttribute('data-current-user-id');
            const username = userElement.getAttribute('data-current-username');

            if (userId && username) {
                console.log('🟢 检测到已登录用户，自动设置在线状态:', username);
                this.setUserOnlineStatus(parseInt(userId), username);
            }
        }
    }

    /**
     * 设置用户在线状态
     */
    async setUserOnlineStatus(userId, username) {
        try {
            const response = await fetch('/api/chat/user/online', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    userId: userId,
                    username: username
                }),
                credentials: 'include'
            });

            if (response.ok) {
                const data = await response.json();
                console.log('✅ 用户在线状态设置成功:', data);
                // 立即更新在线用户数量
                this.getOnlineUsers();
            } else {
                console.warn('⚠️ 设置用户在线状态失败:', response.status);
            }
        } catch (error) {
            console.error('❌ 设置用户在线状态失败:', error);
        }
    }

    /**
     * 订阅在线状态更新
     */
    subscribeToOnlineUpdates() {
        // 检查是否有可用的WebSocket连接
        if (window.stompClient && window.stompClient.connected) {
            this.setupWebSocketSubscription(window.stompClient);
        } else if (window.ChatApp && window.ChatApp.stompClient && window.ChatApp.connected) {
            this.setupWebSocketSubscription(window.ChatApp.stompClient);
        } else {
            // 如果没有现有连接，创建自己的WebSocket连接
            this.createWebSocketConnection();
        }
    }

    /**
     * 创建WebSocket连接
     */
    createWebSocketConnection() {
        try {
            console.log('🔌 创建在线状态WebSocket连接...');

            // 使用SockJS和STOMP
            const socket = new SockJS('/ws');
            const stompClient = Stomp.over(socket);

            // 禁用调试输出
            stompClient.debug = null;

            stompClient.connect({},
                (frame) => {
                    console.log('✅ 在线状态WebSocket连接成功');
                    this.stompClient = stompClient;
                    this.setupWebSocketSubscription(stompClient);

                    // 发送用户身份标识（如果用户已登录）
                    this.sendUserIdentification(stompClient);
                },
                (error) => {
                    console.error('❌ 在线状态WebSocket连接失败:', error);
                    // 5秒后重试
                    setTimeout(() => this.subscribeToOnlineUpdates(), 5000);
                }
            );
        } catch (error) {
            console.error('❌ 创建WebSocket连接失败:', error);
            // 5秒后重试
            setTimeout(() => this.subscribeToOnlineUpdates(), 5000);
        }
    }

    /**
     * 发送用户身份标识
     */
    sendUserIdentification(stompClient) {
        // 检查用户是否已登录
        const userElement = document.querySelector('[data-current-user-id]');
        if (userElement) {
            const userId = userElement.getAttribute('data-current-user-id');
            const username = userElement.getAttribute('data-current-username');

            if (userId && username) {
                try {
                    const identificationMessage = {
                        type: 'user_identification',
                        userId: parseInt(userId),
                        username: username,
                        timestamp: Date.now()
                    };

                    // 兼容新旧版本的STOMP.js
                    if (typeof stompClient.publish === 'function') {
                        // 新版本 @stomp/stompjs (7.x)
                        stompClient.publish({
                            destination: '/app/user-identification',
                            body: JSON.stringify(identificationMessage)
                        });
                    } else if (typeof stompClient.send === 'function') {
                        // 旧版本 stomp.js (2.x)
                        stompClient.send('/app/user-identification', {}, JSON.stringify(identificationMessage));
                    } else {
                        console.error('❌ 不支持的STOMP客户端版本');
                        return;
                    }

                    console.log('📤 发送用户身份标识:', identificationMessage);
                } catch (error) {
                    console.error('❌ 发送用户身份标识失败:', error);
                }
            }
        }
    }

    /**
     * 设置WebSocket订阅
     */
    setupWebSocketSubscription(stompClient) {
        try {
            // 订阅在线用户数量更新
            stompClient.subscribe('/topic/online-count', (message) => {
                const data = JSON.parse(message.body);
                if (data.type === 'online_count_update') {
                    this.updateOnlineCount(data.count);
                    console.log('📡 收到在线用户数量更新:', data.count);
                }
            });

            // 订阅用户状态变化
            stompClient.subscribe('/topic/user-status', (message) => {
                const data = JSON.parse(message.body);
                if (data.type === 'user_status_change') {
                    this.updateUserStatus(data.userId, data.isOnline ? 'online' : 'offline');
                    console.log('📡 收到用户状态变化:', data.userId, data.isOnline ? '上线' : '下线');
                }
            });

            console.log('✅ 已订阅在线状态更新');
        } catch (error) {
            console.error('❌ 订阅在线状态更新失败:', error);
        }
    }

    /**
     * 检查用户在线状态
     */
    async checkUserStatus(userIds) {
        if (!userIds || userIds.size === 0) {
            return;
        }

        try {
            // 获取在线用户列表
            const response = await fetch('/api/online-users/list');
            if (response.ok) {
                const data = await response.json();
                if (data.success) {
                    const onlineUserIds = new Set(data.onlineUsers);

                    // 更新用户状态
                    const userIdsArray = Array.from(userIds);
                    userIdsArray.forEach(userId => {
                        const isOnline = onlineUserIds.has(userId.toString());
                        this.updateUserStatus(userId, isOnline ? 'online' : 'offline');
                    });

                    console.log('🔍 检查用户在线状态完成:', {
                        '检查用户': userIdsArray,
                        '在线用户': Array.from(onlineUserIds),
                        '总在线数': data.onlineCount
                    });
                }
            }
        } catch (error) {
            console.error('❌ 检查用户状态失败:', error);
            // 降级处理：标记所有用户为离线
            const userIdsArray = Array.from(userIds);
            userIdsArray.forEach(userId => {
                this.updateUserStatus(userId, 'offline');
            });
        }
    }

    /**
     * 更新用户状态
     */
    updateUserStatus(userId, status) {
        this.userStatuses.set(userId, status);
        
        // 更新页面上的状态指示器
        const userElements = document.querySelectorAll(`[data-user-id="${userId}"]`);
        userElements.forEach(element => {
            this.updateStatusIndicator(element, status);
        });
        
        // 更新在线用户集合
        if (status === 'online') {
            this.onlineUsers.add(userId);
        } else {
            this.onlineUsers.delete(userId);
        }
    }

    /**
     * 更新状态指示器
     */
    updateStatusIndicator(element, status) {
        // 移除所有状态类
        element.classList.remove('online', 'offline', 'away', 'busy');
        
        // 添加新状态类
        element.classList.add(status);
        
        // 如果元素有状态文本，也更新它
        const statusText = element.querySelector('.status-text');
        if (statusText) {
            statusText.textContent = this.getStatusText(status);
            statusText.className = `status-text ${status}`;
        }
    }

    /**
     * 获取状态文本
     */
    getStatusText(status) {
        const statusTexts = {
            'online': '在线',
            'offline': '离线',
            'away': '离开',
            'busy': '忙碌'
        };
        return statusTexts[status] || '未知';
    }

    /**
     * 获取在线用户统计
     */
    async getOnlineUsers() {
        try {
            // 调用后端API获取真实在线用户数量
            const response = await fetch('/api/online-users/count');
            if (response.ok) {
                const data = await response.json();
                if (data.success) {
                    this.updateOnlineCount(data.onlineCount);
                    console.log('📊 在线用户数量:', data.onlineCount);
                } else {
                    console.warn('⚠️ 获取在线用户数量失败:', data.message);
                    this.updateOnlineCount(0);
                }
            } else {
                console.error('❌ API请求失败:', response.status);
                this.updateOnlineCount(0);
            }
        } catch (error) {
            console.error('❌ 获取在线用户失败:', error);
            this.updateOnlineCount(0);
        }
    }

    /**
     * 更新在线用户数量显示
     */
    updateOnlineCount(count) {
        const onlineCountElements = document.querySelectorAll('.online-count');
        onlineCountElements.forEach(element => {
            element.textContent = count;
        });
    }

    /**
     * 启动定期更新
     */
    startPeriodicUpdate() {
        // 每10秒更新一次在线状态（更频繁的更新）
        this.updateInterval = setInterval(() => {
            this.getOnlineUsers();

            // 重新检查页面上的用户状态
            const userElements = document.querySelectorAll('[data-user-id]');
            const userIds = new Set();

            userElements.forEach(element => {
                const userId = element.getAttribute('data-user-id');
                if (userId && userId !== 'null') {
                    userIds.add(parseInt(userId));
                }
            });

            if (userIds.size > 0) {
                this.checkUserStatus(userIds);
            }
        }, 10000); // 改为10秒
    }

    /**
     * 停止定期更新
     */
    stopPeriodicUpdate() {
        if (this.updateInterval) {
            clearInterval(this.updateInterval);
            this.updateInterval = null;
        }
    }

    /**
     * 设置页面可见性监听器
     */
    setupVisibilityListener() {
        document.addEventListener('visibilitychange', () => {
            if (document.hidden) {
                // 页面隐藏时停止更新
                this.stopPeriodicUpdate();
                console.log('⏸️ 页面隐藏，停止在线状态更新');
            } else {
                // 页面显示时恢复更新
                this.startPeriodicUpdate();
                console.log('▶️ 页面显示，恢复在线状态更新');
                
                // 立即更新一次
                this.getOnlineUsers();
            }
        });
    }

    /**
     * 销毁管理器
     */
    destroy() {
        this.stopPeriodicUpdate();
        this.onlineUsers.clear();
        this.userStatuses.clear();
        console.log('🔴 在线状态管理器已销毁');
    }
}

// 创建全局实例
window.onlineStatusManager = new OnlineStatusManager();

// 页面卸载时清理
window.addEventListener('beforeunload', () => {
    if (window.onlineStatusManager) {
        window.onlineStatusManager.destroy();
    }
});

console.log('📦 在线状态管理模块加载完成');
