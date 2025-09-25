/**
 * 全局WebRTC管理器
 * 在所有页面中提供统一的WebRTC通话接收功能
 */
class GlobalWebRTCManager {
    constructor() {
        this.isInitialized = false;
        this.webrtcClient = null;
        this.stompClient = null;
        this.currentUser = null;
        this.subscriptions = [];
        this.connectionAttempts = 0;
        this.maxConnectionAttempts = 20;
        this.pageType = this.detectPageType();
    }

    /**
     * 检测当前页面类型
     */
    detectPageType() {
        const path = window.location.pathname;
        if (path.includes('/chat')) return 'chat';
        if (path.includes('/webrtc-test')) return 'test';
        if (path.includes('/test-call')) return 'test-call';
        if (path === '/' || path.includes('/index')) return 'home';
        return 'other';
    }

    /**
     * 初始化全局WebRTC管理器
     */
    async initialize() {
        if (this.isInitialized) {
            console.log('全局WebRTC管理器已初始化');
            return;
        }

        try {
            console.log(`=== 开始初始化全局WebRTC管理器 (${this.pageType}页面) ===`);

            // 1. 检查必要的库
            if (!this.checkRequiredLibraries()) {
                console.warn('必要的库未加载，尝试基础连接');
                // 即使WebRTC库未加载，也尝试建立基础WebSocket连接以注册在线状态
                await this.setupBasicConnection();
                return;
            }

            // 2. 初始化全局来电弹窗
            await this.initGlobalPopup();

            // 3. 获取用户信息
            await this.getCurrentUser();

            // 4. 建立WebSocket连接
            await this.setupWebSocketConnection();

            // 5. 创建WebRTC客户端
            await this.initWebRTCClient();

            // 6. 订阅消息
            this.subscribeToMessages();

            // 7. 启动保活机制
            this.startKeepAlive();

            this.isInitialized = true;
            console.log('=== 全局WebRTC管理器初始化成功 ===');

        } catch (error) {
            console.error('全局WebRTC管理器初始化失败:', error);
        }
    }

    /**
     * 检查必要的库是否加载
     */
    checkRequiredLibraries() {
        const required = ['SockJS', 'Stomp'];
        const missing = [];

        for (const lib of required) {
            if (typeof window[lib] === 'undefined') {
                console.warn(`${lib}库未加载`);
                missing.push(lib);
            }
        }

        // 检查WebRTCClient类
        if (typeof WebRTCClient === 'undefined') {
            console.warn('WebRTCClient类未加载');
            missing.push('WebRTCClient');
        }

        if (missing.length > 0) {
            console.error('缺少必要的库:', missing);
            return false;
        }

        console.log('✅ 所有必要的库都已加载');
        return true;
    }

    /**
     * 建立基础连接（即使WebRTC库未加载）
     */
    async setupBasicConnection() {
        try {
            console.log('🔄 尝试建立基础WebSocket连接...');

            // 检查基础库
            if (typeof SockJS === 'undefined' || typeof Stomp === 'undefined') {
                console.error('❌ SockJS或Stomp库未加载，无法建立连接');
                return;
            }

            // 获取用户信息
            await this.getCurrentUser();
            if (!this.currentUser) {
                console.warn('⚠️ 无法获取用户信息，跳过连接');
                return;
            }

            // 建立WebSocket连接
            await this.setupWebSocketConnection();

            // 注册在线状态
            if (this.stompClient && this.stompClient.connected) {
                this.stompClient.send('/app/webrtc/join', {}, JSON.stringify({
                    userId: this.currentUser.id.toString()
                }));
                console.log('✅ 基础连接建立成功，已注册在线状态');
            }

        } catch (error) {
            console.error('❌ 基础连接建立失败:', error);
        }
    }

    /**
     * 初始化全局来电弹窗
     */
    async initGlobalPopup() {
        if (typeof GlobalCallPopup !== 'undefined' && !window.globalCallPopup) {
            window.globalCallPopup = new GlobalCallPopup();
            console.log('✅ 全局来电弹窗初始化成功');
        }
    }

    /**
     * 获取当前用户信息
     */
    async getCurrentUser() {
        // 尝试从不同来源获取用户信息
        if (window.currentUser && window.currentUser.id) {
            this.currentUser = window.currentUser;
            console.log('✅ 从window.currentUser获取用户信息:', this.currentUser.username);
            return;
        }

        // 尝试从API获取
        try {
            const response = await fetch('/api/user/current');
            if (response.ok) {
                const userData = await response.json();
                if (userData.success && userData.user) {
                    this.currentUser = userData.user;
                    window.currentUser = userData.user;
                    console.log('✅ 从API获取用户信息:', userData.user.username);
                    return;
                }
            }
        } catch (error) {
            console.warn('从API获取用户信息失败:', error);
        }

        throw new Error('无法获取当前用户信息');
    }

    /**
     * 建立WebSocket连接
     */
    async setupWebSocketConnection() {
        // 对于聊天页面，尝试复用聊天的WebSocket连接
        if (this.pageType === 'chat') {
            return this.setupChatPageConnection();
        } else {
            return this.createIndependentConnection();
        }
    }

    /**
     * 聊天页面连接设置
     */
    async setupChatPageConnection() {
        // 直接检查是否有聊天连接，如果没有就创建独立连接
        if (window.ChatApp && window.ChatApp.stompClient && window.ChatApp.stompClient.connected) {
            console.log('✅ 复用聊天WebSocket连接');
            this.stompClient = window.ChatApp.stompClient;
            window.stompClient = this.stompClient;
            return;
        }

        console.log('⚠️ 聊天连接不可用，创建独立连接');
        return this.createIndependentConnection();
    }

    /**
     * 创建独立的WebSocket连接
     */
    async createIndependentConnection() {
        return new Promise((resolve, reject) => {
            console.log('创建独立WebSocket连接...');
            
            const socket = new SockJS('/ws');
            this.stompClient = Stomp.over(socket);
            this.stompClient.debug = null;
            
            this.stompClient.connect({}, (frame) => {
                console.log('✅ 独立WebSocket连接成功');
                window.stompClient = this.stompClient;
                resolve();
            }, (error) => {
                console.error('❌ 独立WebSocket连接失败:', error);
                reject(error);
            });
        });
    }

    /**
     * 初始化WebRTC客户端
     */
    async initWebRTCClient() {
        this.webrtcClient = new WebRTCClient();
        this.webrtcClient.stompClient = this.stompClient;
        this.webrtcClient.currentUserId = this.currentUser.id.toString();

        // 设置全局变量
        window.webrtcClient = this.webrtcClient;

        console.log('✅ WebRTC客户端创建成功');
    }

    /**
     * 订阅WebRTC消息
     */
    subscribeToMessages() {
        if (!this.stompClient || !this.currentUser) {
            console.error('❌ 无法订阅消息：连接或用户信息缺失');
            return;
        }

        console.log('🎯 开始订阅WebRTC消息，用户ID:', this.currentUser.id);

        // 订阅通话邀请（用户队列）
        const sub1 = this.stompClient.subscribe(`/user/queue/webrtc/call-invitation`, (message) => {
            const data = JSON.parse(message.body);
            console.log('🔔 收到通话邀请(用户队列):', data);
            console.log('🎯 开始处理来电邀请...');
            this.handleCallInvitation(data);
        });

        // 订阅通话邀请（广播频道）
        const sub2 = this.stompClient.subscribe(`/topic/webrtc/call-invitation/${this.currentUser.id}`, (message) => {
            const data = JSON.parse(message.body);
            console.log('🔔 收到通话邀请(广播频道):', data);
            console.log('🎯 开始处理来电邀请...');
            this.handleCallInvitation(data);
        });

        // 订阅强制广播（确保消息能够到达）
        const sub2b = this.stompClient.subscribe(`/topic/webrtc/call-invitation-broadcast`, (message) => {
            const data = JSON.parse(message.body);
            console.log('🔔 收到通话邀请(强制广播):', data);
            console.log('检查是否为目标用户:', data.calleeId, '===', this.currentUser.id.toString());
            if (data.calleeId === this.currentUser.id.toString()) {
                console.log('🎯 开始处理来电邀请...');
                this.handleCallInvitation(data);
            } else {
                console.log('❌ 不是目标用户，忽略消息');
            }
        });

        // 订阅通话接受（用户队列）
        const sub3 = this.stompClient.subscribe(`/user/queue/webrtc/call-accepted`, (message) => {
            const data = JSON.parse(message.body);
            console.log('✅ 通话被接受(用户队列):', data);
            this.handleCallAccepted(data);
        });

        // 订阅通话接受（广播频道备用）
        const sub3b = this.stompClient.subscribe(`/topic/webrtc/call-accepted/${this.currentUser.id}`, (message) => {
            const data = JSON.parse(message.body);
            console.log('✅ 通话被接受(广播频道):', data);
            this.handleCallAccepted(data);
        });

        // 订阅通话拒绝
        const sub4 = this.stompClient.subscribe(`/user/queue/webrtc/call-rejected`, (message) => {
            const data = JSON.parse(message.body);
            console.log('❌ 通话被拒绝:', data);
            this.handleCallRejected(data);
        });

        // 订阅通话结束
        const sub5 = this.stompClient.subscribe(`/user/queue/webrtc/call-ended`, (message) => {
            const data = JSON.parse(message.body);
            console.log('📞 通话结束:', data);
            this.handleCallEnded(data);
        });

        this.subscriptions = [sub1, sub2, sub2b, sub3, sub3b, sub4, sub5];
        console.log('✅ WebRTC消息订阅完成，共订阅', this.subscriptions.length, '个频道');

        // 延迟发送加入消息，确保订阅完成
        setTimeout(() => {
            this.stompClient.send('/app/webrtc/join', {}, JSON.stringify({
                userId: this.currentUser.id.toString()
            }));
            console.log('📤 已发送加入信令服务器消息，用户ID:', this.currentUser.id);
        }, 500);
    }

    /**
     * 处理来电邀请
     */
    handleCallInvitation(data) {
        console.log('🎯 处理来电邀请:', data);
        console.log('🔍 检查弹窗组件状态:');
        console.log('  - window.globalCallPopup存在:', !!window.globalCallPopup);
        console.log('  - GlobalCallPopup类存在:', typeof GlobalCallPopup !== 'undefined');

        // 确保弹窗组件存在
        if (!window.globalCallPopup && typeof GlobalCallPopup !== 'undefined') {
            console.log('🔧 创建全局弹窗组件...');
            window.globalCallPopup = new GlobalCallPopup();
        }

        // 显示全局弹窗
        if (window.globalCallPopup) {
            console.log('📱 显示来电弹窗...');
            try {
                window.globalCallPopup.show(data);
                console.log('✅ 来电弹窗已显示');
            } catch (error) {
                console.error('❌ 显示弹窗失败:', error);
                // 使用备用方案
                this.showFallbackDialog(data);
            }
        } else {
            console.warn('⚠️ 全局弹窗组件不可用，使用备用方案');
            this.showFallbackDialog(data);
        }
    }

    /**
     * 备用对话框方案
     */
    showFallbackDialog(data) {
        console.log('🔄 使用备用对话框方案');
        const accept = confirm(`收到来自 ${data.callerName || '未知用户'} 的${data.callType === 'video' ? '视频' : '语音'}通话邀请，是否接听？`);
        if (accept) {
            this.acceptCall(data.roomId);
        } else {
            this.rejectCall(data.roomId);
        }
    }

    /**
     * 接受通话
     */
    acceptCall(roomId) {
        if (this.stompClient) {
            this.stompClient.send('/app/webrtc/accept', {}, JSON.stringify({
                roomId: roomId,
                calleeId: this.currentUser.id.toString()
            }));
            console.log('✅ 已发送接受通话消息');
        }
    }

    /**
     * 拒绝通话
     */
    rejectCall(roomId) {
        if (this.stompClient) {
            this.stompClient.send('/app/webrtc/reject', {}, JSON.stringify({
                roomId: roomId,
                calleeId: this.currentUser.id.toString()
            }));
            console.log('❌ 已发送拒绝通话消息');
        }
    }

    /**
     * 处理通话被接受
     */
    handleCallAccepted(data) {
        console.log('🎉 ===== 通话被接受 =====');
        console.log('📋 接受数据:', data);
        console.log('🏠 当前页面:', window.location.href);
        console.log('🔗 即将跳转到通话页面...');

        // 如果有WebRTC客户端，也通知它
        if (this.webrtcClient && this.webrtcClient.handleCallAccepted) {
            console.log('📞 通知WebRTC客户端通话被接受');
            this.webrtcClient.handleCallAccepted(data);
        }

        // 跳转到通话页面（作为发起方）
        const callUrl = `/call?roomId=${data.roomId}&type=video&initiator=true`;
        console.log('🚀 跳转URL:', callUrl);

        // 延迟一点时间确保日志输出
        setTimeout(() => {
            window.location.href = callUrl;
        }, 100);
    }

    /**
     * 处理通话被拒绝
     */
    handleCallRejected(data) {
        console.log('❌ 通话被拒绝:', data);

        // 显示通知
        if (window.showNotification) {
            window.showNotification('通话被拒绝', 'warning');
        } else {
            alert('对方拒绝了通话');
        }
    }

    /**
     * 处理通话结束
     */
    handleCallEnded(data) {
        console.log('📞 通话结束:', data);

        // 如果在通话页面，返回聊天页面
        if (window.location.pathname.includes('/call')) {
            window.history.back();
        }

        // 显示通知
        if (window.showNotification) {
            window.showNotification('通话已结束', 'info');
        }
    }

    /**
     * 启动保活机制
     */
    startKeepAlive() {
        setInterval(() => {
            if (this.stompClient && this.currentUser) {
                this.stompClient.send('/app/webrtc/keepalive', {}, JSON.stringify({
                    userId: this.currentUser.id.toString()
                }));
                console.log('💓 发送保活消息');
            }
        }, 30000);
    }

    /**
     * 发起通话
     */
    async initiateCall(targetUserId, callType) {
        if (!this.webrtcClient) {
            throw new Error('WebRTC客户端未初始化');
        }

        console.log(`🚀 发起${callType}通话给用户:`, targetUserId);
        return await this.webrtcClient.initiateCall(targetUserId.toString(), callType);
    }

    /**
     * 获取状态
     */
    getStatus() {
        return {
            initialized: this.isInitialized,
            pageType: this.pageType,
            hasStompClient: !!this.stompClient,
            hasWebRTCClient: !!this.webrtcClient,
            hasCurrentUser: !!this.currentUser,
            subscriptionsCount: this.subscriptions.length
        };
    }
}

// 自动初始化全局WebRTC管理器
document.addEventListener('DOMContentLoaded', async function() {
    // 延迟初始化，确保其他脚本已加载
    setTimeout(async () => {
        try {
            if (!window.globalWebRTCManager) {
                window.globalWebRTCManager = new GlobalWebRTCManager();
                await window.globalWebRTCManager.initialize();
            }
        } catch (error) {
            console.error('自动初始化全局WebRTC管理器失败:', error);
        }
    }, 3000); // 3秒延迟
});

// 导出到全局
window.GlobalWebRTCManager = GlobalWebRTCManager;
