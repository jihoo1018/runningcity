/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_BASE?: string;
  // 사용 예정 env를 미리 정의
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
