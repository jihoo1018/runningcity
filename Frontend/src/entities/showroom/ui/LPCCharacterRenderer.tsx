// src/entities/showroom/ui/LPCCharacterRenderer.tsx
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
  items = [], // 배열 null 보호
  animation = "walk",
  direction = 2,
  frameDelay = 150,
}: Props) => {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const frameRef = useRef(0);

  /** 🎨 경로 */
  function resolvePath(basePath: string) {
    return normalizePath(basePath, animation);
  }

  /** 🧱 clothes 상의/하의/신발 레이어 분리 */
  function getClothesLayer(sub: string) {
    if (["tshirt", "longsleeve", "shortsleeves"].includes(sub)) return "clothes_top";
    if (["shorts"].includes(sub)) return "clothes_bottom";
    if (["shoes", "boots"].includes(sub)) return "clothes_shoes";
    return "clothes";
  }

  /** 🎨 RENDER 레이어 이름 */
  function getLayerName(item: EquippedItem) {
    if (item.category === "clothes") return getClothesLayer(item.subcategory);
    if (item.category === "head") return item.subcategory; // faces/eyes/eyebrows...
    return item.category; // bodies, hair
  }

  /** 🖼 이미지 로드 */
  async function loadImage(path: string): Promise<HTMLImageElement> {
    return new Promise((resolve, reject) => {
      const img = new Image();
      img.src = `${ENV.ASSETS_ORIGIN}/${path}`;

      img.onload = () => resolve(img);
      img.onerror = (err) => {
        console.error("❌ 이미지 로드 실패:", img.src);
        reject(err);
      };
    });
  }

  /** 🧱 레이어 순서 (Z-index) */
  const LAYER_ORDER = [
    "bodies",
    "clothes_bottom",
    "clothes_top",
    "clothes_shoes",
    // 얼굴 기본(머리형)
    "heads",
    // 세부 얼굴 요소
    "faces",
    "nose",
    "eyes",
    "eyebrows",
    "ears",
    // 맨 앞
    "hair",
  ];

  /** 🎮 렌더링 */
  useEffect(() => {
    if (!Array.isArray(items)) return;

    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext("2d");
    if (!ctx) return;

    canvas.width = 64;
    canvas.height = 64;

    let active = true;

    async function render() {
      const loadedLayers: { layer: string; img: HTMLImageElement }[] = [];

      for (const item of items) {
        if (!item) continue;

        const path = resolvePath(item.basePath);
        try {
          const img = await loadImage(path);
          loadedLayers.push({ layer: getLayerName(item), img });
        } catch (e) {
          console.warn("로드 실패한 아이템:", item);
        }
      }
      function drawFrame() {
        if (!active) return;

        const canvas = canvasRef.current;
        if (!canvas) return;

        const ctx2 = canvas.getContext("2d");
        if (!ctx2) return; // ← 안전장치

        ctx2.clearRect(0, 0, 64, 64);

        const frame = frameRef.current;
        const frameX = frame * 64;
        const frameY = direction * 64;

        for (const layer of LAYER_ORDER) {
          const found = loadedLayers.find((l) => l.layer === layer);
          if (!found) continue;

          ctx2.drawImage(found.img, frameX, frameY, 64, 64, 0, 0, 64, 64);
        }

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
