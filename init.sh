#!/bin/bash

# 初始化 Gradle Wrapper
echo "正在初始化 Gradle Wrapper..."

# 创建必要的目录
mkdir -p gradle/wrapper

# 下载 gradle-wrapper.jar
echo "下载 gradle-wrapper.jar..."
curl -L -o gradle/wrapper/gradle-wrapper.jar \
  https://raw.githubusercontent.com/gradle/gradle/v8.0.0/gradle/wrapper/gradle-wrapper.jar

if [ $? -eq 0 ]; then
    echo "✅ Gradle Wrapper 初始化成功"
    echo "现在可以运行: ./gradlew assembleDebug"
else
    echo "❌ 下载失败，请手动下载 gradle-wrapper.jar"
    echo "下载地址: https://raw.githubusercontent.com/gradle/gradle/v8.0.0/gradle/wrapper/gradle-wrapper.jar"
    echo "保存到: gradle/wrapper/gradle-wrapper.jar"
fi
