/**
 * 头像同步工具
 * 用于在不同页面之间同步头像更新
 */

window.AvatarSync = {
    
    /**
     * 初始化头像同步
     */
    init() {
        this.bindEvents();
        console.log('头像同步工具已初始化');
    },

    /**
     * 绑定事件监听
     */
    bindEvents() {
        // 监听localStorage变化（跨窗口/标签页）
        window.addEventListener('storage', (e) => {
            if (e.key === 'avatarUpdate') {
                try {
                    const data = JSON.parse(e.newValue);
                    this.handleAvatarUpdate(data);
                } catch (error) {
                    console.error('解析头像更新数据失败:', error);
                }
            }
        });

        // 监听自定义头像更新事件（同一页面内）
        window.addEventListener('avatarUpdated', (e) => {
            this.handleAvatarUpdate(e.detail);
        });
    },

    /**
     * 处理头像更新
     */
    handleAvatarUpdate(data) {
        const { userId, avatarUrl } = data;
        
        console.log('收到头像更新通知:', data);
        
        // 更新当前用户信息
        if (window.currentUser && window.currentUser.id === userId) {
            window.currentUser.avatar = avatarUrl;
            this.updatePageAvatars(avatarUrl);
        }
    },

    /**
     * 更新页面中的头像显示
     */
    updatePageAvatars(avatarUrl) {
        // 更新导航栏头像
        this.updateNavbarAvatar(avatarUrl);
        
        // 更新聊天页面头像（如果存在ChatApp）
        if (window.ChatApp && typeof window.ChatApp.syncAvatarUpdate === 'function') {
            window.ChatApp.syncAvatarUpdate(window.currentUser.id, avatarUrl);
        }
        
        // 更新个人资料页面头像
        this.updateProfileAvatar(avatarUrl);
    },

    /**
     * 更新导航栏头像
     */
    updateNavbarAvatar(avatarUrl) {
        const navAvatar = document.querySelector('.navbar .dropdown-toggle img');
        const navContainer = document.querySelector('.navbar .dropdown-toggle');
        
        if (navContainer) {
            if (avatarUrl && avatarUrl !== '/images/default-avatar.png') {
                // 显示自定义头像
                if (navAvatar) {
                    navAvatar.src = avatarUrl;
                    navAvatar.style.display = 'block';
                } else {
                    // 创建img元素
                    const username = window.currentUser ? window.currentUser.username : '';
                    navContainer.innerHTML = `
                        <img src="${avatarUrl}" alt="头像" class="rounded-circle me-1" width="24" height="24">
                        <span>${username}</span>
                    `;
                }
            } else {
                // 显示默认头像（用户名首字母）
                const username = window.currentUser ? window.currentUser.username : '?';
                const initial = username.charAt(0).toUpperCase();
                navContainer.innerHTML = `
                    <div style="width: 24px; height: 24px; border-radius: 50%; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); display: flex; align-items: center; justify-content: center; color: white; font-size: 0.8rem; margin-right: 0.25rem;">
                        ${initial}
                    </div>
                    <span>${username}</span>
                `;
            }
        }
    },

    /**
     * 更新个人资料页面头像
     */
    updateProfileAvatar(avatarUrl) {
        // 更新个人资料页面的头像显示
        const profileAvatar = document.querySelector('.profile-avatar-compact img');
        const profileContainer = document.querySelector('.profile-avatar-compact');
        
        if (profileContainer) {
            if (avatarUrl && avatarUrl !== '/images/default-avatar.png') {
                // 显示自定义头像
                if (profileAvatar) {
                    profileAvatar.src = avatarUrl;
                    profileAvatar.style.display = 'block';
                } else {
                    profileContainer.innerHTML = `<img src="${avatarUrl}" alt="用户头像" class="avatar-img">`;
                }
            } else {
                // 显示默认头像图标
                profileContainer.innerHTML = `<i class="fas fa-user"></i>`;
            }
        }

        // 更新编辑模态框中的头像预览
        const avatarPreview = document.querySelector('.avatar-preview');
        const avatarPlaceholder = document.querySelector('.avatar-placeholder');
        
        if (avatarUrl && avatarUrl !== '/images/default-avatar.png') {
            if (avatarPreview) {
                avatarPreview.src = avatarUrl;
                avatarPreview.style.display = 'block';
            }
            if (avatarPlaceholder) {
                avatarPlaceholder.style.display = 'none';
            }
        } else {
            if (avatarPreview) {
                avatarPreview.style.display = 'none';
            }
            if (avatarPlaceholder) {
                avatarPlaceholder.style.display = 'flex';
            }
        }
    },

    /**
     * 触发头像更新事件
     */
    triggerUpdate(userId, avatarUrl) {
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
    }
};

// 页面加载完成后自动初始化
document.addEventListener('DOMContentLoaded', function() {
    window.AvatarSync.init();
});
