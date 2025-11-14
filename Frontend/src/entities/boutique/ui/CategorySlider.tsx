// src/entities/boutique/ui/CategorySlider.tsx
import { useRef } from 'react';
import type { StoreResponse, ItemCategory } from '../model/types';
import { SpritePreview } from './SpritePreview';

interface CategorySliderProps {
  category: ItemCategory;
  categoryLabel: string;
  categoryIcon: string;
  items: StoreResponse[];
  userCurrency: number;
  onPurchaseClick: (item: StoreResponse) => void;
}

export function CategorySlider({
  category,
  categoryLabel,
  categoryIcon,
  items,
  userCurrency,
  onPurchaseClick,
}: CategorySliderProps) {
  const scrollRef = useRef<HTMLDivElement>(null);

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

  const scrollLeft = () => {
    if (scrollRef.current) {
      scrollRef.current.scrollBy({ left: -200, behavior: 'smooth' });
    }
  };

  const scrollRight = () => {
    if (scrollRef.current) {
      scrollRef.current.scrollBy({ left: 200, behavior: 'smooth' });
    }
  };

  if (items.length === 0) {
    return null;
  }

  return (
    <div className="mb-6">
      {/* 카테고리 헤더 */}
      <div className="flex items-center justify-between mb-3">
        <h3 className="text-content text-custom-white font-bold">
          {categoryIcon} {categoryLabel}
        </h3>
        <div className="flex gap-2">
          <button
            onClick={scrollLeft}
            className="w-8 h-8 bg-section-bg border border-primary/30 rounded-full flex items-center justify-center text-custom-white hover:bg-primary/10 transition-colors"
          >
            ←
          </button>
          <button
            onClick={scrollRight}
            className="w-8 h-8 bg-section-bg border border-primary/30 rounded-full flex items-center justify-center text-custom-white hover:bg-primary/10 transition-colors"
          >
            →
          </button>
        </div>
      </div>

      {/* 아이템 슬라이더 */}
      <div
        ref={scrollRef}
        className="flex gap-3 overflow-x-auto scrollbar-hide pb-2"
        style={{ scrollbarWidth: 'none', msOverflowStyle: 'none' }}
      >
        {items.map((item) => (
          <div
            key={item.itemId}
            className={`flex-shrink-0 w-32 bg-section-bg border border-primary/20 rounded-lg overflow-hidden ${
              item.isOwned ? 'opacity-60' : ''
            }`}
          >
            {/* 아이템 이미지 */}
            <div className="aspect-square bg-gradient-to-br from-gray-700 to-gray-800 flex items-center justify-center relative overflow-hidden">
              {/* 체크보드 패턴 배경 */}
              <div 
                className="absolute inset-0 opacity-30"
                style={{
                  backgroundImage: `
                    linear-gradient(45deg, #4a5568 25%, transparent 25%),
                    linear-gradient(-45deg, #4a5568 25%, transparent 25%),
                    linear-gradient(45deg, transparent 75%, #4a5568 75%),
                    linear-gradient(-45deg, transparent 75%, #4a5568 75%)
                  `,
                  backgroundSize: '20px 20px',
                  backgroundPosition: '0 0, 0 10px, 10px -10px, -10px 0px'
                }}
              />
              <div className="relative z-10">
                <SpritePreview 
                  basePath={item.basePath}
                  category={category}
                />
              </div>
            </div>

            {/* 아이템 정보 */}
            <div className="p-2">
              <div className="mb-1">
                <h4 className="text-xs text-custom-white font-medium truncate">
                  {item.name}
                </h4>
                <span className={`inline-block px-1 py-0.5 rounded text-xs font-medium ${rarityColors[item.rarity]}`}>
                  {rarityLabels[item.rarity]}
                </span>
              </div>

              <div className="flex items-center justify-between">
                <span className="text-primary font-bold text-sm">
                  {item.priceCr.toLocaleString()} CR
                </span>
                
                {item.isOwned ? (
                  <span className="text-xs text-primary font-medium">소유</span>
                ) : (
                  <button
                    onClick={() => onPurchaseClick(item)}
                    disabled={userCurrency < item.priceCr}
                    className={`px-2 py-1 rounded text-xs font-medium transition-colors ${
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

      <style>{`
        .scrollbar-hide::-webkit-scrollbar {
          display: none;
        }
      `}</style>
    </div>
  );
}
