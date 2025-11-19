import { useEffect, useRef } from "react";
import { EquippedItem } from "@/entities/showroom/model/type";
import { ENV } from "@/shared/config/env";
import { normalizePath } from "@/entities/showroom/api/renderer";

interface Props {
  items: EquippedItem[];
  animation?: "walk" | "run";
  direction?: 0 | 1 | 2 | 3;
  frameDelay?: number;
}

export const LPCCharacterRenderer = ({
  items = [],
  animation = "walk",
  direction = 2,
  frameDelay = 150,
}: Props) => {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const frameRef = useRef(0);

  function resolvePath(path: string) {
    return normalizePath(path, animation);
  }

  function getClothesLayer(sub: string) {
    if (["tshirt", "longsleeve", "shortsleeves"].includes(sub)) return "clothes_top";
    if (["shorts"].includes(sub)) return "clothes_bottom";
    if (["shoes", "boots"].includes(sub)) return "clothes_shoes";
    return "clothes";
  }

  function getLayerName(item: EquippedItem) {
    if (item.category === "clothes" && item.subcategory) return getClothesLayer(item.subcategory);
    if (item.category === "head") return item.subcategory || "head";
    return item.category || "unknown";
  }

  const LAYER_ORDER = [
    "bodies",
    "clothes_bottom",
    "clothes_top",
    "clothes_shoes",

    "heads",
    "faces",
    "nose",
    "eyes",
    "eyebrows",
    "ears",

    "hair",
  ];

  async function loadImage(path: string) {
    return new Promise<HTMLImageElement>((resolve, reject) => {
      const img = new Image();
      img.src = `${ENV.ASSETS_ORIGIN}/${path}`;
      img.onload = () => resolve(img);
      img.onerror = (err) => reject(err);
    });
  }

  useEffect(() => {
    let alive = true;

    console.log("🎨 LPCCharacterRenderer 실행");
    console.log("받은 아이템 개수:", items.length);
    if (items.length > 0) {
      console.log("첫 번째 아이템:", items[0]);
      console.log("spriteType:", items[0].spriteType);
    }

    // COMPLETE 타입 체크: 첫 번째 아이템의 spriteType이 COMPLETE인 경우
    const isCompleteType = items.length > 0 && items[0].spriteType === "COMPLETE";
    console.log("isCompleteType:", isCompleteType);

    async function render() {
      // COMPLETE 타입: 단일 스프라이트시트 사용
      if (isCompleteType) {
        console.log("=== COMPLETE 타입 감지 ===");
        console.log("전체 아이템 정보:", JSON.stringify(items[0], null, 2));
        console.log("basePath:", items[0].basePath);
        console.log("ENV.ASSETS_ORIGIN:", ENV.ASSETS_ORIGIN);
        
        try {
          // COMPLETE 타입은 basePath에 완성된 스프라이트 경로가 들어옴
          // 역슬래시를 슬래시로 변환하고 앞 슬래시 제거
          let completePath = items[0].basePath
            .replace(/\\/g, "/")
            .replace(/^\/+/, "");
          
          console.log("정규화된 경로:", completePath);
          console.log("최종 URL:", `${ENV.ASSETS_ORIGIN}/${completePath}`);
          
          const img = await loadImage(completePath);
          console.log("COMPLETE 스프라이트 로드 성공!", img);

          function drawCompleteFrame() {
            if (!alive) return;

            const canvas = canvasRef.current;
            const ctx = canvas?.getContext("2d");
            if (!canvas || !ctx) return;

            canvas.width = 64;
            canvas.height = 64;
            ctx.clearRect(0, 0, 64, 64);

            const frame = frameRef.current;
            const fx = frame * 64;
            const fy = direction * 64;

            ctx.drawImage(img, fx, fy, 64, 64, 0, 0, 64, 64);

            frameRef.current = (frame + 1) % 9;
            setTimeout(drawCompleteFrame, frameDelay);
          }

          drawCompleteFrame();
        } catch (e) {
          console.error("COMPLETE 스프라이트 로드 실패:", e);
          console.error("아이템 정보:", items[0]);
        }
        return;
      }

      // COMPOSITE 타입: 기존 방식 (여러 레이어 합성)
      const layers: { layer: string; img: HTMLImageElement }[] = [];

      for (const item of items) {
        try {
          const img = await loadImage(resolvePath(item.basePath));
          layers.push({ layer: getLayerName(item), img });
        } catch (e) {
          console.warn("로드 실패:", item);
        }
      }

      function drawFrame() {
        if (!alive) return;

        const canvas = canvasRef.current;
        const ctx = canvas?.getContext("2d");
        if (!canvas || !ctx) return;

        canvas.width = 64;
        canvas.height = 64;
        ctx.clearRect(0, 0, 64, 64);

        const frame = frameRef.current;
        const fx = frame * 64;
        const fy = direction * 64;

        for (const layer of LAYER_ORDER) {
          const found = layers.find((l) => l.layer === layer);
          if (!found) continue;

          ctx.drawImage(found.img, fx, fy, 64, 64, 0, 0, 64, 64);
        }

        frameRef.current = (frame + 1) % 9;
        setTimeout(drawFrame, frameDelay);
      }

      drawFrame();
    }

    render();
    return () => {
      alive = false;
    };
  }, [items, animation, direction, frameDelay]);

  return <canvas ref={canvasRef} style={{ width: 128, height: 128 }} />;
};
