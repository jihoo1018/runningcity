// src/features/modals/boutique/GachaResult.tsx
import { useState, useEffect } from 'react';
import { ModalProps } from "@/app/modal/types";
import { Modal } from "@/shared/ui";
import { CommonButton } from "@/shared/ui";
import type { GachaResponse } from '@/entities/boutique/model/types';
import { SpritePreview } from '@/entities/boutique/ui/SpritePreview';

type GachaResultPayload = {
  result: GachaResponse;
};

export default function BoutiqueGachaResult({ onClose, payload }: ModalProps) {
  const data = (payload as GachaResultPayload | undefined) ?? undefined;
  const result = data?.result;
  const [isAnimating, setIsAnimating] = useState(true);
  const [capsuleOpened, setCapsuleOpened] = useState(false);

  if (!result) {
    return (
      <Modal open onClose={onClose} title="오류">
        <p className="text-accent-red">가챠 결과를 불러올 수 없습니다.</p>
      </Modal>
    );
  }

  // 캡슐 애니메이션 타이밍
  useEffect(() => {
    // 1초 후 캡슐 열림
    const openTimer = setTimeout(() => {
      setCapsuleOpened(true);
    }, 1000);

    // 2초 후 결과 화면으로 전환
    const finishTimer = setTimeout(() => {
      setIsAnimating(false);
    }, 2000);

    return () => {
      clearTimeout(openTimer);
      clearTimeout(finishTimer);
    };
  }, []);

  // 희귀도별 색상
  const rarityColors = {
    common: 'text-custom-gray',
    rare: 'text-blue-400',
    epic: 'text-purple-400',
    legendary: 'text-orange-400',
  };

  const rarityLabels = {
    common: '일반',
    rare: '희귀',
    epic: '에픽',
    legendary: '전설',
  };

  return (
    <Modal
      open
      onClose={onClose}
      title={isAnimating ? '가챠 뽑는 중...' : `${result.drawType === 'single' ? '1' : '10'}뽑 가챠 결과`}
      footer={
        !isAnimating ? (
          <CommonButton
            variant="solid"
            onClick={onClose}
            className="w-full"
          >
            확인
          </CommonButton>
        ) : undefined
      }
    >
      {isAnimating ? (
        // CSS 캡슐 애니메이션
        <div className="space-y-4">
          <div className="text-center py-8">
            <p className="text-custom-white text-lg font-bold mb-6">
              {result.drawType === 'single' ? '🎁 가챠 뽑는 중...' : '🎁 10연차 뽑는 중...'}
            </p>
            
            {/* 캡슐 애니메이션 */}
            <div className="relative mx-auto w-64 h-64 flex items-center justify-center">
              {/* 빛나는 효과 */}
              <div 
                className={`absolute inset-0 rounded-full blur-3xl transition-all duration-500 ${capsuleOpened ? 'scale-150 opacity-100' : 'scale-100 opacity-50'}`}
                style={{ backgroundColor: 'rgba(0, 229, 255, 0.2)' }}
              />
              
              {/* 캡슐 */}
              <div className={`relative w-32 h-48 ${!capsuleOpened ? 'animate-shake' : ''}`}>
                {/* 캡슐 상단 (밝은 청록색) - 픽셀아트 스타일 */}
                <div 
                  className={`absolute top-0 left-0 w-full h-24 border-4 transition-all duration-700 ease-out ${
                    capsuleOpened ? '-translate-y-20 opacity-0' : 'translate-y-0 opacity-100'
                  }`}
                  style={{
                    background: 'linear-gradient(to bottom, #00E5FF, #00B8E6)',
                    borderColor: '#00D4FF',
                    boxShadow: '0 0 25px rgba(0, 229, 255, 0.6), 0 0 40px rgba(0, 184, 230, 0.4), inset 0 -10px 20px rgba(0,0,0,0.2)',
                    imageRendering: 'pixelated',
                    borderRadius: '50% 50% 0 0',
                    clipPath: 'polygon(10% 0%, 90% 0%, 100% 10%, 100% 90%, 90% 100%, 0 100%, 0 90%, 0% 10%)'
                  }}
                >
                  <div 
                    className="absolute"
                    style={{ 
                      backgroundColor: 'rgba(255, 255, 255, 0.25)',
                      top: '16px',
                      left: '16px',
                      right: '16px',
                      bottom: '16px',
                      clipPath: 'polygon(15% 0%, 85% 0%, 100% 15%, 100% 85%, 85% 100%, 0 100%, 0 85%, 0% 15%)'
                    }}
                  />
                </div>
                
                {/* 캡슐 하단 (진한 파란색) - 픽셀아트 스타일 */}
                <div 
                  className={`absolute bottom-0 left-0 w-full h-24 border-4 transition-all duration-700 ease-out ${
                    capsuleOpened ? 'translate-y-20 opacity-0' : 'translate-y-0 opacity-100'
                  }`}
                  style={{
                    background: 'linear-gradient(to top, #0A1E5C, #1E40AF)',
                    borderColor: '#1E3A8A',
                    boxShadow: '0 0 25px rgba(30, 58, 138, 0.6), 0 0 40px rgba(10, 30, 92, 0.4), inset 0 10px 20px rgba(0,0,0,0.2)',
                    imageRendering: 'pixelated',
                    borderRadius: '0 0 50% 50%',
                    clipPath: 'polygon(0 0%, 90% 0%, 100% 10%, 100% 90%, 90% 100%, 10% 100%, 0 90%, 0% 10%)'
                  }}
                >
                  <div 
                    className="absolute"
                    style={{ 
                      backgroundColor: 'rgba(255, 255, 255, 0.15)',
                      top: '16px',
                      left: '16px',
                      right: '16px',
                      bottom: '16px',
                      clipPath: 'polygon(0 15%, 85% 0%, 100% 15%, 100% 85%, 85% 100%, 15% 100%, 0 85%)'
                    }}
                  />
                </div>
                
                {/* 중앙 다이아몬드 빛 효과 (캡슐이 열릴 때) */}
                {capsuleOpened && (
                  <div className="absolute inset-0 flex items-center justify-center">
                    {/* 외부 다이아몬드 - 펄스 효과 (진한 파란색) */}
                    <div 
                      className="w-20 h-20 animate-ping"
                      style={{ 
                        backgroundColor: '#1E3A8A',
                        transform: 'rotate(45deg)',
                        imageRendering: 'pixelated',
                        opacity: 0.7
                      }}
                    />
                    {/* 중간 다이아몬드 - 메인 (진한 네이비 블루) */}
                    <div 
                      className="absolute w-16 h-16"
                      style={{ 
                        background: 'linear-gradient(135deg, #1E40AF 0%, #1E3A8A 50%, #0A1E5C 100%)',
                        transform: 'rotate(45deg)',
                        boxShadow: '0 0 25px rgba(30, 64, 175, 0.8), 0 0 50px rgba(30, 58, 138, 0.5), inset 0 0 15px rgba(100, 150, 255, 0.2)',
                        imageRendering: 'pixelated',
                        border: '3px solid #3B82F6'
                      }}
                    >
                      {/* 십자 빛 효과 (가운데 십자가) */}
                      <div 
                        className="absolute top-1/2 left-0 w-full h-1 -translate-y-1/2"
                        style={{ 
                          backgroundColor: 'rgba(100, 180, 255, 0.6)',
                          boxShadow: '0 0 8px rgba(100, 180, 255, 0.6)',
                          imageRendering: 'pixelated'
                        }}
                      />
                      <div 
                        className="absolute left-1/2 top-0 w-1 h-full -translate-x-1/2"
                        style={{ 
                          backgroundColor: 'rgba(100, 180, 255, 0.6)',
                          boxShadow: '0 0 8px rgba(100, 180, 255, 0.6)',
                          imageRendering: 'pixelated'
                        }}
                      />
                      {/* 다이아몬드 상단 하이라이트 */}
                      <div 
                        className="absolute top-2 left-1/2 -translate-x-1/2 w-6 h-6"
                        style={{ 
                          backgroundColor: 'rgba(100, 150, 255, 0.4)',
                          imageRendering: 'pixelated'
                        }}
                      />
                    </div>
                    {/* 내부 작은 다이아몬드 - 회전 */}
                    <div 
                      className="absolute w-8 h-8 animate-spin"
                      style={{ 
                        backgroundColor: '#2563EB',
                        transform: 'rotate(45deg)',
                        animationDuration: '3s',
                        imageRendering: 'pixelated',
                        border: '2px solid #60A5FA'
                      }}
                    />
                  </div>
                )}
              </div>
            </div>

            <p className="text-custom-gray text-sm mt-4">
              {capsuleOpened ? '✨ 결과 확인 중...' : '잠시만 기다려주세요...'}
            </p>
          </div>
          
          {/* 애니메이션 스타일 */}
          <style>{`
            @keyframes shake {
              0%, 100% { transform: translateX(0) rotate(0deg); }
              10%, 30%, 50%, 70%, 90% { transform: translateX(-5px) rotate(-2deg); }
              20%, 40%, 60%, 80% { transform: translateX(5px) rotate(2deg); }
            }
            
            .animate-shake {
              animation: shake 0.8s ease-in-out infinite;
            }
          `}</style>
        </div>
      ) : (
        // 결과 화면
        <div className="space-y-4">
        {/* 가챠 정보 */}
        <div className="bg-section-bg border border-primary/20 rounded-lg p-4">
          <div className="grid grid-cols-2 gap-3 text-sm">
            <div>
              <span className="text-custom-gray">뽑은 개수:</span>
              <span className="ml-2 text-custom-white font-medium">{result.totalDraws}개</span>
            </div>
            <div>
              <span className="text-custom-gray">소비 CR:</span>
              <span className="ml-2 text-accent-red font-medium">-{result.spentCredit}</span>
            </div>
            <div className="col-span-2">
              <span className="text-custom-gray">남은 CR:</span>
              <span className="ml-2 text-primary font-bold">{result.remainingCredit.toLocaleString()}</span>
            </div>
          </div>
        </div>

        {/* 획득 아이템 목록 */}
        <div className="space-y-2">
          <h3 className="text-custom-white font-bold text-sm">획득 아이템</h3>
          <div className="grid grid-cols-2 gap-2 max-h-96 overflow-y-auto">
            {result.items.map((item, index) => (
              <div
                key={`${item.itemId}-${index}`}
                className="bg-section-bg border border-primary/20 rounded-lg p-2 relative"
              >
                {/* 신규 획득 뱃지 */}
                {item.isNew && (
                  <div className="absolute top-1 right-1 bg-primary text-custom-black text-xs px-2 py-0.5 rounded-full font-bold z-10">
                    NEW
                  </div>
                )}

                {/* 아이템 이미지 */}
                <div className="aspect-square bg-custom-black/50 rounded overflow-hidden mb-2">
                  <SpritePreview
                    basePath={item.path}
                    category={item.category}
                    className="w-full h-full"
                  />
                </div>

                {/* 아이템 정보 */}
                <div className="text-center">
                  <p className="text-xs text-custom-white font-medium truncate mb-1">
                    {item.itemName}
                  </p>
                  <p className={`text-xs font-bold ${rarityColors[item.rarity]}`}>
                    {rarityLabels[item.rarity]}
                  </p>
                </div>
              </div>
            ))}
          </div>
        </div>
        </div>
      )}
    </Modal>
  );
}

