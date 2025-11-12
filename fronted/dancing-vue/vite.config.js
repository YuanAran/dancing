import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import fs from 'fs'
import path from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  define: {
    global: 'globalThis',
  },
  optimizeDeps: {
    include: ['stompjs', 'sockjs-client'],
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    https: {
      key: fs.readFileSync(path.resolve(__dirname, 'server.key')),
      cert: fs.readFileSync(path.resolve(__dirname, 'cert.pem')),
    },
    host: '0.0.0.0', // 允许局域网访问
    port: 5173,       // 前端启动端口
    proxy: {
      '/api': {
        target: 'https://192.168.1.113:8080', // 后端 Spring Boot 地址
        changeOrigin: true,
        secure: false, // 后端是自签名证书时要设为 false
      },
    },
  },
})
