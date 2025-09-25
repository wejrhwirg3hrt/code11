/**
 * 实时功能JavaScript
 * 包含WebSocket连接、实时评论、通知等功能
 */
class RealtimeFeatures {
    constructor() {
        try {
            this.stompClient = null;
            this.connected = false;
            this.currentVideoId = null;
            this.currentUser = null;
            this.notificationCount = 0;

            this.init();
        } catch (error) {
            console.error('RealtimeFeatures构造函数出错:', error);
        }
    }

    init() {
        try {
            this.getCurrentUser();
            this.connect();
            this.bindEvents();
            this.loadNotifications();
        } catch (error) {
            console.error('RealtimeFeatures初始化出错:', error);
        }
    }

    getCurrentUser() {
        // 从页面获取当前用户信息
        const userElement = document.querySelector('[data-current-user]');
        if (userElement) {
            this.currentUser = {
                username: userElement.dataset.username,
                avatar: userElement.dataset.avatar
            };
        }
    }

    connect() {
        try {
            if (this.connected) return;

            // 检查SockJS和Stomp是否可用
            if (typeof SockJS === 'undefined') {
                console.error('SockJS库未加载');
                return;
            }
            if (typeof Stomp === 'undefined') {
                console.error('Stomp库未加载');
                return;
            }

            const socket = new SockJS('/ws');
            this.stompClient = Stomp.over(socket);

            // 禁用调试日志
            this.stompClient.debug = null;

            this.stompClient.connect({}, (frame) => {
                console.log('WebSocket连接成功: ' + frame);
                this.connected = true;
                this.onConnected();
            }, (error) => {
                console.error('WebSocket连接失败:', error);
                this.connected = false;
                // 5秒后重连
                setTimeout(() => this.connect(), 5000);
            });
        } catch (error) {
            console.error('WebSocket连接过程中出错:', error);
            this.connected = false;
        }
    }

    onConnected() {
        // 订阅个人通知
        if (this.currentUser) {
            this.stompClient.subscribe(`/user/${this.currentUser.username}/notifications`, (message) => {
                this.handleNotification(JSON.parse(message.body));
            });
        }

        // 如果在视频页面，订阅视频相关消息
        this.subscribeToVideo();
    }

    subscribeToVideo() {
        const videoElement = document.querySelector('[data-video-id]');
        if (videoElement) {
            this.currentVideoId = videoElement.dataset.videoId;
            
            // 订阅视频评论
            this.stompClient.subscribe(`/topic/video/${this.currentVideoId}/comments`, (message) => {
                this.handleRealtimeComment(JSON.parse(message.body));
            });

            // 订阅视频弹幕
            this.stompClient.subscribe(`/topic/video/${this.currentVideoId}/danmaku`, (message) => {
                this.handleRealtimeDanmaku(JSON.parse(message.body));
            });

            // 订阅视频活动
            this.stompClient.subscribe(`/topic/video/${this.currentVideoId}/activity`, (message) => {
                this.handleVideoActivity(JSON.parse(message.body));
            });

            // 发送加入视频观看消息
            this.sendMessage('/app/video.join', {
                videoId: this.currentVideoId
            });
        }
    }

    sendMessage(destination, message) {
        if (this.connected && this.stompClient) {
            this.stompClient.send(destination, {}, JSON.stringify(message));
        }
    }

    // 发送实时评论
    sendRealtimeComment(content) {
        if (!this.currentVideoId || !content.trim()) return;

        this.sendMessage('/app/comment.send', {
            content: content,
            videoId: this.currentVideoId
        });
    }

    // 发送弹幕
    sendDanmaku(content, time) {
        if (!this.currentVideoId || !content.trim()) return;

        this.sendMessage('/app/danmaku.send', {
            content: content,
            videoId: this.currentVideoId,
            time: time
        });
    }

    // 订阅弹幕
    subscribeToDanmaku(videoId, callback) {
        if (!this.connected || !this.stompClient) {
            console.warn('WebSocket未连接，无法订阅弹幕');
            return;
        }

        const topic = `/topic/video/${videoId}/danmaku`;
        this.stompClient.subscribe(topic, (message) => {
            try {
                const danmaku = JSON.parse(message.body);
                if (callback && typeof callback === 'function') {
                    callback(danmaku);
                }
            } catch (error) {
                console.error('解析弹幕消息失败:', error);
            }
        });

        console.log(`已订阅视频 ${videoId} 的弹幕频道`);
    }

    // 处理实时评论
    handleRealtimeComment(message) {
        const commentsContainer = document.querySelector('.comments-list');
        if (!commentsContainer) return;

        const commentHTML = this.createCommentHTML(message);
        
        // 添加到评论列表顶部
        const emptyMessage = commentsContainer.querySelector('.text-center.text-muted');
        if (emptyMessage) {
            emptyMessage.remove();
        }
        
        commentsContainer.insertAdjacentHTML('afterbegin', commentHTML);
        
        // 添加动画效果
        const newComment = commentsContainer.firstElementChild;
        newComment.style.opacity = '0';
        newComment.style.transform = 'translateY(-20px)';
        
        setTimeout(() => {
            newComment.style.transition = 'all 0.3s ease';
            newComment.style.opacity = '1';
            newComment.style.transform = 'translateY(0)';
        }, 100);

        // 显示通知
        this.showToast('新评论', `${message.username}: ${message.content}`);
    }

    // 处理实时弹幕
    handleRealtimeDanmaku(message) {
        // 如果页面有增强视频播放器，添加弹幕
        const player = document.querySelector('.enhanced-video-player');
        if (player && window.enhancedPlayer) {
            window.enhancedPlayer.addDanmaku(message.content, message.time);
        }
    }

    // 处理视频活动
    handleVideoActivity(message) {
        if (message.type === 'USER_JOIN') {
            this.showToast('用户加入', message.content, 'info');
        } else if (message.type === 'USER_LEAVE') {
            this.showToast('用户离开', message.content, 'info');
        }
    }

    // 处理通知
    handleNotification(notification) {
        this.notificationCount++;
        this.updateNotificationBadge();
        
        // 显示通知
        this.showNotificationToast(notification);
        
        // 播放通知声音
        this.playNotificationSound();
    }

    // 创建评论HTML
    createCommentHTML(message) {
        const timeAgo = this.timeAgo(new Date(message.createdAt));
        const avatar = message.userAvatar || '/images/default-avatar.png';
        
        return `
            <div class="comment-item border-bottom pb-3 mb-3">
                <div class="d-flex">
                    <img src="${avatar}" alt="${message.username}" 
                         class="rounded-circle me-3" style="width: 40px; height: 40px; object-fit: cover;">
                    <div class="flex-grow-1">
                        <div class="d-flex justify-content-between align-items-start">
                            <h6 class="mb-1">${message.username}</h6>
                            <small class="text-muted">${timeAgo}</small>
                        </div>
                        <p class="mb-0">${this.escapeHtml(message.content)}</p>
                    </div>
                </div>
            </div>
        `;
    }

    // 绑定事件
    bindEvents() {
        // 评论表单提交
        const commentForm = document.querySelector('form[action*="/comment"]');
        if (commentForm) {
            commentForm.addEventListener('submit', (e) => {
                e.preventDefault();
                const textarea = commentForm.querySelector('textarea[name="content"]');
                if (textarea && textarea.value.trim()) {
                    this.sendRealtimeComment(textarea.value.trim());
                    textarea.value = '';
                }
            });
        }

        // 通知相关事件
        this.bindNotificationEvents();
    }

    bindNotificationEvents() {
        // 通知铃铛点击
        const notificationBell = document.querySelector('.notification-bell');
        if (notificationBell) {
            notificationBell.addEventListener('click', () => {
                this.toggleNotificationPanel();
            });
        }

        // 标记所有通知为已读
        const markAllReadBtn = document.querySelector('.mark-all-read');
        if (markAllReadBtn) {
            markAllReadBtn.addEventListener('click', () => {
                this.markAllNotificationsAsRead();
            });
        }
    }

    // 加载通知
    loadNotifications() {
        fetch('/api/notifications/unread-count')
            .then(response => response.json())
            .then(count => {
                this.notificationCount = count;
                this.updateNotificationBadge();
            })
            .catch(error => console.error('加载通知数量失败:', error));
    }

    // 更新通知徽章
    updateNotificationBadge() {
        const badge = document.querySelector('.notification-badge');
        if (badge) {
            if (this.notificationCount > 0) {
                badge.textContent = this.notificationCount > 99 ? '99+' : this.notificationCount;
                badge.style.display = 'inline-block';
            } else {
                badge.style.display = 'none';
            }
        }
    }

    // 显示Toast通知
    showToast(title, message, type = 'success') {
        // 创建Toast元素
        const toast = document.createElement('div');
        toast.className = `toast align-items-center text-white bg-${type} border-0`;
        toast.setAttribute('role', 'alert');
        toast.innerHTML = `
            <div class="d-flex">
                <div class="toast-body">
                    <strong>${title}</strong><br>
                    ${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" 
                        data-bs-dismiss="toast"></button>
            </div>
        `;

        // 添加到页面
        let toastContainer = document.querySelector('.toast-container');
        if (!toastContainer) {
            toastContainer = document.createElement('div');
            toastContainer.className = 'toast-container position-fixed top-0 end-0 p-3';
            document.body.appendChild(toastContainer);
        }
        
        toastContainer.appendChild(toast);

        // 显示Toast
        const bsToast = new bootstrap.Toast(toast, { delay: 3000 });
        bsToast.show();

        // 自动移除
        toast.addEventListener('hidden.bs.toast', () => {
            toast.remove();
        });
    }

    // 显示通知Toast
    showNotificationToast(notification) {
        const type = this.getNotificationToastType(notification.type);
        this.showToast(notification.title, notification.content, type);
    }

    getNotificationToastType(notificationType) {
        switch (notificationType) {
            case 'LIKE': return 'danger';
            case 'FAVORITE': return 'warning';
            case 'COMMENT': return 'primary';
            case 'FOLLOW': return 'success';
            case 'SYSTEM': return 'info';
            default: return 'secondary';
        }
    }

    // 播放通知声音
    playNotificationSound() {
        try {
            // 检查是否支持音频播放
            if (typeof Audio === 'undefined') {
                return;
            }

            const audio = new Audio('/sounds/notification.mp3');
            audio.volume = 0.3;

            // 设置错误处理
            audio.onerror = () => {
                console.log('通知音频加载失败，使用静默模式');
            };

            audio.play().catch(() => {
                // 忽略播放失败（用户可能没有交互过页面或音频文件不存在）
                console.log('通知音频播放失败，可能需要用户交互');
            });
        } catch (error) {
            // 忽略音频播放错误
            console.log('音频播放不可用');
        }
    }

    // 工具方法
    timeAgo(date) {
        const now = new Date();
        const diffInSeconds = Math.floor((now - date) / 1000);
        
        if (diffInSeconds < 60) return '刚刚';
        if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)}分钟前`;
        if (diffInSeconds < 86400) return `${Math.floor(diffInSeconds / 3600)}小时前`;
        if (diffInSeconds < 2592000) return `${Math.floor(diffInSeconds / 86400)}天前`;
        
        return date.toLocaleDateString();
    }

    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    // 页面卸载时断开连接
    disconnect() {
        if (this.connected && this.stompClient) {
            // 发送离开视频观看消息
            if (this.currentVideoId) {
                this.sendMessage('/app/video.leave', {
                    videoId: this.currentVideoId
                });
            }
            
            this.stompClient.disconnect();
            this.connected = false;
        }
    }
}

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', function() {
    try {
        // 检查是否有SockJS和Stomp库
        if (typeof SockJS !== 'undefined' && typeof Stomp !== 'undefined') {
            console.log('初始化实时功能...');
            window.realtimeFeatures = new RealtimeFeatures();

            // 页面卸载时断开连接
            window.addEventListener('beforeunload', () => {
                try {
                    if (window.realtimeFeatures) {
                        window.realtimeFeatures.disconnect();
                    }
                } catch (error) {
                    console.error('断开连接时出错:', error);
                }
            });
        } else {
            console.warn('SockJS或Stomp库未加载，实时功能不可用');
            console.log('SockJS available:', typeof SockJS !== 'undefined');
            console.log('Stomp available:', typeof Stomp !== 'undefined');
        }
    } catch (error) {
        console.error('初始化实时功能时出错:', error);
    }
});
