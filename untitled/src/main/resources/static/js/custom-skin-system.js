class CustomSkinSystem {
    constructor(smartPet) {
        this.smartPet = smartPet;
        this.customSkins = JSON.parse(localStorage.getItem('customSkins') || '[]');
        this.currentCustomSkin = null;
        
        this.initCustomSkinUI();
    }

    initCustomSkinUI() {
        // 添加自定义皮肤按钮到主题选择器
        const themeSelector = document.getElementById('themeSelector');
        if (themeSelector) {
            const customBtn = document.createElement('button');
            customBtn.className = 'theme-btn custom-skin-btn';
            customBtn.innerHTML = '🎨';
            customBtn.title = '自定义皮肤';
            customBtn.onclick = () => this.openCustomSkinEditor();
            themeSelector.appendChild(customBtn);
        }
    }

    openCustomSkinEditor() {
        const modal = document.createElement('div');
        modal.className = 'custom-skin-modal';
        modal.innerHTML = `
            <div class="modal-content">
                <div class="modal-header">
                    <h3>自定义皮肤编辑器</h3>
                    <button class="close-btn" onclick="this.parentElement.parentElement.parentElement.remove()">×</button>
                </div>
                <div class="modal-body">
                    <div class="skin-editor">
                        <div class="upload-section">
                            <h4>上传图片</h4>
                            <input type="file" id="skinImageUpload" accept="image/*" multiple>
                            <div class="image-preview" id="imagePreview"></div>
                        </div>
                        
                        <div class="animation-section">
                            <h4>动画设置</h4>
                            <label>空闲动画：
                                <select id="idleAnimation">
                                    <option value="bounce">弹跳</option>
                                    <option value="float">漂浮</option>
                                    <option value="pulse">脉冲</option>
                                    <option value="rotate">旋转</option>
                                </select>
                            </label>
                            
                            <label>说话动画：
                                <select id="talkingAnimation">
                                    <option value="swing">摇摆</option>
                                    <option value="wiggle">摆动</option>
                                    <option value="pulse">脉冲</option>
                                </select>
                            </label>
                        </div>
                        
                        <div class="effect-section">
                            <h4>特效设置</h4>
                            <label>粒子颜色：
                                <input type="color" id="particleColor" value="#ff69b4">
                            </label>
                            
                            <label>粒子形状：
                                <select id="particleShape">
                                    <option value="heart">爱心</option>
                                    <option value="star">星星</option>
                                    <option value="circle">圆圈</option>
                                    <option value="flower">花朵</option>
                                </select>
                            </label>
                            
                            <label>背景渐变：
                                <input type="color" id="bgColor1" value="#ff9a9e">
                                <input type="color" id="bgColor2" value="#fecfef">
                            </label>
                        </div>
                        
                        <div class="voice-section">
                            <h4>语音设置</h4>
                            <label>语速：<input type="range" id="voiceRate" min="0.5" max="2" step="0.1" value="0.8"></label>
                            <label>音调：<input type="range" id="voicePitch" min="0.5" max="2" step="0.1" value="1.3"></label>
                            <label>音量：<input type="range" id="voiceVolume" min="0" max="1" step="0.1" value="0.8"></label>
                        </div>
                        
                        <div class="preview-section">
                            <h4>预览</h4>
                            <div class="skin-preview" id="skinPreview">
                                <div class="preview-avatar">🎨</div>
                            </div>
                        </div>
                        
                        <div class="action-buttons">
                            <button onclick="customSkinSystem.saveSkin()">保存皮肤</button>
                            <button onclick="customSkinSystem.applySkin()">应用皮肤</button>
                            <button onclick="customSkinSystem.resetSkin()">重置</button>
                        </div>
                    </div>
                </div>
            </div>
        `;
        
        document.body.appendChild(modal);
        this.setupSkinEditor();
    }

    setupSkinEditor() {
        const imageUpload = document.getElementById('skinImageUpload');
        imageUpload.addEventListener('change', (e) => {
            this.handleImageUpload(e.target.files);
        });
        
        // 实时预览
        const inputs = ['idleAnimation', 'talkingAnimation', 'particleColor', 'particleShape', 'bgColor1', 'bgColor2'];
        inputs.forEach(id => {
            const element = document.getElementById(id);
            if (element) {
                element.addEventListener('change', () => this.updatePreview());
            }
        });
    }

    handleImageUpload(files) {
        const preview = document.getElementById('imagePreview');
        preview.innerHTML = '';
        
        Array.from(files).forEach((file, index) => {
            const reader = new FileReader();
            reader.onload = (e) => {
                const img = document.createElement('img');
                img.src = e.target.result;
                img.className = 'uploaded-image';
                img.style.width = '60px';
                img.style.height = '60px';
                img.style.margin = '5px';
                img.style.borderRadius = '50%';
                img.style.cursor = 'pointer';
                img.onclick = () => this.selectImage(e.target.result);
                preview.appendChild(img);
                
                // 第一张图片自动选中
                if (index === 0) {
                    this.selectImage(e.target.result);
                }
            };
            reader.readAsDataURL(file);
        });
    }

    selectImage(imageSrc) {
        this.currentImageSrc = imageSrc;
        this.updatePreview();
    }

    updatePreview() {
        const preview = document.getElementById('skinPreview');
        const avatar = preview.querySelector('.preview-avatar');
        
        if (this.currentImageSrc) {
            avatar.style.backgroundImage = `url(${this.currentImageSrc})`;
            avatar.style.backgroundSize = 'cover';
            avatar.style.backgroundPosition = 'center';
            avatar.textContent = '';
        }
        
        const bgColor1 = document.getElementById('bgColor1').value;
        const bgColor2 = document.getElementById('bgColor2').value;
        preview.style.background = `linear-gradient(135deg, ${bgColor1} 0%, ${bgColor2} 100%)`;
        
        const animation = document.getElementById('idleAnimation').value;
        avatar.style.animation = `${animation} 2s ease-in-out infinite`;
    }

    saveSkin() {
        const skinData = {
            id: Date.now(),
            name: prompt('请输入皮肤名称:') || '自定义皮肤',
            image: this.currentImageSrc,
            background: `linear-gradient(135deg, ${document.getElementById('bgColor1').value} 0%, ${document.getElementById('bgColor2').value} 100%)`,
            animations: {
                idle: document.getElementById('idleAnimation').value + ' 2s ease-in-out infinite',
                talking: document.getElementById('talkingAnimation').value + ' 0.8s ease-in-out infinite'
            },
            particles: {
                color: document.getElementById('particleColor').value,
                shape: document.getElementById('particleShape').value
            },
            voiceSettings: {
                rate: parseFloat(document.getElementById('voiceRate').value),
                pitch: parseFloat(document.getElementById('voicePitch').value),
                volume: parseFloat(document.getElementById('voiceVolume').value)
            }
        };
        
        this.customSkins.push(skinData);
        localStorage.setItem('customSkins', JSON.stringify(this.customSkins));
        
        alert('皮肤保存成功！');
        this.addCustomSkinToSelector(skinData);
    }

    applySkin() {
        if (!this.currentImageSrc) {
            alert('请先上传图片！');
            return;
        }
        
        const skinData = {
            name: '临时皮肤',
            image: this.currentImageSrc,
            background: `linear-gradient(135deg, ${document.getElementById('bgColor1').value} 0%, ${document.getElementById('bgColor2').value} 100%)`,
            animations: {
                idle: document.getElementById('idleAnimation').value + ' 2s ease-in-out infinite',
                talking: document.getElementById('talkingAnimation').value + ' 0.8s ease-in-out infinite'
            },
            particles: {
                color: document.getElementById('particleColor').value,
                shape: document.getElementById('particleShape').value
            },
            voiceSettings: {
                rate: parseFloat(document.getElementById('voiceRate').value),
                pitch: parseFloat(document.getElementById('voicePitch').value),
                volume: parseFloat(document.getElementById('voiceVolume').value)
            }
        };
        
        this.applyCustomSkin(skinData);
        document.querySelector('.custom-skin-modal').remove();
    }

    applyCustomSkin(skinData) {
        const container = this.smartPet.pet.querySelector('.pet-container');
        const avatar = document.getElementById('petAvatar');
        
        // 应用背景
        container.style.background = skinData.background;
        
        // 应用图片
        if (skinData.image) {
            avatar.style.backgroundImage = `url(${skinData.image})`;
            avatar.style.backgroundSize = 'cover';
            avatar.style.backgroundPosition = 'center';
            avatar.textContent = '';
        }
        
        // 应用动画
        avatar.style.animation = skinData.animations.idle;
        
        // 保存当前自定义皮肤配置
        this.smartPet.currentThemeConfig = {
            name: skinData.name,
            animations: skinData.animations,
            particles: skinData.particles,
            voiceSettings: skinData.voiceSettings
        };
        
        this.smartPet.speak(`${skinData.name}皮肤已应用！`);
        this.smartPet.createParticles(8);
    }

    addCustomSkinToSelector(skinData) {
        const themeSelector = document.getElementById('themeSelector');
        const skinBtn = document.createElement('button');
        skinBtn.className = 'theme-btn';
        skinBtn.style.backgroundImage = `url(${skinData.image})`;
        skinBtn.style.backgroundSize = 'cover';
        skinBtn.title = skinData.name;
        skinBtn.onclick = () => this.applyCustomSkin(skinData);
        
        // 插入到自定义按钮前面
        const customBtn = themeSelector.querySelector('.custom-skin-btn');
        themeSelector.insertBefore(skinBtn, customBtn);
    }

    loadCustomSkins() {
        this.customSkins.forEach(skin => {
            this.addCustomSkinToSelector(skin);
        });
    }

    resetSkin() {
        document.getElementById('bgColor1').value = '#ff9a9e';
        document.getElementById('bgColor2').value = '#fecfef';
        document.getElementById('particleColor').value = '#ff69b4';
        document.getElementById('voiceRate').value = '0.8';
        document.getElementById('voicePitch').value = '1.3';
        document.getElementById('voiceVolume').value = '0.8';
        this.updatePreview();
    }
}