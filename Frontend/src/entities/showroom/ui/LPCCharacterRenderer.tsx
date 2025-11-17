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
    if (item.category === "clothes") return getClothesLayer(item.subcategory);
    if (item.category === "head") return item.subcategory;
    return item.category;
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

    async function render() {
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
