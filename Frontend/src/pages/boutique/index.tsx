import { useState, useEffect, useCallback } from 'react';
import { useAuthStore } from '@/features/auth/model/useAuthStore';
import { getStoreItems, getUserCurrency, drawGacha } from '@/entities/boutique/api';
import { useModalRouter } from '@/app/modal/useModalRouter';
import { BoutiqueHeader, GachaSection, StoreSection } from '@/entities/boutique/ui';
import type { StoreResponse, ItemCategory, SortOption, DrawType } from '@/entities/boutique/model/types';
import { AndroidBridge } from '@/shared/lib/webview';

export default function BoutiquePage() {
  const { user } = useAuthStore();
  const { open } = useModalRouter();
  const [storeItems, setStoreItems] = useState<StoreResponse[]>([]);
  const [userCurrency, setUserCurrency] = useState<number>(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // 데이터 로드
  useEffect(() => {
    if (!user?.userId) return;

    const loadData = async () => {
      try {
        setLoading(true);
        setError(null);

        const [storeResponse, currencyResponse] = await Promise.all([
          getStoreItems(user.userId),
          getUserCurrency(user.userId),
        ]);

        if (storeResponse.status === 200) {
          setStoreItems(storeResponse.data);
        } else {
          throw new Error(storeResponse.message || '스토어 아이템을 불러오는데 실패했습니다.');
        }

        if (currencyResponse.status === 200) {
          setUserCurrency(currencyResponse.data.cr);
        }

      } catch (err) {
        console.error('부티크 데이터 로드 실패:', err);
        setError(err instanceof Error ? err.message : '데이터를 불러오는데 실패했습니다.');
        AndroidBridge.showToast('데이터를 불러오는데 실패했습니다.');
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, [user?.userId]);

  // 데이터 새로고침 함수 (useCallback으로 메모이제이션)
  const refreshData = useCallback(async () => {
    if (!user?.userId) return;

    try {
      const [updatedStoreResponse, updatedCurrencyResponse] = await Promise.all([
        getStoreItems(user.userId),
        getUserCurrency(user.userId),
      ]);

      if (updatedStoreResponse.status === 200) {
        setStoreItems(updatedStoreResponse.data);
      }

      if (updatedCurrencyResponse.status === 200) {
        setUserCurrency(updatedCurrencyResponse.data.cr);
      }
    } catch (error) {
      console.error('데이터 새로고침 실패:', error);
    }
  }, [user?.userId]);

  // 구매 성공 이벤트 리스너
  useEffect(() => {
    const handlePurchaseSuccess = (e: Event) => {
      const customEvent = e as CustomEvent;
      console.log('[부티크] 구매 성공 이벤트 수신:', customEvent.detail);
      refreshData();
    };

    window.addEventListener('boutique:purchase-success', handlePurchaseSuccess);

    return () => {
      window.removeEventListener('boutique:purchase-success', handlePurchaseSuccess);
    };
  }, [refreshData]);

  // 구매 모달 열기
  const handlePurchaseClick = (item: StoreResponse) => {
    open("boutique", "purchaseConfirm", {
      item,
      userCurrency,
      // 함수는 History API에서 직렬화할 수 없으므로 제거
    });
  };

  // 가챠 뽑기 핸들러
  const handleGachaPull = async (type: DrawType) => {
    if (!user?.userId) return;

    const price = type === 'single' ? 50 : 450;
    const count = type === 'single' ? 1 : 10;
    
    if (userCurrency < price) {
      AndroidBridge.showToast('CR이 부족합니다!');
      return;
    }

    try {
      const response = await drawGacha(user.userId, { drawType: type });

      if (response.status === 200) {
        AndroidBridge.showToast(`${count}뽑 가챠 완료!`);
        
        // 가챠 결과 모달 열기
        open("boutique", "gachaResult", {
          result: response.data,
        });

        // CR 업데이트
        setUserCurrency(response.data.remainingCredit);
        
        // 스토어 아이템 새로고침 (새로 얻은 아이템이 구매 목록에 반영되도록)
        refreshData();
      } else {
        throw new Error(response.message || '가챠에 실패했습니다.');
      }
    } catch (error) {
      console.error('가챠 실패:', error);
      AndroidBridge.showToast('가챠에 실패했습니다.');
    }
  };

  if (!user) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-custom-black">
        <p className="text-custom-gray">로그인이 필요합니다.</p>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-custom-black">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4"></div>
          <p className="text-custom-gray">부티크 로딩 중...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-custom-black">
        <div className="text-center">
          <p className="text-accent-red mb-4">{error}</p>
          <button 
            onClick={() => window.location.reload()}
            className="px-4 py-2 bg-primary text-custom-black rounded hover:bg-primary/80 transition-colors"
          >
            다시 시도
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="flex h-full w-full flex-col gap-4">
      {/* 헤더 */}
      <BoutiqueHeader userCurrency={userCurrency} />
      
      {/* 가챠 섹션 */}
      <GachaSection 
        userCurrency={userCurrency}
        onGachaPull={handleGachaPull}
      />
      
      {/* 상점 섹션 */}
      <div className="flex-1">
        <StoreSection
          items={storeItems}
          userCurrency={userCurrency}
          onPurchaseClick={handlePurchaseClick}
        />
      </div>
    </div>
  );
}
