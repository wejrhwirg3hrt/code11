/**
 * 全局智能助手状态管理器
 * 负责在所有页面间保持小智的状态和对话历史
 */
class GlobalSmartPetManager {
    constructor() {
        this.storageKey = 'smartPetState';
        this.chatHistoryKey = 'smartPetChatHistory';
        this.maxHistoryLength = 50; // 最多保存50条对话记录
        this.currentState = this.loadState();
        this.chatHistory = this.loadChatHistory();
        this.isVisible = false;
        this.currentPage = window.location.pathname;
        
        this.init();
    }

    init() {
        this.createGlobalPetContainer();
        this.bindEvents();
        this.restoreState();
        
        // 监听页面变化
        this.observePageChanges();
    }

    /**
     * 创建全局智能助手容器
     */
    createGlobalPetContainer() {
        // 检查是否已存在
        if (document.getElementById('global-smart-pet')) {
            return;
        }

        const container = document.createElement('div');
        container.id = 'global-smart-pet';
        container.innerHTML = `
            <!-- 智能助手触发按钮 -->
            <div id="smart-pet-trigger" class="smart-pet-trigger">
                <div class="pet-avatar">
                    <div class="pet-face">
                        <div class="pet-eyes">
                            <div class="eye left"></div>
                            <div class="eye right"></div>
                        </div>
                        <div class="pet-mouth"></div>
                    </div>
                    <div class="pet-notification-dot" style="display: none;"></div>
                </div>
                <div class="pet-speech-bubble" style="display: none;">
                    <span class="bubble-text">点击和我聊天吧！</span>
                </div>
            </div>

            <!-- 智能助手聊天窗口 -->
            <div id="smart-pet-chat" class="smart-pet-chat" style="display: none;">
                <div class="chat-header">
                    <div class="header-left">
                        <div class="pet-avatar-small">
                            <div class="pet-face-small">
                                <div class="pet-eyes-small">
                                    <div class="eye-small left"></div>
                                    <div class="eye-small right"></div>
                                </div>
                                <div class="pet-mouth-small"></div>
                            </div>
                        </div>
                        <span class="pet-name">小智助手</span>
                    </div>
                    <div class="header-right">
                        <button class="btn-minimize" title="最小化">−</button>
                        <button class="btn-close" title="关闭">×</button>
                    </div>
                </div>
                <div class="chat-messages" id="pet-chat-messages">
                    <!-- 消息将在这里显示 -->
                </div>
                <div class="chat-input-area">
                    <div class="input-group">
                        <input type="text" id="pet-chat-input" placeholder="和小智聊聊吧..." maxlength="500">
                        <button id="pet-send-btn" class="send-btn">
                            <i class="fas fa-paper-plane"></i>
                        </button>
                    </div>
                    <div class="quick-actions">
                        <button class="quick-btn" data-action="recommend">推荐视频</button>
                        <button class="quick-btn" data-action="help">使用帮助</button>
                        <button class="quick-btn" data-action="clear">清空对话</button>
                    </div>
                </div>
            </div>
        `;

        // 添加样式
        this.addStyles();
        
        document.body.appendChild(container);
        
        // 绑定事件
        this.bindPetEvents();
    }

    /**
     * 添加样式
     */
    addStyles() {
        if (document.getElementById('global-smart-pet-styles')) {
            return;
        }

        const style = document.createElement('style');
        style.id = 'global-smart-pet-styles';
        style.textContent = `
            .smart-pet-trigger {
                position: fixed;
                bottom: 20px;
                right: 20px;
                z-index: 9998;
                cursor: pointer;
                user-select: none;
            }

            .pet-avatar {
                width: 60px;
                height: 60px;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                border-radius: 50%;
                display: flex;
                align-items: center;
                justify-content: center;
                box-shadow: 0 4px 20px rgba(0,0,0,0.2);
                transition: all 0.3s ease;
                position: relative;
                animation: petFloat 3s ease-in-out infinite;
            }

            .pet-avatar:hover {
                transform: scale(1.1);
                box-shadow: 0 6px 25px rgba(0,0,0,0.3);
            }

            @keyframes petFloat {
                0%, 100% { transform: translateY(0px); }
                50% { transform: translateY(-5px); }
            }

            .pet-face {
                color: white;
                font-size: 24px;
            }

            .pet-eyes {
                display: flex;
                gap: 8px;
                margin-bottom: 4px;
            }

            .eye {
                width: 6px;
                height: 6px;
                background: white;
                border-radius: 50%;
                animation: petBlink 4s infinite;
            }

            @keyframes petBlink {
                0%, 90%, 100% { transform: scaleY(1); }
                95% { transform: scaleY(0.1); }
            }

            .pet-mouth {
                width: 12px;
                height: 6px;
                border: 2px solid white;
                border-top: none;
                border-radius: 0 0 12px 12px;
                margin: 0 auto;
            }

            .pet-notification-dot {
                position: absolute;
                top: -2px;
                right: -2px;
                width: 12px;
                height: 12px;
                background: #ff4757;
                border-radius: 50%;
                border: 2px solid white;
                animation: pulse 2s infinite;
            }

            .pet-speech-bubble {
                position: absolute;
                bottom: 70px;
                right: 0;
                background: white;
                padding: 8px 12px;
                border-radius: 15px;
                box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                white-space: nowrap;
                font-size: 12px;
                color: #333;
                animation: bubbleShow 0.3s ease;
            }

            .pet-speech-bubble::after {
                content: '';
                position: absolute;
                top: 100%;
                right: 15px;
                border: 6px solid transparent;
                border-top-color: white;
            }

            @keyframes bubbleShow {
                from { opacity: 0; transform: translateY(10px); }
                to { opacity: 1; transform: translateY(0); }
            }

            .smart-pet-chat {
                position: fixed;
                bottom: 90px;
                right: 20px;
                width: 320px;
                height: 450px;
                background: white;
                border-radius: 15px;
                box-shadow: 0 10px 40px rgba(0,0,0,0.2);
                z-index: 9999;
                display: flex;
                flex-direction: column;
                animation: chatShow 0.3s ease;
            }

            @keyframes chatShow {
                from { opacity: 0; transform: translateY(20px) scale(0.9); }
                to { opacity: 1; transform: translateY(0) scale(1); }
            }

            .chat-header {
                padding: 15px;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                color: white;
                border-radius: 15px 15px 0 0;
                display: flex;
                justify-content: space-between;
                align-items: center;
            }

            .header-left {
                display: flex;
                align-items: center;
                gap: 10px;
            }

            .pet-avatar-small {
                width: 30px;
                height: 30px;
                background: rgba(255,255,255,0.2);
                border-radius: 50%;
                display: flex;
                align-items: center;
                justify-content: center;
            }

            .pet-face-small {
                color: white;
                font-size: 12px;
            }

            .pet-eyes-small {
                display: flex;
                gap: 3px;
                margin-bottom: 2px;
            }

            .eye-small {
                width: 3px;
                height: 3px;
                background: white;
                border-radius: 50%;
            }

            .pet-mouth-small {
                width: 6px;
                height: 3px;
                border: 1px solid white;
                border-top: none;
                border-radius: 0 0 6px 6px;
                margin: 0 auto;
            }

            .pet-name {
                font-weight: 500;
                font-size: 14px;
            }

            .header-right {
                display: flex;
                gap: 5px;
            }

            .btn-minimize, .btn-close {
                width: 20px;
                height: 20px;
                border: none;
                background: rgba(255,255,255,0.2);
                color: white;
                border-radius: 50%;
                cursor: pointer;
                font-size: 12px;
                display: flex;
                align-items: center;
                justify-content: center;
                transition: background 0.2s;
            }

            .btn-minimize:hover, .btn-close:hover {
                background: rgba(255,255,255,0.3);
            }

            .chat-messages {
                flex: 1;
                padding: 15px;
                overflow-y: auto;
                max-height: 300px;
            }

            .message {
                margin-bottom: 15px;
                display: flex;
                align-items: flex-start;
                gap: 8px;
            }

            .message.user {
                flex-direction: row-reverse;
            }

            .message-content {
                max-width: 70%;
                padding: 8px 12px;
                border-radius: 15px;
                font-size: 13px;
                line-height: 1.4;
            }

            .message.pet .message-content {
                background: #f1f3f4;
                color: #333;
            }

            .message.user .message-content {
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                color: white;
            }

            .chat-input-area {
                padding: 15px;
                border-top: 1px solid #e9ecef;
            }

            .input-group {
                display: flex;
                gap: 8px;
                margin-bottom: 10px;
            }

            #pet-chat-input {
                flex: 1;
                padding: 8px 12px;
                border: 1px solid #ddd;
                border-radius: 20px;
                outline: none;
                font-size: 13px;
            }

            #pet-chat-input:focus {
                border-color: #667eea;
            }

            .send-btn {
                width: 35px;
                height: 35px;
                border: none;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                color: white;
                border-radius: 50%;
                cursor: pointer;
                display: flex;
                align-items: center;
                justify-content: center;
                transition: transform 0.2s;
            }

            .send-btn:hover {
                transform: scale(1.1);
            }

            .quick-actions {
                display: flex;
                gap: 5px;
                flex-wrap: wrap;
            }

            .quick-btn {
                padding: 4px 8px;
                border: 1px solid #ddd;
                background: white;
                color: #666;
                border-radius: 12px;
                cursor: pointer;
                font-size: 11px;
                transition: all 0.2s;
            }

            .quick-btn:hover {
                background: #f8f9fa;
                border-color: #667eea;
                color: #667eea;
            }

            @media (max-width: 768px) {
                .smart-pet-chat {
                    width: calc(100vw - 40px);
                    right: 20px;
                    left: 20px;
                }
            }
        `;
        
        document.head.appendChild(style);
    }

    /**
     * 绑定智能助手事件
     */
    bindPetEvents() {
        const trigger = document.getElementById('smart-pet-trigger');
        const chatWindow = document.getElementById('smart-pet-chat');
        const minimizeBtn = chatWindow.querySelector('.btn-minimize');
        const closeBtn = chatWindow.querySelector('.btn-close');
        const sendBtn = document.getElementById('pet-send-btn');
        const input = document.getElementById('pet-chat-input');
        const quickBtns = chatWindow.querySelectorAll('.quick-btn');

        // 点击触发器显示/隐藏聊天窗口
        trigger.addEventListener('click', () => {
            this.toggleChat();
        });

        // 最小化按钮
        minimizeBtn.addEventListener('click', () => {
            this.hideChat();
        });

        // 关闭按钮
        closeBtn.addEventListener('click', () => {
            this.hideChat();
        });

        // 发送消息
        sendBtn.addEventListener('click', () => {
            this.sendMessage();
        });

        // 回车发送
        input.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                this.sendMessage();
            }
        });

        // 快捷按钮
        quickBtns.forEach(btn => {
            btn.addEventListener('click', () => {
                const action = btn.dataset.action;
                this.handleQuickAction(action);
            });
        });
    }

    /**
     * 切换聊天窗口显示状态
     */
    toggleChat() {
        if (this.isVisible) {
            this.hideChat();
        } else {
            this.showChat();
        }
    }

    /**
     * 显示聊天窗口
     */
    showChat() {
        const chatWindow = document.getElementById('smart-pet-chat');
        const speechBubble = document.querySelector('.pet-speech-bubble');
        
        chatWindow.style.display = 'flex';
        speechBubble.style.display = 'none';
        this.isVisible = true;
        
        // 聚焦输入框
        setTimeout(() => {
            document.getElementById('pet-chat-input').focus();
        }, 100);
        
        this.saveState();
    }

    /**
     * 隐藏聊天窗口
     */
    hideChat() {
        const chatWindow = document.getElementById('smart-pet-chat');
        chatWindow.style.display = 'none';
        this.isVisible = false;
        this.saveState();
    }

    /**
     * 发送消息
     */
    async sendMessage() {
        const input = document.getElementById('pet-chat-input');
        const message = input.value.trim();
        
        if (!message) return;
        
        // 添加用户消息到界面
        this.addMessage('user', message);
        input.value = '';
        
        // 保存到历史记录
        this.addToHistory('user', message);
        
        // 显示正在输入状态
        this.showTyping();
        
        try {
            // 发送到后端
            const response = await fetch('/api/pet/chat', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    message: message,
                    userId: this.getCurrentUserId(),
                    context: this.getRecentContext()
                })
            });
            
            const data = await response.json();
            
            // 移除正在输入状态
            this.hideTyping();
            
            // 添加AI回复
            this.addMessage('pet', data.response || '抱歉，我现在有点忙，稍后再聊吧~');
            this.addToHistory('pet', data.response || '抱歉，我现在有点忙，稍后再聊吧~');
            
        } catch (error) {
            console.error('发送消息失败:', error);
            this.hideTyping();
            this.addMessage('pet', '抱歉，网络似乎有点问题，请稍后再试~');
        }
    }

    /**
     * 添加消息到界面
     */
    addMessage(type, content) {
        const messagesContainer = document.getElementById('pet-chat-messages');
        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${type}`;
        
        messageDiv.innerHTML = `
            <div class="message-content">${this.escapeHtml(content)}</div>
        `;
        
        messagesContainer.appendChild(messageDiv);
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }

    /**
     * 显示正在输入状态
     */
    showTyping() {
        const messagesContainer = document.getElementById('pet-chat-messages');
        const typingDiv = document.createElement('div');
        typingDiv.className = 'message pet typing-indicator';
        typingDiv.innerHTML = `
            <div class="message-content">
                <span class="typing-dots">
                    <span>.</span><span>.</span><span>.</span>
                </span>
            </div>
        `;
        
        // 添加打字动画样式
        if (!document.getElementById('typing-animation-style')) {
            const style = document.createElement('style');
            style.id = 'typing-animation-style';
            style.textContent = `
                .typing-dots span {
                    animation: typingDots 1.4s infinite;
                }
                .typing-dots span:nth-child(2) {
                    animation-delay: 0.2s;
                }
                .typing-dots span:nth-child(3) {
                    animation-delay: 0.4s;
                }
                @keyframes typingDots {
                    0%, 60%, 100% { opacity: 0.3; }
                    30% { opacity: 1; }
                }
            `;
            document.head.appendChild(style);
        }
        
        messagesContainer.appendChild(typingDiv);
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }

    /**
     * 隐藏正在输入状态
     */
    hideTyping() {
        const typingIndicator = document.querySelector('.typing-indicator');
        if (typingIndicator) {
            typingIndicator.remove();
        }
    }

    /**
     * 处理快捷操作
     */
    handleQuickAction(action) {
        switch (action) {
            case 'recommend':
                this.addMessage('user', '推荐一些视频给我');
                this.addToHistory('user', '推荐一些视频给我');
                this.sendQuickMessage('推荐一些视频给我');
                break;
            case 'help':
                this.addMessage('user', '使用帮助');
                this.addToHistory('user', '使用帮助');
                this.sendQuickMessage('使用帮助');
                break;
            case 'clear':
                this.clearChat();
                break;
        }
    }

    /**
     * 发送快捷消息
     */
    async sendQuickMessage(message) {
        this.showTyping();
        
        try {
            const response = await fetch('/api/pet/chat', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    message: message,
                    userId: this.getCurrentUserId(),
                    context: this.getRecentContext()
                })
            });
            
            const data = await response.json();
            this.hideTyping();
            this.addMessage('pet', data.response || '好的，我来帮你~');
            this.addToHistory('pet', data.response || '好的，我来帮你~');
            
        } catch (error) {
            console.error('发送快捷消息失败:', error);
            this.hideTyping();
            this.addMessage('pet', '抱歉，出了点小问题~');
        }
    }

    /**
     * 清空聊天记录
     */
    clearChat() {
        if (confirm('确定要清空所有聊天记录吗？')) {
            const messagesContainer = document.getElementById('pet-chat-messages');
            messagesContainer.innerHTML = '';
            this.chatHistory = [];
            this.saveChatHistory();
            this.addMessage('pet', '聊天记录已清空，我们重新开始吧！');
        }
    }

    /**
     * 保存状态到本地存储
     */
    saveState() {
        const state = {
            isVisible: this.isVisible,
            currentPage: this.currentPage,
            timestamp: Date.now()
        };
        localStorage.setItem(this.storageKey, JSON.stringify(state));
    }

    /**
     * 从本地存储加载状态
     */
    loadState() {
        try {
            const saved = localStorage.getItem(this.storageKey);
            return saved ? JSON.parse(saved) : { isVisible: false };
        } catch (error) {
            console.error('加载状态失败:', error);
            return { isVisible: false };
        }
    }

    /**
     * 恢复状态
     */
    restoreState() {
        // 恢复聊天历史
        this.restoreChatHistory();
        
        // 如果之前是显示状态，则显示聊天窗口
        if (this.currentState.isVisible) {
            setTimeout(() => {
                this.showChat();
            }, 500);
        }
    }

    /**
     * 保存聊天历史
     */
    saveChatHistory() {
        try {
            localStorage.setItem(this.chatHistoryKey, JSON.stringify(this.chatHistory));
        } catch (error) {
            console.error('保存聊天历史失败:', error);
        }
    }

    /**
     * 加载聊天历史
     */
    loadChatHistory() {
        try {
            const saved = localStorage.getItem(this.chatHistoryKey);
            return saved ? JSON.parse(saved) : [];
        } catch (error) {
            console.error('加载聊天历史失败:', error);
            return [];
        }
    }

    /**
     * 恢复聊天历史到界面
     */
    restoreChatHistory() {
        const messagesContainer = document.getElementById('pet-chat-messages');
        messagesContainer.innerHTML = '';
        
        // 只显示最近的20条消息
        const recentHistory = this.chatHistory.slice(-20);
        
        recentHistory.forEach(item => {
            this.addMessage(item.type, item.content);
        });
        
        // 如果没有历史记录，显示欢迎消息
        if (this.chatHistory.length === 0) {
            this.addMessage('pet', '你好！我是小智，你的智能助手。有什么可以帮助你的吗？');
        }
    }

    /**
     * 添加到历史记录
     */
    addToHistory(type, content) {
        this.chatHistory.push({
            type: type,
            content: content,
            timestamp: Date.now(),
            page: this.currentPage
        });
        
        // 限制历史记录长度
        if (this.chatHistory.length > this.maxHistoryLength) {
            this.chatHistory = this.chatHistory.slice(-this.maxHistoryLength);
        }
        
        this.saveChatHistory();
    }

    /**
     * 获取最近的对话上下文
     */
    getRecentContext() {
        return this.chatHistory.slice(-6).map(item => ({
            role: item.type === 'user' ? 'user' : 'assistant',
            content: item.content
        }));
    }

    /**
     * 获取当前用户ID
     */
    getCurrentUserId() {
        const userDataElement = document.querySelector('[data-current-user]');
        const username = userDataElement ? userDataElement.getAttribute('data-username') : null;
        return username || 'anonymous';
    }

    /**
     * HTML转义
     */
    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    /**
     * 监听页面变化
     */
    observePageChanges() {
        // 监听路由变化（SPA应用）
        window.addEventListener('popstate', () => {
            this.currentPage = window.location.pathname;
            this.saveState();
        });
        
        // 监听页面卸载
        window.addEventListener('beforeunload', () => {
            this.saveState();
        });
    }

    /**
     * 绑定全局事件
     */
    bindEvents() {
        // 监听来自其他页面的消息
        window.addEventListener('storage', (e) => {
            if (e.key === this.chatHistoryKey) {
                this.chatHistory = this.loadChatHistory();
                this.restoreChatHistory();
            }
        });
    }

    /**
     * 显示通知红点
     */
    showNotification() {
        const dot = document.querySelector('.pet-notification-dot');
        if (dot) {
            dot.style.display = 'block';
        }
    }

    /**
     * 隐藏通知红点
     */
    hideNotification() {
        const dot = document.querySelector('.pet-notification-dot');
        if (dot) {
            dot.style.display = 'none';
        }
    }

    /**
     * 销毁管理器
     */
    destroy() {
        const container = document.getElementById('global-smart-pet');
        if (container) {
            container.remove();
        }
        
        const styles = document.getElementById('global-smart-pet-styles');
        if (styles) {
            styles.remove();
        }
    }
}

// 全局实例
let globalSmartPetManager = null;

// 自动初始化
document.addEventListener('DOMContentLoaded', () => {
    // 只在用户登录时初始化
    const userDataElement = document.querySelector('[data-current-user]');
    const username = userDataElement ? userDataElement.getAttribute('data-username') : null;
    
    if (username && username.trim() !== '') {
        globalSmartPetManager = new GlobalSmartPetManager();
        
        // 暴露到全局作用域
        window.globalSmartPetManager = globalSmartPetManager;
    }
});

// 页面卸载时清理
window.addEventListener('beforeunload', () => {
    if (globalSmartPetManager) {
        globalSmartPetManager.saveState();
    }
});
