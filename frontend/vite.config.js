import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  base: '/', // 前端独立部署，使用根路径
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: false,
    minify: 'terser'
  },
  server: {
    port: 3000,
    // 允许通过 ngrok 等代理的外网域名访问，否则会返回 403
    allowedHosts: true,
    proxy: {
      '/api': {
        target: 'http://localhost:19080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '/agt/api')
        // 将 /api 重写为 /agt/api，匹配后端新的 context-path
      },
      '/downloads': {
        target: 'http://localhost:19080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/downloads/, '/agt/api/downloads')
        // 将 /downloads 重写为 /agt/api/downloads，匹配后端新的 context-path
      }
    }
  }
})
