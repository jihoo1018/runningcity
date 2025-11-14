import { Showroom } from "../model/type";
import { metersToKm, formatPace, formatDuration } from "@/shared/lib/format";

type Props = { tab: "me" | "friend" | "global"; data: Showroom };

export const CharacterCard = ({ tab, data }: Props) => {
  return (
    <div className="border-primary w-80 overflow-hidden rounded-xl border">
      {/* LV 영역 */}
      <div className="p-4 text-lg font-bold">LV {data.userLv}</div>

      {/* 캐릭터 이미지 */}
      <div className="flex h-48 items-center justify-center bg-gray-100">
        <div className="text-gray-500">캐릭터 이미지</div>
      </div>

      {/* 닉네임 */}
      <div className="p-4 text-xl font-semibold">{data.userNm}</div>

      {/* 프로필 정보 */}
      <div className="border-primary mb-4 grid grid-cols-3 gap-3 border-t pt-4 pr-3 pl-3 text-sm">
        <div>
          <div className="text-gray-500">총 러닝</div>
          <div className="font-medium">{metersToKm(data.totalDist)}</div>
        </div>
        <div>
          <div className="text-gray-500">최장 거리</div>
          <div className="font-medium">{metersToKm(data.longestDist)}</div>
        </div>
        <div>
          <div className="text-gray-500">평균 페이스</div>
          <div className="font-medium">{formatPace(data.avgPace)}</div>
        </div>
        <div>
          <div className="text-gray-500">최고 기록</div>
          <div className="font-medium">{formatPace(data.bestPace)}</div>
        </div>
        <div>
          <div className="text-gray-500">잠입</div>
          <div className="font-medium">{data.totalEntryCnt}회</div>
        </div>
      </div>
      {/* <div className="space-y-1 px-4 pb-4 text-sm">
        <div>총 러닝 : 2142km</div>
        <div>최장 거리 : 10km</div>
        <div>평균 페이스 : 8m 21s</div>
        <div>최고 기록 : 6m 34s</div>
        <div>잠입 : 3423회</div>
      </div>*/}
    </div>
  );
};
