class DesktopPet {
    constructor() {
        this.pet = null;
        this.isDragging = false;
        this.currentX = 0;
        this.currentY = 0;
        this.initialX = 0;
        this.initialY = 0;
        this.xOffset = 0;
        this.yOffset = 0;
        this.messages = [
            "欢迎来到视频网站！🎬",
            "发现有趣的视频吧！✨",
            "别忘了点赞和收藏哦！❤️",
            "上传你的精彩视频！📹",
            "和其他用户互动吧！💬",
            "探索更多精彩内容！🌟"
        ];
        this.currentMessageIndex = 0;
        this.init();
    }

    init() {
        this.createPet();
        this.addEventListeners();
        this.startRandomMessages();
    }

    createPet() {
        this.pet = document.createElement('div');
        this.pet.className = 'desktop-pet';
        this.pet.innerHTML = `
            <div class="pet-body">
                <div class="pet-face">
                    <div class="pet-eyes">
                        <div class="eye left-eye"></div>
                        <div class="eye right-eye"></div>
                    </div>
                    <div class="pet-mouth"></div>
                </div>
                <div class="pet-message" id="petMessage">
                    ${this.messages[0]}
                </div>
            </div>
        `;
        
        // 设置初始位置
        this.pet.style.left = '20px';
        this.pet.style.top = '50%';
        
        document.body.appendChild(this.pet);
    }

    addEventListeners() {
        this.pet.addEventListener('mousedown', this.dragStart.bind(this));
        document.addEventListener('mousemove', this.dragMove.bind(this));
        document.addEventListener('mouseup', this.dragEnd.bind(this));
        
        // 点击桌宠显示消息
        this.pet.addEventListener('click', this.showRandomMessage.bind(this));
    }

    dragStart(e) {
        this.initialX = e.clientX - this.xOffset;
        this.initialY = e.clientY - this.yOffset;

        if (e.target === this.pet || this.pet.contains(e.target)) {
            this.isDragging = true;
            this.pet.style.cursor = 'grabbing';
        }
    }

    dragMove(e) {
        if (this.isDragging) {
            e.preventDefault();
            this.currentX = e.clientX - this.initialX;
            this.currentY = e.clientY - this.initialY;

            this.xOffset = this.currentX;
            this.yOffset = this.currentY;

            this.pet.style.left = this.currentX + 'px';
            this.pet.style.top = this.currentY + 'px';
        }
    }

    dragEnd() {
        this.initialX = this.currentX;
        this.initialY = this.currentY;
        this.isDragging = false;
        this.pet.style.cursor = 'grab';
    }

    showRandomMessage() {
        const messageElement = document.getElementById('petMessage');
        this.currentMessageIndex = (this.currentMessageIndex + 1) % this.messages.length;
        messageElement.textContent = this.messages[this.currentMessageIndex];
        
        // 添加动画效果
        messageElement.style.animation = 'none';
        setTimeout(() => {
            messageElement.style.animation = 'bounce 0.5s ease-in-out';
        }, 10);
    }

    startRandomMessages() {
        setInterval(() => {
            if (!this.isDragging) {
                this.showRandomMessage();
            }
        }, 10000); // 每10秒显示一条新消息
    }
}

// 页面加载完成后初始化桌宠
document.addEventListener('DOMContentLoaded', () => {
    new DesktopPet();
});