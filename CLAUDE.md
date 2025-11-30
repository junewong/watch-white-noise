# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 重要规则

- **Git Push 策略**：如果只是文档编辑或小修改（不涉及代码构建），不要主动 git push 触发打包。仅在代码变更需要构建时才推送。

## 构建命令

### GitHub Actions 自动构建
推送代码到 main 分支会自动触发构建：
```bash
git add .
git commit -m "描述"
git push
```
在 Actions 页面下载构建产物：https://github.com/junewong/watch-white-noise/actions

### 安装到设备
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 架构概览

这是一个针对安卓手表的极简白噪音播放应用，采用原生 Android 开发。

### 核心组件

1. **MainActivity.java** - 主界面，负责UI交互和播放控制
   - 使用 ViewPager2 实现左右滑动布局
     - 第一页：播放/暂停按钮（适配手表小屏幕）
     - 第二页：白噪音名称、定时器设置、倒计时显示
   - 启动时自动播放白噪音（仅首次启动）
   - 使用静态标志位 `serviceStarted` 避免重复启动服务
   - 通过广播接收服务状态更新（UPDATE_TIME, PLAYBACK_FINISHED, PLAYBACK_STATUS）
   - 应用生命周期管理（暂停5秒后自动退出）

2. **MusicService.java** - 前台服务，负责音频播放
   - 使用 MediaPlayer 播放 R.raw.rain 音频文件
   - 循环播放和定时功能
   - 维护 `isPlaying` 状态并通过广播同步到 MainActivity
   - 支持 PAUSE/RESUME 操作，保持定时器状态
   - PARTIAL_WAKE_LOCK 保证后台播放
   - 淡出效果和自动退出
   - **MediaSession 集成**：
     - 创建 MediaSessionCompat 处理媒体控制
     - 实现 MediaSessionCompat.Callback 响应蓝牙耳机线控
     - 更新 MediaMetadataCompat（白噪音信息）
     - 更新 PlaybackStateCompat（播放状态）同步到系统状态栏

3. **TimerPickerDialog.java** - 定时选择对话框
   - 使用 ScrollView 支持上下滚动
   - 提供6个选项：10分钟、20分钟、30分钟、1小时、2小时、不限制
   - 使用回调接口与 MainActivity 通信

### 关键权限和配置
- `FOREGROUND_SERVICE` - 前台服务播放音频
- `WAKE_LOCK` - 保持设备唤醒状态
- 屏幕方向锁定为 portrait
- Android 11+ (minSdk 30), 目标 SDK 34

### 通信机制
- MainActivity 与 MusicService 通过 Intent 和 Service 调用通信
- MusicService 通过广播发送状态更新（UPDATE_TIME, PLAYBACK_FINISHED, PLAYBACK_STATUS）
- MainActivity 在 onResume 时请求服务状态同步（REQUEST_STATUS）
- 使用 SharedPreferences 持久化定时器设置

### 省电策略
- 暂停后5秒自动退出应用
- 定时结束后自动淡出并退出
- 使用最低功耗的后台播放模式

## 开发注意事项

- 音频文件放在 `app/src/main/res/raw/rain.m4a`
- 应用专为目标设备（如 OPPO Watch 3）优化
- 使用 Java 8 兼容性设置
- 包名：com.junewong.watch.witenoise
