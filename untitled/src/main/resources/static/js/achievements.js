// 成就页面JavaScript
class AchievementsManager {
    constructor() {
        this.achievements = [];
        this.currentFilter = 'all';
        this.init();
    }

    init() {
        this.bindEvents();
        this.loadAchievements();
        this.loadStats();
    }

    bindEvents() {
        // 过滤器按钮
        document.querySelectorAll('.filter-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                this.setFilter(e.target.dataset.filter);
            });
        });

        // 成就卡片点击
        document.addEventListener('click', (e) => {
            if (e.target.closest('.achievement-card')) {
                const achievementId = e.target.closest('.achievement-card').dataset.achievementId;
                this.showAchievementDetail(achievementId);
            }
        });

        // 模态框关闭
        const modal = document.getElementById('achievementModal');
        if (modal) {
            const closeBtn = modal.querySelector('.modal-close');
            if (closeBtn) {
                closeBtn.addEventListener('click', () => this.hideAchievementDetail());
            }
            
            modal.addEventListener('click', (e) => {
                if (e.target === modal) {
                    this.hideAchievementDetail();
                }
            });
        }
    }

    async loadAchievements() {
        try {
            const response = await fetch('/api/achievements/current-user');
            if (response.ok) {
                const data = await response.json();
                if (data.success) {
                    this.achievements = data.achievements || [];
                    this.renderAchievements();

                    // 同时更新统计数据
                    if (data.statistics) {
                        this.renderStats(data.statistics);
                    }
                } else {
                    console.error('API error:', data.message);
                    this.showError(data.message || '加载成就失败');
                }
            } else {
                this.showError('网络请求失败');
            }
        } catch (error) {
            console.error('Error loading achievements:', error);
            this.showError('加载成就失败');
        }
    }

    async loadStats() {
        // 统计数据现在在 loadAchievements 中一起加载
        // 保留这个方法以防其他地方调用
    }

    renderStats(stats) {
        const statsContainer = document.querySelector('.achievements-stats');
        if (!statsContainer) return;

        statsContainer.innerHTML = `
            <div class="stat-card">
                <div class="stat-number">${stats.unlockedCount || 0}</div>
                <p class="stat-label">已解锁成就</p>
            </div>
            <div class="stat-card">
                <div class="stat-number">${stats.totalCount || 0}</div>
                <p class="stat-label">总成就数</p>
            </div>
            <div class="stat-card">
                <div class="stat-number">${stats.completionRate || 0}%</div>
                <p class="stat-label">完成率</p>
            </div>
            <div class="stat-card">
                <div class="stat-number">${stats.totalPoints || 0}</div>
                <p class="stat-label">成就点数</p>
            </div>
        `;
    }

    setFilter(filter) {
        this.currentFilter = filter;
        
        // 更新按钮状态
        document.querySelectorAll('.filter-btn').forEach(btn => {
            btn.classList.remove('active');
        });
        document.querySelector(`[data-filter="${filter}"]`).classList.add('active');

        // 过滤成就
        this.renderAchievements();
    }

    renderAchievements() {
        const achievementsGrid = document.getElementById('achievementsGrid');
        if (!achievementsGrid) return;

        let filteredAchievements = this.achievements;
        
        switch (this.currentFilter) {
            case 'unlocked':
                filteredAchievements = this.achievements.filter(a => a.completed);
                break;
            case 'locked':
                filteredAchievements = this.achievements.filter(a => !a.completed);
                break;
            case 'rare':
                filteredAchievements = this.achievements.filter(a => a.rarity === 'rare' || a.rarity === 'epic' || a.rarity === 'legendary');
                break;
            case 'all':
            default:
                filteredAchievements = this.achievements;
                break;
        }

        if (filteredAchievements.length === 0) {
            achievementsGrid.innerHTML = `
                <div class="empty-achievements">
                    <div class="empty-achievements-icon">🏆</div>
                    <h3>暂无成就</h3>
                    <p>继续使用网站来解锁更多成就吧！</p>
                </div>
            `;
            return;
        }

        achievementsGrid.innerHTML = filteredAchievements.map(achievement => `
            <div class="achievement-card ${achievement.completed ? 'unlocked' : 'locked'}"
                 data-achievement-id="${achievement.id}">
                <div class="achievement-header">
                    <span class="achievement-icon">${achievement.icon || '🏆'}</span>
                    <span class="achievement-rarity rarity-${achievement.rarity || 'common'}">${this.getRarityText(achievement.rarity)}</span>
                </div>
                <div class="achievement-body">
                    <h3 class="achievement-name">${achievement.name}</h3>
                    <p class="achievement-description">${achievement.description}</p>
                    ${this.renderProgress(achievement)}
                </div>
                <div class="achievement-footer">
                    <span class="achievement-points">+${achievement.points || 0} 点</span>
                    <span class="achievement-date">${achievement.completed ? this.formatDate(achievement.completedAt) : '未解锁'}</span>
                </div>
            </div>
        `).join('');
    }

    renderProgress(achievement) {
        if (!achievement.progress) return '';
        
        const percentage = Math.min((achievement.progress.current / achievement.progress.target) * 100, 100);
        
        return `
            <div class="achievement-progress">
                <div class="progress-label">
                    <span>进度</span>
                    <span>${achievement.progress.current}/${achievement.progress.target}</span>
                </div>
                <div class="progress-bar">
                    <div class="progress-fill" style="width: ${percentage}%"></div>
                </div>
            </div>
        `;
    }

    getRarityText(rarity) {
        const rarityMap = {
            'common': '普通',
            'rare': '稀有',
            'epic': '史诗',
            'legendary': '传说'
        };
        return rarityMap[rarity] || '普通';
    }

    showAchievementDetail(achievementId) {
        const achievement = this.achievements.find(a => a.id == achievementId);
        if (!achievement) return;

        const modal = document.getElementById('achievementModal');
        if (!modal) return;

        // 更新模态框内容
        const modalContent = modal.querySelector('.achievement-modal-content');
        if (modalContent) {
            modalContent.innerHTML = `
                <button class="modal-close">×</button>
                <div class="modal-achievement-header">
                    <span class="modal-achievement-icon">${achievement.icon || '🏆'}</span>
                    <h2 class="modal-achievement-name">${achievement.name}</h2>
                    <span class="modal-achievement-rarity rarity-${achievement.rarity || 'common'}">${this.getRarityText(achievement.rarity)}</span>
                </div>
                <div class="modal-achievement-body">
                    <p class="modal-achievement-description">${achievement.description}</p>
                    <div class="modal-achievement-details">
                        <div class="detail-item">
                            <div class="detail-value">${achievement.points || 0}</div>
                            <div class="detail-label">成就点数</div>
                        </div>
                        <div class="detail-item">
                            <div class="detail-value">${achievement.completed ? '已解锁' : '未解锁'}</div>
                            <div class="detail-label">状态</div>
                        </div>
                    </div>
                    ${achievement.progress ? `
                        <div class="achievement-progress">
                            <div class="progress-label">
                                <span>完成进度</span>
                                <span>${achievement.progress.current}/${achievement.progress.target}</span>
                            </div>
                            <div class="progress-bar">
                                <div class="progress-fill" style="width: ${Math.min((achievement.progress.current / achievement.progress.target) * 100, 100)}%"></div>
                            </div>
                        </div>
                    ` : ''}
                    ${achievement.completed ? `
                        <p><strong>解锁时间：</strong>${this.formatDate(achievement.completedAt)}</p>
                    ` : ''}
                </div>
            `;
        }

        // 重新绑定关闭事件
        const closeBtn = modal.querySelector('.modal-close');
        if (closeBtn) {
            closeBtn.addEventListener('click', () => this.hideAchievementDetail());
        }

        modal.classList.add('show');
    }

    hideAchievementDetail() {
        const modal = document.getElementById('achievementModal');
        if (modal) {
            modal.classList.remove('show');
        }
    }

    formatDate(timestamp) {
        if (!timestamp) return '';
        
        const date = new Date(timestamp);
        return date.toLocaleDateString('zh-CN', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    }

    showError(message) {
        const achievementsGrid = document.getElementById('achievementsGrid');
        if (achievementsGrid) {
            achievementsGrid.innerHTML = `
                <div class="empty-achievements">
                    <div class="empty-achievements-icon">❌</div>
                    <h3>加载失败</h3>
                    <p>${message}</p>
                </div>
            `;
        }
    }

    // 解锁成就动画
    unlockAchievement(achievementId) {
        const achievementCard = document.querySelector(`[data-achievement-id="${achievementId}"]`);
        if (achievementCard) {
            achievementCard.classList.add('unlock-animation');
            achievementCard.classList.remove('locked');
            achievementCard.classList.add('unlocked');
            
            setTimeout(() => {
                achievementCard.classList.remove('unlock-animation');
            }, 600);
        }
    }

    // 监听成就解锁事件
    listenForAchievementUnlocks() {
        // 这里可以添加WebSocket监听或定期检查
        // 当有新成就解锁时调用 unlockAchievement 方法
    }
}

// 初始化成就管理器
let achievementsManager;
document.addEventListener('DOMContentLoaded', () => {
    achievementsManager = new AchievementsManager();
});
