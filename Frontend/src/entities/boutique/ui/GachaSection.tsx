// src/entities/boutique/ui/GachaSection.tsx
import  CommonButton from "@/shared/ui/CommonButton";
import { ENV } from "@/shared/config/env";

interface GachaSectionProps {
  userCurrency: number;
  onGachaPull: (type: 'single' | 'multi') => void;
}

export function GachaSection({ userCurrency, onGachaPull }: GachaSectionProps) {
  const singlePrice = 50;
  const multiPrice = 450;
  const gachaCapsuleImageUrl = `${ENV.ASSETS_ORIGIN}/gacha_capsule.png`;

  return (
    <div className="mb-6">
      <div className="text-center mb-4">
        <h2 className="text-subtitle text-custom-white mb-2">🎲 가챠</h2>
        <p className="text-desc text-custom-gray">운을 시험해보세요!</p>
      </div>
      
      {/* 가챠 이미지 영역 */}
      <div className="bg-section-bg border border-primary/30 rounded-lg p-6 mb-4">
        <div className="aspect-video bg-custom-black/50 rounded-lg flex items-center justify-center mb-4 overflow-hidden">
          <img 
            src={gachaCapsuleImageUrl} 
            alt="가챠 캡슐" 
            className="w-full h-full object-contain"
          />
        </div>
        
        {/* 가챠 버튼들 */}
        <div className="grid grid-cols-2 gap-3">
          <div className="text-center">
            <div className="mb-2 h-[4.5rem]">
              <p className="text-content text-custom-white font-bold">1뽑</p>
              <p className="text-desc text-primary">{singlePrice} CR</p>
            </div>
            <CommonButton
              variant="outline"
              onClick={() => onGachaPull('single')}
              disabled={userCurrency < singlePrice}
              className="w-full"
            >
              뽑기
            </CommonButton>
          </div>
          
          <div className="text-center">
            <div className="mb-2 h-[4.5rem]">
              <p className="text-content text-custom-white font-bold">10뽑</p>
              <p className="text-desc text-primary">{multiPrice} CR</p>
              <p className="text-xs text-custom-gray">50 CR 할인!</p>
            </div>
            <CommonButton
              variant="solid"
              onClick={() => onGachaPull('multi')}
              disabled={userCurrency < multiPrice}
              className="w-full"
            >
              뽑기
            </CommonButton>
          </div>
        </div>
      </div>
    </div>
  );
}
