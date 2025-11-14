// src\entities\showroom\ui\ItemGrid.tsx

type ItemGridProps = {
  items: string[];
  onPrev: () => void;
  onNext: () => void;
};

export const ItemGrid = ({ items, onPrev, onNext }: ItemGridProps) => {
  return (
    <div className="relative">
      {/* prev 버튼 */}
      <button
        className="absolute top-1/2 left-0 h-12 w-12 -translate-y-1/2 rounded-full border bg-white"
        onClick={onPrev}
      >
        prev
      </button>

      {/* grid */}
      <div className="border-primary mt-1 grid grid-cols-3 gap-4 p-4">
        {items.map((item, idx) => (
          <div key={idx} className="flex h-20 w-20 items-center justify-center border">
            {item || ""}
          </div>
        ))}
      </div>

      {/* next 버튼 */}
      <button
        className="absolute top-1/2 right-0 h-12 w-12 -translate-y-1/2 rounded-full border bg-white"
        onClick={onNext}
      >
        next
      </button>
    </div>
  );
};
