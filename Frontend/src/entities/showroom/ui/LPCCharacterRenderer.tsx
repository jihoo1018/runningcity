//src\entities\showroom\ui\LPCCharacterRenderer.tsx
import { useEffect, useRef } from "react";
import { EquippedItem } from "@/entities/showroom/model/type";
import { ENV } from "@/shared/config/env";

interface Props {
  items: EquippedItem[]; // 착용 중인 아이템들
  animation?: "walk" | "run"; // 현재 애니메이션
  direction?: 0 | 1 | 2 | 3; // 남(0), 서(1), 동(2), 북(3)
  frameDelay?: number; // 프레임 변경 속도
}

export const LPCCharacterRenderer = ({
  items,
  animation = "walk",
  direction = 0,
  frameDelay = 150,
}: Props) => {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const frameRef = useRef(0);

  /** 🔧 DB basePath → 실제 사용 경로로 변환 */
  function resolvePath(basePath: string) {
    return basePath.replace("{animation}", animation).replace(/\\/g, "/");
  }

  /** 🧱 레이어 순서 정의 */
  const LAYER_ORDER = ["bodies", "clothes", "ears", "eyebrows", "eyes", "nose", "faces", "hair"];

  /** 🧩 head 파츠는 subcategory 기준으로 레이어 분리 */
  function getLayerName(item: EquippedItem) {
    if (item.category !== "head") return item.category;
    return item.subcategory; // eyes, eyebrows, faces 등
  }

  /** 🖼️ 이미지 로드 */
  async function loadImage(path: string): Promise<HTMLImageElement> {
    return new Promise((resolve, reject) => {
      const img = new Image();
      img.src = `${ENV.ASSETS_ORIGIN}/${path}`;
      img.onload = () => resolve(img);
      img.onerror = reject;
    });
  }

  /** 🎨 렌더링 */
  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext("2d")!;
    canvas.width = 64;
    canvas.height = 64;

    let active = true;

    async function render() {
      /** 1) 이미지 로드 */
      const loadedLayers: { layer: string; img: HTMLImageElement }[] = [];

      for (const item of items) {
        const path = resolvePath(item.basePath);
        const img = await loadImage(path);
        loadedLayers.push({
          layer: getLayerName(item),
          img: img,
        });
      }

      /** 2) 애니메이션 루프 */
      async function drawFrame() {
        if (!active) return;

        ctx.clearRect(0, 0, 64, 64);

        const frame = frameRef.current;
        const frameX = frame * 64;
        const frameY = direction * 64;

        /** 3) 레이어 순서대로 그리기 */
        for (const layerName of LAYER_ORDER) {
          const layer = loadedLayers.find((l) => l.layer === layerName);
          if (!layer) continue;

          ctx.drawImage(
            layer.img,
            frameX,
            frameY,
            64,
            64, // source
            0,
            0,
            64,
            64, // destination
          );
        }

        /** 4) 다음 프레임 */
        frameRef.current = (frame + 1) % 9;

        setTimeout(drawFrame, frameDelay);
      }

      drawFrame();
    }

    render();

    return () => {
      active = false;
    };
  }, [items, animation, direction, frameDelay]);

  return <canvas ref={canvasRef} style={{ width: 128, height: 128 }} />;
};
