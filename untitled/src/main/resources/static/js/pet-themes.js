// 桌宠主题配置
const PetThemes = {
    // 可爱女孩主题
    cute_girl: {
        name: "可爱女孩",
        avatar: "👧",
        background: "linear-gradient(135deg, #ff9a9e 0%, #fecfef 50%, #fecfef 100%)",
        voiceSettings: {
            rate: 0.8,
            pitch: 1.3,
            volume: 0.8
        },
        animations: {
            idle: "bounce 2s ease-in-out infinite",
            talking: "pulse 0.5s ease-in-out infinite",
            happy: "tada 1s ease-in-out",
            thinking: "headShake 1s ease-in-out"
        },
        expressions: {
            happy: "😊",
            sad: "😢",
            thinking: "🤔",
            excited: "🤩",
            love: "😍"
        },
        particles: {
            color: "#ff69b4",
            shape: "heart"
        }
    },
    
    // 动漫少女主题
    anime_girl: {
        name: "动漫少女",
        avatar: "🌸",
        background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
        voiceSettings: {
            rate: 0.9,
            pitch: 1.4,
            volume: 0.9
        },
        animations: {
            idle: "float 3s ease-in-out infinite",
            talking: "swing 0.8s ease-in-out infinite",
            happy: "bounceIn 1s ease-in-out",
            thinking: "wobble 1s ease-in-out"
        },
        expressions: {
            happy: "✨",
            sad: "💧",
            thinking: "💭",
            excited: "⭐",
            love: "💖"
        },
        particles: {
            color: "#9c88ff",
            shape: "star"
        }
    },
    
    // 科技机器人主题
    robot: {
        name: "科技机器人",
        avatar: "🤖",
        background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
        voiceSettings: {
            rate: 1.0,
            pitch: 0.8,
            volume: 1.0
        },
        animations: {
            idle: "pulse 2s ease-in-out infinite",
            talking: "flash 0.5s ease-in-out infinite",
            happy: "rotateIn 1s ease-in-out",
            thinking: "fadeInOut 1s ease-in-out infinite"
        },
        expressions: {
            happy: "⚡",
            sad: "🔋",
            thinking: "💻",
            excited: "🚀",
            love: "💙"
        },
        particles: {
            color: "#00ffff",
            shape: "circle"
        }
    },
    
    // 猫娘主题
    cat_girl: {
        name: "猫娘",
        avatar: "🐱",
        background: "linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)",
        voiceSettings: {
            rate: 0.85,
            pitch: 1.5,
            volume: 0.8
        },
        animations: {
            idle: "sway 2s ease-in-out infinite",
            talking: "wiggle 0.6s ease-in-out infinite",
            happy: "bounce 1s ease-in-out",
            thinking: "tilt 1s ease-in-out"
        },
        expressions: {
            happy: "😸",
            sad: "😿",
            thinking: "🙀",
            excited: "😻",
            love: "😽"
        },
        particles: {
            color: "#ff8a65",
            shape: "paw"
        }
    }
};