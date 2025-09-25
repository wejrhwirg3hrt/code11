// 用户个人资料页面JavaScript功能

// 关注/取消关注功能
function toggleFollow(button) {
    console.log('关注按钮被点击');
    const userId = button.getAttribute('data-user-id');
    console.log('目标用户ID:', userId);

    // 显示加载状态
    const originalText = button.innerHTML;
    button.disabled = true;
    button.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>处理中...';

    fetch(`/api/follow/user/${userId}/toggle`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        },
        credentials: 'same-origin'
    })
    .then(response => {
        console.log('响应状态:', response.status);
        return response.json();
    })
    .then(data => {
        console.log('服务器响应:', data);
        if (data.success) {
            // 重新加载页面以获取最新的关注状态（包括互相关注状态）
            window.location.reload();
        } else {
            alert(data.message || '操作失败');
        }
    })
    .catch(error => {
        console.error('关注操作失败:', error);
        alert('操作失败，请稍后重试');

        // 恢复按钮状态
        button.disabled = false;
        button.innerHTML = originalText;
    });
}

// 发送私信功能
function sendPrivateMessage(button) {
    const userId = button.getAttribute('data-user-id');
    const username = button.getAttribute('data-username');

    console.log('私信按钮被点击');
    console.log('用户ID:', userId, '用户名:', username);

    if (!userId || !username) {
        alert('用户信息获取失败');
        return;
    }

    // 跳转到私信页面
    const url = `/messages?user=${userId}&username=${username}`;
    console.log('跳转URL:', url);
    window.location.href = url;
}

// 页面加载完成后的初始化
document.addEventListener('DOMContentLoaded', function() {
    console.log('User profile page loaded');
    
    // 可以在这里添加其他初始化代码
    // 比如工具提示、动画效果等
    
    // 初始化Bootstrap工具提示（如果需要）
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
});
