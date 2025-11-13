import { useState } from "react";
import { ShowroomTabs } from "@/pages/showroom/ShowroomTabs";
import { CharacterCard } from "@/pages/showroom/CharacterCard";
import { ShowroomActions } from "@/pages/showroom/ShowroomActions";
import { onNext, onNextGlobal } from "@/entities/showroom/api/me";

const ShowroomPage = () => {
  const [tab, setTab] = useState<"me" | "friend" | "global">("me");

  return (
    <div className="flex min-h-screen flex-col">
      {/* 상단 탭 */}
      <ShowroomTabs tab={tab} setTab={setTab} />

      {/* 캐릭터 카드 */}
      <div className="flex items-start justify-center py-4">
        <CharacterCard tab={tab} />
      </div>

      {/* 하단 액션 버튼 */}
      <ShowroomActions tab={tab} onNext={onNext} onNextGlobal={onNextGlobal} />
    </div>
  );
};

export default ShowroomPage;
