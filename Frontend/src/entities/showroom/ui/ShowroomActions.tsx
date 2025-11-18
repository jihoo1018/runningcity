import { useNavigate } from "react-router-dom";

type Props = {
  tab: "me" | "friend" | "global";
  onNext?: () => void;
  onNextGlobal?: () => void;
  onChangeClothes?: () => void;
  onEditInfo?: () => void;
};
export const ShowroomActions = ({ tab, onNext, onNextGlobal }: Props) => {
  const navigate = useNavigate();

  const neonButton = `
    bg-primary/20
    px-6 py-3 rounded-xl font-semibold transition-all
    border border-cyan-400 text-cyan-200
    bg-[#0f1624]/60
    hover:border-cyan-300 hover:text-cyan-100
    hover:shadow-[0_0_10px_rgba(0,255,255,0.6)]
  `;

  const neonPrimary = `
    px-6 py-3 rounded-xl font-semibold
    bg-gradient-to-b from-cyan-400 to-cyan-600
    text-black shadow-[0_0_15px_rgba(0,255,255,0.6)]
  `;

  if (tab === "friend") {
    return (
      <div className="mb-4 flex justify-center">
        <button className={neonPrimary} onClick={onNext}>
          next ▶
        </button>
      </div>
    );
  }

  if (tab === "global") {
    return (
      <div className="mb-4 flex justify-center">
        <button className={neonPrimary} onClick={onNextGlobal}>
          next ▶
        </button>
      </div>
    );
  }
  return (
    <div className="flex justify-center gap-3">
      <button className={neonButton} onClick={() => navigate("/showroom/customize")}>
        옷 갈아입기
      </button>
      <button className={neonButton} onClick={() => navigate("/showroom/edit")}>
        사무실 정보 수정
      </button>
    </div>
  );
};
