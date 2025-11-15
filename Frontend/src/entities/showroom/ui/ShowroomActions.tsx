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

  const goChangeClothes = () => navigate("/showroom/customize");
  const goEditInfo = () => navigate("/showroom/edit");

  if (tab === "friend") {
    return (
      <div className="mb-2 flex justify-center">
        <button
          className="rounded-xl border px-4 py-2 text-sm shadow sm:text-base"
          onClick={onNext}
        >
          next ▶
        </button>
      </div>
    );
  }

  if (tab === "global") {
    return (
      <div className="mb-2 flex justify-center">
        <button
          className="rounded-xl border px-4 py-2 text-sm shadow sm:text-base"
          onClick={onNextGlobal}
        >
          next ▶
        </button>
      </div>
    );
  }

  return (
    <div className="my-3 flex justify-center gap-2 sm:gap-4">
      <button
        className="rounded-xl border px-4 py-2 text-sm sm:px-6 sm:py-3 sm:text-base"
        onClick={goChangeClothes}
      >
        옷 갈아입기
      </button>
      <button
        className="rounded-xl border px-4 py-2 text-sm sm:px-6 sm:py-3 sm:text-base"
        onClick={goEditInfo}
      >
        사무실 정보 수정
      </button>
    </div>
  );
};
