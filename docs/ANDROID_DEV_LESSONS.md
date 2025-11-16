# Android 开发经验总结

基于白噪音手表应用开发过程中遇到的问题和解决方案。

## 权限问题

**Android 14+ 前台服务权限**
- 问题：前台服务无法启动
- 原因：Android 14+ 要求声明特定类型的前台服务权限
- 解决：除了 `FOREGROUND_SERVICE`，还需添加 `FOREGROUND_SERVICE_MEDIA_PLAYBACK`
```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
```

## 生命周期和空指针

**ViewPager2 视图引用**
- 问题：在广播接收器中更新 UI 导致空指针崩溃
- 原因：ViewPager2 的子视图可能未初始化或已销毁
- 解决：使用 `findViewById()` 前必须判空，避免缓存视图引用

**Activity 生命周期**
- 问题：onResume 时 UI 状态不同步
- 解决：在 onResume 发送 `REQUEST_STATUS` 广播，让 Service 主动同步状态

## Service 与 Activity 通信

**状态同步机制**
- 使用广播实现 Service → Activity 单向通信
- Service 维护 `isPlaying` 状态，通过 `PLAYBACK_STATUS` 广播同步
- Activity 通过 Intent 调用 Service 方法（startService）
- 避免双向绑定，减少复杂度

**重复启动问题**
- 问题：多次调用 startService 导致状态混乱
- 解决：使用静态标志位 `serviceStarted` 防止重复启动

## 定时器和播放控制

**暂停/恢复逻辑**
- 问题：暂停后无法恢复播放，定时器被重置
- 解决：PAUSE 操作保持定时器状态，只停止音频；RESUME 恢复播放但不重置定时器

**首次启动行为**
- 问题：每次打开应用都自动播放
- 解决：使用静态标志位区分首次启动和后续恢复，仅首次自动播放

## GitHub Actions

**版本兼容性**
- 问题：`actions/checkout@v3` 和 `actions/upload-artifact@v3` 已过时
- 解决：升级到 v4 版本避免警告和潜在问题

**构建策略**
- 文档修改不应触发构建，浪费 CI 资源
- 在 CLAUDE.md 中明确标注 Git Push 策略

## UI 设计

**手表小屏幕适配**
- 使用 ViewPager2 左右滑动分页，避免单页内容过多
- 第一页：核心操作（播放/暂停）
- 第二页：次要功能（定时器、倒计时）

**对话框滚动**
- 问题：定时选项过多，小屏幕显示不全
- 解决：使用 ScrollView 包裹内容，确保可滚动

## 省电优化

- 使用 `PARTIAL_WAKE_LOCK` 而非 `FULL_WAKE_LOCK`
- 暂停后 5 秒自动退出应用
- 定时结束后淡出并自动退出
- 避免不必要的后台运行
