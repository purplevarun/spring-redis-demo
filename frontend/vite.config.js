import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    // proxy to backend when running locally outside Docker
    proxy: {
      '/api': 'http://localhost:8080'
    }
  }
})
