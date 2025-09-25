/**
 * 全局来电弹窗组件
 * 在整个应用中提供统一的来电弹窗功能
 */
class GlobalCallPopup {
    constructor() {
        this.isInitialized = false;
        this.currentCallData = null;
        this.popupElement = null;
        this.ringtoneAudio = null;
        
        this.init();
    }
    
    /**
     * 初始化全局来电弹窗
     */
    init() {
        if (this.isInitialized) {
            return;
        }
        
        // 创建弹窗HTML结构
        this.createPopupHTML();
        
        // 绑定事件监听器
        this.bindEvents();
        
        // 初始化铃声
        this.initRingtone();
        
        this.isInitialized = true;
        console.log('全局来电弹窗已初始化');
    }
    
    /**
     * 创建弹窗HTML结构
     */
    createPopupHTML() {
        // 检查是否已存在
        if (document.getElementById('globalCallPopup')) {
            this.popupElement = document.getElementById('globalCallPopup');
            return;
        }
        
        const popupHTML = `
            <div id="globalCallPopup" class="global-call-popup" style="display: none;">
                <div class="call-popup-overlay"></div>
                <div class="call-popup-content">
                    <div class="call-popup-header">
                        <div class="caller-avatar">
                            <img id="globalCallerAvatar" src="/images/default-avatar.png" alt="来电者头像">
                        </div>
                        <div class="caller-info">
                            <div class="caller-name" id="globalCallerName">未知用户</div>
                            <div class="call-type" id="globalCallType">语音通话</div>
                        </div>
                    </div>
                    
                    <div class="call-popup-controls">
                        <button id="globalRejectBtn" class="call-btn reject-btn" title="拒绝">
                            <i class="fas fa-phone-slash"></i>
                        </button>
                        <button id="globalAcceptBtn" class="call-btn accept-btn" title="接听">
                            <i class="fas fa-phone"></i>
                        </button>
                    </div>
                    
                    <div class="call-popup-actions">
                        <small class="text-muted">按ESC键拒绝通话</small>
                    </div>
                </div>
            </div>
        `;
        
        // 添加到body
        document.body.insertAdjacentHTML('beforeend', popupHTML);
        this.popupElement = document.getElementById('globalCallPopup');
        
        // 添加CSS样式
        this.addStyles();
    }
    
    /**
     * 添加CSS样式
     */
    addStyles() {
        if (document.getElementById('globalCallPopupStyles')) {
            return;
        }
        
        const styles = `
            <style id="globalCallPopupStyles">
                .global-call-popup {
                    position: fixed;
                    top: 0;
                    left: 0;
                    width: 100%;
                    height: 100%;
                    z-index: 10000;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                }
                
                .call-popup-overlay {
                    position: absolute;
                    top: 0;
                    left: 0;
                    width: 100%;
                    height: 100%;
                    background: rgba(0, 0, 0, 0.8);
                    backdrop-filter: blur(5px);
                }
                
                .call-popup-content {
                    position: relative;
                    background: white;
                    border-radius: 20px;
                    padding: 30px;
                    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
                    text-align: center;
                    min-width: 320px;
                    animation: callPopupSlideIn 0.3s ease-out;
                }
                
                @keyframes callPopupSlideIn {
                    from {
                        transform: translateY(-50px);
                        opacity: 0;
                    }
                    to {
                        transform: translateY(0);
                        opacity: 1;
                    }
                }
                
                .caller-avatar img {
                    width: 80px;
                    height: 80px;
                    border-radius: 50%;
                    object-fit: cover;
                    border: 4px solid #007bff;
                    margin-bottom: 15px;
                }
                
                .caller-name {
                    font-size: 1.5rem;
                    font-weight: 600;
                    color: #333;
                    margin-bottom: 5px;
                }
                
                .call-type {
                    font-size: 1rem;
                    color: #666;
                    margin-bottom: 25px;
                }
                
                .call-popup-controls {
                    display: flex;
                    justify-content: center;
                    gap: 30px;
                    margin-bottom: 20px;
                }
                
                .call-btn {
                    width: 60px;
                    height: 60px;
                    border-radius: 50%;
                    border: none;
                    font-size: 1.5rem;
                    cursor: pointer;
                    transition: all 0.3s ease;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                }
                
                .reject-btn {
                    background: #dc3545;
                    color: white;
                }
                
                .reject-btn:hover {
                    background: #c82333;
                    transform: scale(1.1);
                }
                
                .accept-btn {
                    background: #28a745;
                    color: white;
                }
                
                .accept-btn:hover {
                    background: #218838;
                    transform: scale(1.1);
                }
                
                .call-popup-actions {
                    text-align: center;
                }
                
                /* 响应式设计 */
                @media (max-width: 480px) {
                    .call-popup-content {
                        margin: 20px;
                        padding: 20px;
                        min-width: auto;
                        width: calc(100% - 40px);
                    }
                    
                    .caller-avatar img {
                        width: 60px;
                        height: 60px;
                    }
                    
                    .caller-name {
                        font-size: 1.3rem;
                    }
                    
                    .call-btn {
                        width: 50px;
                        height: 50px;
                        font-size: 1.2rem;
                    }
                }
            </style>
        `;
        
        document.head.insertAdjacentHTML('beforeend', styles);
    }
    
    /**
     * 绑定事件监听器
     */
    bindEvents() {
        // 接听按钮
        document.addEventListener('click', (e) => {
            if (e.target.id === 'globalAcceptBtn' || e.target.closest('#globalAcceptBtn')) {
                this.acceptCall();
            }
        });
        
        // 拒绝按钮
        document.addEventListener('click', (e) => {
            if (e.target.id === 'globalRejectBtn' || e.target.closest('#globalRejectBtn')) {
                this.rejectCall();
            }
        });
        
        // ESC键拒绝通话
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && this.isVisible()) {
                this.rejectCall();
            }
        });
        
        // 点击遮罩层关闭（可选）
        document.addEventListener('click', (e) => {
            if (e.target.classList.contains('call-popup-overlay') && this.isVisible()) {
                // 可以选择是否允许点击遮罩层关闭
                // this.rejectCall();
            }
        });
    }
    
    /**
     * 初始化铃声
     */
    initRingtone() {
        try {
            // 这里可以添加铃声文件
            // this.ringtoneAudio = new Audio('/sounds/ringtone.mp3');
            // this.ringtoneAudio.loop = true;
        } catch (error) {
            console.warn('铃声初始化失败:', error);
        }
    }
    
    /**
     * 显示来电弹窗
     */
    show(callData) {
        console.log('🎯 显示全局来电弹窗:', callData);
        console.log('🔍 弹窗组件状态检查:');
        console.log('  - 已初始化:', this.isInitialized);
        console.log('  - 弹窗元素存在:', !!this.popupElement);
        console.log('  - 当前通话数据:', this.currentCallData);

        if (!this.isInitialized) {
            console.warn('⚠️ 弹窗组件未初始化，尝试重新初始化...');
            this.init();
        }

        if (!this.popupElement) {
            console.error('❌ 弹窗元素不存在，无法显示弹窗');
            return;
        }

        this.currentCallData = callData;

        // 更新弹窗内容
        console.log('📝 更新弹窗内容...');
        this.updatePopupContent(callData);

        // 显示弹窗
        console.log('👁️ 显示弹窗元素...');
        this.popupElement.style.display = 'flex';
        console.log('✅ 弹窗已设置为可见');

        // 播放铃声
        this.playRingtone();

        // 添加body类以防止滚动
        document.body.classList.add('call-popup-active');

        console.log('🎉 来电弹窗显示完成');
    }
    
    /**
     * 隐藏来电弹窗
     */
    hide() {
        console.log('隐藏全局来电弹窗');
        
        if (this.popupElement) {
            this.popupElement.style.display = 'none';
        }
        
        // 停止铃声
        this.stopRingtone();
        
        // 移除body类
        document.body.classList.remove('call-popup-active');
        
        this.currentCallData = null;
    }
    
    /**
     * 更新弹窗内容
     */
    updatePopupContent(callData) {
        console.log('📝 更新弹窗内容，数据:', callData);

        const callerName = callData.callerName || callData.caller?.username || '未知用户';
        const callType = callData.callType === 'video' ? '视频通话' : '语音通话';
        const callerAvatar = callData.callerAvatar || callData.caller?.avatar || '/images/default-avatar.png';

        console.log('📋 解析后的显示信息:');
        console.log('  - 来电者姓名:', callerName);
        console.log('  - 通话类型:', callType);
        console.log('  - 头像地址:', callerAvatar);

        // 检查DOM元素是否存在
        const nameElement = document.getElementById('globalCallerName');
        const typeElement = document.getElementById('globalCallType');
        const avatarElement = document.getElementById('globalCallerAvatar');

        console.log('🔍 DOM元素检查:');
        console.log('  - 姓名元素存在:', !!nameElement);
        console.log('  - 类型元素存在:', !!typeElement);
        console.log('  - 头像元素存在:', !!avatarElement);

        if (nameElement) nameElement.textContent = callerName;
        if (typeElement) typeElement.textContent = callType;
        if (avatarElement) avatarElement.src = callerAvatar;

        console.log('✅ 弹窗内容更新完成');
    }
    
    /**
     * 接听通话
     */
    acceptCall() {
        console.log('🎯 接听通话');

        if (this.currentCallData) {
            const roomId = this.currentCallData.roomId;
            const callType = this.currentCallData.callType || 'video';

            if (roomId) {
                // 发送接受通话消息
                if (window.globalWebRTCManager && window.globalWebRTCManager.stompClient) {
                    window.globalWebRTCManager.stompClient.send('/app/webrtc/accept', {}, JSON.stringify({
                        roomId: roomId,
                        calleeId: window.globalWebRTCManager.currentUser.id.toString()
                    }));
                    console.log('✅ 已发送接受通话消息');
                }

                // 如果在聊天页面，调用聊天页面的接听函数
                if (typeof window.acceptCall === 'function') {
                    console.log('📞 调用聊天页面的接听函数');
                    this.hide();
                    window.acceptCall();
                    return;
                }

                // 否则跳转到通话页面（作为接收方）
                const callUrl = `/call?roomId=${roomId}&type=${callType}&initiator=false`;
                console.log('🚀 跳转到通话页面:', callUrl);

                this.hide();
                window.location.href = callUrl;
            } else {
                console.error('缺少roomId，无法接听通话');
            }
        } else {
            console.error('缺少通话数据');
        }
    }

    /**
     * 拒绝通话
     */
    rejectCall() {
        console.log('拒绝通话');

        // 如果在聊天页面，调用聊天页面的拒绝函数
        if (typeof window.rejectCall === 'function') {
            console.log('📞 调用聊天页面的拒绝函数');
            this.hide();
            window.rejectCall();
            return;
        }

        // 否则使用WebRTC客户端拒绝
        if (this.currentCallData && window.webrtcClient) {
            const roomId = this.currentCallData.roomId;
            if (roomId) {
                window.webrtcClient.rejectCall(roomId);
            } else {
                console.error('缺少roomId，无法拒绝通话');
            }
        }

        this.hide();
    }
    
    /**
     * 播放铃声
     */
    playRingtone() {
        try {
            if (this.ringtoneAudio) {
                this.ringtoneAudio.play();
            }
        } catch (error) {
            console.warn('播放铃声失败:', error);
        }
    }
    
    /**
     * 停止铃声
     */
    stopRingtone() {
        try {
            if (this.ringtoneAudio) {
                this.ringtoneAudio.pause();
                this.ringtoneAudio.currentTime = 0;
            }
        } catch (error) {
            console.warn('停止铃声失败:', error);
        }
    }
    
    /**
     * 检查弹窗是否可见
     */
    isVisible() {
        return this.popupElement && this.popupElement.style.display !== 'none';
    }
}

// 创建全局实例
window.globalCallPopup = new GlobalCallPopup();

// 导出类以供其他模块使用
if (typeof module !== 'undefined' && module.exports) {
    module.exports = GlobalCallPopup;
}
