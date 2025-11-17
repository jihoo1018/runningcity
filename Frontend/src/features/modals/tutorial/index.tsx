// src/features/modals/tutorial/index.tsx
import { useState, useEffect, useRef } from 'react';
import { ModalProps } from "@/app/modal/types";
import { Modal } from "@/shared/ui";
import { CommonButton } from "@/shared/ui";

// 튜토리얼 슬라이드 타입
type TutorialSlide = {
  slideId: number;
  title: string;
  description: string;
  scene: 'welcome' | 'city' | 'enemy' | 'nature';
  isLastSlide?: boolean;
};

// 튜토리얼 데이터
const tutorialData: TutorialSlide[] = [
  {
    slideId: 1,
    title: "SYSTEM_BOOT\nRUNNERS_PROTOCOL.EXE",
    description: "운동 에너지를 해킹 무기로\n전환하는 시스템 가동 중...",
    scene: "welcome"
  },
  {
    slideId: 2,
    title: "WORLD_DATA\nMOTION_CITY.DAT",
    description: "러닝 에너지로 구동되는 도시\nSTATIC 조직의 해킹 감지!",
    scene: "city"
  },
  {
    slideId: 3,
    title: "ROLE_ASSIGN\nACTIVE_RUNNER.SYS",
    description: "당신은 액티브 러너\n운동으로 AI 코어 PULSE 보호",
    scene: "nature"
  },
  {
    slideId: 4,
    title: "DAILY_MISSION\nPULSE.CORE",
    description: "AI 코어가 매일 미션 송출\n크레딧과 데이터 칩 획득",
    scene: "welcome"
  },
  {
    slideId: 5,
    title: "SOCIAL_NET\nRUNNERS_UNION.LNK",
    description: "러너즈 유니온 구축\n협력으로 더 강한 에너지 생성",
    scene: "nature"
  },
  {
    slideId: 6,
    title: "ENEMY_BASE\nSTATIC_SPOT.TGT",
    description: "스태틱 기지 발견 시\n러닝으로 역해킹 개시!",
    scene: "enemy"
  },
  {
    slideId: 7,
    title: "PROTOCOL_START\nREADY_TO_RUN",
    description: "모든 시스템 정상\n런닝 프로토콜을 시작합니다",
    scene: "city",
    isLastSlide: true
  }
];

export default function TutorialModal({ onClose }: ModalProps) {
  const [currentSlide, setCurrentSlide] = useState(0);
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const animFrameRef = useRef(0);
  const animationIdRef = useRef<number | undefined>(undefined);

  const slide = tutorialData[currentSlide];
  const progress = ((currentSlide + 1) / tutorialData.length) * 100;

  // 픽셀 사각형 그리기
  const drawPixelRect = (
    ctx: CanvasRenderingContext2D,
    x: number,
    y: number,
    width: number,
    height: number,
    color: string
  ) => {
    ctx.fillStyle = color;
    ctx.fillRect(Math.floor(x), Math.floor(y), Math.ceil(width), Math.ceil(height));
  };

  // 사이버 러너 그리기
  const drawRunner = (
    ctx: CanvasRenderingContext2D,
    x: number,
    y: number,
    frame: number
  ) => {
    const scale = 2;
    const colors = {
      skin: '#ffcc99',
      shirt: '#00ffff',
      pants: '#1a1a3e',
      shoes: '#ff00ff'
    };

    // 글로우 효과
    ctx.shadowColor = colors.shirt;
    ctx.shadowBlur = 10;

    // 머리
    drawPixelRect(ctx, x + 5*scale, y + 2*scale, 6*scale, 6*scale, colors.skin);
    
    ctx.shadowBlur = 0;
    
    // 눈 (사이버 고글)
    drawPixelRect(ctx, x + 6*scale, y + 4*scale, 2*scale, 1*scale, colors.shirt);
    drawPixelRect(ctx, x + 9*scale, y + 4*scale, 2*scale, 1*scale, colors.shirt);
    
    // 몸통
    ctx.shadowColor = colors.shirt;
    ctx.shadowBlur = 8;
    drawPixelRect(ctx, x + 4*scale, y + 8*scale, 8*scale, 6*scale, colors.shirt);
    
    ctx.shadowBlur = 0;
    
    // 팔 (달리기 모션)
    if (frame === 0) {
      drawPixelRect(ctx, x + 2*scale, y + 9*scale, 2*scale, 5*scale, colors.skin);
      drawPixelRect(ctx, x + 12*scale, y + 9*scale, 2*scale, 5*scale, colors.skin);
    } else {
      drawPixelRect(ctx, x + 2*scale, y + 10*scale, 2*scale, 5*scale, colors.skin);
      drawPixelRect(ctx, x + 12*scale, y + 8*scale, 2*scale, 5*scale, colors.skin);
    }
    
    // 다리
    drawPixelRect(ctx, x + 5*scale, y + 14*scale, 2*scale, 4*scale, colors.pants);
    drawPixelRect(ctx, x + 9*scale, y + 14*scale, 2*scale, 4*scale, colors.pants);
    
    // 신발
    ctx.shadowColor = colors.shoes;
    ctx.shadowBlur = 6;
    drawPixelRect(ctx, x + 5*scale, y + 18*scale, 2*scale, 2*scale, colors.shoes);
    drawPixelRect(ctx, x + 9*scale, y + 18*scale, 2*scale, 2*scale, colors.shoes);
    
    ctx.shadowBlur = 0;
  };

  // 사이버 적 그리기
  const drawEnemy = (ctx: CanvasRenderingContext2D, x: number, y: number) => {
    const scale = 2;
    const colors = {
      body: '#8b0000',
      eye: '#ff0000',
      dark: '#4a0000'
    };

    ctx.shadowColor = colors.eye;
    ctx.shadowBlur = 12;

    // 몸통
    drawPixelRect(ctx, x + 2*scale, y + 2*scale, 8*scale, 8*scale, colors.body);
    drawPixelRect(ctx, x + 1*scale, y + 3*scale, 10*scale, 6*scale, colors.body);
    
    ctx.shadowBlur = 8;
    
    // 어두운 부분
    drawPixelRect(ctx, x + 3*scale, y + 4*scale, 6*scale, 4*scale, colors.dark);
    
    ctx.shadowBlur = 15;
    
    // 눈 (빨간 글로우)
    drawPixelRect(ctx, x + 4*scale, y + 5*scale, 1*scale, 2*scale, colors.eye);
    drawPixelRect(ctx, x + 7*scale, y + 5*scale, 1*scale, 2*scale, colors.eye);
    
    ctx.shadowBlur = 0;
  };

  // 사이버 빌딩 그리기
  const drawBuilding = (ctx: CanvasRenderingContext2D, x: number, y: number) => {
    const scale = 1.5;
    
    ctx.shadowColor = '#00ffff';
    ctx.shadowBlur = 8;
    
    // 건물 본체
    drawPixelRect(ctx, x, y + 10*scale, 20*scale, 20*scale, '#1a1a3e');
    
    ctx.shadowBlur = 12;
    
    // 네온 창문
    for (let i = 0; i < 3; i++) {
      for (let j = 0; j < 2; j++) {
        const windowColor = (i + j) % 2 === 0 ? '#00ffff' : '#ff00ff';
        ctx.shadowColor = windowColor;
        drawPixelRect(ctx, x + (4 + i*6)*scale, y + (12 + j*8)*scale, 3*scale, 4*scale, windowColor);
      }
    }
    
    ctx.shadowBlur = 6;
    ctx.shadowColor = '#ff00ff';
    
    // 네온 안테나
    drawPixelRect(ctx, x + 9*scale, y + 6*scale, 2*scale, 5*scale, '#ff00ff');
    
    ctx.shadowBlur = 0;
  };

  // 데이터 스트림 그리기
  const drawDataStream = (
    ctx: CanvasRenderingContext2D,
    x: number,
    y: number,
    animFrame: number
  ) => {
    const height = 40 + Math.sin(animFrame * 0.02 + x * 0.1) * 10;
    
    ctx.shadowColor = '#00ffff';
    ctx.shadowBlur = 8;
    ctx.fillStyle = 'rgba(0, 255, 255, 0.3)';
    ctx.fillRect(x, y, 2, height);
    
    ctx.fillStyle = '#00ffff';
    ctx.fillRect(x, y, 2, 4);
    
    ctx.shadowBlur = 0;
  };

  // 씬 애니메이션
  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const animate = () => {
      // 배경 클리어
      ctx.fillStyle = '#0a0e27';
      ctx.fillRect(0, 0, canvas.width, canvas.height);

      // 데이터 스트림 배경
      for (let i = 0; i < 15; i++) {
        const x = (animFrameRef.current * 0.5 + i * 40) % canvas.width;
        drawDataStream(ctx, x, 0, animFrameRef.current);
      }

      // 씬 타입에 따라 다른 내용 그리기
      const runnerFrame = Math.floor(animFrameRef.current / 15) % 2;
      
      if (slide.scene === 'welcome') {
        drawRunner(ctx, 240, 60, runnerFrame);
      } else if (slide.scene === 'city') {
        drawBuilding(ctx, 80, 50);
        drawBuilding(ctx, 380, 60);
        drawRunner(ctx, 240, 60, runnerFrame);
      } else if (slide.scene === 'enemy') {
        drawEnemy(ctx, 180, 60);
        drawEnemy(ctx, 320, 50);
        drawRunner(ctx, 240, 60, runnerFrame);
      } else if (slide.scene === 'nature') {
        drawBuilding(ctx, 120, 50);
        drawBuilding(ctx, 360, 60);
        drawRunner(ctx, 240, 60, runnerFrame);
      }

      animFrameRef.current++;
      animationIdRef.current = requestAnimationFrame(animate);
    };

    animate();

    return () => {
      if (animationIdRef.current) {
        cancelAnimationFrame(animationIdRef.current);
      }
    };
  }, [slide.scene]);

  const handleNext = () => {
    if (currentSlide < tutorialData.length - 1) {
      setCurrentSlide(prev => prev + 1);
    } else {
      onClose();
    }
  };

  const handleSkip = () => {
    onClose();
  };

  return (
    <Modal
      open
      onClose={onClose}
      title={
        <span className="text-xs text-primary font-mono">
          TUTORIAL [{currentSlide + 1}/7]
        </span>
      }
      className="max-w-2xl"
    >
      <div className="space-y-4">
        {/* 사이버 씬 캔버스 */}
        <div 
          className="relative w-full h-48 rounded-lg overflow-hidden border-2 border-primary"
          style={{
            background: 'linear-gradient(180deg, #1a1a3e 0%, #0a0e27 100%)',
            boxShadow: 'inset 0 0 30px rgba(0, 255, 255, 0.2)',
            imageRendering: 'pixelated'
          }}
        >
          <canvas
            ref={canvasRef}
            width={550}
            height={180}
            className="w-full h-full"
            style={{ imageRendering: 'pixelated' }}
          />
          
          {/* 스캔라인 효과 */}
          <div 
            className="absolute inset-0 pointer-events-none"
            style={{
              background: 'repeating-linear-gradient(0deg, transparent, transparent 2px, rgba(0, 255, 255, 0.03) 2px, rgba(0, 255, 255, 0.03) 4px)',
              animation: 'scanline 8s linear infinite'
            }}
          />
        </div>

        {/* 타이틀 */}
        <div className="text-center">
          <h2 
            className="text-sm font-bold text-primary mb-2 whitespace-pre-line"
            style={{
              textShadow: '0 0 10px rgba(0, 255, 255, 0.8), 0 0 20px rgba(0, 255, 255, 0.5)',
              fontFamily: 'monospace'
            }}
          >
            // {slide.title}
          </h2>
          <p 
            className="text-xs text-[#00ffaa] whitespace-pre-line leading-relaxed"
            style={{
              textShadow: '0 0 5px rgba(0, 255, 170, 0.5)'
            }}
          >
            {slide.description}
          </p>
        </div>

        {/* 프로그레스 바 */}
        <div className="w-full h-6 bg-custom-black/50 border-2 border-primary rounded p-1">
          <div 
            className="h-full rounded transition-all duration-300"
            style={{
              width: `${progress}%`,
              background: 'linear-gradient(90deg, #00ffff 0%, #ff00ff 50%, #00ffff 100%)',
              backgroundSize: '200% 100%',
              boxShadow: '0 0 10px rgba(0, 255, 255, 0.8), inset 0 0 5px rgba(255, 255, 255, 0.5)',
              animation: 'energyFlow 2s linear infinite'
            }}
          />
        </div>

        {/* 버튼들 */}
        <div className="flex gap-3 justify-center">
          <CommonButton
            variant="outline"
            onClick={handleSkip}
            className="border-primary text-primary hover:bg-primary/10"
            style={{
              textShadow: '0 0 5px rgba(0, 255, 255, 0.8)',
              boxShadow: '0 0 15px rgba(0, 255, 255, 0.3)'
            }}
          >
            건너뛰기
          </CommonButton>
          <CommonButton
            variant="solid"
            onClick={handleNext}
            className="bg-primary/20 border-2 border-[#ff00ff] text-[#ff00ff] hover:bg-[#ff00ff]/20"
            style={{
              textShadow: '0 0 5px rgba(255, 0, 255, 0.8)',
              boxShadow: '0 0 15px rgba(255, 0, 255, 0.3)'
            }}
          >
            {slide.isLastSlide ? 'EXECUTE >' : 'NEXT >'}
          </CommonButton>
        </div>

        {/* 애니메이션 스타일 */}
        <style>{`
          @keyframes energyFlow {
            0% { background-position: 0% 0%; }
            100% { background-position: 200% 0%; }
          }
          @keyframes scanline {
            0% { transform: translateY(0); }
            100% { transform: translateY(100%); }
          }
        `}</style>
      </div>
    </Modal>
  );
}

