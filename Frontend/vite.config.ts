import path from "node:path";
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    react({
      babel: {
        plugins: [["babel-plugin-react-compiler"]],
      },
    }),
    tailwindcss(),
  ],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "src"),
      "@shared": path.resolve(__dirname, "src/shared"),
    },
  },

  // ✅ Android WebView 설정 추가
  base: './', // 상대 경로로 변경 (중요!)

  build: {
    sourcemap: true,
    outDir: 'dist', // 빌드 결과물 폴더
    assetsDir: 'assets', // 정적 파일 폴더

    // 단일 파일로 번들링 (WebView 최적화)
    rollupOptions: {
      output: {
        manualChunks: undefined,
      }
    },

    // 파일 크기 경고 제한 완화
    chunkSizeWarningLimit: 1000,
  },

  // 개발 서버 설정 (모바일에서 테스트 시)
  server: {
    host: '0.0.0.0', // 같은 네트워크의 모바일에서 접근 가능
    port: 5173,
  }
});