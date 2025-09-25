/**
 * 通话界面控制JavaScript
 */

// 通话相关变量
let currentCallData = null;
let callTimer = null;
let callStartTime = null;

// WebRTC客户端实例将在页面加载完成后创建
// 这样可以确保所有依赖都已加载

// 更新通话状态
function updateCallStatus(status) {
    const callStatus = document.getElementById('callStatus');
    const connectionStatus = document.getElementById('connectionStatus');
    
    let statusText = '';
    let connectionClass = '';
    
    switch (status) {
        case 'calling':
            statusText = '呼叫中...';
            connectionClass = 'connecting';
            break;
        case 'ringing':
            statusText = '响铃中...';
            connectionClass = 'connecting';
            break;
        case 'connecting':
            statusText = '连接中...';
            connectionClass = 'connecting';
            break;
        case 'connected':
            statusText = '通话中';
            connectionClass = 'connected';
            break;
        case 'ended':
            statusText = '通话结束';
            connectionClass = 'failed';
            break;
    }
    
    if (callStatus) callStatus.textContent = statusText;
    if (connectionStatus) {
        connectionStatus.textContent = statusText;
        connectionStatus.className = 'connection-status ' + connectionClass;
    }
}

// 开始通话计时
function startCallTimer() {
    callStartTime = Date.now();
    callTimer = setInterval(updateCallDuration, 1000);
}

// 停止通话计时
function stopCallTimer() {
    if (callTimer) {
        clearInterval(callTimer);
        callTimer = null;
    }
    callStartTime = null;
}

// 更新通话时长
function updateCallDuration() {
    if (!callStartTime) return;
    
    const duration = Math.floor((Date.now() - callStartTime) / 1000);
    const minutes = Math.floor(duration / 60);
    const seconds = duration % 60;
    
    const durationText = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
    const callDuration = document.getElementById('callDuration');
    if (callDuration) {
        callDuration.textContent = durationText;
    }
}

// 处理通话被接受
function handleCallAccepted(data) {
    updateCallStatus('connecting');
    startCallTimer();
}

// 处理通话被拒绝
function handleCallRejected(data) {
    alert('对方拒绝了通话');
    hideCallInterface();
}

// 处理通话结束
function handleCallEnded(data) {
    hideCallInterface();
}

// 处理远程媒体流
function handleRemoteStream(stream) {
    const remoteVideo = document.getElementById('remoteVideo');
    const localVideo = document.getElementById('localVideo');
    
    if (remoteVideo) {
        remoteVideo.srcObject = stream;
    }
    
    // 设置本地视频流
    if (localVideo && window.webrtcClient.localStream) {
        localVideo.srcObject = window.webrtcClient.localStream;
    }
    
    updateCallStatus('connected');
}

// 处理连接状态变化
function handleConnectionStateChange(state) {
    console.log('连接状态变化:', state);
    
    switch (state) {
        case 'connected':
            updateCallStatus('connected');
            break;
        case 'disconnected':
        case 'failed':
            updateCallStatus('ended');
            setTimeout(hideCallInterface, 2000);
            break;
    }
}

// 获取用户信息（从聊天应用中）
function getCurrentChatUser() {
    if (typeof ChatApp !== 'undefined' && ChatApp.targetUser) {
        return ChatApp.targetUser;
    }
    return null;
}

// 播放通话铃声
function playRingtone() {
    // 这里可以添加铃声播放逻辑
    console.log('播放铃声');
}

// 停止通话铃声
function stopRingtone() {
    // 这里可以添加停止铃声逻辑
    console.log('停止铃声');
}

// 检查浏览器兼容性
function checkWebRTCSupport() {
    if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
        alert('您的浏览器不支持WebRTC功能，无法进行音视频通话');
        return false;
    }
    
    if (!window.RTCPeerConnection) {
        alert('您的浏览器不支持WebRTC连接，无法进行音视频通话');
        return false;
    }
    
    return true;
}

// 请求媒体权限
async function requestMediaPermissions(video = false) {
    try {
        const constraints = {
            audio: true,
            video: video
        };
        
        const stream = await navigator.mediaDevices.getUserMedia(constraints);
        // 立即停止流，只是为了获取权限
        stream.getTracks().forEach(track => track.stop());
        return true;
    } catch (error) {
        console.error('获取媒体权限失败:', error);
        
        if (error.name === 'NotAllowedError') {
            alert('请允许访问摄像头和麦克风权限以进行通话');
        } else if (error.name === 'NotFoundError') {
            alert('未找到摄像头或麦克风设备');
        } else {
            alert('无法访问摄像头或麦克风：' + error.message);
        }
        
        return false;
    }
}

// 显示权限请求提示
function showPermissionDialog(callType) {
    const message = callType === 'video' 
        ? '视频通话需要访问您的摄像头和麦克风权限，请点击允许。'
        : '语音通话需要访问您的麦克风权限，请点击允许。';
    
    return confirm(message);
}

// 格式化通话时长
function formatCallDuration(seconds) {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const secs = seconds % 60;
    
    if (hours > 0) {
        return `${hours}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    } else {
        return `${minutes}:${secs.toString().padStart(2, '0')}`;
    }
}

// 显示通话错误信息
function showCallError(message) {
    const errorDiv = document.createElement('div');
    errorDiv.className = 'alert alert-danger position-fixed';
    errorDiv.style.cssText = 'top: 20px; right: 20px; z-index: 10001; max-width: 300px;';
    errorDiv.innerHTML = `
        <strong>通话错误</strong><br>
        ${message}
        <button type="button" class="btn-close" onclick="this.parentElement.remove()"></button>
    `;
    
    document.body.appendChild(errorDiv);
    
    // 5秒后自动移除
    setTimeout(() => {
        if (errorDiv.parentElement) {
            errorDiv.remove();
        }
    }, 5000);
}

// 显示通话成功信息
function showCallSuccess(message) {
    const successDiv = document.createElement('div');
    successDiv.className = 'alert alert-success position-fixed';
    successDiv.style.cssText = 'top: 20px; right: 20px; z-index: 10001; max-width: 300px;';
    successDiv.innerHTML = `
        <strong>通话信息</strong><br>
        ${message}
        <button type="button" class="btn-close" onclick="this.parentElement.remove()"></button>
    `;
    
    document.body.appendChild(successDiv);
    
    // 3秒后自动移除
    setTimeout(() => {
        if (successDiv.parentElement) {
            successDiv.remove();
        }
    }, 3000);
}

// 切换静音功能
async function toggleMute() {
    console.log('=== toggleMute 开始 ===');

    const muteBtn = document.getElementById('muteBtn');
    if (!muteBtn) {
        console.error('静音按钮未找到');
        return;
    }

    // 防止重复点击
    if (muteBtn.disabled) {
        console.log('按钮已禁用，忽略点击');
        return;
    }

    muteBtn.disabled = true;

    try {
        // 检查是否在通话中
        const isInCall = window.webrtcClient && window.webrtcClient.callStatus === 'connected';

        if (isInCall) {
            // 在通话中，实际切换音频
            console.log('在通话中，调用 webrtcClient.toggleAudio()...');
            const isEnabled = await window.webrtcClient.toggleAudio();
            console.log('音频状态切换结果:', isEnabled);

            // 更新按钮状态
            if (isEnabled) {
                // 音频开启状态
                muteBtn.classList.remove('active');
                muteBtn.innerHTML = '<i class="fas fa-microphone"></i>';
                muteBtn.title = '静音';
                console.log('UI更新: 音频已开启');
            } else {
                // 音频关闭状态（静音）
                muteBtn.classList.add('active');
                muteBtn.innerHTML = '<i class="fas fa-microphone-slash"></i>';
                muteBtn.title = '取消静音';
                console.log('UI更新: 音频已静音');
            }
        } else {
            // 不在通话中，只更新UI状态
            console.log('不在通话中，只更新UI状态');
            const isCurrentlyMuted = muteBtn.classList.contains('active');

            if (isCurrentlyMuted) {
                // 当前是静音状态，切换为开启
                muteBtn.classList.remove('active');
                muteBtn.innerHTML = '<i class="fas fa-microphone"></i>';
                muteBtn.title = '静音';
                console.log('UI更新: 音频已开启（仅UI）');
            } else {
                // 当前是开启状态，切换为静音
                muteBtn.classList.add('active');
                muteBtn.innerHTML = '<i class="fas fa-microphone-slash"></i>';
                muteBtn.title = '取消静音';
                console.log('UI更新: 音频已静音（仅UI）');
            }
        }
    } catch (error) {
        console.error('切换静音失败:', error);
        // 不显示alert，只在控制台记录错误
    } finally {
        muteBtn.disabled = false;
        console.log('=== toggleMute 结束 ===');
    }
}

// 切换视频功能
async function toggleVideo() {
    console.log('toggleVideo 被调用');

    const videoBtn = document.getElementById('videoBtn');
    if (!videoBtn) {
        console.error('视频按钮未找到');
        return;
    }

    try {
        // 检查是否在通话中
        const isInCall = window.webrtcClient && window.webrtcClient.callStatus === 'connected';

        if (isInCall) {
            // 在通话中，实际切换视频
            const isEnabled = await window.webrtcClient.toggleVideo();
            console.log('视频状态切换结果:', isEnabled);

            // 更新按钮状态
            if (isEnabled) {
                // 视频开启状态
                videoBtn.classList.remove('active');
                videoBtn.innerHTML = '<i class="fas fa-video"></i>';
                videoBtn.title = '关闭摄像头';
                console.log('视频已开启');
            } else {
                // 视频关闭状态
                videoBtn.classList.add('active');
                videoBtn.innerHTML = '<i class="fas fa-video-slash"></i>';
                videoBtn.title = '开启摄像头';
                console.log('视频已关闭');
            }
        } else {
            // 不在通话中，只更新UI状态
            console.log('不在通话中，只更新UI状态');
            const isCurrentlyOff = videoBtn.classList.contains('active');

            if (isCurrentlyOff) {
                // 当前是关闭状态，切换为开启
                videoBtn.classList.remove('active');
                videoBtn.innerHTML = '<i class="fas fa-video"></i>';
                videoBtn.title = '关闭摄像头';
                console.log('视频已开启（仅UI）');
            } else {
                // 当前是开启状态，切换为关闭
                videoBtn.classList.add('active');
                videoBtn.innerHTML = '<i class="fas fa-video-slash"></i>';
                videoBtn.title = '开启摄像头';
                console.log('视频已关闭（仅UI）');
            }
        }
    } catch (error) {
        console.error('切换视频失败:', error);
    }
}

// 挂断通话
function endCall() {
    console.log('endCall 被调用');

    if (window.webrtcClient) {
        window.webrtcClient.endCall();
    }

    hideCallInterface();
}

// 初始化通话界面事件监听
function initializeCallUI() {
    // 监听键盘事件
    document.addEventListener('keydown', function(event) {
        // ESC键挂断通话
        if (event.key === 'Escape' && document.getElementById('callOverlay').classList.contains('show')) {
            endCall();
        }

        // 空格键切换静音
        if (event.code === 'Space' && document.getElementById('callOverlay').classList.contains('show')) {
            event.preventDefault();
            toggleMute();
        }
    });

    // 监听页面关闭事件
    window.addEventListener('beforeunload', function(event) {
        if (window.webrtcClient && window.webrtcClient.callStatus !== 'idle') {
            window.webrtcClient.endCall();
        }
    });
}

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', function() {
    initializeCallUI();
});
