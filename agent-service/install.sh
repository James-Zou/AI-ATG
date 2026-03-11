#!/bin/bash

echo "===================================="
echo "  AI-ATG 测试服务安装程序"
echo "===================================="
echo ""

# 检查Node.js
if ! command -v node &> /dev/null; then
    echo "[错误] 未检测到Node.js"
    echo ""
    echo "请先安装Node.js 18+: https://nodejs.org/"
    echo ""
    echo "macOS: brew install node"
    echo "Ubuntu: sudo apt install nodejs npm"
    exit 1
fi

echo "[信息] Node.js 已安装"
node -v
echo ""

# 安装依赖
echo "[步骤 1/5] 安装依赖包..."
npm install
if [ $? -ne 0 ]; then
    echo "[错误] 依赖安装失败"
    exit 1
fi
echo "[信息] 依赖安装完成"
echo ""

# 创建配置文件
echo "[步骤 2/5] 创建配置文件..."
CONFIG_DIR="$HOME/.atg-client"
CONFIG_FILE="$CONFIG_DIR/config.json"

# 确保配置目录存在
mkdir -p "$CONFIG_DIR"

# 创建配置文件（如果不存在）
if [ ! -f "$CONFIG_FILE" ]; then
    cat > "$CONFIG_FILE" << 'EOF'
{
  "serverUrl": "http://localhost:19080",
  "browser": "chrome",
  "headless": false,
  "autoStart": true,
  "chromeDriverPath": "",
  "geckoDriverPath": ""
}
EOF
    echo "[信息] 配置文件已创建: $CONFIG_FILE"
    echo ""
    echo "💡 提示: 如需配置WebDriver路径，请编辑此文件："
    echo "   $CONFIG_FILE"
    echo ""
else
    echo "[信息] 配置文件已存在，跳过创建"
fi
echo ""

# 安装服务
echo "[步骤 3/5] 安装系统服务..."

if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    npm run install-service
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    # Linux - 需要sudo
    if [ "$EUID" -ne 0 ]; then
        echo "[信息] 需要sudo权限安装系统服务"
        sudo npm run install-service
    else
        npm run install-service
    fi
else
    echo "[错误] 不支持的操作系统: $OSTYPE"
    exit 1
fi

if [ $? -ne 0 ]; then
    echo "[错误] 服务安装失败"
    exit 1
fi
echo "[信息] 服务安装完成"
echo ""

# 启动服务
echo "[步骤 4/5] 启动服务..."

if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS - 修复系统托盘权限
    if [ -d ~/.cache/node-systray ]; then
        chmod -R +x ~/.cache/node-systray 2>/dev/null || true
    fi
    launchctl load ~/Library/LaunchAgents/com.atgclient.plist 2>/dev/null || launchctl start com.atgclient
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    sudo systemctl start atg-client
fi
echo ""

# 验证服务
echo "[步骤 5/5] 验证服务..."
sleep 3

if curl -s http://localhost:9999/health > /dev/null 2>&1; then
    echo "[成功] 服务运行正常！"
    echo ""
    echo "访问: http://localhost:9999/health"
else
    echo "[警告] 服务可能未正常启动"
    echo "请查看日志"
fi
echo ""

echo "===================================="
echo "  安装完成！"
echo "===================================="
echo ""
echo "✓ 服务已安装为系统服务"
echo "✓ 服务将在开机时自动启动"
echo "✓ 系统托盘会显示服务图标"
echo "✓ 配置文件已创建: ~/.atg-client/config.json"
echo ""
echo "⚠️  重要提示："
echo "   1. 请先配置WebDriver（必需）"
echo "   2. 配置文件位置: $HOME/.atg-client/config.json"
echo "   3. 配置完成后重启服务: launchctl restart com.atgclient"
echo ""
echo "现在可以在AI-ATG平台执行UI测试了！"
echo ""

# 显示卸载命令
if [[ "$OSTYPE" == "darwin"* ]]; then
    echo "卸载服务: npm run uninstall-service"
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    echo "卸载服务: sudo npm run uninstall-service"
fi
echo ""
