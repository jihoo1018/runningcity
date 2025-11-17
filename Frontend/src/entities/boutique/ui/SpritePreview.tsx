// src/entities/boutique/ui/SpritePreview.tsx
import { useEffect, useRef, useState } from 'react';
import { ENV } from '@/shared/config/env';

interface SpritePreviewProps {
  basePath: string;
  category: string;
  className?: string;
}

export function SpritePreview({ basePath, category, className = '' }: SpritePreviewProps) {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const [imageLoaded, setImageLoaded] = useState(false);
  const [imageError, setImageError] = useState(false);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    // 경로 생성: {animation}을 walk로 교체하고 백슬래시를 슬래시로 변환
    let imagePath = basePath
      .replace('{animation}', 'walk')
      .replace(/\\/g, '/');

    // 맨 앞의 슬래시 제거
    if (imagePath.startsWith('/')) {
      imagePath = imagePath.substring(1);
    }

    // 서버 URL 구성
    const imageUrl = `${ENV.ASSETS_ORIGIN}/${imagePath}`;

    const img = new Image();
    // CORS 헤더가 없는 경우 crossOrigin 설정 제거
    // img.crossOrigin = 'anonymous';
    
    img.onload = () => {
      // Canvas 크기 설정
      canvas.width = 128;  // 더 큰 크기로 설정
      canvas.height = 128;

      // 배경 클리어
      ctx.clearRect(0, 0, canvas.width, canvas.height);

      // head 카테고리는 중앙 영역을 크게 확대해서 표시
      if (category === 'head') {
        // 스프라이트 시트의 중앙 부분 (64x64)을 잘라서 확대
        // 중앙 프레임 위치: 가로 4번째 프레임 (64*4 = 256), 세로 2번째 줄 (64*1 = 64)
        const centerX = 64 * 4;  // 중앙 프레임
        const centerY = 64 * 1;  // 남쪽에서 두 번째 방향
        
        // 중앙 64x64를 128x128로 2배 확대
        ctx.imageSmoothingEnabled = false; // 픽셀 아트 선명하게
        ctx.drawImage(
          img,
          centerX, centerY,  // 소스 시작 위치 (중앙)
          64, 64,            // 소스 크기
          0, 0,              // 캔버스 시작 위치
          128, 128           // 캔버스 크기 (2배 확대)
        );
      } else {
        // 다른 카테고리는 첫 번째 프레임을 2배 확대
        ctx.imageSmoothingEnabled = false; // 픽셀 아트 선명하게
        ctx.drawImage(
          img,
          0, 0,        // 소스 x, y (첫 번째 프레임)
          64, 64,      // 소스 width, height
          0, 0,        // 목적지 x, y
          128, 128     // 목적지 width, height (2배 확대)
        );
      }

      setImageLoaded(true);
      setImageError(false);
    };

    img.onerror = () => {
      setImageError(true);
      setImageLoaded(false);
      
      // 에러 시 기본 아이콘 표시
      canvas.width = 128;
      canvas.height = 128;
      ctx.clearRect(0, 0, canvas.width, canvas.height);
      ctx.fillStyle = 'rgba(255, 255, 255, 0.1)';
      ctx.fillRect(0, 0, canvas.width, canvas.height);
    };

    img.src = imageUrl;

    return () => {
      img.onload = null;
      img.onerror = null;
    };
  }, [basePath]);

  return (
    <div className={`relative ${className}`}>
      <canvas
        ref={canvasRef}
        className={`w-full h-full ${!imageLoaded ? 'hidden' : ''}`}
        style={{ imageRendering: 'pixelated' }} // 픽셀 아트 선명하게
      />
      
      {/* 로딩 중이거나 에러 시 기본 아이콘 */}
      {(!imageLoaded || imageError) && (
        <div className="w-full h-full flex items-center justify-center text-2xl">
          {category === 'bodies' && '👤'}
          {category === 'clothes' && '👕'}
          {category === 'hair' && '💇'}
          {category === 'head' && '🎭'}
        </div>
      )}
    </div>
  );
}

