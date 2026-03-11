#!/bin/bash

# ATG-Client打包脚本
# 用于快速生成各平台的安装包

set -e

echo "=================================="
echo "  ATG-Client 打包脚本"
echo "=================================="

# 检查是否在项目根目录
if [ ! -d "agent-service" ]; then
    echo "错误：请在项目根目录运行此脚本"
    exit 1
fi

# 创建输出目录
OUTPUT_DIR="backend/downloads"
mkdir -p "$OUTPUT_DIR"

echo ""
echo "1. 准备agent-service目录..."
cd agent-service

# 检查是否安装了依赖
if [ ! -d "node_modules" ]; then
    echo "   安装npm依赖..."
    npm install
fi

echo ""
echo "2. 创建临时打包目录..."
TEMP_DIR="../temp-package"
rm -rf "$TEMP_DIR"
mkdir -p "$TEMP_DIR"

# 复制必要文件
echo "   复制文件..."
cp -r src "$TEMP_DIR/"
cp package.json "$TEMP_DIR/"
cp install.bat "$TEMP_DIR/"
cp install.sh "$TEMP_DIR/"
cp README.md "$TEMP_DIR/"
cp WEBDRIVER_SETUP.md "$TEMP_DIR/"
cp CONFIG_GUIDE.md "$TEMP_DIR/"

# 复制icons（如果存在）
if [ -d "icons" ]; then
    cp -r icons "$TEMP_DIR/"
fi

echo ""
echo "3. 打包各平台版本..."

# Windows
echo "   打包 Windows 版本..."
cd "$TEMP_DIR"
zip -rq "../$OUTPUT_DIR/atg-client-windows.zip" .
echo "   ✓ atg-client-windows.zip"

# macOS
echo "   打包 macOS 版本..."
zip -rq "../$OUTPUT_DIR/atg-client-macos.zip" .
echo "   ✓ atg-client-macos.zip"

# Linux
echo "   打包 Linux 版本..."
tar -czf "../$OUTPUT_DIR/atg-client-linux.tar.gz" .
echo "   ✓ atg-client-linux.tar.gz"

# 清理临时目录
cd ..
rm -rf "$TEMP_DIR"

echo ""
echo "4. 验证打包结果..."
cd "$OUTPUT_DIR"
echo ""
echo "生成的文件："
ls -lh atg-client-*
echo ""

# 计算文件大小
WINDOWS_SIZE=$(du -h atg-client-windows.zip | cut -f1)
MACOS_SIZE=$(du -h atg-client-macos.zip | cut -f1)
LINUX_SIZE=$(du -h atg-client-linux.tar.gz | cut -f1)

echo "文件大小统计："
echo "  Windows: $WINDOWS_SIZE"
echo "  macOS:   $MACOS_SIZE"
echo "  Linux:   $LINUX_SIZE"

echo ""
echo "=================================="
echo "✓ 打包完成！"
echo "=================================="
echo ""
echo "安装包位置：$OUTPUT_DIR/"
echo ""
echo "下一步："
echo "  1. 启动后端服务"
echo "  2. 访问 http://localhost:19080/downloads/atg-client-windows.zip 测试下载"
echo "  3. 或在AI-ATG平台的下载中心进行下载"
echo ""
