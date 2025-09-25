/**
 * 修复成就系统 - 同步用户统计数据并重新检查成就
 */

// 同步当前用户的统计数据
async function syncCurrentUserStats() {
    try {
        console.log('🔄 开始同步用户统计数据...');
        
        const response = await fetch('/api/user-stats/sync', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            }
        });

        const result = await response.json();
        
        if (result.success) {
            console.log('✅ 统计数据同步成功:', result.message);
            
            // 显示成功通知
            if (typeof showToast === 'function') {
                showToast('统计数据同步完成，成就已重新检查！', 'success');
            } else {
                alert('统计数据同步完成，成就已重新检查！');
            }
            
            // 刷新页面以显示更新后的成就
            setTimeout(() => {
                window.location.reload();
            }, 2000);
            
        } else {
            console.error('❌ 统计数据同步失败:', result.message);
            
            if (typeof showToast === 'function') {
                showToast('同步失败: ' + result.message, 'error');
            } else {
                alert('同步失败: ' + result.message);
            }
        }
        
    } catch (error) {
        console.error('❌ 同步请求失败:', error);
        
        if (typeof showToast === 'function') {
            showToast('同步请求失败，请稍后重试', 'error');
        } else {
            alert('同步请求失败，请稍后重试');
        }
    }
}

// 检测成就（原有功能）
async function detectAchievements() {
    try {
        console.log('🔍 开始检测成就...');
        
        const response = await fetch('/achievements/api/detect', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            }
        });

        const result = await response.json();
        
        if (result.success) {
            console.log('✅ 成就检测完成:', result);
            
            if (result.count > 0) {
                if (typeof showToast === 'function') {
                    showToast(`恭喜！获得了 ${result.count} 个新成就！`, 'success');
                } else {
                    alert(`恭喜！获得了 ${result.count} 个新成就！`);
                }
                
                // 刷新页面以显示新成就
                setTimeout(() => {
                    window.location.reload();
                }, 2000);
            } else {
                if (typeof showToast === 'function') {
                    showToast('暂无新成就，继续努力吧！', 'info');
                } else {
                    alert('暂无新成就，继续努力吧！');
                }
            }
            
        } else {
            console.error('❌ 成就检测失败:', result.message);
            
            if (typeof showToast === 'function') {
                showToast('检测失败: ' + result.message, 'error');
            } else {
                alert('检测失败: ' + result.message);
            }
        }
        
    } catch (error) {
        console.error('❌ 检测请求失败:', error);
        
        if (typeof showToast === 'function') {
            showToast('检测请求失败，请稍后重试', 'error');
        } else {
            alert('检测请求失败，请稍后重试');
        }
    }
}

// 一键修复成就问题
async function fixAchievements() {
    try {
        console.log('🔧 开始修复成就问题...');
        
        // 先同步统计数据
        await syncCurrentUserStats();
        
        // 等待一秒后检测成就
        setTimeout(async () => {
            await detectAchievements();
        }, 1000);
        
    } catch (error) {
        console.error('❌ 修复成就失败:', error);
    }
}

// 页面加载完成后自动添加修复按钮
document.addEventListener('DOMContentLoaded', function() {
    // 查找成就页面的按钮容器
    const achievementButtons = document.querySelector('.achievement-buttons');
    
    if (achievementButtons) {
        // 创建修复按钮
        const fixButton = document.createElement('button');
        fixButton.className = 'btn btn-warning me-2';
        fixButton.innerHTML = '<i class="fas fa-wrench"></i> 修复成就';
        fixButton.onclick = fixAchievements;
        
        // 创建同步按钮
        const syncButton = document.createElement('button');
        syncButton.className = 'btn btn-info me-2';
        syncButton.innerHTML = '<i class="fas fa-sync"></i> 同步数据';
        syncButton.onclick = syncCurrentUserStats;
        
        // 插入到现有按钮前面
        achievementButtons.insertBefore(fixButton, achievementButtons.firstChild);
        achievementButtons.insertBefore(syncButton, achievementButtons.firstChild);
    }
    
    // 如果在个人资料页面，也添加修复按钮
    const profileActions = document.querySelector('.profile-actions');
    if (profileActions) {
        const fixButton = document.createElement('button');
        fixButton.className = 'btn btn-warning btn-sm';
        fixButton.innerHTML = '<i class="fas fa-wrench"></i> 修复成就';
        fixButton.onclick = fixAchievements;
        
        profileActions.appendChild(fixButton);
    }
});

// 导出函数供全局使用
window.syncCurrentUserStats = syncCurrentUserStats;
window.detectAchievements = detectAchievements;
window.fixAchievements = fixAchievements;
