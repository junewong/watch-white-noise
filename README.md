# 白噪音 - 手表应用

一款专为安卓手表设计的极简白噪音播放应用。

## 功能特性

- ✅ 自动播放：打开应用立即播放白噪音
- ✅ 循环播放：单曲循环，无缝衔接
- ✅ 定时功能：10分钟、20分钟、30分钟、1小时、2小时、不限制
- ✅ 省电优化：后台播放使用最低功耗，暂停后5秒自动退出
- ✅ 精美UI：深色渐变背景，柔和色调，适合放松场景

## 技术规格

- **包名**: com.junewong.watch.witenoise
- **最低系统**: Android 11+
- **目标设备**: OPPO Watch 3 及其他安卓手表
- **开发语言**: Java
- **构建工具**: Gradle

## 构建说明

### 本地构建

```bash
./gradlew assembleDebug
```

输出文件：`app/build/outputs/apk/debug/app-debug.apk`

### GitHub Actions 自动构建

推送代码到 main/master 分支后，GitHub Actions 会自动构建 APK。

在 Actions 页面下载构建产物 `app-debug`。

## 项目结构

```
waitch-white-noise/
├── app/
│   ├── src/main/
│   │   ├── java/com/junewong/watch/witenoise/
│   │   │   ├── MainActivity.java          # 主界面
│   │   │   ├── MusicService.java          # 音乐播放服务
│   │   │   └── TimerPickerDialog.java     # 定时选择对话框
│   │   ├── res/
│   │   │   ├── layout/                    # 布局文件
│   │   │   ├── drawable/                  # 图标和背景
│   │   │   ├── values/                    # 字符串、颜色、主题
│   │   │   └── raw/                       # 音频文件
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle
├── settings.gradle
└── .github/workflows/build.yml            # CI/CD 配置
```

## 使用说明

1. 安装 APK 到手表
2. 打开应用，自动开始播放
3. 点击中央按钮暂停/播放
4. 点击定时区域选择定时时长
5. 定时结束后自动退出

## 省电策略

- 播放时仅保持 CPU 运行（PARTIAL_WAKE_LOCK）
- 屏幕可以关闭，不影响播放
- 暂停后切换到后台 5 秒自动退出应用
- 定时到期淡出音量后自动退出

## 开发文档

详见 [docs/design.md](docs/design.md)

## 许可证

MIT License
