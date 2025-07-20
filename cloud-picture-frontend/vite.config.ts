import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  css: {
    preprocessorOptions: {
      less: {
        javascriptEnabled: true,
        modifyVars: {
          // 全局主题色调
          'primary-color': '#2564eb',
          'link-color': '#2564eb',
          'success-color': '#10b981',
          'warning-color': '#f59e0b',
          'error-color': '#ef4444',
          // 圆角
          'border-radius-base': '6px',
          // 字体
          'font-family': '"SF Pro Display", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Oxygen, Ubuntu, Cantarell, "Open Sans", "Helvetica Neue", sans-serif',
        },
      },
    },
  },
  build: {
    target: 'es2015',
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true, // 生产环境删除console
        drop_debugger: true // 生产环境删除debugger
      }
    },
    chunkSizeWarningLimit: 1500, // 提高代码分块大小警告限制
    rollupOptions: {
      output: {
        manualChunks: {
          'vendor': ['vue', 'vue-router', 'pinia'],
          'ant-design': ['ant-design-vue'],
          'echarts': ['echarts', 'vue-echarts', 'echarts-wordcloud']
        }
      }
    }
  },
  server: {
    hmr: true, // 开启热更新
    port: 5173,
    open: true,
    cors: true,
    host: '0.0.0.0'
  },
  preview: {
    port: 8080
  },
  optimizeDeps: {
    include: [
      'vue',
      'vue-router',
      'pinia',
      'ant-design-vue/es',
      '@ant-design/icons-vue',
      'axios'
    ]
  },
})
