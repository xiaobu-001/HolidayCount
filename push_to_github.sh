#!/usr/bin/env bash
# =========================================================
# HolidayCount 项目推送到 GitHub 脚本
# 使用方法：在 HolidayCount 项目根目录双击运行，或在终端执行
# =========================================================

set -e

echo "=========================================="
echo "  HolidayCount - 推送到 GitHub 并触发打包"
echo "=========================================="
echo ""

# 检查 git 是否安装
if ! command -v git &> /dev/null; then
    echo "❌ 未找到 git，请先安装 Git for Windows"
    exit 1
fi

# 提示用户输入 GitHub 仓库地址
echo "请输入你的 GitHub 仓库地址（例如：https://github.com/你的用户名/HolidayCount.git）"
echo "如果还没建仓库，请先在 https://github.com/new 创建一个名为 HolidayCount 的公开/私有仓库"
echo ""
read -p "仓库地址: " REPO_URL

if [ -z "$REPO_URL" ]; then
    echo "❌ 仓库地址不能为空"
    exit 1
fi

# 初始化 git（如果还没有）
if [ ! -d ".git" ]; then
    echo "📁 初始化 Git 仓库..."
    git init
    git branch -M main
fi

# 添加 .gitignore
if [ ! -f ".gitignore" ]; then
    cat > .gitignore << 'EOF'
*.iml
.gradle
/local.properties
/.idea/caches
/.idea/libraries
/.idea/modules.xml
/.idea/workspace.xml
/.idea/navEditor.xml
/.idea/assetWizardSettings.xml
.DS_Store
/build
/captures
.externalNativeBuild
.cxx
local.properties
*.apk
*.aab
EOF
    echo "✅ 已创建 .gitignore"
fi

# 设置远程仓库
if git remote | grep -q "origin"; then
    git remote set-url origin "$REPO_URL"
    echo "✅ 已更新远程仓库地址"
else
    git remote add origin "$REPO_URL"
    echo "✅ 已添加远程仓库"
fi

# 暂存所有文件
echo ""
echo "📦 暂存所有文件..."
git add -A

# 提交
echo "💾 创建提交..."
git commit -m "feat: HolidayCount Android App - 节假日倒计时桌面小部件

- 三种尺寸 Widget（2x1、4x1、4x2）
- 内置 25+ 中国节假日（含农历计算）
- 自定义事件管理（增删改）
- WorkManager 每日自动更新
- Material Design 3 主题
- 通知提醒功能" || echo "（无新变更，跳过提交）"

# 推送
echo ""
echo "🚀 推送到 GitHub..."
git push -u origin main

echo ""
echo "=========================================="
echo "✅ 推送成功！"
echo ""
echo "下一步："
echo "1. 打开 GitHub 仓库页面"
echo "2. 点击顶部 Actions 标签"
echo "3. 等待 Build Debug APK 工作流完成（约 5~10 分钟）"
echo "4. 点击完成的工作流 → 在底部 Artifacts 区域下载 APK"
echo "=========================================="
