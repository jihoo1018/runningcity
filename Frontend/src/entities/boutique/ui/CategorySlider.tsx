// src/entities/boutique/ui/CategorySlider.tsx
import { useRef } from "react";
import type { StoreResponse, ItemCategory } from "../model/types";
import { SpritePreview } from "./SpritePreview";

interface CategorySliderProps {
  category: ItemCategory;
  categoryLabel: string;
  categoryIcon: React.ReactNode;
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
    common: "text-custom-gray bg-custom-gray/20",
    rare: "text-blue-400 bg-blue-400/20",
    epic: "text-purple-400 bg-purple-400/20",
    legendary: "text-orange-400 bg-orange-400/20",
  };

  // 희귀도별 라벨
  const rarityLabels = {
    common: "일반",
    rare: "희귀",
    epic: "에픽",
    legendary: "전설",
  };

  const scrollLeft = () => {
    if (scrollRef.current) {
      scrollRef.current.scrollBy({ left: -200, behavior: "smooth" });
    }
  };

  const scrollRight = () => {
    if (scrollRef.current) {
      scrollRef.current.scrollBy({ left: 200, behavior: "smooth" });
    }
  };

  if (items.length === 0) {
    return null;
  }

  return (
    <div className="mb-6">
      {/* 카테고리 헤더 */}
      <div className="mb-3 flex items-center justify-between">
        <h4 className="text-content text-custom-white flex items-center justify-center gap-2 font-bold">
          {categoryIcon}
          <span>{categoryLabel}</span>
        </h4>
        <div className="flex gap-2">
          <button
            onClick={scrollLeft}
            className="bg-section-bg border-primary/30 text-custom-white hover:bg-primary/10 flex h-8 w-8 items-center justify-center rounded-full border transition-colors"
          >
            ←
          </button>
          <button
            onClick={scrollRight}
            className="bg-section-bg border-primary/30 text-custom-white hover:bg-primary/10 flex h-8 w-8 items-center justify-center rounded-full border transition-colors"
          >
            →
          </button>
        </div>
      </div>

      {/* 아이템 슬라이더 */}
      <div
        ref={scrollRef}
        className="scrollbar-hide flex gap-3 overflow-x-auto pb-2"
        style={{ scrollbarWidth: "none", msOverflowStyle: "none" }}
      >
        {items.map((item) => (
          <div
            key={item.itemId}
            className={`bg-section-bg border-primary/20 w-32 flex-shrink-0 overflow-hidden rounded-lg border ${
              item.isPurchased ? "opacity-60" : ""
            }`}
          >
            {/* 아이템 이미지 */}
            <div className="relative flex aspect-square items-center justify-center overflow-hidden bg-gradient-to-br from-gray-700 to-gray-800">
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
                  backgroundSize: "20px 20px",
                  backgroundPosition: "0 0, 0 10px, 10px -10px, -10px 0px",
                }}
              />
              <div className="relative z-10">
                <SpritePreview basePath={item.basePath} category={category} />
              </div>
            </div>

            {/* 아이템 정보 */}
            <div className="p-2">
              <div className="mb-1">
                <h4 className="text-custom-white truncate text-xs font-medium">{item.name}</h4>
                <span
                  className={`text-desc inline-block rounded px-1 py-0.5 ${rarityColors[item.rarity]}`}
                >
                  {rarityLabels[item.rarity]}
                </span>
              </div>

              <div className="flex items-center justify-between">
                <span className="text-primary text-sm font-bold">
                  {item.priceCr.toLocaleString()} CR
                </span>

                {item.isPurchased ? (
                  <span className="text-primary text-xs font-medium">소유</span>
                ) : (
                  <button
                    onClick={() => onPurchaseClick(item)}
                    disabled={userCurrency < item.priceCr}
                    className={`rounded px-2 py-1 text-xs font-medium transition-colors ${
                      userCurrency >= item.priceCr
                        ? "bg-primary text-custom-black hover:bg-primary/80"
                        : "bg-custom-gray/30 text-custom-gray cursor-not-allowed"
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
