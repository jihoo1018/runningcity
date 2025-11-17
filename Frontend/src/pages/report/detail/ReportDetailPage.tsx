// src/pages/report/detail/ReportDetailPage.tsx

import { useMemo } from "react";
import { useParams } from "react-router-dom";
import { useReportDetailQuery } from "@/entities/report/api/useReportDetailQuery";
import ReportRouteMap from "@/entities/report/ui/ReportRouteMap";
import { metersToKm, formatPace, formatDuration } from "@/shared/lib/format";
import { CommonButton } from "@/shared/ui";
import { useRegenerateAiReport } from "@/entities/report/api/useRegenerateAiReport";

export default function ReportDetailPage() {
  const { sid } = useParams();
  const sessionId = Number(sid);
  const { data, isLoading, isError } = useReportDetailQuery(sessionId);
  const { mutate: regenerateAiReport, isPending: isRegenerating } =
    useRegenerateAiReport(sessionId);

  const s = data?.summary;
  const hasAiReport = !!data?.aiReport?.content;

  const bigDistance = useMemo(
    () => (s?.totalDistance ? (s.totalDistance / 1000).toFixed(1) : "-"),
    [s?.totalDistance],
  );

  if (isLoading) return <div className="p-4">불러오는 중…</div>;
  if (isError || !data) return <div className="p-4 text-red-600">기록을 불러오지 못했습니다.</div>;

  return (
    <div className="mx-auto max-w-md p-4">
      <div className="mb-1 text-sm text-gray-500">기록 상세</div>

      <div className="flex items-end gap-2">
        <div className="text-4xl font-bold">
          {bigDistance}
          <span className="text-2xl">km</span>
        </div>
        <span className="rounded-full border px-2 py-1 text-xs">
          {data.type === "ENTRY" ? "기지 잠입" : "에너지"}
        </span>
      </div>

      <div className="mt-4 grid grid-cols-3 gap-3 border-t pt-3 text-sm">
        <div>
          <div className="text-gray-500">평균 페이스</div>
          <div className="font-medium">{formatPace(s?.avgPace)}</div>
        </div>
        <div>
          <div className="text-gray-500">시간</div>
          <div className="font-medium">{formatDuration(s?.duration)}</div>
        </div>
        <div>
          <div className="text-gray-500">칼로리</div>
          <div className="font-medium">{s?.totalCalories ?? "-"}</div>
        </div>
        <div>
          <div className="text-gray-500">고도 상승</div>
          <div className="font-medium">{s?.elevation ?? 0}</div>
        </div>
        <div>
          <div className="text-gray-500">평균 심박</div>
          <div className="font-medium">{s?.avgHeartRate ?? 0}</div>
        </div>
        <div>
          <div className="text-gray-500">케이던스</div>
          <div className="font-medium">{s?.avgCadence ?? 0}</div>
        </div>
      </div>

      <div className="mt-4">
        <ReportRouteMap geojson={data.route?.geojson} height={260} />
      </div>

      {/* AI Report: 줄글 그대로 렌더링 */}
      <div className="mt-6 rounded border p-4">
        <div className="mb-2 text-sm font-semibold">AI Report</div>
        {hasAiReport ? (
          <p className="text-content leading-relaxed whitespace-pre-line">
            {data.aiReport!.content}
          </p>
        ) : (
          <div className="text-content flex flex-col gap-4">
            <p className="text-content">아직 생성된 AI Report가 없어요.</p>
            <CommonButton
              variant="outline"
              onClick={() => regenerateAiReport()}
              disabled={isRegenerating}
            >
              {isRegenerating ? "생성 중..." : "생성 요청하기"}
            </CommonButton>
          </div>
        )}
      </div>

      <div className="mt-4">
        <div className="mb-2 text-base font-semibold">획득 보상</div>
        <div className="grid grid-cols-2 gap-2">
          <div className="rounded border p-3">
            <div className="text-sm text-gray-500">크레딧</div>
            <div className="text-lg font-bold">{data.rewards?.credit ?? 0}</div>
          </div>
          <div className="rounded border p-3">
            <div className="text-sm text-gray-500">경험치</div>
            <div className="text-lg font-bold">{data.rewards?.exp ?? 0}</div>
          </div>
        </div>
      </div>
    </div>
  );
}
