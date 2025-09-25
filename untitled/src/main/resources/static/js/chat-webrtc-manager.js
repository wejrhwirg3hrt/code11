/**
 * 聊天页面专用的WebRTC管理器
 * 统一管理WebRTC连接、消息订阅和通话处理
 */
class ChatWebRTCManager {
    constructor() {
        this.isInitialized = false;
        this.webrtcClient = null;
        this.stompClient = null;
        this.currentUser = null;
        this.subscriptions = [];
        this.connectionAttempts = 0;
        this.maxConnectionAttempts = 20;
    }

    /**
     * 初始化WebRTC管理器
     */
    async initialize() {
        if (this.isInitialized) {
            console.log('WebRTC管理器已初始化');
            return;
        }

        try {
            console.log('=== 开始初始化统一WebRTC管理器 ===');

            // 1. 初始化全局来电弹窗
            await this.initGlobalPopup();

            // 2. 获取用户信息
            await this.getCurrentUser();

            // 3. 等待并复用聊天WebSocket连接
            await this.setupWebSocketConnection();

            // 4. 创建并初始化WebRTC客户端
            await this.initWebRTCClient();

            // 5. 订阅所有WebRTC消息
            this.subscribeToAllMessages();

            // 6. 启动保活机制
            this.startKeepAlive();

            this.isInitialized = true;
            console.log('=== 统一WebRTC管理器初始化成功 ===');

        } catch (error) {
            console.error('WebRTC管理器初始化失败:', error);
            throw error;
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
        if (window.currentUser && window.currentUser.id) {
            this.currentUser = window.currentUser;
            console.log('✅ 获取用户信息成功:', this.currentUser.username);
        } else {
            throw new Error('无法获取当前用户信息');
        }
    }

    /**
     * 设置WebSocket连接
     */
    async setupWebSocketConnection() {
        return new Promise((resolve, reject) => {
            console.log('等待聊天WebSocket连接就绪...');
            
            const checkConnection = () => {
                this.connectionAttempts++;
                console.log(`检查连接状态 (${this.connectionAttempts}/${this.maxConnectionAttempts}):`, {
                    'ChatApp存在': !!window.ChatApp,
                    'stompClient存在': !!(window.ChatApp && window.ChatApp.stompClient),
                    '连接状态': !!(window.ChatApp && window.ChatApp.connected)
                });
                
                if (window.ChatApp && window.ChatApp.stompClient) {
                    console.log('✅ 复用聊天WebSocket连接');
                    this.stompClient = window.ChatApp.stompClient;
                    window.stompClient = this.stompClient;
                    resolve();
                } else if (this.connectionAttempts >= this.maxConnectionAttempts) {
                    console.warn('⚠️ 等待聊天连接超时，创建独立连接');
                    this.createIndependentConnection().then(resolve).catch(reject);
                } else {
                    setTimeout(checkConnection, 500);
                }
            };
            
            checkConnection();
        });
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
        if (typeof WebRTCClient === 'undefined') {
            throw new Error('WebRTCClient类未加载');
        }

        this.webrtcClient = new WebRTCClient();
        this.webrtcClient.stompClient = this.stompClient;
        this.webrtcClient.currentUserId = this.currentUser.id.toString();

        // 设置全局变量
        window.webrtcClient = this.webrtcClient;

        console.log('✅ WebRTC客户端创建成功');
    }

    /**
     * 订阅所有WebRTC消息
     */
    subscribeToAllMessages() {
        if (!this.stompClient || !this.currentUser) {
            console.error('❌ 无法订阅消息：连接或用户信息缺失');
            return;
        }

        console.log('开始订阅所有WebRTC消息...');

        // 订阅通话邀请（用户队列）
        const sub1 = this.stompClient.subscribe(`/user/queue/webrtc/call-invitation`, (message) => {
            const data = JSON.parse(message.body);
            console.log('🔔 收到通话邀请(用户队列):', data);
            this.handleCallInvitation(data);
        });

        // 订阅通话邀请（广播频道）
        const sub2 = this.stompClient.subscribe(`/topic/webrtc/call-invitation/${this.currentUser.id}`, (message) => {
            const data = JSON.parse(message.body);
            console.log('🔔 收到通话邀请(广播频道):', data);
            this.handleCallInvitation(data);
        });

        // 订阅通话接受
        const sub3 = this.stompClient.subscribe(`/user/queue/webrtc/call-accepted`, (message) => {
            const data = JSON.parse(message.body);
            console.log('✅ 通话被接受:', data);
        });

        // 订阅通话拒绝
        const sub4 = this.stompClient.subscribe(`/user/queue/webrtc/call-rejected`, (message) => {
            const data = JSON.parse(message.body);
            console.log('❌ 通话被拒绝:', data);
        });

        // 订阅信令消息
        const sub5 = this.stompClient.subscribe(`/user/queue/webrtc/signal`, (message) => {
            const data = JSON.parse(message.body);
            console.log('📡 收到信令消息:', data);
        });

        this.subscriptions = [sub1, sub2, sub3, sub4, sub5];
        console.log('✅ 所有WebRTC消息订阅完成');

        // 发送加入信令服务器消息
        this.stompClient.send('/app/webrtc/join', {}, JSON.stringify({
            userId: this.currentUser.id.toString()
        }));
        console.log('📤 已发送加入信令服务器消息');
    }

    /**
     * 处理来电邀请
     */
    handleCallInvitation(data) {
        console.log('🎯 处理来电邀请:', data);
        console.log('GlobalCallPopup存在:', !!window.globalCallPopup);
        console.log('GlobalCallPopup类型:', typeof window.globalCallPopup);

        // 显示全局弹窗
        if (window.globalCallPopup && typeof window.globalCallPopup.show === 'function') {
            try {
                window.globalCallPopup.show(data);
                console.log('📱 来电弹窗已显示');
            } catch (error) {
                console.error('显示弹窗失败:', error);
                this.showFallbackDialog(data);
            }
        } else {
            console.warn('全局弹窗不可用，使用备用方案');
            this.showFallbackDialog(data);
        }
    }

    /**
     * 显示备用对话框
     */
    showFallbackDialog(data) {
        // 备用方案：浏览器确认对话框
        const accept = confirm(`收到来自 ${data.callerName} 的${data.callType}通话邀请，是否接听？`);
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
     * 启动保活机制
     */
    startKeepAlive() {
        // 每30秒发送保活消息
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
     * 发起通话的统一接口
     */
    async initiateCall(targetUserId, callType) {
        if (!this.webrtcClient) {
            throw new Error('WebRTC客户端未初始化');
        }

        console.log(`🚀 发起${callType}通话给用户:`, targetUserId);
        return await this.webrtcClient.initiateCall(targetUserId.toString(), callType);
    }

    /**
     * 获取连接状态
     */
    getStatus() {
        return {
            initialized: this.isInitialized,
            hasStompClient: !!this.stompClient,
            hasWebRTCClient: !!this.webrtcClient,
            hasCurrentUser: !!this.currentUser,
            subscriptionsCount: this.subscriptions.length
        };
    }
}

// 导出到全局
window.ChatWebRTCManager = ChatWebRTCManager;
