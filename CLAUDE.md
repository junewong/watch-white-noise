# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 构建命令

### 初始化（首次构建前）
```bash
./init.sh
```
下载 gradle-wrapper.jar，仅需执行一次。

### 本地构建
```bash
./gradlew assembleDebug
```
输出位置：`app/build/outputs/apk/debug/app-debug.apk`

### 清理项目
```bash
./gradlew clean
```

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
   - 启动时自动播放白噪音
   - 播放/暂停按钮控制
   - 定时选择和剩余时间显示
   - 应用生命周期管理（暂停5秒后自动退出）

2. **MusicService.java** - 前台服务，负责音频播放
   - 使用 MediaPlayer 播放 R.raw.rain 音频文件
   - 循环播放和定时功能
   - PARTIAL_WAKE_LOCK 保证后台播放
   - 淡出效果和自动退出

3. **TimerPickerDialog.java** - 定时选择对话框
   - 提供6个选项：10分钟、20分钟、30分钟、1小时、2小时、不限制
   - 使用回调接口与 MainActivity 通信

### 关键权限和配置
- `FOREGROUND_SERVICE` - 前台服务播放音频
- `WAKE_LOCK` - 保持设备唤醒状态
- 屏幕方向锁定为 portrait
- Android 11+ (minSdk 30), 目标 SDK 34

### 通信机制
- MainActivity 与 MusicService 通过 Intent 和 Service 调用通信
- MusicService 通过广播发送状态更新（UPDATE_TIME, PLAYBACK_FINISHED）
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