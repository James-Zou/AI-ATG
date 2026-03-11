#!/bin/bash

# 测试下载链接脚本
# 用于验证所有下载链接是否正常工作

echo "=================================="
echo "  下载链接测试脚本"
echo "=================================="
echo ""

BASE_URL="http://localhost:19080"

echo "测试下载接口..."
echo ""

# 测试 ATG-Client 下载
echo "1. 测试 ATG-Client Windows 版本"
curl -I "${BASE_URL}/downloads/atg-client-windows.zip" 2>&1 | grep "HTTP/"

echo ""
echo "2. 测试 ATG-Client macOS 版本"
curl -I "${BASE_URL}/downloads/atg-client-macos.zip" 2>&1 | grep "HTTP/"

echo ""
echo "3. 测试 ATG-Client Linux 版本"
curl -I "${BASE_URL}/downloads/atg-client-linux.tar.gz" 2>&1 | grep "HTTP/"

echo ""
echo "=================================="
echo "测试完成！"
echo "=================================="
echo ""
echo "期望结果：所有请求应该返回 HTTP/1.1 200"
echo "如果返回 404，请检查："
echo "  1. 后端服务是否已启动 (http://localhost:19080)"
echo "  2. 文件是否存在 (backend/downloads/)"
echo "  3. DownloadController 路径配置是否正确"
echo ""
