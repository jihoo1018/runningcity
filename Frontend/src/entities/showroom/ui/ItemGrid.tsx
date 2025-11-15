// src\entities\showroom\ui\ItemGrid.tsx
import { LPCPreviewRenderer } from "./LPCPreviewRenderer";
import { ItemGridProps, InventoryItem } from "@/entities/showroom/model/type";

export const ItemGrid = ({ items, onPrev, onNext, onSelect }: ItemGridProps) => {
  return (
    <div className="relative mt-2">
      {/* prev 버튼 */}
      <button
        className="bg-primary absolute top-1/2 left-0 h-7 w-7 -translate-y-1/2 rounded-full shadow sm:h-12 sm:w-12"
        onClick={onPrev}
      >
        ◀
      </button>

      {/* grid */}
      <div className="border-primary mt-1 grid grid-cols-4 gap-3 p-4 sm:grid-cols-4 sm:gap-4 md:grid-cols-5">
        {items.map((item, idx) => (
          <div
            key={idx}
            onClick={() => onSelect(item)}
            className="flex h-16 w-16 items-center justify-center rounded-lg border sm:h-20 sm:w-20 md:h-24 md:w-24"
          >
            <LPCPreviewRenderer basePath={item.basePath} />
          </div>
        ))}
      </div>

      {/* next 버튼 */}
      <button
        className="bg-primary absolute top-1/2 right-0 h-7 w-7 -translate-y-1/2 rounded-full border sm:h-12 sm:w-12"
        onClick={onNext}
      >
        ▶
      </button>
    </div>
  );
};
