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
  // 1) 세계관
  {
    slideId: 1,
    title: "러닝시티 소개",
    description: "러닝시티는 사람들이 달릴 때 생성되는 에너지로 운영되는 도시입니다.\n최근 에너지 부족으로 도시가 약해지고 있습니다.",
    scene: "city"
  },
  // 2) 위협과 갈등
  {
    slideId: 2,
    title: "STATIC의 방해",
    description: "STATIC 조직의 공격으로 시민들이 달리기를 멈추며 도시의 에너지가 줄고 있습니다.",
    scene: "enemy"
  },
  // 3) 내 역할
  {
    slideId: 3,
    title: "당신의 역할",
    description: "당신의 달리기가 러닝시티를 회복시키는 핵심 에너지입니다.",
    scene: "welcome"
  },
  // 4) 달리면 생기는 효과
  {
    slideId: 4,
    title: "달리면 변하는 것",
    description: "당신이 달릴 때마다 에너지가 생성되고,\n도시는 다시 활성화됩니다.",
    scene: "nature"
  },
  // 5) 보상 시스템
  {
    slideId: 5,
    title: "미션과 보상",
    description: "일일 미션으로 특별 보상을 얻고,\n1km마다 데이터 칩을 수집해 추가 정보를 해제할 수 있습니다.",
    scene: "city"
  },
  // 6) 친구·경쟁 기능
  {
    slideId: 6,
    title: "함께 달리기",
    description: "친구를 추가해 서로의 활동을 비교하고,\n랭킹으로 경쟁을 즐겨보세요.",
    scene: "nature"
  },
  // 7) 시작 안내
  {
    slideId: 7,
    title: "준비 완료",
    description: "이제 러닝시티를 위한 러닝을 시작할 준비가 되었습니다.",
    scene: "welcome",
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

  const handlePrevious = () => {
    if (currentSlide > 0) {
      setCurrentSlide(prev => prev - 1);
    }
  };

  const handleSkip = () => {
    onClose();
  };

  return (
    <Modal
      open
      onClose={onClose}
      title={`튜토리얼 (${currentSlide + 1}/7)`}
      className="max-w-2xl"
    >
      <div className="space-y-6">
        {/* 픽셀 아트 씬 캔버스 */}
        <div 
          className="relative w-full h-48 rounded-lg overflow-hidden border border-primary/30 bg-gradient-to-b from-[#1a1a3e] to-[#0a0e27]"
          style={{
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
        </div>

        {/* 타이틀 & 설명 */}
        <div className="text-center space-y-3 px-4">
          <h2 className="text-lg font-bold text-custom-white">
            {slide.title}
          </h2>
          <p className="text-sm text-custom-gray whitespace-pre-line leading-relaxed">
            {slide.description}
          </p>
        </div>

        {/* 프로그레스 바 */}
        <div className="space-y-2">
          <div className="flex justify-between text-xs text-custom-gray px-1">
            <span>{currentSlide + 1} / 7</span>
            <span>{Math.round(progress)}%</span>
          </div>
          <div className="w-full h-2 bg-section-bg rounded-full overflow-hidden">
            <div 
              className="h-full bg-primary rounded-full transition-all duration-300"
              style={{ width: `${progress}%` }}
            />
          </div>
        </div>

        {/* 버튼들 */}
        <div className="space-y-3 pt-2">
          {/* 이전/다음 버튼 */}
          <div className="flex gap-3 justify-between">
            <CommonButton
              variant="outline"
              onClick={handlePrevious}
              disabled={currentSlide === 0}
              className="flex-1"
            >
              ← 이전
            </CommonButton>
            <CommonButton
              variant="solid"
              onClick={handleNext}
              className="flex-1"
            >
              {slide.isLastSlide ? '시작하기' : '다음 →'}
            </CommonButton>
          </div>

          {/* 건너뛰기 버튼 */}
          <div className="text-center">
            <button
              onClick={handleSkip}
              className="text-xs text-custom-gray hover:text-custom-white transition-colors underline"
            >
              튜토리얼 건너뛰기
            </button>
          </div>
        </div>
      </div>
    </Modal>
  );
}

