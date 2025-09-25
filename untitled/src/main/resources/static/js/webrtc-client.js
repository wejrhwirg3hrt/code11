/**
 * WebRTC客户端
 * 处理音视频通话功能
 */
class WebRTCClient {
    constructor() {
        this.localStream = null;
        this.remoteStream = null;
        this.peerConnection = null;
        this.stompClient = null;
        this.currentCallType = null; // 'audio' 或 'video'
        this.currentRoomId = null;
        this.currentUserId = null;
        this.isInitiator = false;
        this.callStatus = 'idle'; // 'idle', 'calling', 'ringing', 'connected', 'ended'
        
        // WebRTC配置
        this.rtcConfiguration = {
            iceServers: [
                { urls: 'stun:stun.l.google.com:19302' },
                { urls: 'stun:stun1.l.google.com:19302' }
            ]
        };
        
        // 事件回调
        this.onCallInvitation = null;
        this.onCallAccepted = null;
        this.onCallRejected = null;
        this.onCallEnded = null;
        this.onRemoteStream = null;
        this.onLocalStream = null;
        this.onConnectionStateChange = null;
    }
    
    /**
     * 初始化WebRTC客户端
     */
    async initialize(userId) {
        try {
            console.log('🚀 开始初始化WebRTC客户端，用户ID:', userId);

            if (!userId) {
                throw new Error('用户ID不能为空');
            }

            this.currentUserId = userId;

            // 检查浏览器支持
            if (!this.checkBrowserSupport()) {
                throw new Error('浏览器不支持WebRTC功能');
            }

            // 连接到信令服务器
            await this.connectToSignalingServer();

            console.log('✅ WebRTC客户端初始化成功');

        } catch (error) {
            console.error('❌ WebRTC客户端初始化失败:', error);
            throw error;
        }
    }

    /**
     * 检查浏览器支持
     */
    checkBrowserSupport() {
        if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
            console.error('浏览器不支持getUserMedia');
            return false;
        }

        if (!window.RTCPeerConnection) {
            console.error('浏览器不支持RTCPeerConnection');
            return false;
        }

        if (typeof SockJS === 'undefined') {
            console.error('SockJS库未加载');
            return false;
        }

        if (typeof Stomp === 'undefined') {
            console.error('Stomp库未加载');
            return false;
        }

        return true;
    }

    /**
     * 初始化WebRTC客户端 (别名)
     */
    async init(userId) {
        return this.initialize(userId);
    }
    
    /**
     * 连接到信令服务器
     */
    async connectToSignalingServer() {
        return new Promise((resolve, reject) => {
            try {
                console.log('🔗 连接到信令服务器...');

                // 创建WebSocket连接
                const socket = new SockJS('/ws');

                // 检查Stomp版本并使用相应的API
                if (typeof Stomp !== 'undefined' && Stomp.over) {
                    // 旧版本Stomp (2.x)
                    this.stompClient = Stomp.over(socket);
                } else if (typeof Stomp !== 'undefined' && Stomp.Client) {
                    // 新版本Stomp (7.x)
                    this.stompClient = new Stomp.Client({
                        webSocketFactory: () => socket,
                        debug: (str) => {
                            console.log('STOMP Debug:', str);
                        }
                    });
                } else {
                    throw new Error('Stomp库未正确加载');
                }

                // 设置连接超时
                const connectTimeout = setTimeout(() => {
                    console.error('❌ 信令服务器连接超时');
                    reject(new Error('信令服务器连接超时'));
                }, 10000); // 10秒超时

                // 连接成功回调
                const connectCallback = (frame) => {
                    clearTimeout(connectTimeout);
                    console.log('✅ 信令服务器连接成功:', frame);

                    try {
                        // 设置信令处理器
                        this.setupSignalingHandlers();

                        // 发送加入信令服务器消息
                        this.stompClient.send('/app/webrtc/join', {}, JSON.stringify({
                            userId: this.currentUserId
                        }));
                        console.log('📤 已发送加入信令服务器消息');

                        resolve();
                    } catch (error) {
                        console.error('❌ 设置信令处理器失败:', error);
                        reject(error);
                    }
                };

                // 连接失败回调
                const errorCallback = (error) => {
                    clearTimeout(connectTimeout);
                    console.error('❌ 信令服务器连接失败:', error);
                    reject(new Error('信令服务器连接失败: ' + (error.message || error)));
                };

                // 执行连接
                if (this.stompClient.connect) {
                    // 旧版本API
                    this.stompClient.connect({}, connectCallback, errorCallback);
                } else if (this.stompClient.activate) {
                    // 新版本API
                    this.stompClient.onConnect = connectCallback;
                    this.stompClient.onStompError = errorCallback;
                    this.stompClient.activate();
                } else {
                    throw new Error('无法识别的Stomp客户端版本');
                }

            } catch (error) {
                console.error('❌ 创建信令连接失败:', error);
                reject(error);
            }
        });
    }
    
    /**
     * 设置信令处理器
     */
    setupSignalingHandlers() {
        try {
            console.log('🔧 设置信令处理器...');

            // 订阅个人WebRTC消息
            this.stompClient.subscribe(`/user/${this.currentUserId}/queue/webrtc`, (message) => {
                try {
                    const data = JSON.parse(message.body);
                    this.handleSignalingMessage(data);
                } catch (error) {
                    console.error('❌ 解析信令消息失败:', error, message.body);
                }
            });

            // 订阅房间信令消息
            this.stompClient.subscribe(`/topic/webrtc-signal`, (message) => {
                try {
                    const data = JSON.parse(message.body);
                    // 只处理与当前房间相关的消息
                    if (data.roomId === this.currentRoomId) {
                        this.handleSignalingMessage(data);
                    }
                } catch (error) {
                    console.error('❌ 解析房间信令消息失败:', error, message.body);
                }
            });

            console.log('✅ 信令处理器设置完成');

        } catch (error) {
            console.error('❌ 设置信令处理器失败:', error);
            throw error;
        }
    }
    
    /**
     * 处理信令消息
     */
    handleSignalingMessage(data) {
        console.log('📨 收到信令消息:', data.type, data);

        try {
            switch (data.type) {
                case 'offer':
                    this.handleOffer(data);
                    break;
                case 'answer':
                    this.handleAnswer(data);
                    break;
                case 'ice-candidate':
                    this.handleIceCandidate(data);
                    break;
                case 'call-invitation':
                    this.handleCallInvitation(data);
                    break;
                case 'call-accepted':
                    this.handleCallAccepted(data);
                    break;
                case 'call-rejected':
                    this.handleCallRejected(data);
                    break;
                case 'call-ended':
                    this.handleCallEnded(data);
                    break;
                default:
                    console.warn('⚠️ 未知的信令消息类型:', data.type);
                    break;
            }
        } catch (error) {
            console.error('❌ 处理信令消息失败:', error, data);
        }
    }
    
    /**
     * 获取本地媒体流
     */
    async getLocalStream(callType = 'video') {
        try {
            console.log(`🎤 获取本地${callType}流...`);
            
            const constraints = {
                audio: true,
                video: callType === 'video'
            };
            
            this.localStream = await navigator.mediaDevices.getUserMedia(constraints);
            this.currentCallType = callType;

            // 触发本地流回调
            if (this.onLocalStream) {
                this.onLocalStream(this.localStream);
            }

            console.log('✅ 本地媒体流获取成功');
            return this.localStream;
            
        } catch (error) {
            console.error('❌ 获取本地媒体流失败:', error);
            throw new Error('无法获取摄像头/麦克风权限');
        }
    }
    
    /**
     * 创建PeerConnection
     */
    createPeerConnection() {
        try {
            console.log('🔗 创建PeerConnection...');

            // 如果已存在连接，先关闭
            if (this.peerConnection) {
                console.log('🔄 关闭现有PeerConnection');
                this.peerConnection.close();
            }

            this.peerConnection = new RTCPeerConnection(this.rtcConfiguration);

            // 添加本地流
            if (this.localStream) {
                console.log('📤 添加本地流到PeerConnection');
                this.localStream.getTracks().forEach(track => {
                    this.peerConnection.addTrack(track, this.localStream);
                });
            }

            // 处理远程流
            this.peerConnection.ontrack = (event) => {
                console.log('📺 收到远程流');
                this.remoteStream = event.streams[0];
                if (this.onRemoteStream) {
                    this.onRemoteStream(this.remoteStream);
                }
            };

            // 处理ICE候选
            this.peerConnection.onicecandidate = (event) => {
                if (event.candidate) {
                    console.log('🧊 发送ICE候选');
                    this.sendSignalingMessage({
                        type: 'ice-candidate',
                        candidate: event.candidate,
                        roomId: this.currentRoomId
                    });
                } else {
                    console.log('🧊 ICE候选收集完成');
                }
            };

            // 连接状态变化
            this.peerConnection.onconnectionstatechange = () => {
                const state = this.peerConnection.connectionState;
                console.log('🔗 连接状态变化:', state);
                if (this.onConnectionStateChange) {
                    this.onConnectionStateChange(state);
                }
            };

            // ICE连接状态变化
            this.peerConnection.oniceconnectionstatechange = () => {
                const state = this.peerConnection.iceConnectionState;
                console.log('🧊 ICE连接状态变化:', state);
            };

            console.log('✅ PeerConnection创建完成');

        } catch (error) {
            console.error('❌ 创建PeerConnection失败:', error);
            throw error;
        }
    }
    
    /**
     * 加入房间
     */
    async joinRoom(roomId, callType = 'video', isInitiator = false) {
        this.currentRoomId = roomId;
        this.currentCallType = callType;
        this.isInitiator = isInitiator;
        
        console.log(`🏠 加入房间: ${roomId}, 类型: ${callType}, 发起者: ${isInitiator}`);
        
        // 获取本地媒体流
        if (!this.localStream) {
            await this.getLocalStream(callType);
        }
        
        // 创建PeerConnection
        this.createPeerConnection();
        
        if (isInitiator) {
            // 发起者创建offer
            await this.createOffer();
        }
        
        console.log('✅ 成功加入房间');
    }
    
    /**
     * 创建Offer
     */
    async createOffer() {
        try {
            console.log('📤 创建Offer...');

            const offer = await this.peerConnection.createOffer();
            await this.peerConnection.setLocalDescription(offer);

            this.sendSignalingMessage({
                type: 'offer',
                offer: offer,
                roomId: this.currentRoomId
            });

            console.log('✅ Offer创建并发送成功');

        } catch (error) {
            console.error('❌ 创建Offer失败:', error);
            throw error;
        }
    }

    /**
     * 创建并发送Offer (别名方法)
     */
    async createAndSendOffer() {
        return this.createOffer();
    }
    
    /**
     * 处理Offer
     */
    async handleOffer(data) {
        try {
            console.log('📥 处理Offer...');
            
            if (!this.peerConnection) {
                this.createPeerConnection();
            }
            
            await this.peerConnection.setRemoteDescription(data.offer);
            
            const answer = await this.peerConnection.createAnswer();
            await this.peerConnection.setLocalDescription(answer);
            
            this.sendSignalingMessage({
                type: 'answer',
                answer: answer,
                roomId: this.currentRoomId
            });
            
            console.log('✅ Answer创建并发送成功');
            
        } catch (error) {
            console.error('❌ 处理Offer失败:', error);
        }
    }
    
    /**
     * 处理Answer
     */
    async handleAnswer(data) {
        try {
            console.log('📥 处理Answer...');
            await this.peerConnection.setRemoteDescription(data.answer);
            console.log('✅ Answer处理成功');
        } catch (error) {
            console.error('❌ 处理Answer失败:', error);
        }
    }
    
    /**
     * 处理ICE候选
     */
    async handleIceCandidate(data) {
        try {
            console.log('🧊 处理ICE候选...');
            await this.peerConnection.addIceCandidate(data.candidate);
            console.log('✅ ICE候选添加成功');
        } catch (error) {
            console.error('❌ 处理ICE候选失败:', error);
        }
    }
    
    /**
     * 发送信令消息
     */
    sendSignalingMessage(message) {
        try {
            if (!message) {
                console.error('❌ 信令消息不能为空');
                return false;
            }

            if (!this.stompClient) {
                console.error('❌ STOMP客户端未初始化');
                return false;
            }

            // 检查连接状态
            const isConnected = this.stompClient.connected ||
                               (this.stompClient.state && this.stompClient.state === 'CONNECTED');

            if (!isConnected) {
                console.error('❌ 信令服务器未连接，消息类型:', message.type);
                return false;
            }

            // 添加发送者信息
            message.senderId = this.currentUserId;
            message.timestamp = Date.now();

            this.stompClient.send('/app/webrtc-signal', {}, JSON.stringify(message));
            console.log('📤 信令消息已发送:', message.type, message);
            return true;

        } catch (error) {
            console.error('❌ 发送信令消息失败:', error, message);
            return false;
        }
    }
    
    /**
     * 结束通话
     */
    endCall() {
        console.log('📞 结束通话');
        
        // 停止本地流
        if (this.localStream) {
            this.localStream.getTracks().forEach(track => track.stop());
            this.localStream = null;
        }
        
        // 关闭PeerConnection
        if (this.peerConnection) {
            this.peerConnection.close();
            this.peerConnection = null;
        }
        
        // 发送结束通话消息
        this.sendSignalingMessage({
            type: 'call-ended',
            roomId: this.currentRoomId
        });
        
        this.callStatus = 'ended';
        
        if (this.onCallEnded) {
            this.onCallEnded();
        }
    }
    
    /**
     * 处理通话邀请
     */
    handleCallInvitation(data) {
        console.log('📞 收到通话邀请');
        if (this.onCallInvitation) {
            this.onCallInvitation(data);
        }
    }
    
    /**
     * 处理通话接受
     */
    handleCallAccepted(data) {
        console.log('✅ 通话被接受');
        if (this.onCallAccepted) {
            this.onCallAccepted(data);
        }
    }
    
    /**
     * 处理通话拒绝
     */
    handleCallRejected(data) {
        console.log('❌ 通话被拒绝');
        if (this.onCallRejected) {
            this.onCallRejected(data);
        }
    }
    
    /**
     * 处理通话结束
     */
    handleCallEnded(data) {
        console.log('📞 通话已结束');
        this.endCall();
    }

    /**
     * 发起通话
     */
    async initiateCall(targetUserId, callType = 'video') {
        try {
            console.log(`🚀 发起${callType}通话给用户:`, targetUserId);

            if (!this.stompClient) {
                throw new Error('信令服务器未连接');
            }

            if (!targetUserId) {
                throw new Error('目标用户ID不能为空');
            }

            // 生成房间ID
            const roomId = `room_${this.currentUserId}_${targetUserId}_${Date.now()}`;
            this.currentRoomId = roomId;
            this.currentCallType = callType;
            this.isInitiator = true;
            this.callStatus = 'calling';

            // 获取本地媒体流
            await this.getLocalStream(callType);

            // 发送通话邀请
            const invitationMessage = {
                callerId: this.currentUserId.toString(),
                calleeId: targetUserId.toString(),
                callType: callType
            };

            this.stompClient.send('/app/webrtc/call', {}, JSON.stringify(invitationMessage));
            console.log('📤 通话邀请已发送:', invitationMessage);

            return roomId;

        } catch (error) {
            console.error('❌ 发起通话失败:', error);
            this.callStatus = 'idle';
            throw error;
        }
    }

    /**
     * 接受通话
     */
    async acceptCall(roomId, callType = 'video') {
        try {
            console.log(`✅ 接受${callType}通话，房间ID:`, roomId);

            this.currentRoomId = roomId;
            this.currentCallType = callType;
            this.isInitiator = false;
            this.callStatus = 'connected';

            // 获取本地媒体流
            await this.getLocalStream(callType);

            // 创建PeerConnection
            this.createPeerConnection();

            // 发送接受消息
            const acceptMessage = {
                type: 'call-accepted',
                roomId: roomId,
                calleeId: this.currentUserId,
                timestamp: Date.now()
            };

            this.stompClient.send('/app/webrtc/accept', {}, JSON.stringify(acceptMessage));
            console.log('📤 通话接受消息已发送:', acceptMessage);

        } catch (error) {
            console.error('❌ 接受通话失败:', error);
            this.callStatus = 'idle';
            throw error;
        }
    }

    /**
     * 拒绝通话
     */
    rejectCall(roomId) {
        try {
            console.log('❌ 拒绝通话，房间ID:', roomId);

            const rejectMessage = {
                type: 'call-rejected',
                roomId: roomId,
                calleeId: this.currentUserId,
                timestamp: Date.now()
            };

            this.stompClient.send('/app/webrtc/reject', {}, JSON.stringify(rejectMessage));
            console.log('📤 通话拒绝消息已发送:', rejectMessage);

            this.callStatus = 'idle';

        } catch (error) {
            console.error('❌ 拒绝通话失败:', error);
        }
    }
}

// 将WebRTCClient设置为全局变量
window.WebRTCClient = WebRTCClient;
