// src/entities/boutique/ui/StoreSection.tsx
import { useState } from 'react';
import { CategorySlider } from './CategorySlider';
import type { StoreResponse, ItemCategory } from '../model/types';

interface StoreSectionProps {
  items: StoreResponse[];
  userCurrency: number;
  onPurchaseClick: (item: StoreResponse) => void;
}

export function StoreSection({ items, userCurrency, onPurchaseClick }: StoreSectionProps) {
  const categories = [
    { value: 'bodies' as const, label: '몸', icon: '👤' },
    { value: 'clothes' as const, label: '옷', icon: '👕' },
    { value: 'hair' as const, label: '머리카락', icon: '💇' },
    { value: 'head' as const, label: '머리장식', icon: '🎭' },
  ];

  const [selectedCategory, setSelectedCategory] = useState<ItemCategory>('bodies');

  // 카테고리별로 아이템 그룹화
  const groupedItems = categories.reduce((acc, category) => {
    acc[category.value] = items.filter(item => item.category === category.value);
    return acc;
  }, {} as Record<ItemCategory, StoreResponse[]>);

  // 선택된 카테고리 정보
  const selectedCategoryInfo = categories.find(c => c.value === selectedCategory)!;

  return (
    <div>
      <div className="text-center mb-4">
        <h2 className="text-subtitle text-custom-white mb-2">🛒 상점</h2>
        <p className="text-desc text-custom-gray">직접 구매할 수 있는 아이템들</p>
      </div>

      {/* 카테고리 탭 */}
      <div className="mb-4 overflow-x-auto scrollbar-hide">
        <div className="flex gap-2 min-w-max pb-2">
          {categories.map((category) => (
            <button
              key={category.value}
              onClick={() => setSelectedCategory(category.value)}
              className={`flex items-center gap-2 px-4 py-2 rounded-lg font-medium transition-all ${
                selectedCategory === category.value
                  ? 'bg-primary text-custom-black'
                  : 'bg-section-bg border border-primary/30 text-custom-white hover:border-primary/50'
              }`}
            >
              <span>{category.icon}</span>
              <span className="text-content">{category.label}</span>
              <span className={`text-xs px-1.5 py-0.5 rounded ${
                selectedCategory === category.value 
                  ? 'bg-custom-black/20 text-custom-black' 
                  : 'bg-primary/20 text-primary'
              }`}>
                {groupedItems[category.value].length}
              </span>
            </button>
          ))}
        </div>
      </div>

      {/* 선택된 카테고리의 아이템 슬라이더 */}
      <CategorySlider
        category={selectedCategory}
        categoryLabel={selectedCategoryInfo.label}
        categoryIcon={selectedCategoryInfo.icon}
        items={groupedItems[selectedCategory]}
        userCurrency={userCurrency}
        onPurchaseClick={onPurchaseClick}
      />

      {/* 빈 상태 */}
      {items.length === 0 && (
        <div className="text-center py-8">
          <div className="text-4xl mb-3">🛍️</div>
          <h3 className="text-content text-custom-white mb-2">아이템이 없습니다</h3>
          <p className="text-desc text-custom-gray">곧 새로운 아이템이 추가될 예정입니다.</p>
        </div>
      )}

      <style>{`
        .scrollbar-hide::-webkit-scrollbar {
          display: none;
        }
        .scrollbar-hide {
          scrollbar-width: none;
          -ms-overflow-style: none;
        }
      `}</style>
    </div>
  );
}
