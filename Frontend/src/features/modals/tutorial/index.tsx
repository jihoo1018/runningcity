// src/features/modals/tutorial/index.tsx
import { useState, useEffect, useRef } from "react";
import { ModalProps } from "@/app/modal/types";
import { Modal } from "@/shared/ui";
import { CommonButton } from "@/shared/ui";
import { ENV } from "@/shared/config/env";

// 튜토리얼 슬라이드 타입
type TutorialSlide = {
  slideId: number;
  title: string;
  description: string;
  scene: "welcome" | "city" | "enemy" | "nature";
  isLastSlide?: boolean;
};

// 튜토리얼 데이터
const tutorialData: TutorialSlide[] = [
  // 1) 세계관
  {
    slideId: 1,
    title: "러닝시티 소개",
    description:
      "러닝시티는 사람들이 달릴 때\n 생성되는 에너지로 운영되는 도시입니다.\n최근 에너지 부족으로\n 도시가 약해지고 있습니다.",
    scene: "city",
  },
  // 2) 위협과 갈등
  {
    slideId: 2,
    title: "STATIC의 방해",
    description:
      "STATIC 조직의 공격으로\n 시민들이 달리기를 멈추며\n 도시의 에너지가 줄고 있습니다.",
    scene: "enemy",
  },
  // 3) 내 역할
  {
    slideId: 3,
    title: "당신의 역할",
    description: "당신의 달리기가 러닝시티를 회복시키는\n 핵심 에너지입니다.",
    scene: "welcome",
  },
  // 4) 달리면 생기는 효과
  {
    slideId: 4,
    title: "달리면 변하는 것",
    description: "당신이 달릴 때마다 에너지가 생성되고,\n도시는 다시 활성화됩니다.",
    scene: "nature",
  },
  // 5) 보상 시스템
  {
    slideId: 5,
    title: "미션과 보상",
    description:
      "일일 미션으로 특별 보상을 얻고,\n1km마다 데이터 칩을 수집해 추가 정보를 해제할 수 있습니다.",
    scene: "city",
  },
  // 6) 친구·경쟁 기능
  {
    slideId: 6,
    title: "함께 달리기",
    description: "친구를 추가해 서로의 활동을 비교하고,\n랭킹으로 경쟁을 즐겨보세요.",
    scene: "nature",
  },
  // 7) 시작 안내
  {
    slideId: 7,
    title: "준비 완료",
    description: "이제 러닝시티를 위한\n 러닝을 시작할 준비가 되었습니다.",
    scene: "welcome",
    isLastSlide: true,
  },
];

const HAMSTER_IMAGE_URL = `${ENV.ASSETS_ORIGIN}/ham.png`;

export default function TutorialModal({ onClose }: ModalProps) {
  const [currentSlide, setCurrentSlide] = useState(0);
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const animFrameRef = useRef(0);
  const animationIdRef = useRef<number | undefined>(undefined);

  const slide = tutorialData[currentSlide];
  const progress = ((currentSlide + 1) / tutorialData.length) * 100;

  const sceneTheme = {
    welcome: {
      gradient: "from-[#1a1f4d] via-[#121632] to-[#080a16]",
      glow: "shadow-[0_0_35px_rgba(0,255,255,0.35)]",
      accent: "text-primary",
    },
    city: {
      gradient: "from-[#112130] via-[#09111f] to-[#020509]",
      glow: "shadow-[0_0_40px_rgba(0,136,255,0.35)]",
      accent: "text-[#77e0ff]",
    },
    enemy: {
      gradient: "from-[#2b0a18] via-[#12030b] to-[#060104]",
      glow: "shadow-[0_0_40px_rgba(255,69,122,0.4)]",
      accent: "text-[#ff5b8d]",
    },
    nature: {
      gradient: "from-[#0d2f27] via-[#061b15] to-[#020705]",
      glow: "shadow-[0_0_35px_rgba(0,255,170,0.3)]",
      accent: "text-[#6df7c5]",
    },
  }[slide.scene];

  // 픽셀 사각형 그리기
  const drawPixelRect = (
    ctx: CanvasRenderingContext2D,
    x: number,
    y: number,
    width: number,
    height: number,
    color: string,
  ) => {
    ctx.fillStyle = color;
    ctx.fillRect(Math.floor(x), Math.floor(y), Math.ceil(width), Math.ceil(height));
  };

  // 사이버 적 그리기
  const drawEnemy = (ctx: CanvasRenderingContext2D, x: number, y: number) => {
    const scale = 3;
    const glowColors = ["#ff1f3d", "#ff4f4f", "#ff9c63"];

    ctx.save();
    ctx.shadowBlur = 18;
    ctx.shadowColor = "#ff1f3d";

    // main body
    drawPixelRect(ctx, x + 1 * scale, y + 1 * scale, 12 * scale, 10 * scale, "#450007");
    drawPixelRect(ctx, x + 2 * scale, y + 2 * scale, 10 * scale, 8 * scale, "#6c0010");

    ctx.shadowBlur = 10;
    drawPixelRect(ctx, x + 3 * scale, y + 3 * scale, 8 * scale, 6 * scale, "#9f001a");
    drawPixelRect(ctx, x + 4 * scale, y + 4 * scale, 6 * scale, 4 * scale, "#c8001f");

    // glowing eyes & aura
    ctx.shadowBlur = 20;
    glowColors.forEach((color, idx) => {
      ctx.shadowColor = color;
      drawPixelRect(ctx, x + (4 - idx) * scale, y + 5 * scale, 2 * scale, 2 * scale, color);
      drawPixelRect(ctx, x + (8 + idx) * scale, y + 5 * scale, 2 * scale, 2 * scale, color);
    });

    // top sparks
    ctx.shadowBlur = 12;
    ctx.shadowColor = "#ff4f4f";
    drawPixelRect(ctx, x + 5 * scale, y, 2 * scale, 2 * scale, "#ff8c5a");
    drawPixelRect(ctx, x + 8 * scale, y + 1 * scale, 2 * scale, 2 * scale, "#ffd25a");

    ctx.restore();
  };

  // 사이버 빌딩 그리기
  const drawBuilding = (ctx: CanvasRenderingContext2D, x: number, y: number) => {
    const scale = 1.5;

    ctx.shadowColor = "#00ffff";
    ctx.shadowBlur = 8;

    // 건물 본체
    drawPixelRect(ctx, x, y + 10 * scale, 20 * scale, 20 * scale, "#1a1a3e");

    ctx.shadowBlur = 12;

    // 네온 창문
    for (let i = 0; i < 3; i++) {
      for (let j = 0; j < 2; j++) {
        const windowColor = (i + j) % 2 === 0 ? "#00ffff" : "#ff00ff";
        ctx.shadowColor = windowColor;
        drawPixelRect(
          ctx,
          x + (4 + i * 6) * scale,
          y + (12 + j * 8) * scale,
          3 * scale,
          4 * scale,
          windowColor,
        );
      }
    }

    ctx.shadowBlur = 6;
    ctx.shadowColor = "#ff00ff";

    // 네온 안테나
    drawPixelRect(ctx, x + 9 * scale, y + 6 * scale, 2 * scale, 5 * scale, "#ff00ff");

    ctx.shadowBlur = 0;
  };

  // 데이터 스트림 그리기
  const drawDataStream = (
    ctx: CanvasRenderingContext2D,
    x: number,
    y: number,
    animFrame: number,
  ) => {
    const height = 40 + Math.sin(animFrame * 0.02 + x * 0.1) * 10;

    ctx.shadowColor = "#00ffff";
    ctx.shadowBlur = 8;
    ctx.fillStyle = "rgba(0, 255, 255, 0.3)";
    ctx.fillRect(x, y, 2, height);

    ctx.fillStyle = "#00ffff";
    ctx.fillRect(x, y, 2, 4);

    ctx.shadowBlur = 0;
  };

  // 씬 애니메이션
  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext("2d");
    if (!ctx) return;

    animFrameRef.current = 0;

    const animate = () => {
      // 배경 클리어
      ctx.fillStyle = "#0a0e27";
      ctx.fillRect(0, 0, canvas.width, canvas.height);

      if (slide.scene !== "enemy") {
        for (let i = 0; i < 15; i++) {
          const x = (animFrameRef.current * 0.5 + i * 40) % canvas.width;
          drawDataStream(ctx, x, 0, animFrameRef.current);
        }
      }

      // 씬 타입에 따라 다른 내용 그리기
      if (slide.scene === "city") {
        drawBuilding(ctx, 80, 50);
        drawBuilding(ctx, 380, 60);
      } else if (slide.scene === "enemy") {
        drawEnemy(ctx, 60, 70);
        drawEnemy(ctx, 160, 70);
        drawEnemy(ctx, 260, 70);
        drawEnemy(ctx, 360, 70);
        drawEnemy(ctx, 460, 70);
      } else if (slide.scene === "nature") {
        drawBuilding(ctx, 120, 50);
        drawBuilding(ctx, 360, 60);
      }

      animFrameRef.current++;
      animationIdRef.current = requestAnimationFrame(animate);
    };

    animationIdRef.current = requestAnimationFrame(animate);

    return () => {
      if (animationIdRef.current) {
        cancelAnimationFrame(animationIdRef.current);
        animFrameRef.current = 0;
      }
    };
  }, [currentSlide]);

  const handleNext = () => {
    if (currentSlide < tutorialData.length - 1) {
      setCurrentSlide((prev) => prev + 1);
    } else {
      onClose();
    }
  };

  const handlePrevious = () => {
    if (currentSlide > 0) {
      setCurrentSlide((prev) => prev - 1);
    }
  };

  const handleSkip = () => {
    onClose();
  };

  return (
    <Modal
      open
      onClose={onClose}
      title="튜토리얼"
      className="!max-h-[85vh] max-w-2xl"
      closeOnBackdrop={false}
      footer={
        <div className="space-y-2 pt-2">
          <div className="flex justify-between gap-3">
            <CommonButton
              variant="outline"
              onClick={handlePrevious}
              disabled={currentSlide === 0}
              className="border-primary/40 text-custom-white/80 hover:text-custom-white flex-1"
            >
              ← 이전
            </CommonButton>
            <CommonButton
              variant="solid"
              onClick={handleNext}
              className="from-primary shadow-primary/40 flex-1 bg-gradient-to-r to-[#7de6ff] font-semibold text-black shadow-lg"
            >
              {slide.isLastSlide ? "런닝 시작" : "다음 →"}
            </CommonButton>
          </div>

          <div className="text-center">
            <button
              onClick={handleSkip}
              className="text-custom-gray hover:text-custom-white text-[11px] tracking-wide underline underline-offset-4 transition-colors"
            >
              튜토리얼 건너뛰기
            </button>
          </div>
        </div>
      }
    >
      <div className="flex flex-1 flex-col space-y-5">
        {/* <div className="flex items-center justify-between text-xs text-custom-gray">
          <span className="px-3 py-1 rounded-full border border-primary/30 text-primary/80 tracking-wide">
            RUNNING CITY STORY
          </span>
        </div> */}
        {/* 프로그레스 바 */}
        <div>
          <div className="text-custom-gray/70 flex items-center justify-between text-[10px] tracking-[0.2em] uppercase">
            {tutorialData.map((slideItem, idx) => (
              <div
                key={slideItem.slideId}
                className={`mx-0.5 h-[2px] flex-1 rounded-full transition-colors ${idx <= currentSlide ? "bg-primary" : "bg-custom-gray/30"}`}
              />
            ))}
          </div>
        </div>

        {/* 픽셀 아트 씬 캔버스 */}
        <div
          className={`border-primary/40 relative h-48 w-full overflow-hidden rounded-2xl border bg-gradient-to-br ${sceneTheme.gradient} ${sceneTheme.glow}`}
          style={{
            imageRendering: "pixelated",
          }}
        >
          <div className="absolute inset-0 bg-[radial-gradient(circle,_rgba(0,255,255,0.12),_transparent_45%)] opacity-30" />
          {slide.scene === "city" && (
            <div className="pointer-events-none absolute inset-0 z-10">
              <div className="absolute inset-0 bg-gradient-to-t from-[#02121e] via-[#031d30] to-transparent opacity-80" />
              <div className="absolute inset-0 bg-[radial-gradient(circle,_rgba(0,255,255,0.2),_transparent_65%)] opacity-40" />
              <div className="relative flex h-full w-full items-end justify-between px-6">
                {[45, 80, 60, 95, 55, 75].map((height, idx) => (
                  <div
                    key={idx}
                    className="mx-1 flex-1 rounded-t-lg bg-gradient-to-t from-[#05172a] via-[#0a3356] to-[#6bf4ff] shadow-[0_-8px_20px_rgba(107,244,255,0.25)]"
                    style={{ height: `${height}%`, minHeight: "35%" }}
                  >
                    <div className="flex flex-col gap-1 p-2">
                      {[...new Array(3)].map((__, windowIdx) => (
                        <div key={windowIdx} className="flex gap-1">
                          {[...new Array(3)].map((___, lightIdx) => (
                            <span
                              key={lightIdx}
                              className={`h-1 w-2 rounded-sm ${
                                (windowIdx + lightIdx + idx) % 2 === 0
                                  ? "bg-cyan-200/70"
                                  : "bg-fuchsia-300/60"
                              }`}
                            />
                          ))}
                        </div>
                      ))}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
          {slide.scene === "enemy" && (
            <div className="pointer-events-none absolute inset-0 z-10">
              <div className="absolute inset-0 bg-gradient-to-b from-[#2d0007] via-transparent to-[#2d0007] opacity-80" />
              <div className="absolute inset-0 bg-[radial-gradient(circle,_rgba(255,0,0,0.25),_transparent_60%)] opacity-60" />
            </div>
          )}
          <canvas
            ref={canvasRef}
            width={550}
            height={180}
            className="relative z-20 h-full w-full"
            style={{ imageRendering: "pixelated" }}
          />
          <img
            src={HAMSTER_IMAGE_URL}
            alt="Running hamster"
            className="pointer-events-none absolute top-1/2 left-1/2 z-30 h-28 -translate-x-1/2 -translate-y-1/2 object-contain mix-blend-screen select-none"
          />
          <div className="text-custom-gray/80 absolute right-4 bottom-3 left-4 z-30 flex items-center justify-end text-[11px]">
            <span>
              {currentSlide + 1} / {tutorialData.length}
            </span>
          </div>
        </div>

        {/* 타이틀 & 설명 */}
        <div className="flex h-full flex-1 flex-col space-y-3 px-3">
          <h3
            className={`text-custom-white text-center text-xl font-bold drop-shadow-md ${sceneTheme.accent}`}
          >
            {slide.title}
          </h3>
          <p className="text-custom-gray flex min-h-20 flex-1 items-center justify-center text-center text-xs leading-relaxed whitespace-pre-line">
            {slide.description}
          </p>
        </div>
      </div>
      <style>{`
        @keyframes enemyGlow {
          0% {transform: translateX(-20%); opacity: 0.4;}
          50% {opacity: 0.8;}
          100% {transform: translateX(20%); opacity: 0.4;}
        }
      `}</style>
    </Modal>
  );
}
