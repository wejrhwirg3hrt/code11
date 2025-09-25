class SmartPet {
    constructor() {
        this.pet = null;
        this.isDragging = false;
        this.isMinimized = false;
        this.isRecording = false;
        this.recognition = null;
        this.synthesis = window.speechSynthesis;
        this.userId = localStorage.getItem('petUserId') || this.generateUserId();
        this.currentTheme = localStorage.getItem('petTheme') || 'cute_girl';
        this.currentX = 0;
        this.currentY = 0;
        this.initialX = 0;
        this.initialY = 0;
        this.xOffset = 0;
        this.yOffset = 0;
        this.dragThreshold = 5; // 拖拽阈值
        this.dragStartTime = 0;
        this.lastExpression = 'happy';
        this.isThinking = false;

        // 保存用户ID到本地存储
        localStorage.setItem('petUserId', this.userId);

        this.init();
    }

    generateUserId() {
        return 'user_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
    }

    init() {
        // 检查是否已有全局智能助手
        if (window.globalSmartPetManager) {
            console.log('全局智能助手已存在，跳过页面级助手初始化');
            return;
        }

        this.createPet();
        this.addEventListeners();
        this.initSpeechRecognition();
        this.applyTheme(this.currentTheme);
        this.greet();
        this.startIdleAnimations();
    }

    createPet() {
        this.pet = document.createElement('div');
        this.pet.className = 'smart-pet';
        this.pet.innerHTML = `
            <div class="pet-container">
                <div class="status-indicator"></div>
                <div class="pet-header">
                    <div class="pet-avatar" id="petAvatar">🤖</div>
                    <div class="pet-controls">
                        <button class="control-btn" onclick="smartPet.toggleThemeSelector()" title="换肤">
                            <i class="fas fa-palette"></i>
                        </button>
                        <button class="control-btn" onclick="smartPet.toggleMinimize()" title="最小化">
                            <i class="fas fa-minus"></i>
                        </button>
                        <button class="control-btn" onclick="smartPet.closePet()" title="关闭">
                            <i class="fas fa-times"></i>
                        </button>
                    </div>
                </div>
                <div class="theme-selector" id="themeSelector">
                    <button class="theme-btn" onclick="smartPet.changeTheme('cute_girl')" title="可爱女孩">👧</button>
                    <button class="theme-btn" onclick="smartPet.changeTheme('anime_girl')" title="动漫少女">🌸</button>
                    <button class="theme-btn" onclick="smartPet.changeTheme('cat_girl')" title="猫娘">🐱</button>
                    <button class="theme-btn" onclick="smartPet.changeTheme('robot')" title="机器人">🤖</button>
                </div>
                <div class="pet-chat">
                    <div class="chat-messages" id="chatMessages"></div>
                    <div class="voice-wave" id="voiceWave">
                        <div class="wave-bar"></div>
                        <div class="wave-bar"></div>
                        <div class="wave-bar"></div>
                        <div class="wave-bar"></div>
                        <div class="wave-bar"></div>
                    </div>
                    <div class="chat-input-area">
                        <input type="text" class="chat-input" id="chatInput" 
                               placeholder="输入消息..." onkeypress="smartPet.handleKeyPress(event)">
                        <button class="voice-btn" id="voiceBtn" onclick="smartPet.toggleVoiceRecording()" title="语音输入">
                            <i class="fas fa-microphone"></i>
                        </button>
                        <button class="send-btn" onclick="smartPet.sendMessage()" title="发送">
                            <i class="fas fa-paper-plane"></i>
                        </button>
                    </div>
                </div>
            </div>
        `;

        document.body.appendChild(this.pet);
    }

    addEventListeners() {
        const header = this.pet.querySelector('.pet-header');

        // 改进的拖拽事件
        header.addEventListener('mousedown', this.dragStart.bind(this));
        document.addEventListener('mousemove', this.dragMove.bind(this));
        document.addEventListener('mouseup', this.dragEnd.bind(this));

        // 触摸事件支持
        header.addEventListener('touchstart', this.touchStart.bind(this));
        document.addEventListener('touchmove', this.touchMove.bind(this));
        document.addEventListener('touchend', this.touchEnd.bind(this));

        // 点击事件（用于最小化状态下的恢复）
        this.pet.addEventListener('click', this.handlePetClick.bind(this));

        // 防止选择文本
        this.pet.addEventListener('selectstart', (e) => e.preventDefault());
    }

    dragStart(e) {
        e.preventDefault();
        this.dragStartTime = Date.now();
        this.initialX = e.clientX - this.xOffset;
        this.initialY = e.clientY - this.yOffset;
        this.isDragging = false; // 先不设为true，等移动超过阈值再设置
        this.pet.style.cursor = 'grabbing';
        this.pet.style.transition = 'none';
    }

    dragMove(e) {
        if (this.dragStartTime === 0) return;

        e.preventDefault();
        const currentX = e.clientX - this.initialX;
        const currentY = e.clientY - this.initialY;

        // 计算移动距离
        const deltaX = Math.abs(currentX - this.currentX);
        const deltaY = Math.abs(currentY - this.currentY);

        // 只有移动超过阈值才开始拖拽
        if (!this.isDragging && (deltaX > this.dragThreshold || deltaY > this.dragThreshold)) {
            this.isDragging = true;
        }

        if (this.isDragging) {
            this.currentX = currentX;
            this.currentY = currentY;
            this.xOffset = this.currentX;
            this.yOffset = this.currentY;
            this.pet.style.transform = `translate(${this.currentX}px, ${this.currentY}px)`;
        }
    }

    dragEnd(e) {
        if (this.dragStartTime === 0) return;

        const dragDuration = Date.now() - this.dragStartTime;

        // 如果是短时间点击且没有拖拽，则不处理（让点击事件处理）
        if (dragDuration < 200 && !this.isDragging) {
            this.dragStartTime = 0;
            this.pet.style.cursor = 'grab';
            this.pet.style.transition = 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)';
            return;
        }

        this.isDragging = false;
        this.dragStartTime = 0;
        this.pet.style.cursor = 'grab';
        this.pet.style.transition = 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)';

        // 边界检查
        this.checkBoundaries();
    }

    touchStart(e) {
        const touch = e.touches[0];
        this.dragStart({
            clientX: touch.clientX,
            clientY: touch.clientY,
            preventDefault: () => e.preventDefault()
        });
    }

    touchMove(e) {
        const touch = e.touches[0];
        this.dragMove({
            clientX: touch.clientX,
            clientY: touch.clientY,
            preventDefault: () => e.preventDefault()
        });
    }

    touchEnd(e) {
        this.dragEnd(e);
    }

    checkBoundaries() {
        const rect = this.pet.getBoundingClientRect();
        const windowWidth = window.innerWidth;
        const windowHeight = window.innerHeight;

        let newX = this.currentX;
        let newY = this.currentY;

        // 确保桌宠不会完全移出屏幕
        if (rect.left < -rect.width * 0.8) {
            newX = -rect.width * 0.8;
        } else if (rect.right > windowWidth + rect.width * 0.8) {
            newX = windowWidth - rect.width * 0.2;
        }

        if (rect.top < 0) {
            newY = -this.currentY + 10;
        } else if (rect.bottom > windowHeight) {
            newY = windowHeight - rect.height - 10;
        }

        if (newX !== this.currentX || newY !== this.currentY) {
            this.currentX = newX;
            this.currentY = newY;
            this.xOffset = newX;
            this.yOffset = newY;
            this.pet.style.transform = `translate(${newX}px, ${newY}px)`;
        }
    }

    handlePetClick(e) {
        // 如果是拖拽操作，不处理点击
        if (this.isDragging) return;

        // 如果点击的是控制按钮，不处理
        if (e.target.closest('.control-btn') || e.target.closest('.theme-selector')) return;

        // 最小化状态下点击恢复
        if (this.isMinimized) {
            this.toggleMinimize();
            return;
        }

        // 正常状态下点击显示表情
        this.showRandomExpression();
    }

    toggleMinimize() {
        this.isMinimized = !this.isMinimized;
        this.pet.classList.toggle('minimized');

        const icon = this.pet.querySelector('.control-btn i');
        if (icon && icon.classList.contains('fa-minus')) {
            icon.className = this.isMinimized ? 'fas fa-plus' : 'fas fa-minus';
        }

        // 最小化时隐藏主题选择器
        if (this.isMinimized) {
            this.hideThemeSelector();
        }
    }

    toggleThemeSelector() {
        const selector = document.getElementById('themeSelector');
        selector.classList.toggle('show');

        // 3秒后自动隐藏
        setTimeout(() => {
            this.hideThemeSelector();
        }, 3000);
    }

    hideThemeSelector() {
        const selector = document.getElementById('themeSelector');
        selector.classList.remove('show');
    }

    changeTheme(themeName) {
        this.currentTheme = themeName;
        localStorage.setItem('petTheme', themeName);
        this.applyTheme(themeName);
        this.hideThemeSelector();

        // 播放换肤特效
        this.playThemeChangeEffect();

        // 语音反馈
        const theme = PetThemes[themeName];
        this.speak(`我换了新装扮，现在我是${theme.name}啦！`);
    }

    applyTheme(themeName) {
        const theme = PetThemes[themeName];
        if (!theme) return;

        const container = this.pet.querySelector('.pet-container');
        const avatar = document.getElementById('petAvatar');

        // 应用主题样式
        container.style.background = theme.background;
        avatar.textContent = theme.avatar;

        // 应用动画
        avatar.style.animation = theme.animations.idle;

        // 保存当前主题配置
        this.currentThemeConfig = theme;
    }

    playThemeChangeEffect() {
        // 创建换肤特效
        const avatar = document.getElementById('petAvatar');
        avatar.style.animation = 'tada 1s ease-in-out';

        // 创建粒子特效
        this.createParticles(10);

        // 恢复默认动画
        setTimeout(() => {
            if (this.currentThemeConfig) {
                avatar.style.animation = this.currentThemeConfig.animations.idle;
            }
        }, 1000);
    }

    createParticles(count) {
        const theme = this.currentThemeConfig;
        if (!theme) return;

        for (let i = 0; i < count; i++) {
            setTimeout(() => {
                const particle = document.createElement('div');
                particle.className = `particle ${theme.particles.shape}`;

                // 随机位置
                const rect = this.pet.getBoundingClientRect();
                particle.style.left = Math.random() * rect.width + 'px';
                particle.style.top = Math.random() * rect.height + 'px';

                if (theme.particles.shape === 'circle') {
                    particle.style.background = theme.particles.color;
                }

                this.pet.appendChild(particle);

                // 3秒后移除
                setTimeout(() => {
                    if (particle.parentNode) {
                        particle.parentNode.removeChild(particle);
                    }
                }, 3000);
            }, i * 100);
        }
    }

    showRandomExpression() {
        const theme = this.currentThemeConfig;
        if (!theme) return;

        const expressions = Object.keys(theme.expressions);
        const randomExpression = expressions[Math.floor(Math.random() * expressions.length)];

        this.showExpression(randomExpression);
    }

    showExpression(expressionType) {
        const theme = this.currentThemeConfig;
        if (!theme || !theme.expressions[expressionType]) return;

        const avatar = document.getElementById('petAvatar');
        const originalContent = avatar.textContent;

        // 显示表情
        avatar.textContent = theme.expressions[expressionType];

        // 播放对应动画
        if (theme.animations[expressionType]) {
            avatar.style.animation = theme.animations[expressionType];
        }

        // 2秒后恢复
        setTimeout(() => {
            avatar.textContent = theme.avatar;
            avatar.style.animation = theme.animations.idle;
        }, 2000);

        this.lastExpression = expressionType;
    }

    startIdleAnimations() {
        // 每30秒随机显示一个表情
        setInterval(() => {
            if (!this.isRecording && !this.isThinking && !this.isMinimized) {
                this.showRandomExpression();
            }
        }, 30000);
    }

    initSpeechRecognition() {
        if ('webkitSpeechRecognition' in window || 'SpeechRecognition' in window) {
            const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
            this.recognition = new SpeechRecognition();
            this.recognition.continuous = false;
            this.recognition.interimResults = false;
            this.recognition.lang = 'zh-CN';

            this.recognition.onresult = (event) => {
                const transcript = event.results[0][0].transcript;
                this.addMessage(transcript, 'user');
                this.processMessage(transcript);
            };

            this.recognition.onerror = (event) => {
                console.error('语音识别错误:', event.error);
                this.stopRecording();
                this.showExpression('sad');
            };

            this.recognition.onend = () => {
                this.stopRecording();
            };
        }
    }

    closePet() {
        // 播放告别动画
        this.pet.style.animation = 'fadeOut 0.5s ease-in-out';
        this.speak('再见！下次见面时记得叫我哦！');

        setTimeout(() => {
            this.pet.style.display = 'none';
        }, 500);
    }

    greet() {
        setTimeout(() => {
            this.loadChatHistory();
            setTimeout(() => {
                const theme = PetThemes[this.currentTheme];
                this.addMessage(`嗨！我是${theme.name}小智，你的AI视频助手！我现在更聪明更可爱了哦！🤖✨`, 'bot');
                this.speak(`嗨！我是${theme.name}小智，你的AI视频助手！`);
                this.showExpression('happy');
            }, 500);
        }, 1000);
    }

    addMessage(text, sender) {
        const messagesContainer = document.getElementById('chatMessages');
        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${sender}`;
        messageDiv.textContent = text;
        messagesContainer.appendChild(messageDiv);
        messagesContainer.scrollTop = messagesContainer.scrollHeight;

        // 如果是机器人消息，显示对应表情
        if (sender === 'bot') {
            if (text.includes('😊') || text.includes('开心') || text.includes('高兴')) {
                this.showExpression('happy');
            } else if (text.includes('🤔') || text.includes('想想') || text.includes('思考')) {
                this.showExpression('thinking');
            } else if (text.includes('❤️') || text.includes('喜欢') || text.includes('爱')) {
                this.showExpression('love');
            }
        }
    }

    handleKeyPress(event) {
        if (event.key === 'Enter') {
            this.sendMessage();
        }
    }

    sendMessage() {
        const input = document.getElementById('chatInput');
        const message = input.value.trim();
        if (message) {
            this.addMessage(message, 'user');
            this.processMessage(message);
            input.value = '';
        }
    }

    async processMessage(message) {
        // 显示思考状态
        this.isThinking = true;
        this.showExpression('thinking');
        this.addMessage('🤔 让我想想...', 'thinking');

        try {
            const response = await fetch('/api/pet/chat', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    message: message,
                    userId: this.userId
                })
            });

            const data = await response.json();

            // 移除思考消息
            const messages = document.getElementById('chatMessages');
            const lastMessage = messages.lastElementChild;
            if (lastMessage && lastMessage.classList.contains('thinking')) {
                messages.removeChild(lastMessage);
            }

            this.isThinking = false;

            if (data.success) {
                this.addMessage(data.response, 'bot');
                this.speak(data.response);
                this.showExpression('happy');

                // 创建回复特效
                this.createParticles(3);
            } else {
                this.addMessage('抱歉，我现在有点忙，请稍后再试！', 'bot');
                this.showExpression('sad');
            }
        } catch (error) {
            console.error('AI聊天错误:', error);

            // 移除思考消息
            const messages = document.getElementById('chatMessages');
            const lastMessage = messages.lastElementChild;
            if (lastMessage && lastMessage.classList.contains('thinking')) {
                messages.removeChild(lastMessage);
            }

            this.isThinking = false;

            // 使用本地回复作为备选
            const localResponse = this.getLocalResponse(message);
            this.addMessage(localResponse, 'bot');
            this.speak(localResponse);
            this.showExpression('thinking');
        }
    }

    getLocalResponse(message) {
        const lowerMessage = message.toLowerCase();

        if (lowerMessage.includes('你好') || lowerMessage.includes('嗨')) {
            return "你好！我是小智，你的专属视频助手！有什么可以帮你的吗？😊";
        } else if (lowerMessage.includes('推荐') || lowerMessage.includes('热门')) {
            return "我为你推荐一些热门视频！点击主页可以看到最新最热的内容哦！🔥";
        } else if (lowerMessage.includes('上传')) {
            return "想分享你的精彩视频？点击导航栏的'上传'按钮就可以啦！📹";
        } else if (lowerMessage.includes('搜索')) {
            return "用搜索功能可以找到你想看的任何视频，试试输入关键词吧！🔍";
        } else if (lowerMessage.includes('帮助')) {
            return "我可以帮你导航网站、推荐视频、回答问题！有什么想了解的尽管问我！💡";
        } else if (lowerMessage.includes('可爱') || lowerMessage.includes('漂亮')) {
            return "谢谢夸奖！你也很棒哦！要不要试试给我换个新造型？💖";
        } else {
            return "这个问题很有趣！我还在学习中，能换个话题聊聊吗？或者试试问我关于视频的问题！🤔";
        }
    }

    async loadChatHistory() {
        try {
            const response = await fetch(`/api/pet/history/${this.userId}`);
            const history = await response.json();

            // 确保history是数组，然后显示最近的3条对话
            if (Array.isArray(history) && history.length > 0) {
                history.slice(0, 3).reverse().forEach(chat => {
                    this.addMessage(chat.userMessage, 'user');
                    this.addMessage(chat.aiResponse, 'bot');
                });
            }
        } catch (error) {
            console.error('加载聊天历史失败:', error);
        }
    }

    toggleVoiceRecording() {
        if (!this.recognition) {
            alert('您的浏览器不支持语音识别功能');
            return;
        }

        if (this.isRecording) {
            this.stopRecording();
        } else {
            this.startRecording();
        }
    }

    startRecording() {
        this.isRecording = true;
        const voiceBtn = document.getElementById('voiceBtn');
        const voiceWave = document.getElementById('voiceWave');

        voiceBtn.classList.add('recording');
        voiceWave.classList.add('active');

        this.recognition.start();
        this.addMessage('🎤 正在听...', 'bot');
        this.showExpression('thinking');
    }

    stopRecording() {
        this.isRecording = false;
        const voiceBtn = document.getElementById('voiceBtn');
        const voiceWave = document.getElementById('voiceWave');

        voiceBtn.classList.remove('recording');
        voiceWave.classList.remove('active');

        if (this.recognition) {
            this.recognition.stop();
        }
    }

    speak(text) {
        if (this.synthesis) {
            // 停止当前播放
            this.synthesis.cancel();

            const utterance = new SpeechSynthesisUtterance(text);
            utterance.lang = 'zh-CN';

            // 应用主题语音设置
            if (this.currentThemeConfig && this.currentThemeConfig.voiceSettings) {
                const voice = this.currentThemeConfig.voiceSettings;
                utterance.rate = voice.rate;
                utterance.pitch = voice.pitch;
                utterance.volume = voice.volume;
            } else {
                utterance.rate = 0.9;
                utterance.pitch = 1.1;
                utterance.volume = 0.8;
            }

            // 说话时显示动画
            utterance.onstart = () => {
                this.showExpression('happy');
                if (this.currentThemeConfig && this.currentThemeConfig.animations.talking) {
                    const avatar = document.getElementById('petAvatar');
                    avatar.style.animation = this.currentThemeConfig.animations.talking;
                }
            };

            utterance.onend = () => {
                if (this.currentThemeConfig) {
                    const avatar = document.getElementById('petAvatar');
                    avatar.style.animation = this.currentThemeConfig.animations.idle;
                }
            };

            this.synthesis.speak(utterance);
        }
    }
}

// 全局变量
let smartPet;

// 页面加载完成后初始化智能桌宠
document.addEventListener('DOMContentLoaded', () => {
    // 加载主题配置
    if (typeof PetThemes === 'undefined') {
        // 如果主题文件没有加载，创建一个基本的主题
        window.PetThemes = {
            cute_girl: {
                name: "可爱女孩",
                avatar: "👧",
                background: "linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%)",
                voiceSettings: { rate: 0.8, pitch: 1.3, volume: 0.8 },
                animations: { idle: "bounce 2s ease-in-out infinite", talking: "pulse 0.5s ease-in-out infinite", happy: "tada 1s ease-in-out", thinking: "headShake 1s ease-in-out" },
                expressions: { happy: "😊", sad: "😢", thinking: "🤔", excited: "🤩", love: "😍" },
                particles: { color: "#ff69b4", shape: "heart" }
            }
        };
    }

    smartPet = new SmartPet();
});