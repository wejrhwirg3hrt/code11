/**
 * 聊天系统JavaScript
 */

// 聊天应用主对象
const ChatApp = {
    currentUser: null,
    currentConversationId: null,
    targetUser: null,
    conversations: [],
    messages: [],
    unreadCount: 0,
    stompClient: null,
    connected: false,
    currentSubscription: null,
    onlineCount: 0,
    userOnlineStatus: new Map(), // 存储用户在线状态
    
    // 初始化
    init() {
        this.currentUser = window.currentUser;
        console.log('ChatApp初始化 - 当前用户:', this.currentUser);

        if (!this.currentUser || !this.currentUser.id) {
            console.error('当前用户信息无效:', this.currentUser);
        }

        // 初始化分页参数
        this.currentPage = 0;
        this.hasMoreMessages = true;
        this.isLoadingMessages = false;

        this.bindEvents();
        this.loadConversations();
        this.loadEmojis();
        this.updateUnreadCount();
        this.connectWebSocket();
        this.initAvatarSync(); // 初始化头像同步
        this.initScrollListener(); // 初始化滚动监听

        // 定期更新未读消息数量和在线状态
        setInterval(() => {
            this.updateUnreadCount();
            this.updateAllUsersOnlineStatus(); // 定期更新在线状态
        }, 30000); // 30秒更新一次

        // 定期发送心跳保持在线状态
        this.heartbeatInterval = setInterval(() => {
            this.sendHeartbeat();
        }, 60000); // 60秒发送一次心跳

        // 页面卸载时清理资源
        window.addEventListener('beforeunload', () => {
            this.disconnectWebSocket();
            if (this.heartbeatInterval) {
                clearInterval(this.heartbeatInterval);
            }
        });

        console.log('聊天应用初始化完成');
        console.log('当前用户信息:', this.currentUser);
        console.log('在线状态缓存:', this.userOnlineStatus);
    },
    
    // 绑定事件
    bindEvents() {
        // 搜索会话
        const searchInput = document.getElementById('searchInput');
        if (searchInput) {
            searchInput.addEventListener('input', (e) => {
                this.searchConversations(e.target.value);
            });
        }
        
        // 用户搜索
        const userSearchInput = document.getElementById('userSearchInput');
        if (userSearchInput) {
            userSearchInput.addEventListener('input', (e) => {
                this.searchUsers(e.target.value);
            });
        }
        
        // 文件输入
        const fileInput = document.getElementById('fileInput');
        if (fileInput) {
            fileInput.addEventListener('change', (e) => {
                this.handleFileSelect(e.target.files);
            });
        }

        // 发送按钮
        const sendBtn = document.getElementById('sendBtn');
        if (sendBtn) {
            sendBtn.addEventListener('click', () => {
                this.sendMessage();
            });
        }

        // 文件按钮
        const fileBtn = document.getElementById('fileBtn');
        if (fileBtn) {
            fileBtn.addEventListener('click', () => {
                fileInput.click();
            });
        }

        // 消息输入框
        const messageInput = document.getElementById('messageInput');
        if (messageInput) {
            messageInput.addEventListener('keypress', (e) => {
                if (e.key === 'Enter' && !e.shiftKey) {
                    e.preventDefault();
                    this.sendMessage();
                }
            });

            messageInput.addEventListener('input', () => {
                this.autoResize(messageInput);
            });
        }
        
        // 点击空白处关闭表情选择器
        document.addEventListener('click', (e) => {
            const emojiPicker = document.getElementById('emojiPicker');
            const emojiButton = e.target.closest('[onclick="toggleEmojiPicker()"]');
            if (emojiPicker && !emojiPicker.contains(e.target) && !emojiButton) {
                emojiPicker.style.display = 'none';
            }
        });
    },
    
    // 加载会话列表
    async loadConversations() {
        try {
            const response = await fetch('/api/chat/conversations?page=0&size=20');
            if (response.ok) {
                const data = await response.json();
                if (data.success) {
                    this.conversations = data.conversations || [];
                    console.log('加载的会话数据:', this.conversations);
                    // 打印每个会话的详细信息
                    this.conversations.forEach((conv, index) => {
                        console.log(`会话 ${index}:`, {
                            id: conv.id,
                            type: conv.type,
                            title: conv.title,
                            otherUser: conv.otherUser,
                            participants: conv.participants
                        });
                    });
                    // 先加载所有用户的在线状态，然后渲染会话列表
                    await this.updateAllUsersOnlineStatus();
                    this.renderConversations();
                } else {
                    console.error('加载会话列表失败:', data.error);
                }
            } else {
                console.error('加载会话列表失败:', response.status);
            }
        } catch (error) {
            console.error('加载会话列表失败:', error);
        }
    },
    
    // 渲染会话列表
    renderConversations() {
        try {
            const conversationList = document.getElementById('conversationList');
            if (!conversationList) {
                console.warn('找不到会话列表容器');
                return;
            }

            if (this.conversations.length === 0) {
                conversationList.innerHTML = `
                    <div class="text-center text-muted py-4">
                        <i class="fas fa-comments fa-3x mb-3"></i>
                        <p>暂无会话</p>
                        <p class="small">点击右上角的 + 号开始新的聊天</p>
                    </div>
                `;
                return;
            }

            conversationList.innerHTML = this.conversations.map(conv => {
                try {
                    // 获取显示名称
                    let displayName = conv.title || conv.name || '未知会话';
                    let avatarHtml = '';

                    if (conv.otherUser) {
                        displayName = conv.otherUser.nickname || conv.otherUser.username;
                        // 创建带在线状态的头像
                        avatarHtml = this.createAvatarElementWithOnlineStatus(conv.otherUser, 'conversation-avatar');
                    } else {
                        // 群聊或其他类型的会话，使用默认图标
                        avatarHtml = `<div class="conversation-avatar">
                                        <i class="fas fa-users"></i>
                                      </div>`;
                    }

                    return `
                        <div class="conversation-item ${conv.id === this.currentConversationId ? 'active' : ''}"
                             data-conversation-id="${conv.id}"
                             onclick="selectConversationWrapper(${conv.id})"
                             oncontextmenu="showConversationContextMenu(event, ${conv.id}, '${displayName.replace(/'/g, "\\'")}')">
                            ${avatarHtml}
                            <div class="conversation-info">
                                <div class="conversation-name">${displayName}</div>
                                <div class="conversation-last-message">${this.formatLastMessage(conv)}</div>
                            </div>
                            <div class="conversation-meta">
                                <div class="conversation-time">${this.formatTime(conv.updatedAt || conv.lastMessageTime)}</div>
                                ${conv.unreadCount > 0 ? `<div class="conversation-unread">${conv.unreadCount}</div>` : ''}
                            </div>
                        </div>
                    `;
                } catch (error) {
                    console.error('渲染单个会话失败:', error, conv);
                    return '';
                }
            }).join('');
        } catch (error) {
            console.error('渲染会话列表失败:', error);
        }
    },

    // 格式化最后一条消息显示
    formatLastMessage(conversation) {
        if (conversation.lastMessage) {
            // 如果有发送者信息，显示发送者
            if (conversation.lastMessageSender) {
                const senderName = conversation.lastMessageSender;
                const isCurrentUser = this.currentUser && senderName === (this.currentUser.nickname || this.currentUser.username);
                const prefix = isCurrentUser ? '我: ' : `${senderName}: `;
                return prefix + conversation.lastMessage;
            }
            return conversation.lastMessage;
        }
        return '暂无消息';
    },
    
    // 选择会话
    async selectConversation(conversationId) {
        console.log('切换到会话:', conversationId, '当前会话:', this.currentConversationId);

        // 强制转换为数字进行比较，避免字符串和数字比较问题
        const newConversationId = parseInt(conversationId);
        const currentConversationId = parseInt(this.currentConversationId);

        // 如果是同一个会话，不需要重新加载
        if (currentConversationId === newConversationId && this.currentConversationId) {
            console.log('已经是当前会话，无需切换');
            return;
        }

        try {
            // 显示加载状态
            this.showConversationLoading();

            // 清除之前的状态
            this.clearCurrentConversationState();

            // 设置新的会话ID
            this.currentConversationId = newConversationId;

            // 显示聊天界面
            this.showChatInterface();

            // 重新渲染会话列表以更新选中状态
            this.renderConversations();

            // 加载新会话（等待完成）
            await this.loadConversation(newConversationId);

            // 订阅当前会话的WebSocket消息
            this.subscribeToConversation(newConversationId);

            // 立即更新当前会话用户的在线状态
            this.updateCurrentUserOnlineStatus();

            // 显示通话按钮
            this.showCallActions();

            // 隐藏加载状态
            this.hideConversationLoading();

            console.log('会话切换完成:', newConversationId);
        } catch (error) {
            console.error('切换会话失败:', error);
            this.hideConversationLoading();
            this.showError('切换会话失败，请重试');
        }
    },

    // 清除当前会话状态
    clearCurrentConversationState() {
        // 清空消息列表
        const messagesList = document.getElementById('messagesList');
        if (messagesList) {
            messagesList.innerHTML = '';
        }

        // 重置分页参数
        this.currentPage = 0;
        this.hasMoreMessages = true;
        this.isLoadingMessages = false;

        // 清除目标用户信息
        this.targetUser = null;

        // 清除WebSocket订阅
        if (this.currentSubscription) {
            console.log('清除WebSocket订阅');
            this.currentSubscription.unsubscribe();
            this.currentSubscription = null;
        }

        // 清除回复状态
        if (typeof cancelReply === 'function') {
            cancelReply();
        }

        // 清除@功能状态
        if (typeof closeMentionDropdown === 'function') {
            closeMentionDropdown();
        }

        // 隐藏加载指示器
        this.hideLoadingIndicator();

        console.log('已清除当前会话状态');
    },

    // 显示聊天界面
    showChatInterface() {
        const chatWelcome = document.getElementById('chatWelcome');
        const chatInterface = document.getElementById('chatInterface');

        if (chatWelcome) chatWelcome.style.display = 'none';
        if (chatInterface) chatInterface.style.display = 'flex';
    },

    // 隐藏聊天界面
    hideChatInterface() {
        const chatWelcome = document.getElementById('chatWelcome');
        const chatInterface = document.getElementById('chatInterface');

        if (chatWelcome) chatWelcome.style.display = 'block';
        if (chatInterface) chatInterface.style.display = 'none';

        // 隐藏通话按钮
        this.hideCallActions();
    },

    // 显示通话按钮
    showCallActions() {
        const chatActions = document.getElementById('chatActions');
        if (chatActions) {
            chatActions.style.display = 'block';
        }
    },

    // 隐藏通话按钮
    hideCallActions() {
        const chatActions = document.getElementById('chatActions');
        if (chatActions) {
            chatActions.style.display = 'none';
        }
    },
    
    // 加载会话详情
    async loadConversation(conversationId) {
        if (!conversationId) {
            console.error('loadConversation: conversationId为空');
            return;
        }

        console.log('开始加载会话详情:', conversationId);

        // 确保会话ID是数字
        this.currentConversationId = parseInt(conversationId);

        try {
            // 设置目标用户信息（用于WebRTC通话）
            this.setTargetUserFromConversation(this.currentConversationId);

            // 更新聊天界面头部信息
            this.updateChatHeader();

            // 加载消息
            await this.loadConversationMessages();

            console.log('会话详情加载完成:', this.currentConversationId);
        } catch (error) {
            console.error('加载会话详情失败:', error);
            this.showError('加载会话失败，请重试');
        }
    },

    // 从会话中设置目标用户信息
    setTargetUserFromConversation(conversationId) {
        const conversation = this.conversations.find(c => c.id === conversationId);
        if (conversation && conversation.participants) {
            // 找到不是当前用户的参与者
            const otherParticipant = conversation.participants.find(
                p => p.user.id !== this.currentUser.id
            );

            if (otherParticipant) {
                this.targetUser = otherParticipant.user;
                console.log('设置目标用户:', this.targetUser);
            }
        }
    },

    // 获取用户头像URL（统一处理默认头像）
    getUserAvatarUrl(user) {
        if (!user) return null;

        // 如果用户有头像且不是空字符串
        if (user.avatar && user.avatar.trim() !== '') {
            // 检查是否是有效的头像路径
            if (user.avatar !== '/uploads/avatars/default.png' &&
                user.avatar !== '/uploads/avatars/default.svg' &&
                user.avatar !== '/images/default-avatar.png') {
                return user.avatar;
            }
        }

        // 返回null表示使用默认样式
        return null;
    },

    // 显示用户操作菜单（关注/私信）
    showUserActionMenu(user) {
        // 移除已存在的菜单
        const existingMenu = document.getElementById('user-action-menu');
        if (existingMenu) {
            existingMenu.remove();
        }

        // 创建菜单元素
        const menu = document.createElement('div');
        menu.id = 'user-action-menu';
        menu.className = 'user-action-menu';
        menu.style.cssText = `
            position: fixed;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            background: white;
            border-radius: 12px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.3);
            padding: 20px;
            z-index: 9999;
            min-width: 200px;
            text-align: center;
        `;

        // 添加用户信息
        const userInfo = document.createElement('div');
        userInfo.className = 'mb-3';
        userInfo.innerHTML = `
            <img src="${user.avatar || '/uploads/avatars/default.svg'}" 
                 alt="${user.username}" 
                 style="width: 60px; height: 60px; border-radius: 50%; margin-bottom: 10px;">
            <h6 class="mb-0">${user.username}</h6>
        `;
        menu.appendChild(userInfo);

        // 添加操作按钮
        const actionsDiv = document.createElement('div');
        actionsDiv.className = 'd-flex flex-column gap-2';

        // 关注按钮
        const followBtn = document.createElement('button');
        followBtn.className = 'btn btn-primary btn-sm';
        followBtn.innerHTML = '<i class="fas fa-user-plus me-2"></i>关注';
        followBtn.onclick = () => this.toggleFollow(user.id, followBtn);
        actionsDiv.appendChild(followBtn);

        // 私信按钮
        const messageBtn = document.createElement('button');
        messageBtn.className = 'btn btn-outline-primary btn-sm';
        messageBtn.innerHTML = '<i class="fas fa-comment me-2"></i>私信';
        messageBtn.onclick = () => {
            this.openPrivateChat(user.id);
            this.closeUserActionMenu();
        };
        actionsDiv.appendChild(messageBtn);

        // 关闭按钮
        const closeBtn = document.createElement('button');
        closeBtn.className = 'btn btn-outline-secondary btn-sm mt-2';
        closeBtn.innerHTML = '关闭';
        closeBtn.onclick = () => this.closeUserActionMenu();
        actionsDiv.appendChild(closeBtn);

        menu.appendChild(actionsDiv);

        // 添加遮罩层
        const overlay = document.createElement('div');
        overlay.id = 'user-action-overlay';
        overlay.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0,0,0,0.5);
            z-index: 9998;
        `;
        overlay.onclick = () => this.closeUserActionMenu();

        document.body.appendChild(overlay);
        document.body.appendChild(menu);

        // 检查关注状态
        this.checkFollowStatus(user.id, followBtn);
    },

    // 关闭用户操作菜单
    closeUserActionMenu() {
        const menu = document.getElementById('user-action-menu');
        const overlay = document.getElementById('user-action-overlay');
        if (menu) menu.remove();
        if (overlay) overlay.remove();
    },

    // 检查关注状态
    async checkFollowStatus(userId, followBtn) {
        try {
            const response = await fetch(`/api/follow/status/${userId}`, {
                method: 'GET',
                credentials: 'include'
            });
            
            if (response.ok) {
                const data = await response.json();
                if (data.following) {
                    followBtn.innerHTML = '<i class="fas fa-user-check me-2"></i>已关注';
                    followBtn.className = 'btn btn-secondary btn-sm';
                }
            }
        } catch (error) {
            console.error('检查关注状态失败:', error);
        }
    },

    // 切换关注状态
    async toggleFollow(userId, followBtn) {
        try {
            const response = await fetch(`/api/follow/toggle/${userId}`, {
                method: 'POST',
                credentials: 'include'
            });
            
            if (response.ok) {
                const data = await response.json();
                if (data.following) {
                    followBtn.innerHTML = '<i class="fas fa-user-check me-2"></i>已关注';
                    followBtn.className = 'btn btn-secondary btn-sm';
                } else {
                    followBtn.innerHTML = '<i class="fas fa-user-plus me-2"></i>关注';
                    followBtn.className = 'btn btn-primary btn-sm';
                }
            }
        } catch (error) {
            console.error('关注操作失败:', error);
        }
    },

    // 打开私信聊天
    openPrivateChat(userId) {
        // 跳转到私信页面
        window.open(`/chat?userId=${userId}`, '_blank');
    },

    // 创建头像元素（支持图片和默认样式，包含在线状态）
    createAvatarElement(user, className = 'conversation-avatar', showOnlineStatus = true) {
        const avatarUrl = this.getUserAvatarUrl(user);
        const isOnline = this.isUserOnline(user.id);
        const onlineStatusClass = isOnline ? 'online' : 'offline';
        const onlineStatusIcon = isOnline ? 'fas fa-circle text-success' : 'fas fa-circle text-muted';

        // 根据用户ID决定点击行为：如果是当前用户跳转到个人资料页，否则跳转到用户主页
        let clickHandler = '';
        if (this.currentUser && user.id === this.currentUser.id) {
            // 当前用户点击头像跳转到个人资料页
            clickHandler = `onclick="window.open('/profile', '_blank')" style="cursor: pointer;"`;
        } else {
            // 其他用户点击头像跳转到用户主页
            clickHandler = `onclick="window.open('/user/${user.id}', '_blank')" style="cursor: pointer;"`;
        }

        if (avatarUrl) {
            // 有自定义头像，使用img标签
            return `<div class="avatar-container position-relative" ${clickHandler}>
                        <img src="${avatarUrl}" alt="头像" class="${className}"
                             onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';">
                        <div class="${className}" style="display: none;">
                            <i class="fas fa-user"></i>
                        </div>
                        ${showOnlineStatus ? `<div class="online-status-indicator ${onlineStatusClass}">
                            <i class="${onlineStatusIcon}" style="font-size: 8px;"></i>
                        </div>` : ''}
                    </div>`;
        } else {
            // 使用默认样式显示用户名首字母
            const displayName = user.nickname || user.username || '?';
            const initial = displayName.charAt(0).toUpperCase();
            return `<div class="avatar-container position-relative" ${clickHandler}>
                        <div class="${className}">
                            ${initial}
                        </div>
                        ${showOnlineStatus ? `<div class="online-status-indicator ${onlineStatusClass}">
                            <i class="${onlineStatusIcon}" style="font-size: 8px;"></i>
                        </div>` : ''}
                    </div>`;
        }
    },

    // 创建带在线状态的头像元素（同步版本，用于会话列表）
    createAvatarElementWithOnlineStatus(user, className = 'conversation-avatar') {
        const avatarUrl = this.getUserAvatarUrl(user);
        const isOnline = this.isUserOnline(user.id);
        const onlineStatusClass = isOnline ? 'online' : 'offline';

        // 根据用户ID决定点击行为：如果是当前用户跳转到个人资料页，否则跳转到用户主页
        let clickHandler = '';
        if (this.currentUser && user.id === this.currentUser.id) {
            // 当前用户点击头像跳转到个人资料页
            clickHandler = `onclick="window.open('/profile', '_blank')" style="cursor: pointer;"`;
        } else {
            // 其他用户点击头像跳转到用户主页
            clickHandler = `onclick="window.open('/user/${user.id}', '_blank')" style="cursor: pointer;"`;
        }

        if (avatarUrl) {
            // 有自定义头像，使用img标签
            return `<div class="avatar-container position-relative" ${clickHandler}>
                        <img src="${avatarUrl}" alt="头像" class="${className}"
                             onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';">
                        <div class="${className}" style="display: none;">
                            <i class="fas fa-user"></i>
                        </div>
                        <div class="online-status-indicator ${onlineStatusClass}">
                            <div class="status-dot"></div>
                        </div>
                    </div>`;
        } else {
            // 使用默认样式显示用户名首字母
            const displayName = user.nickname || user.username || '?';
            const initial = displayName.charAt(0).toUpperCase();
            return `<div class="avatar-container position-relative" ${clickHandler}>
                        <div class="${className}">
                            ${initial}
                        </div>
                        <div class="online-status-indicator ${onlineStatusClass}">
                            <div class="status-dot"></div>
                        </div>
                    </div>`;
        }
    },

    // 检查用户是否在线（从缓存中获取，避免频繁API调用）
    isUserOnline(userId) {
        // 从缓存中获取在线状态，如果没有则默认为离线
        return this.userOnlineStatus.get(userId) || false;
    },

    // 异步更新用户在线状态
    async updateUserOnlineStatus(userId) {
        try {
            const response = await fetch(`/api/online-users/status/${userId}`, {
                method: 'GET',
                credentials: 'include'
            });

            if (response.ok) {
                const data = await response.json();
                const isOnline = data.success && data.isOnline;
                this.userOnlineStatus.set(userId, isOnline);
                return isOnline;
            }
        } catch (error) {
            console.error('检查用户在线状态失败:', error);
        }
        return false;
    },

    // 批量更新在线状态
    async updateAllUsersOnlineStatus() {
        const userIds = [];
        this.conversations.forEach(conv => {
            if (conv.otherUser && conv.otherUser.id) {
                userIds.push(conv.otherUser.id);
            }
        });

        if (userIds.length === 0) return;

        try {
            const response = await fetch('/api/online-users/status/batch', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ userIds }),
                credentials: 'include'
            });

            if (response.ok) {
                const data = await response.json();
                console.log('批量在线状态API响应:', data);
                if (data.success && data.userStatuses) {
                    Object.entries(data.userStatuses).forEach(([userId, isOnline]) => {
                        this.userOnlineStatus.set(parseInt(userId), isOnline);
                        console.log(`缓存用户在线状态 - 用户ID: ${userId}, 在线状态: ${isOnline}`);
                    });
                    // 只有在不是初始加载时才重新渲染
                    if (this.conversations.length > 0) {
                        this.renderConversations();
                        this.updateChatHeader();
                    }
                }
            }
        } catch (error) {
            console.error('批量更新用户在线状态失败:', error);
        }
    },

    // 更新当前会话用户的在线状态
    async updateCurrentUserOnlineStatus() {
        if (!this.currentConversationId) return;

        const conversation = this.conversations.find(conv => conv.id === this.currentConversationId);
        if (!conversation || !conversation.otherUser) return;

        try {
            const isOnline = await this.updateUserOnlineStatus(conversation.otherUser.id);
            console.log(`更新用户 ${conversation.otherUser.id} 的在线状态: ${isOnline ? '在线' : '离线'}`);

            // 更新聊天头部显示
            this.updateChatHeader();
            // 重新渲染会话列表
            this.renderConversations();
        } catch (error) {
            console.error('更新当前用户在线状态失败:', error);
        }
    },

    // 更新聊天界面头部
    updateChatHeader() {
        try {
            const conversation = this.conversations.find(conv => conv.id === this.currentConversationId);
            if (!conversation) {
                console.warn('找不到当前会话信息，无法更新头部');
                return;
            }

            const chatTitle = document.getElementById('chatTitle');
            const chatStatus = document.getElementById('chatStatus');
            const chatAvatarContainer = document.getElementById('chatAvatar')?.parentElement;

            if (conversation.otherUser) {
                if (chatTitle) chatTitle.textContent = conversation.otherUser.nickname || conversation.otherUser.username;
                if (chatAvatarContainer) {
                    const avatarHtml = this.createAvatarElementWithOnlineStatus(conversation.otherUser, 'rounded-circle');
                    chatAvatarContainer.innerHTML = avatarHtml;
                }

                // 更新在线状态
                if (chatStatus) {
                    const isOnline = this.userOnlineStatus.get(conversation.otherUser.id) || false;
                    console.log(`更新聊天头部在线状态 - 用户ID: ${conversation.otherUser.id}, 在线状态: ${isOnline}`);
                    chatStatus.textContent = isOnline ? '在线' : '离线';
                    chatStatus.className = isOnline ? 'text-success' : 'text-muted';

                    // 异步检查最新的在线状态
                    this.updateUserOnlineStatus(conversation.otherUser.id).then(isOnline => {
                        if (chatStatus) {
                            console.log(`异步更新聊天头部在线状态 - 用户ID: ${conversation.otherUser.id}, 在线状态: ${isOnline}`);
                            chatStatus.textContent = isOnline ? '在线' : '离线';
                            chatStatus.className = isOnline ? 'text-success' : 'text-muted';
                            // 更新缓存
                            this.userOnlineStatus.set(conversation.otherUser.id, isOnline);
                        }
                    });
                }
            } else {
                if (chatTitle) chatTitle.textContent = conversation.title || conversation.name || '未知会话';
                if (chatStatus) {
                    chatStatus.textContent = '群聊';
                    chatStatus.className = 'text-muted';
                }
                if (chatAvatarContainer) {
                    chatAvatarContainer.innerHTML = `<div class="rounded-circle" style="width: 32px; height: 32px; background: var(--primary-gradient); display: flex; align-items: center; justify-content: center; color: white;">
                        <i class="fas fa-users"></i>
                    </div>`;
                }
            }
        } catch (error) {
            console.error('更新聊天头部失败:', error);
        }
    },

    // 同步头像更新
    syncAvatarUpdate(userId, newAvatarUrl) {
        // 更新当前用户信息
        if (this.currentUser && this.currentUser.id === userId) {
            this.currentUser.avatar = newAvatarUrl;

            // 更新导航栏头像
            const navAvatar = document.querySelector('.navbar .dropdown-toggle img');
            if (navAvatar) {
                if (newAvatarUrl && newAvatarUrl !== '/images/default-avatar.png') {
                    navAvatar.src = newAvatarUrl;
                    navAvatar.style.display = 'block';
                } else {
                    // 使用默认样式
                    const container = navAvatar.parentElement;
                    const username = this.currentUser.username || '?';
                    const initial = username.charAt(0).toUpperCase();
                    container.innerHTML = `<div style="width: 24px; height: 24px; border-radius: 50%; background: var(--primary-gradient); display: flex; align-items: center; justify-content: center; color: white; font-size: 0.8rem; margin-right: 0.25rem;">
                        ${initial}
                    </div>
                    <span>${username}</span>`;
                }
            }
        }

        // 更新会话列表中的头像
        this.conversations.forEach(conv => {
            if (conv.otherUser && conv.otherUser.id === userId) {
                conv.otherUser.avatar = newAvatarUrl;
            }
        });

        // 重新渲染会话列表
        this.renderConversations();

        // 更新聊天头部头像
        this.updateChatHeader();
    },

    // 监听头像更新事件
    initAvatarSync() {
        // 监听来自其他窗口的头像更新消息
        window.addEventListener('storage', (e) => {
            if (e.key === 'avatarUpdate') {
                const data = JSON.parse(e.newValue);
                this.syncAvatarUpdate(data.userId, data.avatarUrl);
            }
        });

        // 监听自定义头像更新事件
        window.addEventListener('avatarUpdated', (e) => {
            this.syncAvatarUpdate(e.detail.userId, e.detail.avatarUrl);
        });
    },

    // 触发头像更新事件
    triggerAvatarUpdate(userId, avatarUrl) {
        // 存储到localStorage以便其他窗口监听
        localStorage.setItem('avatarUpdate', JSON.stringify({
            userId: userId,
            avatarUrl: avatarUrl,
            timestamp: Date.now()
        }));

        // 触发自定义事件
        window.dispatchEvent(new CustomEvent('avatarUpdated', {
            detail: { userId: userId, avatarUrl: avatarUrl }
        }));
    },

    // 初始化私聊
    async initPrivateChat(targetUser) {
        this.targetUser = targetUser;

        // 检查关注状态
        await this.checkFollowStatus();

        // 尝试获取或创建私聊会话
        await this.getOrCreatePrivateConversation();
    },
    
    // 检查关注状态
    async checkFollowStatus() {
        if (!this.targetUser || !this.targetUser.id) {
            console.error('目标用户信息不完整');
            return;
        }

        try {
            // 调用API检查关注状态
            const response = await fetch(`/api/follow/status/${this.targetUser.id}`, {
                method: 'GET',
                credentials: 'include'
            });

            if (!response.ok) {
                throw new Error('获取关注状态失败');
            }

            const data = await response.json();

            // 根据API返回的数据确定关注状态
            let followStatus;
            if (data.isMutual) {
                followStatus = 'mutual-friends';
            } else if (data.isFollowing) {
                followStatus = 'following';
            } else {
                followStatus = 'not-following';
            }

            console.log('关注状态检查结果:', data, '最终状态:', followStatus);

            this.updateFollowStatusUI(followStatus);
        } catch (error) {
            console.error('检查关注状态失败:', error);
            // 出错时默认为未关注状态
            this.updateFollowStatusUI('not-following');
        }
    },

    // 更新关注状态UI
    updateFollowStatusUI(followStatus) {
        
        const alertArea = document.getElementById('followStatusAlert');
        if (alertArea) {
            let alertContent = '';
            let alertClass = '';
            
            switch (followStatus) {
                case 'mutual-friends':
                    // 互相关注，隐藏提示，直接开始聊天
                    alertArea.style.display = 'none';
                    this.enableMessageInput();
                    break;
                case 'following':
                    // 已关注，隐藏提示，允许聊天
                    alertArea.style.display = 'none';
                    this.enableMessageInput();
                    break;
                case 'not-following':
                    alertClass = 'alert alert-danger not-following';
                    alertContent = `<i class="fas fa-user-times me-2"></i>您还未关注 ${this.targetUser.username}，无法发送消息`;
                    alertArea.style.display = 'block';
                    this.disableMessageInput();
                    break;
            }

            // 只有在未关注状态下才设置alert内容
            if (followStatus === 'not-following') {
                alertArea.className = alertClass;
                alertArea.innerHTML = alertContent;
            }
        }
    },
    
    // 显示发送限制提示
    showSendLimitNotice() {
        const notice = document.getElementById('sendLimitNotice');
        if (notice) {
            notice.style.display = 'block';
        }
    },
    
    // 禁用消息输入
    disableMessageInput() {
        const messageInput = document.getElementById('messageInput');
        const sendButton = document.getElementById('sendButton');

        if (messageInput) {
            messageInput.disabled = true;
            messageInput.placeholder = '请先关注对方才能发送消息';
        }

        if (sendButton) {
            sendButton.disabled = true;
        }
    },

    // 启用消息输入
    enableMessageInput() {
        const messageInput = document.getElementById('messageInput');
        const sendButton = document.getElementById('sendButton');

        if (messageInput) {
            messageInput.disabled = false;
            messageInput.placeholder = '输入消息...';
        }

        if (sendButton) {
            sendButton.disabled = false;
        }
    },
    
    // 获取或创建私聊会话
    async getOrCreatePrivateConversation() {
        if (!this.targetUser || !this.targetUser.id) {
            console.error('目标用户信息不完整');
            return;
        }

        try {
            // 调用API获取或创建会话
            const response = await fetch('/api/chat/conversations/private', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: new URLSearchParams({
                    targetUserId: this.targetUser.id
                }),
                credentials: 'include'
            });

            if (!response.ok) {
                throw new Error('获取会话失败');
            }

            const data = await response.json();

            if (data.success && data.conversation) {
                this.currentConversationId = data.conversation.id;
                console.log('私聊会话ID:', this.currentConversationId);

                // 加载会话消息
                await this.loadConversationMessages();
            } else {
                throw new Error(data.error || '获取会话失败');
            }

        } catch (error) {
            console.error('获取私聊会话失败:', error);
            this.showError('获取会话失败，请重试');
        }
    },

    // 加载会话消息
    async loadConversationMessages(loadMore = false) {
        if (!this.currentConversationId) {
            console.error('没有当前会话ID');
            return;
        }

        console.log('开始加载会话消息，会话ID:', this.currentConversationId, '加载更多:', loadMore);

        try {
            // 初始化分页参数
            if (!loadMore) {
                this.currentPage = 0;
                this.hasMoreMessages = true;
                this.isLoadingMessages = false;
            }

            if (this.isLoadingMessages) {
                console.log('正在加载消息，跳过重复请求');
                return;
            }

            this.isLoadingMessages = true;

            const response = await fetch(`/api/chat/conversations/${this.currentConversationId}/messages?page=${this.currentPage}&size=20`, {
                method: 'GET',
                credentials: 'include'
            });

            if (!response.ok) {
                throw new Error(`加载消息失败: ${response.status}`);
            }

            const data = await response.json();
            console.log('收到消息数据:', data);

            if (data.success && data.messages) {
                const messagesList = document.getElementById('messagesList');
                if (!messagesList) {
                    console.error('找不到消息列表容器');
                    return;
                }

                // 如果是首次加载，清空现有消息
                if (!loadMore) {
                    console.log('首次加载，清空现有消息，准备加载', data.messages.length, '条新消息');
                    messagesList.innerHTML = '';
                    messagesList.offsetHeight; // 触发重排
                }

                // 消息是按时间倒序返回的，需要反转以正确显示
                const messages = data.messages.reverse();
                console.log('开始渲染消息到UI，消息数量:', messages.length);

                if (messages.length === 0) {
                    console.log('没有消息需要渲染');
                    this.hasMoreMessages = false;
                } else {
                    // 记录当前滚动位置（用于加载更多时保持位置）
                    const messagesList = document.getElementById('messagesList');
                    const oldScrollHeight = messagesList ? messagesList.scrollHeight : 0;
                    const oldScrollTop = messagesList ? messagesList.scrollTop : 0;

                    if (loadMore) {
                        // 加载更多时，在顶部插入消息
                        messages.reverse().forEach((message) => {
                            this.prependMessageToUI(message);
                        });

                        // 恢复滚动位置
                        if (messagesList) {
                            const newScrollHeight = messagesList.scrollHeight;
                            messagesList.scrollTop = oldScrollTop + (newScrollHeight - oldScrollHeight);
                        }
                    } else {
                        // 首次加载时，正常添加消息
                        messages.forEach((message, index) => {
                            console.log(`渲染第${index + 1}条消息:`, message);
                            this.addMessageToUI(message);
                        });

                        // 强制滚动到最新消息（进入聊天时）
                        this.forceScrollToLatestMessage();
                    }

                    // 更新分页信息
                    this.currentPage++;
                    this.hasMoreMessages = this.currentPage < data.totalPages;
                }

                console.log('消息加载完成，当前会话ID:', this.currentConversationId, '当前页:', this.currentPage, '还有更多:', this.hasMoreMessages);
            } else {
                console.error('消息数据格式错误:', data);
                throw new Error(data.error || '加载消息失败');
            }

        } catch (error) {
            console.error('加载会话消息失败:', error);
            this.showError('加载消息失败，请重试');
        } finally {
            this.isLoadingMessages = false;
        }
    },

    // 发送消息
    async sendMessage() {
        const messageInput = document.getElementById('messageInput');
        const content = messageInput.value.trim();

        if (!content && !this.hasFilePreview()) {
            return;
        }

        try {
            // 发送文本消息（包含回复信息）
            if (content) {
                let apiUrl = '/api/chat/messages/text';
                let requestBody = new URLSearchParams({
                    conversationId: this.currentConversationId || 0,
                    content: content
                });

                // 如果是回复消息，使用回复API
                if (typeof currentReplyTo !== 'undefined' && currentReplyTo) {
                    apiUrl = '/api/chat/messages/reply';
                    requestBody.append('replyToMessageId', currentReplyTo);

                    // 检查是否有@用户
                    const mentionedUsers = extractMentionedUsers(content);
                    if (mentionedUsers.length > 0) {
                        requestBody.append('mentionedUsers', JSON.stringify(mentionedUsers));
                    }
                }

                const response = await fetch(apiUrl, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                    },
                    body: requestBody,
                    credentials: 'include'
                });

                if (response.ok) {
                    const data = await response.json();
                    if (data.success && data.message) {
                        messageInput.value = '';
                        this.autoResize(messageInput);

                        // 清除回复状态
                        if (typeof cancelReply === 'function') {
                            cancelReply();
                        }

                        // 确保消息包含正确的发送者信息
                        const messageWithSender = {
                            ...data.message,
                            senderId: this.currentUser.id,
                            senderName: this.currentUser.username
                        };

                        console.log('发送消息后添加到UI:', messageWithSender);
                        this.addMessageToUI(messageWithSender);
                        this.scrollToLatestMessage(true); // 强制滚动
                        // 更新会话列表
                        this.loadConversations();
                    } else {
                        throw new Error(data.error || '发送消息失败');
                    }
                } else {
                    const errorData = await response.json();
                    throw new Error(errorData.error || '发送消息失败');
                }
            }
            
            // 发送文件
            if (this.hasFilePreview()) {
                await this.sendFiles();
            }
            
        } catch (error) {
            console.error('发送消息失败:', error);
            alert('发送消息失败，请重试');
        }
    },
    


    
    // 处理消息输入
    handleMessageInput(event) {
        if (event.key === 'Enter' && !event.shiftKey) {
            event.preventDefault();
            this.sendMessage();
        }
    },
    
    // 自动调整文本框高度
    autoResize(textarea) {
        textarea.style.height = 'auto';
        textarea.style.height = Math.min(textarea.scrollHeight, 120) + 'px';
    },
    
    // 选择文件
    selectFile(type) {
        const fileInput = document.getElementById('fileInput');
        if (!fileInput) return;

        // 设置多选属性
        fileInput.multiple = true;

        switch (type) {
            case 'image':
                fileInput.accept = 'image/*';
                break;
            case 'video':
                fileInput.accept = 'video/*';
                break;
            case 'file':
                fileInput.accept = '*';
                break;
        }

        fileInput.click();
    },
    
    // 处理文件选择
    handleFileSelect(files) {
        if (!files || files.length === 0) return;

        // 检查文件数量限制
        if (files.length > 9) {
            alert('最多只能同时选择9个文件');
            return;
        }

        // 检查文件大小
        for (let file of files) {
            if (file.size > 5 * 1024 * 1024 * 1024) { // 5GB
                alert(`文件 "${file.name}" 超过5GB大小限制`);
                return;
            }
        }

        this.showFilePreview(files);
    },
    
    // 显示文件预览
    showFilePreview(files) {
        const previewArea = document.getElementById('filePreviewArea');
        const previewList = document.getElementById('filePreviewList');

        if (!previewArea || !previewList) return;

        previewList.innerHTML = '';
        this.selectedFiles = Array.from(files);

        this.selectedFiles.forEach((file, index) => {
            const previewItem = document.createElement('div');
            previewItem.className = 'file-preview-item';
            previewItem.dataset.index = index;

            // 创建预览内容
            if (this.isImageFile(file.type)) {
                // 图片预览
                const reader = new FileReader();
                reader.onload = (e) => {
                    previewItem.innerHTML = `
                        <div class="file-preview-image">
                            <img src="${e.target.result}" alt="${file.name}" style="max-width: 100px; max-height: 100px; object-fit: cover; border-radius: 4px;">
                        </div>
                        <div class="file-preview-info">
                            <div class="file-preview-name">${file.name}</div>
                            <div class="file-preview-size">${this.formatFileSize(file.size)}</div>
                        </div>
                        <button class="btn btn-sm btn-outline-danger" onclick="ChatApp.removeFilePreview(${index})">
                            <i class="fas fa-times"></i>
                        </button>
                    `;
                };
                reader.readAsDataURL(file);
            } else if (this.isVideoFile(file.type)) {
                // 视频预览
                const videoUrl = URL.createObjectURL(file);
                previewItem.innerHTML = `
                    <div class="file-preview-video">
                        <video src="${videoUrl}" style="max-width: 100px; max-height: 100px; object-fit: cover; border-radius: 4px;" muted></video>
                        <div class="video-overlay"><i class="fas fa-play"></i></div>
                    </div>
                    <div class="file-preview-info">
                        <div class="file-preview-name">${file.name}</div>
                        <div class="file-preview-size">${this.formatFileSize(file.size)}</div>
                    </div>
                    <button class="btn btn-sm btn-outline-danger" onclick="ChatApp.removeFilePreview(${index})">
                        <i class="fas fa-times"></i>
                    </button>
                `;
            } else {
                // 其他文件类型
                previewItem.innerHTML = `
                    <div class="file-preview-icon">${this.getFileIcon(file.name)}</div>
                    <div class="file-preview-info">
                        <div class="file-preview-name">${file.name}</div>
                        <div class="file-preview-size">${this.formatFileSize(file.size)}</div>
                    </div>
                    <button class="btn btn-sm btn-outline-danger" onclick="ChatApp.removeFilePreview(${index})">
                        <i class="fas fa-times"></i>
                    </button>
                `;
            }

            previewList.appendChild(previewItem);
        });

        previewArea.style.display = 'block';
    },
    
    // 移除文件预览
    removeFilePreview(index) {
        if (!this.selectedFiles || index < 0 || index >= this.selectedFiles.length) {
            return;
        }

        // 移除指定索引的文件
        this.selectedFiles.splice(index, 1);

        // 如果没有文件了，清空预览区域
        if (this.selectedFiles.length === 0) {
            this.clearFilePreview();
        } else {
            // 重新显示预览
            this.showFilePreview(this.selectedFiles);
        }
    },
    
    // 清空文件预览
    clearFilePreview() {
        const previewArea = document.getElementById('filePreviewArea');
        if (previewArea) {
            previewArea.style.display = 'none';
        }
        this.selectedFiles = null;
    },
    
    // 检查是否有文件预览
    hasFilePreview() {
        return this.selectedFiles && this.selectedFiles.length > 0;
    },
    
    // 发送文件
    async sendFiles() {
        if (!this.selectedFiles || this.selectedFiles.length === 0) return;

        const formData = new FormData();
        formData.append('conversationId', this.currentConversationId || 0);

        // 添加所有文件到FormData
        this.selectedFiles.forEach(file => {
            formData.append('files', file);
        });

        try {
            console.log('开始发送文件，会话ID:', this.currentConversationId);
            console.log('文件列表:', this.selectedFiles);

            const response = await fetch('/api/chat/messages/file', {
                method: 'POST',
                body: formData,
                credentials: 'include'
            });

            console.log('文件上传响应状态:', response.status);

            // 检查响应内容类型
            const contentType = response.headers.get('content-type');
            console.log('响应内容类型:', contentType);

            if (contentType && contentType.includes('application/json')) {
                const data = await response.json();
                console.log('文件上传响应数据:', data);

                if (response.ok && data.success) {
                    // 为每个文件创建消息UI
                    if (data.files) {
                        data.files.forEach((fileInfo, index) => {
                            const file = this.selectedFiles[index];
                            const messageWithSender = {
                                id: fileInfo.id || Date.now() + index,
                                fileName: fileInfo.fileName || file.name,
                                fileSize: fileInfo.fileSize || file.size,
                                fileUrl: fileInfo.fileUrl,
                                thumbnailUrl: fileInfo.thumbnailUrl,
                                sender: this.currentUser,
                                senderId: this.currentUser.id,
                                senderName: this.currentUser.username,
                                timestamp: new Date(),
                                type: fileInfo.messageType || this.getFileType(file.name),
                                messageType: fileInfo.messageType || this.getFileType(file.name)
                            };

                            console.log('发送文件后添加到UI:', messageWithSender);
                            this.addMessageToUI(messageWithSender);
                        });

                        // 滚动到最新消息
                        this.scrollToLatestMessage(true); // 强制滚动

                        // 更新会话列表
                        this.loadConversations();
                    }
                } else {
                    const errorMsg = data.error || '文件发送失败';
                    console.error('文件发送失败:', errorMsg);
                    alert('文件发送失败: ' + errorMsg);
                }
            } else {
                // 响应不是JSON，可能是HTML错误页面
                const text = await response.text();
                console.error('服务器返回非JSON响应:', text.substring(0, 200));
                alert('服务器错误，请检查应用程序是否正常运行');
            }
        } catch (error) {
            console.error('发送文件失败:', error);
            alert('文件发送失败，请重试: ' + error.message);
        }

        this.clearFilePreview();
    },
    
    // 切换表情选择器
    toggleEmojiPicker() {
        const emojiPicker = document.getElementById('emojiPicker');
        if (emojiPicker) {
            emojiPicker.style.display = emojiPicker.style.display === 'none' ? 'block' : 'none';
        }
    },
    
    // 加载表情
    loadEmojis() {
        // 模拟表情数据
        const emojis = {
            faces: ['😊', '😂', '😍', '😘', '😎', '🤔', '😄', '😉'],
            gestures: ['👍', '👎', '👏', '👌', '✌️', '✊', '🤝', '🙏'],
            hearts: ['❤️', '💛', '💚', '💙', '💜', '🧡', '🖤', '🤍'],
            animals: ['🐱', '🐶', '🐼', '🐰', '🐯', '🦁', '🐸', '🐵']
        };
        
        this.renderEmojis(emojis);
    },
    
    // 渲染表情
    renderEmojis(emojis) {
        const emojiGrid = document.getElementById('emojiGrid');
        if (!emojiGrid) return;
        
        // 默认显示笑脸类表情
        this.showEmojiCategory('faces', emojis);
        
        // 绑定分类切换事件
        document.querySelectorAll('.emoji-category').forEach(btn => {
            btn.addEventListener('click', (e) => {
                document.querySelectorAll('.emoji-category').forEach(b => b.classList.remove('active'));
                e.target.classList.add('active');
                this.showEmojiCategory(e.target.dataset.category, emojis);
            });
        });
    },
    
    // 显示表情分类
    showEmojiCategory(category, emojis) {
        const emojiGrid = document.getElementById('emojiGrid');
        if (!emojiGrid || !emojis[category]) return;
        
        emojiGrid.innerHTML = emojis[category].map(emoji => `
            <button class="emoji-item" onclick="ChatApp.insertEmoji('${emoji}')">${emoji}</button>
        `).join('');
    },
    
    // 插入表情
    insertEmoji(emoji) {
        const messageInput = document.getElementById('messageInput');
        if (messageInput) {
            const cursorPos = messageInput.selectionStart;
            const textBefore = messageInput.value.substring(0, cursorPos);
            const textAfter = messageInput.value.substring(messageInput.selectionEnd);
            
            messageInput.value = textBefore + emoji + textAfter;
            messageInput.focus();
            messageInput.setSelectionRange(cursorPos + emoji.length, cursorPos + emoji.length);
        }
        
        this.toggleEmojiPicker();
    },
    
    // 搜索会话
    searchConversations(keyword) {
        // 实现会话搜索逻辑
        console.log('搜索会话:', keyword);
    },
    
    // 搜索用户
    async searchUsers(keyword) {
        try {
            // 如果关键词为空，获取所有用户
            const url = keyword && keyword.trim()
                ? `/api/chat/users/search?keyword=${encodeURIComponent(keyword.trim())}`
                : '/api/chat/users/search?keyword=';

            const response = await fetch(url);
            if (response.ok) {
                const data = await response.json();
                this.renderUserSearchResults(data.users || []);
            } else {
                const errorData = await response.json();
                console.error('搜索用户失败:', errorData.error);
                this.renderUserSearchResults([]);
            }
        } catch (error) {
            console.error('搜索用户请求失败:', error);
            this.renderUserSearchResults([]);
        }
    },
    
    // 渲染用户搜索结果
    renderUserSearchResults(users) {
        const resultsContainer = document.getElementById('userSearchResults');
        if (!resultsContainer) return;

        if (users.length === 0) {
            resultsContainer.innerHTML = '<div class="text-center text-muted py-3">未找到用户</div>';
            return;
        }

        resultsContainer.innerHTML = users.map(user => `
            <div class="user-search-item" onclick="ChatApp.startChatWithUser(${user.id})">
                <img src="${user.avatar}" alt="头像" class="user-search-avatar">
                <div class="user-search-info">
                    <div class="user-search-name">${user.nickname || user.username}</div>
                    <div class="user-search-username">@${user.username}</div>
                </div>
            </div>
        `).join('');
    },
    
    // 开始与用户聊天
    startChatWithUser(userId) {
        window.location.href = `/chat/private?userId=${userId}`;
    },
    
    // 更新未读消息数量
    async updateUnreadCount() {
        try {
            const response = await fetch('/api/chat/unread-count');
            if (response.ok) {
                const data = await response.json();
                this.unreadCount = data.count || 0;
                this.updateUnreadBadge();
            }
        } catch (error) {
            console.error('更新未读消息数量失败:', error);
        }
    },
    
    // 更新未读消息徽章
    updateUnreadBadge() {
        const badge = document.getElementById('totalUnreadCount');
        if (badge) {
            if (this.unreadCount > 0) {
                badge.textContent = this.unreadCount > 99 ? '99+' : this.unreadCount;
                badge.style.display = 'inline';
            } else {
                badge.style.display = 'none';
            }
        }
    },
    
    // 工具方法
    formatTime(timestamp) {
        if (!timestamp) return '';
        
        const date = new Date(timestamp);
        const now = new Date();
        const diff = now - date;
        
        if (diff < 60000) { // 1分钟内
            return '刚刚';
        } else if (diff < 3600000) { // 1小时内
            return Math.floor(diff / 60000) + '分钟前';
        } else if (diff < 86400000) { // 24小时内
            return Math.floor(diff / 3600000) + '小时前';
        } else {
            return date.toLocaleDateString();
        }
    },
    
    formatFileSize(bytes) {
        if (bytes === 0) return '0 B';
        const k = 1024;
        const sizes = ['B', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    },
    
    getFileIcon(fileName) {
        const ext = fileName.split('.').pop().toLowerCase();
        const iconMap = {
            'jpg': '🖼️', 'jpeg': '🖼️', 'png': '🖼️', 'gif': '🖼️',
            'mp4': '🎥', 'avi': '🎥', 'mov': '🎥',
            'mp3': '🎵', 'wav': '🎵', 'flac': '🎵',
            'pdf': '📄', 'doc': '📝', 'docx': '📝',
            'xls': '📊', 'xlsx': '📊',
            'zip': '🗜️', 'rar': '🗜️', '7z': '🗜️'
        };
        return iconMap[ext] || '📎';
    },
    
    getFileType(fileName) {
        const ext = fileName.split('.').pop().toLowerCase();
        if (['jpg', 'jpeg', 'png', 'gif', 'webp'].includes(ext)) return 'image';
        if (['mp4', 'avi', 'mov', 'wmv'].includes(ext)) return 'video';
        if (['mp3', 'wav', 'flac', 'aac'].includes(ext)) return 'voice';
        return 'file';
    },
    
    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    },

    // 检查是否是图片文件
    isImageFile(type) {
        return type && type.startsWith('image/');
    },

    // 检查是否是视频文件
    isVideoFile(type) {
        return type && type.startsWith('video/');
    },

    // 显示错误消息
    showError(message) {
        // 可以使用toast或alert显示错误
        console.error(message);
        alert(message); // 简单的错误显示，可以后续改进
    },

    // 显示会话加载状态
    showConversationLoading() {
        const messagesList = document.getElementById('messagesList');
        if (messagesList) {
            messagesList.innerHTML = `
                <div class="text-center py-4">
                    <div class="spinner-border text-primary" role="status">
                        <span class="visually-hidden">加载中...</span>
                    </div>
                    <p class="mt-2 text-muted">正在加载会话...</p>
                </div>
            `;
        }
    },

    // 隐藏会话加载状态
    hideConversationLoading() {
        // 加载状态会在消息加载完成后自动被替换
    },

    // 滚动到底部（平滑滚动）
    scrollToBottom(smooth = false) {
        const messagesList = document.getElementById('messagesList');
        if (messagesList) {
            if (smooth) {
                messagesList.scrollTo({
                    top: messagesList.scrollHeight,
                    behavior: 'smooth'
                });
            } else {
                messagesList.scrollTop = messagesList.scrollHeight;
            }
        }
    },

    // 滚动到最新消息（带动画效果）
    scrollToLatestMessage(force = false) {
        const messagesList = document.getElementById('messagesList');
        if (!messagesList) return;

        // 检查是否需要滚动（如果用户正在查看历史消息，不要强制滚动）
        const isNearBottom = messagesList.scrollHeight - messagesList.scrollTop - messagesList.clientHeight < 100;

        if (force || isNearBottom) {
            // 使用多重requestAnimationFrame确保DOM完全更新
            requestAnimationFrame(() => {
                requestAnimationFrame(() => {
                    messagesList.scrollTo({
                        top: messagesList.scrollHeight,
                        behavior: 'smooth'
                    });
                    console.log('滚动到最新消息，scrollHeight:', messagesList.scrollHeight);

                    // 隐藏滚动到底部按钮
                    this.hideScrollToBottomButton();
                });
            });
        }
    },

    // 强制滚动到最新消息（用于进入聊天时）
    forceScrollToLatestMessage() {
        const messagesList = document.getElementById('messagesList');
        if (!messagesList) return;

        // 延迟执行，确保消息已经渲染完成
        setTimeout(() => {
            requestAnimationFrame(() => {
                messagesList.scrollTo({
                    top: messagesList.scrollHeight,
                    behavior: 'smooth'
                });
                console.log('强制滚动到最新消息，scrollHeight:', messagesList.scrollHeight);

                // 隐藏滚动到底部按钮
                this.hideScrollToBottomButton();
            });
        }, 100);
    },

    // 显示滚动到底部按钮
    showScrollToBottomButton() {
        const scrollBtn = document.getElementById('scrollToBottomBtn');
        if (scrollBtn) {
            scrollBtn.style.display = 'block';
        }
    },

    // 隐藏滚动到底部按钮
    hideScrollToBottomButton() {
        const scrollBtn = document.getElementById('scrollToBottomBtn');
        if (scrollBtn) {
            scrollBtn.style.display = 'none';
        }
    },

    // 检查是否需要显示滚动到底部按钮
    checkScrollToBottomButton() {
        const messagesList = document.getElementById('messagesList');
        if (!messagesList) return;

        const isNearBottom = messagesList.scrollHeight - messagesList.scrollTop - messagesList.clientHeight < 100;

        if (isNearBottom) {
            this.hideScrollToBottomButton();
        } else {
            this.showScrollToBottomButton();
        }
    },

    // 修复addMessageToUI方法以正确处理数据库返回的消息格式
    addMessageToUI(message) {
        const messagesList = document.getElementById('messagesList');
        if (!messagesList) return;

        // 如果是空状态，先清空
        if (messagesList.querySelector('.text-center')) {
            messagesList.innerHTML = '';
        }

        const messageElement = document.createElement('div');
        messageElement.className = 'message-item';

        // 判断是否是当前用户发送的消息
        // 确保类型匹配 - 将两个值都转换为字符串进行比较
        const currentUserId = this.currentUser ? String(this.currentUser.id) : null;
        const messageSenderId = message.senderId ? String(message.senderId) : null;
        const currentUsername = this.currentUser ? this.currentUser.username : null;
        const messageSenderName = message.senderName || message.sender?.username;

        console.log('消息发送者判断:', {
            currentUserId,
            messageSenderId,
            currentUsername,
            messageSenderName,
            message: message
        });

        const isCurrentUser = this.currentUser && (
            (currentUserId && messageSenderId && currentUserId === messageSenderId) ||
            (currentUsername && messageSenderName && currentUsername === messageSenderName)
        );

        console.log('是否为当前用户消息:', isCurrentUser);

        if (isCurrentUser) {
            messageElement.classList.add('sent');
        } else {
            messageElement.classList.add('received');
        }

        let messageContent = '';

        // 调试信息
        const messageType = message.messageType || message.type;
        if (messageType === 'image' || messageType === 'IMAGE') {
            console.log('渲染图片消息:', {
                fileUrl: message.fileUrl,
                fileName: message.fileName,
                messageType: messageType,
                fullMessage: message
            });
        }

        // 根据消息类型渲染不同的内容
        switch (messageType) {
            case 'text':
                let replyHtml = '';
                // 如果是回复消息，显示被回复的消息
                if (message.replyTo) {
                    replyHtml = `
                        <div class="reply-message" onclick="scrollToMessage('${message.replyTo.id}')">
                            <div class="reply-author">${this.escapeHtml(message.replyTo.senderName)}</div>
                            <div class="reply-content">${this.escapeHtml(message.replyTo.content || '文件消息')}</div>
                        </div>
                    `;
                }

                messageContent = `
                    <div class="message-content">
                        ${replyHtml}
                        <div class="message-text">${this.processMessageContent(message.content || '')}</div>
                        <div class="message-time">${this.formatTime(message.timestamp)}</div>
                    </div>
                `;
                break;
            case 'image':
            case 'IMAGE':
                // 统一图片URL处理逻辑
                let imageUrl = '';
                console.log('渲染图片消息:', {
                    fileUrl: message.fileUrl,
                    fileName: message.fileName,
                    messageType: messageType
                });

                if (message.fileUrl) {
                    // 强制使用API路径，避免直接访问uploads目录
                    if (message.fileUrl.startsWith('/api/chat/')) {
                        // 已经是API路径，直接使用
                        imageUrl = message.fileUrl;
                    } else if (message.fileUrl.startsWith('/uploads/')) {
                        // 如果是uploads路径，提取文件名并转换为API路径
                        const fileName = message.fileUrl.split('/').pop();
                        imageUrl = `/api/chat/file/${fileName}`;
                        console.log('转换uploads路径为API路径:', message.fileUrl, '->', imageUrl);
                    } else if (message.fileUrl.startsWith('http')) {
                        // 外部URL，直接使用
                        imageUrl = message.fileUrl;
                    } else {
                        // 其他情况，尝试提取文件名
                        const fileName = message.fileUrl.split('/').pop();
                        imageUrl = `/api/chat/file/${fileName}`;
                        console.log('提取文件名构造API路径:', fileName);
                    }
                    console.log('使用服务器返回的fileUrl:', imageUrl);
                } else if (message.fileName) {
                    // 如果没有fileUrl但有fileName，尝试通过文件API访问
                    imageUrl = `/api/chat/file/${message.fileName}`;
                    console.log('通过fileName构造URL:', imageUrl);
                } else {
                    // 如果都没有，使用默认图片
                    console.error('服务器未返回fileUrl和fileName，消息数据:', message);
                    imageUrl = '/images/default-thumbnail.jpg';
                }

                messageContent = `
                    <div class="message-content">
                        <div class="message-image">
                            <img src="${imageUrl}" alt="${message.fileName || '图片'}"
                                 onclick="this.requestFullscreen()"
                                 style="max-width: 200px; max-height: 200px; cursor: pointer; border-radius: 8px;"
                                 onload="console.log('图片加载成功:', '${imageUrl}'); this.nextElementSibling.style.display='none';"
                                 onerror="console.log('图片加载失败:', '${imageUrl}'); this.style.display='none'; this.nextElementSibling.style.display='block';">
                            <div style="display: none; padding: 20px; background: #f8f9fa; border-radius: 8px; text-align: center; max-width: 200px;">
                                <i class="fas fa-image" style="font-size: 24px; color: #6c757d;"></i><br>
                                <small class="text-muted">图片不存在</small><br>
                                <small class="text-muted">${message.fileName || '未知文件'}</small>
                            </div>
                        </div>
                        ${message.content ? `<div class="message-text">${this.escapeHtml(message.content)}</div>` : ''}
                        <div class="message-time">${this.formatTime(message.timestamp)}</div>
                    </div>
                `;
                break;
            case 'video':
            case 'VIDEO':
                let videoUrl = message.fileUrl || '';
                console.log('渲染视频消息:', {
                    fileUrl: message.fileUrl,
                    fileName: message.fileName,
                    messageType: messageType
                });

                // 智能视频URL处理逻辑 - 优先使用服务器返回的fileUrl
                if (videoUrl) {
                    // 如果已经是完整URL，直接使用
                    if (!videoUrl.startsWith('http') && !videoUrl.startsWith('/')) {
                        videoUrl = '/' + videoUrl;
                    }
                    console.log('使用服务器返回的视频fileUrl:', videoUrl);
                } else {
                    // 如果没有fileUrl，这是异常情况，记录错误
                    console.error('服务器未返回视频fileUrl，消息数据:', message);
                    videoUrl = ''; // 空URL，让视频元素显示错误
                }

                messageContent = `
                    <div class="message-content">
                        <div class="message-video">
                            <video controls style="max-width: 300px; max-height: 200px; border-radius: 8px;"
                                   onerror="this.style.display='none'; this.nextElementSibling.style.display='block';">
                                <source src="${videoUrl}" type="video/mp4">
                                <source src="${videoUrl}" type="video/webm">
                                <source src="${videoUrl}" type="video/ogg">
                                您的浏览器不支持视频播放。
                            </video>
                            <div style="display: none; padding: 20px; background: #f8f9fa; border-radius: 8px; text-align: center;">
                                <i class="fas fa-video" style="font-size: 24px; color: #6c757d;"></i><br>
                                <small class="text-muted">视频加载失败</small><br>
                                <small class="text-muted">${message.fileName || '未知视频文件'}</small><br>
                                <small class="text-muted">URL: ${videoUrl}</small>
                            </div>
                            <div class="video-info">
                                <small class="text-muted">${message.fileName || '视频文件'}</small>
                            </div>
                        </div>
                        ${message.content ? `<div class="message-text">${this.escapeHtml(message.content)}</div>` : ''}
                        <div class="message-time">${this.formatTime(message.timestamp)}</div>
                    </div>
                `;
                break;
            default:
                messageContent = `
                    <div class="message-content">
                        <div class="message-text">${this.escapeHtml(message.content || '')}</div>
                        <div class="message-time">${this.formatTime(message.timestamp)}</div>
                    </div>
                `;
        }

        // 添加消息ID和右键菜单支持
        messageElement.setAttribute('data-message-id', message.id);
        messageElement.innerHTML = messageContent;

        // 添加消息操作功能
        this.addMessageActions(messageElement, message, isCurrentUser);

        // 添加右键菜单
        this.addContextMenu(messageElement, message, isCurrentUser);

        messagesList.appendChild(messageElement);

        // 滚动到最新消息（不使用平滑滚动，避免频繁动画）
        this.scrollToBottom();
    },

    // 在消息列表顶部插入消息（用于加载历史消息）
    prependMessageToUI(message) {
        const messagesList = document.getElementById('messagesList');
        if (!messagesList) return;

        // 如果是空状态，先清空
        if (messagesList.querySelector('.text-center')) {
            messagesList.innerHTML = '';
        }

        const messageElement = document.createElement('div');
        messageElement.className = 'message-item';

        // 判断是否是当前用户发送的消息
        const currentUserId = this.currentUser ? String(this.currentUser.id) : null;
        const messageSenderId = message.senderId ? String(message.senderId) : null;
        const currentUsername = this.currentUser ? this.currentUser.username : null;
        const messageSenderName = message.senderName || message.sender?.username;

        const isCurrentUser = this.currentUser && (
            (currentUserId && messageSenderId && currentUserId === messageSenderId) ||
            (currentUsername && messageSenderName && currentUsername === messageSenderName)
        );

        if (isCurrentUser) {
            messageElement.classList.add('sent');
        } else {
            messageElement.classList.add('received');
        }

        let messageContent = '';

        // 根据消息类型渲染不同的内容（复用addMessageToUI的逻辑）
        const messageType = message.messageType || message.type;
        switch (messageType) {
            case 'text':
                let replyHtml = '';
                if (message.replyTo) {
                    replyHtml = `
                        <div class="reply-message" onclick="scrollToMessage('${message.replyTo.id}')">
                            <div class="reply-author">${this.escapeHtml(message.replyTo.senderName)}</div>
                            <div class="reply-content">${this.escapeHtml(message.replyTo.content || '文件消息')}</div>
                        </div>
                    `;
                }

                messageContent = `
                    <div class="message-content">
                        ${replyHtml}
                        <div class="message-text">${this.processMessageContent(message.content || '')}</div>
                        <div class="message-time">${this.formatTime(message.timestamp)}</div>
                    </div>
                `;
                break;
            case 'image':
            case 'IMAGE':
                let imageUrl = '';
                if (message.fileUrl) {
                    // 强制使用API路径，避免直接访问uploads目录
                    if (message.fileUrl.startsWith('/api/chat/')) {
                        // 已经是API路径，直接使用
                        imageUrl = message.fileUrl;
                    } else if (message.fileUrl.startsWith('/uploads/')) {
                        // 如果是uploads路径，提取文件名并转换为API路径
                        const fileName = message.fileUrl.split('/').pop();
                        imageUrl = `/api/chat/file/${fileName}`;
                    } else if (message.fileUrl.startsWith('http')) {
                        // 外部URL，直接使用
                        imageUrl = message.fileUrl;
                    } else {
                        // 其他情况，尝试提取文件名
                        const fileName = message.fileUrl.split('/').pop();
                        imageUrl = `/api/chat/file/${fileName}`;
                    }
                } else if (message.fileName) {
                    // 如果没有fileUrl但有fileName，尝试通过文件API访问
                    imageUrl = `/api/chat/file/${message.fileName}`;
                } else {
                    // 如果都没有，使用默认图片
                    imageUrl = '/images/default-thumbnail.jpg';
                }

                messageContent = `
                    <div class="message-content">
                        <div class="image-message">
                            <img src="${imageUrl}" alt="${message.fileName || '图片'}"
                                 onclick="this.requestFullscreen()"
                                 style="max-width: 200px; max-height: 200px; cursor: pointer; border-radius: 8px;"
                                 onload="console.log('图片加载成功:', '${imageUrl}'); this.nextElementSibling.style.display='none';"
                                 onerror="console.log('图片加载失败:', '${imageUrl}'); this.style.display='none'; this.nextElementSibling.style.display='block';">
                            <div style="display: none; padding: 20px; background: #f8f9fa; border-radius: 8px; text-align: center; max-width: 200px;">
                                <i class="fas fa-image" style="font-size: 24px; color: #6c757d;"></i><br>
                                <small class="text-muted">图片不存在</small><br>
                                <small class="text-muted">${message.fileName || '未知文件'}</small>
                            </div>
                        </div>
                        <div class="message-time">${this.formatTime(message.timestamp)}</div>
                    </div>
                `;
                break;
            default:
                messageContent = `
                    <div class="message-content">
                        <div class="message-text">${this.processMessageContent(message.content || '')}</div>
                        <div class="message-time">${this.formatTime(message.timestamp)}</div>
                    </div>
                `;
        }

        messageElement.innerHTML = messageContent;

        // 添加消息操作按钮
        this.addMessageActions(messageElement, message, isCurrentUser);

        // 添加右键菜单
        this.addContextMenu(messageElement, message, isCurrentUser);

        // 在顶部插入消息
        messagesList.insertBefore(messageElement, messagesList.firstChild);
    },

    // 初始化滚动监听
    initScrollListener() {
        const messagesList = document.getElementById('messagesList');
        if (!messagesList) {
            console.error('找不到消息列表容器');
            return;
        }

        let isScrolling = false;

        messagesList.addEventListener('scroll', () => {
            if (isScrolling) return;

            // 检查是否滚动到顶部
            if (messagesList.scrollTop <= 100 && this.hasMoreMessages && !this.isLoadingMessages) {
                console.log('滚动到顶部，加载更多历史消息');
                isScrolling = true;

                // 显示加载指示器
                this.showLoadingIndicator();

                // 加载更多消息
                this.loadConversationMessages(true).finally(() => {
                    this.hideLoadingIndicator();
                    isScrolling = false;
                });
            }

            // 检查是否需要显示滚动到底部按钮
            this.checkScrollToBottomButton();
        });

        console.log('滚动监听初始化完成');
    },

    // 显示加载指示器
    showLoadingIndicator() {
        const messagesList = document.getElementById('messagesList');
        if (!messagesList) return;

        // 检查是否已经有加载指示器
        if (messagesList.querySelector('.loading-indicator')) return;

        const loadingDiv = document.createElement('div');
        loadingDiv.className = 'loading-indicator';
        loadingDiv.style.cssText = `
            text-align: center;
            padding: 10px;
            color: #666;
            font-size: 14px;
        `;
        loadingDiv.innerHTML = `
            <i class="fas fa-spinner fa-spin"></i> 加载历史消息中...
        `;

        messagesList.insertBefore(loadingDiv, messagesList.firstChild);
    },

    // 隐藏加载指示器
    hideLoadingIndicator() {
        const loadingIndicator = document.querySelector('.loading-indicator');
        if (loadingIndicator) {
            loadingIndicator.remove();
        }
    },

    // 添加消息操作按钮（三个点菜单）
    addMessageActions(messageElement, message, isCurrentUser) {
        const messageContent = messageElement.querySelector('.message-content');
        if (!messageContent) return;

        // 检查消息是否可以撤回（20分钟内）
        const canRecall = isCurrentUser && this.canRecallMessage(message);

        // 为所有消息添加三个点菜单
        const actionsHtml = `
            <div class="message-actions" style="display: none;">
                <div class="dropdown">
                    <button class="btn btn-sm btn-outline-secondary dropdown-toggle" type="button"
                            data-bs-toggle="dropdown" aria-expanded="false" title="更多操作">
                        <i class="fas fa-ellipsis-v"></i>
                    </button>
                    <ul class="dropdown-menu">
                        <li><a class="dropdown-item" href="#" onclick="copyMessageContent('${message.content || ''}')">
                            <i class="fas fa-copy me-2"></i>复制内容
                        </a></li>
                        <li><a class="dropdown-item" href="#" onclick="replyToMessage('${message.id}')">
                            <i class="fas fa-reply me-2"></i>回复
                        </a></li>
                        ${isCurrentUser ? `
                            <li><hr class="dropdown-divider"></li>
                            ${canRecall ? `<li><a class="dropdown-item text-warning" href="#" onclick="recallMessage('${message.id}')">
                                <i class="fas fa-undo me-2"></i>撤回消息
                            </a></li>` : ''}
                            <li><a class="dropdown-item text-danger" href="#" onclick="deleteMessage('${message.id}')">
                                <i class="fas fa-trash me-2"></i>删除消息
                            </a></li>
                        ` : ''}
                    </ul>
                </div>
            </div>
        `;

        messageContent.insertAdjacentHTML('beforeend', actionsHtml);

        // 鼠标悬停显示操作按钮
        messageElement.addEventListener('mouseenter', () => {
            const actions = messageElement.querySelector('.message-actions');
            if (actions) actions.style.display = 'block';
        });

        messageElement.addEventListener('mouseleave', () => {
            const actions = messageElement.querySelector('.message-actions');
            if (actions) actions.style.display = 'none';
        });
    },

    // 添加右键菜单
    addContextMenu(messageElement, message, isCurrentUser) {
        messageElement.addEventListener('contextmenu', (e) => {
            e.preventDefault();
            this.showContextMenu(e, message, isCurrentUser);
        });
    },

    // 显示右键菜单
    showContextMenu(event, message, isCurrentUser) {
        // 移除现有的右键菜单
        const existingMenu = document.querySelector('.message-context-menu');
        if (existingMenu) {
            existingMenu.remove();
        }

        const menu = document.createElement('div');
        menu.className = 'message-context-menu';
        menu.style.cssText = `
            position: fixed;
            top: ${event.clientY}px;
            left: ${event.clientX}px;
            background: white;
            border: 1px solid #ddd;
            border-radius: 4px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            z-index: 1000;
            min-width: 120px;
        `;

        const canRecall = isCurrentUser && this.canRecallMessage(message);

        let menuItems = [];

        // 复制消息内容
        if (message.content) {
            menuItems.push(`<div class="context-menu-item" onclick="copyMessageContent('${message.content}')">
                <i class="fas fa-copy me-2"></i>复制内容
            </div>`);
        }

        // 回复消息
        menuItems.push(`<div class="context-menu-item" onclick="replyToMessage('${message.id}')">
            <i class="fas fa-reply me-2"></i>回复
        </div>`);

        if (isCurrentUser) {
            // 撤回消息（20分钟内）
            if (canRecall) {
                menuItems.push(`<div class="context-menu-item" onclick="recallMessage('${message.id}')">
                    <i class="fas fa-undo me-2"></i>撤回消息
                </div>`);
            }

            // 删除消息
            menuItems.push(`<div class="context-menu-item text-danger" onclick="deleteMessage('${message.id}')">
                <i class="fas fa-trash me-2"></i>删除消息
            </div>`);
        }

        menu.innerHTML = menuItems.join('');

        document.body.appendChild(menu);

        // 点击其他地方关闭菜单
        setTimeout(() => {
            document.addEventListener('click', function closeMenu() {
                menu.remove();
                document.removeEventListener('click', closeMenu);
            });
        }, 0);
    },

    // 检查消息是否可以撤回（20分钟内）
    canRecallMessage(message) {
        if (!message.timestamp && !message.createdAt) return false;

        const messageTime = new Date(message.timestamp || message.createdAt);
        const now = new Date();
        const diffMinutes = (now - messageTime) / (1000 * 60);

        return diffMinutes <= 20;
    },

    // HTML转义
    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    },

    // 处理消息内容（支持@用户高亮）
    processMessageContent(content) {
        if (!content) return '';

        // 先进行HTML转义
        let processedContent = this.escapeHtml(content);

        // 处理@用户高亮
        processedContent = processedContent.replace(/@(\w+)/g, '<span class="mention">@$1</span>');

        return processedContent;
    },

    // 格式化时间
    formatTime(timestamp) {
        if (!timestamp) return '';

        let date;
        if (typeof timestamp === 'string') {
            date = new Date(timestamp);
        } else {
            date = timestamp;
        }

        if (isNaN(date.getTime())) {
            return '';
        }

        return date.toLocaleTimeString('zh-CN', {
            hour: '2-digit',
            minute: '2-digit'
        });
    },

    // WebSocket连接
    connectWebSocket() {
        try {
            if (this.connected) {
                console.log('WebSocket已连接，跳过重复连接');
                return;
            }

            // 检查重连次数限制
            if (this.reconnectAttempts && this.reconnectAttempts >= 5) {
                console.error('WebSocket重连次数过多，停止重连');
                return;
            }

            // 检查SockJS和Stomp是否可用
            if (typeof SockJS === 'undefined') {
                console.error('SockJS库未加载');
                return;
            }
            if (typeof Stomp === 'undefined') {
                console.error('Stomp库未加载');
                return;
            }

            console.log('开始建立WebSocket连接...');
            
            // 创建SockJS连接，添加超时配置
            const socket = new SockJS('/ws', null, {
                timeout: 10000, // 10秒连接超时
                heartbeat: 30000, // 30秒心跳
                disconnect_delay: 3000 // 3秒断开延迟
            });

            // 检查Stomp版本并使用相应的API
            if (typeof Stomp !== 'undefined' && Stomp.over) {
                // 旧版本Stomp (2.x)
                this.stompClient = Stomp.over(socket);
                // 禁用调试日志
                this.stompClient.debug = null;

                this.stompClient.connect({}, (frame) => {
                    console.log('聊天WebSocket连接成功: ' + frame);
                    this.connected = true;
                    this.reconnectAttempts = 0; // 重置重连计数
                    this.onWebSocketConnected();
                }, (error) => {
                    console.error('聊天WebSocket连接失败:', error);
                    this.connected = false;
                    this.stompClient = null;
                    
                    // 智能重连策略
                    this.handleReconnect();
                });
            } else if (typeof Stomp !== 'undefined' && Stomp.Client) {
                // 新版本Stomp (7.x)
                this.stompClient = new Stomp.Client({
                    webSocketFactory: () => socket,
                    debug: (str) => {
                        // 禁用调试日志
                    }
                });

                this.stompClient.onConnect = (frame) => {
                    console.log('聊天WebSocket连接成功: ' + frame);
                    this.connected = true;
                    this.reconnectAttempts = 0; // 重置重连计数
                    this.onWebSocketConnected();
                };

                this.stompClient.onStompError = (error) => {
                    console.error('聊天WebSocket连接失败:', error);
                    this.connected = false;
                    this.stompClient = null;
                    
                    // 智能重连策略
                    this.handleReconnect();
                };

                this.stompClient.activate();
            } else {
                throw new Error('Stomp库未正确加载');
            }
        } catch (error) {
            console.error('聊天WebSocket连接过程中出错:', error);
            this.connected = false;
            this.stompClient = null;
            this.handleReconnect();
        }
    },

    // 智能重连处理
    handleReconnect() {
        if (!this.reconnectAttempts) {
            this.reconnectAttempts = 0;
        }
        
        this.reconnectAttempts++;
        
        // 指数退避重连策略
        const baseDelay = 2000; // 基础延迟2秒
        const maxDelay = 60000; // 最大延迟60秒
        const reconnectDelay = Math.min(maxDelay, baseDelay * Math.pow(2, this.reconnectAttempts - 1));
        
        console.log(`WebSocket连接失败，${reconnectDelay/1000}秒后重试... (第${this.reconnectAttempts}次)`);
        
        if (this.reconnectAttempts < 5) {
            setTimeout(() => {
                this.connectWebSocket();
            }, reconnectDelay);
        } else {
            console.error('WebSocket重连次数过多，停止重连');
            // 显示用户友好的错误提示
            this.showConnectionError();
        }
    },

    // 显示连接错误提示
    showConnectionError() {
        const errorMessage = '聊天连接失败，请刷新页面重试';
        console.error(errorMessage);
        
        // 可以在这里添加UI提示
        if (typeof showToast === 'function') {
            showToast(errorMessage, 'error');
        }
    },

    // WebSocket连接成功后的处理
    onWebSocketConnected() {
        if (!this.currentUser || !this.currentUser.username) {
            console.error('当前用户信息无效，无法订阅消息');
            return;
        }

        console.log('WebSocket连接成功，当前用户:', this.currentUser);

        // 发送用户身份标识，设置在线状态
        this.sendUserIdentification();

        // 订阅当前会话的消息（如果有的话）
        if (this.currentConversationId) {
            this.subscribeToConversation(this.currentConversationId);
        }
    },

    // 发送用户身份标识
    sendUserIdentification() {
        if (!this.stompClient || !this.connected || !this.currentUser) {
            console.log('无法发送用户身份标识 - WebSocket未连接或用户信息无效');
            return;
        }

        try {
            const identificationMessage = {
                type: 'user_identification',
                userId: this.currentUser.id,
                username: this.currentUser.username,
                timestamp: Date.now()
            };

            // 使用旧版本Stomp的send方法
            this.stompClient.send('/app/user-identification', {}, JSON.stringify(identificationMessage));

            console.log('发送用户身份标识成功:', identificationMessage);
        } catch (error) {
            console.error('发送用户身份标识失败:', error);
        }
    },

    // 发送心跳保持在线状态
    sendHeartbeat() {
        if (!this.stompClient || !this.connected || !this.currentUser) {
            return;
        }

        try {
            const heartbeatMessage = {
                type: 'heartbeat',
                userId: this.currentUser.id,
                timestamp: Date.now()
            };

            // 使用旧版本Stomp的send方法
            this.stompClient.send('/app/heartbeat', {}, JSON.stringify(heartbeatMessage));

            console.log('发送心跳成功');
        } catch (error) {
            console.error('发送心跳失败:', error);
            // 心跳失败可能表示连接已断开
            this.connected = false;
        }
    },

    // 断开WebSocket连接
    disconnectWebSocket() {
        if (this.stompClient && this.connected) {
            try {
                this.stompClient.disconnect(() => {
                    console.log('WebSocket连接已断开');
                });
            } catch (error) {
                console.error('断开WebSocket连接时出错:', error);
            }
        }
        this.connected = false;
        this.stompClient = null;
        this.currentSubscription = null;
    },



    // 订阅特定会话的消息
    subscribeToConversation(conversationId) {
        if (!this.stompClient || !this.connected) {
            console.log('WebSocket未连接，无法订阅会话');
            return;
        }

        // 取消之前的订阅
        if (this.currentSubscription) {
            console.log('取消之前的会话订阅');
            this.currentSubscription.unsubscribe();
        }

        console.log('订阅会话消息:', conversationId);
        this.currentSubscription = this.stompClient.subscribe(`/topic/conversation/${conversationId}`, (message) => {
            try {
                const chatMessage = JSON.parse(message.body);
                console.log('收到会话消息:', chatMessage);
                this.handleRealtimeMessage(chatMessage);
            } catch (error) {
                console.error('解析WebSocket消息失败:', error);
            }
        });
    },

    // 处理实时消息
    handleRealtimeMessage(message) {
        console.log('收到实时消息:', message);

        try {
            // 如果是当前会话的消息，直接显示
            if (this.currentConversationId && message.conversationId === this.currentConversationId) {
                this.addMessageToUI(message);
                this.scrollToLatestMessage(true); // 强制滚动到新消息
            }

            // 更新会话列表
            this.updateConversationLastMessage(message);

            // 更新未读消息数
            this.updateUnreadCount();
        } catch (error) {
            console.error('处理实时消息失败:', error, message);
        }
    },

    // 处理会话更新
    handleConversationUpdate(conversation) {
        console.log('收到会话更新:', conversation);

        // 更新会话列表中的对应项
        const existingIndex = this.conversations.findIndex(c => c.id === conversation.id);
        if (existingIndex >= 0) {
            this.conversations[existingIndex] = conversation;
        } else {
            this.conversations.unshift(conversation);
        }

        this.renderConversations();
    },

    // 更新会话的最后一条消息
    updateConversationLastMessage(message) {
        try {
            if (!message || !message.conversationId) {
                console.warn('无效的消息数据:', message);
                return;
            }

            const conversation = this.conversations.find(c => c.id === message.conversationId);
            if (conversation) {
                conversation.lastMessage = message.content || '';
                conversation.lastMessageTime = message.timestamp || new Date().toISOString();
                conversation.unreadCount = (conversation.unreadCount || 0) + 1;

                // 将此会话移到列表顶部
                this.conversations = this.conversations.filter(c => c.id !== conversation.id);
                this.conversations.unshift(conversation);

                this.renderConversations();
            } else {
                console.warn('找不到对应的会话:', message.conversationId);
            }
        } catch (error) {
            console.error('更新会话最后消息失败:', error, message);
        }
    },






};

// 全局函数（供HTML调用）
function handleMessageInput(event) {
    ChatApp.handleMessageInput(event);
}

function autoResize(textarea) {
    ChatApp.autoResize(textarea);
}

// 异步会话选择包装函数
async function selectConversationWrapper(conversationId) {
    console.log('selectConversationWrapper被调用，会话ID:', conversationId);
    try {
        await ChatApp.selectConversation(conversationId);
        console.log('会话切换成功完成');
    } catch (error) {
        console.error('选择会话失败:', error);
        alert('切换会话失败，请重试: ' + error.message);
    }
}



function sendMessage() {
    ChatApp.sendMessage();
}

function selectFile(type) {
    ChatApp.selectFile(type);
}

function toggleEmojiPicker() {
    ChatApp.toggleEmojiPicker();
}

function clearFilePreview() {
    ChatApp.clearFilePreview();
}

// 图片加载失败时尝试备用路径的全局函数
window.tryAlternativeImagePaths = function(imgElement, originalUrl) {
    console.error('图片加载失败，尝试备用路径:', imgElement.src);

    // 从URL中提取文件名
    let fileName = '';
    if (originalUrl) {
        const urlParts = originalUrl.split('/');
        fileName = urlParts[urlParts.length - 1];

        // 移除查询参数
        if (fileName.includes('?')) {
            fileName = fileName.split('?')[0];
        }

        // 如果文件名包含路径分隔符，只取最后一部分
        if (fileName.includes('\\')) {
            fileName = fileName.split('\\').pop();
        }
    }

    // 如果还是没有文件名，尝试从src中提取
    if (!fileName && imgElement.src) {
        const srcParts = imgElement.src.split('/');
        fileName = srcParts[srcParts.length - 1];
        if (fileName.includes('?')) {
            fileName = fileName.split('?')[0];
        }
    }

    console.log('提取的文件名:', fileName);

    const alternativePaths = [
        `/api/chat/file/${fileName}`,
        `/api/chat/image/${fileName}`,
        `/uploads/chat/files/${fileName}`,
        `/uploads/chat/images/${fileName}`,
        `/uploads/${fileName}`,
        `/images/default-thumbnail.jpg` // 最后的默认图片
    ];

    let currentIndex = 0;

    function tryNextPath() {
        if (currentIndex < alternativePaths.length) {
            const nextPath = alternativePaths[currentIndex++];
            console.log('尝试备用路径:', nextPath);

            imgElement.onload = function() {
                console.log('备用路径加载成功:', nextPath);
            };

            imgElement.onerror = function() {
                console.log('备用路径失败:', nextPath);
                tryNextPath();
            };

            imgElement.src = nextPath;
        } else {
            console.error('所有路径都失败了，显示错误信息');
            imgElement.style.display = 'none';
            if (imgElement.nextElementSibling) {
                imgElement.nextElementSibling.style.display = 'block';
            }
        }
    }

    tryNextPath();
};

// ==================== 新增功能：消息管理 ====================

/**
 * 清空聊天记录
 */
function clearChatMessages() {
    if (!ChatApp.currentConversationId) {
        alert('请先选择一个会话');
        return;
    }

    if (!confirm('确定要清空所有聊天记录吗？此操作不可撤销！')) {
        return;
    }

    fetch(`/api/chat/conversations/${ChatApp.currentConversationId}/messages`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        },
        credentials: 'include'
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // 清空消息列表UI
            const messagesList = document.getElementById('messagesList');
            if (messagesList) {
                messagesList.innerHTML = '<div class="text-center text-muted py-4">聊天记录已清空</div>';
            }
            alert('聊天记录已清空');
        } else {
            alert('清空失败：' + (data.error || '未知错误'));
        }
    })
    .catch(error => {
        console.error('清空聊天记录失败:', error);
        alert('清空失败，请重试');
    });
}

/**
 * 删除会话
 */
function deleteConversation() {
    if (!ChatApp.currentConversationId) {
        alert('请先选择一个会话');
        return;
    }

    if (!confirm('确定要删除这个会话吗？此操作不可撤销！')) {
        return;
    }

    fetch(`/api/chat/conversations/${ChatApp.currentConversationId}`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        },
        credentials: 'include'
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // 从会话列表中移除
            const conversationElement = document.querySelector(`[data-conversation-id="${ChatApp.currentConversationId}"]`);
            if (conversationElement) {
                conversationElement.remove();
            }

            // 从ChatApp的会话数组中移除
            ChatApp.conversations = ChatApp.conversations.filter(conv => conv.id !== ChatApp.currentConversationId);

            // 如果删除的是当前会话，清空聊天区域
            if (ChatApp.currentConversationId) {
            ChatApp.currentConversationId = null;
            const messagesList = document.getElementById('messagesList');
            if (messagesList) {
                messagesList.innerHTML = '<div class="text-center text-muted py-4">请选择一个会话开始聊天</div>';
                }
                
                // 显示欢迎界面
                ChatApp.hideChatInterface();
            }

            // 如果会话列表为空，显示空状态
            if (ChatApp.conversations.length === 0) {
                const conversationList = document.getElementById('conversationList');
                if (conversationList) {
                    conversationList.innerHTML = `
                        <div class="text-center text-muted py-4">
                            <i class="fas fa-comments fa-3x mb-3"></i>
                            <p>暂无会话</p>
                            <p class="small">点击右上角的 + 号开始新的聊天</p>
                        </div>
                    `;
                }
            }

            alert('会话已删除');
        } else {
            alert('删除失败：' + (data.error || '未知错误'));
        }
    })
    .catch(error => {
        console.error('删除会话失败:', error);
        alert('删除失败，请重试');
    });
}

/**
 * 取消关注用户
 */
function unfollowUser() {
    if (!ChatApp.currentConversationId) {
        alert('请先选择一个会话');
        return;
    }

    if (!ChatApp.targetUser) {
        alert('无法获取目标用户信息');
        return;
    }

    const confirmMessage = `确定要取消关注 ${ChatApp.targetUser.username} 吗？取消关注后将删除与该用户的会话。`;

    if (!confirm(confirmMessage)) {
        return;
    }

    // 调用取消关注API
    fetch(`/api/follow/user/${ChatApp.targetUser.id}/toggle`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        },
        credentials: 'include'
    }).then(response => {
        if (response.ok) {
            return response.json();
        }
        throw new Error('取消关注失败');
    }).then(data => {
        if (data.success) {
            alert(data.message || '已取消关注');
            // 删除当前会话
            deleteConversation();
        } else {
            throw new Error(data.message || '取消关注失败');
        }
    }).catch(error => {
        console.error('取消关注失败:', error);
        alert('取消关注失败，请重试');
    });
}

/**
 * 置顶会话
 */
function pinConversation() {
    if (!ChatApp.currentConversationId) {
        alert('请先选择一个会话');
        return;
    }

    // TODO: 实现置顶功能
    alert('置顶功能开发中...');
}

/**
 * 静音会话
 */
function muteConversation() {
    if (!ChatApp.currentConversationId) {
        alert('请先选择一个会话');
        return;
    }

    // TODO: 实现静音功能
    alert('静音功能开发中...');
}

/**
 * 撤回消息
 */
function recallMessage(messageId) {
    if (!messageId) {
        alert('消息ID无效');
        return;
    }

    if (!confirm('确定要撤回这条消息吗？')) {
        return;
    }

    fetch(`/api/chat/messages/${messageId}/recall`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        },
        credentials: 'include'
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // 更新消息UI为撤回状态
            const messageElement = document.querySelector(`[data-message-id="${messageId}"]`);
            if (messageElement) {
                const messageContent = messageElement.querySelector('.message-content');
                if (messageContent) {
                    messageContent.innerHTML = '<em class="text-muted">消息已撤回</em>';
                }

                // 移除操作按钮
                const messageActions = messageElement.querySelector('.message-actions');
                if (messageActions) {
                    messageActions.remove();
                }
            }

            console.log('消息撤回成功');
        } else {
            alert('撤回失败：' + (data.error || '未知错误'));
        }
    })
    .catch(error => {
        console.error('撤回消息失败:', error);
        alert('撤回失败，请重试');
    });
}

/**
 * 删除消息
 */
function deleteMessage(messageId) {
    if (!messageId) {
        alert('消息ID无效');
        return;
    }

    if (!confirm('确定要删除这条消息吗？')) {
        return;
    }

    fetch(`/api/chat/messages/${messageId}`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        },
        credentials: 'include'
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // 从UI中移除消息
            const messageElement = document.querySelector(`[data-message-id="${messageId}"]`);
            if (messageElement) {
                messageElement.remove();
            }

            console.log('消息删除成功');
        } else {
            alert('删除失败：' + (data.error || '未知错误'));
        }
    })
    .catch(error => {
        console.error('删除消息失败:', error);
        alert('删除失败，请重试');
    });
}

/**
 * 复制消息内容
 */
function copyMessageContent(content) {
    if (!content) return;

    // 创建临时文本区域
    const textarea = document.createElement('textarea');
    textarea.value = content;
    document.body.appendChild(textarea);
    textarea.select();

    try {
        document.execCommand('copy');
        console.log('消息内容已复制');
        // 可以添加一个小提示
        showToast('消息内容已复制');
    } catch (err) {
        console.error('复制失败:', err);
        alert('复制失败');
    }

    document.body.removeChild(textarea);
}

/**
 * 回复消息
 */
function replyToMessage(messageId) {
    console.log('回复消息:', messageId);

    // 找到被回复的消息
    const messageElement = document.querySelector(`[data-message-id="${messageId}"]`);
    if (!messageElement) {
        alert('找不到要回复的消息');
        return;
    }

    // 获取消息内容用于显示
    const messageContent = messageElement.querySelector('.message-text');
    const senderName = messageElement.querySelector('.message-sender-name');

    let replyContent = '';
    let replyAuthor = '';

    if (messageContent) {
        replyContent = messageContent.textContent || '';
    }

    if (senderName) {
        replyAuthor = senderName.textContent || '';
    } else {
        // 从消息数据中获取发送者信息
        const messageData = ChatApp.getMessageData(messageId);
        if (messageData) {
            replyAuthor = messageData.senderName || '';
        }
    }

    // 显示回复预览
    showReplyPreview(messageId, replyAuthor, replyContent);

    // 聚焦到输入框
    const messageInput = document.getElementById('messageInput');
    if (messageInput) {
        messageInput.focus();
    }
}

/**
 * 显示提示消息
 */
function showToast(message, type = 'success') {
    // 创建提示元素
    const toast = document.createElement('div');
    toast.className = `toast-message toast-${type}`;
    toast.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: ${type === 'success' ? '#28a745' : '#dc3545'};
        color: white;
        padding: 12px 20px;
        border-radius: 4px;
        z-index: 9999;
        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        animation: slideInRight 0.3s ease-out;
    `;
    toast.textContent = message;

    // 添加动画样式
    if (!document.querySelector('#toast-styles')) {
        const style = document.createElement('style');
        style.id = 'toast-styles';
        style.textContent = `
            @keyframes slideInRight {
                from { transform: translateX(100%); opacity: 0; }
                to { transform: translateX(0); opacity: 1; }
            }
            @keyframes slideOutRight {
                from { transform: translateX(0); opacity: 1; }
                to { transform: translateX(100%); opacity: 0; }
            }
        `;
        document.head.appendChild(style);
    }

    document.body.appendChild(toast);

    // 3秒后自动消失
    setTimeout(() => {
        toast.style.animation = 'slideOutRight 0.3s ease-out';
        setTimeout(() => {
            if (toast.parentNode) {
                toast.parentNode.removeChild(toast);
            }
        }, 300);
    }, 3000);
}

// ==================== 回复功能 ====================

let currentReplyTo = null; // 当前回复的消息ID

/**
 * 显示回复预览
 */
function showReplyPreview(messageId, authorName, content) {
    currentReplyTo = messageId;

    // 移除现有的回复预览
    const existingPreview = document.querySelector('.reply-preview');
    if (existingPreview) {
        existingPreview.remove();
    }

    // 创建回复预览元素
    const replyPreview = document.createElement('div');
    replyPreview.className = 'reply-preview';
    replyPreview.innerHTML = `
        <div class="reply-preview-content">
            <div class="reply-preview-header">
                <i class="fas fa-reply me-2"></i>
                <span class="reply-preview-author">回复 ${authorName}</span>
                <button class="btn btn-sm btn-outline-secondary ms-auto" onclick="cancelReply()">
                    <i class="fas fa-times"></i>
                </button>
            </div>
            <div class="reply-preview-text">${content.length > 50 ? content.substring(0, 50) + '...' : content}</div>
        </div>
    `;

    // 插入到输入区域上方
    const messageInputArea = document.querySelector('.message-input-area');
    if (messageInputArea) {
        messageInputArea.insertBefore(replyPreview, messageInputArea.firstChild);
    }
}

/**
 * 取消回复
 */
function cancelReply() {
    currentReplyTo = null;
    const replyPreview = document.querySelector('.reply-preview');
    if (replyPreview) {
        replyPreview.remove();
    }
}

/**
 * 获取消息数据（从ChatApp中）
 */
ChatApp.getMessageData = function(messageId) {
    // 这里可以从已加载的消息中查找
    // 暂时返回null，实际使用时需要实现
    return null;
};



/**
 * 提取@用户
 */
function extractMentionedUsers(content) {
    const mentionRegex = /@(\w+)/g;
    const mentions = [];
    let match;

    while ((match = mentionRegex.exec(content)) !== null) {
        mentions.push(match[1]);
    }

    return mentions;
}

/**
 * 滚动到指定消息
 */
function scrollToMessage(messageId) {
    const messageElement = document.querySelector(`[data-message-id="${messageId}"]`);
    if (messageElement) {
        messageElement.scrollIntoView({
            behavior: 'smooth',
            block: 'center'
        });

        // 高亮显示消息
        messageElement.classList.add('message-highlight');
        setTimeout(() => {
            messageElement.classList.remove('message-highlight');
        }, 2000);
    }
}

// ==================== @功能实现 ====================

let mentionDropdown = null;
let mentionStartPos = -1;
let mentionUsers = [];

/**
 * 初始化@功能
 */
function initMentionFeature() {
    const messageInput = document.getElementById('messageInput');
    if (!messageInput) return;

    messageInput.addEventListener('input', handleMentionInput);
    messageInput.addEventListener('keydown', handleMentionKeydown);

    // 点击其他地方关闭@下拉框
    document.addEventListener('click', (e) => {
        if (mentionDropdown && !mentionDropdown.contains(e.target) && e.target !== messageInput) {
            closeMentionDropdown();
        }
    });
}

/**
 * 处理@输入
 */
function handleMentionInput(event) {
    const input = event.target;
    const value = input.value;
    const cursorPos = input.selectionStart;

    // 查找最近的@符号位置
    let atPos = -1;
    for (let i = cursorPos - 1; i >= 0; i--) {
        if (value[i] === '@') {
            atPos = i;
            break;
        } else if (value[i] === ' ' || value[i] === '\n') {
            break;
        }
    }

    if (atPos !== -1) {
        const query = value.substring(atPos + 1, cursorPos);
        mentionStartPos = atPos;

        if (query.length >= 0) {
            searchMentionUsers(query, input);
        }
    } else {
        closeMentionDropdown();
    }
}

/**
 * 处理@功能的键盘事件
 */
function handleMentionKeydown(event) {
    if (!mentionDropdown) return;

    const items = mentionDropdown.querySelectorAll('.mention-item');
    const selectedItem = mentionDropdown.querySelector('.mention-item.selected');
    let selectedIndex = -1;

    if (selectedItem) {
        selectedIndex = Array.from(items).indexOf(selectedItem);
    }

    switch (event.key) {
        case 'ArrowDown':
            event.preventDefault();
            if (selectedIndex < items.length - 1) {
                if (selectedItem) selectedItem.classList.remove('selected');
                items[selectedIndex + 1].classList.add('selected');
            }
            break;

        case 'ArrowUp':
            event.preventDefault();
            if (selectedIndex > 0) {
                if (selectedItem) selectedItem.classList.remove('selected');
                items[selectedIndex - 1].classList.add('selected');
            }
            break;

        case 'Enter':
        case 'Tab':
            event.preventDefault();
            if (selectedItem) {
                const username = selectedItem.dataset.username;
                insertMention(username, event.target);
            }
            break;

        case 'Escape':
            closeMentionDropdown();
            break;
    }
}

/**
 * 搜索@用户
 */
async function searchMentionUsers(query, inputElement) {
    try {
        if (!ChatApp.currentConversationId) {
            console.log('没有当前会话ID，无法搜索@用户');
            return;
        }

        const response = await fetch(`/api/chat/users/mention?query=${encodeURIComponent(query)}&conversationId=${ChatApp.currentConversationId}&limit=10`, {
            credentials: 'include'
        });

        if (response.ok) {
            const data = await response.json();
            if (data.success && data.users) {
                showMentionDropdown(data.users, inputElement);
            }
        }
    } catch (error) {
        console.error('搜索用户失败:', error);
    }
}

/**
 * 显示@用户下拉框
 */
function showMentionDropdown(users, inputElement) {
    closeMentionDropdown();

    if (users.length === 0) return;

    mentionDropdown = document.createElement('div');
    mentionDropdown.className = 'mention-dropdown';

    users.forEach((user, index) => {
        const item = document.createElement('div');
        item.className = 'mention-item';
        if (index === 0) item.classList.add('selected');
        item.dataset.username = user.username;

        item.innerHTML = `
            <img src="${user.avatar || '/images/default-avatar.png'}" alt="${user.username}">
            <span class="mention-item-name">${user.username}</span>
        `;

        item.addEventListener('click', () => {
            insertMention(user.username, inputElement);
        });

        mentionDropdown.appendChild(item);
    });

    // 定位下拉框
    const inputRect = inputElement.getBoundingClientRect();
    mentionDropdown.style.position = 'fixed';
    mentionDropdown.style.left = inputRect.left + 'px';
    mentionDropdown.style.top = (inputRect.top - mentionDropdown.offsetHeight - 10) + 'px';

    document.body.appendChild(mentionDropdown);

    // 重新计算位置（因为现在有了实际高度）
    setTimeout(() => {
        const dropdownRect = mentionDropdown.getBoundingClientRect();
        mentionDropdown.style.top = (inputRect.top - dropdownRect.height - 10) + 'px';
    }, 0);
}

/**
 * 插入@用户
 */
function insertMention(username, inputElement) {
    const value = inputElement.value;
    const beforeMention = value.substring(0, mentionStartPos);
    const afterCursor = value.substring(inputElement.selectionStart);

    const newValue = beforeMention + '@' + username + ' ' + afterCursor;
    inputElement.value = newValue;

    // 设置光标位置
    const newCursorPos = beforeMention.length + username.length + 2;
    inputElement.setSelectionRange(newCursorPos, newCursorPos);

    closeMentionDropdown();
    inputElement.focus();
}

/**
 * 关闭@用户下拉框
 */
function closeMentionDropdown() {
    if (mentionDropdown) {
        mentionDropdown.remove();
        mentionDropdown = null;
    }
    mentionStartPos = -1;
}

// 在ChatApp初始化时启用@功能
const originalInit = ChatApp.init;
ChatApp.init = function() {
    originalInit.call(this);
    initMentionFeature();
};

// ==================== 右键菜单功能 ====================

// 全局变量存储当前右键点击的会话信息
let contextMenuConversationId = null;
let contextMenuConversationName = null;

/**
 * 显示会话右键菜单
 */
function showConversationContextMenu(event, conversationId, conversationName) {
    event.preventDefault();
    
    // 存储当前右键点击的会话信息
    contextMenuConversationId = conversationId;
    contextMenuConversationName = conversationName;
    
    const contextMenu = document.getElementById('conversationContextMenu');
    if (!contextMenu) {
        console.error('找不到右键菜单元素');
        return;
    }
    
    // 设置菜单位置
    contextMenu.style.left = event.pageX + 'px';
    contextMenu.style.top = event.pageY + 'px';
    contextMenu.style.display = 'block';
    
    // 添加点击外部关闭菜单的事件
    setTimeout(() => {
        document.addEventListener('click', hideConversationContextMenu);
        document.addEventListener('contextmenu', hideConversationContextMenu);
    }, 100);
}

/**
 * 隐藏会话右键菜单
 */
function hideConversationContextMenu(event) {
    const contextMenu = document.getElementById('conversationContextMenu');
    if (contextMenu) {
        contextMenu.style.display = 'none';
    }
    
    // 移除事件监听器
    document.removeEventListener('click', hideConversationContextMenu);
    document.removeEventListener('contextmenu', hideConversationContextMenu);
}

/**
 * 打开会话（从右键菜单）
 */
function openConversation() {
    if (contextMenuConversationId) {
        selectConversationWrapper(contextMenuConversationId);
        hideConversationContextMenu();
    }
}

/**
 * 置顶会话（从右键菜单）
 */
function pinConversationFromContext() {
    if (contextMenuConversationId) {
        // 设置当前会话ID并调用置顶功能
        ChatApp.currentConversationId = contextMenuConversationId;
        pinConversation();
        hideConversationContextMenu();
    }
}

/**
 * 静音会话（从右键菜单）
 */
function muteConversationFromContext() {
    if (contextMenuConversationId) {
        // 设置当前会话ID并调用静音功能
        ChatApp.currentConversationId = contextMenuConversationId;
        muteConversation();
        hideConversationContextMenu();
    }
}

/**
 * 清空聊天记录（从右键菜单）
 */
function clearChatMessagesFromContext() {
    if (contextMenuConversationId) {
        // 设置当前会话ID并调用清空功能
        ChatApp.currentConversationId = contextMenuConversationId;
        clearChatMessages();
        hideConversationContextMenu();
    }
}

/**
 * 取消关注（从右键菜单）
 */
function unfollowUserFromContext() {
    if (contextMenuConversationId) {
        // 设置当前会话ID并调用取消关注功能
        ChatApp.currentConversationId = contextMenuConversationId;
        unfollowUser();
        hideConversationContextMenu();
    }
}

/**
 * 删除会话（从右键菜单）
 */
function deleteConversationFromContext() {
    if (contextMenuConversationId) {
        // 设置当前会话ID并调用删除功能
        ChatApp.currentConversationId = contextMenuConversationId;
        deleteConversation();
        hideConversationContextMenu();
    }
}
