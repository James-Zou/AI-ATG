const path = require('path');

class TrayManager {
  constructor(options = {}) {
    this.options = options;
    this.tray = null;
    
    try {
      const SysTray = require('systray2').default;
      
      this.tray = new SysTray({
        menu: {
          icon: path.join(__dirname, '../icons/icon.png'),
          title: 'AI-ATG',
          tooltip: 'AI-ATG 测试服务',
          items: [
            {
              title: 'AI-ATG 测试服务',
              tooltip: '正在运行',
              enabled: false
            },
            {
              title: '',
              enabled: false
            },
            {
              title: '打开配置',
              tooltip: '查看配置',
              checked: false,
              enabled: true
            },
            {
              title: '访问控制台',
              tooltip: '打开浏览器控制台',
              checked: false,
              enabled: true
            },
            {
              title: '',
              enabled: false
            },
            {
              title: '退出',
              tooltip: '停止服务',
              checked: false,
              enabled: true
            }
          ]
        },
        debug: false,
        copyDir: true
      });
      
      this.tray.onClick(action => {
        if (action.seq_id === 2) { // 打开配置
          if (this.options.onShowConfig) {
            this.options.onShowConfig();
          }
        } else if (action.seq_id === 3) { // 访问控制台
          require('child_process').exec('open http://localhost:9999');
        } else if (action.seq_id === 5) { // 退出
          if (this.options.onExit) {
            this.options.onExit();
          }
        }
      });
      
      console.log('系统托盘图标已创建');
    } catch (e) {
      console.log('系统托盘初始化失败，继续运行（无托盘图标）');
    }
  }
  
  destroy() {
    if (this.tray) {
      this.tray.kill();
    }
  }
}

module.exports = TrayManager;
