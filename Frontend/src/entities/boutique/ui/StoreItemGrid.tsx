// src/entities/boutique/ui/StoreItemGrid.tsx
import type { StoreResponse } from '../model/types';

interface StoreItemGridProps {
  items: StoreResponse[];
  userCurrency: number;
  onPurchaseClick: (item: StoreResponse) => void;
}

export function StoreItemGrid({ items, userCurrency, onPurchaseClick }: StoreItemGridProps) {
  // 희귀도별 색상 매핑
  const rarityColors = {
    common: 'text-custom-gray bg-custom-gray/20',
    rare: 'text-blue-400 bg-blue-400/20', 
    epic: 'text-purple-400 bg-purple-400/20',
    legendary: 'text-orange-400 bg-orange-400/20',
  };

  // 희귀도별 라벨
  const rarityLabels = {
    common: '일반',
    rare: '희귀',
    epic: '에픽', 
    legendary: '전설',
  };

  if (items.length === 0) {
    return (
      <div className="text-center py-8">
        <div className="text-4xl mb-3">🛍️</div>
        <h3 className="text-content text-custom-white mb-2">아이템이 없습니다</h3>
        <p className="text-desc text-custom-gray">다른 필터를 시도해보세요.</p>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-2 gap-3">
      {items.map((item) => (
        <div
          key={item.itemId}
          className={`bg-section-bg border border-primary/20 rounded-lg overflow-hidden ${
            item.isPurchased ? 'opacity-60' : ''
          }`}
        >
          {/* 아이템 이미지 영역 */}
          <div className="aspect-square bg-custom-black/50 flex items-center justify-center">
            <div className="text-3xl">
              {item.category === 'bodies' && '👤'}
              {item.category === 'clothes' && '👕'}
              {item.category === 'hair' && '💇'}
              {item.category === 'head' && '🎭'}
            </div>
          </div>

          {/* 아이템 정보 */}
          <div className="p-3">
            <div className="flex items-start justify-between mb-2">
              <h3 className="text-content text-custom-white text-sm font-medium truncate flex-1">
                {item.name}
              </h3>
              <span className={`px-2 py-1 rounded-full text-xs font-medium ml-2 ${rarityColors[item.rarity]}`}>
                {rarityLabels[item.rarity]}
              </span>
            </div>

            <p className="text-desc text-custom-gray mb-3 text-xs">
              {item.subcategory} {item.style && `• ${item.style}`}
            </p>

            <div className="flex items-center justify-between">
              <span className="text-primary font-bold text-sm">
                {item.priceCr.toLocaleString()} CR
              </span>
              
              {item.isPurchased ? (
                <span className="text-xs text-primary font-medium">소유중</span>
              ) : (
                <button
                  onClick={() => onPurchaseClick(item)}
                  disabled={userCurrency < item.priceCr}
                  className={`px-3 py-1 rounded text-xs font-medium transition-colors ${
                    userCurrency >= item.priceCr
                      ? 'bg-primary text-custom-black hover:bg-primary/80'
                      : 'bg-custom-gray/30 text-custom-gray cursor-not-allowed'
                  }`}
                >
                  구매
                </button>
              )}
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}
