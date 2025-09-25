/**
 * 全局通知管理器
 * 负责在所有页面显示未读通知红点
 */
class GlobalNotificationManager {
    constructor() {
        this.updateInterval = 30000; // 30秒更新一次
        this.intervalId = null;
        this.lastUnreadCount = 0;
        this.init();
    }

    init() {
        this.createNotificationBadge();
        this.startPeriodicUpdate();
        this.bindEvents();
        
        // 立即检查一次
        this.updateNotificationCount();
    }

    /**
     * 创建通知徽章
     */
    createNotificationBadge() {
        // 查找导航栏中的通知链接
        const notificationLinks = document.querySelectorAll('a[href="/notifications"], a[href*="notification"]');
        
        notificationLinks.forEach(link => {
            if (!link.querySelector('.notification-badge')) {
                const badge = document.createElement('span');
                badge.className = 'notification-badge';
                badge.style.cssText = `
                    position: absolute;
                    top: -5px;
                    right: -5px;
                    background: #ff4757;
                    color: white;
                    border-radius: 50%;
                    width: 18px;
                    height: 18px;
                    font-size: 11px;
                    display: none;
                    align-items: center;
                    justify-content: center;
                    font-weight: bold;
                    z-index: 1000;
                `;
                
                // 确保父元素有相对定位
                if (getComputedStyle(link).position === 'static') {
                    link.style.position = 'relative';
                }
                
                link.appendChild(badge);
            }
        });

        // 如果没有找到通知链接，创建一个全局的通知指示器
        if (notificationLinks.length === 0) {
            this.createGlobalIndicator();
        }
    }

    /**
     * 创建全局通知指示器
     */
    createGlobalIndicator() {
        if (document.getElementById('global-notification-indicator')) {
            return;
        }

        const indicator = document.createElement('div');
        indicator.id = 'global-notification-indicator';
        indicator.innerHTML = `
            <div class="notification-dot"></div>
            <span class="notification-count">0</span>
        `;
        indicator.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: #ff4757;
            color: white;
            border-radius: 20px;
            padding: 8px 12px;
            font-size: 12px;
            font-weight: bold;
            cursor: pointer;
            z-index: 9999;
            display: none;
            align-items: center;
            gap: 5px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.2);
            transition: all 0.3s ease;
        `;

        const dot = indicator.querySelector('.notification-dot');
        dot.style.cssText = `
            width: 8px;
            height: 8px;
            background: white;
            border-radius: 50%;
            animation: pulse 2s infinite;
        `;

        // 添加脉冲动画
        const style = document.createElement('style');
        style.textContent = `
            @keyframes pulse {
                0% { opacity: 1; }
                50% { opacity: 0.5; }
                100% { opacity: 1; }
            }
        `;
        document.head.appendChild(style);

        // 点击跳转到通知页面
        indicator.addEventListener('click', () => {
            window.location.href = '/notifications';
        });

        document.body.appendChild(indicator);
    }

    /**
     * 更新通知数量
     */
    async updateNotificationCount() {
        try {
            const response = await fetch('/api/notifications/unread-count', {
                method: 'GET',
                credentials: 'same-origin'
            });

            if (response.ok) {
                const data = await response.json();
                this.updateBadges(data.count, data.hasUnread);
                
                // 如果有新通知，播放提示音
                if (data && data.count > this.lastUnreadCount && this.lastUnreadCount >= 0) {
                    this.playNotificationSound();
                }

                this.lastUnreadCount = data ? data.count : 0;
            }
        } catch (error) {
            console.log('获取通知数量失败:', error);
        }
    }

    /**
     * 更新所有徽章
     */
    updateBadges(count, hasUnread) {
        // 更新导航栏徽章
        const badges = document.querySelectorAll('.notification-badge');
        badges.forEach(badge => {
            if (hasUnread && count > 0) {
                badge.textContent = count > 99 ? '99+' : count.toString();
                badge.style.display = 'flex';
            } else {
                badge.style.display = 'none';
            }
        });

        // 更新全局指示器
        const globalIndicator = document.getElementById('global-notification-indicator');
        if (globalIndicator) {
            const countSpan = globalIndicator.querySelector('.notification-count');
            if (hasUnread && count > 0) {
                countSpan.textContent = count > 99 ? '99+' : count.toString();
                globalIndicator.style.display = 'flex';
            } else {
                globalIndicator.style.display = 'none';
            }
        }

        // 更新页面标题
        this.updatePageTitle(count, hasUnread);
    }

    /**
     * 更新页面标题
     */
    updatePageTitle(count, hasUnread) {
        const originalTitle = document.title.replace(/^\(\d+\)\s*/, '');
        
        if (hasUnread && count > 0) {
            document.title = `(${count}) ${originalTitle}`;
        } else {
            document.title = originalTitle;
        }
    }

    /**
     * 播放通知提示音
     */
    playNotificationSound() {
        try {
            // 创建一个简单的提示音
            const audioContext = new (window.AudioContext || window.webkitAudioContext)();
            const oscillator = audioContext.createOscillator();
            const gainNode = audioContext.createGain();
            
            oscillator.connect(gainNode);
            gainNode.connect(audioContext.destination);
            
            oscillator.frequency.setValueAtTime(800, audioContext.currentTime);
            oscillator.frequency.setValueAtTime(600, audioContext.currentTime + 0.1);
            
            gainNode.gain.setValueAtTime(0.1, audioContext.currentTime);
            gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.2);
            
            oscillator.start(audioContext.currentTime);
            oscillator.stop(audioContext.currentTime + 0.2);
        } catch (error) {
            console.log('播放提示音失败:', error);
        }
    }

    /**
     * 开始定期更新
     */
    startPeriodicUpdate() {
        if (this.intervalId) {
            clearInterval(this.intervalId);
        }
        
        this.intervalId = setInterval(() => {
            this.updateNotificationCount();
        }, this.updateInterval);
    }

    /**
     * 停止定期更新
     */
    stopPeriodicUpdate() {
        if (this.intervalId) {
            clearInterval(this.intervalId);
            this.intervalId = null;
        }
    }

    /**
     * 绑定事件
     */
    bindEvents() {
        // 页面可见性变化时的处理
        document.addEventListener('visibilitychange', () => {
            if (document.hidden) {
                this.stopPeriodicUpdate();
            } else {
                this.startPeriodicUpdate();
                this.updateNotificationCount(); // 立即更新
            }
        });

        // 窗口焦点变化时的处理
        window.addEventListener('focus', () => {
            this.updateNotificationCount();
        });

        // 监听通知相关的自定义事件
        document.addEventListener('notificationRead', () => {
            setTimeout(() => this.updateNotificationCount(), 1000);
        });

        document.addEventListener('notificationReceived', () => {
            this.updateNotificationCount();
        });
    }

    /**
     * 手动刷新通知数量
     */
    refresh() {
        this.updateNotificationCount();
    }

    /**
     * 销毁管理器
     */
    destroy() {
        this.stopPeriodicUpdate();
        
        // 移除徽章
        const badges = document.querySelectorAll('.notification-badge');
        badges.forEach(badge => badge.remove());
        
        // 移除全局指示器
        const globalIndicator = document.getElementById('global-notification-indicator');
        if (globalIndicator) {
            globalIndicator.remove();
        }
        
        // 恢复页面标题
        document.title = document.title.replace(/^\(\d+\)\s*/, '');
    }
}

// 自动初始化
let globalNotificationManager = null;

document.addEventListener('DOMContentLoaded', () => {
    // 只在用户登录时初始化
    const userDataElement = document.querySelector('[data-current-user]');
    const username = userDataElement ? userDataElement.getAttribute('data-username') : null;
    
    if (username && username.trim() !== '') {
        globalNotificationManager = new GlobalNotificationManager();
        
        // 暴露到全局作用域，方便其他脚本调用
        window.globalNotificationManager = globalNotificationManager;
    }
});

// 页面卸载时清理
window.addEventListener('beforeunload', () => {
    if (globalNotificationManager) {
        globalNotificationManager.destroy();
    }
});
