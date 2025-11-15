// src/entities/showroom/ui/LPCPreviewRenderer.tsx
import { useEffect, useRef } from "react";
import { ENV } from "@/shared/config/env";
import { normalizePath } from "@/entities/showroom/api/renderer";

interface Props {
  basePath: string;
}

export const LPCPreviewRenderer = ({ basePath }: Props) => {
  const canvasRef = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    if (!basePath) return;

    const img = new Image();
    img.src = `${ENV.ASSETS_ORIGIN}/${normalizePath(basePath, "walk")}`;

    img.onload = () => {
      const canvas = canvasRef.current;
      if (!canvas) return;

      const ctx = canvas.getContext("2d")!;
      canvas.width = 64;
      canvas.height = 64;

      const frameX = 0;
      const frameY = 2 * 64; // 정면
      ctx.drawImage(img, frameX, frameY, 64, 64, 0, 0, 64, 64);
    };
  }, [basePath]);

  return <canvas ref={canvasRef} className="h-16 w-16 rounded-lg" />;
};
