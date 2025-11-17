// src/features/modals/boutique/PurchaseConfirm.tsx
import { useState } from "react";
import { ModalProps } from "@/app/modal/types";
import { useModalRouter } from "@/app/modal/useModalRouter";
import { Modal } from "@/shared/ui";
import { CommonButton } from "@/shared/ui";
import { purchaseItem, getStoreItems, getUserCurrency } from "@/entities/boutique/api";
import { useAuthStore } from "@/features/auth/model/useAuthStore";
import type { StoreResponse } from "@/entities/boutique/model/types";
import { AndroidBridge } from "@/shared/lib/webview";
import { SpritePreview } from "@/entities/boutique/ui/SpritePreview";

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
        window.dispatchEvent(
          new CustomEvent("boutique:purchase-success", {
            detail: { itemId: item.itemId },
          }),
        );

        // 모달 닫기
        onClose();
      } else {
        throw new Error(response.message || "구매에 실패했습니다.");
      }
    } catch (error) {
      console.error("구매 실패:", error);
      AndroidBridge.showToast("구매에 실패했습니다.");
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
        <div className="flex w-full gap-3">
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
                <div className="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent"></div>
                구매 중...
              </div>
            ) : (
              "구매하기"
            )}
          </CommonButton>
        </div>
      }
    >
      <div className="space-y-4">
        {/* 아이템 이미지 */}
        <div className="flex justify-center">
          <div className="bg-custom-black/50 border-primary/30 h-32 w-32 overflow-hidden rounded-lg border">
            <SpritePreview
              basePath={item.basePath}
              category={item.category}
              className="h-full w-full"
            />
          </div>
        </div>

        {/* 아이템 정보 */}
        <div className="bg-section-bg border-primary/20 rounded-lg">
          <div className="text-center">
            <h3 className="text-custom-white mb-1 text-lg font-bold">{item.name}</h3>
          </div>
        </div>

        {/* 잔액 정보 */}
        <div className="bg-custom-black/30 space-y-2 rounded-lg p-4">
          <div className="flex items-center justify-between">
            <span className="text-custom-gray text-sm">구매 가격</span>
            <span className="text-primary text-content-bold">
              {item.priceCr.toLocaleString()} CR
            </span>
          </div>
          <div className="bg-primary/20 my-2 h-px" />
          <div className="flex items-center justify-between">
            <span className="text-custom-white text-sm font-bold">구매 후 잔액</span>
            <span className={`text-desc ${canAfford ? "text-custom-white" : "text-accent-red"}`}>
              {remainingCurrency.toLocaleString()} CR
            </span>
          </div>
        </div>

        {/* 경고 메시지 */}
        {!canAfford && (
          <div className="bg-accent-red/20 border-accent-red/50 rounded-lg border p-3">
            <p className="text-accent-red text-center text-sm font-medium">💰 CR이 부족합니다!</p>
          </div>
        )}
      </div>
    </Modal>
  );
}
