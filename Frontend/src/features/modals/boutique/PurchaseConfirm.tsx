// src/features/modals/boutique/PurchaseConfirm.tsx
import { useState } from 'react';
import { ModalProps } from "@/app/modal/types";
import { useModalRouter } from "@/app/modal/useModalRouter";
import { Modal } from "@/shared/ui";
import { CommonButton } from "@/shared/ui";
import { purchaseItem, getStoreItems, getUserCurrency } from '@/entities/boutique/api';
import { useAuthStore } from '@/features/auth/model/useAuthStore';
import type { StoreResponse } from '@/entities/boutique/model/types';
import { AndroidBridge } from '@/shared/lib/webview';
import { SpritePreview } from '@/entities/boutique/ui/SpritePreview';

// 구매 확인 모달에 전달될 데이터 타입
type PurchasePayload = {
  item: StoreResponse;
  userCurrency: number;
};

export default function BoutiquePurchaseConfirm({ onClose, payload }: ModalProps) {
  const { user } = useAuthStore();
  const [isPurchasing, setIsPurchasing] = useState(false);

  // payload에서 데이터 추출
  const data = (payload as PurchasePayload | undefined) ?? undefined;
  const item = data?.item;
  const userCurrency = data?.userCurrency ?? 0;

  if (!item || !user?.userId) {
    return (
      <Modal open onClose={onClose} title="오류">
        <p className="text-red-500">잘못된 접근입니다.</p>
      </Modal>
    );
  }

  const canAfford = userCurrency >= item.priceCr;
  const remainingCurrency = userCurrency - item.priceCr;

  const handlePurchase = async () => {
    try {
      setIsPurchasing(true);
      
      const response = await purchaseItem(user.userId, {
        itemId: item.itemId,
      });

      if (response.status === 200) {
        AndroidBridge.showToast(`${item.name}을(를) 구매했습니다!`);
        
        // 구매 성공 이벤트 발생
        window.dispatchEvent(new CustomEvent('boutique:purchase-success', { 
          detail: { itemId: item.itemId } 
        }));
        
        // 모달 닫기
        onClose();
      } else {
        throw new Error(response.message || '구매에 실패했습니다.');
      }
    } catch (error) {
      console.error('구매 실패:', error);
      AndroidBridge.showToast('구매에 실패했습니다.');
    } finally {
      setIsPurchasing(false);
    }
  };

  return (
    <Modal 
      open 
      onClose={onClose} 
      title="구매 확인"
      closeOnBackdrop={!isPurchasing} // 구매 중일 때는 백드롭 클릭으로 닫기 방지
      footer={
        <div className="flex gap-3 w-full">
          <CommonButton
            variant="outline"
            onClick={onClose}
            disabled={isPurchasing}
            className="flex-1"
          >
            취소
          </CommonButton>
          <CommonButton
            variant="solid"
            onClick={handlePurchase}
            disabled={!canAfford || isPurchasing}
            className="flex-1"
          >
            {isPurchasing ? (
              <div className="flex items-center justify-center gap-2">
                <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                구매 중...
              </div>
            ) : (
              '구매하기'
            )}
          </CommonButton>
        </div>
      }
    >
      <div className="space-y-4">
        {/* 아이템 이미지 */}
        <div className="flex justify-center">
          <div className="w-32 h-32 bg-custom-black/50 rounded-lg overflow-hidden border border-primary/30">
            <SpritePreview 
              basePath={item.basePath}
              category={item.category}
              className="w-full h-full"
            />
          </div>
        </div>

        {/* 아이템 정보 */}
        <div className="bg-section-bg border border-primary/20 rounded-lg p-4">
          <div className="text-center mb-3">
            <h3 className="text-lg font-bold text-custom-white mb-1">{item.name}</h3>
            <p className="text-sm text-custom-gray">
              {item.subcategory} {item.style && `• ${item.style}`}
            </p>
          </div>
          
          <div className="text-center pt-3 border-t border-primary/20">
            <p className="text-2xl font-bold text-primary mb-1">
              {item.priceCr.toLocaleString()} CR
            </p>
            <p className="text-xs text-custom-gray">구매 가격</p>
          </div>
        </div>

        {/* 잔액 정보 */}
        <div className="bg-custom-black/30 rounded-lg p-4 space-y-2">
          <div className="flex justify-between items-center">
            <span className="text-sm text-custom-gray">현재 잔액:</span>
            <span className="font-medium text-custom-white">{userCurrency.toLocaleString()} CR</span>
          </div>
          <div className="flex justify-between items-center">
            <span className="text-sm text-custom-gray">구매 가격:</span>
            <span className="font-medium text-accent-red">-{item.priceCr.toLocaleString()} CR</span>
          </div>
          <div className="h-px bg-primary/20 my-2" />
          <div className="flex justify-between items-center">
            <span className="text-sm font-bold text-custom-white">구매 후 잔액:</span>
            <span className={`font-bold text-lg ${canAfford ? 'text-primary' : 'text-accent-red'}`}>
              {remainingCurrency.toLocaleString()} CR
            </span>
          </div>
        </div>

        {/* 경고 메시지 */}
        {!canAfford && (
          <div className="bg-accent-red/20 border border-accent-red/50 rounded-lg p-3">
            <p className="text-sm text-accent-red text-center font-medium">
              💰 CR이 부족합니다!
            </p>
          </div>
        )}
      </div>
    </Modal>
  );
}
