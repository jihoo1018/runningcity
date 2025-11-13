import { Showroom } from "../model/type";

type Props = { tab: "me" | "friend" | "global"; data: Showroom };

export const CharacterCard = ({ tab, data }: Props) => {
  return (
    <div className="border-primary w-80 overflow-hidden rounded-xl border">
      {/* LV 영역 */}
      <div className="p-4 text-lg font-bold">LV 72</div>

      {/* 캐릭터 이미지 */}
      <div className="flex h-48 items-center justify-center bg-gray-100">
        <div className="text-gray-500">캐릭터 이미지</div>
      </div>

      {/* 닉네임 */}
      <div className="p-4 text-xl font-semibold">
        {tab === "friend" ? "러닝지존 친구" : "러닝zi존"}
      </div>

      {/* 프로필 정보 */}
      <div className="space-y-1 px-4 pb-4 text-sm">
        <div>총 러닝 2142km</div>
        <div>최장 거리 10km</div>
        <div>평균 페이스 8m 21s</div>
        <div>최고 기록 6m 34s</div>
        <div>해킹 3423회</div>
      </div>
    </div>
  );
};
