// src/features/modals/boutique/GachaResult.tsx
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

  if (!result) {
    return (
      <Modal open onClose={onClose} title="오류">
        <p className="text-accent-red">가챠 결과를 불러올 수 없습니다.</p>
      </Modal>
    );
  }

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
      title={`${result.drawType === 'single' ? '1' : '10'}뽑 가챠 결과`}
      footer={
        <CommonButton
          variant="solid"
          onClick={onClose}
          className="w-full"
        >
          확인
        </CommonButton>
      }
    >
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
    </Modal>
  );
}

