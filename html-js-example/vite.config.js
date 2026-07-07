import { defineConfig } from 'vite'
import { resolve } from 'path'

export default defineConfig({
  build: {
    rollupOptions: {
      input: {
        landing: resolve(__dirname, 'landing-page.html'),
        pdp: resolve(__dirname, 'pdp.html'),
        modal: resolve(__dirname, 'modal.html'),
      },
    },
  },
  server: {
    port: 3000,
    open: '/landing-page.html',
  },
  preview: {
    port: 3000,
    open: '/landing-page.html',
  },
})
