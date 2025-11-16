#!/bin/bash

# 获取最新成功构建的 run ID
echo "🔍 获取最新构建..."
RUN_ID=$(gh run list --workflow=build.yml --status=success --limit=1 --json databaseId --jq '.[0].databaseId')

if [ -z "$RUN_ID" ]; then
    echo "❌ 没有找到成功的构建"
    exit 1
fi

echo "📦 最新构建 ID: $RUN_ID"
echo "⬇️  开始下载..."

# 下载 artifact
gh run download $RUN_ID

echo "✅ 下载完成！"
echo "📱 安装命令: adb install app-debug/app-debug.apk"
