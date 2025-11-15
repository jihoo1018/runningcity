// src/entities/showroom/api/renderer.ts
export function normalizePath(basePath: string, animation: string) {
  if (!basePath) return "";

  let path = basePath
    .replace(/\\/g, "/") // 역슬래시 → 슬래시
    .replace("{animation}", animation);

  // 앞 슬래시 제거
  if (path.startsWith("/")) path = path.slice(1);

  return path;
}
