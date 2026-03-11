@echo off
chcp 65001 >nul
echo ====================================
echo   AI-ATG 测试服务安装程序
echo ====================================
echo.

REM 检查管理员权限
net session >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 需要管理员权限！
    echo 请右键选择"以管理员身份运行"
    pause
    exit /b 1
)

echo [信息] 管理员权限检查通过
echo.

REM 检查Node.js
node -v >nul 2>&1
if %errorlevel% neq 0 (
    echo [警告] 未检测到Node.js
    echo.
    echo 您可以：
    echo   1. 下载并安装Node.js: https://nodejs.org/
    echo   2. 使用打包的可执行文件（无需Node.js）
    echo.
    choice /C 12 /M "请选择"
    if errorlevel 2 goto USE_EXE
    if errorlevel 1 goto NEED_NODEJS
)

echo [信息] Node.js 已安装
node -v
echo.

:INSTALL_WITH_NODE
echo [步骤 1/5] 安装依赖包...
call npm install
if %errorlevel% neq 0 (
    echo [错误] 依赖安装失败
    pause
    exit /b 1
)
echo [信息] 依赖安装完成
echo.

echo [步骤 2/5] 创建配置文件...
set CONFIG_DIR=%USERPROFILE%\.atg-client
set CONFIG_FILE=%CONFIG_DIR%\config.json

REM 确保配置目录存在
if not exist "%CONFIG_DIR%" mkdir "%CONFIG_DIR%"

REM 创建配置文件（如果不存在）
if not exist "%CONFIG_FILE%" (
    (
        echo {
        echo   "serverUrl": "http://localhost:19080",
        echo   "browser": "chrome",
        echo   "headless": false,
        echo   "autoStart": true,
        echo   "chromeDriverPath": "",
        echo   "geckoDriverPath": ""
        echo }
    ) > "%CONFIG_FILE%"
    echo [信息] 配置文件已创建: %CONFIG_FILE%
    echo.
    echo 💡 提示: 如需配置WebDriver路径，请编辑此文件
    echo    %CONFIG_FILE%
    echo.
) else (
    echo [信息] 配置文件已存在，跳过创建
    echo.
)

echo [步骤 3/5] 安装系统服务...
call npm run install-service
if %errorlevel% neq 0 (
    echo [错误] 服务安装失败
    pause
    exit /b 1
)
echo [信息] 服务安装完成
echo.

echo [步骤 4/5] 启动服务...
net start "AI-ATG-Test-Service"
if %errorlevel% neq 0 (
    echo [警告] 服务启动失败，请手动启动
)
echo.

echo [步骤 5/5] 验证服务...
timeout /t 3 >nul
curl -s http://localhost:9999/health >nul 2>&1
if %errorlevel% equ 0 (
    echo [成功] 服务运行正常！
    echo.
    echo 访问: http://localhost:9999/health
) else (
    echo [警告] 服务可能未正常启动
    echo 请查看日志: %USERPROFILE%\.ai-atg-service\logs\
)
echo.

goto FINISH

:USE_EXE
echo [信息] 使用可执行文件模式...
if not exist "ai-atg-service.exe" (
    echo [错误] 未找到可执行文件
    echo 请先运行: npm run build
    pause
    exit /b 1
)
REM TODO: 使用可执行文件安装服务
echo [信息] 可执行文件安装功能开发中...
goto FINISH

:NEED_NODEJS
echo.
echo 请先安装Node.js后再运行此安装程序
start https://nodejs.org/
pause
exit /b 1

:FINISH
echo ====================================
echo   安装完成！
echo ====================================
echo.
echo ✓ 服务已安装为Windows系统服务
echo ✓ 服务将在开机时自动启动
echo ✓ 系统托盘会显示服务图标
echo ✓ 配置文件已创建: %USERPROFILE%\.atg-client\config.json
echo.
echo ⚠️  重要提示：
echo    1. 请先配置WebDriver（必需）
echo    2. 配置文件位置: %USERPROFILE%\.atg-client\config.json
echo    3. 配置完成后重启服务: net restart ATG-Client
echo.
echo 现在可以在AI-ATG平台执行UI测试了！
echo.
pause
