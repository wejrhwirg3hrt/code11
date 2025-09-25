class VoiceCommandSystem {
    constructor(smartPet) {
        this.smartPet = smartPet;
        this.isListening = false;
        this.isActivated = false; // 是否已激活
        this.isWaitingForCommand = false; // 是否在等待指令
        this.recognition = null;
        this.musicPlayer = null;
        this.currentPlaylist = [];
        this.currentTrackIndex = 0;
        this.isPlaying = false;
        this.activationKeywords = ['小智', '智能助手', '语音助手']; // 激活关键词
        this.commandTimeout = null; // 指令超时定时器

        this.initVoiceRecognition();
        this.initMusicPlayer();
        this.loadMusicLibrary();
        this.checkActivationStatus();
    }

    initVoiceRecognition() {
        if ('webkitSpeechRecognition' in window || 'SpeechRecognition' in window) {
            const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
            this.recognition = new SpeechRecognition();
            this.recognition.continuous = true;
            this.recognition.interimResults = false;
            this.recognition.lang = 'zh-CN';

            this.recognition.onresult = (event) => {
                const lastResult = event.results[event.results.length - 1];
                if (lastResult.isFinal) {
                    const command = lastResult[0].transcript.trim();
                    this.handleVoiceInput(command);
                }
            };

            this.recognition.onerror = (event) => {
                console.error('语音识别错误:', event.error);
                if (event.error === 'no-speech') {
                    // 重新开始监听
                    setTimeout(() => this.startListening(), 1000);
                }
            };

            this.recognition.onend = () => {
                if (this.isListening) {
                    // 自动重新开始监听
                    setTimeout(() => this.recognition.start(), 100);
                }
            };
        }
    }

    // 检查激活状态
    checkActivationStatus() {
        const activated = localStorage.getItem('voiceAssistantActivated');
        this.isActivated = activated === 'true';

        if (!this.isActivated) {
            this.showActivationPrompt();
        } else {
            this.startPassiveListening();
        }
    }

    // 显示激活提示（静默模式）
    showActivationPrompt() {
        // 不自动发声，只在控制台显示提示
        console.log('智能语音助手小智已准备就绪，请说"激活小智"来首次激活。');
        this.startPassiveListening(); // 开始被动监听激活指令
    }

    // 激活语音助手
    activateAssistant() {
        this.isActivated = true;
        localStorage.setItem('voiceAssistantActivated', 'true');
        this.smartPet.speak('语音助手已激活！现在你可以说"小智"来唤醒我。');
        this.startPassiveListening();
    }

    // 处理语音输入
    handleVoiceInput(command) {
        console.log('收到语音输入:', command);

        // 如果未激活，检查是否是激活指令
        if (!this.isActivated) {
            if (command.includes('激活小智') || command.includes('激活语音助手')) {
                this.activateAssistant();
                return;
            }
            return; // 未激活时忽略其他指令
        }

        // 如果正在等待指令，直接处理
        if (this.isWaitingForCommand) {
            this.processVoiceCommand(command);
            this.stopWaitingForCommand();
            return;
        }

        // 检查是否包含激活关键词
        const hasActivationKeyword = this.activationKeywords.some(keyword =>
            command.includes(keyword)
        );

        if (hasActivationKeyword) {
            this.startWaitingForCommand();
        }
    }

    // 开始等待指令
    startWaitingForCommand() {
        this.isWaitingForCommand = true;
        this.smartPet.speak('我在听，请说出你的指令。');

        // 设置超时，10秒后自动停止等待
        this.commandTimeout = setTimeout(() => {
            this.stopWaitingForCommand();
            this.smartPet.speak('没有收到指令，我继续待机。');
        }, 10000);
    }

    // 停止等待指令
    stopWaitingForCommand() {
        this.isWaitingForCommand = false;
        if (this.commandTimeout) {
            clearTimeout(this.commandTimeout);
            this.commandTimeout = null;
        }
    }

    // 开始被动监听
    startPassiveListening() {
        if (!this.recognition) return;

        try {
            this.isListening = true;
            this.recognition.start();
            console.log('开始被动语音监听...');
        } catch (error) {
            console.error('启动语音识别失败:', error);
        }
    }

    startListening() {
        if (!this.recognition) return;
        
        this.isListening = true;
        this.recognition.start();
        this.smartPet.addMessage('🎤 我在听您说话...', 'bot');
        this.smartPet.showExpression('thinking');
        
        // 显示监听状态
        this.showListeningIndicator();
    }

    stopListening() {
        this.isListening = false;
        if (this.recognition) {
            this.recognition.stop();
        }
        this.hideListeningIndicator();
    }

    showListeningIndicator() {
        const indicator = document.createElement('div');
        indicator.id = 'listeningIndicator';
        indicator.className = 'listening-indicator';
        indicator.innerHTML = '🎤 监听中...';
        document.body.appendChild(indicator);
    }

    hideListeningIndicator() {
        const indicator = document.getElementById('listeningIndicator');
        if (indicator) {
            indicator.remove();
        }
    }

    processVoiceCommand(command) {
        console.log('收到语音命令:', command);

        const lowerCommand = command.toLowerCase();
        let commandProcessed = false;

        // 音乐控制命令
        if (lowerCommand.includes('播放音乐') || lowerCommand.includes('放音乐')) {
            this.playMusic();
            commandProcessed = true;
        } else if (lowerCommand.includes('暂停音乐') || lowerCommand.includes('停止音乐')) {
            this.pauseMusic();
            commandProcessed = true;
        } else if (lowerCommand.includes('下一首')) {
            this.nextTrack();
            commandProcessed = true;
        } else if (lowerCommand.includes('上一首')) {
            this.previousTrack();
            commandProcessed = true;
        } else if (lowerCommand.includes('播放') && (lowerCommand.includes('歌') || lowerCommand.includes('音乐'))) {
            const songName = this.extractSongName(command);
            this.playSpecificSong(songName);
            commandProcessed = true;
        }
        // 桌宠控制命令
        else if (lowerCommand.includes('换皮肤') || lowerCommand.includes('换装')) {
            this.smartPet.toggleThemeSelector();
            this.smartPet.speak('好的，请选择您喜欢的皮肤！');
            commandProcessed = true;
        } else if (lowerCommand.includes('最小化')) {
            this.smartPet.toggleMinimize();
            this.smartPet.speak('我缩小了，需要时点击我就好！');
            commandProcessed = true;
        } else if (lowerCommand.includes('关闭') || lowerCommand.includes('再见')) {
            this.smartPet.speak('再见主人，下次见！');
            setTimeout(() => this.smartPet.closePet(), 2000);
            commandProcessed = true;
        }
        // 网站功能命令
        else if (lowerCommand.includes('搜索')) {
            const searchTerm = this.extractSearchTerm(command);
            this.performSearch(searchTerm);
            commandProcessed = true;
        } else if (lowerCommand.includes('上传视频')) {
            window.location.href = '/upload';
            this.smartPet.speak('正在为您打开上传页面！');
            commandProcessed = true;
        } else if (lowerCommand.includes('个人中心') || lowerCommand.includes('我的主页')) {
            window.location.href = '/profile';
            this.smartPet.speak('正在为您打开个人中心！');
            commandProcessed = true;
        } else if (lowerCommand.includes('重置激活') || lowerCommand.includes('重新激活')) {
            this.resetActivation();
            commandProcessed = true;
        }
        // 默认AI对话
        else {
            this.smartPet.processMessage(command);
            commandProcessed = true;
        }

        // 如果指令被处理，记录日志
        if (commandProcessed) {
            this.logVoiceCommand(command);
        }
    }

    // 重置激活状态
    resetActivation() {
        this.isActivated = false;
        localStorage.removeItem('voiceAssistantActivated');
        this.smartPet.speak('语音助手已重置，请重新激活。');
        this.showActivationPrompt();
    }

    // 记录语音指令日志
    logVoiceCommand(command) {
        fetch('/api/logs/frontend-error', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                message: `Voice command executed: ${command}`,
                url: window.location.href,
                userAgent: navigator.userAgent
            })
        }).catch(error => {
            console.error('Failed to log voice command:', error);
        });
    }

    initMusicPlayer() {
        this.musicPlayer = document.createElement('audio');
        this.musicPlayer.addEventListener('ended', () => {
            this.nextTrack();
        });
        
        this.musicPlayer.addEventListener('loadstart', () => {
            this.smartPet.showExpression('excited');
        });
        
        this.musicPlayer.addEventListener('play', () => {
            this.isPlaying = true;
            this.smartPet.createParticles(5);
        });
        
        this.musicPlayer.addEventListener('pause', () => {
            this.isPlaying = false;
        });
    }

    async loadMusicLibrary() {
        try {
            const response = await fetch('/api/music/library');
            this.currentPlaylist = await response.json();
        } catch (error) {
            console.error('加载音乐库失败:', error);
            // 使用默认音乐库
            this.currentPlaylist = [
                { title: '轻音乐1', url: '/music/bgm1.mp3', artist: '未知' },
                { title: '轻音乐2', url: '/music/bgm2.mp3', artist: '未知' }
            ];
        }
    }

    playMusic() {
        if (this.currentPlaylist.length === 0) {
            this.smartPet.speak('抱歉，音乐库是空的！');
            return;
        }

        const track = this.currentPlaylist[this.currentTrackIndex];
        this.musicPlayer.src = track.url;
        this.musicPlayer.play();
        
        this.smartPet.speak(`正在播放：${track.title}`);
        this.smartPet.addMessage(`🎵 正在播放：${track.title} - ${track.artist}`, 'bot');
        this.smartPet.showExpression('excited');
    }

    pauseMusic() {
        this.musicPlayer.pause();
        this.smartPet.speak('音乐已暂停');
        this.smartPet.addMessage('⏸️ 音乐已暂停', 'bot');
    }

    nextTrack() {
        this.currentTrackIndex = (this.currentTrackIndex + 1) % this.currentPlaylist.length;
        this.playMusic();
    }

    previousTrack() {
        this.currentTrackIndex = this.currentTrackIndex === 0 ? 
            this.currentPlaylist.length - 1 : this.currentTrackIndex - 1;
        this.playMusic();
    }

    playSpecificSong(songName) {
        const track = this.currentPlaylist.find(t => 
            t.title.toLowerCase().includes(songName.toLowerCase())
        );
        
        if (track) {
            this.currentTrackIndex = this.currentPlaylist.indexOf(track);
            this.playMusic();
        } else {
            this.smartPet.speak(`没有找到歌曲：${songName}`);
        }
    }

    extractSongName(command) {
        const match = command.match(/播放(.+)/);
        return match ? match[1].trim() : '';
    }

    extractSearchTerm(command) {
        const match = command.match(/搜索(.+)/);
        return match ? match[1].trim() : '';
    }

    performSearch(searchTerm) {
        if (searchTerm) {
            const searchInput = document.querySelector('input[name="keyword"]');
            if (searchInput) {
                searchInput.value = searchTerm;
                searchInput.form.submit();
            }
            this.smartPet.speak(`正在搜索：${searchTerm}`);
        }
    }
}