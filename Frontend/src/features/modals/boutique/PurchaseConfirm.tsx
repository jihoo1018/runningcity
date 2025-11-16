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
      <div className="space-y-6">
        {/* 아이템 정보 */}
        <div className="border rounded-lg p-4">
          <div className="flex items-center gap-3 mb-3">
            <div className="w-12 h-12 bg-gray-100 rounded-lg flex items-center justify-center">
              <span className="text-2xl">
                {item.category === 'bodies' && '👤'}
                {item.category === 'clothes' && '👕'}
                {item.category === 'hair' && '💇'}
                {item.category === 'head' && '🎭'}
              </span>
            </div>
            <div className="text-left">
              <h3 className="font-medium text-gray-900">{item.name}</h3>
              <p className="text-sm text-gray-500">
                {item.subcategory} {item.style && `• ${item.style}`}
              </p>
            </div>
          </div>
          
          <div className="text-center">
            <p className="text-2xl font-bold text-blue-600 mb-1">
              {item.priceCr.toLocaleString()} CR
            </p>
            <p className="text-sm text-gray-500">구매 가격</p>
          </div>
        </div>

        {/* 잔액 정보 */}
        <div className="bg-gray-50 rounded-lg p-4">
          <div className="flex justify-between items-center mb-2">
            <span className="text-sm text-gray-600">현재 잔액:</span>
            <span className="font-medium">{userCurrency.toLocaleString()} CR</span>
          </div>
          <div className="flex justify-between items-center mb-2">
            <span className="text-sm text-gray-600">구매 가격:</span>
            <span className="font-medium text-red-600">-{item.priceCr.toLocaleString()} CR</span>
          </div>
          <hr className="my-2" />
          <div className="flex justify-between items-center">
            <span className="text-sm font-medium text-gray-900">구매 후 잔액:</span>
            <span className={`font-bold ${canAfford ? 'text-green-600' : 'text-red-600'}`}>
              {remainingCurrency.toLocaleString()} CR
            </span>
          </div>
        </div>

        {/* 경고 메시지 */}
        {!canAfford && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-3">
            <p className="text-sm text-red-600 text-center">
              💰 CR이 부족합니다!
            </p>
          </div>
        )}
      </div>
    </Modal>
  );
}
