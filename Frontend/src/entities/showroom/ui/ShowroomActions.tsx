import { useNavigate } from "react-router-dom";

type Props = {
  tab: "me" | "friend" | "global";
  onNext?: () => void;
  onNextGlobal?: () => void;
  onChangeClothes?: () => void;
  onEditInfo?: () => void;
};

export const ShowroomActions = ({
  tab,
  onNext, // 쇼룸 다음버튼 클릭이벤트
  onNextGlobal, // 글로벌 쇼룸 다음버튼 클릭이벤트
}: Props) => {
  const navigate = useNavigate();

  // 네비게이션 이동 함수
  const goChangeClothes = () => navigate("/showroom/customize"); // 옷 갈아입기 버튼 클릭이벤트
  const goEditInfo = () => navigate("/showroom/edit"); //사무실 정보 수정 버튼 클릭이벤트

  if (tab === "friend") {
    // 쇼룸
    return (
      <div className="mb-4 flex justify-center">
        <button className="border-primary rounded-xl border px-4 py-2 shadow" onClick={onNext}>
          next ▶
        </button>
      </div>
    );
  } else if (tab === "global") {
    // 글로벌
    return (
      <div className="mb-4 flex justify-center">
        <button
          className="border-primary rounded-xl border px-4 py-2 shadow"
          onClick={onNextGlobal}
        >
          next ▶
        </button>
      </div>
    );
  } else {
    // 내 사무실
    return (
      <div className="my-4 flex justify-center gap-4">
        <button className="rounded-xl border px-6 py-3 text-sm" onClick={goChangeClothes}>
          옷 갈아입기
        </button>
        <button className="rounded-xl border px-6 py-3 text-sm" onClick={goEditInfo}>
          사무실 정보 수정
        </button>
      </div>
    );
  }
};
