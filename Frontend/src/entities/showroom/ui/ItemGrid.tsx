// src/entities/showroom/ui/ItemGrid.tsx
import { LPCPreviewRenderer } from "./LPCPreviewRenderer";
import { InventoryItem } from "@/entities/showroom/model/type";

type Props = {
  items: InventoryItem[];
  onPrev: () => void;
  onNext: () => void;
  onSelect: (item: InventoryItem) => void;
  disablePrev: boolean;
  disableNext: boolean;
};

export const ItemGrid = ({ items, onPrev, onNext, onSelect, disablePrev, disableNext }: Props) => {
  return (
    <div className="relative flex h-full w-full items-start justify-center">
      {/* Prev 버튼 */}
      <button
        disabled={disablePrev}
        className={`bg-primary absolute top-1/2 left-1 h-8 w-8 -translate-y-1/2 rounded-full ${disablePrev ? "cursor-not-allowed opacity-30" : ""}`}
        onClick={onPrev}
      >
        ◀
      </button>
      {/* Grid */}
      <div className="grid w-full grid-cols-3 gap-3 px-2 py-2 sm:grid-cols-3 md:grid-cols-4">
        {items.map((item, idx) => (
          <div
            key={item.inventoryId ?? item.itemId} // 이걸로 해야 안전
            onClick={() => onSelect(item)}
            className="border-primary flex flex-col rounded-xl border p-2 shadow-lg transition"
          >
            <div className="flex flex-1 items-center justify-center rounded-md bg-black/20">
              <LPCPreviewRenderer basePath={item.basePath} />
            </div>

            <div className="mt-1 h-7 text-center text-[10px] text-white">{item.name}</div>
          </div>
        ))}
      </div>

      {/* Next 버튼 */}
      <button
        disabled={disableNext}
        className={`bg-primary absolute top-1/2 right-1 h-8 w-8 -translate-y-1/2 rounded-full ${disableNext ? "cursor-not-allowed opacity-30" : ""}`}
        onClick={onNext}
      >
        ▶
      </button>
    </div>
  );
};
