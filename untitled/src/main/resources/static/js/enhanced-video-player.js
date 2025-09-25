/**
 * 增强视频播放器
 * 支持自定义控件、弹幕、画质切换、倍速播放等
 */
class EnhancedVideoPlayer {
    constructor(container, options = {}) {
        this.container = container;
        this.options = {
            autoplay: false,
            controls: true,
            danmaku: true,
            qualitySwitch: true,
            speedControl: true,
            fullscreen: true,
            ...options
        };
        
        this.video = null;
        this.danmakuContainer = null;
        this.danmakuList = [];
        this.currentQuality = 'auto';
        this.currentSpeed = 1.0;
        
        this.init();
    }

    init() {
        this.createPlayer();
        this.createControls();
        this.bindEvents();
        this.loadDanmaku();
    }

    createPlayer() {
        const playerHTML = `
            <div class="enhanced-video-player">
                <div class="video-container">
                    <video class="video-element" ${this.options.controls ? 'controls' : ''}>
                        <source src="${this.options.src}" type="video/mp4">
                        您的浏览器不支持视频播放。
                    </video>
                    ${this.options.danmaku ? '<div class="danmaku-container"></div>' : ''}
                </div>
                
                <div class="custom-controls">
                    <div class="progress-container">
                        <div class="progress-bar">
                            <div class="progress-filled"></div>
                            <div class="progress-handle"></div>
                        </div>
                        <div class="time-display">
                            <span class="current-time">0:00</span> / <span class="total-time">0:00</span>
                        </div>
                    </div>
                    
                    <div class="control-buttons">
                        <button class="play-pause-btn">
                            <i class="fas fa-play"></i>
                        </button>
                        
                        <div class="volume-control">
                            <button class="volume-btn">
                                <i class="fas fa-volume-up"></i>
                            </button>
                            <div class="volume-slider">
                                <input type="range" min="0" max="100" value="100" class="volume-range">
                            </div>
                        </div>
                        
                        ${this.options.speedControl ? `
                        <div class="speed-control">
                            <button class="speed-btn">1.0x</button>
                            <div class="speed-menu">
                                <div class="speed-option" data-speed="0.5">0.5x</div>
                                <div class="speed-option" data-speed="0.75">0.75x</div>
                                <div class="speed-option active" data-speed="1.0">1.0x</div>
                                <div class="speed-option" data-speed="1.25">1.25x</div>
                                <div class="speed-option" data-speed="1.5">1.5x</div>
                                <div class="speed-option" data-speed="2.0">2.0x</div>
                            </div>
                        </div>
                        ` : ''}
                        
                        ${this.options.qualitySwitch ? `
                        <div class="quality-control">
                            <button class="quality-btn">自动</button>
                            <div class="quality-menu">
                                <div class="quality-option active" data-quality="auto">自动</div>
                                <div class="quality-option" data-quality="1080p">1080P</div>
                                <div class="quality-option" data-quality="720p">720P</div>
                                <div class="quality-option" data-quality="480p">480P</div>
                                <div class="quality-option" data-quality="360p">360P</div>
                            </div>
                        </div>
                        ` : ''}
                        
                        ${this.options.danmaku ? `
                        <button class="danmaku-toggle-btn active">
                            <i class="fas fa-comment"></i>
                        </button>
                        ` : ''}
                        
                        ${this.options.fullscreen ? `
                        <button class="fullscreen-btn">
                            <i class="fas fa-expand"></i>
                        </button>
                        ` : ''}
                    </div>
                </div>
                
                ${this.options.danmaku ? `
                <div class="danmaku-input">
                    <input type="text" placeholder="发个弹幕见证当下" maxlength="100">
                    <button class="send-danmaku-btn">发送</button>
                </div>
                ` : ''}
            </div>
        `;

        this.container.innerHTML = playerHTML;
        this.video = this.container.querySelector('.video-element');
        this.danmakuContainer = this.container.querySelector('.danmaku-container');
    }

    createControls() {
        // 添加CSS样式
        const style = document.createElement('style');
        style.textContent = `
            .enhanced-video-player {
                position: relative;
                background: #000;
                border-radius: 8px;
                overflow: hidden;
                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            }

            .video-container {
                position: relative;
                width: 100%;
                height: 0;
                padding-bottom: 56.25%; /* 16:9 */
            }

            .video-element {
                position: absolute;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                object-fit: contain;
            }

            .danmaku-container {
                position: absolute;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                pointer-events: none;
                overflow: hidden;
            }

            .danmaku-item {
                position: absolute;
                color: white;
                font-size: 16px;
                font-weight: bold;
                text-shadow: 1px 1px 2px rgba(0,0,0,0.8);
                white-space: nowrap;
                animation: danmaku-move 10s linear;
                z-index: 10;
            }

            @keyframes danmaku-move {
                from { transform: translateX(100vw); }
                to { transform: translateX(-100%); }
            }

            .custom-controls {
                position: absolute;
                bottom: 0;
                left: 0;
                right: 0;
                background: linear-gradient(transparent, rgba(0,0,0,0.8));
                padding: 20px 15px 15px;
                opacity: 0;
                transition: opacity 0.3s ease;
            }

            .enhanced-video-player:hover .custom-controls {
                opacity: 1;
            }

            .progress-container {
                margin-bottom: 10px;
            }

            .progress-bar {
                position: relative;
                height: 4px;
                background: rgba(255,255,255,0.3);
                border-radius: 2px;
                cursor: pointer;
            }

            .progress-filled {
                height: 100%;
                background: #ff6b6b;
                border-radius: 2px;
                width: 0%;
                transition: width 0.1s ease;
            }

            .progress-handle {
                position: absolute;
                top: -6px;
                width: 16px;
                height: 16px;
                background: #ff6b6b;
                border-radius: 50%;
                left: 0%;
                transform: translateX(-50%);
                opacity: 0;
                transition: opacity 0.3s ease;
            }

            .progress-bar:hover .progress-handle {
                opacity: 1;
            }

            .time-display {
                color: white;
                font-size: 12px;
                margin-top: 5px;
            }

            .control-buttons {
                display: flex;
                align-items: center;
                gap: 15px;
            }

            .control-buttons button {
                background: none;
                border: none;
                color: white;
                font-size: 16px;
                cursor: pointer;
                padding: 8px;
                border-radius: 4px;
                transition: background 0.3s ease;
            }

            .control-buttons button:hover {
                background: rgba(255,255,255,0.2);
            }

            .volume-control, .speed-control, .quality-control {
                position: relative;
                display: flex;
                align-items: center;
                gap: 10px;
            }

            .volume-slider {
                width: 80px;
                opacity: 0;
                transition: opacity 0.3s ease;
            }

            .volume-control:hover .volume-slider {
                opacity: 1;
            }

            .volume-range {
                width: 100%;
                height: 4px;
                background: rgba(255,255,255,0.3);
                outline: none;
                border-radius: 2px;
            }

            .speed-menu, .quality-menu {
                position: absolute;
                bottom: 100%;
                left: 0;
                background: rgba(0,0,0,0.9);
                border-radius: 4px;
                padding: 8px 0;
                min-width: 80px;
                opacity: 0;
                visibility: hidden;
                transition: all 0.3s ease;
                transform: translateY(10px);
            }

            .speed-control:hover .speed-menu,
            .quality-control:hover .quality-menu {
                opacity: 1;
                visibility: visible;
                transform: translateY(0);
            }

            .speed-option, .quality-option {
                padding: 8px 16px;
                color: white;
                cursor: pointer;
                font-size: 14px;
                transition: background 0.3s ease;
            }

            .speed-option:hover, .quality-option:hover {
                background: rgba(255,255,255,0.2);
            }

            .speed-option.active, .quality-option.active {
                background: #ff6b6b;
            }

            .danmaku-input {
                position: absolute;
                bottom: 80px;
                left: 15px;
                right: 15px;
                display: flex;
                gap: 10px;
                opacity: 0;
                transition: opacity 0.3s ease;
            }

            .enhanced-video-player:hover .danmaku-input {
                opacity: 1;
            }

            .danmaku-input input {
                flex: 1;
                padding: 8px 12px;
                border: none;
                border-radius: 20px;
                background: rgba(0,0,0,0.7);
                color: white;
                outline: none;
            }

            .danmaku-input input::placeholder {
                color: rgba(255,255,255,0.6);
            }

            .send-danmaku-btn {
                padding: 8px 16px;
                background: #ff6b6b;
                color: white;
                border: none;
                border-radius: 20px;
                cursor: pointer;
                font-size: 14px;
                transition: background 0.3s ease;
            }

            .send-danmaku-btn:hover {
                background: #ff5252;
            }

            .danmaku-toggle-btn.active {
                background: #ff6b6b !important;
            }
        `;
        
        document.head.appendChild(style);
    }

    bindEvents() {
        // 播放/暂停
        const playPauseBtn = this.container.querySelector('.play-pause-btn');
        playPauseBtn.addEventListener('click', () => this.togglePlay());

        // 进度条
        const progressBar = this.container.querySelector('.progress-bar');
        progressBar.addEventListener('click', (e) => this.seekTo(e));

        // 音量控制
        const volumeRange = this.container.querySelector('.volume-range');
        if (volumeRange) {
            volumeRange.addEventListener('input', (e) => this.setVolume(e.target.value / 100));
        }

        // 倍速控制
        const speedOptions = this.container.querySelectorAll('.speed-option');
        speedOptions.forEach(option => {
            option.addEventListener('click', () => this.setSpeed(parseFloat(option.dataset.speed)));
        });

        // 画质控制
        const qualityOptions = this.container.querySelectorAll('.quality-option');
        qualityOptions.forEach(option => {
            option.addEventListener('click', () => this.setQuality(option.dataset.quality));
        });

        // 弹幕开关
        const danmakuToggle = this.container.querySelector('.danmaku-toggle-btn');
        if (danmakuToggle) {
            danmakuToggle.addEventListener('click', () => this.toggleDanmaku());
        }

        // 发送弹幕
        const danmakuInput = this.container.querySelector('.danmaku-input input');
        const sendBtn = this.container.querySelector('.send-danmaku-btn');
        if (danmakuInput && sendBtn) {
            sendBtn.addEventListener('click', () => this.sendDanmaku());
            danmakuInput.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') this.sendDanmaku();
            });
        }

        // 全屏
        const fullscreenBtn = this.container.querySelector('.fullscreen-btn');
        if (fullscreenBtn) {
            fullscreenBtn.addEventListener('click', () => this.toggleFullscreen());
        }

        // 视频事件
        this.video.addEventListener('timeupdate', () => this.updateProgress());
        this.video.addEventListener('loadedmetadata', () => this.updateDuration());
        this.video.addEventListener('play', () => this.updatePlayButton());
        this.video.addEventListener('pause', () => this.updatePlayButton());
    }

    togglePlay() {
        if (this.video.paused) {
            this.video.play();
        } else {
            this.video.pause();
        }
    }

    updatePlayButton() {
        const icon = this.container.querySelector('.play-pause-btn i');
        icon.className = this.video.paused ? 'fas fa-play' : 'fas fa-pause';
    }

    seekTo(e) {
        const rect = e.target.getBoundingClientRect();
        const percent = (e.clientX - rect.left) / rect.width;
        this.video.currentTime = percent * this.video.duration;
    }

    updateProgress() {
        const percent = (this.video.currentTime / this.video.duration) * 100;
        const progressFilled = this.container.querySelector('.progress-filled');
        const progressHandle = this.container.querySelector('.progress-handle');
        const currentTime = this.container.querySelector('.current-time');
        
        progressFilled.style.width = percent + '%';
        progressHandle.style.left = percent + '%';
        currentTime.textContent = this.formatTime(this.video.currentTime);
    }

    updateDuration() {
        const totalTime = this.container.querySelector('.total-time');
        totalTime.textContent = this.formatTime(this.video.duration);
    }

    formatTime(seconds) {
        const mins = Math.floor(seconds / 60);
        const secs = Math.floor(seconds % 60);
        return `${mins}:${secs.toString().padStart(2, '0')}`;
    }

    setVolume(volume) {
        this.video.volume = volume;
        const volumeIcon = this.container.querySelector('.volume-btn i');
        if (volume === 0) {
            volumeIcon.className = 'fas fa-volume-mute';
        } else if (volume < 0.5) {
            volumeIcon.className = 'fas fa-volume-down';
        } else {
            volumeIcon.className = 'fas fa-volume-up';
        }
    }

    setSpeed(speed) {
        this.currentSpeed = speed;
        this.video.playbackRate = speed;
        
        // 更新按钮文本
        const speedBtn = this.container.querySelector('.speed-btn');
        speedBtn.textContent = speed + 'x';
        
        // 更新选中状态
        this.container.querySelectorAll('.speed-option').forEach(option => {
            option.classList.toggle('active', parseFloat(option.dataset.speed) === speed);
        });
    }

    setQuality(quality) {
        this.currentQuality = quality;
        
        // 更新按钮文本
        const qualityBtn = this.container.querySelector('.quality-btn');
        qualityBtn.textContent = quality === 'auto' ? '自动' : quality;
        
        // 更新选中状态
        this.container.querySelectorAll('.quality-option').forEach(option => {
            option.classList.toggle('active', option.dataset.quality === quality);
        });
        
        // 这里可以实现实际的画质切换逻辑
        console.log('切换画质到:', quality);
    }

    toggleDanmaku() {
        const btn = this.container.querySelector('.danmaku-toggle-btn');
        const isActive = btn.classList.toggle('active');
        
        if (this.danmakuContainer) {
            this.danmakuContainer.style.display = isActive ? 'block' : 'none';
        }
    }

    sendDanmaku() {
        const input = this.container.querySelector('.danmaku-input input');
        const text = input.value.trim();
        
        if (text) {
            this.addDanmaku(text, this.video.currentTime);
            input.value = '';
            
            // 发送到服务器
            this.sendDanmakuToServer(text, this.video.currentTime);
        }
    }

    addDanmaku(text, time) {
        if (!this.danmakuContainer) return;
        
        const danmaku = document.createElement('div');
        danmaku.className = 'danmaku-item';
        danmaku.textContent = text;
        danmaku.style.top = Math.random() * 80 + '%';
        danmaku.style.color = this.getRandomColor();
        
        this.danmakuContainer.appendChild(danmaku);
        
        // 10秒后移除
        setTimeout(() => {
            if (danmaku.parentNode) {
                danmaku.parentNode.removeChild(danmaku);
            }
        }, 10000);
    }

    getRandomColor() {
        const colors = ['#ffffff', '#ff6b6b', '#4ecdc4', '#45b7d1', '#96ceb4', '#feca57', '#ff9ff3', '#54a0ff'];
        return colors[Math.floor(Math.random() * colors.length)];
    }

    sendDanmakuToServer(text, time) {
        // 优先使用WebSocket发送弹幕
        if (typeof realtimeFeatures !== 'undefined' && realtimeFeatures.connected) {
            realtimeFeatures.sendDanmaku(text, time);
        } else {
            // 备用REST API发送弹幕
            fetch('/api/danmaku/send', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    videoId: this.options.videoId,
                    text: text,
                    time: time
                })
            }).catch(error => console.error('发送弹幕失败:', error));
        }
    }

    loadDanmaku() {
        // 从服务器加载弹幕
        if (this.options.videoId) {
            fetch(`/api/danmaku/${this.options.videoId}`)
                .then(response => response.json())
                .then(data => {
                    this.danmakuList = data;
                })
                .catch(error => console.error('加载弹幕失败:', error));
        }
    }

    toggleFullscreen() {
        if (!document.fullscreenElement) {
            this.container.requestFullscreen();
        } else {
            document.exitFullscreen();
        }
    }
}

// 全局暴露
window.EnhancedVideoPlayer = EnhancedVideoPlayer;
