// src/entities/showroom/ui/LPCPreviewRenderer.tsx
import { useEffect, useRef } from "react";
import { ENV } from "@/shared/config/env";

interface Props {
  basePath: string;
}

export const LPCPreviewRenderer = ({ basePath }: Props) => {
  const canvasRef = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    if (!basePath) return;

    const img = new Image();
    img.src = `${ENV.ASSETS_ORIGIN}/${basePath.replace(/\\/g, "/").replace("{animation}", "walk")}`;

    img.onload = () => {
      const canvas = canvasRef.current;
      if (!canvas) return;
      const ctx = canvas.getContext("2d")!;

      canvas.width = 64;
      canvas.height = 64;

      // 스프라이트 시트 첫 프레임(0,0)만 잘라서 그림
      ctx.drawImage(img, 0, 0, 64, 64, 0, 0, 64, 64);
    };
  }, [basePath]);

  return <canvas ref={canvasRef} className="h-16 w-16 rounded-lg sm:h-20 sm:w-20" />;
};
