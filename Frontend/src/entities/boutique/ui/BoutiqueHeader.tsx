// src/entities/boutique/ui/BoutiqueHeader.tsx
interface BoutiqueHeaderProps {
  userCurrency: number;
}

export function BoutiqueHeader({ userCurrency }: BoutiqueHeaderProps) {
  return (
    <div className="mb-6">
      <h1 className="text-game-title text-custom-white text-center mb-4">🏪 부티크</h1>
      <div className="text-center">
        <div className="inline-flex items-center bg-primary/20 px-4 py-2 rounded-full border border-primary/30">
          <span className="text-primary font-bold text-lg">💰 {userCurrency.toLocaleString()} CR</span>
        </div>
      </div>
    </div>
  );
}
