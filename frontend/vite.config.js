import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

export default defineConfig({
	plugins: [react()],
	server: {
		// proxy to backend when running locally outside Docker
		proxy: {
			'/api': 'http://localhost:8080'
		}
	}
})
