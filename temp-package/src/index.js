#!/usr/bin/env node

const express = require('express');
const cors = require('cors');
const path = require('path');
const fs = require('fs');
const TestExecutor = require('./executor');
const TrayManager = require('./tray');
const notifier = require('node-notifier');

const app = express();
const PORT = 9999;

// 配置文件路径
const CONFIG_DIR = path.join(require('os').homedir(), '.atg-client');
const CONFIG_FILE = path.join(CONFIG_DIR, 'config.json');

// 确保配置目录存在
if (!fs.existsSync(CONFIG_DIR)) {
  fs.mkdirSync(CONFIG_DIR, { recursive: true });
}

// 默认配置
const defaultConfig = {
  serverUrl: 'http://localhost:19080',
  browser: 'chrome',
  headless: false,
  autoStart: true,
  chromeDriverPath: '',  // ChromeDriver 路径，需要用户手动配置
  geckoDriverPath: ''    // GeckoDriver 路径，需要用户手动配置
};

// 加载配置
let config = { ...defaultConfig };

if (fs.existsSync(CONFIG_FILE)) {
  try {
    config = { ...config, ...JSON.parse(fs.readFileSync(CONFIG_FILE, 'utf8')) };
  } catch (e) {
    console.error('配置文件加载失败，使用默认配置');
  }
} else {
  // 配置文件不存在，自动创建
  try {
    fs.writeFileSync(CONFIG_FILE, JSON.stringify(defaultConfig, null, 2));
    console.log('配置文件已自动创建:', CONFIG_FILE);
  } catch (e) {
    console.error('配置文件创建失败:', e.message);
  }
}

// 保存配置
function saveConfig() {
  fs.writeFileSync(CONFIG_FILE, JSON.stringify(config, null, 2));
}

// 中间件
app.use(cors());
app.use(express.json({ limit: '50mb' }));

// 测试执行器
const executor = new TestExecutor(config);

// 当前执行状态
let currentExecution = null;

// 健康检查
app.get('/health', (req, res) => {
  res.json({
    status: 'ok',
    version: '1.0.0',
    config: {
      browser: config.browser,
      headless: config.headless
    },
    executing: currentExecution !== null
  });
});

// 获取配置
app.get('/config', (req, res) => {
  res.json(config);
});

// 更新配置
app.post('/config', (req, res) => {
  config = { ...config, ...req.body };
  saveConfig();
  res.json({ success: true, config });
});

// 执行测试
app.post('/execute', async (req, res) => {
  const { testCase } = req.body;
  
  if (currentExecution) {
    return res.status(409).json({
      success: false,
      error: '已有测试正在执行中'
    });
  }
  
  currentExecution = {
    testCaseId: testCase.id,
    testCaseTitle: testCase.title,
    startTime: Date.now(),
    result: null
  };
  
  // 发送通知
  notifier.notify({
    title: 'ATG-Client',
    message: `开始执行测试：${testCase.title}`,
    icon: path.join(__dirname, '../icons/icon.png'),
    sound: false
  });
  
  try {
    // 解析测试步骤（如果是字符串，则解析为 JSON）
    let steps = testCase.steps;
    if (typeof steps === 'string') {
      try {
        steps = JSON.parse(steps);
      } catch (e) {
        console.error('解析测试步骤失败:', e);
        currentExecution = null;
        return res.status(400).json({
          success: false,
          error: '测试步骤格式错误'
        });
      }
    }
    
    // 构建执行数据
    const executionData = {
      ...testCase,
      steps: steps
    };
    
    // 异步执行测试
    executor.execute(executionData).then(result => {
      // 保存结果到 currentExecution
      if (currentExecution) {
        currentExecution.result = result;
        currentExecution.completedTime = Date.now();
      }
      
      // 立即上传结果到服务器
      uploadResult(testCase.executionId, testCase.id, result)
        .then(() => {
          console.log('结果已上传到服务器');
          // 上传成功后清空
          setTimeout(() => {
            currentExecution = null;
          }, 5000);
        })
        .catch(err => {
          console.error('上传结果失败:', err);
          // 上传失败也清空，避免影响下次执行
          setTimeout(() => {
            currentExecution = null;
          }, 5000);
        });
      
      // 发送完成通知
      notifier.notify({
        title: 'ATG-Client',
        message: `测试完成：${testCase.title} - ${result.status === 'passed' ? '✓ 通过' : '✗ 失败'}`,
        icon: path.join(__dirname, '../icons/icon.png'),
        sound: true
      });
    }).catch(error => {
      console.error('测试执行失败:', error);
      
      // 构建错误结果
      const errorResult = {
        status: 'failed',
        duration: Date.now() - currentExecution.startTime,
        logs: `执行失败: ${error.message}`,
        errorMessage: error.message,
        screenshot: null
      };
      
      // 保存错误结果
      if (currentExecution) {
        currentExecution.result = errorResult;
        currentExecution.completedTime = Date.now();
      }
      
      // 立即上传错误结果
      uploadResult(testCase.executionId, testCase.id, errorResult)
        .then(() => {
          console.log('错误结果已上传到服务器');
          setTimeout(() => {
            currentExecution = null;
          }, 5000);
        })
        .catch(err => {
          console.error('上传错误结果失败:', err);
          setTimeout(() => {
            currentExecution = null;
          }, 5000);
        });
      
      notifier.notify({
        title: 'ATG-Client',
        message: `测试失败：${testCase.title}`,
        icon: path.join(__dirname, '../icons/icon.png'),
        sound: true
      });
    });
    
    res.json({
      success: true,
      message: '测试已开始执行'
    });
    
  } catch (error) {
    currentExecution = null;
    res.status(500).json({
      success: false,
      error: error.message
    });
  }
});

// 获取执行状态
app.get('/status', (req, res) => {
  if (currentExecution && currentExecution.result) {
    // 执行已完成，返回结果
    res.json({
      executing: false,
      completed: true,
      current: currentExecution,
      result: currentExecution.result
    });
  } else if (currentExecution) {
    // 执行中
    res.json({
      executing: true,
      completed: false,
      current: currentExecution
    });
  } else {
    // 无执行任务
    res.json({
      executing: false,
      completed: false,
      current: null
    });
  }
});

// 停止当前测试
app.post('/stop', async (req, res) => {
  if (currentExecution) {
    await executor.stop();
    currentExecution = null;
    res.json({ success: true, message: '测试已停止' });
  } else {
    res.json({ success: false, message: '当前没有正在执行的测试' });
  }
});

// 上传结果到服务器
async function uploadResult(executionId, testCaseId, result) {
  try {
    const fetch = (await import('node-fetch')).default;
    
    const payload = {
      executionId: executionId,
      testCaseId: testCaseId,
      status: result.status,
      duration: result.duration,
      logs: result.logs,
      errorMessage: result.errorMessage || null,
      screenshot: result.screenshot || null
    };
    
    console.log('正在上传结果到服务器:', config.serverUrl);
    
    const response = await fetch(`${config.serverUrl}/api/atg-client/callback/result`, {
      method: 'POST',
      headers: { 
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      },
      body: JSON.stringify(payload),
      timeout: 10000
    });
    
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }
    
    const data = await response.json();
    console.log('结果上传成功:', data);
    return data;
  } catch (error) {
    console.error('上传结果失败:', error.message);
    throw error;
  }
}

// 启动服务器
const server = app.listen(PORT, '127.0.0.1', () => {
  console.log('=====================================');
  console.log('  ATG-Client 已启动');
  console.log('=====================================');
  console.log(`  地址: http://localhost:${PORT}`);
  console.log(`  浏览器: ${config.browser}`);
  console.log(`  无头模式: ${config.headless}`);
  console.log('=====================================');
  
  // 发送启动通知
  notifier.notify({
    title: 'ATG-Client',
    message: '服务已启动，可以开始执行UI测试',
    icon: path.join(__dirname, '../icons/icon.png'),
    sound: false
  });
});

// 创建系统托盘图标
const tray = new TrayManager({
  onExit: () => {
    console.log('正在关闭服务...');
    server.close();
    process.exit(0);
  },
  onShowConfig: () => {
    console.log('配置:', config);
  }
});

// 优雅关闭
process.on('SIGINT', () => {
  console.log('\n正在关闭服务...');
  server.close();
  process.exit(0);
});

process.on('SIGTERM', () => {
  console.log('\n正在关闭服务...');
  server.close();
  process.exit(0);
});
