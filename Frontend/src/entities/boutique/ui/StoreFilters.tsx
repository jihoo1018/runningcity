// src/entities/boutique/ui/StoreFilters.tsx
import type { ItemCategory, SortOption } from '../model/types';

interface StoreFiltersProps {
  selectedCategory: ItemCategory | 'all';
  sortBy: SortOption;
  showOwnedItems: boolean;
  onCategoryChange: (category: ItemCategory | 'all') => void;
  onSortChange: (sort: SortOption) => void;
  onShowOwnedChange: (show: boolean) => void;
}

export function StoreFilters({
  selectedCategory,
  sortBy,
  showOwnedItems,
  onCategoryChange,
  onSortChange,
  onShowOwnedChange,
}: StoreFiltersProps) {
  const categories = [
    { value: 'all' as const, label: '전체', icon: '🏪' },
    { value: 'bodies' as const, label: '몸', icon: '👤' },
    { value: 'clothes' as const, label: '옷', icon: '👕' },
    { value: 'hair' as const, label: '머리카락', icon: '💇' },
    { value: 'head' as const, label: '머리장식', icon: '🎭' },
  ];

  const sortOptions = [
    { value: 'name' as const, label: '이름순' },
    { value: 'price_low' as const, label: '가격 낮은순' },
    { value: 'price_high' as const, label: '가격 높은순' },
    { value: 'rarity' as const, label: '희귀도순' },
  ];

  return (
    <div className="bg-section-bg border border-primary/30 rounded-lg p-4 mb-4">
      {/* 카테고리 필터 */}
      <div className="mb-4">
        <h3 className="text-label text-custom-white mb-2">카테고리</h3>
        <div className="flex flex-wrap gap-2">
          {categories.map((category) => (
            <button
              key={category.value}
              onClick={() => onCategoryChange(category.value)}
              className={`px-3 py-2 rounded-full text-xs font-medium transition-colors ${
                selectedCategory === category.value
                  ? 'bg-primary text-custom-black'
                  : 'bg-custom-black border border-primary/30 text-custom-gray hover:bg-primary/10 hover:text-primary'
              }`}
            >
              {category.icon} {category.label}
            </button>
          ))}
        </div>
      </div>

      {/* 정렬 및 옵션 */}
      <div className="flex flex-wrap items-center gap-4 text-xs">
        <div className="flex items-center gap-2">
          <label className="text-label text-custom-white">정렬:</label>
          <select
            value={sortBy}
            onChange={(e) => onSortChange(e.target.value as SortOption)}
            className="px-2 py-1 border border-primary/30 rounded text-xs bg-custom-black text-custom-white"
          >
            {sortOptions.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>

        <label className="flex items-center gap-2 text-xs text-custom-white">
          <input
            type="checkbox"
            checked={showOwnedItems}
            onChange={(e) => onShowOwnedChange(e.target.checked)}
            className="rounded accent-primary"
          />
          소유 아이템 포함
        </label>
      </div>
    </div>
  );
}
