#!/usr/bin/env node

const path = require('path');
const fs = require('fs');

const SERVICE_NAME = 'ATG-Client';
const SERVICE_SCRIPT = path.join(__dirname, 'index.js');

async function installService() {
  const platform = process.platform;
  
  console.log('正在安装服务...');
  console.log(`平台: ${platform}`);
  
  try {
    if (platform === 'win32') {
      await installWindowsService();
    } else if (platform === 'darwin') {
      await installMacService();
    } else if (platform === 'linux') {
      await installLinuxService();
    } else {
      throw new Error(`不支持的平台: ${platform}`);
    }
    
    console.log('✓ 服务安装成功！');
    console.log('✓ 服务将在开机时自动启动');
    console.log('');
    console.log('现在可以：');
    console.log('  1. 重启电脑，服务会自动启动');
    console.log('  2. 或手动启动服务（见下方命令）');
    console.log('');
    
    if (platform === 'win32') {
      console.log('手动启动服务:');
      console.log(`  net start ${SERVICE_NAME}`);
    } else if (platform === 'darwin') {
      console.log('手动启动服务:');
      console.log(`  launchctl load ~/Library/LaunchAgents/com.atgclient.plist`);
    } else {
      console.log('手动启动服务:');
      console.log(`  sudo systemctl start atg-client`);
    }
    
  } catch (error) {
    console.error('✗ 服务安装失败:', error.message);
    process.exit(1);
  }
}

async function uninstallService() {
  const platform = process.platform;
  
  console.log('正在卸载服务...');
  
  try {
    if (platform === 'win32') {
      await uninstallWindowsService();
    } else if (platform === 'darwin') {
      await uninstallMacService();
    } else if (platform === 'linux') {
      await uninstallLinuxService();
    }
    
    console.log('✓ 服务卸载成功！');
    
  } catch (error) {
    console.error('✗ 服务卸载失败:', error.message);
    process.exit(1);
  }
}

// Windows 服务安装
async function installWindowsService() {
  const Service = require('node-windows').Service;
  
  const svc = new Service({
    name: SERVICE_NAME,
    description: 'AI-ATG UI自动化测试服务',
    script: SERVICE_SCRIPT,
    nodeOptions: []
  });
  
  return new Promise((resolve, reject) => {
    svc.on('install', () => {
      svc.start();
      resolve();
    });
    
    svc.on('error', reject);
    
    svc.install();
  });
}

async function uninstallWindowsService() {
  const Service = require('node-windows').Service;
  
  const svc = new Service({
    name: SERVICE_NAME,
    script: SERVICE_SCRIPT
  });
  
  return new Promise((resolve, reject) => {
    svc.on('uninstall', resolve);
    svc.on('error', reject);
    svc.uninstall();
  });
}

// macOS 服务安装
async function installMacService() {
  const plistContent = `<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>Label</key>
    <string>com.atgclient</string>
    <key>ProgramArguments</key>
    <array>
        <string>${process.execPath}</string>
        <string>${SERVICE_SCRIPT}</string>
    </array>
    <key>RunAtLoad</key>
    <true/>
    <key>KeepAlive</key>
    <true/>
    <key>StandardOutPath</key>
    <string>${require('os').homedir()}/Library/Logs/atg-client.log</string>
    <key>StandardErrorPath</key>
    <string>${require('os').homedir()}/Library/Logs/atg-client-error.log</string>
</dict>
</plist>`;
  
  const plistPath = path.join(require('os').homedir(), 'Library', 'LaunchAgents', 'com.atgclient.plist');
  
  // 确保目录存在
  const dir = path.dirname(plistPath);
  if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir, { recursive: true });
  }
  
  fs.writeFileSync(plistPath, plistContent);
  
  // 修复 node-systray 权限（macOS特定问题）
  try {
    const systrayCache = path.join(require('os').homedir(), '.cache', 'node-systray');
    if (fs.existsSync(systrayCache)) {
      console.log('正在修复系统托盘权限...');
      require('child_process').execSync(`chmod -R +x ${systrayCache}`);
    }
  } catch (err) {
    console.warn('警告: 无法修复系统托盘权限，可能需要手动处理');
  }
  
  // 加载服务
  require('child_process').execSync(`launchctl load ${plistPath}`);
}

async function uninstallMacService() {
  const plistPath = path.join(require('os').homedir(), 'Library', 'LaunchAgents', 'com.atgclient.plist');
  
  if (fs.existsSync(plistPath)) {
    require('child_process').execSync(`launchctl unload ${plistPath}`);
    fs.unlinkSync(plistPath);
  }
}

// Linux 服务安装
async function installLinuxService() {
  const serviceContent = `[Unit]
Description=ATG-Client UI Test Service
After=network.target

[Service]
Type=simple
User=${process.env.USER}
ExecStart=${process.execPath} ${SERVICE_SCRIPT}
Restart=always
RestartSec=10
StandardOutput=syslog
StandardError=syslog
SyslogIdentifier=atg-client

[Install]
WantedBy=multi-user.target
`;
  
  const servicePath = '/etc/systemd/system/atg-client.service';
  
  fs.writeFileSync(servicePath, serviceContent);
  
  require('child_process').execSync('systemctl daemon-reload');
  require('child_process').execSync('systemctl enable atg-client');
}

async function uninstallLinuxService() {
  require('child_process').execSync('systemctl stop atg-client');
  require('child_process').execSync('systemctl disable atg-client');
  
  const servicePath = '/etc/systemd/system/atg-client.service';
  if (fs.existsSync(servicePath)) {
    fs.unlinkSync(servicePath);
  }
  
  require('child_process').execSync('systemctl daemon-reload');
}

// 命令行处理
const command = process.argv[2];

if (command === 'install') {
  installService();
} else if (command === 'uninstall') {
  uninstallService();
} else {
  console.log('用法:');
  console.log('  node installer.js install   - 安装服务');
  console.log('  node installer.js uninstall - 卸载服务');
  process.exit(1);
}
