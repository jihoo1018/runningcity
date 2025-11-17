// src/entities/boutique/ui/GachaSection.tsx
import CommonButton from "@/shared/ui/CommonButton";
import { ENV } from "@/shared/config/env";
import { DiceIcon } from "@/shared/assets/icons";

interface GachaSectionProps {
  userCurrency: number;
  onGachaPull: (type: "single" | "multi") => void;
}

export function GachaSection({ userCurrency, onGachaPull }: GachaSectionProps) {
  const singlePrice = 50;
  const multiPrice = 450;
  const gachaCapsuleImageUrl = `${ENV.ASSETS_ORIGIN}/gacha_capsule.png`;

  return (
    <div className="mb-6">
      <div className="mb-4 flex flex-col gap-1">
        <div className="flex items-center gap-2">
          <DiceIcon className="size-8" />
          <h3 className="text-subtitle text-custom-white">가챠</h3>
        </div>
        <p className="text-content text-custom-gray">운을 시험해보세요!</p>
      </div>

      {/* 가챠 이미지 영역 */}
      <div className="bg-section-bg border-primary/30 mb-4 rounded-lg border p-6">
        <div className="mb-4 flex items-center justify-center overflow-hidden rounded-lg px-12 py-1">
          <img
            src={gachaCapsuleImageUrl}
            alt="가챠 캡슐"
            className="animate-float h-full w-full object-contain will-change-transform"
          />
        </div>

        {/* 가챠 버튼들 */}
        <div className="grid grid-cols-2 gap-3">
          <div className="text-center">
            <CommonButton
              variant="outline"
              onClick={() => onGachaPull("single")}
              disabled={userCurrency < singlePrice}
              className="flex w-full flex-col"
            >
              <p className="text-content-bold text-custom-white">1회 뽑기</p>
              <p className="text-button text-primary">{singlePrice} CR</p>
            </CommonButton>
          </div>

          <div className="text-center">
            <CommonButton
              variant="outline"
              onClick={() => onGachaPull("multi")}
              disabled={userCurrency < multiPrice}
              className="bg-primary/20 flex w-full flex-col"
            >
              <p className="text-content-bold text-custom-white">10회 뽑기</p>
              <p className="text-button text-primary">
                <span className="text-desc text-custom-gray pr-1 pl-1 font-normal line-through decoration-1">
                  {singlePrice * 10}
                </span>
                {multiPrice} CR
              </p>
            </CommonButton>
          </div>
        </div>
      </div>
    </div>
  );
}
