# 白噪音手表应用 - 设计文档

## 项目概述
一款专为安卓手表设计的极简白噪音播放应用，专注于放松和助眠场景。

## 基本信息
- **应用名称**: 白噪音
- **包名**: com.junewong.watch.witenoise
- **目标设备**: OPPO Watch 3 及其他安卓手表
- **最低系统**: Android 11+
- **屏幕分辨率**: 410x494 像素 (OPPO Watch 3)
- **开发语言**: Java
- **构建工具**: Gradle

## 核心功能

### 1. 自动播放
- 应用启动后立即开始播放白噪音
- 单曲循环播放
- 音频文件: `music/0.m4a` (雨声)

### 2. 定时功能
- **默认**: 30分钟
- **可选项**: 10分钟、20分钟、30分钟、1小时、2小时、不限制
- **到期行为**: 淡出停止播放，直接退出应用

### 3. 播放控制
- 播放/暂停按钮
- 音乐选择（当前仅一首，预留扩展）

### 4. 省电策略
- **播放时**: 
  - 使用 MediaPlayer（轻量级）
  - PARTIAL_WAKE_LOCK（仅保持CPU，屏幕可关闭）
  - 前台服务（Android要求，但OPPO手表无通知显示）
- **暂停时**: 
  - 释放所有资源
  - 切换到后台5秒后自动退出应用
- **其他**: 
  - 不使用网络、GPS、传感器
  - 锁定竖屏方向

## 技术架构

### 核心组件

#### 1. MainActivity
- 唯一的Activity
- 负责UI展示和用户交互
- 监听应用生命周期（后台5秒退出）

#### 2. MusicService (Foreground Service)
- 后台播放音乐
- 管理MediaPlayer生命周期
- 处理定时器逻辑
- 定时到期自动退出

#### 3. MediaPlayer
- 播放 m4a 音频文件
- 循环播放模式
- 音量淡入淡出

#### 4. CountDownTimer
- 实现定时功能
- 倒计时结束触发退出

### 数据存储
- **SharedPreferences**: 保存用户上次选择的定时时长

### 权限需求
- `FOREGROUND_SERVICE` - 后台播放
- `WAKE_LOCK` - 保持CPU运行

## UI设计

### 设计原则
- **极简**: 只显示必要信息
- **放松**: 柔和色调，舒适视觉
- **易用**: 大按钮，适合手表操作

### 配色方案
- **背景**: 深色渐变（深蓝/深紫，夜间友好）
- **主色**: 柔和蓝/青色（平静感）
- **强调色**: 温暖橙/金色（按钮高亮）
- **文字**: 白色/浅灰（高对比度）

### 布局结构
```
┌─────────────────────┐
│                     │
│   [音乐名称: 雨声]   │
│                     │
│   ┌───────────┐     │
│   │  播放/暂停 │     │  (大圆形按钮)
│   └───────────┘     │
│                     │
│   [定时: 30分钟]     │  (可点击选择)
│                     │
│   [剩余: 29:45]     │  (倒计时显示)
│                     │
└─────────────────────┘
```

### 交互设计
1. **播放按钮**: 点击切换播放/暂停状态
2. **定时选择**: 点击弹出选择器（底部弹窗或对话框）
3. **音乐选择**: 点击音乐名称切换（当前仅一首）
4. **视觉反馈**: 播放时按钮有动画效果

## 省电优化细节

### 1. 播放时优化
```java
// 仅保持CPU运行，允许屏幕关闭
PowerManager.WakeLock wakeLock = 
    powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WhiteNoise::lock");

// MediaPlayer使用最低资源
mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
```

### 2. 后台退出机制
```
用户点击Home键 → onPause() → 启动5秒定时器 → 
  ├─ 5秒内返回 → 取消定时器，继续播放
  └─ 5秒后 → stopService() + finish()
```

### 3. 定时到期
```
倒计时结束 → 淡出音量(1秒) → stopService() → System.exit(0)
```

## 构建与发布

### GitHub Actions工作流
1. **触发条件**: Push到main分支或手动触发
2. **构建步骤**:
   - 设置JDK 11
   - 设置Android SDK
   - 授予Gradle执行权限
   - 执行 `./gradlew assembleDebug`
   - 上传APK为artifact
3. **输出**: `app-debug.apk`

### 版本管理
- **versionCode**: 1
- **versionName**: 1.0.0

## 文件结构
```
waitch-white-noise/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/junewong/watch/witenoise/
│   │       │   ├── MainActivity.java
│   │       │   ├── MusicService.java
│   │       │   └── TimerPickerDialog.java
│   │       ├── res/
│   │       │   ├── layout/
│   │       │   │   ├── activity_main.xml
│   │       │   │   └── dialog_timer_picker.xml
│   │       │   ├── values/
│   │       │   │   ├── strings.xml
│   │       │   │   ├── colors.xml
│   │       │   │   └── themes.xml
│   │       │   ├── drawable/
│   │       │   │   ├── bg_gradient.xml
│   │       │   │   ├── ic_play.xml
│   │       │   │   └── ic_pause.xml
│   │       │   └── raw/
│   │       │       └── rain.m4a (从music/0.m4a复制)
│   │       └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle
├── settings.gradle
├── gradle.properties
├── .github/
│   └── workflows/
│       └── build.yml
├── music/
│   └── 0.m4a
└── docs/
    ├── oppo-watch3.md
    └── design.md
```

## 未来扩展
- 支持多个白噪音音频文件
- 自定义定时时长
- 音量调节
- 淡入淡出时长设置
- 播放统计

## 开发注意事项
1. 不使用Wear OS SDK，使用标准Android SDK
2. 所有UI尺寸使用dp单位，适配不同DPI
3. 测试后台5秒退出逻辑
4. 测试定时到期退出逻辑
5. 确保音频文件正确打包到APK
6. 省电测试（长时间播放电量消耗）
