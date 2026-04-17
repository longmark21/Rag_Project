import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000,
    proxy: {
      '/api/v1': {
        target: 'http://localhost:8082',
        changeOrigin: true,
        secure: false
      }
    }
  }
})