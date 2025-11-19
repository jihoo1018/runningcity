import path from "node:path";
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";
import svgr from "vite-plugin-svgr";

export default defineConfig({
  plugins: [
    react({
      babel: {
        plugins: [["babel-plugin-react-compiler"]],
      },
    }),
    svgr({
      svgrOptions: {
        exportType: "named",
        ref: true,
        titleProp: true,
      },
      include: "**/*.svg",
    }),
    tailwindcss(),
  ],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "src"),
      "@shared": path.resolve(__dirname, "src/shared"),
    },
  },

  base: "./", // ✅ WebView용 상대 경로 유지

  build: {
    sourcemap: true,
    outDir: "dist",
    assetsDir: "assets",
    rollupOptions: {
      output: {
        manualChunks: undefined,
      },
    },
    chunkSizeWarningLimit: 1000,
  },

  // ✅ Android WebView 호환 설정
  server: {
    host: "0.0.0.0", // ✅ 모든 네트워크 IP에서 접근 허용
    port: 5173,
    strictPort: true,
    cors: true,

    allowedHosts: [
      "nonconjunctive-cami-outdoor.ngrok-free.dev",
      "localhost",
      "70.12.247.131", // 각자의 IP 주소로 수정
      "10.0.2.2",
    ],
    hmr: {
      protocol: "ws",
      host: "70.12.247.131", // 각자의 IP 주소로 수정
      clientPort: 5173,
    },
    headers: {
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Methods": "GET,POST,PUT,DELETE,PATCH,OPTIONS",
      "Access-Control-Allow-Headers": "*",
      "Cache-Control": "no-store",
    },

        // ✅ 추가: 프록시 설정 (공인 IP에서 같은 오리진처럼 보이게)
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
        // /api → /api/v1 로 매핑
        rewrite: (p) => p.replace(/^\/api(?!\/v1)/, "/api/v1"),
      },
    },
  },
});
  // },

  






  // server: {
  //   allowedHosts: ["nonconjunctive-cami-outdoor.ngrok-free.dev"],
  //   host: true, // ✅ 외부 접속 허용 (기본적으로 localhost만 허용)
  //   // host: '0.0.0.0', // 같은 네트워크의 모바일에서 접근 가능
  //   port: 5173,
  //   proxy: {
  //     "/api": {
  //       target: "http://localhost:8080", // 백엔드 프록시
  //       changeOrigin: true,
  //     },
  //   },
  // },
// });
