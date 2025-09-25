# 视频音乐分享平台

一个基于Spring Boot的视频和音乐分享平台，支持用户上传、播放、点赞、收藏等功能。

## 功能特性

### 视频功能
- 视频上传和播放
- 视频分类管理
- 点赞和收藏功能
- 评论系统
- 视频搜索

### 音乐功能
- 音乐上传和播放
- 歌词编辑功能
- 音乐分类
- 播放统计
- 音乐搜索

### 用户系统
- 用户注册和登录
- 用户权限管理
- 个人资料管理
- 用户音乐收藏

### 管理功能
- 超级管理员面板
- 内容审核
- 数据统计
- 系统监控

## 技术栈

- **后端**: Spring Boot 3.0+
- **数据库**: MySQL 8.0+
- **模板引擎**: Thymeleaf
- **前端**: Bootstrap 5, jQuery
- **构建工具**: Maven

## 快速开始

### 环境要求
- JDK 17+
- MySQL 8.0+
- Maven 3.6+

### 安装步骤

1. 克隆项目
```bash
git clone [your-repository-url]
cd untitled
```

2. 配置数据库
- 创建MySQL数据库
- 修改 `src/main/resources/application.properties` 中的数据库连接信息

3. 运行项目
```bash
mvn spring-boot:run
```

4. 访问应用
- 主页: http://localhost:8081
- 音乐播放器: http://localhost:8081/music
- 管理面板: http://localhost:8081/super-admin

## 项目结构

```
src/
├── main/
│   ├── java/org/example/
│   │   ├── config/          # 配置类
│   │   ├── controller/      # 控制器
│   │   ├── entity/          # 实体类
│   │   ├── repository/      # 数据访问层
│   │   ├── service/         # 业务逻辑层
│   │   └── exception/       # 异常处理
│   └── resources/
│       ├── templates/       # Thymeleaf模板
│       ├── static/          # 静态资源
│       └── application.properties
└── test/                    # 测试代码
```

## 主要页面

- `/` - 主页
- `/music` - 音乐播放器
- `/music/upload` - 音乐上传
- `/music/play/{id}` - 音乐播放
- `/search` - 搜索页面
- `/super-admin` - 管理面板
- `/about` - 关于页面

## 贡献

欢迎提交Issue和Pull Request来改进项目。

## 许可证

MIT License 